package org.commandPattern.clientCommands;

import org.commandPattern.Command;
import org.commandPattern.CommandFactory;
import org.server.ServerData;

import java.util.HashMap;
import java.util.Map;

public class ClientCommandFactory extends CommandFactory {

    public ClientCommandFactory(ServerData serverData) {
        super(serverData);

        //добавление команд
        commands.put("login", new LoginCommand());
        commands.put("create topic", new CreateTopicCommand(serverData));
        commands.put("create vote", new CreateVoteCommand(serverData));
        commands.put("view", new ViewCommand(serverData));
        commands.put("vote", new VoteCommand(serverData));
        commands.put("delete", new DeleteVoteCommand(serverData));

    }

}
