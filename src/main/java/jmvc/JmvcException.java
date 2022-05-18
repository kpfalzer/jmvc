package jmvc;

import static gblibx.Util.getLocalDateTime;

public class JmvcException extends RuntimeException {
    public JmvcException(String message) {
        super(message);
        printStackTrace(this);
    }

    public JmvcException(Exception ex) {
        super(ex);
        printStackTrace(this);
    }

    public static void printStackTrace(Exception ex) {
        System.err.println(getLocalDateTime());
        ex.printStackTrace(System.err);
    }

    public static class TODO extends JmvcException {
        public TODO(String message) {
            super(message);
        }

        public TODO(Exception ex) {
            super(ex);
        }

    }
}