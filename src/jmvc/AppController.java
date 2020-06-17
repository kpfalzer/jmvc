package jmvc;

import com.sun.net.httpserver.HttpExchange;
import jmvc.model.Table;
import jmvc.server.RequestHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Base of all controllers.
 */
public class AppController<E extends Enum<E>> {
    protected AppController(Table<E> model, boolean addDefaultCreate) {
        _model = model;
        if (addDefaultCreate) {
            addDefaultCreate();
        }
    }

    /**
     * Add route for /table/create (POST)
     */
    private void addDefaultCreate() {
        final String path = String.format("/%s/%s", _model.name.toLowerCase(), CREATE);
        App.addRoute(path, _createHandler);
    }

    protected final Table<E> _model;
    public static final String CREATE = "create";

    private final RequestHandler _createHandler = new RequestHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            initialize(exchange);
            boolean isValid = isPOST() && (_bodyType == EBodyType.eJsonObj);
            if (! isValid) {
                //TODO: need to respond
                throw new Exception.TODO("Expected POST and eJsonObj");
            }
            final Map<String, Object> kvs = bodyAsObj();
            Integer id = _model.insertRow(kvs);
            sendResponse(id.toString(), "json");
        }
    };
}
