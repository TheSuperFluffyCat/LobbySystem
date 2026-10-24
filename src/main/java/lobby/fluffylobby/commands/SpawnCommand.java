package lobby.fluffylobby.commands;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.managers.SpawnManager;
import lobby.fluffylobby.utils.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to teleport players to the spawn location.
 */
public class SpawnCommand implements CommandExecutor {

    private final FluffyLobby plugin;
    private final SpawnManager spawnManager;

    public SpawnCommand(FluffyLobby plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Teleport player to spawn location
            player.teleport(spawnManager.getSpawn());

            // Get spawn message from config
            String spawnMessage = plugin.getConfig().getString("messages.spawn_message", "<yellow>You have been teleported to the spawn!</yellow>");
            if (plugin.isPlaceholderAPIEnabled()) {
                spawnMessage = PlaceholderAPI.setPlaceholders(player, spawnMessage);
            }

            // Send message to player
            MessageUtils.sendMessage(player, spawnMessage, plugin);
            return true;
        } else {
            // Send error message for non-player usage
            String onlyPlayersMessage = plugin.getConfig().getString("messages.only_players", "<red>Only players can use this command!</red>");
            MessageUtils.sendMessage(sender, onlyPlayersMessage, plugin);
            return false;
        }
    }
}
