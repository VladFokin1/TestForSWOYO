package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;
import org.server.ServerHandler;
import org.server.UserAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CreateVoteCommand extends DialogCommand {

    private Map<ChannelHandlerContext, VoteCreationContext> voteCreationContexts;
    private ServerData serverData;

    public CreateVoteCommand(ServerData serverData) {
        this.serverData = serverData;
        voteCreationContexts = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (!voteCreationContexts.containsKey(ctx)) {
            if (args.length < 1 || !args[0].startsWith("-t=")) {
                ctx.writeAndFlush("Define theme with parameter -t=");
                voteCreationContexts.remove(ctx);
                finishDialog(ctx);
                return;
            }
            String topicName = null;
            for (String arg : args) {
                if (arg.startsWith("-t=")) {
                    topicName = arg.substring(3);
                }
            }
            if (!serverData.viewTopics().contains(topicName)) {
                ctx.writeAndFlush("There is no topic with name " + topicName);
                voteCreationContexts.remove(ctx);
                finishDialog(ctx);
                return;
            }
            VoteCreationContext context = new VoteCreationContext();
            context.topic = topicName;
            voteCreationContexts.put(ctx, context);

            ctx.writeAndFlush("Enter vote name:");
        } else {
            handleNextStep(ctx, args);
        }
    }

    @Override
    protected void handleNextStep(ChannelHandlerContext ctx, String[] args) {
        VoteCreationContext context = voteCreationContexts.get(ctx);
        String userInput = String.join(" ", args);

        switch (context.currentStep) {
            case NAME:
                context.voteName = userInput;
                context.currentStep = Step.DESCRIPTION;
                ctx.writeAndFlush("Enter vote description:");
                break;

            case DESCRIPTION:
                context.description = userInput;
                context.currentStep = Step.OPTION_COUNT;
                ctx.writeAndFlush("Enter the number of possible options:");
                break;

            case OPTION_COUNT:
                try {
                    int count = Integer.parseInt(userInput);
                    context.optionCount = count;
                    context.currentStep = Step.OPTIONS;
                    ctx.writeAndFlush("Enter option 1:");
                } catch (NumberFormatException e) {
                    ctx.writeAndFlush("Error! Enter number");
                }
                break;

            case OPTIONS:
                context.options.add(userInput);
                if (context.options.size() < context.optionCount) {
                    ctx.writeAndFlush(String.format("Enter option %d:",
                            context.options.size() + 1));
                } else {
                    finishCreation(ctx);
                }
                break;
        }
    }

    void finishCreation(ChannelHandlerContext ctx) {
        VoteCreationContext context = voteCreationContexts.remove(ctx);
        Map<String, Integer> optionsMap = new ConcurrentHashMap<>();
        for (String option : context.options) {
            optionsMap.put(option, 0);
        }
        serverData.createVoting(context.topic, context.voteName, context.description, optionsMap, ctx.channel().attr(UserAttributes.USERNAME).get());
        ctx.writeAndFlush(String.format(
                "Vote '%s' created! Options: %s",
                context.voteName,
                String.join(", ", context.options)
        ));
        finishDialog(ctx);
    }

    public Map<ChannelHandlerContext, VoteCreationContext> getVoteCreationContexts() {
        return voteCreationContexts;
    }


    public static class VoteCreationContext {
        public String topic;
        public String voteName;
        public String description;
        public int optionCount;
        public final List<String> options = new ArrayList<>();
        public Step currentStep = Step.NAME;
    }



    public enum Step {
        NAME, DESCRIPTION, OPTION_COUNT, OPTIONS
    }
}
