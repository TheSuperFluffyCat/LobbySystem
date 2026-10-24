package lobby.fluffylobby.commands;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.managers.SpawnManager;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to set the server's spawn location.
 */
public class SetSpawnCommand implements CommandExecutor {

    private final FluffyLobby plugin;
    private final SpawnManager spawnManager;

    public SetSpawnCommand(FluffyLobby plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has the required permission
        // Somehow plugins.yml doesn't understand the permissions so we douple check
        if (!sender.hasPermission("lobby.setspawn")) {
            String noPermissionMessage = plugin.getConfig().getString("messages.no_permission", "<red>You do not have permission to execute this command!</red>");
            MessageUtils.sendMessage(sender, noPermissionMessage, plugin);
            return false;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Set spawn location
            spawnManager.setSpawn(player.getLocation());

            // Send success message from config
            String setSpawnMessage = plugin.getConfig().getString("messages.setspawn_message", "<green>Spawn point set successfully!</green>");
            MessageUtils.sendMessage(player, setSpawnMessage, plugin);

            return true;
        } else {
            // Send error message for non-player usage
            String onlyPlayersMessage = plugin.getConfig().getString("messages.only_players", "<red>Only players can use this command!</red>");
            MessageUtils.sendMessage(sender, onlyPlayersMessage, plugin);
            return false;
        }
    }
}
