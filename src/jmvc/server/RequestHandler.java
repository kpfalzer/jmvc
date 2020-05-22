package jmvc.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jmvc.Exception;
import jmvc.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.*;

public abstract class RequestHandler implements HttpHandler {
    protected void _initialize(HttpExchange exchange) {
        _exchange = exchange;
        _reqHeaders = _exchange.getRequestHeaders();
        _reqMethod = _exchange.getRequestMethod();
        _accept = _reqHeaders.getFirst(ACCEPT);
        _contentType = _reqHeaders.getFirst(CONTENT_TYPE);
        __setURI()._readBody()._bodyAsJSON();
    }

    protected RequestHandler _readBody() {
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

    protected boolean _hasUriParams() {
        return isNonNull(_uriParams);
    }

    protected boolean _hasBody() {
        return isNonNull(_body);
    }

    protected Object[] _bodyAsObjAry() {
        return (EBodyType.eJsonAry == _bodyType) ? castobj(_bodyObj) : null;
    }

    protected Map<String, Object> _bodyAsObj() {
        return (EBodyType.eJsonObj == _bodyType) ? castobj(_bodyObj) : null;
    }

    protected RequestHandler _bodyAsJSON() {
        if (_hasBody()) {
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

    private RequestHandler __setURI() {
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

    public static enum EBodyType {
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
