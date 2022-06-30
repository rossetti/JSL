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
import jsl.utilities.random.robj.DPopulation;

/**
 * A random variable that samples from the provided data
 */
public final class EmpiricalRV extends ParameterizedRV {

    private final DPopulation myPop;

    public EmpiricalRV(double[] data) {
        this(data, JSLRandom.nextRNStream());
    }

    public EmpiricalRV(double[] data, int streamNum) {
        this(data, JSLRandom.rnStream(streamNum));
    }

    public EmpiricalRV(double[] data, RNStreamIfc rng) {
        super(rng);
        if (rng == null) {
            throw new IllegalArgumentException("The supplied RngIfc was null");
        }
        if (data == null) {
            throw new IllegalArgumentException("The supplied data array was null");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("The supplied data array had no elements.");
        }
        myPop = new DPopulation(data);
    }

    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new EmpiricalRV(myPop.getParameters(), myRNStream);
    }

    @Override
    protected double generate() {
        return myPop.getValue();
    }

    /**
     * The parameter name is "Population"
     *
     * @return parameters for Bernoulli random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.EmpiricalRVParameters();
        parameters.changeDoubleArrayParameter("population", myPop.getParameters());
        return parameters;
    }
}
