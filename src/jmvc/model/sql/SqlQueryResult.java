package jmvc.model.sql;

import jmvc.Exception;
import jmvc.model.QueryResult;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class SqlQueryResult extends QueryResult implements AutoCloseable {
    public SqlQueryResult(SqlDatabase dbase, String sqlQuery) {
        _dbase = dbase;
        _sqlQuery = sqlQuery;
    }

    public static SqlQueryResult executeQuery(SqlDatabase dbase, String sqlQuery) {
        final SqlQueryResult qr = new SqlQueryResult(dbase, sqlQuery);
        return qr.executeQuery();
    }

    public SqlQueryResult executeQuery() {
        _conn = _dbase.getConnection();
        try {
            _stmt = _conn.createStatement();
            _rs = _stmt.executeQuery(_sqlQuery);
            updateColInfo().collectRows();
        } catch (SQLException ex) {
            _exception = ex;
        } finally {
            close();
        }
        if (hasException()) {
            //raw result is nullified
            _rows = null;
            throw new Exception.TODO(exception());
        }
        return this;
    }

    private final SqlDatabase _dbase;
    private final String _sqlQuery;
    private ResultSet _rs;
    private Connection _conn;
    private Statement _stmt;

    @Override
    public void close() {
        _dbase.sclose(_conn, _stmt, _rs);
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
