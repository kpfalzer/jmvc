package jmvc.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagesTest {

    @Test
    void format() {
        String f = Messages.format("x",1,2,3);
        f = Messages.format("UNKNOWN-1");
        boolean stop = true;
    }
}