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
 * JohnsonB(alpha1, alpha2, min, max) random variable
 */
public final class JohnsonBRV extends ParameterizedRV {

    private final double myAlpha1;

    private final double myAlpha2;

    private final double myMin;

    private final double myMax;

    public JohnsonBRV(double alpha1, double alpha2, double min, double max) {
        this(alpha1, alpha2, min, max, JSLRandom.nextRNStream());
    }

    public JohnsonBRV(double alpha1, double alpha2, double min, double max, int streamNum) {
        this(alpha1, alpha2, min, max, JSLRandom.rnStream(streamNum));
    }

    public JohnsonBRV(double alpha1, double alpha2, double min, double max, RNStreamIfc rng) {
        super(rng);
        if (alpha2 <= 0) {
            throw new IllegalArgumentException("alpha2 must be > 0");
        }

        if (max <= min) {
            throw new IllegalArgumentException("the min must be < than the max");
        }
        myAlpha1 = alpha1;
        myAlpha2 = alpha2;
        myMin = min;
        myMax = max;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public JohnsonBRV newInstance(RNStreamIfc rng) {
        return new JohnsonBRV(getAlpha1(), getAlpha2(), myMin, myMax, rng);
    }

    @Override
    public String toString() {
        return "JohnsonBRV{" +
                "alpha1=" + myAlpha1 +
                ", alpha2=" + myAlpha2 +
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
        return JSLRandom.rJohnsonB(myAlpha1, myAlpha2, myMin, myMax, myRNStream);
    }

    /**
     * The parameter names are "alpha1", "alpha2", "min", and "max"
     *
     * @return a control for JohnsonB random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.JohnsonBRVParameters();
        parameters.changeDoubleParameter("alpha1", myAlpha1);
        parameters.changeDoubleParameter("alpha2", myAlpha2);
        parameters.changeDoubleParameter("min", myMin);
        parameters.changeDoubleParameter("max", myMax);
        return parameters;
    }
}
