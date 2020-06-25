package jmvc.server;

import com.sun.net.httpserver.HttpExchange;
import jmvc.App;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static gblibx.Util.*;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.util.Objects.isNull;
import static jmvc.Util.TODO;

public class StaticPageHandler {
    public static StaticPageHandler addDefault() {
        return addDefault("/public");
    }

    public static StaticPageHandler addDefault(String path) {
        App.addRoute(path, _REQUEST_HANDLER);
        return _theOne;
    }

    private static byte[] createResponse(String fname) {
        byte[] resp = null;
        File file = getFile(getFileName(fname));
        if (isNonNull(file)) {
            try {
                resp = Files.readAllBytes(toPath(file));
            } catch (IOException e) {
                TODO(e);
                file = null;
            }
        }
        return resp;
    }

    private static File getFile(String fname) {
        File file = getReadableFile(fname);
        if (isNull(file)) {
            file = getReadableFile(getFileName("/public/404.html"));
        }
        return file;
    }

    private static String getFileName(String fname) {
        return getAbsoluteFileName(ROOT_DIR + fname);
    }

    private static final RequestHandler _REQUEST_HANDLER = new RequestHandler() {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Handler.factory().handle(exchange);
        }
    };

    private static class Handler extends RequestHandler {
        private static RequestHandler factory() {
            return new Handler();
        }

        private Handler() {
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            initialize(exchange);
            final byte[] response = createResponse(getRequestURI());
            if (isNonNull(response)) {
                final String type = getResponseType();
                sendResponse(response, type);
            } else {
                sendBadResponse(HTTP_NOT_FOUND);
            }
        }
    }

    private StaticPageHandler() {
    }

    private static final StaticPageHandler _theOne = new StaticPageHandler();

    public static final String ROOT_DIR =
            System.getProperty("server.StaticPageHandler.ROOT_DIR", ".");
}
