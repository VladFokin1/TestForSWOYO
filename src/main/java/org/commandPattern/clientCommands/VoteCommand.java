package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;

import java.util.Arrays;

public class VoteCommand implements Command {

    private final ServerData serverData;

    public VoteCommand(ServerData serverData) {
        this.serverData = serverData;
    }


    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (args.length < 2 || !args[0].startsWith("-t=") || !args[1].startsWith("-v=")) {
            ctx.writeAndFlush("Usage of command: vote -t=<topic> -v=<vote>");
            return;
        }
            String topicName = args[0].substring("-t=".length());
            String voteName = args[1].substring("-v=".length());

            String[] options = serverData.getVoteOptionsNames(topicName, voteName);
            StringBuilder answer = new StringBuilder();
            answer.append("Options in this vote:\n");
            for (int i = 0; i < options.length; i++) {
                answer.append(i).append(". ").append(options[i]).append("\n");
            }


    }
}
