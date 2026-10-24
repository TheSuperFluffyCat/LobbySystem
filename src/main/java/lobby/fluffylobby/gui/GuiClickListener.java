package lobby.fluffylobby.gui;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class GuiClickListener implements Listener {

    private final FluffyLobby plugin;

    public GuiClickListener(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) return;

        plugin.getGuiConfigLoader().getMenuList().forEach((menuName, menu) -> {
            boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
            boolean leftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
            boolean allowed = "BOTH".equals(menu.getClickAction())
                    || ("RIGHT".equals(menu.getClickAction()) && rightClick)
                    || ("LEFT".equals(menu.getClickAction()) && leftClick);
            if (allowed && menu.getOpenItemMaterial() == item.getType()) {
                plugin.getGuiManager().openMenu(player, menuName);
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        GuiMenu menu = plugin.getGuiConfigLoader().getMenuByTitle(event.getView().getTitle());
        if (menu != null) {
                event.setCancelled(true);

                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) {
                    return;
                }

                menu.getItems().stream()
                        .filter(guiItem -> guiItem.getSlot() == event.getSlot())
                        .findFirst()
                        .ifPresent(guiItem -> {
                            String actionType = guiItem.getActionType();

                            if ("connect".equalsIgnoreCase(actionType)) {
                                String address = guiItem.getAddress();
                                boolean directEnabled = plugin.getConfig().getBoolean("gui.direct_connect.enabled", true);
                                if (!address.isEmpty() && directEnabled) {
                                    sendConnectMessage(player, address);
                                    scheduleAddressConnect(player, address, 40L); // 40L = 2 Sekunden
                                } else {
                                    String server = guiItem.getServer();
                                    if (!server.isEmpty()) {
                                        sendConnectMessage(player, server);
                                        scheduleServerConnect(player, server, 40L); // 40L = 2 Sekunden
                                    } else if (!address.isEmpty()) {
                                        MessageUtils.sendMessage(player, plugin.getConfig().getString(
                                                "messages.connect_error", "Failed to connect to <server>!")
                                                .replace("<server>", address), plugin);
                                    }
                                }
                            } else if ("command".equalsIgnoreCase(actionType)) {
                                if (!plugin.getConfig().getBoolean("gui.command_actions.enabled", true)) return;
                                String command = guiItem.getCommand().trim().replace("%player%", player.getName());
                                if (!command.isEmpty()) {
                                    if (command.startsWith("/")) command = command.substring(1);
                                    if (guiItem.isConsole()) {
                                        if (!plugin.getConfig().getBoolean("gui.command_actions.allow_console", false)) return;
                                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                                    } else {
                                        player.performCommand(command);
                                    }
                                }
                            }
                        });
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (plugin.getGuiConfigLoader().getMenuByTitle(event.getView().getTitle()) != null) event.setCancelled(true);
    }

    private void sendConnectMessage(Player player, String server) {
        String connectMessage = plugin.getConfig().getString("messages.connect_attempt", "Connecting to <server>...");
        connectMessage = connectMessage.replace("<server>", server);
        MessageUtils.sendMessage(player, connectMessage, plugin);
    }

    private void scheduleServerConnect(Player player, String server, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);

                out.writeUTF("Connect");
                out.writeUTF(server);

                player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
            } catch (Exception e) {
                String errorMessage = plugin.getConfig().getString("messages.connect_error", "Failed to connect to <server>!");
                errorMessage = errorMessage.replace("<server>", server);
                MessageUtils.sendMessage(player, errorMessage, plugin);
            }
        }, delay);
    }

    private void scheduleAddressConnect(Player player, String address, long delay) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String host = address;
            int port = 25565;
            if (host.startsWith("[")) {
                int closingBracket = host.indexOf(']');
                if (closingBracket > 0) {
                    String ipv6 = host.substring(1, closingBracket);
                    String rest = host.substring(closingBracket + 1);
                    if (rest.startsWith(":")) {
                        try {
                            port = Integer.parseInt(rest.substring(1));
                        } catch (NumberFormatException ignored) {
                            MessageUtils.sendMessage(player, plugin.getConfig().getString(
                                    "messages.connect_error", "Failed to connect to <server>!").replace("<server>", address), plugin);
                            return;
                        }
                    }
                    host = ipv6;
                }
            } else {
                int colonIndex = host.lastIndexOf(':');
                if (colonIndex > 0) {
                    try {
                        port = Integer.parseInt(host.substring(colonIndex + 1));
                        host = host.substring(0, colonIndex);
                    } catch (NumberFormatException ignored) {
                        MessageUtils.sendMessage(player, plugin.getConfig().getString(
                                "messages.connect_error", "Failed to connect to <server>!").replace("<server>", address), plugin);
                        return;
                    }
                }
            }
            if (host.isEmpty()) {
                MessageUtils.sendMessage(player, plugin.getConfig().getString(
                        "messages.connect_error", "Failed to connect to <server>!").replace("<server>", address), plugin);
                return;
            }
            player.transfer(host, port);
        }, delay);
    }
}
