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
 * Normal(mean, variance)
 */
public final class LaplaceRV extends AbstractRVariable {

    private final double myMean;

    private final double myScale;

    public LaplaceRV(double mean, double scale) {
        this(mean, scale, JSLRandom.nextRNStream());
    }

    public LaplaceRV(double mean, double scale, int streamNum) {
        this(mean, scale, JSLRandom.rnStream(streamNum));
    }

    public LaplaceRV(double mean, double scale, RNStreamIfc rng) {
        super(rng);
        myMean = mean;
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be positive");
        }
        myScale = scale;
    }

    /**
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public LaplaceRV newInstance(RNStreamIfc rng) {
        return new LaplaceRV(this.myMean, this.myScale, rng);
    }

    @Override
    public String toString() {
        return "LaplaceRV{" +
                "mean=" + myMean +
                ", scale=" + myScale +
                '}';
    }

    /**
     * @return mean of the random variable
     */
    public double getMean() {
        return myMean;
    }

    /**
     * @return the scale parameter
     */
    public double getScale() {
        return myScale;
    }

    @Override
    protected double generate() {
        double v = JSLRandom.rLaplace(myMean, myScale, myRNStream);
        return v;
    }

    /**
     * The keys are "mean" with default value 0.0 and "scale" with
     * default value 1.0
     *
     * @return a control for Laplace random variables
     */
    public static RVControls makeControls() {
        return new RVControls() {
            @Override
            protected final void fillControls() {
                addDoubleControl("mean", 0.0);
                addDoubleControl("scale", 1.0);
                setName(RVType.Laplace.name());
                setRVType(RVType.Laplace);
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double scale = getDoubleControl("scale");
                double mean = getDoubleControl("mean");
                return new LaplaceRV(mean, scale, rnStream);
            }
        };
    }
}
