package jmvc.model.sql;

import gblibx.Util;
import jmvc.JmvcException;
import jmvc.model.ColumnInfo;
import jmvc.model.Database;
import jmvc.model.QueryResult;
import jmvc.model.Select;
import jmvc.model.Table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gblibx.Util.append;
import static gblibx.Util.arrayFill;
import static gblibx.Util.castobj;
import static gblibx.Util.invariant;
import static gblibx.Util.isEven;
import static gblibx.Util.isNonNull;
import static gblibx.Util.join;
import static gblibx.Util.logException;
import static gblibx.Util.upcase;
import static java.util.Objects.isNull;

public class SqlTable<E extends Enum<E>> extends Table {

    public static <E extends Enum<E>> SqlTable
    create(Database dbase, String name, Class<E> cols) {
        return create(dbase,name,cols,new String[0]);
    }

    public static <E extends Enum<E>> SqlTable
    create(Database dbase, String name, Class<E> cols, String... colIndex) {
        SqlTable tbl = new SqlTable(name, cols, dbase);
        tbl.createIndex(colIndex);
        return tbl;
    }

    public static <E extends Enum<E>> SqlTable
    create(Database dbase, String name, Class<E> cols, E... colIndex) {
        return create(dbase,name,cols,Arrays.stream(colIndex).map(e->e.toString()).toArray(String[]::new));
    }

    private SqlTable(String name, Class<E> cols, Database dbase) {
        super(name, cols, dbase);
        super.initialize();
    }

    private SqlDatabase dbase() {
        return SqlDatabase.myDbase(_dbase);
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
            throw new JmvcException.TODO(exception);
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
        } catch (SQLException e) {
            exception = e;
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                exception = e;
            }
        }
        if (isNonNull(exception)) {
            throw new JmvcException.TODO(exception);
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
            throw new JmvcException.TODO(e);
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

    private int error(SQLException ex) {
        logException(ex);
        return -1;
    }

    @Override
    public int insertRow(Object... colVals) {
        try {
            return __insertRow(colVals);
        } catch (SQLException ex) {
            //TODO: need to deal w/ exception here, since we dont propagate exception
            //through method signature.
            return error(ex);
        }
    }

    @Override
    public Select select(String... cols) {
        return new SqlSelect(this, cols);
    }

    @Override
    public Select selectDistinct(String... cols) {
        return new SqlSelect(this, true, cols);
    }

    @Override
    public void createIndex(String... cols) {
        for (String col : cols) {
            String xstmt = String.format("CREATE INDEX %s_IX_ON_%s ON %s(%s)",
                    col, name, name, col);
            dbase().executeStatementNoResult(xstmt.toString());
        }
    }

    @Override
    public int updateTableById(int id, Object... colVals) {
        if (isNull(_updateTableById)) {
            _updateTableById = new UpdateTableById();
        }
        try {
            int rval = _updateTableById.execute(id, colVals);
            // We expect 0, since we cannot return value in MySql
            if (0 != rval) {
                throw new JmvcException.TODO("Expected 0");
            }
            //return same id
            return id;
        } catch (SQLException ex) {
            //TODO: need to deal w/ exception here, since we dont propagate exception
            //through method signature.
            //TODO: log message;
            return error(ex);
        }
    }

    @Override
    public QueryResult executeQuery(String statement) {
        return SqlQueryResult.executeQuery(dbase(), statement);
    }

    @Override
    public QueryResult executeQuery(String statement, Function forEachRow) {
        return SqlQueryResult.executeQuery(dbase(), statement, forEachRow);
    }

    @Override
    public Object findById(Stream ids, boolean orderByIdAsc) {
        String csv = ((Stream<Integer>) ids)
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

    private int executeUpdate(PreparedStatement pstmt) throws SQLException {
        int rowCnt = 0, rval = -1;
        //debug: boolean[] isClosed = {false, false, false};
        try {
            //debug: isClosed[0] = pstmt.isClosed();
            rowCnt = pstmt.executeUpdate();
            //debug: isClosed[1] = pstmt.isClosed();
            {
                try {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    //debug: isClosed[2] = rs.isClosed();
                    if (rs.next()) {
                        int colCnt = rs.getMetaData().getColumnCount();
                        if (1 != colCnt) {
                            throw new JmvcException.TODO("Unexpected column count: " + colCnt);
                        }
                        rval = rs.getInt(1);
                    }
                } catch (SQLException ex) {
                    throw ex;
                }
            }
        } catch (SQLException ex) {
            throw ex;
        }
        if (1 != rowCnt) {
            throw new JmvcException.TODO("Expected 1 row");
        }
        return rval;
    }

    private Map<EnumSet<E>, InsertRow> __insertByCols = new HashMap<>();

    private <E extends Enum<E>> int __insertRow(Object... colVals) throws SQLException {
        final int n = colVals.length;
        invariant(isEven(n));
        EnumSet cols = getColsAsKey(colVals);
        if (!__insertByCols.containsKey(cols)) {
            __insertByCols.put(cols, new InsertRow(cols));
        }
        final InsertRow insert = __insertByCols.get(cols);
        return insert.execute(colVals);
    }

    private static <E extends Enum<E>> EnumSet<E> getColsAsKey(Object... colVals) {
        List<E> cols = new LinkedList();
        for (int i = 0; i < colVals.length; i += 2) {
            cols.add(castobj(colVals[i]));
        }
        EnumSet<E> key = EnumSet.copyOf(cols);
        return key;
    }

    /**
     * Bookeeping for INSERT prepared statement
     */
    private class InsertRow<E extends Enum<E>> {
        private InsertRow(EnumSet<E> cols) {
            initialize(cols);
        }

        private int execute(Object... colVals) throws SQLException {
            if ((2 * __pstmt.numPositions) != colVals.length) {
                throw new JmvcException.TODO("Invalid # of values");
            }
            Connection conn;
            PreparedStatement pstmt;
            int id = -1;
            try {
                int rid = -1;
                synchronized (this) {
                    conn = connection();
                    pstmt = (__hasID)
                            ? __pstmt.getPreparedStatement(conn, Statement.RETURN_GENERATED_KEYS, colVals)
                            : __pstmt.getPreparedStatement(conn, colVals);
                    rid = executeUpdate(pstmt);
                    __pstmt.close();
                }
                if (__hasID) id = rid;
            } catch (SQLException ex) {
                throw ex;
            }
            return id;
        }

        private void initialize(EnumSet<E> cols) {
            Integer[] positionByOrdinal = new Integer[_config.length];
            for (int i = 0; i < positionByOrdinal.length; ++i) positionByOrdinal[i] = -1;
            List<String> colNames = new LinkedList<>();
            for (Enum col : cols.stream().collect(Collectors.toList())) {
                final int ordinal = col.ordinal();
                colNames.add(col.name().toUpperCase());
                positionByOrdinal[ordinal] = ++__numPositions;
            }
            __hasID = Arrays.stream(_config).map(c -> c.name()).anyMatch(name -> name.equalsIgnoreCase("ID"));
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

    private final Function<Object, Integer> getOrdinal = new Function<Object, Integer>() {
        @Override
        public Integer apply(Object o) {
            final E col = castobj(o);
            if (!isValidColumn(col)) {
                throw new JmvcException.TODO("Invalid column: " + col.name());
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
                throw new JmvcException.TODO("Table does not have ID column");
            }
        }

        private int execute(int id, Object... xcolVals) throws SQLException {
            int rid = -1;
            PreparedStatementX pstmtx = getPstmt(xcolVals);
            //Then add ID
            Object[] colVals = append(xcolVals, getEnumOfCol("ID"), id);
            synchronized (this) {
                PreparedStatement pstmt = pstmtx.getPreparedStatement(connection(), Statement.RETURN_GENERATED_KEYS, colVals);
                rid = executeUpdate(pstmt);
                pstmtx.close();
            }
            return rid;
        }

        /**
         * Get PreparedStatement for columns in colVals.
         *
         * @param colVals pairs of col,value.
         * @return PreparedStatement.
         */
        private PreparedStatementX getPstmt(Object... colVals) throws SQLException {
            EnumSet<E> key = getColsAsKey(colVals);
            if (!_pstmtByCol.containsKey(key)) {
                Integer[] positionByOrdinal = arrayFill(new Integer[_config.length], -1);
                int currPos = 0;
                StringBuilder stmt = new StringBuilder();
                stmt
                        .append("UPDATE ")
                        .append(name)
                        .append(" SET ")
                ;
                boolean hasUpdatedVal = false;
                for (E col : key.stream().collect(Collectors.toList())) {
                    if (0 < currPos)
                        stmt.append(',');
                    final String colName = col.name().toUpperCase();
                    stmt
                            .append(colName)
                            .append("=?");
                    final int ordinal = col.ordinal();
                    positionByOrdinal[ordinal] = ++currPos;
                    hasUpdatedVal |= colName.equalsIgnoreCase("UPDATED_AT");
                }
                if (!hasUpdatedVal && hasColumn("UPDATED_AT")) {
                    if (0 < currPos) stmt.append(',');
                    stmt.append("UPDATED_AT=CURRENT_TIMESTAMP");
                }
                stmt.append(" WHERE ID=?");
                positionByOrdinal[getColInfoOrdinal("ID")] = ++currPos;
                _pstmtByCol.put(key, new PreparedStatementX(stmt.toString(), positionByOrdinal, getOrdinal, _colInfo));
            }
            return _pstmtByCol.get(key);
        }

        private Map<EnumSet<E>, PreparedStatementX> _pstmtByCol = new HashMap<>();
    }
}
