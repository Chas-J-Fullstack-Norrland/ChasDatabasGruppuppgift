package org.chasdb.gruppuppgift.cli;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class InputParser {

    public CommandInput parse(String line) {
        if (line == null || line.isBlank()) return null;

        String[] parts = line.trim().split("\\s+");
        if(parts.length == 0) return null;

        String domain = parts[0].toLowerCase();
        String action = parts.length > 1 && !parts[1].startsWith("--") ? parts[1].toLowerCase() : "";

        Map<String, String> flags = new HashMap<>();
        StringBuilder rawArgsBuilder = new StringBuilder();

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            if (part.startsWith("--")) {
                int eqIndex = part.indexOf('=');
                if (eqIndex > 0) {
                    String key = part.substring(2, eqIndex);
                    String value = part.substring(eqIndex + 1);
                    flags.put(key, value);
                } else {
                    flags.put(part.substring(2), "true");
                }
            } else if (i > 1 || action.isEmpty()) {
                if (!rawArgsBuilder.isEmpty()) rawArgsBuilder.append(" ");
                rawArgsBuilder.append(part);
            }
        }

        return new CommandInput(domain, action, flags, rawArgsBuilder.toString());
    }

}
