package lobby.fluffylobby.listeners;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class TablistUpdater implements Listener {

    private final FluffyLobby plugin;

    public TablistUpdater(FluffyLobby plugin) {
        this.plugin = plugin;

        startRepeatingUpdate();
    }

    private void startRepeatingUpdate() {
        if (!plugin.getConfig().getBoolean("tablist.enabled", false)) return;

        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAllTablists, 0L, 100L); // 100 ticks = 5 seconds
    }

    public void updateAllTablists() {
        if (!plugin.getConfig().getBoolean("tablist.enabled", false)) return;

        FileConfiguration config = plugin.getConfig();

        String headerRaw = config.getString("tablist.header", "");
        String footerRaw = config.getString("tablist.footer", "");

        for (Player player : Bukkit.getOnlinePlayers()) {
            String header = replaceCustomPlaceholders(headerRaw, player);
            String footer = replaceCustomPlaceholders(footerRaw, player);

            String headerComponent = MessageUtils.formatMessage(header, player, plugin);
            String footerComponent = MessageUtils.formatMessage(footer, player, plugin);

            player.setPlayerListHeaderFooter(headerComponent, footerComponent);
        }
    }

    private String replaceCustomPlaceholders(String input, Player player) {
        return input
                .replace("%player%", player.getName())
                .replace("%online_playercount%", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }
}
