package model;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class identityServerConnector {

    private final String serverHost = "localhost";      // 인증 서버 주소
    private final int serverPort = 2010;                // 인증 서버 포트
    private final String certPath = "server-cert.pem";  // 서버 인증서 경로

    private CompletableFuture<String> responseFuture;
    // 토큰 요청 메서드
    public Map<String, String> requestTokensFromExternalServer(Map<String, String> paramMap) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        int maxRetryCount = 3;                 // 최대 재시도 횟수
        int retryDelayMillis = 1000;
        while (attempt < maxRetryCount) {
            try {
                responseFuture = new CompletableFuture<>();
                SslContext sslCtx = buildSslContext(certPath);

                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) {
                                    ChannelPipeline p = ch.pipeline();
                                    p.addLast(sslCtx.newHandler(ch.alloc(), serverHost, serverPort));
                                    p.addLast(new LineBasedFrameDecoder(8192));  // must be before StringDecoder
                                    p.addLast(new StringDecoder(StandardCharsets.UTF_8));
                                    p.addLast(new StringEncoder(StandardCharsets.UTF_8));
                                    p.addLast(new ClientHandler());
                                }
                            });

                    ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();

                    // 로그인 명령 생성 (반드시 "\n" 포함)
                    StringBuilder commandBuilder = new StringBuilder();
                    commandBuilder.append("LOGIN%");
                    if (paramMap.containsKey("id")) {
                        commandBuilder.append("&id$").append(paramMap.get("id")).append("&");
                    }
                    if (paramMap.containsKey("password")) {
                        commandBuilder.append("password$").append(paramMap.get("password"));
                    }
                    commandBuilder.append("%");

                    String command = commandBuilder.toString();
                    System.out.println("[DEBUG] Sending login command: " + command);

                    // 반드시 개행문자 포함해서 전송
                    future.channel().writeAndFlush(command + "\n");

                    // 응답 대기
                    String response = responseFuture.get();

                    System.out.println("[DEBUG] Received response: " + response);

                    return parseRefreshToken(response);

                } finally {
                    group.shutdownGracefully();
                }
            } catch (Exception e) {
                lastException = e;
                attempt++;
                System.out.println("[WARN] requestTokensFromExternalServer attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetryCount) {
                    Thread.sleep(retryDelayMillis);
                    System.out.println("[INFO] Retrying connection...");
                }
            }
        }
        throw lastException;  // 최대 재시도 실패시 마지막 예외 던짐
    }

    // 공개키 요청 메서드 (줄바꿈 포함 전송 + 파이프라인 동일 구성)
    public String requestPublicKeyFromExternalServer() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        SslContext sslCtx = buildSslContext(certPath);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(sslCtx.newHandler(ch.alloc(), serverHost, serverPort));
                            p.addLast(new LineBasedFrameDecoder(8192));  // must be before StringDecoder
                            p.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            p.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            p.addLast(new SimpleChannelInboundHandler<String>() {
                                private final StringBuilder receivedData = new StringBuilder();
                                private boolean completed = false;

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                    // 받은 한 줄 메시지를 누적
                                    receivedData.append(msg).append("\n");

                                    // 종료 조건 예: PEM 키가 끝나는 부분 포함하면 완료
                                    if (msg.contains("-----END PUBLIC KEY-----")) {
                                        if (!completed) {
                                            completed = true;
                                            // 완료된 PEM 문자열 전달
                                            future.complete(receivedData.toString());
                                            // 여기서 ctx.close() 안 해도 됨, 서버가 닫으면 닫힘
                                        }
                                    }
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    if (!completed) {
                                        completed = true;
                                        future.completeExceptionally(cause);
                                    }
                                    ctx.close();
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    // 서버가 채널 닫으면 만약 완료 안 됐으면 완료 처리 (예외 상황 대비)
                                    if (!completed) {
                                        completed = true;
                                        future.complete(receivedData.toString());
                                    }
                                    super.channelInactive(ctx);
                                }
                            });
                        }
                    });

            ChannelFuture futureConnect = bootstrap.connect(serverHost, serverPort).sync();

            // 반드시 "\n" 포함
            String requestMsg = "GET_REFRESH_PUBLIC_KEY%%\n";
            futureConnect.channel().writeAndFlush(requestMsg);

            String response = future.get();
            System.out.println("[DEBUG] Received public key response: " + response);

// ✳ escape 된 \n 을 실제 줄바꿈으로 변환
            String fixedResponse = response.replace("\\n", "\n");

// ✳ 접두사 제거 (서버가 이렇게 보냄: refPublicKey%&refKey$... )
            if (fixedResponse.startsWith("refPublicKey%&refKey$")) {
                fixedResponse = fixedResponse.substring("refPublicKey%&refKey$".length());
            }

            System.out.println("[DEBUG] Fixed public key PEM:\n" + fixedResponse);

            return fixedResponse;

        } finally {
            group.shutdownGracefully();
        }
    }

    // SSLContext 빌드
    private SslContext buildSslContext(String certPath) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        return SslContextBuilder.forClient()
                .trustManager(new FileInputStream(certPath))
                .build();
    }

    // 응답 파싱
    private Map<String, String> parseRefreshToken(String response) {
        Map<String, String> result = new HashMap<>();

        if (response == null || !response.startsWith("login%") || !response.contains("refreshToken$")) {
            System.out.println("[ERROR] Invalid or missing refreshToken in response");
            return result;
        }

        String[] parts = response.split("&");
        for (String part : parts) {
            if (part.startsWith("refreshToken$")) {
                result.put("refreshToken", part.substring("refreshToken$".length()));
            }
        }

        if (!result.isEmpty()) {
            System.out.println("[INFO] Successfully extracted refreshToken");
        }
        return result;
    }

    // 기존 핸들러 (토큰 요청용)
    private class ClientHandler extends SimpleChannelInboundHandler<String> {
        private boolean completed = false;  // 중복완료 방지
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            if (!completed) {
                responseFuture.complete(msg);
                completed = true;
            }
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (!completed) {
                responseFuture.completeExceptionally(cause);
                completed = true;
            }
            ctx.close();
        }
    }

    // 로그인 메서드
    public static String login(String id, String password) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("id", id);
        paramMap.put("password", password);

        System.out.println("[로그인 시도] 입력 데이터: " + paramMap);
        if (!paramMap.containsKey("id") || !paramMap.containsKey("password")) {
            System.out.println("[로그인 실패] id 또는 password 누락");
            return null;
        }

        identityServerConnector authServiceServer = new identityServerConnector();

        try {
            // 1. 공개키 먼저 요청 (필수: 서버가 줄바꿈 포함 응답해야 정상작동)
            String publicKeyPEM = authServiceServer.requestPublicKeyFromExternalServer();
            System.out.println("[INFO] Received public key:\n" + publicKeyPEM);

            // 2. 로그인 및 토큰 요청
            Map<String, String> tokenSet = authServiceServer.requestTokensFromExternalServer(paramMap);
            System.out.println("[INFO] Received tokens: " + tokenSet);

            return tokenSet.get("refreshToken");

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to request tokens from identity server");
            e.printStackTrace();
            return null;
        }
    }
}
