package jmvc;

import java.nio.file.Paths;

import static gblibx.RunCmd.runCommand;
import static gblibx.Util.invariant;
import static gblibx.Util.outOfDate;

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

    public static String getViewFileName(String path) {
        return Paths.get(App.APPROOT, path).toString();
    }

    public static void runSASS(String[] sasses, String sassCmd) {
        for (String sass : sasses) {
            String in = getViewFileName(sass);
            String out = in.replace(".sass", ".css");
            if (outOfDate(in, out)) {
                String cmd = sassCmd.replace("@in@", in).replace("@out@", out);
                int ecode = runCommand(cmd);
                invariant(0 == ecode);
            }
        }
    }
}
