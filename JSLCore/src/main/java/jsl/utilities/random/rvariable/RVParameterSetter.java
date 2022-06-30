package jsl.utilities.random.rvariable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.utilities.misc.RuntimeTypeAdapterFactory;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.reporting.JSL;

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
     *
     * @param model the model to process
     * @return the parameters in a map for each parameterized random variable in the model
     */
    public Map<String, RVParameters> extractParameters(Model model){
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

    /** The returned map cannot be modified, but the values can be retrieved and changed
     *  as needed. Changing the values have no effect within the model until they are applied.
     *
     * @return parameters for every parameterized random variable within the model
     */
    public Map<String, RVParameters> getAllRVParameters(){
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
     *
     * @return the list of names for the random variables that are parameterized
     */
    public List<String> getRandomVariableNames(){
        return new ArrayList<String>(rvParameters.keySet());
    }

    /**
     *
     * @return the number of parameterized random variables that can be changed
     */
    public int getNumberOfParameterizedRandomVariables(){
        return rvParameters.size();
    }

    /**
     *
     * @return the number of parameterized random variables that had their parameters changed in some way
     */
    public int applyParameterChanges(Model model){
        Objects.requireNonNull(model, "The supplied model was null");
        if (!modelName.equals(model.getName())){
            throw new IllegalArgumentException("Cannot apply parameters from model, " + model.getName() +
                    ", to model " + modelName);
        }
        if (modelId != model.getId()){
            throw new IllegalArgumentException("Cannot apply parameters from model id =, " + model.getId()+
                    ", to model with id = " + modelId);
        }
        if (model.isRunning()){
            JSL.getInstance().LOGGER.warn("The model was running when attempting to apply parameter changes");
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
                if(!toBe.equals(current)){
                    // change has occurred
                    countChanged++;
                    rv.setInitialRandomSource(toBe.createRVariable());
                }
            }
        }
        return countChanged;
    }

    public String toJSON(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    //TODO does not work because of sub-classes for RVParameters cannot be deserialized
    // https://stackoverflow.com/questions/16000163/using-gson-and-abstract-classes

//    /**
//     *
//     * @param json a json string representing a {@literal RVParameterSetter}
//     * @return the created RVParameterSetter
//     */
//    public static RVParameterSetter fromJSON(String json){
//        Objects.requireNonNull(json, "The supplied json string was null");
//        RuntimeTypeAdapterFactory<RVParameters> adapter =
//                RuntimeTypeAdapterFactory.of(RVParameters.class);
//        for (RVType type : RVType.RVTYPE_SET){
//            System.out.println(type.getRVParameters().getClass());
//            adapter.registerSubtype(type.getRVParameters().getClass());
//        }
//        Gson gson=new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(adapter).create();
//        Type type = new TypeToken<RVParameterSetter>(){}.getType();
//        return gson.fromJson(json, type);
//    }

    public static void main(String[] args) {
        Simulation simulation = new Simulation();
        Model model = simulation.getModel();
        RandomVariable rv1 = new RandomVariable(model, new BinomialRV(0.8, 10), "rv1");
        RandomVariable rv2 = new RandomVariable(model, new TriangularRV(10.0, 15.0, 25.0), "rv2");
        RandomVariable rv3 = new RandomVariable(model, new NormalRV(10.0, 4.0), "rv3");
        RVariableIfc de = new DEmpiricalRV(new double[] {1.0, 2.0, 3.0}, new double[] {0.35, 0.80, 1.0});
        RandomVariable rv4 = new RandomVariable(model, de, "rv4");

        RVParameterSetter setter = new RVParameterSetter();
        setter.extractParameters(model);
        System.out.println(setter.toJSON());
        RVParameters parameters1 = setter.getRVParameters("rv1");
        parameters1.changeDoubleParameter("probOfSuccess", 0.66);
        RVParameters parameters2 = setter.getRVParameters("rv4");
        parameters2.changeDoubleArrayParameter("values", new double[] {5.0, 6.0, 8.0});
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

//        RVParameterSetter setter3 = RVParameterSetter.fromJSON(json);
//        System.out.println();
//        System.out.println("From JSON string");
//        System.out.println(setter3.toJSON());
    }
}
