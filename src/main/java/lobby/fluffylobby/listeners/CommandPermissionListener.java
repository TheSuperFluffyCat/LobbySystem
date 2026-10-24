package lobby.fluffylobby.listeners;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Listener to enforce permissions for all commands globally.
 */
public class CommandPermissionListener implements Listener {

    private final FluffyLobby plugin;

    // List of whitelisted commands that do not require permissions
    private static final List<String> WHITELISTED_COMMANDS = Arrays.asList("spawn", "server");

    public CommandPermissionListener(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Check if the feature is enabled in the config
        if (!plugin.getConfig().getBoolean("require_command_permissions", false)) {
            return;
        }

        // Get the player and command
        Player player = event.getPlayer();
        String commandLabel = event.getMessage().split(" ")[0].substring(1).toLowerCase(); // Remove leading "/"

        // Verify if the command is registered
        Command command = plugin.getCommand(commandLabel);
        if (command == null) {
            return; // Not a registered command, allow execution
        }

        // Allow whitelisted commands
        if (WHITELISTED_COMMANDS.contains(commandLabel)) {
            return;
        }

        // Check if the player has the required permission
        String permission = "fluffylobby." + commandLabel;
        if (!player.hasPermission(permission)) {
            // Read and format the no-permission message from the config
            String noPermissionMessage = plugin.getConfig().getString("messages.no_permission", "<red>You do not have permission to execute this command!</red>");
            MessageUtils.sendMessage(player, noPermissionMessage, plugin);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        // Check if the feature is enabled in the config
        if (!plugin.getConfig().getBoolean("require_command_permissions", false)) {
            return;
        }

        // Ensure the sender is a player
        CommandSender sender = event.getSender();
        if (!(sender instanceof Player)) {
            return;
        }

        Player player = (Player) sender;

        // Filter tab completions based on permissions
        List<String> completions = event.getCompletions();
        completions.removeIf(completion -> {
            // Strip leading "/" and check permissions
            String commandLabel = completion.startsWith("/") ? completion.substring(1).toLowerCase() : completion.toLowerCase();
            String permission = "fluffylobby." + commandLabel;

            // Keep only commands the player has permission to execute
            return !WHITELISTED_COMMANDS.contains(commandLabel) && !player.hasPermission(permission);
        });
    }
}
