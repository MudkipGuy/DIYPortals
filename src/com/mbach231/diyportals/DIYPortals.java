package com.mbach231.diyportals;

import com.mbach231.diyportals.database.DatabaseInterface;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World.Environment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 *
 */
public class DIYPortals extends JavaPlugin implements Listener {

    ConfigManager configManager_;
    PortalManager portalManager_;
    ItemStack customPortalBlock_;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        configManager_ = new ConfigManager(this.getConfig());
        customPortalBlock_ = ConfigManager.getCustomPortalBlock();
        addPortalFrameRecipe();
        portalManager_ = new PortalManager();

        if (portalManager_.initialized()) {
            getServer().getPluginManager().registerEvents(this, this);
        } else {
            getLogger().log(Level.INFO, "Portal DB connection failed to initialize.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void addPortalFrameRecipe() {

        Recipe recipe = ConfigManager.getCustomBlockRecipe();

        if (recipe != null) {
            getServer().addRecipe(recipe);
        } else {
            getLogger().log(Level.INFO, "Failed to load recipe!");
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.END_PORTAL) {
            if (!portalManager_.playerCanUsePortal(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        
        if (event.getItemInHand().getItemMeta().equals(customPortalBlock_.getItemMeta())) {
            String world = event.getBlock().getWorld().getName();
            int x = event.getBlock().getLocation().getBlockX();
            int y = event.getBlock().getLocation().getBlockY();
            int z = event.getBlock().getLocation().getBlockZ();

            DatabaseInterface.addCustomBlock(world, x, y, z);
            portalManager_.handlePlaceFrame(event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(customPortalBlock_.getType())) {

            String world = event.getBlock().getWorld().getName();
            int x = event.getBlock().getLocation().getBlockX();
            int y = event.getBlock().getLocation().getBlockY();
            int z = event.getBlock().getLocation().getBlockZ();

            if (DatabaseInterface.isCustomBlock(world, x, y, z)) {
                DatabaseInterface.removeCustomBlock(world, x, y, z);
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);

                if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), customPortalBlock_);
                }

                portalManager_.handleFrameBreak(event.getPlayer(), event.getBlock());
            }
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getItem() == null || event.getPlayer().getWorld().getEnvironment() != Environment.NORMAL) {
            return;
        }

        if (event.getItem().getType() == Material.EYE_OF_ENDER) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                portalManager_.onUseEyeOfEnder(event);
            }
        }
    }
}
