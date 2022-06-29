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
 * Shifted Geometric(probability of success) random variable, range 1, 2, 3, etc.
 */
public final class ShiftedGeometricRV extends AbstractRVariable {

    private final double myProbSuccess;

    /**
     * @param prob probability of success, must be in range (0,1)
     */
    public ShiftedGeometricRV(double prob) {
        this(prob, JSLRandom.nextRNStream());
    }

    /**
     * @param prob      probability of success, must be in range (0,1)
     * @param streamNum the stream number to use
     */
    public ShiftedGeometricRV(double prob, int streamNum) {
        this(prob, JSLRandom.rnStream(streamNum));
    }

    /**
     * @param prob   probability of success, must be in range (0,1)
     * @param stream the random number stream to use
     */
    public ShiftedGeometricRV(double prob, RNStreamIfc stream) {
        super(stream);
        if ((prob <= 0.0) || (prob >= 1.0)) {
            throw new IllegalArgumentException("Probability must be (0,1)");
        }
        myProbSuccess = prob;
    }

    /**
     * @param stream the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public ShiftedGeometricRV newInstance(RNStreamIfc stream) {
        return new ShiftedGeometricRV(this.myProbSuccess, stream);
    }

    @Override
    public String toString() {
        return "ShiftedGeometricRV{" +
                "probSuccess=" + myProbSuccess +
                '}';
    }

    /**
     * Gets the success probability
     *
     * @return The success probability
     */
    public double getProbabilityOfSuccess() {
        return (myProbSuccess);
    }

    @Override
    protected double generate() {
        return 1.0 + JSLRandom.rGeometric(myProbSuccess, myRNStream);
    }

    /**
     * The key is "ProbOfSuccess", the default value is 0.5
     *
     * @return a control for ShiftedGeometric random variables
     */
    public static RVParameters makeControls() {
        return new ShiftedGeometricRVParameters();
    }

    private static class ShiftedGeometricRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("ProbOfSuccess", 0.5);
            setName(RVType.ShiftedGeometric.name());
            setRVType(RVType.ShiftedGeometric);
        }

        public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
            double probOfSuccess = getDoubleParameter("ProbOfSuccess");
            return new ShiftedGeometricRV(probOfSuccess, rnStream);
        }
    }
}
