package jmvc.model.derby;

import jmvc.model.Database;
import org.junit.jupiter.api.Test;

class DerbyDatabaseTest {

    @Test
    void open() {
        Database model = new DerbyDatabase(null);
        model.open();
    }
}