package lobby.fluffylobby.listeners;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class ModrinthUpdateChecker implements Listener {

    private static final String VERSIONS_URL = "https://api.modrinth.com/v2/project/lobbysystem/version?include_changelog=false";
    private static final String MODRINTH_PAGE = "https://modrinth.com/plugin/lobbysystem/versions";
    private static final long CHECK_INTERVAL_TICKS = 24L * 60L * 60L * 20L;

    private final FluffyLobby plugin;
    private volatile String availableVersion;
    private String notifiedVersion;

    public ModrinthUpdateChecker(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkForUpdate, 1L, CHECK_INTERVAL_TICKS);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String latest = availableVersion;
        if (latest != null && isOperator(event.getPlayer())) {
            notifyPlayer(event.getPlayer(), latest);
        }
    }

    private void checkForUpdate() {
        String latest;
        try {
            latest = fetchLatestRelease();
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().log(Level.FINE, "Could not check Modrinth for FluffyLobby updates.", exception);
            return;
        }

        String installed = plugin.getDescription().getVersion();
        boolean outdated = isNewerVersion(latest, installed);
        Bukkit.getScheduler().runTask(plugin, () -> {
            availableVersion = outdated ? latest : null;
            if (!outdated || latest.equals(notifiedVersion)) {
                return;
            }

            notifiedVersion = latest;
            plugin.getLogger().warning("A newer FluffyLobby release is available (installed: " + installed
                    + ", latest: " + latest + "). Download it from " + MODRINTH_PAGE);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isOperator(player)) {
                    notifyPlayer(player, latest);
                }
            }
        });
    }

    private String fetchLatestRelease() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(VERSIONS_URL).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "FluffyLobby/" + plugin.getDescription().getVersion()
                + " (" + MODRINTH_PAGE + ")");

        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Modrinth returned HTTP " + status);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            return findLatestRelease(response.toString());
        } finally {
            connection.disconnect();
        }
    }

    private static String findLatestRelease(String response) throws IOException {
        String latestVersion = null;
        Instant latestPublished = null;
        for (String object : topLevelObjects(response)) {
            if (!"listed".equals(jsonString(object, "status"))
                    || !"release".equals(jsonString(object, "version_type"))) {
                continue;
            }

            String version = jsonString(object, "version_number");
            String published = jsonString(object, "date_published");
            if (version == null || published == null) {
                continue;
            }

            try {
                Instant publishedAt = Instant.parse(published);
                if (latestPublished == null || publishedAt.isAfter(latestPublished)) {
                    latestPublished = publishedAt;
                    latestVersion = version;
                }
            } catch (RuntimeException ignored) {
                // Ignore malformed entries and keep checking the remaining releases.
            }
        }

        if (latestVersion == null) {
            throw new IOException("Modrinth returned no listed release versions");
        }
        return latestVersion;
    }

    private static List<String> topLevelObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int objectStart = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int index = 0; index < json.length(); index++) {
            char character = json.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    inString = false;
                }
                continue;
            }

            if (character == '"') {
                inString = true;
            } else if (character == '{') {
                if (depth == 0) {
                    objectStart = index;
                }
                depth++;
            } else if (character == '}' && depth > 0) {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    objects.add(json.substring(objectStart, index + 1));
                    objectStart = -1;
                }
            }
        }
        return objects;
    }

    private static String jsonString(String json, String field) {
        String key = String.valueOf('"') + field + '"';
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) {
            return null;
        }
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
            return null;
        }

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int index = valueStart + 1; index < json.length(); index++) {
            char character = json.charAt(index);
            if (escaped) {
                value.append(character);
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '"') {
                return value.toString();
            } else {
                value.append(character);
            }
        }
        return null;
    }

    private static boolean isNewerVersion(String candidate, String installed) {
        try {
            return compareVersions(candidate, installed) > 0;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static int compareVersions(String left, String right) {
        Version first = Version.parse(left);
        Version second = Version.parse(right);
        int sharedParts = Math.max(first.numbers.size(), second.numbers.size());
        for (int index = 0; index < sharedParts; index++) {
            BigInteger firstPart = index < first.numbers.size() ? first.numbers.get(index) : BigInteger.ZERO;
            BigInteger secondPart = index < second.numbers.size() ? second.numbers.get(index) : BigInteger.ZERO;
            int comparison = firstPart.compareTo(secondPart);
            if (comparison != 0) {
                return comparison;
            }
        }

        if (first.preRelease == null) return second.preRelease == null ? 0 : 1;
        if (second.preRelease == null) return -1;
        return comparePreReleases(first.preRelease, second.preRelease);
    }

    private static int comparePreReleases(String left, String right) {
        String[] first = left.split("\\.");
        String[] second = right.split("\\.");
        for (int index = 0; index < Math.min(first.length, second.length); index++) {
            boolean firstNumeric = first[index].matches("\\d+");
            boolean secondNumeric = second[index].matches("\\d+");
            int comparison;
            if (firstNumeric && secondNumeric) {
                comparison = new BigInteger(first[index]).compareTo(new BigInteger(second[index]));
            } else if (firstNumeric != secondNumeric) {
                comparison = firstNumeric ? -1 : 1;
            } else {
                comparison = first[index].compareTo(second[index]);
            }
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(first.length, second.length);
    }

    private static boolean isOperator(Player player) {
        return player.isOp() || player.hasPermission("lobby.manage");
    }

    private static void notifyPlayer(Player player, String latest) {
        player.sendMessage("§e[FluffyLobby] An update is available: " + latest
                + ". Download new features and bug fixes: " + MODRINTH_PAGE);
    }

    private static final class Version {
        private final List<BigInteger> numbers;
        private final String preRelease;

        private Version(List<BigInteger> numbers, String preRelease) {
            this.numbers = numbers;
            this.preRelease = preRelease;
        }

        private static Version parse(String input) {
            String value = input.trim();
            if (value.startsWith("v") || value.startsWith("V")) {
                value = value.substring(1);
            }

            int buildIndex = value.indexOf('+');
            if (buildIndex >= 0) {
                value = value.substring(0, buildIndex);
            }
            int preReleaseIndex = value.indexOf('-');
            String preRelease = preReleaseIndex >= 0 ? value.substring(preReleaseIndex + 1) : null;
            String numericVersion = preReleaseIndex >= 0 ? value.substring(0, preReleaseIndex) : value;
            if (numericVersion.isEmpty() || (preRelease != null && preRelease.isEmpty())) {
                throw new IllegalArgumentException("Invalid version: " + input);
            }

            List<BigInteger> numbers = new ArrayList<>();
            for (String part : numericVersion.split("\\.")) {
                if (!part.matches("\\d+")) {
                    throw new IllegalArgumentException("Invalid version: " + input);
                }
                numbers.add(new BigInteger(part));
            }
            return new Version(numbers, preRelease);
        }
    }
}
