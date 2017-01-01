
package com.mbach231.diyportals;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * 
 */
public class ConfigManager {
    
    private static Set<String> worldSet_;
    
    private static int maxPortalsPerWorld_;
    private static int maxPortalsPerPlayer_;
    private static int minDistanceBetweenPortals_;
    private static int maxPortalSearchDistance_;
    
    private static Material customBlockMaterial_;
    private static ItemStack customPortalBlock_;
    
    ConfigManager(FileConfiguration config) {
        worldSet_ = new HashSet(config.getStringList("WorldsCanBuildPortals"));
        maxPortalsPerWorld_ = config.getInt("MaxPortalsPerWorld");
        maxPortalsPerPlayer_ = config.getInt("MaxPortalsPerPlayer");
        minDistanceBetweenPortals_ = config.getInt("MinDistanceBetweenPortals");
        maxPortalSearchDistance_ = config.getInt("MaxPortalSearchDistance");
        
        if(maxPortalsPerWorld_ == 0) {
            maxPortalsPerWorld_ = Integer.MAX_VALUE;
        }
        
        if(maxPortalsPerPlayer_ == 0) {
            maxPortalsPerPlayer_ = Integer.MAX_VALUE;
        }
        
        if(maxPortalSearchDistance_ == 0) {
            maxPortalSearchDistance_ = Integer.MAX_VALUE;
        }
        
        customBlockMaterial_ = Material.valueOf(config.getString("CustomPortalBlock.Type"));
        
        customPortalBlock_ = new ItemStack(customBlockMaterial_, 1);
        ItemMeta meta = customPortalBlock_.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + config.getString("CustomPortalBlock.Name"));
        customPortalBlock_.setItemMeta(meta);
    }
    
    public static boolean isValidWorld(String worldName) {
        return worldSet_.contains(worldName);
    }
    
    public static int getMaxPortalsPerWorld() {
        return maxPortalsPerWorld_;
    }
    
    public static int getMaxPortalsPerPlayer() {
        return maxPortalsPerPlayer_;
    }
    
    public static int getMinDistanceBetweenPortals() {
        return minDistanceBetweenPortals_;
    }
    
    public static int getMaxPortalSearchDistance() {
        return maxPortalSearchDistance_;
    }
    
    public static Material getCustomBlockMaterial() {
        return customBlockMaterial_;
    }
    
    public static ItemStack getCustomPortalBlock() {
        return customPortalBlock_;
    }
}
