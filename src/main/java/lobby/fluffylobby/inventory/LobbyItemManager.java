package lobby.fluffylobby.inventory;

import lobby.fluffylobby.FluffyLobby;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyItemManager {

    private final FluffyLobby plugin;

    public LobbyItemManager(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    public void loadLobbyItems(Player player) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection itemsSection = config.getConfigurationSection("lobby_items");

        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) continue;

                String itemName = itemSection.getString("name");
                Material itemType = Material.matchMaterial(itemSection.getString("material"));
                int slot = itemSection.getInt("slot");

                if (itemName != null && itemType != null) {
                    if (plugin.isPlaceholderAPIEnabled()) {
                        itemName = PlaceholderAPI.setPlaceholders(player, itemName);
                    }
                    itemName = ChatColor.translateAlternateColorCodes('&', itemName);

                    ItemStack itemStack = new ItemStack(itemType);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName(itemName);
                    itemStack.setItemMeta(meta);

                    player.getInventory().setItem(slot - 1, itemStack);
                }
            }
        }
    }
}
