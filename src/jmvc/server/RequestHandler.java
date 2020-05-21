package jmvc.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        _exchange = exchange;
        _reqHeaders = _exchange.getRequestHeaders();
        _reqMethod = _exchange.getRequestMethod();
        _uri = _exchange.getRequestURI().toString();
        _accept = _reqHeaders.getFirst(ACCEPT);
        _contentType = _reqHeaders.getFirst(CONTENT_TYPE);
        _readBody();
        boolean debug = true;
        //todo add stuff
        //caller should _cleanup() after use/done
    }

    protected void _readBody() {
        _body = null;
        if (! _reqHeaders.containsKey(CONTENT_LENGTH)) return;
        int n = Integer.parseInt(_reqHeaders.getFirst(CONTENT_LENGTH));
        byte buf[] = new byte[n];
        try (InputStream ins = _exchange.getRequestBody()) {
            ins.read(buf, 0, buf.length);
            _body = new String(buf, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean debug = true;
    }

    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-type";
    public static final String CONTENT_LENGTH = "Content-length";

    protected RequestHandler _cleanup() {
        _exchange = null;
        return this;
    }

    protected HttpExchange _exchange;
    protected Headers _reqHeaders;
    protected String _uri, _reqMethod, _contentType, _accept;
    protected String _body;

    protected RequestHandler() {
        //do nothing
    }
}
