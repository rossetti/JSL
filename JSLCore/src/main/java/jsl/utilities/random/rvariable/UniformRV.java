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
 * Continuous uniform(min, max) random variable
 */
public final class UniformRV extends ParameterizedRV {

    private final double min;
    private final double max;

    public UniformRV() {
        this(0.0, 1.0);
    }

    public UniformRV(double min, double max) {
        this(min, max, JSLRandom.nextRNStream());
    }

    public UniformRV(double min, double max, int streamNum) {
        this(min, max, JSLRandom.rnStream(streamNum));
    }

    public UniformRV(double min, double max, RNStreamIfc rng) {
        super(rng);
        if (min >= max) {
            throw new IllegalArgumentException("Lower limit must be < upper limit. lower limit = " + min + " upper limit = " + max);
        }
        this.min = min;
        this.max = max;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public UniformRV newInstance(RNStreamIfc rng) {
        return new UniformRV(this.min, this.max, rng);
    }

    @Override
    public String toString() {
        return "UniformRV{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }

    /**
     * Gets the lower limit
     *
     * @return The lower limit
     */
    public double getMinimum() {
        return (min);
    }

    /**
     * Gets the upper limit
     *
     * @return The upper limit
     */
    public double getMaximum() {
        return (max);
    }

    @Override
    protected double generate() {
        return JSLRandom.rUniform(min, max, myRNStream);
    }

    /**
     * The parameter names are "min" and "max"
     *
     * @return the parameters for Uniform random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.UniformRVParameters();
        parameters.changeDoubleParameter("min", min);
        parameters.changeDoubleParameter("max", max);
        return parameters;
    }

}
