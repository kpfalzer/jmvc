package jmvc.model.sql;

import static org.junit.jupiter.api.Assertions.*;

class SqlModelTest {

    @org.junit.jupiter.api.Test
    void openDbase() {
        SqlModel model = new SqlModel(null);
        model.openDbase();
    }
}