package jmvc.model.sql;

class SqlDatabaseTest {

    @org.junit.jupiter.api.Test
    void open() {
        SqlDatabase model = new SqlDatabase(null);
        model.open();
    }
}