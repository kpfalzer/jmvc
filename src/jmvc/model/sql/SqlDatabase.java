package jmvc.model.sql;

import jmvc.Config;
import jmvc.Exception;
import jmvc.model.Database;

import java.sql.SQLException;
import java.sql.Statement;

public class SqlDatabase extends Database {
    public SqlDatabase(Config config) {
        super(config);
    }

    @Override
    public Object executeStatement(String statement) {
        Object r = null;
        try {
            Statement stmt = getConnection().createStatement();
            stmt.execute(statement);
            r = stmt.getResultSet();
        } catch (SQLException e) {
            throw new Exception(e);
        }
        return r;
    }
}
