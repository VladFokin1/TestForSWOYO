package org.commandPattern;

import io.netty.channel.ChannelHandlerContext;

public interface Command {
    void execute(String[] args, ChannelHandlerContext ctx);
}
