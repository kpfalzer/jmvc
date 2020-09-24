package jmvc.view.html;

import java.io.IOException;

import static gblibx.Util.*;
import static jmvc.Util.getViewFileName;

public class Html {
    public static final String APP_VIEW_TMPL = "html.appViewTmpl";

    private static final String _APP_VIEW_TMPL =
            System.getProperty(APP_VIEW_TMPL, "views/application.x.html");

    public static String htmlify(String pathToHtmlTmpl, Object... keyVals) throws IOException {
        invariant(isEven(keyVals.length));
        String path = getViewFileName(_APP_VIEW_TMPL);
        path = getViewFileName(pathToHtmlTmpl);
        String content = "";
        content = readFile(getViewFileName(_APP_VIEW_TMPL))
                .replace("<!--@body@-->", readFile(getViewFileName(pathToHtmlTmpl)));
        for (int i = 0; i < keyVals.length; i += 2) {
            String key = "<!--@" + keyVals[i] + "@-->";
            content = content.replace(key, keyVals[i + 1].toString());
        }
        return content.toString();
    }
}
