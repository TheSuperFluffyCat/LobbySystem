package lobby.fluffylobby.commands;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for managing lobby features with MiniMessage integration.
 */
public class LobbyCommand implements CommandExecutor, TabCompleter {

    private final FluffyLobby plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage(); // For direct MiniMessage parsing

    public LobbyCommand(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Send usage instructions if no arguments are provided
            sendFancyMessage(sender, "<yellow>Use <green>/lobby help</green> for available commands!</yellow>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReloadCommand(sender);
                break;

            case "items":
                handleItemsCommand(sender);
                break;

            case "help":
                handleHelpCommand(sender);
                break;

            default:
                sendFancyMessage(sender, "<red>Unknown subcommand. Use <green>/lobby help</green> for a list of commands.</red>");
        }
        return true;
    }

    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("lobby.reload")) {
            sendFancyMessage(sender, "<red>You do not have permission to use this command.</red>");
            return;
        }

        plugin.reloadConfigFile();
        plugin.getGuiConfigLoader().reload();
        sendFancyMessage(sender, "<green>Plugin configuration reloaded successfully!</green>");
    }

    private void handleItemsCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendFancyMessage(sender, "<red>Only players can use this command.</red>");
            return;
        }

        Player player = (Player) sender;
        plugin.getLobbyItemManager().loadLobbyItems(player);
        sendFancyMessage(player, "<green>Lobby items loaded successfully!</green>");
    }

    private void handleHelpCommand(CommandSender sender) {
        // Provide a help menu
        sendFancyMessage(sender, """
                <gradient:#32CD32:#7FFF00>Lobby Command Help:</gradient>
                <yellow>/fluffylobby reload</yellow> - Reload the plugin configuration.
                <yellow>/fluffylobby items</yellow> - Load lobby items (players only).
                <yellow>/fluffylobby help</yellow> - Display this help menu.
                """);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Provide tab completions for the first argument (subcommands)
            completions.add("reload");
            completions.add("items");
            completions.add("help");

            // Filter completions based on user input
            String currentInput = args[0].toLowerCase();
            completions.removeIf(completion -> !completion.toLowerCase().startsWith(currentInput));
        }

        return completions;
    }

    private void sendFancyMessage(CommandSender sender, String message) {
        String formattedMessage = LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(message));

        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(formattedMessage);
        } else {
            sender.sendMessage(formattedMessage.toString());
        }
    }
}
