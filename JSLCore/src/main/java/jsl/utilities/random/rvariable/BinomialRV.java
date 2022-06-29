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
     * @param pSuccess  the probability of success, must be in (0,1)
     * @param numTrials the number of trials, must be greater than 0
     */
    public BinomialRV(double pSuccess, int numTrials) {
        this(pSuccess, numTrials, JSLRandom.nextRNStream());
    }

    /**
     * @param pSuccess  the probability of success, must be in (0,1)
     * @param numTrials the number of trials, must be greater than 0
     * @param streamNum the stream number from the stream provider to use
     */
    public BinomialRV(double pSuccess, int numTrials, int streamNum) {
        this(pSuccess, numTrials, JSLRandom.rnStream(streamNum));
    }

    /**
     * @param pSuccess  the probability of success, must be in (0,1)
     * @param numTrials the number of trials, must be greater than 0
     * @param stream    the stream from the stream provider to use
     */
    public BinomialRV(double pSuccess, int numTrials, RNStreamIfc stream) {
        super(stream);
        if ((pSuccess < 0.0) || (pSuccess > 1.0)) {
            throw new IllegalArgumentException("Success Probability must be [0,1]");
        }
        if (numTrials <= 0) {
            throw new IllegalArgumentException("Number of trials must be >= 1");
        }
        myProbSuccess = pSuccess;
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
                "probSuccess=" + myProbSuccess +
                ", numTrials=" + myNumTrials +
                '}';
    }

    /**
     * Gets the success probability
     *
     * @return The success probability
     */
    public double getProb() {
        return (myProbSuccess);
    }

    /**
     * Gets the number of trials
     *
     * @return the number of trials
     */
    public int getTrials() {
        return (myNumTrials);
    }

    @Override
    protected double generate() {
        return JSLRandom.rBinomial(myProbSuccess, myNumTrials, myRNStream);
    }

    @Override
    public RVParameters getParameters() {
        RVParameters parameters = new BinomialRVParameters();
        parameters.changeDoubleParameter("ProbOfSuccess", myProbSuccess);
        parameters.changeIntegerParameter("NumTrials", myNumTrials);
        return parameters;
    }

    /**
     * The keys are "ProbOfSuccess", the default value is 0.5 and
     * "NumTrials" with default value 2.  NumTrials is an Integer control and
     * ProbOfSuccess is a Double control.
     *
     * @return a control for Binomial random variables
     */
    public static RVParameters createParameters() {
        return new BinomialRVParameters();
    }

    private static class BinomialRVParameters extends RVParameters {
        @Override
        protected final void fillParameters() {
            addDoubleParameter("ProbOfSuccess", 0.5);
            addIntegerParameter("NumTrials", 2);
            setClassName(RVType.Binomial.asClass().getName());
            setRVType(RVType.Binomial);
        }

        @Override
        public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
            double probOfSuccess = getDoubleParameter("ProbOfSuccess");
            int numTrials = getIntegerParameter("NumTrials");
            return new BinomialRV(probOfSuccess, numTrials, rnStream);
        }
    }
}
