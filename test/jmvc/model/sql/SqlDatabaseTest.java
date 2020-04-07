package jmvc.model.sql;

class SqlDatabaseTest {

    @org.junit.jupiter.api.Test
    void openDbase() {
        SqlDatabase model = new SqlDatabase(null);
        model.open();
    }
}