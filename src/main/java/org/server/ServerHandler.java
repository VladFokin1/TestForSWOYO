package org.server;

import io.netty.channel.*;
import org.commandPattern.clientCommands.ClientCommandFactory;
import org.commandPattern.clientCommands.CreateVoteCommand;
import org.commandPattern.Command;
import org.commandPattern.clientCommands.DialogCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private final ClientCommandFactory clientCommandFactory;
    private final Map<ChannelHandlerContext, Command> activeDialogs = new ConcurrentHashMap<>();

    public ServerHandler(ClientCommandFactory factory) {
        clientCommandFactory = factory;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println(
                "Received: " + "`" + msg+"`");


        msg = msg.trim();


        //region Продолжаем диалог (если есть)
        Command activeCommand = activeDialogs.get(ctx);
        if (activeCommand != null) {
            activeCommand.execute(new String[]{msg}, ctx);
            return;
        }
        //endregion


        //region Остальные команды
        String commandName = findCommand(msg);
        if (commandName == null) {
            ctx.writeAndFlush("Unknown command: " + msg + "\n");
            return;
        }

        // Извлекаем параметры
        String[] commandArgs = msg.substring(commandName.length()).trim().split(" ");
        /*for (String str : commandArgs) {
            System.out.println(str);
        }*/

        // Получаем команду из фабрики
        Command command = clientCommandFactory.getCommand(commandName);

        //проверка на комманду-логин
        if (commandName.equals("login")) {
            command.execute(commandArgs, ctx);
            System.out.println("Command executed: " + commandName + "\n");
            return;
        }

        Boolean isAuthenticated = ctx.channel().attr(UserAttributes.AUTHENTICATED).get();
        if (isAuthenticated != null && isAuthenticated) {
            // Выполняем команду
            if (command instanceof DialogCommand) {
                activeDialogs.put(ctx, command);
            }
            command.execute(commandArgs, ctx);
            // Отправляем ответ клиенту
            System.out.println("Command executed: " + commandName + "\n");

        } else {
            ctx.writeAndFlush("You need to login first!");
        }
        //endregion


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        activeDialogs.remove(ctx);
        System.out.println("Channel " + ctx.channel().id() + " inactive");
    }

    public void clearActiveDialog(ChannelHandlerContext ctx) {
        activeDialogs.remove(ctx); // Удаляем команду для этого контекста
    }

    private String findCommand(String input) {
        // Получаем все возможные команды из фабрики
        String[] possibleCommands = clientCommandFactory.getCommandsArray();

        // Проверяем, начинается ли входная строка с одной из команд
        for (String command : possibleCommands) {
            if (input.startsWith(command)) {
                return command;
            }
        }

        return null;
    }
}
