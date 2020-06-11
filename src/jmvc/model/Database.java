package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.SqlDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gblibx.Util.expectNever;
import static gblibx.Util.expectNonNull;
import static gblibx.Util.invariant;

/**
 * Model base class.
 * Implementations extend, as in Sql.
 */
public abstract class Database {
    public static Database connect(Config config) {
        final String url = config.requireProperty(URL);
        final String type = getDbaseType(url);
        Database dbase = null;
        switch (type) {
            case "derby":   //fall through
            case "mariadb":
                dbase = new SqlDatabase(config);
                break;
            default:
                expectNever("Invalid dbase type: " + type);
        }
        expectNonNull(dbase);
        return dbase;
    }

    protected Database(Config config) {
        _config = config;
    }

    public abstract String getSchema();

    public String name() {
        return _config.requireProperty(NAME);
    }

    public String getFullTableName(String tblName) {
        return (0 < tblName.indexOf('.'))
                ? tblName
                : String.format("%s.%s", name(), tblName);
    }

    public abstract boolean hasTable(String shortTblName);

    public static final String URL = "url";
    public static final String NAME = "name";
    public static final String USER = "user";
    public static final String PASSWORD = "password";

    public static Config getConfig(String url, String name, String user, String password) {
        Config config = new Config();
        return config.add(URL, url).add(NAME, name).add(USER, user).add(PASSWORD, password);
    }

    private static final Pattern __PATTERN = Pattern.compile("jdbc:([^:]+):.+");

    private static String getDbaseType(String url) {
        final Matcher matcher = __PATTERN.matcher(url);
        invariant(matcher.matches());
        return matcher.group(1);
    }

    protected final Config _config;

    public static final String SCHEMA = System.getProperty("db.schema", "DEVELOPMENT");
}
