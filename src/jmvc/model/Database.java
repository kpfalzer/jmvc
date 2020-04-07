package jmvc.model;

import java.util.Properties;

/**
 * Model base class.
 * Implementations extend, as in Sql.
 */
public abstract class Database {
    protected Database(Properties props) {
        __properties = props;
    }

    public String dbName() {
        return __properties.getProperty(DB_NAME);
    }

    public abstract void open();

    public static final String DB_NAME = "db.name";
    public static final String DB_USER = "db.user";
    public static final String DB_PASSWD = "db.passwd";

    private final Properties __properties;
}
