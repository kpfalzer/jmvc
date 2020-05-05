package jmvc.model;

/**
 * A proxy for specific implementation of java.sql.Connection-like.
 */
public class Connection {
    public Connection(Object conn) {
        connection = conn;
    }

    public final Object connection;
}
