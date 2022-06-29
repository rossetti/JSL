package jsl.utilities.random.rvariable;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.Model;
import jsl.utilities.random.RandomIfc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class RVParameterSetter {

    private final Model model;

    private final LinkedHashMap<String, RVParameters> rvControls;

    public RVParameterSetter(Model model) {
        Objects.requireNonNull(model, "The supplied model was null");
        this.model = model;
        rvControls = new LinkedHashMap<>();
        List<RandomVariable> rvList = model.getRandomVariables();
        for (RandomVariable rv : rvList) {
            RandomIfc rs = rv.getRandomSource();
            if (rs instanceof AbstractRVariable) {
                @SuppressWarnings("unchecked")
                Class<? extends AbstractRVariable> cls = (Class<? extends AbstractRVariable>) rs.getClass();
                RVType type = RVType.getRVType(cls);
                RVParameters parameters = type.getRVParameters();
                rvControls.put(rv.getName(), parameters);
            }
        }
    }

    /**
     * @param rvName the name of the random variable, must not be null and must be in the model
     * @return the controls associated with the named random variable
     */
    public RVParameters getRVControls(String rvName) {
        Objects.requireNonNull(rvName, "The name of the random variable cannot be null");
        if (!rvControls.containsKey(rvName)) {
            throw new IllegalArgumentException("The supplied name is not a valid random variable name");
        }
        return rvControls.get(rvName);
    }
}
