package org.commandPattern.clientCommands;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.server.ServerData;
import org.server.UserAttributes;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteVoteCommandTest {

    @Mock
    private ServerData serverData;

    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    @Mock
    private Attribute<String> usernameAttribute;

    private DeleteVoteCommand command;

    @Before
    public void setUp() {
        // Настраиваем цепочку вызовов для ctx
        when(ctx.channel()).thenReturn(channel);
        when(channel.attr(UserAttributes.USERNAME)).thenReturn(usernameAttribute);
        when(usernameAttribute.get()).thenReturn("testUser");

        command = new DeleteVoteCommand(serverData);
    }

    @Test
    public void testExecute_WithValidParameters_DeletesVote() {
        // Arrange
        String[] args = {"-t=testTopic", "-v=testVote"};
        when(serverData.deleteVote("testTopic", "testVote", "testUser"))
                .thenReturn("Vote testVote deleted!");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).deleteVote("testTopic", "testVote", "testUser");
        verify(ctx).writeAndFlush("Vote testVote deleted!");
    }

    @Test
    public void testExecute_WithInsufficientParameters_ShowsUsage() {
        // Arrange
        String[] args1 = {"-t=testTopic"}; // Не хватает -v
        String[] args2 = {"-v=testVote"};  // Не хватает -t
        String[] args3 = {};               // Нет параметров

        // Act & Assert
        command.execute(args1, ctx);
        verify(ctx).writeAndFlush("Usage of command: delete -t=<topic> -v=<vote>");

        command.execute(args2, ctx);
        verify(ctx, times(2)).writeAndFlush("Usage of command: delete -t=<topic> -v=<vote>");

        command.execute(args3, ctx);
        verify(ctx, times(3)).writeAndFlush("Usage of command: delete -t=<topic> -v=<vote>");
    }

    @Test
    public void testExecute_WithMalformedParameters_ShowsUsage() {
        // Arrange
        String[] args1 = {"wrong=testTopic", "-v=testVote"}; // Неправильный формат -t
        String[] args2 = {"-t=testTopic", "wrong=testVote"}; // Неправильный формат -v

        // Act & Assert
        command.execute(args1, ctx);
        verify(ctx).writeAndFlush("Usage of command: delete -t=<topic> -v=<vote>");

        command.execute(args2, ctx);
        verify(ctx, times(2)).writeAndFlush("Usage of command: delete -t=<topic> -v=<vote>");
    }

    @Test
    public void testExecute_WhenUserNotCreator_ShowsError() {
        // Arrange
        String[] args = {"-t=testTopic", "-v=testVote"};
        when(serverData.deleteVote("testTopic", "testVote", "testUser"))
                .thenReturn("You can not delete this vote!");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).deleteVote("testTopic", "testVote", "testUser");
        verify(ctx).writeAndFlush("You can not delete this vote!");
    }

    @Test
    public void testExecute_WhenTopicNotExists_ShowsError() {
        // Arrange
        String[] args = {"-t=nonexistent", "-v=testVote"};
        when(serverData.deleteVote("nonexistent", "testVote", "testUser"))
                .thenReturn("There is no topic with name nonexistent");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).deleteVote("nonexistent", "testVote", "testUser");
        verify(ctx).writeAndFlush("There is no topic with name nonexistent");
    }

    @Test
    public void testExecute_WhenVoteNotExists_ShowsError() {
        // Arrange
        String[] args = {"-t=testTopic", "-v=nonexistent"};
        when(serverData.deleteVote("testTopic", "nonexistent", "testUser"))
                .thenReturn("There is no vote with name nonexistent in topic testTopic");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).deleteVote("testTopic", "nonexistent", "testUser");
        verify(ctx).writeAndFlush("There is no vote with name nonexistent in topic testTopic");
    }
}
