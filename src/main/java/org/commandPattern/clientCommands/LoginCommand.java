package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.UserAttributes;

public class LoginCommand implements Command {


    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (args.length < 1 || !args[0].startsWith("-u=")) {
            ctx.writeAndFlush("Define username with parameter -u= ");
            return;
        }
        String username = null;
        if (args[0].startsWith("-u=")) {
            username = args[0].substring(3); // Извлекаем значение после -u=
        }

        if (!username.equals("")) {
            ctx.channel().attr(UserAttributes.USERNAME).set(username);
            ctx.writeAndFlush("Logged successfully! Welcome, " + username + "!");
        } else {
            ctx.writeAndFlush("Empty username. Login failed.");
        }
    }
}
