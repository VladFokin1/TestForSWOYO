package org.commandPattern.clientCommands;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Attribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.server.ServerData;
import org.server.ServerHandler;
import org.server.UserAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateVoteCommandTest {

    @Mock
    private ServerData serverData;

    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    @Mock
    private Attribute<String> usernameAttribute;

    @Mock
    private ChannelPipeline pipeline;

    @Mock
    private ServerHandler serverHandler;

    private CreateVoteCommand command;

    @Before
    public void setUp() {
        // Настраиваем цепочку вызовов для ctx
        when(ctx.channel()).thenReturn(channel);
        when(channel.attr(UserAttributes.USERNAME)).thenReturn(usernameAttribute);
        when(usernameAttribute.get()).thenReturn("testUser");
        when(ctx.pipeline()).thenReturn(pipeline);
        when(pipeline.get("serverHandler")).thenReturn(serverHandler);

        command = new CreateVoteCommand(serverData);
    }

    // Все остальные тесты остаются без изменений
    @Test
    public void testExecute_WithoutTopicParameter_ShowsError() {
        String[] args = {"invalidArg"};
        command.execute(args, ctx);
        verify(ctx).writeAndFlush("Define theme with parameter -t=");
    }

    @Test
    public void testExecute_WithNonExistingTopic_ShowsError() {
        String[] args = {"-t=nonexistent"};
        when(serverData.viewTopics()).thenReturn("existingTopic (votes in topic=0)\n");
        command.execute(args, ctx);
        verify(ctx).writeAndFlush("There is no topic with name nonexistent");
    }
    @Test
    public void testExecute_WithValidTopic_StartsDialog() {
        // Arrange
        String[] args = {"-t=existingTopic"};
        when(serverData.viewTopics()).thenReturn("existingTopic (votes in topic=0)\n");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(ctx).writeAndFlush("Enter vote name:");
        assert(command.getVoteCreationContexts().containsKey(ctx));
    }

    @Test
    public void testHandleNextStep_NameStep_ProceedsToDescription() {
        // Arrange
        CreateVoteCommand.VoteCreationContext context = new CreateVoteCommand.VoteCreationContext();
        context.currentStep = CreateVoteCommand.Step.NAME;
        command.getVoteCreationContexts().put(ctx, context);
        String[] args = {"TestVote"};

        // Act
        command.handleNextStep(ctx, args);

        // Assert
        verify(ctx).writeAndFlush("Enter vote description:");
        assert(context.voteName.equals("TestVote"));
        assert(context.currentStep == CreateVoteCommand.Step.DESCRIPTION);
    }

    @Test
    public void testHandleNextStep_DescriptionStep_ProceedsToOptionCount() {
        // Arrange
        CreateVoteCommand.VoteCreationContext context = new CreateVoteCommand.VoteCreationContext();
        context.currentStep = CreateVoteCommand.Step.DESCRIPTION;
        command.getVoteCreationContexts().put(ctx, context);
        String[] args = {"Test description"};

        // Act
        command.handleNextStep(ctx, args);

        // Assert
        verify(ctx).writeAndFlush("Enter the number of possible options:");
        assert(context.description.equals("Test description"));
        assert(context.currentStep == CreateVoteCommand.Step.OPTION_COUNT);
    }

    @Test
    public void testHandleNextStep_OptionCountStepWithInvalidNumber_ShowsError() {
        // Arrange
        CreateVoteCommand.VoteCreationContext context = new CreateVoteCommand.VoteCreationContext();
        context.currentStep = CreateVoteCommand.Step.OPTION_COUNT;
        command.getVoteCreationContexts().put(ctx, context);
        String[] args = {"invalid"};

        // Act
        command.handleNextStep(ctx, args);

        // Assert
        verify(ctx).writeAndFlush("Error! Enter number");
        assert(context.currentStep == CreateVoteCommand.Step.OPTION_COUNT);
    }

    @Test
    public void testHandleNextStep_OptionCountStep_ProceedsToOptions() {
        // Arrange
        CreateVoteCommand.VoteCreationContext context = new CreateVoteCommand.VoteCreationContext();
        context.currentStep = CreateVoteCommand.Step.OPTION_COUNT;
        command.getVoteCreationContexts().put(ctx, context);
        String[] args = {"3"};

        // Act
        command.handleNextStep(ctx, args);

        // Assert
        verify(ctx).writeAndFlush("Enter option 1:");
        assert(context.optionCount == 3);
        assert(context.currentStep == CreateVoteCommand.Step.OPTIONS);
    }

    @Test
    public void testHandleNextStep_OptionsStep_CollectsAllOptions() {
        // Arrange
        CreateVoteCommand.VoteCreationContext context = new CreateVoteCommand.VoteCreationContext();
        context.currentStep = CreateVoteCommand.Step.OPTIONS;
        context.optionCount = 2;
        command.getVoteCreationContexts().put(ctx, context);

        // First option
        String[] args1 = {"Option1"};
        command.handleNextStep(ctx, args1);

        // Second option
        String[] args2 = {"Option2"};
        command.handleNextStep(ctx, args2);

        // Assert
        verify(ctx).writeAndFlush("Enter option 2:");
        verify(ctx).writeAndFlush(String.format(
                "Vote '%s' created! Options: %s",
                context.voteName,
                "Option1, Option2"
        ));
        assert(context.options.size() == 2);
        assert(context.options.get(0).equals("Option1"));
        assert(context.options.get(1).equals("Option2"));
    }

    @Test
    public void testFinishCreation_CreatesVoteCorrectly() {
        // Arrange
        CreateVoteCommand.VoteCreationContext context = new CreateVoteCommand.VoteCreationContext();
        context.topic = "testTopic";
        context.voteName = "testVote";
        context.description = "testDescription";
        context.options.add("Option1");
        context.options.add("Option2");
        command.getVoteCreationContexts().put(ctx, context);

        //ServerHandler serverHandler = mock(ServerHandler.class);
       // when(pipeline.get(ServerHandler.class)).thenReturn(serverHandler);

        // Act
        command.finishCreation(ctx);

        // Assert
        verify(serverData).createVoting(
                eq("testTopic"),
                eq("testVote"),
                eq("testDescription"),
                any(Map.class),
                eq("testUser")
        );
        verify(ctx).writeAndFlush("Vote 'testVote' created! Options: Option1, Option2");
        assertFalse(command.getVoteCreationContexts().containsKey(ctx));
    }

    // Вспомогательный метод для доступа к приватному полю (в тестовых целях)
    private Map<ChannelHandlerContext, CreateVoteCommand.VoteCreationContext> getVoteCreationContexts() {
        return command.getVoteCreationContexts();
    }
}