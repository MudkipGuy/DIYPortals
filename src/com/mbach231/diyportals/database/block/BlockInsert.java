
package com.mbach231.diyportals.database.block;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import net.arcation.arcadion.interfaces.Insertable;

/**
 *
 *
 */
public class BlockInsert implements Insertable {

    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;

    public BlockInsert(String world, int x, int y, int z) {
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
        return "INSERT INTO `custom_portal_blocks` (`col_world`,`col_block_x`,`col_block_y`,`col_block_z`) "
                    + "VALUES(?, ?, ?, ?)";
    }

}
