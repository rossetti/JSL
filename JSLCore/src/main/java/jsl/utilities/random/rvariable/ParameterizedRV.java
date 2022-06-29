package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

abstract public class ParameterizedRV extends RVariable implements RVParametersIfc {

    /**
     * @param stream the source of the randomness
     * @throws NullPointerException if rng is null
     */
    public ParameterizedRV(RNStreamIfc stream) {
        super(stream);
    }
}
