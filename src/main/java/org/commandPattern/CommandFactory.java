package org.commandPattern;

import org.server.ServerData;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandFactory {
    protected Map<String, Command> commands;
    protected ServerData serverData;

    public CommandFactory(ServerData serverData) {
        commands = new HashMap<>();
        this.serverData = serverData;
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    public String[] getCommandsArray() {
        return commands.keySet().toArray(new String[0]);
    }
}
