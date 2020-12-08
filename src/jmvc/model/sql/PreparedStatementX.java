package jmvc.model.sql;

import jmvc.model.ColumnInfo;

import java.sql.*;
import java.util.Arrays;
import java.util.function.Function;

import static gblibx.Util.castobj;
import static gblibx.Util.isNonNull;

/**
 * Manage PreparedStatement.
 */
public class PreparedStatementX implements AutoCloseable {
    public PreparedStatementX(String stmt, Integer[] positionByOrdinal, Function<Object, Integer> getOrdinal, ColumnInfo[] colInfo) {
        _stmt = stmt;
        _positionByOrdinal = positionByOrdinal;
        _getOrdinal = getOrdinal;
        _colInfo = colInfo;
        numPositions = (int)Arrays
                .stream(_positionByOrdinal)
                .filter(i -> (0 <= i))
                .count();
    }

    public PreparedStatement getPreparedStatement(Connection conn, int keys, Object... colVals) throws SQLException {
        return setPreparedStatement(conn, keys)
                .setValues(colVals)
                ._pstmt;
    }

    public PreparedStatement getPreparedStatement(Connection conn, Object... colVals) throws SQLException {
        return setPreparedStatement(conn)
                .setValues(colVals)
                ._pstmt;
    }

    private PreparedStatementX setPreparedStatement(Connection conn) throws SQLException {
        close();
        _pstmt = conn.prepareStatement(_stmt);
        return this;
    }

    private PreparedStatementX setPreparedStatement(Connection conn, int keys) throws SQLException {
        close();
        _pstmt = conn.prepareStatement(_stmt, keys);
        return this;
    }

    private void setValue(Object val, ColumnInfo col, int position) throws SQLException {
        if (0 > position) {
            throw new SQLException("Invalid position: " + position);
        }
        switch (col.type) {
            case Types.INTEGER:
                _pstmt.setInt(position, castobj(val));
                break;
            case Types.VARCHAR: //fall through
            case Types.CHAR:
                _pstmt.setString(position, castobj(val));
                break;
            case Types.TIMESTAMP:
                //v must be form: YYYY-MM-DD HH:MM:SS
                final String v = castobj(val);
                _pstmt.setTimestamp(position, Timestamp.valueOf(v));
                break;
            default:
                throw new SQLException("Invalid type: " + col.type);
        }
    }

    private PreparedStatementX setValues(Object... colVals) throws SQLException {
        for (int i = 0; i < colVals.length; ++i) {
            final int ordinal = _getOrdinal.apply(colVals[i++]);
            setValue(colVals[i], _colInfo[ordinal], _positionByOrdinal[ordinal]);
        }
        return this;
    }

    private final String _stmt;
    public final Integer[] _positionByOrdinal;
    private final Function<Object, Integer> _getOrdinal;
    private final ColumnInfo[] _colInfo;
    private PreparedStatement _pstmt = null;
    public final int numPositions;

    @Override
    public void close() {
        if (isNonNull(_pstmt)) {
            try {
                _pstmt.close();
            } catch (SQLException throwables) {
                ;//do nothing
            }
            _pstmt = null;
        }
    }
}
