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
 * Lognormal(mean, variance). The mean and variance are for the lognormal random variables
 */
public final class LognormalRV extends ParameterizedRV {

    private final double myMean;

    private final double myVar;

    public LognormalRV(double mean, double variance) {
        this(mean, variance, JSLRandom.nextRNStream());
    }

    public LognormalRV(double mean, double variance, int streamNum) {
        this(mean, variance, JSLRandom.rnStream(streamNum));
    }

    public LognormalRV(double mean, double variance, RNStreamIfc rng) {
        super(rng);
        if (mean <= 0) {
            throw new IllegalArgumentException("Mean must be positive");
        }
        myMean = mean;

        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        myVar = variance;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public LognormalRV newInstance(RNStreamIfc rng) {
        return new LognormalRV(this.myMean, this.myVar, rng);
    }

    @Override
    public String toString() {
        return "LognormalRV{" +
                "mean=" + myMean +
                ", variance=" + myVar +
                '}';
    }

    /**
     * @return mean of the random variable
     */
    public double getMean() {
        return myMean;
    }

    /**
     * @return variance of the random variable
     */
    public double getVariance() {
        return myVar;
    }

    @Override
    protected double generate() {
        return JSLRandom.rLogNormal(myMean, myVar, myRNStream);
    }

    /**
     * The parameter names are "mean" and "variance"
     *
     * @return parameters for Lognormal random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.LognormalRVParameters();
        parameters.changeDoubleParameter("mean", myMean);
        parameters.changeDoubleParameter("variance", myVar);
        return parameters;
    }
}
