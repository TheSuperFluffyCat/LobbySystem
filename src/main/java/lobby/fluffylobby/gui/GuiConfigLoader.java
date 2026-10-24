package lobby.fluffylobby.gui;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class GuiConfigLoader {

    private final FluffyLobby plugin;
    private final Map<String, GuiMenu> menus = new HashMap<>();
    private final Map<String, GuiMenu> menusByTitle = new HashMap<>();
    private File configFile;
    private YamlConfiguration config;

    public GuiConfigLoader(FluffyLobby plugin) {
        this.plugin = plugin;
        ensureConfigExists();
        loadConfig();
    }

    private void ensureConfigExists() {
        configFile = new File(plugin.getDataFolder(), "guimaker.yml");
        if (!configFile.exists()) {
            plugin.saveResource("guimaker.yml", false);
            plugin.getLogger().info("guimaker.yml has been created.");
        }
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.getLogger().severe("guimaker.yml not found!");
            return;
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection menuSection = config.getConfigurationSection("menus");
        if (menuSection == null) {
            plugin.getLogger().severe("No 'menus' section found in guimaker.yml!");
            return;
        }

        menus.clear(); // Vorhandene Menüs löschen, bevor sie neu geladen werden
        menusByTitle.clear();
        for (String key : menuSection.getKeys(false)) {
            ConfigurationSection menuConfig = menuSection.getConfigurationSection(key);
            if (menuConfig != null) {
                menus.put(key, new GuiMenu(menuConfig));
                menusByTitle.put(menus.get(key).getTitle(), menus.get(key));
                plugin.getLogger().info("Loaded menu: " + key);
            }
        }

        if (menus.isEmpty()) {
            plugin.getLogger().severe("No menus loaded from guimaker.yml!");
        } else {
            plugin.getLogger().info(menus.size() + " menus loaded successfully.");
        }
    }

    // Neue reload()-Methode
    public void reload() {
        ensureConfigExists();
        loadConfig();
        plugin.getLogger().info("guimaker.yml has been reloaded.");
    }

    public Map<String, GuiMenu> getMenuList() {
        return Collections.unmodifiableMap(menus);
    }

    public GuiMenu getMenu(String menuName) {
        return menus.get(menuName);
    }

    public GuiMenu getMenuByTitle(String title) {
        return menusByTitle.get(title);
    }
}
