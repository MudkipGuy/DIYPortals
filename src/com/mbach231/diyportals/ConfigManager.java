package com.mbach231.diyportals;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
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
    private static boolean enableNameLayerGroupChecking_;

    private static Material customBlockMaterial_;
    private static ItemStack customPortalBlock_;
    private static Recipe customBlockRecipe_;

    ConfigManager(FileConfiguration config) {
        worldSet_ = new HashSet(config.getStringList("WorldsCanBuildPortals"));
        maxPortalsPerWorld_ = config.getInt("MaxPortalsPerWorld");
        maxPortalsPerPlayer_ = config.getInt("MaxPortalsPerPlayer");
        minDistanceBetweenPortals_ = config.getInt("MinDistanceBetweenPortals");
        maxPortalSearchDistance_ = config.getInt("MaxPortalSearchDistance");
        enableNameLayerGroupChecking_ = config.getBoolean("EnableNameLayerGroupChecking");
        
        if (maxPortalsPerWorld_ == 0) {
            maxPortalsPerWorld_ = Integer.MAX_VALUE;
        }

        if (maxPortalsPerPlayer_ == 0) {
            maxPortalsPerPlayer_ = Integer.MAX_VALUE;
        }

        if (maxPortalSearchDistance_ == 0) {
            maxPortalSearchDistance_ = Integer.MAX_VALUE;
        }

        customBlockMaterial_ = Material.valueOf(config.getString("CustomPortalBlock.Type"));

        customPortalBlock_ = new ItemStack(customBlockMaterial_, 1);
        ItemMeta meta = customPortalBlock_.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + config.getString("CustomPortalBlock.Name"));
        List<String> lore = config.getStringList("CustomPortalBlock.Lore");
        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }
        
        if (config.getBoolean("CustomPortalBlock.Glow")) {
            try {
                Field f = Enchantment.class.getDeclaredField("acceptingNew");
                f.setAccessible(true);
                f.set(null, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Glow glow = new Glow(231);
                Enchantment.registerEnchantment(glow);
                meta.addEnchant(glow, 1, true);
            } catch (IllegalArgumentException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        customPortalBlock_.setItemMeta(meta);

        if (config.getString("CustomPortalBlock.Recipe").equalsIgnoreCase("ShapedRecipe")) {
            ShapedRecipe recipe = new ShapedRecipe(customPortalBlock_);
            recipe.shape(config.getStringList("CustomPortalBlock.ShapedRecipe.Shape").toArray(new String[0]));
            for (String materialStr : config.getConfigurationSection("CustomPortalBlock.ShapedRecipe.Ingredients").getValues(false).keySet()) {
                recipe.setIngredient(config.getString("CustomPortalBlock.ShapedRecipe.Ingredients." + materialStr).charAt(0), Material.valueOf(materialStr));
            }
            customBlockRecipe_ = recipe;
        } else if (config.getString("CustomPortalBlock.Recipe").equalsIgnoreCase("ShapelessRecipe")) {
            ShapelessRecipe recipe = new ShapelessRecipe(customPortalBlock_);
            for (String materialStr : config.getConfigurationSection("CustomPortalBlock.ShapelessRecipe.Ingredients").getValues(false).keySet()) {
                recipe.addIngredient(config.getInt("CustomPortalBlock.ShapelessRecipe.Ingredients." + materialStr), Material.valueOf(materialStr));
            }
            customBlockRecipe_ = recipe;
        }

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
    
    public static boolean getEnableNameLayerGroupChecking() {
        return enableNameLayerGroupChecking_;
    }

    public static Material getCustomBlockMaterial() {
        return customBlockMaterial_;
    }

    public static ItemStack getCustomPortalBlock() {
        return customPortalBlock_;
    }

    public static Recipe getCustomBlockRecipe() {
        return customBlockRecipe_;
    }
}
