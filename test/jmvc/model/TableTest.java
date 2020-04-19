package jmvc.model;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @Test
    void getConfig() {
        final Table.Config config = Table.getConfig(
                "col1", Types.BIGINT,
                "id", Table.PRIMARY_KEY,
                "dt", Types.TIMESTAMP_WITH_TIMEZONE
        );
        boolean stop =true;
    }
}