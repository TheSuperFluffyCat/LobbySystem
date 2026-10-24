package lobby.fluffylobby.commands;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class VanishCommand implements CommandExecutor {
    private final FluffyLobby plugin;
    public VanishCommand(FluffyLobby plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { MessageUtils.sendMessage(sender, "<red>Only players can use this command.</red>", plugin); return true; }
        if (plugin.getVanishedPlayers().remove(player.getUniqueId())) {
            plugin.getParticleHaloManager().startHalo(player);
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!plugin.getVanishedPlayers().contains(online.getUniqueId())) online.showPlayer(plugin, player);
            }
            broadcastPresenceMessage("messages.join_message", "<yellow>%player% joined the server.</yellow>", player);
            MessageUtils.sendMessage(player, plugin.getConfig().getString("messages.vanish_disabled", "<red>Vanish disabled.</red>"), plugin);
        } else {
            plugin.getParticleHaloManager().stopHalo(player);
            plugin.getVanishedPlayers().add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) if (!online.equals(player)) online.hidePlayer(plugin, player);
            broadcastPresenceMessage("messages.quit_message", "<yellow>%player% left the server.</yellow>", player);
            MessageUtils.sendMessage(player, plugin.getConfig().getString("messages.vanish_enabled", "<green>Vanish enabled.</green>"), plugin);
        }
        return true;
    }

    private void broadcastPresenceMessage(String path, String fallback, Player player) {
        String message = plugin.getConfig().getString(path, fallback).replace("%player%", player.getName());
        String component = MessageUtils.formatMessage(message, player, plugin);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player) && !plugin.getVanishedPlayers().contains(online.getUniqueId())) {
                online.sendMessage(component);
            }
        }
    }
}
