package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.SqlDatabase;
import jmvc.model.sql.SqlTable;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static gblibx.Util.downcast;

class TableTest {

    static enum ETeacher implements Table.ColSpec {
        ID("? INT NOT NULL GENERATED ALWAYS AS IDENTITY; PRIMARY KEY (?)"),
        LOCATION("? VARCHAR(255)"),
        UPDATED_AT("? timestamp default current_timestamp"),
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

    static enum EBad {eFoo};

    @Test
    void getConfig() throws SQLException {
        final Config config = Database.getConfig(
                "jdbc:derby://localhost:3308/MyDbTest",
                "MyDbTest",
                "MyDbTestUser",
                "MyDbTestPasswd"
        );
        final SqlDatabase dbase = downcast(Database.connect(config));
        final Table table = SqlTable.<ETeacher>create(
                dbase,
                "teacher",
                ETeacher.class //values()
        );
        table.initialize();
        {
            String s = "INSERT INTO teacher(Location) VALUES ('here')";
            dbase.executeStatementNoResult(s);
            s = "SELECT * FROM teacher";
            final ResultSet rs = dbase.executeStatement(s);
            dbase.close(rs);
        }
        int lastId = 0;
        {
            lastId = table.insertRow(ETeacher.LOCATION, "new location");
        }
        {
            table.updateTableById(lastId, ETeacher.LOCATION, "updated location");
            table.updateTableById(lastId-1, ETeacher.LOCATION, "previous location");
        }
        if (true){  //this tests for bad column
            try {
                int id = table.insertRow(EBad.eFoo, "bad value");
                id += 0;
            } catch (Exception ex) {
                System.err.println("Expected: " + ex.getMessage());
            }
        }
        boolean stop = true;
    }
}