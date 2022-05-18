package jmvc.view.html;

import java.io.IOException;

import static gblibx.Util.*;
import static jmvc.Util.getViewFileName;

public class Html {
    public static final String APP_VIEW_TMPL = "html.appViewTmpl";

    private static final String _APP_VIEW_TMPL =
            System.getProperty(APP_VIEW_TMPL, "views/application.x.html");

    public static String replace(String content, Object... keyVals) {
        for (int i = 0; i < keyVals.length; i += 2) {
            String key = "<!--@" + keyVals[i] + "@-->";
            content = content.replace(key, keyVals[i + 1].toString());
        }
        return content;
    }

    public static String partialHtmlify(String pathToHtmlTmpl, Object... keyVals) throws IOException {
        String path = getViewFileName(pathToHtmlTmpl);
        String content = "";
        content = replace(readFile(path), keyVals);
        return content;
    }

    /**
     * Generate toplevel HTML response.
     *
     * @param pathToHtmlTmpl
     * @param keyVals
     * @return
     * @throws IOException
     */
    public static String htmlify(String pathToHtmlTmpl, Object... keyVals) throws IOException {
        String body = partialHtmlify(pathToHtmlTmpl, keyVals);
        String html = partialHtmlify(_APP_VIEW_TMPL, "body", body);
        //in case we modify something in app-view
        return replace(html, keyVals);
    }

    /**
     * Replace special chars with htmlify version, so they appear as chars (not special).
     *
     * @param s input string.
     * @return htmlified string.
     */
    public static String htmlify(String s) {
        return s
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
