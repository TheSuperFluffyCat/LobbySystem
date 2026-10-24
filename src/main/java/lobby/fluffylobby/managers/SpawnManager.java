package lobby.fluffylobby.managers;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnManager {

    private final FluffyLobby plugin;

    public SpawnManager(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    public void setSpawn(Location location) {
        plugin.getConfig().set("spawn.world", location.getWorld().getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", location.getYaw());
        plugin.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
    }

    public Location getSpawn() {
        String worldName = plugin.getConfig().getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        double x = plugin.getConfig().getDouble("spawn.x", 0);
        double y = plugin.getConfig().getDouble("spawn.y", 72);
        double z = plugin.getConfig().getDouble("spawn.z", 0);
        float yaw = (float) plugin.getConfig().getDouble("spawn.yaw", 0);
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch", 0);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
