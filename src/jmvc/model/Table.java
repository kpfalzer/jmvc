package jmvc.model;

import java.util.HashMap;

import static gblibx.Util.*;
import static java.util.Objects.isNull;

public class Table {
    public static Table create(String name, Object... eles) {
        return new Table(name, getConfig(eles));
    }

    private Table(String name, Config config) {
        __name = name;
        __config = config;
    }

    /**
     * Create configuration.
     *
     * @param eles series of triplet: name, java.sql.Types|PRIMARY_KEY, constraint|null
     * @return type... by name configuration
     */
    public static Config getConfig(Object... eles) {
        Config config = new Config();
        invariant((0 < eles.length) && (0 == (eles.length % 3)));
        for (int i = 0; i < eles.length; i += 3) {
            final String fieldName = invariantThen(eles[i], x -> x instanceof String, x -> downcast(x));
            final Integer fieldType = invariantThen(eles[i + 1], x -> x instanceof Integer, x -> downcast(x));
            final String constraint = invariantThen(
                    eles[i + 2],
                    x -> (x instanceof String) || isNull(x),
                    x -> applyIfNotNull(x, y -> downcast(y)));
            config.addCol(fieldName, fieldType, constraint);
        }
        return config;
    }

    public static final int PRIMARY_KEY = 99999;

    public static class ColType extends Pair<Integer, String> {
        private ColType(Integer type, String constraint) {
            super(type, constraint);
        }
    }

    public static class Config extends HashMap<String, ColType> {
        private void addCol(String name, Integer type, String constraint) {
            invariant(!super.containsKey(name));
            super.put(name, new ColType(type, constraint));
        }
    }

    /*package*/ void initialize(Database dbase) {

    }

    private final String __name;
    private final Config __config;
}
