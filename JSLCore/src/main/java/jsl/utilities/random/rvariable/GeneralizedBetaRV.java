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
 * GeneralizeBetaRV(alpha1, alpha2, min, max) random variable
 */
public final class GeneralizedBetaRV extends ParameterizedRV {

    private final double myMin;

    private final double myMax;

    private final BetaRV myBeta;

    public GeneralizedBetaRV(double alpha1, double alpha2, double min, double max) {
        this(alpha1, alpha2, min, max, JSLRandom.nextRNStream());
    }

    public GeneralizedBetaRV(double alpha1, double alpha2, double min, double max, int streamNum) {
        this(alpha1, alpha2, min, max, JSLRandom.rnStream(streamNum));
    }

    public GeneralizedBetaRV(double alpha1, double alpha2, double min, double max, RNStreamIfc rng) {
        super(rng);
        if (max <= min) {
            throw new IllegalArgumentException("the min must be < than the max");
        }
        myBeta = new BetaRV(alpha1, alpha2, rng);
        myMin = min;
        myMax = max;
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public GeneralizedBetaRV newInstance(RNStreamIfc rng) {
        return new GeneralizedBetaRV(getAlpha1(), getAlpha2(), myMin, myMax, rng);
    }

    @Override
    public String toString() {
        return "GeneralizedBetaRV{" +
                "alpha1=" + myBeta.getAlpha1() +
                ", alpha2=" + myBeta.getAlpha2() +
                ", min=" + myMin +
                ", max=" + myMax +
                '}';
    }

    /**
     * Gets the lower limit
     *
     * @return The lower limit
     */
    public double getMinimum() {
        return (myMin);
    }

    /**
     * Gets the upper limit
     *
     * @return The upper limit
     */
    public double getMaximum() {
        return (myMax);
    }

    /**
     * @return the first shape parameter
     */
    public double getAlpha1() {
        return myBeta.getAlpha1();
    }

    /**
     * @return the second shape parameter
     */
    public double getAlpha2() {
        return myBeta.getAlpha2();
    }

    @Override
    protected double generate() {
        double x = myBeta.getValue();
        return myMin + (myMax - myMin) * x;
    }

    /**
     * The parameter names are "alpha1", "alpha2"  "min", and "max"
     *
     * @return a control for GeneralizeBeta random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.GeneralizedBetaRVParameters();
        parameters.changeDoubleParameter("alpha1", myBeta.getAlpha1());
        parameters.changeDoubleParameter("alpha2", myBeta.getAlpha2());
        parameters.changeDoubleParameter("min", myMin);
        parameters.changeDoubleParameter("max", myMax);
        return parameters;
    }

}
