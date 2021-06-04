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
 *  Normal(mean, variance)
 */
public final class NormalRV extends AbstractRVariable {

    private final double myMean;

    private final double myVar;

    private double nextNormal = 0.0;

    private boolean nextNormalFlag = false;

    /**
     *  N(0,1)
     */
    public NormalRV(){
        this(0,1.0);
    }

    public NormalRV(double mean, double variance){
        this(mean, variance, JSLRandom.nextRNStream());
    }

    public NormalRV(double mean, double variance, int streamNum){
        this(mean, variance, JSLRandom.rnStream(streamNum));
    }

    public NormalRV(double mean, double variance, RNStreamIfc rng){
        super(rng);
        myMean = mean;
        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        myVar = variance;
    }

    /**
     *
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public final NormalRV newInstance(RNStreamIfc rng){
        return new NormalRV(this.myMean, this.myVar, rng);
    }

    @Override
    public String toString() {
        return "NormalRV{" +
                "mean=" + myMean +
                ", variance=" + myVar +
                '}';
    }

    /**
     *
     * @return mean of the random variable
     */
    public final double getMean() {
        return myMean;
    }

    /**
     *
     * @return variance of the random variable
     */
    public final double getVariance() {
        return myVar;
    }

    /**
     *
     * @return the standard deviation of the random variable
     */
    public final double getStandardDeviation(){
        return Math.sqrt(getVariance());
    }

    @Override
    protected final double generate() {
        double v = JSLRandom.rNormal(myMean, myVar, myRNStream);
        return v;
    }

    /**
     * The keys are "mean" with default value 0.0 and "variance" with
     * default value 1.0
     *
     * @return a control for Normal random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.Normal) {
            @Override
            protected final void fillControls() {
                addDoubleControl("mean", 0.0);
                addDoubleControl("variance", 1.0);
                setName(RVariableIfc.RVType.Normal.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double mean = getDoubleControl("mean");
                double variance = getDoubleControl("variance");
                return new NormalRV(mean, variance, rnStream);
            }
        };
    }

    /** Gets a random variate from this normal distribution
     *  via the polar method.
     *
     * @return a normally distributed random variate
     */
    public final double polarMethodRandomVariate() {
        if (nextNormalFlag == true) {
            nextNormalFlag = false;
            return (myMean + getStandardDeviation() * nextNormal);
        } else {
            double u1, u2;
            double v1, v2;
            double w, y;
            do {
                u1 = myRNStream.randU01();
                u2 = myRNStream.randU01();
                v1 = 2.0 * u1 - 1.0;
                v2 = 2.0 * u2 - 1.0;
                w = v1 * v1 + v2 * v2;
            } while (w > 1.0);
            y = Math.sqrt((-2.0 * Math.log(w) / w));

            nextNormal = v2 * y;
            nextNormalFlag = true;
            return (myMean + getStandardDeviation() * (v1 * y));
        }
    }

}
