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
 * NegativeBinomial(probability of success, number of trials until rth success)
 */
public final class NegativeBinomialRV extends ParameterizedRV {

    private final double myProbSuccess;

    private final double myNumSuccesses;

    /**
     * @param prob       the probability of success, must be in (0,1)
     * @param numSuccess number of trials until rth success
     */
    public NegativeBinomialRV(double prob, double numSuccess) {
        this(prob, numSuccess, JSLRandom.nextRNStream());
    }

    /**
     * @param prob       the probability of success, must be in (0,1)
     * @param numSuccess number of trials until rth success
     * @param streamNum  the stream number from the stream provider to use
     */
    public NegativeBinomialRV(double prob, double numSuccess, int streamNum) {
        this(prob, numSuccess, JSLRandom.rnStream(streamNum));
    }

    /**
     * @param prob       the probability of success, must be in (0,1)
     * @param numSuccess number of trials until rth success
     * @param stream     the stream from the stream provider to use
     */
    public NegativeBinomialRV(double prob, double numSuccess, RNStreamIfc stream) {
        super(stream);
        if ((prob <= 0.0) || (prob >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be (0,1)");
        }
        if (numSuccess <= 0) {
            throw new IllegalArgumentException("Number of trials until rth success must be > 0");
        }
        myProbSuccess = prob;
        myNumSuccesses = numSuccess;
    }

    /**
     * @param stream the random number stream to use
     * @return a new instance with same parameter value
     */
    public NegativeBinomialRV newInstance(RNStreamIfc stream) {
        return new NegativeBinomialRV(this.myProbSuccess, this.myNumSuccesses, stream);
    }

    @Override
    public String toString() {
        return "NegativeBinomialRV{" +
                "probSuccess=" + myProbSuccess +
                ", numSuccesses=" + myNumSuccesses +
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

    /**
     * Gets the desired number of successes
     *
     * @return the number of success
     */
    public double getDesiredNumberOfSuccesses() {
        return (myNumSuccesses);
    }


    @Override
    protected double generate() {
        return JSLRandom.rNegBinomial(myProbSuccess, myNumSuccesses, myRNStream);
    }

    /**
     * The parameter names are "probOfSuccess" and "numSuccesses"
     *
     * @return a control for Negative Binomial random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.NegativeBinomialRVParameters();
        parameters.changeDoubleParameter("probOfSuccess", myProbSuccess);
        parameters.changeIntegerParameter("numSuccesses", (int)myNumSuccesses);
        return parameters;
    }
}
