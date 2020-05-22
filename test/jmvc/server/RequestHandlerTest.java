package jmvc.server;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Test POST request:
 * curl --header "Content-Type: application/json" --request POST --data '{"username":"xyz","password":"xyz","ar1":[1,2,3]}' http://localhost:3005/abc
 */
class RequestHandlerTest {

    static MtHttpServer server;

    static class Handler extends RequestHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            _initialize(exchange);
            boolean debug = true;
        }
    }

    static void initialize() throws IOException {
         server = new MtHttpServer("localhost", 3005);
         server.addRoute("/", new Handler(){
         });
         server.start();
    }

    @Test
    void handle() throws IOException, InterruptedException {
        initialize();
        Thread.currentThread().join();
    }
}