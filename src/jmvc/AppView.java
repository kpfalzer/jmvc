package jmvc;

import org.json.JSONObject;

import java.util.Map;

import static gblibx.Util.toMap;

public abstract class AppView {
    protected AppView() {
    }

    //NOTE: not sure yet if this is right params?
    public abstract void handle(AppController.ViewHandler handler);

    public static class Helper {
        public static class JSON {
            public static String createResponse(Integer id) {
                final Map<String, Object> rmap = toMap("status", 0, "result", id);
                final JSONObject jsobj = new JSONObject(rmap);
                return jsobj.toString();
            }

        }
    }
}
