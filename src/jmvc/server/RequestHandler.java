package jmvc.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jmvc.Exception;
import jmvc.Util;
import jmvc.logging.Requests;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.*;
import static java.net.HttpURLConnection.HTTP_OK;

public abstract class RequestHandler implements HttpHandler {
    protected RequestHandler sendResponse(String response, String xtype) throws IOException {
        final String type = String.format("application/%s", xtype);
        final int length = response.length();
        _exchange.getResponseHeaders().add("Content-type", type);
        _exchange.getResponseHeaders().add("Content-length", Integer.toString(length));
        _exchange.sendResponseHeaders(HTTP_OK, length);
        _exchange.getResponseBody().write(response.getBytes());
        _exchange.close();
        return this;
    }

    public boolean isPOST() {
        return getRequestMethod().equalsIgnoreCase("POST");
    }

    protected void initialize(HttpExchange exchange) {
        _exchange = exchange;
        _reqHeaders = _exchange.getRequestHeaders();
        _reqMethod = _exchange.getRequestMethod();
        _accept = _reqHeaders.getFirst(ACCEPT);
        _contentType = _reqHeaders.getFirst(CONTENT_TYPE);
        Requests.logRequest(this);
        setURI().readBody().bodyAsJSON();
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
        return _accept;
    }

    protected RequestHandler readBody() {
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

    protected boolean hasUriParams() {
        return isNonNull(_uriParams);
    }

    protected boolean hasBody() {
        return isNonNull(_body);
    }

    protected Object[] bodyAsObjAry() {
        return (EBodyType.eJsonAry == _bodyType) ? castobj(_bodyObj) : null;
    }

    protected Map<String, Object> bodyAsObj() {
        return (EBodyType.eJsonObj == _bodyType) ? castobj(_bodyObj) : null;
    }

    protected RequestHandler bodyAsJSON() {
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
            } catch (Exception ex) {
                _exception = ex;
                _bodyType = EBodyType.eJsonParseError;
            }
        }
        return this;
    }

    private RequestHandler setURI() {
        //todo: how do funny chars appear?
        String uri = _exchange.getRequestURI().toString();
        int p = uri.indexOf('?');
        if (0 <= p) {
            _uri = uri.substring(0, p);
            uri = uri.substring(p + 1);
            _uriParams = new HashMap<>();
            for (String kv : uri.split("&")) {
                p = kv.indexOf('=');
                String k = (0 < p) ? kv.substring(0, p) : kv;
                String v = (0 < p) ? kv.substring(p + 1) : null;
                //TODO: repeated param overwrites here.  Do we want to append->list?
                _uriParams.put(k, v);
            }
        } else {
            _uri = uri;
        }
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
    protected Exception _exception;
    protected Map<String, String> _uriParams = null;

    protected RequestHandler() {
        //do nothing
    }
}
