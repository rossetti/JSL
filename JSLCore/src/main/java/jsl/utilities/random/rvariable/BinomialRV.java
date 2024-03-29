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
 * BinomialRV(probability of success, number of trials)
 */
public final class BinomialRV extends ParameterizedRV {

    private final double myProbSuccess;

    private final int myNumTrials;

    /**
     * @param probOfSuccess  the probability of success, must be in (0,1)
     * @param numTrials the number of trials, must be greater than 0
     */
    public BinomialRV(double probOfSuccess, int numTrials) {
        this(probOfSuccess, numTrials, JSLRandom.nextRNStream());
    }

    /**
     * @param probOfSuccess  the probability of success, must be in (0,1)
     * @param numTrials the number of trials, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     */
    public BinomialRV(double probOfSuccess, int numTrials, int streamNum) {
        this(probOfSuccess, numTrials, JSLRandom.rnStream(streamNum));
    }

    /**
     * @param probOfSuccess  the probability of success, must be in (0,1)
     * @param numTrials the number of trials, must be greater than 0
     * @param stream    the stream from the stream provider to use
     */
    public BinomialRV(double probOfSuccess, int numTrials, RNStreamIfc stream) {
        super(stream);
        if ((probOfSuccess < 0.0) || (probOfSuccess > 1.0)) {
            throw new IllegalArgumentException("Success Probability must be [0,1]");
        }
        if (numTrials <= 0) {
            throw new IllegalArgumentException("Number of trials must be >= 1");
        }
        myProbSuccess = probOfSuccess;
        myNumTrials = numTrials;
    }

    /**
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public BinomialRV newInstance(RNStreamIfc rng) {
        return new BinomialRV(this.myProbSuccess, this.myNumTrials, rng);
    }

    @Override
    public String toString() {
        return "BinomialRV{" +
                "probOfSuccess=" + myProbSuccess +
                ", numTrials=" + myNumTrials +
                '}';
    }

    /**
     * Gets the success probability
     *
     * @return The success probability
     */
    public double getProbOfSuccess() {
        return (myProbSuccess);
    }

    /**
     * Gets the number of trials
     *
     * @return the number of trials
     */
    public int getNumTrials() {
        return (myNumTrials);
    }

    @Override
    protected double generate() {
        return JSLRandom.rBinomial(myProbSuccess, myNumTrials, myRNStream);
    }

    /**
     * The keys are "probOfSuccess",  numTrials is an Integer and
     * ProbOfSuccess is a Double.
     *
     * @return a parameters for Binomial random variables
     */
    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new RVParameters.BinomialRVParameters();
        parameters.changeDoubleParameter("probOfSuccess", myProbSuccess);
        parameters.changeIntegerParameter("numTrials", myNumTrials);
        return parameters;
    }

}
