package jmvc.model.sql;

import gblibx.Util;
import jmvc.Exception;
import jmvc.model.ColumnInfo;
import jmvc.model.Database;
import jmvc.model.Table;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gblibx.Util.*;
import static java.util.Objects.isNull;

public class SqlTable<E extends Enum<E>> extends Table {

    public static <E extends Enum<E>> SqlTable create(Database dbase, String name, Class<E> cols) {
        return new SqlTable(name, cols, dbase);
    }

    private SqlTable(String name, Class<E> cols, Database dbase) {
        super(name, cols, dbase);
        super.initialize();
    }

    private SqlDatabase dbase() {
        return SqlDatabase.myDbase(_dbase);
    }

    private void close(Connection conn) {
        dbase().close(conn);
    }

    /**
     * Close ResultSet, Statement and Connection.
     * NOTE: only use at very end of connection use.
     *
     * @param rs
     */
    private void close(ResultSet rs) {
        dbase().close(rs);
    }

    private void close(Statement stmt) {
        dbase().close(stmt);
    }

    /**
     * Get DatabaseMetaData.
     * Resources are not closed here.
     *
     * @return DatabaseMetaData.
     */
    private DatabaseMetaData getMetaData() {
        final Connection conn = connection();
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
    protected void setColumnInfo() {
        _colInfo = new ColumnInfo[_config.length];
        final String tblName = upcase(name);
        Set<String> primaryKeys = new HashSet<>();
        ResultSet rs = null;
        SQLException exception = null;
        try {
            rs = getMetaData()
                    .getPrimaryKeys(null, null, tblName);
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
            rs = getMetaData()
                    .getColumns(null, null, tblName, null);
            while (rs.next()) {
                Util.Pair<String, ColumnInfo> info = create(rs, primaryKeys);
                _colInfo[getColInfoOrdinal(info.v1)] = info.v2;
            }
            //rs.close -> __close(rs) for thorough cleanup
        } catch (SQLException e) {
            exception = e;
        } finally {
            close(rs);
        }
        if (isNonNull(exception)) {
            throw new Exception.TODO(exception);
        }
    }

    private Util.Pair<String, ColumnInfo> create(ResultSet rs, Set<String> pk) {
        try {
            final String name = rs.getString("COLUMN_NAME");
            final ColumnInfo info = new ColumnInfo(
                    dbase(),
                    this,
                    name,
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
    protected void createTable() {
        StringBuilder xstmt = new StringBuilder(
                String.format("CREATE TABLE %s (", name));
        int i = 0;
        for (Enum<E> colSpec : super._config) {
            for (String c : getColumnSpec(colSpec))
                xstmt
                        .append((0 < i++) ? "," : "")
                        .append("\n")
                        .append(c);
        }
        xstmt.append(")");
        dbase().executeStatementNoResult(xstmt.toString());
    }

    @Override
    public int insertRow(Object... colVals) {
        if (isNull(_insertRow)) {
            _insertRow = new InsertRow<E>();
        }
        try {
            return _insertRow.execute(colVals);
        } catch (SQLException ex) {
            //TODO: need to deal w/ exception here, since we dont propagate exception
            //through method signature.
            throw new Exception.TODO(ex);
        }
    }

    private InsertRow _insertRow = null;

    @Override
    public int updateTableById(int id, Object... colVals) {
        if (isNull(_updateTableById)) {
            _updateTableById = new UpdateTableById();
        }
        try {
            int rval = _updateTableById.execute(id, colVals);
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

    @Override
    public Object findById(Stream ids, boolean orderByIdAsc) {
        String csv = ((Stream<Integer>)ids)
                .map(e -> e.toString())
                .collect(Collectors.joining(","));
        String query = String.format(
          "SELECT * FROM %s WHERE ID in (%s) ORDER BY ID %s",
          name, csv, ((orderByIdAsc) ? "ASC" : "DESC")
        );
        return dbase().query(query);
    }

    private UpdateTableById _updateTableById = null;

    private Connection connection() {
        return dbase().getConnection();
    }

    private static void executeUpdate(PreparedStatement pstmt) throws SQLException {
        int rowCnt = pstmt.executeUpdate();
        if (1 != rowCnt) {
            throw new Exception.TODO("Expected 1 row");
        }
    }

    /**
     * Bookeeping for INSERT prepared statement
     */
    private class InsertRow<E extends Enum<E>> {
        private InsertRow() {
            initialize();
        }

        private int execute(Object... colVals) throws SQLException {
            if ((2 * __pstmt.numPositions) != colVals.length) {
                throw new Exception.TODO("Invalid # of values");
            }
            final Connection conn = connection();
            PreparedStatement pstmt = (__hasID)
                    ? __pstmt.getPreparedStatement(conn, Statement.RETURN_GENERATED_KEYS, colVals)
                    : __pstmt.getPreparedStatement(conn, colVals);
            executeUpdate(pstmt);
            final int id = (__hasID) ? getResultId(pstmt) : -1;
            __pstmt.close();
            return id;
        }

        private void initialize() {
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
            __pstmt = new PreparedStatementX(stmt.toString(), positionByOrdinal, getOrdinal, _colInfo);
        }

        private PreparedStatementX __pstmt;
        private int __numPositions = 0;
        private boolean __hasID = false;
    }

    private static int getResultId(PreparedStatement pstmt) throws SQLException {
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

    private final Function<Object, Integer> getOrdinal = new Function<Object, Integer>() {
        @Override
        public Integer apply(Object o) {
            final E col = castobj(o);
            if (!isValidColumn(col)) {
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
    private class UpdateTableById<E extends Enum<E>> {
        private UpdateTableById() {
            final int id = getColInfoOrdinal("ID", false);
            if (0 > id) {
                throw new Exception.TODO("Table does not have ID column");
            }
        }

        private int execute(int id, Object... xcolVals) throws SQLException {
            PreparedStatementX pstmtx = getPstmt(xcolVals);
            //Then add ID
            Object[] colVals = append(xcolVals, getEnumOfCol("ID"), id);
            PreparedStatement pstmt = pstmtx.getPreparedStatement(connection(), Statement.RETURN_GENERATED_KEYS, colVals);
            executeUpdate(pstmt);
            final int rid = getResultId(pstmt);
            pstmtx.close();
            return rid;
        }

        /**
         * Get PreparedStatement for columns in colVals.
         *
         * @param colVals pairs of col,value.
         * @return PreparedStatement.
         */
        private PreparedStatementX getPstmt(Object... colVals) throws SQLException {
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
                if (hasColumn("UPDATED_AT")) {
                    if (0 < currPos) stmt.append(',');
                    stmt.append("UPDATED_AT=CURRENT_TIMESTAMP");
                }
                stmt.append(" WHERE ID=?");
                positionByOrdinal[getColInfoOrdinal("ID")] = ++currPos;
                __pstmtByCol.put(key, new PreparedStatementX(stmt.toString(), positionByOrdinal, getOrdinal, _colInfo));
            }
            return __pstmtByCol.get(key);
        }

        private Map<EnumSet<E>, PreparedStatementX> __pstmtByCol = new HashMap<>();
    }
}
