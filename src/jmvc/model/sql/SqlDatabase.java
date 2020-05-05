package jmvc.model.sql;

import jmvc.Config;
import jmvc.Exception;
import jmvc.model.Connection;
import jmvc.model.Database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static gblibx.Util.*;

public class SqlDatabase extends Database {
    public SqlDatabase(Config config) {
        super(config, __initialize(config));
    }

    public String getSchema() {
        try {
            return getMyConnection().getSchema();
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    public boolean hasTable(String shortTblName) {
        ResultSet rs = null;
        try {
            rs = getMyConnection()
                    .getMetaData()
                    .getTables(null, null, upcase(shortTblName), null);
            return rs.next();
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }

    private static Connection __initialize(Config config) {
        try {
            final java.sql.Connection connection = DriverManager.getConnection(config.requireProperty(URL), config);
            connection.setSchema(SCHEMA);
            return new Connection(connection);
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    public java.sql.Connection getMyConnection() {
        return castobj(_connection.connection);
    }

    public static SqlDatabase myDbase(Database dbase) {
        return downcast(dbase);
    }

    @Override
    public Object executeStatement(String statement) {
        try {
            Statement stmt = getMyConnection().createStatement();
            stmt.execute(statement);
            return stmt.getResultSet();
        } catch (SQLException e) {
            throw new Exception(e);
        }
    }
}
