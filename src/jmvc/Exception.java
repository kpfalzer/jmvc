package jmvc;

public class Exception extends RuntimeException {
    public Exception(String message) {
        super(message);
    }

    public Exception(java.lang.Exception ex) {
        super(ex);
    }
}
