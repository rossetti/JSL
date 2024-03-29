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
 * Pearson Type 6(alpha1, alpha2, beta) random variable
 */
public final class PearsonType6RV extends ParameterizedRV {

    private final double myAlpha1;
    private final double myAlpha2;
    private final double myBeta;

    public PearsonType6RV(double alpha1, double alpha2, double beta) {
        this(alpha1, alpha2, beta, JSLRandom.nextRNStream());
    }

    public PearsonType6RV(double alpha1, double alpha2, double beta, int streamNum) {
        this(alpha1, alpha2, beta, JSLRandom.rnStream(streamNum));
    }

    public PearsonType6RV(double alpha1, double alpha2, double beta, RNStreamIfc rng) {
        super(rng);
        if (alpha1 <= 0.0) {
            throw new IllegalArgumentException("The 1st shape parameter must be > 0.0");
        }
        if (alpha2 <= 0.0) {
            throw new IllegalArgumentException("The 2nd shape parameter must be > 0.0");
        }
        if (beta <= 0.0) {
            throw new IllegalArgumentException("The scale parameter must be > 0.0");
        }
        myAlpha1 = alpha1;
        myAlpha2 = alpha2;
        myBeta = beta;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public PearsonType6RV newInstance(RNStreamIfc rng) {
        return new PearsonType6RV(getAlpha1(), getAlpha2(), myBeta, rng);
    }

    @Override
    public String toString() {
        return "PearsonType6RV{" +
                "alpha1=" + myAlpha1 +
                ", alpha2=" + myAlpha2 +
                ", beta=" + myBeta +
                '}';
    }

    public double getAlpha1() {
        return myAlpha1;
    }

    public double getAlpha2() {
        return myAlpha2;
    }

    public double getBeta() {
        return myBeta;
    }

    @Override
    protected double generate() {
        double v = JSLRandom.rPearsonType6(myAlpha1, myAlpha2, myBeta, myRNStream);
        return v;
    }

    /**
     * The parameter names are "alpha1", "alpha2", and "beta"
     *
     * @return the parameters for PearsonType6 random variables
     */
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.PearsonType6RVParameters();
        parameters.changeDoubleParameter("alpha1", myAlpha1);
        parameters.changeDoubleParameter("alpha2", myAlpha2);
        parameters.changeDoubleParameter("beta", myBeta);
        return parameters;
    }
}
