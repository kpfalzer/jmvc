package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.SqlDatabase;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
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
            final Connection conn = SqlDatabase.myDbase(dbase).getConnection();
            assertTrue(conn.isValid(0));
            {
                //todo: rsmd get info about cols.
                //BUT: we need something fancier for primary keys
                //NOTE: uppercase table name!
                ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, "TEACHER");
                while (rs.next()) {
                    System.out.println(rs.getString("COLUMN_NAME") + ":" + rs.getString("KEY_SEQ"));
                }
                boolean stop = true;
            }
            boolean debug = true;
        }
        //"jdbc:mariadb://localhost:3306/foobar","foobaruser","foobarpasswd"
    }
}