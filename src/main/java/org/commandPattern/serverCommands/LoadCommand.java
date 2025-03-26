package org.commandPattern.serverCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import org.commandPattern.Command;
import org.server.ServerData;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LoadCommand implements Command {
    private ServerData serverData;

    public LoadCommand(ServerData serverData) {
        this.serverData = serverData;
    }

    @Override
    public void execute(String[] args, ChannelHandlerContext ctx) {
        if (args.length < 1) {
            System.out.println("Usage: save <filename>");
            return;
        }

        String filename = args[0];
        if (!filename.endsWith(".json")) {
            System.out.println("Error: File must have .json extension");
            return;
        }
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("Error: File not found");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            // Десериализация данных
            Map<String, Object> loadedData = mapper.readValue(file, Map.class);

            // Восстановление состояния сервера
            serverData.restoreFromLoadedData(loadedData);

            System.out.println("Data successfully loaded from " + filename);
        } catch (IOException e) {
            System.out.println("Load error: " + e.getMessage());
        }
    }
}
