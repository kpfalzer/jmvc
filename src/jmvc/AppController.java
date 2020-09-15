package jmvc;

import com.sun.net.httpserver.HttpExchange;
import jmvc.model.Table;
import jmvc.server.RequestHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import static gblibx.Util.toMap;

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
     * @param view  view handler.
     * @return this controller instance.
     */
    protected AppController addRoute(String path, AppView view) {
        //Handler is lightweight delegate, so we do not instance
        //heavy RequestHandler for every controller instance.
        App.addRoute(path, new Handler.Delegate(view));
        return this;
    }

    /**
     * Handler to delegate between router and view.
     */
    private static class Handler extends RequestHandler {
        private Handler(AppView view) {
            _view = view;
        }

        private final AppView _view;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            initialize(exchange);
            //todo
        }

        private static class Delegate extends RequestHandler.Delegate {
            private Delegate(AppView view) {
                _view = view;
            }

            private final AppView _view;

            @Override
            public RequestHandler create() {
                return new Handler(_view);
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
        protected CreateHandler() {}

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            initialize(exchange);
            boolean isValid = isPOST() && (_bodyType == EBodyType.eJsonObj);
            if (!isValid) {
                //TODO: need to respond
                throw new Exception.TODO("Expected POST and eJsonObj");
            }
            final Map<String, Object> kvs = bodyAsObj();
            Integer id = _model.insertRow(kvs);
            sendResponse(createResponse(id), "application/json");
        }
    }

    protected final Table<E> _model;
    public static final String CREATE = "create";

    protected static String createResponse(Integer id) {
        final Map<String, Object> rmap = toMap("status", 0, "result", id);
        final JSONObject jsobj = new JSONObject(rmap);
        return jsobj.toString();
    }

    private RequestHandler _createHandler = null;
}
