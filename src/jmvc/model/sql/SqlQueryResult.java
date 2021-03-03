package jmvc.model.sql;

import jmvc.JmvcException;
import jmvc.model.QueryResult;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static gblibx.Util.now;
import static gblibx.Util.secToNow;

public class SqlQueryResult extends QueryResult implements AutoCloseable {
    public SqlQueryResult(SqlDatabase dbase, String sqlQuery) {
        _dbase = dbase;
        _sqlQuery = sqlQuery;
    }

    public static SqlQueryResult executeQuery(SqlDatabase dbase, String sqlQuery) {
        final SqlQueryResult qr = new SqlQueryResult(dbase, sqlQuery);
        return qr.executeQuery();
    }

    public static SqlQueryResult executeQuery(SqlDatabase dbase,
                                              String sqlQuery,
                                              Function<Object[], Boolean> forEachRow) {
        final SqlQueryResult qr = new SqlQueryResult(dbase, sqlQuery);
        return qr.executeQuery(forEachRow);
    }

    private void debug(LocalDateTime started) {
        String where = Thread.currentThread().getStackTrace()[2].toString();
        String query = _sqlQuery.replaceAll("\\s+", " ");
        System.err.println(String.format("DEBUG: %s: %s took %.1f sec",
                where, query, secToNow(started)));
    }

    private static final boolean _DEBUG = Boolean.valueOf(
            System.getProperty("jmvc.SqlQueryResult.DEBUG", "false"));

    private SqlQueryResult executeQuery(Function<Object[], Boolean> forEachRow) {
        final LocalDateTime dt1 = now();
        _conn = _dbase.getConnection();
        try {
            _stmt = _conn.createStatement();
            _rs = _stmt.executeQuery(_sqlQuery);
            updateColInfo();
            while (_rs.next()) {
                Object[] row = new Object[_colInfos.length];
                for (int i = 1; i <= row.length; ++i) {
                    row[i - 1] = _rs.getObject(i);
                }
                if (forEachRow.apply(row)) break;
            }
        } catch (SQLException ex) {
            _exception = ex;
        } finally {
            close();
        }
        if (hasException()) {
            //raw result is nullified
            _rows = null;
            throw new JmvcException.TODO(exception());
        }
        if (_DEBUG) debug(dt1);
        return this;
    }

    public SqlQueryResult executeQuery() {
        List<Object[]> rows = new LinkedList<>();
        executeQuery((row) -> {
            rows.add(row);
            return false;
        });
        _rows = rows.toArray(new Object[rows.size()][]);
        return this;
    }

    private final SqlDatabase _dbase;
    private final String _sqlQuery;
    private ResultSet _rs;
    private Connection _conn;
    private Statement _stmt;

    @Override
    public void close() {
        try {
            _stmt.close();
            _conn.close();
        } catch (SQLException ex) {
            ;//do nothing
        }
    }

    private SqlQueryResult updateColInfo() throws SQLException {
        final ResultSetMetaData rsmd = _rs.getMetaData();
        final int ncols = rsmd.getColumnCount();
        _colInfos = new ColInfo[ncols];
        for (int i = 1; i <= _colInfos.length; ++i) {
            _colInfos[i - 1] = new ColInfo(
                    rsmd.getTableName(i),
                    rsmd.getColumnName(i),
                    rsmd.getColumnType(i));
        }
        return this;
    }

    private SqlQueryResult collectRows() throws SQLException {
        List<Object[]> rows = new LinkedList<>();
        while (_rs.next()) {
            Object[] row = new Object[_colInfos.length];
            for (int i = 1; i <= row.length; ++i) {
                row[i - 1] = _rs.getObject(i);
            }
            rows.add(row);
        }
        _rows = rows.toArray(new Object[rows.size()][]);
        return this;
    }

}
