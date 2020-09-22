package jmvc.view;

import org.json.JSONObject;

import java.util.Map;

import static gblibx.Util.toMap;

public class Json {
    public static String createResponse(Integer id) {
        final Map<String, Object> rmap = toMap("status", 0, "result", id);
        final JSONObject jsobj = new JSONObject(rmap);
        return jsobj.toString();
    }
}
