package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoteCommand extends DialogCommand {

    private final ServerData serverData;
    private List<ChannelHandlerContext> voteContexts;
    public VoteCommand(ServerData serverData) {
        this.serverData = serverData;
        voteContexts = new ArrayList<>();
    }


    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (!voteContexts.contains(ctx)) {
            if (args.length < 2 || !args[0].startsWith("-t=") || !args[1].startsWith("-v=")) {
                ctx.writeAndFlush("Usage of command: vote -t=<topic> -v=<vote>");
                return;
            }
            String topicName = args[0].substring("-t=".length());
            String voteName = args[1].substring("-v=".length());

            StringBuilder answer = new StringBuilder();
            try {
                String[] options = serverData.getVoteOptionsNames(topicName, voteName);
                answer.append("Options in this vote:\n");
                for (int i = 0; i < options.length; i++) {
                    answer.append(i).append(". ").append(options[i]).append("\n");
                }
                answer.append("Choose number of option you want to vote:\n");
                ctx.writeAndFlush(answer);
                voteContexts.add(ctx);
                //handleNextStep(ctx, args);

            } catch (Exception e) {
                answer.append(e.getMessage());
                ctx.writeAndFlush(answer);
                finishDialog(ctx);
            }

        } else {
            handleNextStep(ctx, args);
        }

    }

    @Override
    protected void handleNextStep(ChannelHandlerContext ctx, String[] args) {
        String userInput = String.join(" ", args);
        
    }
}
