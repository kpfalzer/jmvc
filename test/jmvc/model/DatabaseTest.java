package jmvc.model;

import jmvc.Config;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {

    @Test
    void connect() throws SQLException {
        //"jdbc:derby://localhost:3308/MyDbTest", "MyDbTestUser", "MyDbTestPasswd"
        {
            final Config config = Database.getConfig(
                    "jdbc:derby://localhost:3308/MyDbTest",
                    "MyDbTest",
                    "MyDbTestUser",
                    "MyDbTestPasswd"
            );
            final Database dbase = Database.connect(config);
            assertTrue(dbase.getConnection().isValid(0));
            boolean debug = true;
        }
        //"jdbc:mariadb://localhost:3306/foobar","foobaruser","foobarpasswd"
    }
}