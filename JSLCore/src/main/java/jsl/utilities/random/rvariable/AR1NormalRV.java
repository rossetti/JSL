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

public class AR1NormalRV extends AbstractRVariable {

    private final double myPhi;

    private final double myMean;

    private final double myVar;

    private double myX;

    private final NormalRV myErrors;

    /** Creates an autoregressive order 1 normal, N(0,1) zero correlation process
     *
     */
    public AR1NormalRV() {
        this(0.0, 1.0, 0.0, JSLRandom.nextRNStream());
    }

    public AR1NormalRV(RNStreamIfc rng) {
        this(0.0, 1.0, 0.0, rng);
    }

    /** Creates an autoregressive order 1 normal, N(0,1) process
     *
     * @param correlation
     */
    public AR1NormalRV(double correlation) {
        this(0.0, 1.0, correlation, JSLRandom.nextRNStream());
    }

    /** Creates an autoregressive order 1 normal process
     *
     * @param mean
     * @param variance
     * @param correlation
     */
    public AR1NormalRV(double mean, double variance, double correlation) {
        this(mean, variance, correlation, JSLRandom.nextRNStream());
    }

    /** Creates an autoregressive order 1 normal process
     *
     * @param mean
     * @param variance
     * @param correlation
     * @param rng
     */
    public AR1NormalRV(double mean, double variance, double correlation, RNStreamIfc rng) {
        super(rng);
        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        if ((correlation < -1.0) || (correlation > 1.0)) {
            throw new IllegalArgumentException("The correlation must be [-1,1]");
        }
        myMean = mean;
        myVar = variance;
        myPhi = correlation;
        // generate the first value for the process N(mean, variance)
        myX = JSLRandom.rNormal(mean, variance, rng);
        // set the correlation and the error distribution N(0, myVar*(1-myPhi^2)
        double v = myVar * (1.0 - myPhi * myPhi);
        // create the error random variable
        myErrors = new NormalRV(0.0, v, rng);
    }
    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new AR1NormalRV(myMean, myVar, myPhi, rng);
    }

    /**
     *
     * @return The mean of the process
     */
    public final double getMean() {
        return myMean;
    }

    /**
     *
     * @return The variance of the process
     */
    public final double getVariance() {
        return myVar;
    }

    /**
     *
     * @return The variance of the underlying errors
     */
    public final double getErrorVariance() {
        return (myErrors.getVariance());
    }

    /**
     *
     * @return Gets the lag 1 autocorrelation
     */
    public final double getLag1Correlation() {
        return (myPhi);
    }

    @Override
    protected double generate() {
        myX = myMean + myPhi * (myX - myMean) + myErrors.getValue();
        return myX;
    }

}
