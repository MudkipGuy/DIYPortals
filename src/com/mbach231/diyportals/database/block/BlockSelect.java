package com.mbach231.diyportals.database.block;

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
public class BlockSelect implements Selectable {

    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;
    
    private boolean isCustomBlock_;

    public BlockSelect(String world, int x, int y, int z) {
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;
        
        isCustomBlock_ = false;
    }

    @Override
    public boolean shouldCallbackAsync() {
        return false;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {
        statement.setString(1, world_);
        statement.setInt(2, x_);
        statement.setInt(3, y_);
        statement.setInt(4, z_);
    }

    @Override
    public String getQuery() {
        return "SELECT * FROM custom_portal_blocks WHERE col_world=? AND col_block_x=? AND col_block_y=? AND col_block_z=?;";
    }

    @Override
    public void receiveResult(ResultSet results) {
        if (results != null) {
            try {
                if (results.next()) {
                    isCustomBlock_ = true;
                }
            } catch (SQLException ex) {
                Logger.getLogger(BlockSelect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void callBack() {

    }

    
    public boolean isCustomBlock() {
        return isCustomBlock_;
    }

}
