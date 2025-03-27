package org.commandPattern.clientCommands;

import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.server.ServerData;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ViewCommandTest {

    @Mock
    private ServerData serverData;

    @Mock
    private ChannelHandlerContext ctx;

    private ViewCommand command;

    @Before
    public void setUp() {
        command = new ViewCommand(serverData);

    }

    @Test
    public void testExecute_NoArguments_ShowsAllTopics() {
        // Arrange
        String[] args = {};
        when(serverData.viewTopics()).thenReturn("Topic1\nTopic2\n");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(ctx).writeAndFlush("Topic1\nTopic2\n");
    }

    @Test
    public void testExecute_NoArguments_NoTopics_ShowsEmptyMessage() {
        // Arrange
        String[] args = {};
        when(serverData.viewTopics()).thenReturn("");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).viewTopics();
        verify(ctx).writeAndFlush("There is no topics created yet!");
    }

    @Test
    public void testExecute_WithTopicArgument_ShowsVotesInTopic() {
        // Arrange
        String[] args = {"-t=testTopic"};
        when(serverData.viewVotesInTopic("testTopic")).thenReturn("Votes in testTopic topic:\n[vote1, vote2]");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).viewVotesInTopic("testTopic");
        verify(ctx).writeAndFlush("Votes in testTopic topic:\n[vote1, vote2]");
    }

    @Test
    public void testExecute_WithTopicAndVoteArguments_ShowsVoteDetails() {
        // Arrange
        String[] args = {"-t=testTopic", "-v=testVote"};
        when(serverData.viewVote("testTopic", "testVote"))
                .thenReturn("Vote description:\n1. Option1 : 5\n2. Option2 : 3");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).viewVote("testTopic", "testVote");
        verify(ctx).writeAndFlush("Vote description:\n1. Option1 : 5\n2. Option2 : 3");
    }

    @Test
    public void testExecute_WithInvalidTopicArgument_ShowsErrorMessage() {
        // Arrange
        String[] args = {"-t=invalidTopic"};
        when(serverData.viewVotesInTopic("invalidTopic"))
                .thenReturn("There is no topic with name invalidTopic");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).viewVotesInTopic("invalidTopic");
        verify(ctx).writeAndFlush("There is no topic with name invalidTopic");
    }

    @Test
    public void testExecute_WithInvalidVoteArgument_ShowsErrorMessage() {
        // Arrange
        String[] args = {"-t=testTopic", "-v=invalidVote"};
        when(serverData.viewVote("testTopic", "invalidVote"))
                .thenReturn("There is no vote with name invalidVote in topic testTopic");

        // Act
        command.execute(args, ctx);

        // Assert
        verify(serverData).viewVote("testTopic", "invalidVote");
        verify(ctx).writeAndFlush("There is no vote with name invalidVote in topic testTopic");
    }


}