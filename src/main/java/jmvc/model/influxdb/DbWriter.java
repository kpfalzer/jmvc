package jmvc.model.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;

import static gblibx.Util.isNonNull;

public class DbWriter implements AutoCloseable {
    public DbWriter(DbClient client, String bucket) {
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

    public String getOrg() { return __client.getOrg();}

    public InfluxDBClient getClient() {
        InfluxDBClient client= __client.getClient(getBucket());
        return client;
    }

    private final String __bucket;
    private final DbClient __client;

    @Override
    public void close() throws Exception {
        if (isNonNull(getClient())) {
            getClient().close();
        }
    }
}
