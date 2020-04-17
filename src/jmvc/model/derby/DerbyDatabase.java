package jmvc.model.derby;

import jmvc.model.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DerbyDatabase extends Database {
    public DerbyDatabase(Properties properties) {
        super(properties);
    }

    @Override
    public void open() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:derby://localhost:3308/MyDbTest", "MyDbTestUser", "MyDbTestPasswd");
            boolean todo = true;
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
