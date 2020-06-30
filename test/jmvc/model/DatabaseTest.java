package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.SqlDatabase;
import jmvc.model.sql.SqlTable;
import org.junit.jupiter.api.Test;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    static enum EJenkins implements Table.ColSpec {
        id("? bigint(20) NOT NULL AUTO_INCREMENT; PRIMARY KEY (?)"),
        run_id("? bigint(20) DEFAULT NULL"),
        url("? text"),
        building("? tinyint(1) DEFAULT '1'")
        //KEY `index_jenkins_on_run_id` (`run_id`),
        //CONSTRAINT `fk_rails_e89e8ed338` FOREIGN KEY (`run_id`) REFERENCES `runs` (`id`);
        ;

        EJenkins(String spec) {
            _spec = spec;
        }

        final String _spec;

        @Override
        public String getSpec() {
            return _spec;
        }
    }

    @Test
    void connect() throws SQLException {
        try {
            final String KEY = "GT%^YHJU&*IK!@#$";
            byte[] keyBytes= new String("qwerty12345").getBytes();
            SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] iv = new byte[cipher.getBlockSize()];
            randomSecureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            final String _input = "the quick brown fox jumps over the lazy dog";
            final byte[] input = _input.getBytes();
            //encrypt
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] encrypted= new byte[cipher.getOutputSize(input.length)];
            int enc_len = cipher.update(input, 0, input.length, encrypted, 0);
            enc_len += cipher.doFinal(encrypted, enc_len);
            //
            //descrypt
            boolean stop = true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (ShortBufferException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

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
                    "dvwebdbuser",
                    "dvwebdbpwd"
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
            boolean debug = true;
        }
    }
}