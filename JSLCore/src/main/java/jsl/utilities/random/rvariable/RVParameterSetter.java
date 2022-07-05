package jsl.utilities.random.rvariable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.misc.RuntimeTypeAdapterFactory;
import jsl.utilities.random.RandomIfc;

import java.lang.reflect.Type;
import java.util.*;

public class RVParameterSetter {

    private String modelName;

    private int modelId;

    private final LinkedHashMap<String, RVParameters> rvParameters;

    public RVParameterSetter() {
        rvParameters = new LinkedHashMap<>();
    }

    /**
     * @param model the model to process
     * @return the parameters in a map for each parameterized random variable in the model
     */
    public Map<String, RVParameters> extractParameters(Model model) {
        Objects.requireNonNull(model, "The supplied model was null");
        modelName = model.getName();
        modelId = model.getId();
        List<RandomVariable> rvList = model.getRandomVariables();
        for (RandomVariable rv : rvList) {
            RandomIfc rs = rv.getInitialRandomSource();
            if (rs instanceof ParameterizedRV) {
                rvParameters.put(rv.getName(), ((ParameterizedRV) rs).getParameters());
            }
        }
        return Collections.unmodifiableMap(rvParameters);
    }

    /**
     * Converts double and integer parameters to a Map that holds a Map, with the
     * outer key being the random variable name and the inner map the parameter names
     * as keys and the parameter values as values.  Ignores any double array parameters
     * and converts any integer parameter values to doubles.
     *
     * @return the parameters as a map of maps
     */
    public final Map<String, Map<String, Double>> getParametersAsDoubles() {
        LinkedHashMap<String, Map<String, Double>> theMap = new LinkedHashMap<>();
        for (Map.Entry<String, RVParameters> entry : rvParameters.entrySet()) {
            String rvName = entry.getKey();
            if (!theMap.containsKey(rvName)) {
                Map<String, Double> innerMap = new LinkedHashMap<>();
                theMap.put(rvName, innerMap);
            }
            Map<String, Double> innerMap = theMap.get(rvName);
            RVParameters parameters = entry.getValue();
            for (String pName : parameters.getDoubleParameterKeySet()) {
                innerMap.put(pName, parameters.getDoubleParameter(pName));
            }
            for (String pName : parameters.getIntegerParameterKeySet()) {
                double v = parameters.getIntegerParameter(pName);
                innerMap.put(pName, v);
            }
            // ignore any double[] parameters
        }
        return theMap;
    }

    /**
     * Uses getParametersAsDoubles() to get a map of map, then flattens the map
     * to a single map with the key as the concatenated key of the outer and inner keys
     * concatenated with the "_PARAM_" character string. The combined key needs to be unique
     * and not be present within the random variable names.
     *
     * @return the flattened map
     */
    public final Map<String, Double> getFlatParametersAsDoubles(){
        return getFlatParametersAsDoubles("_PARAM_");
    }

    /**
     * Uses getParametersAsDoubles() to get a map of map, then flattens the map
     * to a single map with the key as the concatenated key of the outer and inner keys
     * concatenated with the supplied character string. The combined key needs to be unique
     * and not be present within the random variable names.
     *
     * @param conCatString the string to form the common key
     * @return the flattened map
     */
    public final Map<String, Double> getFlatParametersAsDoubles(String conCatString) {
        Objects.requireNonNull(conCatString, "The concatenation string must not be null");
        return JSLArrayUtil.flattenMap(getParametersAsDoubles(), conCatString);
    }

    /**
     * A convenience method that will set any Double or Integer parameter to the
     * supplied double value provided that the named random variable is
     * available to be set, and it has the named parameter.
     * <p>
     * The inner map represents the parameters to change.
     * Double values are coerced to Integer values
     * by rounding up. If the key of the supplied map representing the
     * random variable to change is not found, then no change occurs.
     * If the parameter name is not found for the named random variable's parameters
     * then no change occurs.  In other words, the change "fails silently"
     *
     * @param settings the map of settings
     */
    public void changeParameters(Map<String, Map<String, Double>> settings) {
        Objects.requireNonNull(settings, "The supplied map was null");
        for (Map.Entry<String, Map<String, Double>> entry : settings.entrySet()) {
            String rvName = entry.getKey();
            if (rvParameters.containsKey(rvName)) {
                for (Map.Entry<String, Double> e : settings.get(rvName).entrySet()) {
                    String paramName = e.getKey();
                    double value = e.getValue();
                    changeParameter(rvName, paramName, value);
                }
            }
        }
    }

    /**
     * A convenience method to change the named parameter of the named random variable
     * to the supplied value. This will work with either double or integer parameters.
     * Integer parameters are coerced to the rounded up value of the supplied double,
     * provided that the integer can hold the supplied value.  If the named
     * random variable is not in the setter, then no value will change. If the named
     * parameter is not associated with the random variable type, then no change occurs.
     * In other words, the action fails, silently by returning false.
     *
     * @param rvName    the name of the random variable to change, must not be null
     * @param paramName the parameter name of the random variable, must not be null
     * @param value     the value to change to
     * @return true if the value was changed, false if no change occurred
     */
    public boolean changeParameter(String rvName, String paramName, double value) {
        Objects.requireNonNull(rvName, "The supplied random variable name was null");
        Objects.requireNonNull(paramName, "The supplied parameter name was null");
        if (!rvParameters.containsKey(rvName)) {
            return false;
        }
        RVParameters parameters = rvParameters.get(rvName);
        if (!parameters.containsParameter(paramName)) {
            return false;
        }
        // ask the parameter to make the change
        return parameters.changeParameter(paramName, value);
    }

    /**
     * The returned map cannot be modified, but the values can be retrieved and changed
     * as needed. Changing the values have no effect within the model until they are applied.
     *
     * @return parameters for every parameterized random variable within the model
     */
    public Map<String, RVParameters> getAllRVParameters() {
        return Collections.unmodifiableMap(rvParameters);
    }

    /**
     * Gets a parameters instance for the named random variable. This will be the current parameter settings being
     * used in the model.  This instance can be changed and then applied to the model.
     *
     * @param rvName the name of the random variable from the model, must not be null and must be in the model
     * @return the parameters associated with the named random variable
     */
    public RVParameters getRVParameters(String rvName) {
        Objects.requireNonNull(rvName, "The name of the random variable cannot be null");
        if (!rvParameters.containsKey(rvName)) {
            throw new IllegalArgumentException("The supplied name is not a valid random variable name");
        }
        return rvParameters.get(rvName);
    }

    /**
     * @return the list of names for the random variables that are parameterized
     */
    public List<String> getRandomVariableNames() {
        return new ArrayList<String>(rvParameters.keySet());
    }

    /**
     * @return the number of parameterized random variables that can be changed
     */
    public int getNumberOfParameterizedRandomVariables() {
        return rvParameters.size();
    }

    /**
     * @return the number of parameterized random variables that had their parameters changed in some way
     */
    public int applyParameterChanges(Model model) {
        Objects.requireNonNull(model, "The supplied model was null");
        if (!modelName.equals(model.getName())) {
            throw new IllegalArgumentException("Cannot apply parameters from model, " + model.getName() +
                    ", to model " + modelName);
        }
        if (modelId != model.getId()) {
            throw new IllegalArgumentException("Cannot apply parameters from model id =, " + model.getId() +
                    ", to model with id = " + modelId);
        }
        if (model.isRunning()) {
            Simulation.LOGGER.warn("The model was running when attempting to apply parameter changes");
        }
        int countChanged = 0;
        List<RandomVariable> rvList = model.getRandomVariables();
        for (RandomVariable rv : rvList) {
            RandomIfc rs = rv.getInitialRandomSource();
            if (rs instanceof ParameterizedRV) {
                String rvName = rv.getName();
                // compare the map entries
                RVParameters toBe = rvParameters.get(rvName);
                RVParameters current = ((ParameterizedRV) rs).getParameters();
                if (!toBe.equals(current)) {
                    // change has occurred
                    countChanged++;
                    rv.setInitialRandomSource(toBe.createRVariable());
                }
            }
        }
        Simulation.LOGGER.info("{} out of {} random variable parameters were changed in the model via the parameter setter.",
                countChanged, getNumberOfParameterizedRandomVariables());
        return countChanged;
    }

    /**
     * @return the JSON representation of the RVParameterSetter
     */
    public String toJSON() {
        Type type = new TypeToken<RVParameterSetter>() {
        }.getType();
        return getAdaptedGson().toJson(this);
    }

    private static Gson getAdaptedGson() {
        // https://stackoverflow.com/questions/16000163/using-gson-and-abstract-classes
        RuntimeTypeAdapterFactory<RVParameters> adapter =
                RuntimeTypeAdapterFactory.of(RVParameters.class, "typeField");
        for (RVType type : RVType.RVTYPE_SET) {
            adapter.registerSubtype(type.getRVParameters().getClass(), type.getRVParameters().getClass().getName());
        }
        return new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(adapter).create();
    }

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
        return getAdaptedGson().fromJson(json, type);
    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        Model model = simulation.getModel();
        RandomVariable rv1 = new RandomVariable(model, new BinomialRV(0.8, 10), "rv1");
        RandomVariable rv2 = new RandomVariable(model, new TriangularRV(10.0, 15.0, 25.0), "rv2");
        RandomVariable rv3 = new RandomVariable(model, new NormalRV(10.0, 4.0), "rv3");
        RVariableIfc de = new DEmpiricalRV(new double[]{1.0, 2.0, 3.0}, new double[]{0.35, 0.80, 1.0});
        RandomVariable rv4 = new RandomVariable(model, de, "rv4");

        RVParameterSetter setter = new RVParameterSetter();
        setter.extractParameters(model);
        System.out.println(setter.toJSON());
        RVParameters parameters1 = setter.getRVParameters("rv1");
        parameters1.changeDoubleParameter("probOfSuccess", 0.66);
        RVParameters parameters2 = setter.getRVParameters("rv4");
        parameters2.changeDoubleArrayParameter("values", new double[]{5.0, 6.0, 8.0});
        System.out.println();
        System.out.println(setter.toJSON());
        int numChanged = setter.applyParameterChanges(model);
        System.out.println();
        System.out.println("number of parameter changes = " + numChanged);
        RVParameterSetter setter2 = new RVParameterSetter();
        setter2.extractParameters(model);
        System.out.println();
        String json = setter2.toJSON();
        System.out.println(json);

        RVParameterSetter setter3 = RVParameterSetter.fromJSON(json);
        System.out.println();
        System.out.println("From JSON string");
        System.out.println(setter3.toJSON());
    }
}
