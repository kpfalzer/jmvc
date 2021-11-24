package jmvc;

import com.sun.net.httpserver.HttpExchange;
import jmvc.model.Table;
import jmvc.server.RequestHandler;

import java.io.IOException;
import java.util.Map;

import static jmvc.view.Json.createResponse;

/**
 * Base of all controllers: with explicit model (i.e., controller->model).
 * For controllers with no model use BaseAppController.
 */
public abstract class AppController<E extends Enum<E>> extends BaseAppController {
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
     * Add route for /table/create (POST+json)
     */
    protected void addDefaultCreate() {
        final String path = String.format("/%s/%s", _model.name.toLowerCase(), CREATE);
        App.addRoute(path, CreateHandler::new);
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
