package jmvc.model.sql;

import jmvc.Config;
import jmvc.JmvcException;
import jmvc.model.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static gblibx.Util.*;

public class SqlDatabase extends Database {
    public SqlDatabase(Config config) {
        super(config);
    }

    public Connection getConnection() {
        return toConnection(_getConnection());
    }

    public String getSchema() {
        SQLException exception = null;
        String schema = null;
        final Connection conn = getConnection();
        try {
            schema = conn.getSchema();
        } catch (SQLException e) {
            exception = e;
        } finally {
            close();
        }
        if (isNonNull(exception)) {
            throw new JmvcException.TODO(exception);
        }
        return schema;
    }

    @Override
    protected void _closeActualConnection(Object connection) {
        try {
            toConnection(connection).close();
        } catch (Exception ex) {
            ;//ignore
        }
    }

    @Override
    protected Object _getNewConnection() {
        final Config config = super._config;
        Connection connection = null;
        SQLException exception = null;
        try {
            connection = DriverManager.getConnection(config.requireProperty(URL), config);
            connection.setSchema(SCHEMA);
        } catch (SQLException ex) {
            close();
            throw new JmvcException.TODO(ex);
        }
        if (false) logMessage("DEBUG:connection: " + connection.toString());
        return connection;
    }

    @Override
    public boolean isConnectionValid(Object connection) {
        boolean isValid = false;
        try {
            String catalog = toConnection(connection).getCatalog();
            isValid = true;
        } catch (Exception ex) {
            ;//ignore
        }
        return isValid;
    }

    private static Connection toConnection(Object connection) {
        return castobj(connection);
    }

    @Override
    public Object query(String statement) {
        return SqlQueryResult.executeQuery(this, statement);
    }

    public void close() {
        closeConnection();
    }

    public boolean hasTable(String shortTblName) {
        SQLException exception = null;
        ResultSet rs = null;
        boolean result = false;
        final Connection conn = getConnection();
        try {
            rs = conn
                    .getMetaData()
                    .getTables(null, null, upcase(shortTblName), null);
            result = rs.next();
        } catch (SQLException e) {
            exception = e;
        } finally {
            close();
        }
        if (isNonNull(exception)) {
            throw new JmvcException.TODO(exception);
        }
        return result;
    }

    public static SqlDatabase myDbase(Database dbase) {
        return downcast(dbase);
    }

    public void executeStatementNoResult(String statement) {
        executeStatement(statement, false);
    }

    public ResultSet executeStatement(String statement) {
        return executeStatement(statement, true);
    }

    private ResultSet executeStatement(String statement, boolean hasResult) {
        final Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rset = null;
        SQLException exception = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(statement);
            if (hasResult) {
                rset = stmt.getResultSet();
            } else {
                close();
            }
        } catch (SQLException e) {
            exception = e;
        }
        if (isNonNull(exception)) {
            close();
            throw new JmvcException.TODO(exception);
        }
        return rset;
    }
}
