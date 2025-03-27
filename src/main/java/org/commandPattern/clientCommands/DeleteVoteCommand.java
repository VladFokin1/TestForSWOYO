package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;
import org.server.UserAttributes;

public class DeleteVoteCommand implements Command {
    private ServerData serverData;

    public DeleteVoteCommand(ServerData serverData) {
        this.serverData = serverData;
    }

    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (args.length < 2 || !args[0].startsWith("-t=") || !args[1].startsWith("-v=")) {
            ctx.writeAndFlush("Usage of command: delete -t=<topic> -v=<vote>");
            return;
        }
        String topicName = args[0].trim().substring("-t=".length());
        String voteName = args[1].trim().substring("-v=".length());

        String answer = serverData.deleteVote(topicName, voteName, ctx.channel().attr(UserAttributes.USERNAME).get());
        ctx.writeAndFlush(answer);
    }
}
