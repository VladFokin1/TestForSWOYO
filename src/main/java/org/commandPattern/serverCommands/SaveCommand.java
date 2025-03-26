package org.commandPattern.serverCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;

import java.io.File;
import java.io.IOException;

public class SaveCommand implements Command {
    private ServerData serverData;

    public SaveCommand(ServerData serverData) {
        this.serverData = serverData;
    }

    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (args.length < 1) {
            System.out.println("Usage: save <filename>");
            return;
        }

        try {
            Object data = serverData.getDataForSave();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(args[0] + ".json"), data);

            String message = "Data saved to " + args[0] + ".json";

            System.out.println(message);

        } catch (IOException e) {
            String error = "Save error: " + e.getMessage();
            System.err.println(error);
        }
    }
}
