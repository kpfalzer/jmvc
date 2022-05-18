package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.SqlDatabase;
import jmvc.model.sql.SqlTable;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static gblibx.Encryptor.decrypt;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTest {

    static enum ERuns implements Table.ColSpec {
        id("? bigint(20) NOT NULL AUTO_INCREMENT; PRIMARY KEY (?)"),
        brief("? varchar(255) DEFAULT NULL"),
        detail("? text"),
        project("? varchar(255) DEFAULT NULL"),
        expected("? int(11) DEFAULT NULL"),
        completed("? int(11) DEFAULT NULL"),
        created_at("? datetime NOT NULL"),
        updated_at("? datetime NOT NULL"),
        rndseed("? bigint(20) DEFAULT '0'");

        ERuns(String spec) {
            _spec = spec;
        }

        final String _spec;

        @Override
        public String getSpec() {
            return _spec;
        }
    }

    //mysql> show create table jenkins ;
    static enum EJenkins implements Table.ColSpec {
        id("? bigint(20) NOT NULL AUTO_INCREMENT; PRIMARY KEY (?)"),
        run_id("? bigint(20) DEFAULT NULL"
                + "; KEY index_jenkins_on_run_id (?)"
                + "; CONSTRAINT fk_rails_e89e8ed338 FOREIGN KEY (?) REFERENCES runs (id)"
        ),
        url("? text"),
        building("? tinyint(1) DEFAULT '1'");

        EJenkins(String spec) {
            _spec = spec;
        }

        final String _spec;

        @Override
        public String getSpec() {
            return _spec;
        }
    }

    private static String dk(String ek) throws IOException {
        return decrypt(new File("/Users/kpfalzer/.gblibx.cryptokey"), ek);
    }

    @Test
    void connect() throws SQLException, IOException {
        //"jdbc:derby://localhost:3308/MyDbTest", "MyDbTestUser", "MyDbTestPasswd"
        if (false) {
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
        //"jdbc:mysql://localhost/test?user=minty&password=greatsqldb"
        if (true) {
            final Config config = Database.getConfig(
                    "jdbc:mysql://localhost:3306/dvwebdb",
                    "dvwebdb",
                    dk("8Fh85xoqgEbOgBJwKiytPw=="),
                    dk("dmJMFnHW1ubSJml1QNpeAw==")
            );
            //mitigate issue:
            //java.sql.SQLException: The server time zone value 'PDT' is unrecognized or represents more than one time zone. You must configure either the server or JDBC driver (via the 'serverTimezone' configuration property) to use a more specifc time zone value if you want to utilize time zone support.
            config.add("serverTimezone", "US/Pacific");
            final Database dbase = Database.connect(config);
            final Connection conn = SqlDatabase.myDbase(dbase).getConnection();
            assertTrue(conn.isValid(0));
            {
                //todo: rsmd get info about cols.
                //BUT: we need something fancier for primary keys
                //NOTE: uppercase table name!
                for (String table : new String[]{"runs", "jenkins"}) {
                    ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, table);
                    while (rs.next()) {
                        System.out.println(
                                "table="
                                        + table
                                        + ", "
                                        + rs.getString("COLUMN_NAME")
                                        + ":"
                                        + rs.getString("KEY_SEQ"));
                    }
                }
                boolean stop = true;
            }
            final Table<DatabaseTest.ERuns> Runs = SqlTable.create(
                    dbase,
                    "runs",
                    DatabaseTest.ERuns.class //values()
            );
            final Table<DatabaseTest.ERuns> Jenkins = SqlTable.create(
                    dbase,
                    "jenkins",
                    DatabaseTest.EJenkins.class //values()
            );
            boolean debug = true;
        }
    }
}