package org.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.commandPattern.clientCommands.ClientCommandFactory;

import java.net.InetSocketAddress;

public class Server {
    private final int port;
    private final ClientCommandFactory clientCommandFactory;

    public Server(int port, ClientCommandFactory clientCommandFactory) {
        this.port = port;
        this.clientCommandFactory = clientCommandFactory;

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                    "Usage: " + Server.class.getSimpleName() +
                            " <port>");
        }
        int port = Integer.parseInt(args[0]);

        new Server(port, new ClientCommandFactory(new ServerData())).start();
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder());
                            ch.pipeline().addLast("serverHandler", new ServerHandler(clientCommandFactory));
                        }
                    });
            ChannelFuture f = b.bind().sync();
            System.out.println("Server started on port " + port);
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
