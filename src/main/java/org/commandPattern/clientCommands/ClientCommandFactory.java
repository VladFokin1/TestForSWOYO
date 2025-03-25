package org.commandPattern.clientCommands;

import org.commandPattern.Command;
import org.server.ServerData;

import java.util.HashMap;
import java.util.Map;

public class ClientCommandFactory {
    private Map<String, Command> commands;
    private ServerData serverData;

    public ClientCommandFactory(ServerData serverData) {
        commands = new HashMap<>();
        this.serverData = serverData;

        //добавление команд
        commands.put("login", new LoginCommand());
        commands.put("create topic", new CreateTopicCommand(serverData));
        commands.put("create vote", new CreateVoteCommand(serverData));
        commands.put("view", new ViewCommand(serverData));
        // Добавьте другие команды по мере необходимости
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    public String[] getCommandsArray() {
        return commands.keySet().toArray(new String[0]);
    }
}
