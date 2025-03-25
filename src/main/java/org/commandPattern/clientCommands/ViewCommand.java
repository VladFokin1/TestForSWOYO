package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;

public class ViewCommand implements Command {
    private ServerData serverData;

    public ViewCommand(ServerData serverData) {
        this.serverData = serverData;
    }

    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (args.length < 1 || args[0].isEmpty()) {
            ctx.writeAndFlush(serverData.viewTopics());
        } else {
            if (args[0].startsWith("-t=") && args.length == 1) {
                String topicName = args[0].substring("-t=".length());
                ctx.writeAndFlush(serverData.viewVotesInTopic(topicName));
            }
        }
    }


}
