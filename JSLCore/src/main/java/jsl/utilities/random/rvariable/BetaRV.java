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

import jsl.utilities.distributions.Beta;
import jsl.utilities.random.rng.RNStreamIfc;

/**
 * Beta(alpha1, alpha2) random variable, range (0,1)
 */
public final class BetaRV extends ParameterizedRV {

    private final double myAlpha1;

    private final double myAlpha2;

    private final double mylnBetaA1A2;

    public BetaRV(double alpha1, double alpha2) {
        this(alpha1, alpha2, JSLRandom.nextRNStream());
    }

    public BetaRV(double alpha1, double alpha2, int streamNum) {
        this(alpha1, alpha2, JSLRandom.rnStream(streamNum));
    }

    public BetaRV(double alpha1, double alpha2, RNStreamIfc rng) {
        super(rng);
        if (alpha1 <= 0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0");
        }
        myAlpha1 = alpha1;
        if (alpha2 <= 0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0");
        }
        myAlpha2 = alpha2;
        mylnBetaA1A2 = Beta.logBetaFunction(myAlpha1, myAlpha2);
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public BetaRV newInstance(RNStreamIfc rng) {
        return new BetaRV(getAlpha1(), getAlpha2(), rng);
    }

    @Override
    public String toString() {
        return "BetaRV{" +
                "alpha1=" + getAlpha1() +
                ", alpha2=" + getAlpha2() +
                '}';
    }

    /**
     * @return the first shape parameter
     */
    public double getAlpha1() {
        return myAlpha1;
    }

    /**
     * @return the second shape parameter
     */
    public double getAlpha2() {
        return myAlpha2;
    }

    @Override
    protected double generate() {
        return Beta.stdBetaInvCDF(myRNStream.randU01(), myAlpha1, myAlpha2, mylnBetaA1A2);
    }

    /**
     * The keys are "alpha1", the default value is 1.0 and
     * "alpha2" with default value 1.0.
     *
     * @return a parameters for Beta random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.BetaRVParameters();
        parameters.changeDoubleParameter("alpha1", myAlpha1);
        parameters.changeDoubleParameter("alpha2", myAlpha2);
        return parameters;
    }

}
