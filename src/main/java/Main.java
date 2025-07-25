import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import console.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 2010; // 서버 포트

        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 클라이언트 접속 처리 스레드 그룹
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 실제 데이터 처리 스레드 그룹

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder(CharsetUtil.UTF_8)); // inbound String 변환
                            p.addLast(new StringEncoder(CharsetUtil.UTF_8)); // outbound String 변환
                            p.addLast(new MainConsole()); // 커스텀 핸들러 등록
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println("[Netty] Server started on port " + port);

            // 서버 종료 대기
            f.channel().closeFuture().sync();
        } finally {
            // 종료 시 리소스 해제
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
