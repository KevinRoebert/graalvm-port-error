package eu.roebert;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

public final class Main {
    private static final int PORT = Integer.parseInt(System.getProperty("port", "55275"));

    public static void main(final String[] args) {
        final EventLoopGroup group = new NioEventLoopGroup();

        try {
            final Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(final ChannelHandlerContext ctx, final DatagramPacket packet) {
                            final ByteBuf copy = packet.content().copy();
                            final String msg = packet.content().toString(CharsetUtil.UTF_8);

                            System.out.println("Received message: " + msg);

                            if (msg.contains("exit")) {
                                ctx.channel().close();
                            } else {
                                ctx.writeAndFlush(new DatagramPacket(copy, packet.sender()));
                            }
                        }
                    });

            final ChannelFuture f = b.bind("0.0.0.0", PORT).sync();
            f.channel().closeFuture().sync();
        } catch (final InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            group.shutdownGracefully();
        }
    }
}


