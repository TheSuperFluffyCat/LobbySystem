package lobby.fluffylobby.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiMenu {

    private final String title;
    private final int rows;
    private final Material openItemMaterial;
    private final String clickAction;
    private final ItemStack backgroundItem;
    private final List<GuiItem> items;

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GuiMenu(ConfigurationSection section) {
        this.title = LegacyComponentSerializer.legacySection().serialize(
                miniMessage.deserialize(section.getString("title", "Menu")));
        this.rows = Math.max(1, Math.min(6, section.getInt("rows", 3)));

        ConfigurationSection openItemSection = section.getConfigurationSection("open_item");
        if (openItemSection != null) {
            this.openItemMaterial = Material.matchMaterial(openItemSection.getString("material", "PAPER"));
            this.clickAction = openItemSection.getString("click_action", "BOTH").toUpperCase();
        } else {
            this.openItemMaterial = Material.PAPER;
            this.clickAction = "BOTH";
        }

        ConfigurationSection bgSection = section.getConfigurationSection("background_item");
        if (bgSection != null) {
            Material bgMaterial = Material.matchMaterial(bgSection.getString("material", "WHITE_STAINED_GLASS_PANE"));
            this.backgroundItem = createBackgroundItem(bgMaterial);
        } else {
            this.backgroundItem = null;
        }

        this.items = new ArrayList<>();
        List<?> itemList = section.getList("items");
        if (itemList != null) {
            for (Object itemObject : itemList) {
                if (itemObject instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) itemObject;
                    GuiItem item = new GuiItem(itemMap);
                    items.add(item);
                } else {
                    System.out.println("Invalid item configuration: " + itemObject);
                }
            }
        } else {
            System.out.println("No items found for menu: " + this.title);
        }
    }

    private ItemStack createBackgroundItem(Material material) {
        if (material == null) return null;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(null);
            meta.setLore(null);
            item.setItemMeta(meta);
        }
        return item;
    }

    public int getRows() {
        return rows;
    }

    public Material getOpenItemMaterial() {
        return openItemMaterial;
    }

    public String getClickAction() {
        return clickAction;
    }

    public ItemStack getBackgroundItem() {
        return backgroundItem;
    }

    public List<GuiItem> getItems() {
        return items;
    }

    public String getTitle() {
        return title;
    }
}
