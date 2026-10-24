package lobby.fluffylobby.commands;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class FlyCommand implements CommandExecutor {
    private final FluffyLobby plugin;
    public FlyCommand(FluffyLobby plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { MessageUtils.sendMessage(sender, "<red>Only players can use this command.</red>", plugin); return true; }
        boolean enabled = !player.getAllowFlight();
        player.setAllowFlight(enabled);
        player.setFlying(enabled);
        String path = enabled ? "messages.fly_enabled" : "messages.fly_disabled";
        MessageUtils.sendMessage(player, plugin.getConfig().getString(path, enabled ? "<green>Fly enabled.</green>" : "<red>Fly disabled.</red>"), plugin);
        return true;
    }
}
