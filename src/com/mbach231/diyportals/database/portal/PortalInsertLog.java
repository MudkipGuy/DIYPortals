
package com.mbach231.diyportals.database.portal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import net.arcation.arcadion.interfaces.Insertable;

/**
 *
 *
 */
public class PortalInsertLog implements Insertable {

    private final long time_;
    private final String uuid_;
    private final String citadelGroup_;
    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;

    public PortalInsertLog(String uuid, String citadelGroup, String world, int x, int y, int z) {
        time_ = System.currentTimeMillis();
        uuid_ = uuid;
        citadelGroup_ = citadelGroup;
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {
        statement.setTimestamp(1, new Timestamp(time_));
        statement.setString(2, uuid_);
        
        statement.setNull(3, Types.TIMESTAMP);
        statement.setNull(4, Types.VARCHAR);
        
        if (citadelGroup_ != null) {
            statement.setString(5, citadelGroup_);
        } else {
            statement.setNull(5, Types.VARCHAR);
        }
        
        statement.setString(6, world_);
        statement.setInt(7, x_);
        statement.setInt(8, y_);
        statement.setInt(9, z_);
    }

    @Override
    public String getStatement() {
        return "INSERT INTO `tbl_end_portals` (`col_created_timestamp`,`col_created_by`,`col_destroyed_timestamp`,`col_destroyed_by`,`col_citadel_group`,`col_world`,`col_block_x`,`col_block_y`,`col_block_z`) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

}
