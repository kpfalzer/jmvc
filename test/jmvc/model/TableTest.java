package jmvc.model;

import jmvc.Config;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

class TableTest {

    @Test
    void getConfig() throws SQLException {
        final Config config = Database.getConfig(
                "jdbc:derby://localhost:3308/MyDbTest",
                "MyDbTest",
                "MyDbTestUser",
                "MyDbTestPasswd"
        );
        final Database dbase = Database.connect(config);
        final Table table = Table.create("student");
        final boolean hasTbl = dbase.hasTable("student");
        boolean stop = true;
    }
}