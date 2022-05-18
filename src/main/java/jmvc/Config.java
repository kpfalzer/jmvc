package jmvc;

import java.util.Properties;

import static gblibx.Util.expectNonNull;

public class Config extends Properties {
    public Config() {
        super();
    }

    public Config add(String key, String val) {
        super.put(key, val);
        return this;
    }

    public String requireProperty(String key) {
        return expectNonNull(super.getProperty(key));
    }
}
