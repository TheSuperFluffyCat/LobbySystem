package lobby.fluffylobby.effects;

import lobby.fluffylobby.FluffyLobby;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.*;

public class ParticleHaloManager {

    private final Map<UUID, ParticleHalo> activeHalos = new HashMap<>();

    // Special user particles
    private static final Map<UUID, Particle> customParticles = Map.of(
            UUID.fromString("84ce41ef-b8c0-4fa0-8c1c-71f152c2e5dc"), Particle.CHERRY_LEAVES,
            UUID.fromString("5e36e78e-ace2-4202-bba4-7f28c03c51b7"), Particle.WITCH,
            UUID.fromString("b8754e66-3916-4791-9408-a2197856bdc0"), Particle.WITCH,
            UUID.fromString("cf506198-070c-47ad-b0e3-09aa4ec4d866"), Particle.WITCH
    );

    public void startHalo(Player player) {
        stopHalo(player);

        if (!FluffyLobby.getInstance().getConfig().getBoolean("lobbyhalo")) return;

        Particle particle;

        // Check for special UUIDs
        UUID uuid = player.getUniqueId();
        if (customParticles.containsKey(uuid)) {
            particle = customParticles.get(uuid);
        } else {
            particle = player.hasPermission("lobbysystem.admincolor")
                    ? Particle.FLAME
                    : Particle.END_ROD;
        }

        ParticleHalo halo = new ParticleHalo(player, particle);
        halo.start();
        activeHalos.put(uuid, halo);
    }

    public void stopHalo(Player player) {
        ParticleHalo halo = activeHalos.remove(player.getUniqueId());
        if (halo != null) halo.stop();
    }

    public void clearAllHalos() {
        for (ParticleHalo halo : activeHalos.values()) {
            halo.stop();
        }
        activeHalos.clear();
    }

    public boolean hasHalo(Player player) {
        return activeHalos.containsKey(player.getUniqueId());
    }
}
