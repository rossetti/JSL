package jsl.utilities.random.rvariable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jsl.utilities.reporting.JSONUtil;

import java.lang.reflect.Type;
import java.util.*;

/**
 *  This class facilitates the setting and getting of parameters associated with random variables.
 */
public class RVParameters {

    private RVType type;

    private int streamNumber;

    private final List<String> parameterNames;

    private final Map<String, Double> parameterNameValueMap;

    public RVParameters() {
        parameterNames = new ArrayList<>();
        parameterNameValueMap = new LinkedHashMap<>();
    }

    /** Used internally to set the stream number
     *
     * @param streamNumber must be 1 or more
     */
    final void setStreamNumber(int streamNumber){
        if (streamNumber <= 0){
            throw new IllegalArgumentException("The stream number must be 1 or more");
        }
        this.streamNumber = streamNumber;
    }
    /** Used internally to set the type
     *
     * @param type the type
     */
    final void setRVType(RVType type){
        Objects.requireNonNull(type, "The supplied type was null");
        this.type = type;
    }

    /** Used internally to fill parameters
     *
     * @param name the name
     * @param value the value, No validation is performed.
     */
    final void addParameter(String name, double value){
        Objects.requireNonNull(name, "The supplied name was null");
        parameterNameValueMap.put(name, value);
        parameterNames.add(name);
    }

    /**
     *
     * @return the JSON string representation
     */
    public String toJSON(){
        return JSONUtil.toJSONPretty(this);//TODO
    }

    /**
     *
     * @param json a json string representing a RVParameters instance
     * @return the created parameters
     */
    public static RVParameters createFromCJSON(String json){
        Objects.requireNonNull(json, "The supplied json string was null");
        Gson gson = new Gson();
        Type type = new TypeToken<RVParameters>(){}.getType();
        return gson.fromJson(json, type);
    }

    /**
     *
     * @return the type of random variable associated with the parameters
     */
    public final RVType getRVType(){
        return type;
    }

    /**
     *
     * @param name the name, must not be null, and must be a proper parameter name
     *             for this type of random variable
     * @param value the value to assign. No validation is performed.
     */
    public final void setParameterValue(String name, double value){
        if (!parameterNameValueMap.containsKey(name)){
            throw new IllegalArgumentException("A parameter called " + name + " doesn't exist");
        }
        parameterNameValueMap.put(name, value);
    }

    /**
     *
     * @param i the ith parameter, starting at 0
     * @param value the value to assign. No validation is performed.
     */
    public final void setParameterValue(int i, double value){
        setParameterValue(parameterNames.get(i), value);
    }

    /**
     *
     * @param name the name, must not be null, and must be a proper parameter name
     *                  for this type of random variable
     * @return the current value
     */
    public final double getParameterValue(String name){
        if (!parameterNameValueMap.containsKey(name)){
            throw new IllegalArgumentException("A parameter called " + name + " doesn't exist");
        }
        return parameterNameValueMap.get(name);
    }

    /**
     *
     * @return a map holding the parameter names and their current values
     */
    public final Map<String, Double> getParameters(){
        return new LinkedHashMap<>(parameterNameValueMap);
    }

    /**
     *
     * @param i the ith parameter, starting at 0
     * @return the value
     */
    public final double getParameterValue(int i){
        return getParameterValue(parameterNames.get(i));
    }

    /**
     *
     * @return the number of parameters for this random variable
     */
    public final int getNumberOfParameters(){
        return parameterNames.size();
    }

    /**
     *
     * @return the list of valid parameter names for this random variable
     */
    public final List<String> getParameterNames(){
        return Collections.unmodifiableList(parameterNames);
    }

}
