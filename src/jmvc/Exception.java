package jmvc;

public class Exception extends RuntimeException {
    public Exception(String message) {
        super(message);
    }

    public Exception(java.lang.Exception ex) {
        super(ex);
    }

    public static class TODO extends Exception {
        public TODO(String message) {
            super(message);
        }

        public TODO(java.lang.Exception ex) {
            super(ex);
        }

    }
}