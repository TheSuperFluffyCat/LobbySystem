package lobby.fluffylobby.listeners;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.managers.SpawnManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;

public class PlayerMovementListener implements Listener {

    private final FluffyLobby plugin;
    private final SpawnManager spawnManager;
    private boolean invalidRescueHeightLogged;

    private static final Set<Material> PRESSURE_PLATES = EnumSet.of(
            Material.STONE_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.MANGROVE_PRESSURE_PLATE,
            Material.CHERRY_PRESSURE_PLATE,
            Material.BAMBOO_PRESSURE_PLATE,
            Material.CRIMSON_PRESSURE_PLATE,
            Material.WARPED_PRESSURE_PLATE,
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE
    );

    public PlayerMovementListener(FluffyLobby plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;

        FileConfiguration config = plugin.getConfig();
        boolean save = false;

        if (!config.contains("boostpads.enabled")) {
            config.set("boostpads.enabled", true);
            save = true;
        }
        if (!config.contains("boostpads.forward")) {
            config.set("boostpads.forward", 1.5);
            save = true;
        }
        if (!config.contains("boostpads.upward")) {
            config.set("boostpads.upward", 0.4);
            save = true;
        }

        if (save) {
            plugin.saveConfig();
        }
    }

    private void warnInvalidRescueHeight(String message) {
        if (!invalidRescueHeightLogged) {
            plugin.getLogger().warning(message);
            invalidRescueHeightLogged = true;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        // Use a configured rescue height when valid; otherwise follow this world's build height.
        FileConfiguration config = plugin.getConfig();
        int minHeight = loc.getWorld().getMinHeight();
        String configuredY = config.getString("settings.void_rescue.y", "min");
        double rescueY = minHeight;
        if (configuredY != null && !configuredY.equalsIgnoreCase("min")) {
            try {
                rescueY = Double.parseDouble(configuredY);
            } catch (NumberFormatException exception) {
                warnInvalidRescueHeight("Invalid settings.void_rescue.y value '" + configuredY
                        + "'; using each world's minimum height.");
            }
        }
        if (!Double.isFinite(rescueY)) {
            warnInvalidRescueHeight("settings.void_rescue.y must be a finite number or 'min'; using each world's minimum height.");
            rescueY = minHeight;
        }
        if (config.getBoolean("settings.void_rescue.enabled", true) && loc.getY() < rescueY) {
            Location spawn = spawnManager.getSpawn();
            if (spawn != null) {
                player.teleport(spawn);
            }
            return;
        }

        // === Boostpads ===
        if (!config.getBoolean("boostpads.enabled")) return;

        Block block = loc.getBlock();
        if (!PRESSURE_PLATES.contains(block.getType())) return;

        double forward = config.getDouble("boostpads.forward", 1.5);
        double upward = config.getDouble("boostpads.upward", 0.4);

        Vector boost = loc.getDirection().setY(0).normalize().multiply(forward);
        boost.setY(upward);
        player.setVelocity(boost);
    }
}
