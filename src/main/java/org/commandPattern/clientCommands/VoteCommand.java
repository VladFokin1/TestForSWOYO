package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;
import org.server.UserAttributes;

import java.util.ArrayList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoteCommand extends DialogCommand {

    private final ServerData serverData;
    private Map<ChannelHandlerContext, VoteContext> voteContexts;
    public VoteCommand(ServerData serverData) {
        this.serverData = serverData;
        voteContexts = new ConcurrentHashMap<>();
    }


    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (!voteContexts.containsKey(ctx)) {
            if (args.length < 2 || !args[0].startsWith("-t=") || !args[1].startsWith("-v=")) {
                ctx.writeAndFlush("Usage of command: vote -t=<topic> -v=<vote>");
                finishDialog(ctx);
                voteContexts.remove(ctx);
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
                VoteContext voteContext= new VoteContext(topicName, voteName);
                voteContexts.put(ctx, voteContext);

            } catch (Exception e) {
                answer.append(e.getMessage());
                ctx.writeAndFlush(answer);
                finishDialog(ctx);
                voteContexts.remove(ctx);
            }

        } else {
            handleNextStep(ctx, args);
        }

    }

    @Override
    protected void handleNextStep(ChannelHandlerContext ctx, String[] args) {
        String userInput = String.join(" ", args);


        //проверка на число
        if (!userInput.matches("\\d+")) {
            ctx.writeAndFlush("Enter NUMBER of option!");
            return;
        }
        VoteContext voteContext = voteContexts.get(ctx);

        String[] voteOptionNames;
        try {
            voteOptionNames = serverData.getVoteOptionsNames(voteContext.topicName, voteContext.voteName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //проверка на правильность ввода числа
        if (Integer.parseInt(userInput) > voteOptionNames.length) {
            ctx.writeAndFlush("Choosen option number does not exist");
            return;
        }

        String answer = serverData.vote(
                voteContext.topicName,
                voteContext.voteName,
                voteOptionNames[Integer.parseInt(userInput)],
                ctx.channel().attr(UserAttributes.USERNAME).get()
        );
        ctx.writeAndFlush(answer);
        finishDialog(ctx);
        voteContexts.remove(ctx);
    }

    private static class VoteContext {
        private String topicName;
        private String voteName;

        public VoteContext(String topicName, String voteName) {
            this.topicName = topicName;
            this.voteName = voteName;
        }
    }
}
