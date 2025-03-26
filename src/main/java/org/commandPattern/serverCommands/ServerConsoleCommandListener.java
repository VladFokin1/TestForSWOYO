package org.commandPattern.serverCommands;

import org.commandPattern.Command;
import org.server.ServerData;

import java.util.Scanner;

public class ServerConsoleCommandListener implements Runnable{
    private volatile boolean running = true;
    private final ServerCommandFactory commandFactory;
    private final ServerData serverData;

    public ServerConsoleCommandListener(ServerCommandFactory commandFactory, ServerData serverData) {
        this.commandFactory = commandFactory;
        this.serverData = serverData;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            String input = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(input)) {
                running = false;
                break;
            }
            processConsoleCommand(input);
        }
        scanner.close();
    }

    private void processConsoleCommand(String cmd) {
        String[] parts = cmd.split(" ", 2);
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? parts[1].split(" ") : new String[0];

        Command command = commandFactory.getCommand(commandName);

        command.execute(args, null);

    }
}
