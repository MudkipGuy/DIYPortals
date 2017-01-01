
package com.mbach231.diyportals.database.portal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import net.arcation.arcadion.interfaces.Insertable;

/**
 *
 *
 */
public class PortalInsert implements Insertable {

    private final String uuid_;
    private final String citadelGroup_;
    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;

    public PortalInsert(String uuid, String citadelGroup, String world, int x, int y, int z) {
        uuid_ = uuid;
        citadelGroup_ = citadelGroup;
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {
        statement.setString(1, uuid_);
        if (citadelGroup_ != null) {
            statement.setString(2, citadelGroup_);
        } else {
            statement.setNull(2, Types.VARCHAR);
        }
        statement.setString(3, world_);
        statement.setInt(4, x_);
        statement.setInt(5, y_);
        statement.setInt(6, z_);
    }

    @Override
    public String getStatement() {
        return "INSERT INTO `diy_portals` (`col_player`,`col_citadel_group`,`col_world`,`col_block_x`,`col_block_y`,`col_block_z`) "
                    + "VALUES(?, ?, ?, ?, ?, ?)";
    }

}
