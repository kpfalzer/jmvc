package jmvc.logging;

import gblibx.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static gblibx.Util.invariant;
import static gblibx.Util.isEven;

/**
 * Map of messages by code.
 */
public class Messages {

    /**
     * Format message with severity, time and code.
     * @param lvl severity level.
     * @param code message code.
     * @param args message args.
     * @return formatted message: "lvl-TIME-formatted (code)"
     */
    public static String format(Logger.ELevel lvl, String code, Object... args) {
        final String msg = _theOne._format(code, args);
        return Logger.getMessage(lvl, msg);
    }

    public static String format(String code, Object... args) {
        return format(Logger.ELevel.eMessage, code, args);
    }

    /**
     * Format message with severity, time (and no code).
     * @param lvl severity level.
     * @param code message code.
     * @param args message args.
     * @return formatted message: "lvl-TIME-formatted"
     */
    public static String formatnc(Logger.ELevel lvl, String code, Object... args) {
        final String msg = _theOne._format(false, code, args);
        return Logger.getMessage(lvl, msg);
    }

    private String _format(String code, Object... args) {
        return _format(true, code, args);
    }

    private String _format(boolean showCode, String code, Object... args) {
        String formatted;
        try {
            String fmt;
            if (_msgByCode.containsKey(code)) {
                fmt = _msgByCode.get(code);
            } else {
                fmt = _msgByCode.get(_UNKNOWN);
                String lastArg = Arrays
                        .stream(args)
                        .map(e -> e.toString())
                        .collect(Collectors.joining(" "));
                args = new String[]{code, lastArg};
                code = _UNKNOWN;
            }
            formatted = String.format(fmt, args);
        } catch (Exception ex) {
            formatted = this.getClass().getName()
                    + "._format: throws exception: "
                    + ex.getMessage();
            code = "EXCEPTION-1";
        }
        if (showCode)
            formatted += "  (" + code + ")";
        return formatted;
    }

    private Messages() {
        initialize();
    }

    private Messages initialize() {
        invariant(isEven(_MESSAGES.length));
        for (int i = 0; i < _MESSAGES.length; i += 2) {
            _msgByCode.put(_MESSAGES[i], _MESSAGES[i + 1]);
        }
        return this;
    }

    private static final String _UNKNOWN = "UNKNOWN-1";

    // pairs of code, message
    private static final String[] _MESSAGES = new String[]{
            "REQ-1", "%s %s %s %s",
            _UNKNOWN, "Unknown code: %s.  Arguments: %s"
    };

    private final Map<String, String> _msgByCode = new HashMap<>();
    private static Messages _theOne = new Messages();
}
