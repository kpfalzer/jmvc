package jmvc.model.sql;

import gblibx.Util;
import jmvc.JmvcException;
import jmvc.model.*;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gblibx.Util.*;
import static java.util.Objects.isNull;

public class SqlTable<E extends Enum<E>> extends Table {

    public static <E extends Enum<E>> SqlTable
    create(Database dbase, String name, Class<E> cols, String... colIndex) {
        SqlTable tbl = new SqlTable(name, cols, dbase);
        tbl.createIndex(colIndex);
        return tbl;
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
        if (isNull(_insertRow)) {
            _insertRow = new InsertRow<E>();
        }
        try {
            return _insertRow.execute(colVals);
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

    /**
     * Bookeeping for INSERT prepared statement
     */
    private class InsertRow<E extends Enum<E>> {
        private InsertRow() {
            initialize();
        }

        private int execute(Object... colVals) throws SQLException {
            if ((2 * _pstmt.numPositions) != colVals.length) {
                throw new JmvcException.TODO("Invalid # of values");
            }
            Connection conn;
            PreparedStatement pstmt;
            int id = -1;
            try {
                int rid = -1;
                synchronized (this) {
                    conn = connection();
                    pstmt = (_hasID)
                            ? _pstmt.getPreparedStatement(conn, Statement.RETURN_GENERATED_KEYS, colVals)
                            : _pstmt.getPreparedStatement(conn, colVals);
                    rid = executeUpdate(pstmt);
                    _pstmt.close();
                }
                if (_hasID) id = rid;
            } catch (SQLException ex) {
                throw ex;
            }
            return id;
        }

        private void initialize() {
            Integer[] positionByOrdinal = new Integer[_config.length];
            List<String> colNames = new LinkedList<>();
            for (Enum col : _config) {
                final int ordinal = col.ordinal();
                //TODO: we cannot use PreparedStmt with (optional/defaulted) cols, since
                //sometimes may have value and other times not.
                //SO: we will require all cols (except ID) to be specified.
                //if (!_colInfo[ordinal].hasDefaultVal()) {
                if (!col.name().equalsIgnoreCase("ID")) {
                    colNames.add(col.name().toUpperCase());
                    positionByOrdinal[ordinal] = ++_numPositions;
                } else {
                    positionByOrdinal[ordinal] = -1;
                    _hasID |= true;//col.name().equalsIgnoreCase("ID");
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
            _pstmt = new PreparedStatementX(stmt.toString(), positionByOrdinal, getOrdinal, _colInfo);
        }

        private PreparedStatementX _pstmt;
        private int _numPositions = 0;
        private boolean _hasID = false;
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
            List<E> cols = new LinkedList();
            for (int i = 0; i < colVals.length; i += 2) {
                cols.add(castobj(colVals[i]));
            }
            EnumSet<E> key = EnumSet.copyOf(cols);
            if (!_pstmtByCol.containsKey(key)) {
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
                _pstmtByCol.put(key, new PreparedStatementX(stmt.toString(), positionByOrdinal, getOrdinal, _colInfo));
            }
            return _pstmtByCol.get(key);
        }

        private Map<EnumSet<E>, PreparedStatementX> _pstmtByCol = new HashMap<>();
    }
}
