package org.chasdb.gruppuppgift.cli;

import java.util.List;
import java.util.Map;

public record CommandInput(String domain, String action, Map<String, String> flags, List<String> args) {
}
