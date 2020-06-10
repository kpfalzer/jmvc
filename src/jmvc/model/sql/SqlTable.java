package jmvc.model.sql;

import gblibx.Util;
import jmvc.Exception;
import jmvc.model.Database;
import jmvc.model.Table;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

import static gblibx.Util.*;
import static java.util.Objects.isNull;

public class SqlTable<E extends Enum<E>> extends Table {

    public static <E extends Enum<E>> SqlTable create(Database dbase, String name, Class<E> cols) {
        return new SqlTable(name, cols, dbase);
    }

    private SqlTable(String name, Class<E> cols, Database dbase) {
        super(name, cols, dbase);
    }

    private SqlDatabase __dbase() {
        return SqlDatabase.myDbase(_dbase);
    }

    private void __close(Connection conn) {
        __dbase().close(conn);
    }

    /**
     * Close ResultSet, Statement and Connection.
     * NOTE: only use at very end of connection use.
     *
     * @param rs
     */
    private void __close(ResultSet rs) {
        __dbase().close(rs);
    }

    private void __close(Statement stmt) {
        __dbase().close(stmt);
    }

    /**
     * Get DatabaseMetaData.
     * Resources are not closed here.
     *
     * @return DatabaseMetaData.
     */
    private DatabaseMetaData __getMetaData() {
        final Connection conn = __connection();
        SQLException exception = null;
        DatabaseMetaData dmd = null;
        try {
            dmd = conn.getMetaData();
        } catch (SQLException e) {
            exception = e;
        }
        if (isNonNull(exception)) {
            throw new Exception.TODO(exception);
        }
        return dmd;
    }

    @Override
    protected void _setColumnInfo() {
        _colInfo = new ColInfo[_config.length];
        final String tblName = upcase(name);
        Set<String> primaryKeys = new HashSet<>();
        ResultSet rs = null;
        SQLException exception = null;
        try {
            rs = __getMetaData()
                    .getPrimaryKeys(null, null, tblName);
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
            rs = __getMetaData()
                    .getColumns(null, null, tblName, null);
            while (rs.next()) {
                Util.Pair<String, ColInfo> info = __create(rs, primaryKeys);
                _colInfo[_getColInfoOrdinal(info.v1)] = info.v2;
            }
            //rs.close -> __close(rs) for thorough cleanup
        } catch (SQLException e) {
            exception = e;
        } finally {
            __close(rs);
        }
        if (isNonNull(exception)) {
            throw new Exception.TODO(exception);
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
        for (Enum<E> colSpec : super._config) {
            for (String c : _getColumnSpec(colSpec))
                xstmt
                        .append((0 < i++) ? "," : "")
                        .append("\n")
                        .append(c);
        }
        xstmt.append(")");
        __dbase().executeStatementNoResult(xstmt.toString());
    }

    @Override
    public int insertRow(Object... colVals) {
        if (isNull(__insertRow)) {
            __insertRow = new __InsertRow<E>();
        }
        try {
            return __insertRow.__execute(colVals);
        } catch (SQLException ex) {
            //TODO: need to deal w/ exception here, since we dont propagate exception
            //through method signature.
            throw new Exception.TODO(ex);
        }
    }

    private __InsertRow __insertRow = null;

    @Override
    public int updateTableById(int id, Object... colVals) {
        if (isNull(__updateTableById)) {
            __updateTableById = new __UpdateTableById();
        }
        try {
            int rval = __updateTableById.__execute(id, colVals);
            // We expect 0, since we cannot return value in MySql
            if (0 != rval) {
                throw new Exception.TODO("Expected 0");
            }
            //return same id
            return id;
        } catch (SQLException ex) {
            //TODO: need to deal w/ exception here, since we dont propagate exception
            //through method signature.
            //TODO: log message;
            return -1;
        }
    }

    private __UpdateTableById __updateTableById = null;

    private Connection __connection() {
        return __dbase().getConnection();
    }

    private static void __executeUpdate(PreparedStatement pstmt) throws SQLException {
        int rowCnt = pstmt.executeUpdate();
        if (1 != rowCnt) {
            throw new Exception.TODO("Expected 1 row");
        }
    }

    /**
     * Bookeeping for INSERT prepared statement
     */
    private class __InsertRow<E extends Enum<E>> {
        private __InsertRow() {
            __initialize();
        }

        private int __execute(Object... colVals) throws SQLException {
            if ((2 * __pstmt.numPositions) != colVals.length) {
                throw new Exception.TODO("Invalid # of values");
            }
            final Connection conn = __connection();
            PreparedStatement pstmt = (__hasID)
                    ? __pstmt.getPreparedStatement(conn, Statement.RETURN_GENERATED_KEYS, colVals)
                    : __pstmt.getPreparedStatement(conn, colVals);
            __executeUpdate(pstmt);
            final int id = (__hasID) ? __getResultId(pstmt) : -1;
            __pstmt.close();
            return id;
        }

        private void __initialize() {
            Integer[] positionByOrdinal = new Integer[_config.length];
            List<String> colNames = new LinkedList<>();
            for (Enum col : _config) {
                final int ordinal = col.ordinal();
                if (!_colInfo[ordinal].hasDefaultVal()) {
                    colNames.add(col.name().toUpperCase());
                    positionByOrdinal[ordinal] = ++__numPositions;
                } else {
                    positionByOrdinal[ordinal] = -1;
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
                    .append(')')
            ;
            __pstmt = new PreparedStatementX(stmt.toString(), positionByOrdinal, __getOrdinal, _colInfo);
        }

        private PreparedStatementX __pstmt;
        private int __numPositions = 0;
        private boolean __hasID = false;
    }

    private static int __getResultId(PreparedStatement pstmt) throws SQLException {
        int rval = -1;
        ResultSet rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
            int colCnt = rs.getMetaData().getColumnCount();
            if (1 != colCnt) {
                throw new Exception.TODO("Unexpected column count: " + colCnt);
            }
            rval = rs.getInt(1);
            rs.close();
        }
        return rval;
    }

    private final Function<Object, Integer> __getOrdinal = new Function<Object, Integer>() {
        @Override
        public Integer apply(Object o) {
            final E col = castobj(o);
            if (!_isValidColumn(col)) {
                throw new Exception.TODO("Invalid column: " + col.name());
            }
            return col.ordinal();
        }
    };

    /**
     * Bookeeping for UPDATE...WHERE ID=... prepared statement
     *
     * @param <E>
     */
    private class __UpdateTableById<E extends Enum<E>> {
        private __UpdateTableById() {
            final int id = _getColInfoOrdinal("ID", false);
            if (0 > id) {
                throw new Exception.TODO("Table does not have ID column");
            }
        }

        private int __execute(int id, Object... xcolVals) throws SQLException {
            PreparedStatementX pstmtx = __getPstmt(xcolVals);
            //Then add ID
            Object[] colVals = append(xcolVals, _getEnumOfCol("ID"), id);
            PreparedStatement pstmt = pstmtx.getPreparedStatement(__connection(), Statement.RETURN_GENERATED_KEYS, colVals);
            __executeUpdate(pstmt);
            final int rid = __getResultId(pstmt);
            pstmtx.close();
            return rid;
        }

        /**
         * Get PreparedStatement for columns in colVals.
         *
         * @param colVals pairs of col,value.
         * @return PreparedStatement.
         */
        private PreparedStatementX __getPstmt(Object... colVals) throws SQLException {
            List<E> cols = new LinkedList();
            for (int i = 0; i < colVals.length; i += 2) {
                cols.add(castobj(colVals[i]));
            }
            EnumSet<E> key = EnumSet.copyOf(cols);
            if (!__pstmtByCol.containsKey(key)) {
                Integer[] positionByOrdinal = arrayFill(new Integer[_config.length], -1);
                int currPos = 0;
                StringBuilder stmt = new StringBuilder();
                stmt
                        .append("UPDATE ")
                        .append(name)
                        .append(" SET ")
                ;
                for (E col : cols) {
                    if (0 < currPos)
                        stmt.append(',');
                    stmt
                            .append(col.name().toUpperCase())
                            .append("=?");
                    final int ordinal = col.ordinal();
                    positionByOrdinal[ordinal] = ++currPos;
                }
                if (_hasColumn("UPDATED_AT")) {
                    if (0 < currPos) stmt.append(',');
                    stmt.append("UPDATED_AT=CURRENT_TIMESTAMP");
                }
                stmt.append(" WHERE ID=?");
                positionByOrdinal[_getColInfoOrdinal("ID")] = ++currPos;
                __pstmtByCol.put(key, new PreparedStatementX(stmt.toString(), positionByOrdinal, __getOrdinal, _colInfo));
            }
            return __pstmtByCol.get(key);
        }

        private Map<EnumSet<E>, PreparedStatementX> __pstmtByCol = new HashMap<>();
    }
}
