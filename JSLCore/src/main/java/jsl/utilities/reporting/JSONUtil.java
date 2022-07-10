package jsl.utilities.reporting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jsl.utilities.random.rvariable.RVParameterSetter;
import jsl.utilities.random.rvariable.RVParameters;
import jsl.utilities.random.rvariable.RVType;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

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

//    public static <T> String toJSON(Object o, RuntimeTypeAdapterFactory<T> adapter){
//        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(adapter).create();
//        return gson.toJson(o);
//    }

    /**
     * Converts the JSON string to a RVParameterSetter.
     *
     * @param json a json string representing a {@literal RVParameterSetter}
     * @return the created RVParameterSetter
     */
    public static RVParameterSetter fromJSON(String json) {
        Objects.requireNonNull(json, "The supplied json string was null");
        Type type = new TypeToken<RVParameterSetter>() {
        }.getType();
        return getRVParametersGson().fromJson(json, type);
    }

    /**
     *
     * @param setter the setter to convert
     * @return the converted string
     */
    public static String rvParameterSetterToJson(RVParameterSetter setter){
        return getRVParametersGson().toJson(setter);
    }

    private static Gson getRVParametersGson(){
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapterFactory(makeRVParametersAdapter())
                .create();
        return gson;
    }

    private static RuntimeTypeAdapterFactory<RVParameters> makeRVParametersAdapter(){
        RuntimeTypeAdapterFactory<RVParameters> adapter =
                RuntimeTypeAdapterFactory.of(RVParameters.class, "typeField");
        for (RVType type : RVType.RVTYPE_SET) {
            adapter.registerSubtype(type.getRVParameters().getClass(), type.getRVParameters().getClass().getName());
        }
        return adapter;
    }

    /**
     *
     * @param json a json string representing a {@literal Map<String, Double>}
     * @return the created map
     */
    public static Map<String, Double> fromJSONStringToMapStringDouble(String json){
        Objects.requireNonNull(json, "The supplied json string was null");
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Map<String, Double>>(){}.getType();
        return gson.fromJson(json, collectionType);
    }
}
