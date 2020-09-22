package jmvc.logging;

import gblibx.Logger;
import jmvc.server.RequestHandler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static jmvc.Util.TODO;

/**
 * Logger for requests.
 */
public class Requests {
    public static void logRequest(RequestHandler request) {
        if (LOG_JSON || !request.getRequestURI().toLowerCase().endsWith(".json")) {
            _theOne._logRequest(request);
        }
    }

    private synchronized void _logRequest(RequestHandler request) {
        String fullMsg = Messages.formatnc(
                Logger.ELevel.eInfo,
                "REQ-1",
                request.getID(),
                request.getRemoteAddress(),
                request.getRequestMethod(),
                request.getRequestURI(),
                request.getAccept());
        try {
            PrintStream os = new PrintStream(new FileOutputStream(_fileName, true));
            os.println(fullMsg);
            os.close();
        } catch (FileNotFoundException e) {
            TODO(e);
        }
    }

    private Requests(String fileName) {
        _fileName = fileName;
    }

    public static final String LOG =
            System.getProperty("logging.Requests.LOG", "/tmp/jmvc.request.log");
    public static final boolean LOG_JSON = false;

    private final String _fileName;
    private static final Requests _theOne = new Requests(LOG);
}
