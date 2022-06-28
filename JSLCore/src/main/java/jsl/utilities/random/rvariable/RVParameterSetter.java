package jsl.utilities.random.rvariable;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.Model;
import jsl.utilities.random.RandomIfc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class RVParameterSetter {

    private final Model model;

    private final LinkedHashMap<String, RVControls> rvControls;

    public RVParameterSetter(Model model) {
        Objects.requireNonNull(model, "The supplied model was null");
        this.model = model;
        rvControls = new LinkedHashMap<>();
        List<RandomVariable> rvList = model.getRandomVariables();
        for(RandomVariable rv: rvList){
            RandomIfc r = rv.getRandomSource();
            if (r instanceof AbstractRVariable){

            }
        }
    }
}
