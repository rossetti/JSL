package jsl.utilities.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONUtil {

    private JSONUtil() {
    }

    //static simple toJSON call for an object with good defaults (compact)
    public static String toJSONCompact(Object o) {
        return (toJSON(o, false, true));
    }

    public static String toJSONCompact(Object o, boolean serializeNulls) {
        return (toJSON(o, false, serializeNulls));
    }

    // static pretty-print toJSON call for an object with good defaults (pretty)
    public static String toJSONPretty(Object o) {
        return (toJSON(o, true, true));
    }

    // static pretty-print toJSON call for an object with good defaults (pretty)
    public static String toJSONPretty(Object o, boolean serializeNulls) {
        return (toJSON(o, true, serializeNulls));
    }

    public static String toJSON(Object o, boolean pretty, boolean serializeNulls) {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .serializeSpecialFloatingPointValues();
        if (pretty) gsonBuilder.setPrettyPrinting();
        if (serializeNulls) gsonBuilder.serializeNulls();
        Gson gson = gsonBuilder.create();
        return (gson.toJson(o));
    }

}
