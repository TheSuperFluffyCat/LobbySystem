package lobby.fluffylobby.commands;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Command to toggle build mode with customizable messages from the configuration.
 */
public class BuildCommand implements CommandExecutor {

    private static final Set<UUID> builders = new HashSet<>();
    private final FluffyLobby plugin;

    public BuildCommand(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    public static boolean canBuild(Player player) {
        return builders.contains(player.getUniqueId());
    }

    public static boolean toggleBuild(Player player) {
        UUID playerId = player.getUniqueId();
        if (builders.contains(playerId)) {
            builders.remove(playerId);
            return false;
        } else {
            builders.add(playerId);
            return true;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lobby.build")) {
            MessageUtils.sendMessage(sender, plugin.getConfig().getString("messages.no_permission", "<red>You do not have permission to execute this command!</red>"), plugin);
            return true;
        }
        if (args.length > 0) {
            if (!sender.hasPermission("lobby.build.others")) {
                MessageUtils.sendMessage(sender, plugin.getConfig().getString("messages.no_permission", "<red>You do not have permission to execute this command!</red>"), plugin);
                return true;
            }
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                MessageUtils.sendMessage(sender, "<red>Player not found!</red>", plugin);
                return true;
            }

            handleToggleBuild(sender, targetPlayer);
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            handleToggleBuild(sender, player);
        } else {
            MessageUtils.sendMessage(sender, "<red>Usage: /build <player></red>", plugin);
        }
        return true;
    }

    private void handleToggleBuild(CommandSender sender, Player player) {
        boolean canBuild = toggleBuild(player);

        // Load messages from config
        String buildModeEnabled = plugin.getConfig().getString("messages.build_mode_enabled", "<green>Build mode enabled.</green>");
        String buildModeDisabled = plugin.getConfig().getString("messages.build_mode_disabled", "<red>Build mode disabled.</red>");

        if (canBuild) {
            // Enable build mode
            player.setGameMode(GameMode.CREATIVE);
            player.getInventory().clear();
            MessageUtils.sendMessage(player, buildModeEnabled, plugin);
            if (!sender.equals(player)) {
                MessageUtils.sendMessage(sender, buildModeEnabled, plugin);
            }
        } else {
            // Disable build mode
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            plugin.getLobbyItemManager().loadLobbyItems(player);
            MessageUtils.sendMessage(player, buildModeDisabled, plugin);
            if (!sender.equals(player)) {
                MessageUtils.sendMessage(sender, buildModeDisabled, plugin);
            }
        }
    }
}
