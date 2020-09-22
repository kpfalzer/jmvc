package jmvc;

public class Util {
    public static void TODO(String message) {
        throw new JmvcException.TODO(message);
    }

    public static void TODO(Exception ex) {
        throw new JmvcException.TODO(ex);
    }

    public static void dumpAndDie(Exception ex) {
        ex.printStackTrace(System.err);
        System.exit(1);
    }
}
