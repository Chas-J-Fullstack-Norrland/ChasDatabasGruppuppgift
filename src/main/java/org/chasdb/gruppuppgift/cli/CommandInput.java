package org.chasdb.gruppuppgift.cli;

import java.util.Map;

public record CommandInput(String domain, String action, Map<String, String> flags, String rawArgs) {
}
