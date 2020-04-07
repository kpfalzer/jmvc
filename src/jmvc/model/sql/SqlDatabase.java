package jmvc.model.sql;

import jmvc.model.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SqlDatabase extends Database {
    public SqlDatabase(Properties properties) {
        super(properties);
    }
    @Override
    public void open() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/foobar?user=foobaruser&password=foobarpasswd");
            boolean todo = true;
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
