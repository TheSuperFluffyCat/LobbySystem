package lobby.fluffylobby.listeners;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatDisableListener implements Listener {

    private final FluffyLobby plugin;

    public ChatDisableListener(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.getConfig().getBoolean("disablechat", false)) {
            event.setCancelled(true);
        }
    }
}
