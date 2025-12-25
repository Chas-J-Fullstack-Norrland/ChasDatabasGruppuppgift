package org.chasdb.gruppuppgift.cli;

public interface CommandHandler {
    void handle(CommandInput input);
    String getDomain();
}
