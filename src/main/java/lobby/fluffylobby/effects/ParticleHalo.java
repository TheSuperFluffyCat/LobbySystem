package lobby.fluffylobby.effects;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleHalo {

    private final Player player;
    private final Particle particle;
    private int taskID;

    public ParticleHalo(Player player, Particle particle) {
        this.player = player;
        this.particle = particle;
    }

    public void start() {
        taskID = new BukkitRunnable() {
            double radius = 0.33;
            double angle = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    stop();
                    return;
                }

                // Do not spawn world particles while the owner is vanished.
                // World-spawned particles are visible even when the player entity is hidden.
                if (FluffyLobby.getInstance().getVanishedPlayers().contains(player.getUniqueId())) {
                    return;
                }

                Location center = player.getLocation().add(0, 2.3, 0);
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location particleLoc = center.clone().add(x, 0, z);

                player.getWorld().spawnParticle(particle, particleLoc, 1, 0, 0, 0, 0);

                angle += Math.PI / 8;
                if (angle >= 2 * Math.PI) {
                    angle = 0;
                }
            }

            private void stop() {
                if (taskID != 0) {
                    player.getServer().getScheduler().cancelTask(taskID);
                }
            }
        }.runTaskTimer(FluffyLobby.getInstance(), 0, 2).getTaskId();
    }

    public void stop() {
        if (taskID != 0) {
            player.getServer().getScheduler().cancelTask(taskID);
        }
    }
}
