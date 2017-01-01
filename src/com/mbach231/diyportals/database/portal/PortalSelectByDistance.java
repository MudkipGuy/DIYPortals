
package com.mbach231.diyportals.database.portal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.arcation.arcadion.interfaces.Selectable;

/**
 *
 *
 */
public class PortalSelectByDistance implements Selectable {

    private PortalInfo portalInfo_;

    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;
    private final int dist_;

    public PortalSelectByDistance(String world, int x, int y, int z, int dist) {
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;
        dist_ = dist;

        portalInfo_ = null;
    }

    @Override
    public boolean shouldCallbackAsync() {
        return false;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {
        statement.setString(1, world_);
        statement.setInt(2, x_);
        statement.setInt(3, z_);

    }

    @Override
    public String getQuery() {
        return "SELECT * FROM diy_portals WHERE col_world=? ORDER BY (SQRT(POW(col_block_x-?,2) + POW(col_block_z-?,2))) ASC LIMIT 1;";

    }

    @Override
    public void receiveResult(ResultSet results) {
        if (results != null) {
            try {
                while (results.next()) {
                    String player = results.getString("col_player");
                    String group = results.getString("col_citadel_group");
                    int portalX = results.getInt("col_block_x");
                    int portalZ = results.getInt("col_block_z");

                    if (Math.sqrt(Math.pow(portalX - x_, 2) + Math.pow(portalZ - z_, 2)) <= dist_) {
                        portalInfo_ = new PortalInfo(player, group, world_, portalX, y_, portalZ);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(PortalSelectByDistance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void callBack() {

    }

    public PortalInfo getPortalLocation() {
        return portalInfo_;
    }

}
