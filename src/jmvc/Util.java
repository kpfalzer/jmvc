package jmvc;

public class Util {
    public static void TODO(String message) {
        throw new Exception.TODO(message);
    }

    public static void TODO(java.lang.Exception ex) {
        throw new Exception.TODO(ex);
    }

    public static void dumpAndDie(java.lang.Exception ex) {
        ex.printStackTrace(System.err);
        System.exit(1);
    }
}
