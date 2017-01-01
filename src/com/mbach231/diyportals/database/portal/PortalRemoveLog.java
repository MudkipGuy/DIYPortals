
package com.mbach231.diyportals.database.portal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import net.arcation.arcadion.interfaces.Insertable;

/**
 *
 *
 */
public class PortalRemoveLog implements Insertable {

    private final long time_;
    private final String uuid_;
    private final String world_;
    private final int x_;
    private final int y_;
    private final int z_;

    public PortalRemoveLog(String uuid, String world, int x, int y, int z) {
        time_ = System.currentTimeMillis();
        uuid_ = uuid;
        world_ = world;
        x_ = x;
        y_ = y;
        z_ = z;
    }

    @Override
    public void setParameters(PreparedStatement statement) throws SQLException {
        statement.setTimestamp(1, new Timestamp(time_));
        statement.setString(2, uuid_);
        statement.setString(3, world_);
        statement.setInt(4, x_);
        statement.setInt(5, y_);
        statement.setInt(6, z_);
    }

    @Override
    public String getStatement() {
        return "UPDATE tbl_end_portals SET col_destroyed_timestamp=?, col_destroyed_by=? WHERE col_world=? AND col_block_x=? AND col_block_y=? AND col_block_z=? AND col_destroyed_timestamp IS NULL and col_destroyed_by IS NULL;";
    }

}
