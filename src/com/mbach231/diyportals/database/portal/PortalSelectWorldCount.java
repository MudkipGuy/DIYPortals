
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
public class PortalSelectWorldCount implements Selectable {
    
    String world_;
    int count_;

    public PortalSelectWorldCount(String world) {
        world_ = world;
        count_ = 0;
    }

    @Override
    public boolean shouldCallbackAsync() {
        return false;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {
        statement.setString(1, world_);
    }

    @Override
    public String getQuery() {
        return "SELECT COUNT(*) FROM diy_portals WHERE col_world=?;";
    }

    @Override
    public void receiveResult(ResultSet results) {
        if (results != null) {
            try {
                while (results.next()) {
                    count_ = results.getInt(1);
                    return;
                }
            } catch (SQLException ex) {
                Logger.getLogger(PortalSelectWorldCount.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void callBack() {

    }

    public int getNumberOfPortalsInWorld() {
        return count_;
    }

}
