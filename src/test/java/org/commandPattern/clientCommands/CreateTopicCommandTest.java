package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.server.ServerData;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateTopicCommandTest {

    @Mock
    private ServerData serverData;

    @Mock
    private ChannelHandlerContext ctx;

    private CreateTopicCommand command;

    @Before
    public void setUp() {
        command = new CreateTopicCommand(serverData);
    }

    @Test
    public void testExecute_WithValidTopicName_CreatesTopic() {
        // Arrange
        String[] args = {"-n=testTopic"};
        when(serverData.createTopic("testTopic")).thenReturn(true);

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).createTopic("testTopic");
        verify(ctx).writeAndFlush("Topic created with name: testTopic");
    }

    @Test
    public void testExecute_WithExistingTopicName_ReturnsErrorMessage() {
        // Arrange
        String[] args = {"-n=existingTopic"};
        when(serverData.createTopic("existingTopic")).thenReturn(false);

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).createTopic("existingTopic");
        verify(ctx).writeAndFlush("Topic already exist or name is not provided: existingTopic");
    }

    @Test
    public void testExecute_WithoutNameParameter_ReturnsErrorMessage() {
        // Arrange
        String[] args = {"invalidArgument"};

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData, never()).createTopic(any());
        verify(ctx).writeAndFlush("Define topic name with -n=");
    }

    @Test
    public void testExecute_WithEmptyName_ReturnsErrorMessage() {
        // Arrange
        String[] args = {"-n="};
        when(serverData.createTopic("")).thenReturn(false);

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).createTopic("");
        verify(ctx).writeAndFlush("Topic already exist or name is not provided: ");
    }

    @Test
    public void testExecute_WithMultipleArgs_ParsesCorrectly() {
        // Arrange
        String[] args = {"-n=testTopic", "extraArg1", "extraArg2"};
        when(serverData.createTopic("testTopic")).thenReturn(true);

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).createTopic("testTopic");
        verify(ctx).writeAndFlush("Topic created with name: testTopic");
    }

}
