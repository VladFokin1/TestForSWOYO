package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerHandler;

public abstract class DialogCommand implements Command{
    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) { }

    public void finishDialog(ChannelHandlerContext ctx) {
        ((ServerHandler) ctx.pipeline().get("serverHandler")).clearActiveDialog(ctx);
    }
}
