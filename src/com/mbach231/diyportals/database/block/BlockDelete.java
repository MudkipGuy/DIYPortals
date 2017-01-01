
package com.mbach231.diyportals.database.block;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.arcation.arcadion.interfaces.Insertable;

/**
 *
 *
 */
public class BlockDelete implements Insertable {

    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;

    public BlockDelete(String world, int x, int y, int z) {
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {

        statement.setString(1, world_);
        statement.setInt(2, x_);
        statement.setInt(3, y_);
        statement.setInt(4, z_);
    }

    @Override
    public String getStatement() {
        return "DELETE FROM custom_portal_blocks WHERE col_world=? AND col_block_x=? AND col_block_y=? AND col_block_z=?;";
    }

}
