package jmvc.server;

import com.sun.net.httpserver.HttpExchange;
import gblibx.HttpConnection;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gblibx.HttpConnection.postJSON;
import static gblibx.Util.toMap;

/**
 * Test POST request:
 * curl --header "Content-Type: application/json" --request POST --data '{"name":"mr. crazy-k"}' http://localhost:3005/teachers/create
 */
class RequestHandlerTest {

    static MtHttpServer server;

    static class Handler extends RequestHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            initialize(exchange);
            boolean debug = true;
        }
    }

    static void initialize() throws IOException {
         server = new MtHttpServer("localhost", 3005);
         server.addRoute("/", Handler::new);
         server.start();
    }

    @Test
    void handle() throws IOException, InterruptedException {
        initialize();
        {
            //try programmatic POST
            try {
                int n = 2048;
                char[] buf = new char[n];
                for (int i = 0; i < n; ++i) buf[i] = 'x';
                postJSON("http://localhost:3005/foop", toMap("cmd", "create", "data", buf));
            } catch (HttpConnection.Exception e) {
                e.printStackTrace();
            }
        }
        Thread.currentThread().join();
    }
}