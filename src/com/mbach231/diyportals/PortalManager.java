package com.mbach231.diyportals;

import com.mbach231.diyportals.database.DatabaseInterface;
import com.mbach231.diyportals.database.portal.PortalInfo;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.EntityEnderSignal;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.SoundCategory;
import net.minecraft.server.v1_10_R1.SoundEffects;
import net.minecraft.server.v1_10_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEnderSignal;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.NameAPI;

/**
 *
 *
 */
public class PortalManager {

    private final DatabaseInterface database_;
    private final Material customBlockMaterial_;
    private final boolean enableNameLayerGroupChecking_;

    PortalManager() {

        database_ = new DatabaseInterface();
        customBlockMaterial_ = ConfigManager.getCustomBlockMaterial();
        enableNameLayerGroupChecking_ = ConfigManager.getEnableNameLayerGroupChecking();
    }

    public boolean initialized() {
        return database_.initialized();
    }

    public boolean playerCanUsePortal(Player player) {
        Location loc = player.getLocation();
        PortalInfo portalInfo = DatabaseInterface.getNearbyPortal(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 3);
        if (portalInfo.getGroup() != null && enableNameLayerGroupChecking_) {
            if (NameAPI.getGroupManager().getAllGroupNames(player.getUniqueId()).contains(portalInfo.getGroup())) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    public void handlePlaceFrame(Player player, Block block) {

        Location centerBlock = centerOfPortalCheck(getCenterBlockOfCompletedPortal(block));

        if (centerBlock != null) {

            if (!ConfigManager.isValidWorld(centerBlock.getWorld().getName())) {
                player.sendMessage("Portals cannot be created in this world!");
                return;
            }

            if (DatabaseInterface.getNumberOfPortalsForPlayer(player.getUniqueId().toString()) >= ConfigManager.getMaxPortalsPerPlayer()) {
                player.sendMessage("You cannot create another portal!");
                return;
            }

            if (DatabaseInterface.getNumberOfPortalsInWorld(centerBlock.getWorld().getName()) >= ConfigManager.getMaxPortalsPerWorld()) {
                player.sendMessage("Cannot create another portal in this world!");
                return;
            }

            if (DatabaseInterface.getNearbyPortal(centerBlock.getWorld().getName(), centerBlock.getBlockX(), centerBlock.getBlockY(), centerBlock.getBlockZ(), ConfigManager.getMinDistanceBetweenPortals()) != null) {
                player.sendMessage("Cannot create a portal here, too close to another portal!");
                return;
            }

            String groupName = null;

            if (enableNameLayerGroupChecking_) {
                Set<String> groupNameSet = getGroupNamesFromFrame(centerBlock);

                if (groupNameSet.size() == 1) {
                    for (String name : groupNameSet) {
                        groupName = name;
                    }
                }
            }
            
            boolean portalAdded = database_.addPortal(player.getUniqueId().toString(), groupName, centerBlock.getWorld().getName(), centerBlock.getBlockX(), centerBlock.getBlockY(), centerBlock.getBlockZ());

            if (portalAdded) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        centerBlock.getWorld().getBlockAt(centerBlock.clone().add(x, 0, z)).setType(Material.ENDER_PORTAL);
                    }
                }

                if (groupName == null) {
                    player.sendMessage("Portal created!");
                } else {
                    player.sendMessage("Portal created for group " + groupName + "!");
                }
            }
        }
    }

    public void handleFrameBreak(Player player, Block block) {
        Location portalLoc = getCenterBlockOfCompletedPortal(block);

        if (portalLoc != null) {

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (!portalLoc.clone().add(x, 0, z).getBlock().getType().equals(Material.ENDER_PORTAL)) {
                        return;
                    }
                }
            }

            DatabaseInterface.removePortal(player.getUniqueId().toString(), portalLoc.getWorld().getName(), portalLoc.getBlockX(), portalLoc.getBlockY(), portalLoc.getBlockZ());

            Block centerBlock = block.getWorld().getBlockAt(portalLoc.getBlockX(), portalLoc.getBlockY(), portalLoc.getBlockZ());
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    centerBlock.getRelative(x, 0, z).setType(Material.AIR);
                }
            }
        }
    }

    // This function was taken from EnderEyeChanger plugin and modified
    // to better fit our needs, but all credit for the core logic of this
    // function goes to them.
    public void onUseEyeOfEnder(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        EntityPlayer entity = ((CraftPlayer) player).getHandle();

        if (event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == customBlockMaterial_) {
                return;
            }
        }

        event.setCancelled(true); // If block clicked was End Portal Frame, the event is ignored and item is used normally.
        event.setUseItemInHand(Event.Result.DENY); // This makes sure we don't get duplicate ender eyes spawning (happens if this isn't set to 'deny')

        // Get nearest target location
        Location target = getNearestLocation(player.getLocation());
        if (target == null) {
            player.sendMessage("No nearby portals found!");
            return;
        }

        // convert to Minecraft BlockPosition
        BlockPosition targetpos = new BlockPosition(target.getX(), target.getY(), target.getZ());

        // Get the location to spawn the ender signal at
        Location signalLocation = player.getLocation(); // The ender signal spawns at the player's eye height
        signalLocation.setY(signalLocation.getY() + player.getEyeHeight());

        // Spawn the ender signal + get EntityEnderSignal handle
        EntityEnderSignal eye = ((CraftEnderSignal) event.getPlayer().getWorld().spawn(signalLocation, EnderSignal.class
        )).getHandle();

        World w = ((CraftWorld) event.getPlayer().getWorld()).getHandle();

        w.a(entity, entity.locX, entity.locY, entity.locZ, SoundEffects.aT, SoundCategory.NEUTRAL,
                0.5F, 0.4F / ((new Random()).nextFloat() * 0.4F + 0.8F));

        eye.a(targetpos);

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack item = event.getItem();
        EquipmentSlot slot = event.getHand();

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            item = null;
        }

        if (slot == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(item);
        } else if (slot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(item);
        } else {
            Bukkit.getLogger().log(Level.FINE, "Ender eye thrown from " + slot.toString() + "!");
        }

    }

    // Given a portal frame block, determine where the center of the portal would be if this portal is complete
    private Location getCenterBlockOfCompletedPortal(Block block) {
        Location loc = block.getLocation();

        if (loc.clone().add(-1, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {

            if (loc.clone().add(-2, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                if (loc.clone().add(0, 0, 4).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(-1, 0, 2);
                } else if (loc.clone().add(0, 0, -4).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(-1, 0, -2);
                }
            } else if (loc.clone().add(1, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                if (loc.clone().add(0, 0, 4).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(0, 0, 2);
                } else if (loc.clone().add(0, 0, -4).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(0, 0, -2);
                }
            }
        } else if (loc.clone().add(1, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {

            if (loc.clone().add(2, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                if (loc.clone().add(0, 0, 4).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(1, 0, 2);
                } else if (loc.clone().add(0, 0, -4).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(1, 0, -2);
                }
            }
        } else if (loc.clone().add(0, 0, -1).getBlock().getType().equals(customBlockMaterial_)) {

            if (loc.clone().add(0, 0, -2).getBlock().getType().equals(customBlockMaterial_)) {

                if (loc.clone().add(4, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(2, 0, -1);
                } else if (loc.clone().add(-4, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(-2, 0, -1);
                }
            } else if (loc.clone().add(0, 0, 1).getBlock().getType().equals(customBlockMaterial_)) {

                if (loc.clone().add(4, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(2, 0, 0);
                } else if (loc.clone().add(-4, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(-2, 0, 0);
                }
            }
        } else if (loc.clone().add(0, 0, 1).getBlock().getType().equals(customBlockMaterial_)) {
            if (loc.clone().add(0, 0, 2).getBlock().getType().equals(customBlockMaterial_)) {
                if (loc.clone().add(4, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(2, 0, 1);
                } else if (loc.clone().add(-4, 0, 0).getBlock().getType().equals(customBlockMaterial_)) {
                    return loc.clone().add(-2, 0, 1);
                }
            }
        }

        return null;
    }

    // Get all group reinforcement names from all portal frame blocks for a given portal
    private Set<String> getGroupNamesFromFrame(Location loc) {

        Set<String> nameSet = new HashSet();

        for (int x = -2; x <= 2; x += 4) {
            for (int z = -1; z <= 1; z += 1) {

                Reinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(loc.clone().add(x, 0, z));

                if (reinforcement != null) {
                    if (reinforcement instanceof PlayerReinforcement) {
                        nameSet.add(((PlayerReinforcement) reinforcement).getGroup().getName());
                    }
                }
            }
        }

        for (int z = -2; z <= 2; z += 4) {
            for (int x = -1; x <= 1; x += 1) {
                Reinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(loc.clone().add(x, 0, z));

                if (reinforcement != null) {
                    if (reinforcement instanceof PlayerReinforcement) {
                        nameSet.add(((PlayerReinforcement) reinforcement).getGroup().getName());
                    }
                }
            }
        }

        return nameSet;

    }

    // Checks if location is valid center of portal. Ensures frame is built and the middle is clear of blocks.
    private Location centerOfPortalCheck(Location loc) {

        if (loc == null) {
            return null;
        }

        String world = loc.getWorld().getName();
        int centerX = loc.getBlockX();
        int centerY = loc.getBlockY();
        int centerZ = loc.getBlockZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (!loc.clone().add(x, 0, z).getBlock().getType().equals(Material.AIR)) {
                    return null;
                }
            }
        }

        for (int x = -2; x <= 2; x += 4) {
            for (int z = -1; z <= 1; z += 1) {
                if (!DatabaseInterface.isCustomBlock(world, centerX + x, centerY, centerZ + z)) {
                    return null;
                }

            }
        }

        for (int z = -2; z <= 2; z += 4) {
            for (int x = -1; x <= 1; x += 1) {
                if (!DatabaseInterface.isCustomBlock(world, centerX + x, centerY, centerZ + z)) {
                    return null;
                }

            }
        }

        return loc;
    }

    // Returns the location of the nearest portal for given location
    private Location getNearestLocation(Location loc) {

        PortalInfo portalLoc = DatabaseInterface.getNearbyPortal(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), ConfigManager.getMaxPortalSearchDistance());

        if (portalLoc != null) {
            return loc.getWorld().getBlockAt(portalLoc.getX(), portalLoc.getY(), portalLoc.getZ()).getLocation();
        } else {
            return null;
        }

    }

}
