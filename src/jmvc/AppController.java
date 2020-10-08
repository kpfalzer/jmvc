package jmvc;

import com.sun.net.httpserver.HttpExchange;
import jmvc.model.Table;
import jmvc.server.RequestHandler;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static jmvc.view.Json.createResponse;

/**
 * Base of all controllers.
 */
public abstract class AppController<E extends Enum<E>> {
    protected AppController(Table<E> model) {
        this(model, true);
    }

    protected AppController(Table<E> model, boolean addDefaultCreate) {
        _model = model;
        if (addDefaultCreate) {
            addDefaultCreate();
        }
    }

    /**
     * Add a view (handler) for designated route.
     *
     * @param path REST route: typically /table/verb...
     * @param view view handler.
     * @return this controller instance.
     */
    protected AppController addRoute(String path, AppView view) {
        //Handler is lightweight delegate, so we do not instance
        //heavy RequestHandler for every controller instance.
        App.addRoute(path, new ViewHandler.Delegate(view));
        return this;
    }

    /**
     * View handler using lambda.
     *
     * @param path     REST route.
     * @param xhandler view handler.
     * @return this controller instance.
     */
    protected AppController addRoute(String path, Consumer<ViewHandler> xhandler) {
        addRoute(path, new AppView() {
            @Override
            public void handle(ViewHandler handler) {
                xhandler.accept(handler);
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

        public <T> T getUriParamVal(String key, Function<String, T> convert, T defaultVal) {
            String sval = gblibx.Util.applyIfNotNull(getUriParams(), (m)->m.get(key));
            return (isNull(sval))
                    ? defaultVal
                    : applyConversion(sval, convert, defaultVal);
        }

        /**
         * Apply conversion and catch exception to force default.
         * @param sval lookup value.
         * @param convert conversion function.
         * @param defaultVal default value.
         * @param <T> type of return value.
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

        public Integer getUriParamVal(String key, int defaultVal) {
            return getUriParamVal(key, (String s) -> {
                return Integer.parseInt(s);
            }, defaultVal);
        }

        public String getUriParamVal(String key, String defaultVal) {
            return getUriParamVal(key, (String s) -> {
                return s;
            }, defaultVal);
        }

        private static class Delegate extends RequestHandler.Delegate {
            private Delegate(AppView view) {
                _view = view;
            }

            private final AppView _view;

            @Override
            public RequestHandler create() {
                return new ViewHandler(_view);
            }
        }
    }

    /**
     * Add route for /table/create (POST+json)
     */
    protected void addDefaultCreate() {
        // TODO: each Controller instance could have this heavyweight(?) object.
        // TODO: we could use Handler.Delegate like as above?
        _createHandler = new CreateHandler();
        final String path = String.format("/%s/%s", _model.name.toLowerCase(), CREATE);
        App.addRoute(path, _createHandler);
    }

    protected class CreateHandler extends RequestHandler {
        protected CreateHandler() {
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                initialize(exchange);
                boolean isValid = isPOST() && (_bodyType == EBodyType.eJsonObj);
                if (!isValid) {
                    //TODO: need to respond
                    throw new JmvcException.TODO("Expected POST and eJsonObj");
                }
                final Map<String, Object> kvs = bodyAsObj();
                Integer id = _model.insertRow(kvs);
                sendResponse(createResponse(id), APPL_JSON);
            } catch (IOException ex) {
                JmvcException.printStackTrace(ex);
                throw ex;
            } catch (Exception ex) {
                Util.TODO(ex);
            }
        }
    }

    protected final Table<E> _model;
    public static final String CREATE = "create";

    private RequestHandler _createHandler = null;
}
