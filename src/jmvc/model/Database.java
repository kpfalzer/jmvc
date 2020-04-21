package jmvc.model;

import jmvc.Config;
import jmvc.Exception;
import jmvc.model.derby.DerbyDatabase;
import jmvc.model.sql.SqlDatabase;

import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gblibx.Util.*;

/**
 * Model base class.
 * Implementations extend, as in Sql.
 */
public abstract class Database {
    public static Database connect(Config config) {
        final String url = config.requireProperty(URL);
        final String type = __getDbaseType(url);
        Database dbase = null;
        switch (type) {
            case "derby":
                dbase = new DerbyDatabase(config);
                break;
            case "mariadb":
                dbase = new SqlDatabase(config);
            default:
                expectNever("Invalid dbase type: " + type);
        }
        expectNonNull(dbase);
        return dbase;
    }

    public Connection getConnection() {
        return _connection;
    }

    protected Database(Config config) {
        _config = config;
        try {
            _connection = DriverManager.getConnection(_config.requireProperty(URL), _config);
        } catch (SQLException ex) {
            throw new Exception(ex);
        }
    }

    public String name() {
        return _config.requireProperty(NAME);
    }

    public String getFullTableName(String tblName) {
        return (0 < tblName.indexOf('.')) ? tblName : String.format("%s.%s", name(), tblName);
    }

    public boolean hasTable(String shortTblName) throws SQLException {
        ResultSet rs = getConnection()
                .getMetaData()
                .getTables(null, null, upcase(shortTblName), null);
        return rs.next();
    }

    public static final String URL = "url";
    public static final String NAME = "name";
    public static final String USER = "user";
    public static final String PASSWORD = "password";

    public static Config getConfig(String url, String name, String user, String password) {
        Config config = new Config();
        return config.add(URL, url).add(NAME, name).add(USER, user).add(PASSWORD, password);
    }

    private static final Pattern __PATTERN = Pattern.compile("jdbc:([^:]+):.+");

    private static String __getDbaseType(String url) {
        final Matcher matcher = __PATTERN.matcher(url);
        invariant(matcher.matches());
        return matcher.group(1);
    }

    protected final Config _config;
    protected final Connection _connection;
}
