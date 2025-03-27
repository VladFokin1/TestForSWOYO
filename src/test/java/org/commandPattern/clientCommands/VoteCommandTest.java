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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class VoteCommandTest {

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

    private VoteCommand command;

    @Before
    public void setUp() {
        // Настраиваем цепочку mock-объектов
        when(ctx.channel()).thenReturn(channel);
        when(channel.attr(UserAttributes.USERNAME)).thenReturn(usernameAttribute);
        when(usernameAttribute.get()).thenReturn("testUser");
        when(ctx.pipeline()).thenReturn(pipeline);
        when(pipeline.get("serverHandler")).thenReturn(serverHandler);

        command = new VoteCommand(serverData);
    }

    @Test
    public void testExecute_WithValidParameters_StartsVotingProcess() throws Exception {
        // Arrange
        String[] args = {"-t=testTopic", "-v=testVote"};
        String[] options = {"Option1", "Option2"};
        when(serverData.getVoteOptionsNames("testTopic", "testVote")).thenReturn(options);

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).getVoteOptionsNames("testTopic", "testVote");
        verify(ctx).writeAndFlush(eq("Options in this vote:\n1. Option1\n2. Option2\nChoose number of option you want to vote:\n"));
        assertTrue(command.getVoteContexts().containsKey(ctx));
    }

    @Test
    public void testExecute_WithInvalidParameters_ShowsUsage() {
        // Arrange
        String[] args1 = {"-t=testTopic"}; // Не хватает -v
        String[] args2 = {"-v=testVote"};  // Не хватает -t
        String[] args3 = {};               // Нет параметров

        // Act & Assert
        command.execute(args1, ctx);
        verify(ctx).writeAndFlush("Usage of command: vote -t=<topic> -v=<vote>");

        command.execute(args2, ctx);
        verify(ctx, times(2)).writeAndFlush("Usage of command: vote -t=<topic> -v=<vote>");

        command.execute(args3, ctx);
        verify(ctx, times(3)).writeAndFlush("Usage of command: vote -t=<topic> -v=<vote>");
    }

    @Test
    public void testExecute_WhenVoteNotExists_ShowsError() throws Exception {
        // Arrange
        String[] args = {"-t=testTopic", "-v=invalidVote"};
        when(serverData.getVoteOptionsNames("testTopic", "invalidVote"))
                .thenThrow(new Exception("There is no vote invalidVote in topic testTopic"));

        // Act
        command.execute(args, ctx);

        // Assert
        verify(ctx).writeAndFlush("There is no vote invalidVote in topic testTopic");
        assertFalse(command.getVoteContexts().containsKey(ctx));
    }

    @Test
    public void testHandleNextStep_WithValidOptionNumber_CompletesVoting() throws Exception {
        // Arrange
        VoteCommand.VoteContext context = new VoteCommand.VoteContext("testTopic", "testVote");
        command.getVoteContexts().put(ctx, context);

        String[] options = {"Option1", "Option2"};
        when(serverData.getVoteOptionsNames("testTopic", "testVote")).thenReturn(options);
        when(serverData.vote("testTopic", "testVote", "Option1", "testUser"))
                .thenReturn("You voted for Option1 in vote testVote");

        // Act
        command.handleNextStep(ctx, new String[]{"1"}); // Нумерация с 1 для пользователя

        // Assert
        verify(serverData).vote("testTopic", "testVote", "Option1", "testUser");
        verify(ctx).writeAndFlush("You voted for Option1 in vote testVote");
        assertFalse(command.getVoteContexts().containsKey(ctx));
    }

    @Test
    public void testHandleNextStep_WithNonNumericInput_ShowsError() throws Exception {
        // Arrange
        VoteCommand.VoteContext context = new VoteCommand.VoteContext("testTopic", "testVote");
        command.getVoteContexts().put(ctx, context);

        String[] options = {"Option1", "Option2"};


        // Act
        command.handleNextStep(ctx, new String[]{"invalid"});

        // Assert
        verify(ctx).writeAndFlush("Enter NUMBER of option!");
        assertTrue(command.getVoteContexts().containsKey(ctx)); // Диалог продолжается
    }

    @Test
    public void testHandleNextStep_WithInvalidOptionNumber_ShowsError() throws Exception {
        // Arrange
        VoteCommand.VoteContext context = new VoteCommand.VoteContext("testTopic", "testVote");
        command.getVoteContexts().put(ctx, context);

        String[] options = {"Option1", "Option2"};
        when(serverData.getVoteOptionsNames("testTopic", "testVote")).thenReturn(options);

        // Act
        command.handleNextStep(ctx, new String[]{"3"}); // Несуществующий вариант

        // Assert
        verify(ctx).writeAndFlush("Choosen option number does not exist");
        assertTrue(command.getVoteContexts().containsKey(ctx)); // Диалог продолжается
    }

}
