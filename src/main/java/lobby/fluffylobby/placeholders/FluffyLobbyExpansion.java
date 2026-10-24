package lobby.fluffylobby.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import lobby.fluffylobby.FluffyLobby;

public class FluffyLobbyExpansion extends PlaceholderExpansion {

    private final FluffyLobby plugin;

    public FluffyLobbyExpansion(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "fluffylobby";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // Example placeholder you can add stuff here if you want but don't sell this plugin it's a free plugin!
        if (identifier.equals("example_placeholder")) {
            return "This is an example - This class will be deleted soon---";
        }

        // Add more placeholders as needed
        // if (identifier.equals("another_placeholder")) {
        //     return "Another example";
        // }

        return null;
    }
}
