package jmvc.server;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * From: https://dzone.com/articles/simple-http-server-in-java
 * even better:
 * https://dzone.com/articles/java-11-standardized-http-client-api
 * includes websockets
 */
public class HttpServer {
    public HttpServer(String host, int port) throws IOException {
        _server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress(host, port), BACKLOG);
    }

    private final com.sun.net.httpserver.HttpServer _server;
    public static final Integer BACKLOG =
            Integer.parseInt(System.getProperty("httpserver.backlog", "0"));
}
