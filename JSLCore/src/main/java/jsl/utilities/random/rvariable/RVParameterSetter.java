package jsl.utilities.random.rvariable;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.Model;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.reporting.JSL;

import java.util.*;

public class RVParameterSetter {

    private final Model model;

    private final LinkedHashMap<String, RVParameters> rvParameters;
//    private final LinkedHashMap<String, RVParameters> rvCurrentModelParameters;

    public RVParameterSetter(Model model) {
        Objects.requireNonNull(model, "The supplied model was null");
        this.model = model;
        rvParameters = new LinkedHashMap<>();
//        rvCurrentModelParameters = new LinkedHashMap<>();
        List<RandomVariable> rvList = model.getRandomVariables();
        for (RandomVariable rv : rvList) {
            RandomIfc rs = rv.getInitialRandomSource();
            if (rs instanceof ParameterizedRV) {
//                @SuppressWarnings("unchecked")
//                Class<? extends ParameterizedRV> cls = (Class<? extends ParameterizedRV>) rs.getClass();
//                RVType type = RVType.getRVType(cls);
//                RVParameters parameters = type.getRVParameters();
                rvParameters.put(rv.getName(), ((ParameterizedRV) rs).getParameters());
//                rvCurrentModelParameters.put(rv.getName(), ((ParameterizedRV) rs).getParameters());
            }
        }
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
    public int applyParameterChanges(){
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
                    rv.setInitialRandomSource(toBe.makeRVariable());
                }
            }
        }
        return countChanged;
    }
}
