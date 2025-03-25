package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;

public class CreateTopicCommand implements Command {

    private ServerData serverData;

    public CreateTopicCommand(ServerData serverData) {
        this.serverData = serverData;
    }

    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (args.length < 1 || !args[0].startsWith("-n=")) {
            ctx.writeAndFlush("Define topic name with -n=");
            return;
        }

        String topicName = null;
        for (String arg : args) {
            if (arg.startsWith("-n=")) {
                topicName = arg.substring(3);
            }
        }
        boolean isCreated = serverData.createTopic(topicName);
        if (isCreated) {
            ctx.writeAndFlush("Topic created with name: " + topicName);
        } else {
            ctx.writeAndFlush("Topic already exist or name is not provided: " + topicName);
        }
    }
}
