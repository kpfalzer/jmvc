package jmvc.view.html;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static gblibx.Util.invariant;
import static gblibx.Util.isEven;
import static jmvc.Util.getViewFileName;

public class Html {
    public static String htmlify(String pathToHtmlTmpl, Object... keyVals) throws IOException {
        invariant(isEven(keyVals.length));
        String path = getViewFileName(pathToHtmlTmpl);
        String content = "";
        String tmplContent = new String(Files.readAllBytes(Paths.get(path)));
        content = new StringBuilder(getHead()).append(tmplContent).append(getTail()).toString();
        for (int i = 0; i < keyVals.length; i += 2) {
            String key = "<!--@" + keyVals[i] + "@-->";
            content = content.replace(key, keyVals[i + 1].toString());
        }
        return content.toString();
    }

    private static String getHead() {
        StringBuilder sb = new StringBuilder(
                "<!DOCTYPE html>\n" +
                "<meta charset=\"utf-8\" />\n" +
                "<html>\n" +
                "<head>\n" +
                "<title><!--@title@--></title>\n"
        );
        //<script src="/public/jquery-3.5.1.min.js"></script>
        for (String s : HEAD_SCRIPT_SRC) {
            sb.append("<script src=\"").append(s).append("\"></script>\n");
        }
        sb.append("</head>\n").append("<body>\n");
        return sb.toString();
    }

    private static String getTail() {
        return new StringBuilder().append("</body>\n").append("</html>\n").toString();
    }

    /**
     * App should set these (once).  Applies to all generated HTML.
     */
    public static String[] HEAD_SCRIPT_SRC = new String[]{
            "/public/jquery-3.5.1.min.js"
    };


}
