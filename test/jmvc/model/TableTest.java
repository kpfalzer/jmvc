package jmvc.model;

import org.junit.jupiter.api.Test;

import java.sql.Types;

class TableTest {

    @Test
    void getConfig() {
        final Table.Config config = Table.getConfig(
                "col1", Types.BIGINT, null,
                "id", Table.PRIMARY_KEY, null,
                "dt", Types.TIMESTAMP_WITH_TIMEZONE, "some constraint here"
        );
        boolean stop = true;
    }
}