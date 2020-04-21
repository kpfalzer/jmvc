package jmvc.model;

import java.sql.SQLException;
import java.util.LinkedList;

public class Table {
    public static Table create(String name, String... eles) {
        return new Table(name, getConfig(eles));
    }

    private Table(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    /**
     * Create configuration.
     *
     * @param eles series of clauses to CREATE TABLE
     * @return type... by name configuration
     */
    public static Config getConfig(String... eles) {
        return new Config(eles);
    }

    public static class Config extends LinkedList<String> {
        private Config(String[] eles) {
            for (String s : eles) super.add(s);
        }
    }

    /*package*/ void initialize(Database dbase) throws SQLException {
        boolean hasTable = dbase.hasTable(name);
    }

    private final String name;
    private final Config config;
}
