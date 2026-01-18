package org.chasdb.gruppuppgift.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class AppRunner implements CommandLineRunner {
    private final InputParser parser;
    private final Map<String, CommandHandler> handlers;

    private boolean running = true;

    public AppRunner(InputParser parser, List<CommandHandler> commandHandlers) {
        this.parser = parser;
        this.handlers = commandHandlers.stream()
                .collect(Collectors.toMap(CommandHandler::getDomain, h -> h));
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Systemet startat. Skriv 'system help' för lista av kommandon.");

        while (running && scanner.hasNextLine()) {
            System.out.print("> ");
            String line = scanner.nextLine();

            if (line.isBlank()) continue;

            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Avslutar...");
                running = false;
                break;
            }

            try {
                processCommand(line);
            } catch (Exception e) {
                System.out.println("Fel vid exekvering: " + e.getMessage());
            }
        }
    }

    public void processCommand(String line) {
        CommandInput input = parser.parse(line);
        if (input == null) return;

        CommandHandler handler = handlers.get(input.domain());

        if (handler != null) {
            handler.handle(input);
        } else {
            System.out.println("Okänt kommando: " + input.domain());
        }
    }
}
