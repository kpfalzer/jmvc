package jmvc.model.influxdb;

import com.influxdb.query.FluxTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DbClientTest {

    private static class MyDbClient extends DbClient {
        @Override
        public String getToken() {
            return "72MyvGJzG_qZYYqGAqE7mkR-qRqLJQ33mfoJrGvq_XyJszPCuu6C55WuanucX61z9_ZTubOrB2fdqw4zgsrkdw==";
        }

        @Override
        public String getOrg() {
            return "kwp-si5-devops";
        }
    }

    @Test
    void query() {
        final DbClient db = new MyDbClient();
        {
            final String query = "from(bucket: \"jenkins\")\n" +
                    "  |> range(start: 0)\n" +
                    "  |> filter(fn: (r) => r._measurement == \"job\")\n" +
                    "  |> keep(columns: [\"result\"]) |> unique(column: \"result\") |> group()";
            List<FluxTable> result = db.query(query);
            assertFalse(result.isEmpty());
        }
    }
}