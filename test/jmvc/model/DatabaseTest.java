package jmvc.model;

import jmvc.Config;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

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
            final Connection conn = dbase.getConnection();
            assertTrue(conn.isValid(0));
            {
                final String table = "MyDbTest.student";
                final String query = "select * from " + table;
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();
                //todo: rsmd get info about cols.
                //BUT: we need something fancier for primary keys
                //NOTE: uppercase table name!
                rs = conn.getMetaData().getPrimaryKeys(null, null, "STUDENT");
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