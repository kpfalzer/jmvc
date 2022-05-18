package jmvc.model.influxdb;

public interface ApiToken {
    default String getToken() {
        return System.getProperty("jmvc.model.influxdb.dbToken");
    }
    default String getOrg() {
        return System.getProperty("jmvc.model.influxdb.dbOrg");
    }
}
