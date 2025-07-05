package com.example.demo;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(DemoApplication.class, args);
               // SSL 컨텍스트 준비
        SslContext sslCtx = SslContextBuilder
                .forServer(
                        new File("server-cert.pem"),
                        new File("server-key.pem")
                ).build();

        // 이벤트 루프 그룹
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // accept 전용
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // read/write 전용

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     // SSL 핸들러
                     p.addLast(sslCtx.newHandler(ch.alloc()));
                     // 데이터 수신/송신 핸들러
                     p.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                             // 받은 내용 출력
                             System.out.println("received: " + msg.toString(StandardCharsets.UTF_8));

                             // echo
                             ctx.writeAndFlush(msg.copy());
                         }

                         @Override
                         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                             cause.printStackTrace();
                             ctx.close();
                         }
                     });
                 }
             });

            ChannelFuture f = b.bind(2020).sync(); // 8443 포트 TLS
            System.out.println("Netty HTTPS server started on port 8443");

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
