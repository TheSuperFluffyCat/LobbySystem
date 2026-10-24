package lobby.fluffylobby.gui;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuiItem {

    private final int slot;
    private final Material material;
    private final String name;
    private final List<String> lore; // Neu: Lore wird als Liste gespeichert
    private final String actionType;
    private final String server;
    private final String address;
    private final String command;
    private final boolean console;

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @SuppressWarnings("unchecked")
    public GuiItem(Map<String, Object> itemMap) {
        Object configuredSlot = itemMap.getOrDefault("slot", -1);
        this.slot = configuredSlot instanceof Number ? ((Number) configuredSlot).intValue() : -1;
        this.material = Material.matchMaterial(String.valueOf(itemMap.getOrDefault("material", "AIR")));
        this.name = String.valueOf(itemMap.getOrDefault("name", ""));
        Object configuredLore = itemMap.getOrDefault("lore", List.of());
        this.lore = configuredLore instanceof List<?> list ? list.stream().map(String::valueOf).collect(Collectors.toList()) : List.of();
        Object configuredAction = itemMap.getOrDefault("action", Map.of());
        Map<String, Object> actionMap = configuredAction instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        this.actionType = String.valueOf(actionMap.getOrDefault("type", ""));
        this.server = String.valueOf(actionMap.getOrDefault("server", ""));
        this.address = String.valueOf(actionMap.getOrDefault("address", ""));
        this.command = String.valueOf(actionMap.getOrDefault("command", ""));
        this.console = Boolean.parseBoolean(String.valueOf(actionMap.getOrDefault("console", false)));
    }

    public ItemStack toItemStack() {
        return toItemStack(null);
    }

    public ItemStack toItemStack(Player player) {
        if (material == null) {
            return new ItemStack(Material.AIR);
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(toLegacy(applyPlaceholders(player, "<!italic>" + name)));

            if (lore != null && !lore.isEmpty()) {
                List<String> loreLines = lore.stream()
                        .map(line -> toLegacy(applyPlaceholders(player, "<!italic>" + line)))
                        .collect(Collectors.toList());
                meta.setLore(loreLines);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private static String applyPlaceholders(Player player, String text) {
        if (player != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public int getSlot() {
        return slot;
    }

    public Material getMaterial() {
        return material;
    }

    public String getActionType() {
        return actionType;
    }

    public String getServer() {
        return server;
    }

    public String getAddress() {
        return address;
    }

    private static String toLegacy(String miniMessageText) {
        return LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(miniMessageText));
    }

    public String getCommand() { return command; }

    public boolean isConsole() { return console; }

    public List<String> getLore() {
        return lore;
    }
}
