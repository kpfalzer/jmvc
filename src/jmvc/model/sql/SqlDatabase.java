package jmvc.model.sql;

import jmvc.Config;
import jmvc.Exception;
import jmvc.model.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static gblibx.Util.downcast;
import static gblibx.Util.isNonNull;
import static gblibx.Util.upcase;

public class SqlDatabase extends Database {
    public SqlDatabase(Config config) {
        super(config);
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
            xclose(conn);
        }
        if (isNonNull(exception)) {
            throw new Exception.TODO(exception);
        }
        return schema;
    }

    @Override
    public Object query(String statement) {
        return QueryResult.executeQuery(this, statement);
    }

    public void close(Connection conn) {
        sclose(conn);
    }

    public static void sclose(Connection conn) {
        SqlDatabase.xclose(conn);
    }

    public void close(Statement stmt) {
        sclose(stmt);
    }

    public static void sclose(Statement stmt) {
        Connection conn = null;
        if (isNonNull(stmt))
            try {
                conn = stmt.getConnection();
            } catch (SQLException e) {
                ;//ignore
            }
        xclose(stmt, conn);
    }

    public void close(ResultSet rs) {
        sclose(rs);
    }

    public static void sclose(ResultSet rs) {
        Connection conn = null;
        Statement stmt = null;
        if (isNonNull(rs))
            try {
                stmt = rs.getStatement();
            } catch (SQLException e) {
                ;//ignore
            }
        if (isNonNull(stmt))
            try {
                conn = stmt.getConnection();
            } catch (SQLException e) {
                ;//ignore
            }
        sclose(conn, stmt, rs);
    }

    public void close(Connection conn, ResultSet rs) {
        sclose(conn, rs);
    }

    public static void sclose(Connection conn, ResultSet rs) {
        Statement stmt = null;
        if (isNonNull(rs))
            try {
                stmt = rs.getStatement();
            } catch (SQLException e) {
                ;//ignore
            }
        sclose(conn, stmt, rs);
    }

    public void close(Connection conn, Statement stmt, ResultSet rs) {
        sclose(conn, stmt, rs);
    }

    public static void sclose(Connection conn, Statement stmt, ResultSet rs) {
        xclose(rs, stmt, conn);
    }

    /**
     * Close items.
     * Order is important: i.e., close ResultSet, then Statement, then Connection.
     *
     * @param closeables AutoCloseable items.
     */
    private static void xclose(AutoCloseable... closeables) {
        for (AutoCloseable ele : closeables) {
            if (isNonNull(ele)) {
                try {
                    ele.close();
                } catch (java.lang.Exception e) {
                    ;//ignore
                }
            }
        }
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
            xclose(conn, rs);
        }
        if (isNonNull(exception)) {
            throw new Exception.TODO(exception);
        }
        return result;
    }

    public Connection getConnection() {
        final Config config = super._config;
        Connection connection = null;
        SQLException exception = null;
        try {
            connection = DriverManager.getConnection(config.requireProperty(URL), config);
            connection.setSchema(SCHEMA);
        } catch (SQLException ex) {
            xclose(connection);
            throw new Exception.TODO(ex);
        }
        return connection;
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
            if (hasResult)
                rset = stmt.getResultSet();
        } catch (SQLException e) {
            exception = e;
        } finally {
            xclose(stmt, conn);
        }
        if (isNonNull(exception)) {
            throw new Exception.TODO(exception);
        }
        return rset;
    }
}
