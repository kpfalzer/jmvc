package jmvc;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public static String getYYYYMMDD(LocalDateTime ldt) {
        return ldt.format(DT_YYYY_MM_DD);
    }

    public static String getDAYMMDDYYYY(LocalDateTime ldt) {
        return ldt.format(DT_DAY_MM_DD_YYYY);
    }

    public static String getDAYMONDDYYYY(LocalDateTime ldt) {
        return ldt.format(DT_DAY_MMM_DD_YYYY);
    }

    public static LocalDateTime getStartOfDay(int nDaysAgo) {
        nDaysAgo = (0 > nDaysAgo) ? 0 : nDaysAgo;
        LocalDateTime ldt = LocalDateTime.now();
        ldt = ldt.minusDays(nDaysAgo);
        ldt = LocalDateTime.of(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), 0, 0);
        return ldt;
    }

    public static int getPcnt(int a, int b) {
        return (b == 0) ? 0 : ((100 * a) / b);
    }

    public static String nowTimestamp() {
        return LocalDateTime.now().format(DT_FORMAT);
    }

    public static final DateTimeFormatter DT_DAY_MM_DD_YYYY = DateTimeFormatter.ofPattern("EEE-MM-dd-yyyy");
    public static final DateTimeFormatter DT_DAY_MMM_DD_YYYY = DateTimeFormatter.ofPattern("EEE-MMM-dd-yyyy");
    public static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DT_FULL = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
    public static final DateTimeFormatter DT_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

}
