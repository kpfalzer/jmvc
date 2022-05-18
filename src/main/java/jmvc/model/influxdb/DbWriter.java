package jmvc.model.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;

import static gblibx.Util.isNonNull;

public class DbWriter implements ApiToken, AutoCloseable {
    public DbWriter(InfluxDBClient client, String bucket) {
        __bucket = bucket;
        __client = client;
    }

    public DbWriter writeLine(String measurement, String xline, WritePrecision precision) {
        final String line = String.format("%s,%s", measurement, xline);
        getClient().getWriteApiBlocking().writeRecord(getBucket(), getOrg(), precision, line);
        return this;
    }

    public String getBucket() {
        return __bucket;
    }

    public InfluxDBClient getClient() {
        return __client;
    }

    private final String __bucket;
    private final InfluxDBClient __client;

    @Override
    public void close() throws Exception {
        if (isNonNull(getClient())) {
            getClient().close();
        }
    }
}
