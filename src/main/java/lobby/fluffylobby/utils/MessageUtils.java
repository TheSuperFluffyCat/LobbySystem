package lobby.fluffylobby.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import lobby.fluffylobby.FluffyLobby;
import me.clip.placeholderapi.PlaceholderAPI;

/**
 * Utility class for handling messages with MiniMessage and PlaceholderAPI support.
 */
public class MessageUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static String formatMessage(String message, Player player, FluffyLobby plugin) {
        // Apply PlaceholderAPI placeholders if enabled and player is not null
        if (plugin.isPlaceholderAPIEnabled() && player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        return LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(message));
    }

    public static void sendMessage(CommandSender sender, String message, FluffyLobby plugin) {
        // Format the message
        String formattedMessage = formatMessage(message, sender instanceof Player ? (Player) sender : null, plugin);

        // Send message based on type of sender
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(formattedMessage);
        } else {
            sender.sendMessage(formattedMessage);
        }
    }
}
