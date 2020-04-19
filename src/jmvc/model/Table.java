package jmvc.model;

import java.util.HashMap;

import static gblibx.Util.*;

public class Table {
    /**
     * Create configuration.
     * @param eles series of [name, java.sql.Types|PRIMARY_KEY]
     * @return type... by name configuration
     */
    public static Config getConfig( Object... eles){
        Config config = new Config();
        invariant(isEven(eles.length));
        for (int i = 0; i < eles.length; i += 2) {
            final String fieldName = invariantThen(eles[i], x -> x instanceof String, x -> downcast(x));
            final Integer fieldType = invariantThen(eles[i+1], x -> x instanceof Integer, x -> downcast(x));
            invariant(!config.containsKey(fieldName));
            config.put(fieldName, fieldType);
        }
        return config;
    }

    public static final int PRIMARY_KEY = 99999;

    public static class Config extends HashMap<String, Integer> {
    }
}
