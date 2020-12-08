package jmvc.model;

import jmvc.Config;
import jmvc.model.sql.SqlDatabase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gblibx.Util.*;
import static java.util.Objects.isNull;

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
            case "mariadb": //fall through
            case "mysql":
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

    /**
     * Get a connection object for current thread id.
     *
     * @return valid connection (could be null).
     */
    protected Object _getConnection() {
        Object connection = null;
        final long threadId = getCurrentThreadId();
        if (_connectionByThreadId.containsKey(threadId)) {
            connection = _connectionByThreadId.get(threadId);
            if (!isConnectionValid(connection)) {
                connection = null;
                _connectionByThreadId.remove(threadId);
            }
        }
        if (isNull(connection)) {
            connection = _getNewConnection();
            if (isNonNull(connection)) {
                _connectionByThreadId.put(threadId, connection);
            }
        }
        return connection;
    }

    public void closeConnection() {
        final long threadId = getCurrentThreadId();
        Object connection = _connectionByThreadId.get(threadId);
        if (isNonNull(connection)) {
            _closeActualConnection(connection);
        }
        _connectionByThreadId.remove(threadId);
    }

    /**
     * (Implementation) closes actual connection.
     * @param connection connection object.
     */
    protected abstract void _closeActualConnection(Object connection);

    /**
     * Create new connection to Database.
     * @return Database connection or null.
     */
    protected abstract Object _getNewConnection();

    /**
     * (Implementation) validates if connection still valid/(re)usable.
     * @param connection connection to validate.
     * @return true if (still) usable.
     */
    public abstract boolean isConnectionValid(Object connection);

    //Thread safe map
    private static Map<Long, Object> _connectionByThreadId = new ConcurrentHashMap<>();

    /**
     * Query database.
     *
     * @param statement Query command.
     * @return query result.
     */
    public abstract Object query(String statement);

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

    private static final Pattern _PATTERN = Pattern.compile("jdbc:([^:]+):.+");

    private static String getDbaseType(String url) {
        final Matcher matcher = _PATTERN.matcher(url);
        invariant(matcher.matches());
        return matcher.group(1);
    }

    protected final Config _config;

    public static final String SCHEMA = System.getProperty("db.schema", "DEVELOPMENT");
}
