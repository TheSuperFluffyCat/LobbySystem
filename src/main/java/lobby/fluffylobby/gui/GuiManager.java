package lobby.fluffylobby.gui;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiManager {

    private final FluffyLobby plugin;

    public GuiManager(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, String menuName) {
        GuiMenu menu = plugin.getGuiConfigLoader().getMenu(menuName);
        if (menu == null) {
            player.sendMessage("Menu not found!");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, menu.getRows() * 9, menu.getTitle());

        ItemStack backgroundItem = menu.getBackgroundItem();
        if (backgroundItem != null) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, backgroundItem);
            }
        }

        menu.getItems().forEach(item -> {
            if (item.getSlot() >= 0 && item.getSlot() < inventory.getSize()) {
                inventory.setItem(item.getSlot(), item.toItemStack(player));
            }
        });

        player.openInventory(inventory);
    }
}
