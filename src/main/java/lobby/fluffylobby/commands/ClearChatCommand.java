package lobby.fluffylobby.commands;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ClearChatCommand implements CommandExecutor {
    private final FluffyLobby plugin;
    public ClearChatCommand(FluffyLobby plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int lines = Math.max(1, Math.min(200, plugin.getConfig().getInt("clearchat.lines", 100)));
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < lines; i++) {
                // Sending separate chat packets works reliably with modern clients.
                player.sendMessage(" ");
            }
        }
        MessageUtils.sendMessage(sender, plugin.getConfig().getString("messages.clearchat", "<green>Chat cleared.</green>"), plugin);
        return true;
    }
}
