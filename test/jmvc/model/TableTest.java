package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.SqlTable;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static gblibx.Util.castobj;

class TableTest {

    static enum ETeacher implements Table.ColSpec {
        ID("? INT NOT NULL GENERATED ALWAYS AS IDENTITY; PRIMARY KEY (?)"),
        LOCATION("? VARCHAR(255)"),
        CREATED_AT("? timestamp default current_timestamp");

        ETeacher(String spec) {
          __spec = spec;
        }

        final String __spec;

        @Override
        public String getSpec() {
            return __spec;
        }
    }

    @Test
    void getConfig() throws SQLException {
        final Config config = Database.getConfig(
                "jdbc:derby://localhost:3308/MyDbTest",
                "MyDbTest",
                "MyDbTestUser",
                "MyDbTestPasswd"
        );
        final Database dbase = Database.connect(config);
        final Table table = SqlTable.create(
                dbase,
                "teacher",
                ETeacher.values()
        );
        table.initialize();
        {
            String s = "INSERT INTO teacher(Location) VALUES ('here')";
            dbase.executeStatement(s);
            s = "SELECT * FROM teacher";
            Object r = dbase.executeStatement(s);
            ResultSet rs = castobj(r);
            boolean stop = true;
        }
        boolean stop = true;
    }
}