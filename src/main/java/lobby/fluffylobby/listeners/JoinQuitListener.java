package lobby.fluffylobby.listeners;

import lobby.fluffylobby.effects.ParticleHaloManager;
import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.managers.SpawnManager;
import lobby.fluffylobby.utils.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Listener to handle player join, quit, and respawn events.
 */
public class JoinQuitListener implements Listener {

    private final FluffyLobby plugin;
    private final ParticleHaloManager particleHaloManager;
    private final SpawnManager spawnManager;

    public JoinQuitListener(FluffyLobby plugin, ParticleHaloManager particleHaloManager, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.particleHaloManager = particleHaloManager;
        this.spawnManager = spawnManager;

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Re-apply vanish visibility because a newly joined player does not know
        // about players hidden before their connection was established.
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (plugin.getVanishedPlayers().contains(online.getUniqueId()) && !online.equals(player)) {
                player.hidePlayer(plugin, online);
            }
        }
        if (plugin.getVanishedPlayers().contains(player.getUniqueId())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player)) online.hidePlayer(plugin, player);
            }
            event.setJoinMessage(null);
        }

        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("health.enabled", false)) {
            double hearts = config.getDouble("health.hearts", 10.0);
            double maxHearts = config.getDouble("health.max_hearts", 20.0);
            String effect = config.getString("health.effect", "none").toLowerCase();

            // Clamp and apply health values
            player.setMaxHealth(Math.max(1.0, Math.min(maxHearts, 2048.0)));
            player.setHealth(Math.max(1.0, Math.min(hearts, player.getMaxHealth())));

            // Apply visual effect on hearts
            switch (effect) {
                case "wither" -> player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 0, false, false));
                case "poison" -> player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 0, false, false));
                case "none" -> {
                    player.removePotionEffect(PotionEffectType.WITHER);
                    player.removePotionEffect(PotionEffectType.POISON);
                }
            }
        }

        // Check if halo is enabled in the config
        if (plugin.getConfig().getBoolean("lobbyhalo", true)) {
            particleHaloManager.startHalo(player);
        }



        // Teleport player to spawn and clear inventory
        player.teleport(spawnManager.getSpawn());
        player.getInventory().clear();

        // Load lobby items for the player
        plugin.getLobbyItemManager().loadLobbyItems(player);

        // Handle join message from configuration
        String joinMessage = plugin.getConfig().getString("messages.join_message", "<yellow>Welcome, <gradient:#32CD32:#7FFF00>%player%</gradient>!</yellow>");
        joinMessage = joinMessage.replace("%player%", player.getName());
        if (plugin.isPlaceholderAPIEnabled()) {
            joinMessage = PlaceholderAPI.setPlaceholders(player, joinMessage.replace("%player%", player.getName()));
        }
        String formattedJoinMessage = MessageUtils.formatMessage(joinMessage, player, plugin);

        // Set the formatted join message as a Component
        event.setJoinMessage(formattedJoinMessage);

        // Launch firework after a delay
        if (plugin.getConfig().getBoolean("joinfirework", true)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> launchJoinFirework(player.getLocation()), 30L); // 2 seconds delay
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Stop the particle halo for the player
        particleHaloManager.stopHalo(player);

        // Handle quit message from configuration
        String quitMessage = plugin.getConfig().getString("messages.quit_message", "<red><gradient:#FF4444:#FF8844>%player%</gradient> <gray>has left the server.</gray>");
        quitMessage = quitMessage.replace("%player%", player.getName());
        if (plugin.isPlaceholderAPIEnabled()) {
            quitMessage = PlaceholderAPI.setPlaceholders(player, quitMessage.replace("%player%", player.getName())); //Needs some rework here douple convertation not good
        }
        String formattedQuitMessage = MessageUtils.formatMessage(quitMessage, player, plugin);

        // Set the formatted quit message as a Component
        event.setQuitMessage(plugin.getVanishedPlayers().contains(player.getUniqueId()) ? null : formattedQuitMessage);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Set respawn location to the spawn
        event.setRespawnLocation(spawnManager.getSpawn());
    }

    private void launchJoinFirework(Location location) {
        // Create a firework at the specified location
        Firework firework = location.getWorld().spawn(location, Firework.class);

        // Create the firework effect
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.AQUA, Color.FUCHSIA)
                .withFade(Color.BLUE, Color.PURPLE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build();

        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(effect);
        fireworkMeta.setPower(1); // Set flight duration
        firework.setFireworkMeta(fireworkMeta);

        // Detonate immediately for visual effect
        firework.detonate();
    }
}
