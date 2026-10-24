package lobby.fluffylobby.world;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.lang.reflect.Method;

import static org.bukkit.Bukkit.getScheduler;

public class worldsettings implements Listener {

    private final FluffyLobby plugin;
    private static final Method GAME_RULE_BY_NAME = findGameRuleLookup();

    public worldsettings(FluffyLobby plugin) {
        this.plugin = plugin;

        // Ensure all worlds apply the rules on plugin enable
        getScheduler().runTaskLater(plugin, this::applyGameRulesToAllWorlds, 20L);
    }

    /**
     * Applies game rules to the given world based on config settings.
     *
     * @param world The world to apply game rules to.
     */
    private void applyGameRules(World world) {
        boolean doDaylightCycle = plugin.getConfig().getBoolean("settings.GameRules.DO_DAYLIGHT_CYCLE", false);
        boolean doWeatherCycle = plugin.getConfig().getBoolean("settings.GameRules.DO_WEATHER_CYCLE", false);
        boolean doMobSpawning = plugin.getConfig().getBoolean("settings.GameRules.DO_MOB_SPAWNING", false);
        int randomTickSpeed = plugin.getConfig().getInt("settings.GameRules.RANDOM_TICK_SPEED", 0);
        boolean doEntityDrops = plugin.getConfig().getBoolean("settings.GameRules.DO_ENTITY_DROPS", false);
        boolean doTileDrops = plugin.getConfig().getBoolean("settings.GameRules.DO_TILE_DROPS", false);

        // The game-rule names were renamed in the 26.x API. Resolve both
        // spellings at runtime so one jar remains compatible with 1.21-26.3.
        setGameRule(world, "doDaylightCycle", "advance_time", doDaylightCycle);
        setGameRule(world, "doWeatherCycle", "advance_weather", doWeatherCycle);
        setGameRule(world, "doMobSpawning", "spawn_mobs", doMobSpawning);
        setGameRule(world, "randomTickSpeed", "random_tick_speed", randomTickSpeed);
        setGameRule(world, "doEntityDrops", "entity_drops", doEntityDrops);
        setGameRule(world, "doTileDrops", "block_drops", doTileDrops);


        // Color Pack placeholders for esay use
        String RESET = "\u001B[0m";
        String GREEN = "\u001B[32m";
        String YELLOW = "\u001B[33m";
        String BLUE = "\u001B[34m";
        String CYAN = "\u001B[36m";


        plugin.getLogger().info(GREEN + "Applied Lobby-GameRules for world: " + YELLOW + world.getName() + RESET);
    }

    @SuppressWarnings("unchecked")
    private <T> void setGameRule(World world, String legacyName, String modernName, T value) {
        GameRule<T> rule = (GameRule<T>) findGameRule(legacyName);
        if (rule == null) rule = (GameRule<T>) findGameRule(modernName);
        if (rule != null) {
            world.setGameRule(rule, value);
        }
    }

    private static Method findGameRuleLookup() {
        try {
            return GameRule.class.getMethod("getByName", String.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Object findGameRule(String name) {
        if (GAME_RULE_BY_NAME == null) return null;
        try {
            return GAME_RULE_BY_NAME.invoke(null, name);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    /**
     * Applies game rules to all currently loaded worlds.
     */
    private void applyGameRulesToAllWorlds() {
        for (World world : Bukkit.getWorlds()) {
            applyGameRules(world);
        }
    }

    /**
     * Event that triggers when a world is loaded, applying game rules.
     *
     * @param event The world load event.
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        applyGameRules(event.getWorld());
    }

    /**
     * Cancels all block physics updates if NoBlockUpdate is enabled in the config.
     *
     * @param event The BlockPhysicsEvent.
     */
    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (plugin.getConfig().getBoolean("settings.NoBlockUpdate", false)) {
            event.setCancelled(true);
        }
    }
}
