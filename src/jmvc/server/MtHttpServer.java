package jmvc.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Simple multithreaded HTTP server.
 *
 * From: https://dzone.com/articles/simple-http-server-in-java
 * even better:
 * https://dzone.com/articles/java-11-standardized-http-client-api
 * includes websockets
 */
public class MtHttpServer {
    public MtHttpServer(String host, int port) throws IOException {
        _server = HttpServer.create(
                new InetSocketAddress(host, port), BACKLOG);
    }

    public MtHttpServer start() {
        _server.setExecutor(Executors.newFixedThreadPool(NTHREADS));
        _server.start();
        return this;
    }

    public MtHttpServer stop(int delay) {
        _server.stop(delay);
        return this;
    }

    public MtHttpServer stop() {
        return stop(0);
    }

    public MtHttpServer addRoute(String path, RequestHandler handler) {
        _server.createContext(path, handler);
        return this;
    }

    private final HttpServer _server;
    public static final Integer BACKLOG =
            Integer.parseInt(System.getProperty("server.MtHttpServer.BACKLOG", "0"));
    public static final Integer NTHREADS =
            Integer.parseInt(System.getProperty("server.MtHttpServer.NTHREADS", "16"));
}
