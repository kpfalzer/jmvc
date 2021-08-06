package jmvc.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jmvc.JmvcException;
import jmvc.Util;
import jmvc.logging.Requests;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static gblibx.Util.castobj;
import static gblibx.Util.getURLParams;
import static gblibx.Util.isNonNull;
import static gblibx.Util.toArray;
import static gblibx.Util.toMap;
import static java.net.HttpURLConnection.HTTP_OK;

public abstract class RequestHandler implements HttpHandler {
    public static final String TEXT_HTML = "text/html";
    public static final String TEXT_CSS = "text/css";
    public static final String TEXT_JS = "text/javascript";
    public static final String RAW_TYPE = "*/*";
    public static final String APPL_JSON = "application/json";

    public RequestHandler sendResponse(byte[] response, String type) throws IOException {
        return sendResponse(HTTP_OK, response, type);
    }

    public RequestHandler sendResponse(String response, String type) throws IOException {
        return sendResponse(HTTP_OK, response, type);
    }

    public RequestHandler sendJsonResponse(String response) throws IOException {
        return sendResponse(HTTP_OK, response, APPL_JSON);
    }

    public RequestHandler sendHtmlResponse(String response) throws IOException {
        return sendResponse(HTTP_OK, response, TEXT_HTML);
    }

    public RequestHandler sendBadResponse(int rcode) throws IOException {
        return sendResponse(rcode, "", TEXT_HTML);
    }

    public RequestHandler sendResponse(int rcode, String response, String type) throws IOException {
        return sendResponse(rcode, response.getBytes(), type);
    }

    public RequestHandler sendResponse(int rcode, byte[] response, String type) throws IOException {
        final int length = response.length;
        final Headers headers = _exchange.getResponseHeaders();
        headers.add("Content-type", type);
        headers.add("Content-length", Integer.toString(length));
        headers.add("Expires", "0");  //do not cache
        _exchange.sendResponseHeaders(rcode, length);
        _exchange.getResponseBody().write(response);
        _exchange.close();
        return this;
    }

    public boolean isPOST() {
        return getRequestMethod().equalsIgnoreCase("POST");
    }

    public boolean isGET() {
        return getRequestMethod().equalsIgnoreCase("GET");
    }

    protected void initialize(HttpExchange exchange) {
        synchronized (this) {
            _rhid = ++_RHID;
        }
        ;
        _exchange = exchange;
        _reqHeaders = _exchange.getRequestHeaders();
        _reqMethod = _exchange.getRequestMethod();
        _accept = _reqHeaders.getFirst(ACCEPT);
        _contentType = _reqHeaders.getFirst(CONTENT_TYPE);
        Requests.logRequest(this);
        setURI().setURIParams().readBody().bodyAsJSON().addBodyParams();
    }

    public long getID() {
        return _rhid;
    }

    public String getRemoteAddress() {
        return _exchange.getRemoteAddress().toString();
    }

    public String getRequestURI() {
        return _exchange.getRequestURI().toString();
    }

    public String getContentType() {
        return _contentType;
    }

    public String getRequestMethod() {
        return _reqMethod;
    }

    public String getAccept() {
        return isNonNull(_accept) ? _accept : RAW_TYPE;
    }

    public String getResponseType() {
        if (getRequestURI().toLowerCase().endsWith(".js")) return TEXT_JS;
        final String weAccept = getAccept().toLowerCase();
        if (weAccept.contains("/html")) return TEXT_HTML;
        if (weAccept.contains("/css")) return TEXT_CSS;
        return RAW_TYPE;
    }

    public RequestHandler readBody() {
        _body = null;
        if (!_reqHeaders.containsKey(CONTENT_LENGTH)) return this;
        _bodyType = EBodyType.eUnknown;
        int n = Integer.parseInt(_reqHeaders.getFirst(CONTENT_LENGTH));
        byte buf[] = new byte[n];
        try (InputStream ins = _exchange.getRequestBody()) {
            ins.read(buf, 0, buf.length);
            _body = new String(buf, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Util.TODO(e);
        }
        return this;
    }

    public boolean hasURIParams() {
        return !getURIParams().isEmpty();
    }

    public Map<String, List<String>> getURIParams() {
        return _uriParams;
    }

    public boolean hasBody() {
        return isNonNull(_body) && !_body.isEmpty();
    }

    public Object[] bodyAsObjAry() {
        return (EBodyType.eJsonAry == _bodyType) ? castobj(_bodyObj) : null;
    }

    public Map<String, Object> bodyAsObj() {
        return (EBodyType.eJsonObj == _bodyType) ? castobj(_bodyObj) : null;
    }

    public RequestHandler bodyAsJSON() {
        if (hasBody()) {
            char c = _body.charAt(0);
            try {
                if ('[' == c) {
                    _bodyObj = toArray(new JSONArray(_body));
                    _bodyType = EBodyType.eJsonAry;
                } else if ('{' == c) {
                    _bodyObj = toMap(new JSONObject(_body));
                    _bodyType = EBodyType.eJsonObj;
                } else {
                    _bodyType = EBodyType.eUnknown;
                }
            } catch (JmvcException ex) {
                _exception = ex;
                _bodyType = EBodyType.eJsonParseError;
            }
        }
        return this;
    }

    private RequestHandler addBodyParams() {
        if (isPOST() && hasBody() && (_bodyType == EBodyType.eUnknown)) {
            Map<String, List<String>> bodyParms = getURLParams(_body.trim());
            bodyParms.forEach((key, value) -> {
                if (!_uriParams.containsKey(key)) {
                    _uriParams.put(key, value);
                } else {
                    _uriParams.get(key).addAll(value);
                }
            });
        }
        return this;
    }

    private RequestHandler setURI() {
        String uri = _exchange.getRequestURI().toString();
        int p = uri.indexOf('?');
        _uri = (0 <= p) ? uri.substring(0, p) : uri;
        return this;
    }

    private RequestHandler setURIParams() {
        String uri = _exchange.getRequestURI().toString();
        int p = uri.indexOf('?');
        _uriParams = getURLParams((0 <= p) ? uri.substring(p + 1) : null);
        return this;
    }

    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-type";
    public static final String CONTENT_LENGTH = "Content-length";

    public enum EBodyType {
        eNone,
        eUnknown,
        eJsonParseError,
        eJsonAry,
        eJsonObj
    }

    ;

    protected HttpExchange _exchange;
    protected Headers _reqHeaders;
    protected String _uri, _reqMethod, _contentType, _accept;
    protected String _body;
    protected EBodyType _bodyType = EBodyType.eNone;
    protected Object _bodyObj;
    protected JmvcException _exception;
    protected Map<String, List<String>> _uriParams = null;
    protected long _rhid;

    // unique ID for every handler instance.
    private static long _RHID = 0;

    protected RequestHandler() {
        //do nothing
    }

    /**
     * A lightweight wrapper around handler such that we do not create full-blown
     * instances of RequestHandler for every controller instance.
     * Instead, we create the handler on the fly when route/handler invoked.
     */
    public static abstract class Delegate extends RequestHandler {
        /**
         * Create instance of actual handler.
         *
         * @return instance of RequestHandler.
         */
        public abstract RequestHandler create();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            create().handle(exchange);
        }
    }

    public static RequestHandler handlerFactory(Supplier<RequestHandler> factory) {
        return  factory.get();
    }
}
