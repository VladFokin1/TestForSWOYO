package org.commandPattern.clientCommands;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.server.UserAttributes;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class LoginCommandTest {

    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    @Mock
    private Attribute<String> usernameAttribute;

    @Mock
    private Attribute<Boolean> authenticatedAttribute;

    private LoginCommand command;

    @Before
    public void setUp() {
        // Настраиваем цепочку mock-объектов
        when(ctx.channel()).thenReturn(channel);
        when(channel.attr(UserAttributes.USERNAME)).thenReturn(usernameAttribute);
        when(channel.attr(UserAttributes.AUTHENTICATED)).thenReturn(authenticatedAttribute);

        command = new LoginCommand();
    }

    @Test
    public void testExecute_WithValidUsername_SetsAttributesAndWelcomesUser() {
        // Arrange
        String[] args = {"-u=testUser"};

        // Act
        command.execute(args, ctx);

        // Assert
        verify(usernameAttribute).set("testUser");
        verify(authenticatedAttribute).set(true);
        verify(ctx).writeAndFlush("Logged successfully! Welcome, testUser!");
    }

    @Test
    public void testExecute_WithoutUsernameParameter_ShowsError() {
        // Arrange
        String[] args1 = {}; // Нет параметров
        String[] args2 = {"wrongParam"}; // Неправильный параметр

        // Act & Assert
        command.execute(args1, ctx);
        verify(ctx).writeAndFlush("Define username with parameter -u= ");

        command.execute(args2, ctx);
        verify(ctx, times(2)).writeAndFlush("Define username with parameter -u= ");
    }

    @Test
    public void testExecute_WithEmptyUsername_ShowsError() {
        // Arrange
        String[] args = {"-u="}; // Пустое имя пользователя

        // Act
        command.execute(args, ctx);

        // Assert
        verify(usernameAttribute, never()).set(any());
        verify(authenticatedAttribute, never()).set(any());
        verify(ctx).writeAndFlush("Empty username. Login failed.");
    }


}
