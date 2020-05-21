package jmvc.server;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test POST request:
 * curl --header "Content-Type: application/json" --request POST --data '{"username":"xyz","password":"xyz"}' http://localhost:3005/abc
 */
class RequestHandlerTest {

    static MtHttpServer server;

    static class Handler extends RequestHandler {
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