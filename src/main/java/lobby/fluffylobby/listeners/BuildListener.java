package lobby.fluffylobby.listeners;

import lobby.fluffylobby.FluffyLobby;
import lobby.fluffylobby.commands.BuildCommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class BuildListener implements Listener {

    private final FluffyLobby plugin;

    public BuildListener(FluffyLobby plugin) {
        this.plugin = plugin;
    }

    private static final List<Material> blockedInteractions = Arrays.asList(
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
            Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE,
            Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX,
            Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL,
            Material.OAK_DOOR, Material.BIRCH_DOOR, Material.SPRUCE_DOOR, Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.IRON_DOOR,
            Material.OAK_TRAPDOOR, Material.BIRCH_TRAPDOOR, Material.SPRUCE_TRAPDOOR,
            Material.JUNGLE_TRAPDOOR, Material.ACACIA_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.IRON_TRAPDOOR,
            Material.OAK_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.DARK_OAK_FENCE_GATE
    );

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (!BuildCommand.canBuild(player) && clickedBlock != null) {
            Material blockType = clickedBlock.getType();

            if (blockedInteractions.contains(blockType)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!BuildCommand.canBuild(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!BuildCommand.canBuild(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!BuildCommand.canBuild(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (!BuildCommand.canBuild(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (!BuildCommand.canBuild(player)) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                return; // GUI z.B. Villager-Shop
            }
            event.setCancelled(true); // Kein Sortieren etc.
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!BuildCommand.canBuild(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!BuildCommand.canBuild(player)) {
            FileConfiguration config = plugin.getConfig();
            if (config.getBoolean("hunger.override", true)) {
                int foodLevel = config.getInt("hunger.level", 20);
                boolean saturation = config.getBoolean("hunger.saturation", true);

                player.setFoodLevel(foodLevel);
                if (saturation) {
                    player.setSaturation(20.0f);
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (!BuildCommand.canBuild(player)) {
            event.setCancelled(true);
        }
    }
}
