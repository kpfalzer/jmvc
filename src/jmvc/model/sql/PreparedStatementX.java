package jmvc.model.sql;

import jmvc.model.Table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.function.Function;

import static gblibx.Util.castobj;
import static gblibx.Util.isNonNull;

/**
 * Manage PreparedStatement.
 */
public class PreparedStatementX implements AutoCloseable {
    public PreparedStatementX(String stmt, Integer[] positionByOrdinal, Function<Object, Integer> getOrdinal, Table.ColInfo[] colInfo) {
        __stmt = stmt;
        __positionByOrdinal = positionByOrdinal;
        __getOrdinal = getOrdinal;
        __colInfo = colInfo;
        numPositions = (int)Arrays
                .stream(__positionByOrdinal)
                .filter(i -> (0 <= i))
                .count();
    }

    public PreparedStatement getPreparedStatement(Connection conn, int keys, Object... colVals) throws SQLException {
        return setPreparedStatement(conn, keys)
                .setValues(colVals)
                .__pstmt;
    }

    public PreparedStatement getPreparedStatement(Connection conn, Object... colVals) throws SQLException {
        return setPreparedStatement(conn)
                .setValues(colVals)
                .__pstmt;
    }

    private PreparedStatementX setPreparedStatement(Connection conn) throws SQLException {
        close();
        __pstmt = conn.prepareStatement(__stmt);
        return this;
    }

    private PreparedStatementX setPreparedStatement(Connection conn, int keys) throws SQLException {
        close();
        __pstmt = conn.prepareStatement(__stmt, keys);
        return this;
    }

    private void setValue(Object val, Table.ColInfo col, int position) throws SQLException {
        if (0 > position) {
            throw new SQLException("Invalid position: " + position);
        }
        switch (col.type) {
            case Types.INTEGER:
                __pstmt.setInt(position, castobj(val));
                break;
            case Types.VARCHAR:
                __pstmt.setString(position, castobj(val));
                break;
            default:
                throw new SQLException("Invalid type: " + col.type);
        }
    }

    private PreparedStatementX setValues(Object... colVals) throws SQLException {
        for (int i = 0; i < colVals.length; ++i) {
            final int ordinal = __getOrdinal.apply(colVals[i++]);
            setValue(colVals[i], __colInfo[ordinal], __positionByOrdinal[ordinal]);
        }
        return this;
    }

    private final String __stmt;
    public final Integer[] __positionByOrdinal;
    private final Function<Object, Integer> __getOrdinal;
    private final Table.ColInfo[] __colInfo;
    private PreparedStatement __pstmt = null;
    public final int numPositions;

    @Override
    public void close() {
        if (isNonNull(__pstmt)) {
            SqlDatabase.sclose(__pstmt);
            __pstmt = null;
        }
    }
}
