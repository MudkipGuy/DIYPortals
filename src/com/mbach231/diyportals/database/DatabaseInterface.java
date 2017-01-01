package com.mbach231.diyportals.database;

import com.mbach231.diyportals.database.block.*;
import com.mbach231.diyportals.database.portal.*;
import java.sql.SQLException;
import net.arcation.arcadion.interfaces.Arcadion;
import net.arcation.arcadion.interfaces.DatabaseManager;

/**
 *
 *
 */
public class DatabaseInterface {

    private static boolean initialized_;
    private static Arcadion arcadion_;

    public DatabaseInterface() {
        arcadion_ = DatabaseManager.getArcadion();
        initialized_ = true;

        createTable();
    }

    private void createTable() {
        String tableStatement = "CREATE TABLE IF NOT EXISTS `diy_portals` ("
                + "`col_player` varchar(255) NOT NULL,"
                + "`col_citadel_group` varchar(255) DEFAULT NULL,"
                + "`col_world` varchar(255) NOT NULL,"
                + "`col_block_x` int(11) NOT NULL,"
                + "`col_block_y` int(11) NOT NULL,"
                + "`col_block_z` int(11) NOT NULL,"
                + "KEY `col_citadel_group` (`col_citadel_group`),"
                + "KEY `col_world` (`col_world`),"
                + "KEY `col_block_x` (`col_block_x`),"
                + "KEY `col_block_y` (`col_block_y`),"
                + "KEY `col_block_z` (`col_block_z`));";

        String logTableStatement = "CREATE TABLE IF NOT EXISTS `tbl_end_portals` ("
                + "`col_id` int(11) NOT NULL AUTO_INCREMENT,"
                + "`col_created_timestamp` timestamp NULL DEFAULT NULL,"
                + "`col_created_by` varchar(255) NOT NULL,"
                + "`col_destroyed_timestamp` timestamp NULL DEFAULT NULL,"
                + "`col_destroyed_by` varchar(255) DEFAULT NULL,"
                + "`col_citadel_group` varchar(255) DEFAULT NULL,"
                + "`col_world` varchar(255) NOT NULL,"
                + "`col_block_x` int(11) NOT NULL,"
                + "`col_block_y` int(11) NOT NULL,"
                + "`col_block_z` int(11) NOT NULL,"
                + "PRIMARY KEY (`col_id`),"
                + "KEY `col_created_by` (`col_created_by`),"
                + "KEY `col_destroyed_by` (`col_destroyed_by`),"
                + "KEY `col_citadel_group` (`col_citadel_group`),"
                + "KEY `col_world` (`col_world`),"
                + "KEY `col_block_x` (`col_block_x`),"
                + "KEY `col_block_y` (`col_block_y`),"
                + "KEY `col_block_z` (`col_block_z`));";

        String customBlockTableStatement = "CREATE TABLE IF NOT EXISTS `custom_portal_blocks` ("
                + "`col_world` varchar(255) NOT NULL,"
                + "`col_block_x` int(11) NOT NULL,"
                + "`col_block_y` int(11) NOT NULL,"
                + "`col_block_z` int(11) NOT NULL,"
                + "KEY `col_world` (`col_world`),"
                + "KEY `col_block_x` (`col_block_x`),"
                + "KEY `col_block_y` (`col_block_y`),"
                + "KEY `col_block_z` (`col_block_z`));";

        try {
            arcadion_.getConnection().prepareStatement(tableStatement).execute();
            arcadion_.getConnection().prepareStatement(logTableStatement).execute();
            arcadion_.getConnection().prepareStatement(customBlockTableStatement).execute();
        } catch (SQLException ex) {
            initialized_ = false;
            ex.printStackTrace();
        }
    }

    public boolean initialized() {
        return initialized_;
    }
    
    public static void addCustomBlock(String world, int x, int y, int z) {
        arcadion_.insert(new BlockInsert(world, x, y, z));
    }
    
    public static void removeCustomBlock(String world, int x, int y, int z) {
        arcadion_.insert(new BlockDelete(world, x, y, z));
    }
    
    public static boolean isCustomBlock(String world, int x, int y, int z) {
        BlockSelect blockSelect = new BlockSelect(world, x, y, z);
        arcadion_.select(blockSelect);   
        return blockSelect.isCustomBlock();
    }
    
    

    public boolean addPortal(String uuid, String citadelGroup, String world, int x, int y, int z) {
        if (getPortalByFrame(world, x, y, z) == null) {
            arcadion_.insert(new PortalInsert(uuid, citadelGroup, world, x, y, z));
            arcadion_.queueAsyncInsertable(new PortalInsertLog(uuid, citadelGroup, world, x, y, z));
            return true;
        } else {
            return false;
        }
    }

    public PortalInfo handleBrokenPortalFrame(String uuid, String world, int x, int y, int z) {

        PortalInfo portalInfo = getPortalByFrame(world, x, y, z);

        if (portalInfo != null) {
            removePortal(uuid, world, portalInfo.getX(), y, portalInfo.getZ());
            return portalInfo;
        }

        return null;
    }

    public static PortalInfo getPortalByFrame(String world, int x, int y, int z) {
        PortalSelectByFrame select = new PortalSelectByFrame(world, x, y, z);
        arcadion_.select(select);
        return select.getPortalnfo();
    }

    public static PortalInfo getNearbyPortal(String world, int x, int y, int z, int dist) {
        PortalSelectByDistance select = new PortalSelectByDistance(world, x, y, z, dist);
        arcadion_.select(select);
        return select.getPortalLocation();
    }

    public static void removePortal(String uuid, String world, int x, int y, int z) {
        arcadion_.insert(new PortalDelete(world, x, y, z));
        arcadion_.queueAsyncInsertable(new PortalRemoveLog(uuid, world, x, y, z));
        
    }

    public static int getNumberOfPortalsInWorld(String world) {
        PortalSelectWorldCount select = new PortalSelectWorldCount(world);
        arcadion_.select(select);
        return select.getNumberOfPortalsInWorld();
    }

    public static int getNumberOfPortalsForPlayer(String uuid) {
        PortalSelectPlayerCount select = new PortalSelectPlayerCount(uuid);
        arcadion_.select(select);
        return select.getNumberOfPortalsForPlayer();
    }

}
