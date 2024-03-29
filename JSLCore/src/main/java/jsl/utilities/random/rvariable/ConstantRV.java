/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package jsl.utilities.random.rvariable;

import jsl.utilities.random.rng.RNStreamIfc;

/**
 * Allows a constant to pretend to be a random variable
 */
public class ConstantRV extends ParameterizedRV {

    /**
     * A constant to represent zero for sharing
     */
    public final static ConstantRV ZERO = new ConstantRV(0.0);
    /**
     * A constant to represent one for sharing
     */
    public final static ConstantRV ONE = new ConstantRV(1.0);

    /**
     * A constant to represent two for sharing
     */
    public final static ConstantRV TWO = new ConstantRV(2.0);

    /**
     * A constant to represent positive infinity for sharing
     */
    public final static ConstantRV POSITIVE_INFINITY = new ConstantRV(Double.POSITIVE_INFINITY);

    protected double myValue;

    public ConstantRV(double value) {
        super(JSLRandom.getDefaultRNStream());
        myValue = value;
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public final ConstantRV newInstance(RNStreamIfc rng) {
        return new ConstantRV(myValue);
    }

    /**
     * @return a new instance with same parameter value
     */
    public final ConstantRV newInstance() {
        return new ConstantRV(myValue);
    }

    @Override
    public String toString() {
        return "ConstantRV{" +
                "value=" + myValue +
                '}';
    }

    @Override
    protected double generate() {
        return myValue;
    }

    /**
     * The parameter names are "value"
     *
     * @return the parameters for Constant random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.ConstantRVParameters();
        parameters.changeDoubleParameter("value", myValue);
        return parameters;
    }
}
