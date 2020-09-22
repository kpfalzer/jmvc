package jmvc;

import jmvc.server.MtHttpServer;
import jmvc.server.RequestHandler;

import java.io.IOException;

import static gblibx.Util.isNonNull;

/**
 * Base of our JMVC application.
 */
public abstract class App {
    protected App(String host, int port) throws IOException {
        if (isNonNull(_theOne)) {
            throw new JmvcException.TODO("App is singleton");
        }
        _server = new MtHttpServer(host, port);
        _theOne = this;
    }

    public static App addRoute(String path, RequestHandler handler) {
        theOne()._server.addRoute(path, handler);
        return theOne();
    }

    public static App theOne() {
        return _theOne;
    }

    public void start() throws InterruptedException {
        theOne()._server.start();
        Thread.currentThread().join();
    }

    private final MtHttpServer _server;
    private static App _theOne = null;

    private static final String _APP_ROOT = "app.root";

    /**
     * Set to root of app.
     */
    public static final String APPROOT = System.getProperty(
            _APP_ROOT,
            System.getProperty("user.dir"));
}
