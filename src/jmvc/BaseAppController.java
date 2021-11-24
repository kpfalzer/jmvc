package jmvc;

import com.sun.net.httpserver.HttpExchange;
import jmvc.server.RequestHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;

/**
 * Base of all controllers.
 * No relation to a model (use AppController for that).
 */
public abstract class BaseAppController {
    /**
     * Add a view (handler) for designated route.
     *
     * @param path REST route: typically /table/verb...
     * @param view view handler.
     * @return this controller instance.
     */
    protected BaseAppController addRoute(String path, AppView view) {
        //Handler is lightweight delegate, so we do not instance
        //heavy RequestHandler for every controller instance.
        App.addRoute(path, ()->new ViewHandler(view));
        return this;
    }

    /**
     * View handler using lambda.
     *
     * @param path     REST route.
     * @param xhandler view handler.
     * @return this controller instance.
     */
    protected BaseAppController addRoute(String path, Consumer<ViewHandler> xhandler) {
        addRoute(path, new AppView() {
            @Override
            public void handle(ViewHandler handler) {
                xhandler.accept(handler);
                App.closeDbaseConnection();
            }
        });
        return this;
    }


    /**
     * Handler to delegate between router and view.
     */
    public static class ViewHandler extends RequestHandler {
        private ViewHandler(AppView view) {
            _view = view;
        }

        private final AppView _view;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            initialize(exchange);
            _view.handle(this);
        }

        /**
         * Get scalar parameter.
         *
         * @param key        parameter name.
         * @param convert    funciton to convert value to T.
         * @param defaultVal default value.
         * @param <T>        type of value expected.
         * @return converted value or default.
         */
        public <T> T getURIParamVal(String key, Function<String, T> convert, T defaultVal) {
            List<String> vals = getURIParams().get(key);
            //pick off last element
            String sval = (isNull(vals) || vals.isEmpty()) ? null : vals.get(vals.size()-1);
            return (isNull(sval))
                    ? defaultVal
                    : applyConversion(sval, convert, defaultVal);
        }

        /**
         * Apply conversion and catch exception to force default.
         *
         * @param sval       lookup value.
         * @param convert    conversion function.
         * @param defaultVal default value.
         * @param <T>        type of return value.
         * @return converted sval or defaultVal.
         */
        private static <T> T applyConversion(String sval, Function<String, T> convert, T defaultVal) {
            T rval;
            try {
                rval = convert.apply(sval);
            } catch (Exception e) {
                rval = defaultVal;
            }
            return rval;
        }

        /**
         * Get scalar parameter value.
         *
         * @param key        parameter name.
         * @param defaultVal default value.
         * @return value or default.
         */
        public Integer getURIParamVal(String key, int defaultVal) {
            return getURIParamVal(key, (String s) -> {
                return Integer.parseInt(s);
            }, defaultVal);
        }

        /**
         * Get scalar parameter value.
         *
         * @param key        parameter name.
         * @param defaultVal default value.
         * @return value or default.
         */
        public String getURIParamVal(String key, String defaultVal) {
            return getURIParamVal(key, (String s) -> {
                try {
                    return URLDecoder.decode(s, StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return s;
                }
            }, defaultVal);
        }
    }

}
