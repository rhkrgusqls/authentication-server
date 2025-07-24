package console;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import controller.ParsingController;

public class MainConsole extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("[Netty] Client connected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String fullCommand = (String) msg;
            System.out.println("[Netty] Received: " + fullCommand);

            // ✳ ParsingController 로직 호출
            String response = ParsingController.controllerHandle(fullCommand);

            // ✳ 응답 전송
            ctx.writeAndFlush(Unpooled.copiedBuffer(response + "\n", CharsetUtil.UTF_8));
        } catch (Exception e) {
            String errorMsg = "error%ExceptionOccurred: " + e.getMessage();
            ctx.writeAndFlush(Unpooled.copiedBuffer(errorMsg + "\n", CharsetUtil.UTF_8));
            e.printStackTrace();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("[Netty] Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("[Netty] Error: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
