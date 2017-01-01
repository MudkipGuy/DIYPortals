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
public class PortalSelectByFrame implements Selectable {

    private PortalInfo portalInfo_;

    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;

    public PortalSelectByFrame(String world, int x, int y, int z) {
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;

        portalInfo_ = null;
    }

    @Override
    public boolean shouldCallbackAsync() {
        return false;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {
        statement.setString(1, world_);
        statement.setInt(2, y_);

        statement.setInt(3, x_);
        statement.setInt(4, z_);
    }

    @Override
    public String getQuery() {
        return "SELECT * FROM diy_portals WHERE col_world=? AND col_block_y=? ORDER BY (SQRT(POW(col_block_x-?,2) + POW(col_block_z-?,2)) < 3) DESC LIMIT 1;";
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

                    int diffX = Math.abs(x_ - portalX);
                    int diffZ = Math.abs(z_ - portalZ);

                    if ((diffX == 0 && diffZ == 2)
                            || (diffX == 1 && diffZ == 2)
                            || (diffX == 2 && diffZ == 0)
                            || (diffX == 2 && diffZ == 1)) {

                        portalInfo_ = new PortalInfo(player, group, world_, portalX, y_, portalZ);
                    }

                }
            } catch (SQLException ex) {
                Logger.getLogger(PortalSelectByFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void callBack() {

    }

    public PortalInfo getPortalnfo() {
        return portalInfo_;
    }

}
