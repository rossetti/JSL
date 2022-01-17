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

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rng.RNStreamIfc;

/**
 * Allows for the generation of bi-variate normal
 * random variables
 *
 * @author rossetti
 */
public class BivariateNormalRV extends AbstractMVRVariable {

    private final double myMu1;

    private final double myVar1;

    private final double myMu2;

    private final double myVar2;

    private final double myRho;

    /**
     * Constructs a standard bi-variate normal with no correlation
     */
    public BivariateNormalRV() {
        this(0.0, 1.0, 0.0, 1.0, 0.0, JSLRandom.nextRNStream());
    }

    /**
     * Constructs a standard bi-variate normal with no correlation
     */
    public BivariateNormalRV(RNStreamIfc rng) {
        this(0.0, 1.0, 0.0, 1.0, 0.0, rng);
    }

    /**
     * @param mean1 mean of first coordinate
     * @param var1  variance of first coordinate
     * @param mean2 mean of 2nd coordinate
     * @param var2  variance of 2nd coordinate
     * @param rho   correlation between X1 and X2
     */
    public BivariateNormalRV(double mean1, double var1, double mean2, double var2, double rho) {
        this(mean1, var1, mean2, var2, rho, JSLRandom.nextRNStream());
    }

    /**
     * Constructs a bi-variate normal with the provided parameters
     *
     * @param mean1     mean of first coordinate
     * @param var1      variance of first coordinate
     * @param mean2     mean of 2nd coordinate
     * @param var2      variance of 2nd coordinate
     * @param rho       correlation between X1 and X2
     * @param streamNum the stream number
     */
    public BivariateNormalRV(double mean1, double var1, double mean2, double var2, double rho, int streamNum) {
        this(mean1, var1, mean2, var2, rho, JSLRandom.rnStream(streamNum));
    }

    /**
     * Constructs a bi-variate normal with the provided parameters
     *
     * @param mean1 mean of first coordinate
     * @param var1  variance of first coordinate
     * @param mean2 mean of 2nd coordinate
     * @param var2  variance of 2nd coordinate
     * @param rho   correlation between X1 and X2
     * @param rng   the RNStreamIfc
     */
    public BivariateNormalRV(double mean1, double var1, double mean2, double var2, double rho, RNStreamIfc rng) {
        super(rng);
        if (var1 <= 0) {
            throw new IllegalArgumentException("The first variance was <=0");
        }
        if (var2 <= 0) {
            throw new IllegalArgumentException("The second variance was <=0");
        }
        if ((rho < -1.0) || (rho > 1.0)) {
            throw new IllegalArgumentException("The correlation must be [-1,1]");
        }
        myMu1 = mean1;
        myMu2 = mean2;
        myVar1 = var1;
        myVar2 = var2;
        myRho = rho;
    }

    /**
     * Gets the first mean
     *
     * @return the first mean
     */
    public final double getMean1() {
        return myMu1;
    }

    /**
     * Gets the first variance
     *
     * @return the first variance
     */
    public final double getVariance1() {
        return myVar1;
    }

    /**
     * Gets the second mean
     *
     * @return the second mean
     */
    public final double getMean2() {
        return myMu2;
    }

    /**
     * Gets the 2nd variance
     *
     * @return the 2nd variance
     */
    public final double getVariance2() {
        return myVar2;
    }

    /**
     * Gets the correlation
     *
     * @return the correlation
     */
    public final double getCorrelation() {
        return myRho;
    }

    @Override
    public final double[] sample() {
        double[] x = new double[2];
        double z0 = Normal.stdNormalInvCDF(myRNG.randU01());
        double z1 = Normal.stdNormalInvCDF(myRNG.randU01());
        double s1 = Math.sqrt(myVar1);
        double s2 = Math.sqrt(myVar2);
        x[0] = myMu1 + s1 * z0;
        x[1] = myMu2 + s2 * (myRho * z0 + Math.sqrt(1.0 - myRho * myRho) * z1);
        return x;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BivariateNormalRV{");
        sb.append("mu1=").append(myMu1);
        sb.append(", var1=").append(myVar1);
        sb.append(", mu2=").append(myMu2);
        sb.append(", var2=").append(myVar2);
        sb.append(", rho=").append(myRho);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public final MVRVariableIfc newInstance(RNStreamIfc rng) {
        return new BivariateNormalRV(myMu1, myVar1, myMu2, myVar2, myRho, rng);
    }
}
