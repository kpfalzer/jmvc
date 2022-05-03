package jmvc.model.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;

import java.util.List;

public class DbClient implements ApiToken {
    public DbClient(String host, int port) {
        this.host=host;
        this.port=port;
    }

    public final String host;
    public final int port;

    public DbClient() {
        this(System.getProperty("jmvc.model.influxdb.dbHost", "localhost"),
                Integer.parseInt(System.getProperty("jmvc.model.influxdb.dbPort", "8086")));
    }

    public InfluxDBClient getClient() {
        final String url = String.format("http://%s:%d", host, port);
        return InfluxDBClientFactory.create(url, getToken().toCharArray(), getOrg());
    }

    public InfluxDBClient getClient(String bucket) {
        final String url = String.format("http://%s:%d", host, port);
        return InfluxDBClientFactory.create(url, getToken().toCharArray(), getOrg(), bucket);
    }

    public QueryApi getQueryApi() {
        return getClient().getQueryApi();
    }

    /**
     * Query database and return result.
     * (See: https://github.com/influxdata/influxdb-client-java/tree/master/client)
     * @param flux Flux query statement.
     * @return result.
     */
    public List<FluxTable> query(String flux) {
        return getQueryApi().query(flux);
    }
}
