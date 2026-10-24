package lobby.fluffylobby.config;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFixer {

    private static final Pattern SETTINGS_HEADER =
            Pattern.compile("(?m)^settings:[ \\t]*(?:#.*)?(?=\\r?$)");
    private static final Pattern RESCUE_HEADER =
            Pattern.compile("(?m)^  void_rescue:[ \\t]*(?:#.*)?(?=\\r?$)");

    private final FluffyLobby plugin;

    public ConfigFixer(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    public void fix() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        try {
            YamlConfiguration current = YamlConfiguration.loadConfiguration(configFile);
            boolean missingEnabled = !current.contains("settings.void_rescue.enabled", true);
            boolean missingY = !current.contains("settings.void_rescue.y", true);
            if (!missingEnabled && !missingY) {
                return;
            }

            if (current.contains("settings", true)
                    && current.getConfigurationSection("settings") == null) {
                plugin.getLogger().warning("Cannot add void_rescue defaults: 'settings' is not a YAML section.");
                return;
            }
            if (current.contains("settings.void_rescue", true)
                    && current.getConfigurationSection("settings.void_rescue") == null) {
                plugin.getLogger().warning("Cannot add void_rescue defaults: 'settings.void_rescue' is not a YAML section.");
                return;
            }

            String content = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
            String newline = content.contains("\r\n") ? "\r\n" : "\n";
            List<String> missingLines = new ArrayList<>();
            if (missingEnabled) {
                missingLines.add("    enabled: true");
            }
            if (missingY) {
                missingLines.add("    y: min # Use each world's minimum build height, or set a custom Y value.");
            }

            Matcher rescueHeader = RESCUE_HEADER.matcher(content);
            if (rescueHeader.find()) {
                content = insertAfterHeader(content, newline, rescueHeader, String.join(newline, missingLines));
            } else {
                String rescueSection = "  void_rescue: # Teleports players to the lobby spawn when they fall below this Y level."
                        + newline + String.join(newline, missingLines);
                Matcher settingsHeader = SETTINGS_HEADER.matcher(content);
                if (settingsHeader.find()) {
                    content = insertAfterHeader(content, newline, settingsHeader, rescueSection);
                } else {
                    String prefix = content.isEmpty() || content.endsWith("\n") ? "" : newline;
                    content += prefix + "settings:" + newline + rescueSection + newline;
                }
            }

            Files.writeString(configFile.toPath(), content, StandardCharsets.UTF_8);
            plugin.reloadConfig();
            plugin.getLogger().info("Added missing settings.void_rescue defaults to config.yml.");
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not add missing void_rescue config defaults: " + exception.getMessage());
        }
    }

    private String insertAfterHeader(String content, String newline, Matcher header, String insertion) {
        int lineEnd = header.end();
        int contentStart = lineEnd;
        if (content.startsWith("\r\n", lineEnd)) {
            contentStart += 2;
        } else if (content.startsWith("\n", lineEnd)) {
            contentStart += 1;
        }
        return content.substring(0, lineEnd) + newline + insertion + newline + content.substring(contentStart);
    }
}
