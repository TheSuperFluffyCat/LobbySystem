package lobby.fluffylobby.listeners;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.bukkit.Location;

public class EnderPearlListener implements Listener {

    private final FluffyLobby plugin;

    public EnderPearlListener(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack bow = event.getBow();

            if (bow != null && bow.getType().toString().equalsIgnoreCase("BOW")) {
                // Cancel the arrow shot
                event.setCancelled(true);

                // Launch an ender pearl
                EnderPearl enderPearl = player.launchProjectile(EnderPearl.class);
                enderPearl.setVelocity(event.getProjectile().getVelocity());
                plugin.getLobbyItemManager().loadLobbyItems(player);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getType() == EntityType.ENDER_PEARL && projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            // Save player's current yaw and pitch
            float currentYaw = player.getLocation().getYaw();
            float currentPitch = player.getLocation().getPitch();
            // Teleport player to the ender pearl location without changing the yaw and pitch
            Location targetLocation = projectile.getLocation();
            targetLocation.setYaw(currentYaw);
            targetLocation.setPitch(currentPitch);
            player.teleport(targetLocation);
        }
    }
}
