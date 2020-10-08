package jmvc.view;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;

import static gblibx.Util.toMap;

public class Json {
    public static String createResponse(Integer id) {
        return createResponse(id, 0);
    }

    public static String createResponse(Integer id, int status) {
        final Map<String, Object> rmap = toMap("status", status, "result", id);
        final JSONObject jsobj = new JSONObject(rmap);
        return jsobj.toString();
    }

    public static <T> JSONArray toJsonArray(List<T> eles, Function<T, Map<String, Object>> toMap) {
        List<Object> valsAsMap = new LinkedList<>();
        for (T v : eles) {
            Map<String, Object> valAsMap = toMap.apply(v);
            valsAsMap.add(valAsMap);
        }
        return new JSONArray(valsAsMap.toArray());
    }

    public static <T> String createResponse(List<T> eles, Function<T, Map<String, Object>> toMap) {
        return toJsonArray(eles, toMap).toString();
    }

    /**
     * Create JSON response as map of key/string to array of object.
     *
     * @param map map of list elements.
     * @return JSON map of arrays.
     */
    public static <T> String createResponse(Map<String, List<T>> map, Function<T, Map<String, Object>> toMap) {
        final Map<String, JSONArray> rmap = new HashMap<>();
        for (String k : map.keySet()) {
            rmap.put(k, toJsonArray(map.get(k), toMap));
        }
        return new JSONObject(rmap).toString();
    }
}
