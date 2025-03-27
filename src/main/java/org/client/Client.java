package org.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {
    private final String host;
    private final int port;
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                    public void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline().addLast(
                                new StringDecoder(), new StringEncoder(),new ClientHandler());
                    }
                    });
            ChannelFuture f = b.connect().sync();

            Channel channel = f.channel();
            System.out.println("Client started on port " + port + " with host "+host);
            Scanner scanner =  new Scanner(System.in);
            while(true) {
                String command = scanner.nextLine();
                if ("exit".equalsIgnoreCase(command)) {
                    System.out.println("Exiting...");
                    channel.close().sync();
                    break;
                }
                channel.writeAndFlush(command);
            }
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }


    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + Client.class.getSimpleName() +
                            " <host> <port>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        new Client(host, port).start();
    }

}
