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
        __server = HttpServer.create(
                new InetSocketAddress(host, port), BACKLOG);
    }

    public MtHttpServer start() {
        __server.setExecutor(Executors.newFixedThreadPool(NTHREADS));
        __server.start();
        return this;
    }

    public MtHttpServer stop(int delay) {
        __server.stop(delay);
        return this;
    }

    public MtHttpServer stop() {
        return stop(0);
    }

    public MtHttpServer addRoute(String path, RequestHandler handler) {
        __server.createContext(path, handler);
        return this;
    }

    private final HttpServer __server;
    public static final Integer BACKLOG =
            Integer.parseInt(System.getProperty("httpserver.backlog", "0"));
    public static final Integer NTHREADS =
            Integer.parseInt(System.getProperty("httpserver.nthreads", "16"));
}
