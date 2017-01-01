

package com.mbach231.diyportals.database.portal;

/**
 *
 * 
 */
public class PortalInfo {

    private final String player_;
    private final String group_;
    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;
    
    public PortalInfo(String player, String group, String world, int x, int y, int z) {
        player_ = player;
        group_ = group;
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;
    }
    
    public String getPlayer() {
        return player_;
    }

    public String getGroup() {
        return group_;
    }
    
    /**
     * @return the world_
     */
    public String getWorld() {
        return world_;
    }

    /**
     * @return the x_
     */
    public int getX() {
        return x_;
    }

    /**
     * @return the y_
     */
    public int getY() {
        return y_;
    }

    /**
     * @return the z_
     */
    public int getZ() {
        return z_;
    }
    
}
