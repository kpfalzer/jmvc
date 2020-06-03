package jmvc.model.sql;

import gblibx.Util;
import jmvc.Exception;
import jmvc.model.Database;
import jmvc.model.Table;

import java.sql.SQLException;

import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static gblibx.Util.*;
import static java.util.Objects.isNull;

public class SqlTable extends Table {
    public static SqlTable create(Database dbase, String name, Enum[] cols) {
        return new SqlTable(name, cols, dbase);
    }

    private SqlTable(String name, Enum[] cols, Database dbase) {
        super(name, cols, dbase);
    }

    private SqlDatabase __dbase() {
        return SqlDatabase.myDbase(_dbase);
    }

    private DatabaseMetaData __getMetaData() {
        try {
            return __dbase()
                    .getMyConnection()
                    .getMetaData();
        } catch (SQLException e) {
            throw new Exception.TODO(e);
        }
    }

    @Override
    protected void _setColumnInfo() {
        _colInfo = new ColInfo[_config.length];
        final String tblName = upcase(name);
        Set<String> primaryKeys = new HashSet<>();
        try {
            ResultSet rs = __getMetaData()
                    .getPrimaryKeys(null, null, tblName);
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
            rs = __getMetaData()
                    .getColumns(null, null, tblName, null);
            while (rs.next()) {
                Util.Pair<String, ColInfo> info = __create(rs, primaryKeys);
                _colInfo[_getColInfoIx(info.v1)] = info.v2;
            }
        } catch (SQLException e) {
            throw new Exception.TODO(e);
        }
    }

    private static Util.Pair<String, ColInfo> __create(ResultSet rs, Set<String> pk) {
        try {
            final String name = rs.getString("COLUMN_NAME");
            final ColInfo info = new ColInfo(
                    rs.getInt("DATA_TYPE"),
                    rs.getInt("COLUMN_SIZE"),
                    rs.getInt("ORDINAL_POSITION"),
                    rs.getString("COLUMN_DEF"),
                    pk.contains(name)
            );
            return new Util.Pair<>(name, info);
        } catch (SQLException e) {
            throw new Exception.TODO(e);
        }
    }

    @Override
    protected void _createTable() {
        StringBuilder xstmt = new StringBuilder(
                String.format("CREATE TABLE %s (", name));
        int i = 0;
        for (Enum colSpec : super._config) {
            for (String c : _getColumnSpec(colSpec))
                xstmt
                        .append((0 < i++) ? "," : "")
                        .append("\n")
                        .append(c);
        }
        xstmt.append(")");
        try {
            _dbase.executeStatement(xstmt.toString());
        } catch (Exception ex) {
            throw new Exception.TODO(ex);
        }
    }

    @Override
    public int insertRow(Object... colVals) {
        if (isNull(__insertRow)) {
            __insertRow = new InsertRow();
        }
        try {
            return __insertRow.execute(colVals);
        } catch (SQLException ex) {
            //TODO: need to deal w/ exception here, since we dont propagate exception
            //through method signature.
            throw new Exception.TODO(ex);
        }
    }

    private static InsertRow __insertRow = null;

    private static void __setValue(PreparedStatement pstmt, Object val, ColInfo col, int position) throws SQLException {
        if (0 > position) {
            throw new SQLException("Invalid position: " + position);
        }
        switch (col.type) {
            case Types.INTEGER:
                pstmt.setInt(position, castobj(val));
                break;
            case Types.VARCHAR:
                pstmt.setString(position, castobj(val));
                break;
            default:
                throw new SQLException("Invalid type: " + col.type);
        }
    }

    private Connection __connection() {
        return __dbase().getMyConnection();
    }

    /**
     * Bookeeping for INSERT prepared statement
     */
    private class InsertRow {
        private InsertRow() {
            __initialize();
        }

        private int execute(Object... colVals) throws SQLException {
            if ((2 * __numPositions) != colVals.length) {
                throw new Exception.TODO("Invalid # of values");
            }
            PreparedStatement pstmt = __pstmt;
            for (int i = 0; i < colVals.length; ++i) {
                final Enum col = castobj(colVals[i++]);
                final int ordinal = col.ordinal();
                __setValue(pstmt, colVals[i], _colInfo[ordinal], __positionByOrdinal[ordinal]);
            }
            int rowCnt = pstmt.executeUpdate();
            if (1 != rowCnt) {
                throw new SQLException("Expected 1 row");
            }
            int rval = -1;
            if (__hasID) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int colCnt = rs.getMetaData().getColumnCount();
                    if (1 != colCnt) {
                        throw new SQLException("Unexpected column count: " + colCnt);
                    }
                    rval = rs.getInt(1);
                }
            }
            return rval;
        }

        private void __initialize() {
            __positionByOrdinal = new int[_config.length];
            List<String> colNames = new LinkedList<>();
            for (Enum col : _config) {
                final int ordinal = col.ordinal();
                if (!_colInfo[ordinal].hasDefaultVal()) {
                    colNames.add(col.name().toUpperCase());
                    __positionByOrdinal[ordinal] = ++__numPositions;
                } else {
                    __positionByOrdinal[ordinal] = -1;
                    __hasID |= col.name().equalsIgnoreCase("ID");
                }
            }
            StringBuilder stmt = new StringBuilder();
            stmt
                    .append("INSERT INTO ")
                    .append(name)
                    .append('(')
                    .append(join(colNames, ","))
                    .append(')')
                    .append(" VALUES(")
                    .append(join(arrayFill(new String[colNames.size()], "?"), ","))
                    .append(')');
            try {
                if (__hasID) {
                    __pstmt = __connection().prepareStatement(stmt.toString(), Statement.RETURN_GENERATED_KEYS);
                } else {
                    __pstmt = __connection().prepareStatement(stmt.toString());
                }
            } catch (SQLException ex) {
                throw new Exception.TODO(ex);
            }
        }

        /**
         * Position in PreparedStatement by ordinal (of enum value).
         * Or, <0 for names/columns NOT in PreparedStatement.
         */
        private int[] __positionByOrdinal;
        private PreparedStatement __pstmt;
        private int __numPositions = 0;
        private boolean __hasID = false;
    }

}
