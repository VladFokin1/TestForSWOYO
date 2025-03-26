package org.commandPattern.serverCommands;

import org.commandPattern.CommandFactory;
import org.server.ServerData;

public class ServerCommandFactory extends CommandFactory {
    public ServerCommandFactory(ServerData serverData) {
        super(serverData);

        //добавление команд
        commands.put("save", new SaveCommand(serverData));
        commands.put("load", new LoadCommand(serverData));


    }
}
