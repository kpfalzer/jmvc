package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.QueryResult;
import jmvc.model.sql.SqlDatabase;
import jmvc.model.sql.SqlTable;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static gblibx.Util.castobj;
import static gblibx.Util.downcast;

class TableTest {

    static enum ETeacher implements Table.ColSpec {
        ID("? INT NOT NULL GENERATED ALWAYS AS IDENTITY; PRIMARY KEY (?)"),
        NAME("? VARCHAR(255)"),
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

    static enum EStudent implements Table.ColSpec {
        ID("? INT NOT NULL GENERATED ALWAYS AS IDENTITY; PRIMARY KEY (?)"),
        NAME("? VARCHAR(255) NOT NULL"),
        TEACHER_ID("? INT NOT NULL"),
        GRADE("? VARCHAR(1) default '_'"),
        UPDATED_AT("? timestamp default current_timestamp"),
        CREATED_AT("? timestamp default current_timestamp");

        EStudent(String spec) {
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
        final SqlDatabase dbase = downcast(Database.connect(config));
        final Table<ETeacher> Teachers = SqlTable.create(
                dbase,
                "teachers",
                ETeacher.class //values()
        );
        final Table<EStudent> Students = SqlTable.create(
                dbase,
                "students",
                EStudent.class
        );
        if (false) {
            int id = Teachers.insertRow(ETeacher.NAME, "teacherVal1");
            Students.insertRow(
                    EStudent.NAME, "studentVal1",
                    EStudent.TEACHER_ID, id
                    );
        } else {
            final String query =
                    "SELECT * FROM STUDENTS" +
                            " INNER JOIN TEACHERS" +
                            " ON TEACHER_ID = TEACHERS.ID"
                    ;
            QueryResult result = castobj(dbase.query(query));
            int nrows = result.nrows();
            boolean stop = true;
        }
        /*
        {
            table.updateTableById(lastId, ETeacher.LOCATION, "updated location");
            table.updateTableById(lastId-1, ETeacher.LOCATION, "previous location");
        }
        {
            QueryResult result = castobj(dbase.query("SELECT * FROM TEACHER"));
            int nrows = result.nrows();
            assertEquals(nrows, lastId);
        }
        */
        boolean stop = true;
    }
}