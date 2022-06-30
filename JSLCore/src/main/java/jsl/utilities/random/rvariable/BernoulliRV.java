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
 * Bernoulli(probability of success) random variable
 */
public final class BernoulliRV extends ParameterizedRV {

    private final double myProbSuccess;

    /**
     * Uses a new stream from the default provider of streams
     *
     * @param probOfSuccess the probability, must be in (0,1)
     */
    public BernoulliRV(double probOfSuccess) {
        this(probOfSuccess, JSLRandom.nextRNStream());
    }

    /**
     * @param probOfSuccess      the probability, must be in (0,1)
     * @param streamNum the stream number
     */
    public BernoulliRV(double probOfSuccess, int streamNum) {
        this(probOfSuccess, JSLRandom.rnStream(streamNum));
    }

    /**
     * @param probOfSuccess   the probability, must be in (0,1)
     * @param stream the RNStreamIfc to use
     */
    public BernoulliRV(double probOfSuccess, RNStreamIfc stream) {
        super(stream);
        if ((probOfSuccess <= 0.0) || (probOfSuccess >= 1.0)) {
            throw new IllegalArgumentException("Probability must be (0,1)");
        }
        myProbSuccess = probOfSuccess;
    }

    /**
     * @param stream the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public BernoulliRV newInstance(RNStreamIfc stream) {
        return new BernoulliRV(this.myProbSuccess, stream);
    }

    @Override
    public String toString() {
        return "BernoulliRV{" +
                "probOfSuccess=" + myProbSuccess +
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
        return JSLRandom.rBernoulli(myProbSuccess, myRNStream);
    }

    /**
     * Returns a randomly generated boolean according to the Bernoulli distribution
     *
     * @return a randomly generated boolean
     */
    public boolean getBoolean() {
        if (getValue() == 0.0) {
            return (false);
        } else {
            return (true);
        }
    }

    /**
     * Returns a boolean array filled via getBoolean()
     *
     * @param n the generation size, must be at least 1
     * @return the array
     */
    public boolean[] getBooleanSample(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("The generate size must be > 0");
        }
        boolean[] b = new boolean[n];
        for (int i = 0; i < n; i++) {
            b[i] = getBoolean();
        }
        return b;
    }

    /** The parameter names: probOfSuccess
     *
     * @return the parameters
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.BernoulliRVParameters();
        parameters.changeDoubleParameter("probOfSuccess", myProbSuccess);
        return parameters;
    }
}
