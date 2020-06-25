package jmvc;

import jmvc.model.Database;
import jmvc.model.Table;
import jmvc.model.sql.SqlDatabase;
import jmvc.model.sql.SqlTable;
import jmvc.server.StaticPageHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gblibx.Util.downcast;

class AppTest {
    enum ETeacher implements Table.ColSpec {
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

    enum EStudent implements Table.ColSpec {
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

    final Config config = Database.getConfig(
            "jdbc:derby://localhost:3308/MyDbTest",
            "MyDbTest",
            "MyDbTestUser",
            "MyDbTestPasswd"
    );
    final SqlDatabase dbase = downcast(Database.connect(config));
    final Table<AppTest.ETeacher> Teachers = SqlTable.create(
            dbase,
            "teachers",
            AppTest.ETeacher.class //values()
    );
    final Table<AppTest.EStudent> Students = SqlTable.create(
            dbase,
            "students",
            AppTest.EStudent.class
    );

    class MyApp extends App {

        protected MyApp(String host, int port) throws IOException {
            super(host, port);
        }
    }

    static class TeacherController extends AppController {

        protected TeacherController(Table model) {
            super(model, true);
        }
    }


    @Test
    void start() {
        try {
            //Must create App before anything else.
            final MyApp MyApp = new MyApp("localhost", 3005);
            StaticPageHandler.addDefault();
            final TeacherController TeacherController = new TeacherController(Teachers);
            MyApp.start();
        } catch (IOException | InterruptedException e) {
            throw new Exception.TODO(e);
        }
    }
}