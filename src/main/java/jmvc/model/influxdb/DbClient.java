package jmvc.model.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxTable;

import java.util.List;

import static gblibx.Util.invariant;
import static gblibx.Util.isNonNull;
import static java.util.Objects.isNull;

public class DbClient implements ApiToken, AutoCloseable {
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
        if (isNull(__client)) {
            final String url = String.format("http://%s:%d", host, port);
            __client = InfluxDBClientFactory.create(url, getToken().toCharArray(), getOrg());
        }
        invariant(isNonNull(__client));
        return __client;
    }

    public InfluxDBClient getClient(String bucket) {
        if (isNull(__client)) {
            final String url = String.format("http://%s:%d", host, port);
            __client= InfluxDBClientFactory.create(url, getToken().toCharArray(), getOrg(), bucket);
        }
        invariant(isNonNull(__client));
        return __client;
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

    private InfluxDBClient __client = null;

    @Override
    public void close() throws Exception {
        if (isNonNull(__client)) {
            __client.close();
        }
        __client = null;
    }
}
