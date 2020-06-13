package jmvc.model.sql;

import jmvc.Exception;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static gblibx.Util.isNonNull;
import static java.util.Objects.isNull;

public class QueryResult implements AutoCloseable {
    public QueryResult(SqlDatabase dbase, String sqlQuery) {
        _dbase = dbase;
        _sqlQuery = sqlQuery;
    }

    public static QueryResult executeQuery(SqlDatabase dbase, String sqlQuery) {
        final QueryResult qr = new QueryResult(dbase, sqlQuery);
        return qr.executeQuery();
    }

    public QueryResult executeQuery() {
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

    public SQLException exception() {
        return _exception;
    }

    public boolean hasException() {
        return isNonNull(exception());
    }

    public boolean isValid() {
        return !(hasException() || isNull(_rows));
    }

    public int nrows() {
        return (isValid()) ? _rows.length : 0;
    }

    private final SqlDatabase _dbase;
    private final String _sqlQuery;
    private ResultSet _rs;
    private Connection _conn;
    private Statement _stmt;
    private SQLException _exception;
    private ColInfo[] _colInfos;
    private Object[][] _rows;

    @Override
    public void close() {
        _dbase.sclose(_conn, _stmt, _rs);
    }

    private QueryResult updateColInfo() throws SQLException {
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

    private QueryResult collectRows() throws SQLException {
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

    public static class ColInfo {
        public ColInfo(String tableName, String colName, int colType) {
            this.colName = colName;
            this.colType = colType;
            this.tableName = tableName;
        }

        public final String colName, tableName;
        public final int colType;
    }
}
