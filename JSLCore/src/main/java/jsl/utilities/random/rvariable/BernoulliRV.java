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
 *  Bernoulli(probability of success) random variable
 */
public final class BernoulliRV extends AbstractRVariable {

    private final double myProbSuccess;

    /** Uses a new stream from the default provider of streams
     *
     * @param prob the probability, must be in [0,1]
     */
    public BernoulliRV(double prob){
        this(prob, JSLRandom.nextRNStream());
    }

    /**
     *
     * @param prob the probability, must be in [0,1]
     * @param streamNum the stream number
     */
    public BernoulliRV(double prob, int streamNum){
        this(prob, JSLRandom.rnStream(streamNum));
    }

    /**
     *
     * @param prob the probability, must be in [0,1]
     * @param rng the RNStreamIfc to use
     */
    public BernoulliRV(double prob, RNStreamIfc rng){
        super(rng);
        if ((prob < 0.0) || (prob > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }
        myProbSuccess = prob;
    }

    /**
     *
     * @param rng the RNStreamIfc to use
     * @return a new instance with same parameter value
     */
    public final BernoulliRV newInstance(RNStreamIfc rng){
        return new BernoulliRV(this.myProbSuccess, rng);
    }

    @Override
    public String toString() {
        return "BernoulliRV{" +
                "pSuccess=" + myProbSuccess +
                '}';
    }

    /** Gets the success probability
     * @return The success probability
     */
    public final double getProbabilityOfSuccess() {
        return (myProbSuccess);
    }

    @Override
    protected final double generate(){
        double v = JSLRandom.rBernoulli(myProbSuccess, myRNStream);
        return v;
    }

    /** Returns a randomly generated boolean according to the Bernoulli distribution
     *
     * @return a randomly generated boolean
     */
    public final boolean getBoolean() {
        if (getValue() == 0.0) {
            return (false);
        } else {
            return (true);
        }
    }

    /** Returns a boolean array filled via getBoolean()
     *
     * @param n the generate size, must be at least 1
     * @return the array
     */
    public final boolean[] getBooleanSample(int n){
        if (n <= 0){
            throw new IllegalArgumentException("The generate size must be > 0");
        }
        boolean[] b = new boolean[n];
        for(int i=0;i<n;i++){
            b[i] = getBoolean();
        }
        return b;
    }

    /**
     * The key is "ProbOfSuccess", the default value is 0.5
     *
     * @return a control for Bernoulli random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.Bernoulli) {
            @Override
            protected final void fillControls() {
                addDoubleControl("ProbOfSuccess", 0.5);
                setName(RVariableIfc.RVType.Bernoulli.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double probOfSuccess = getDoubleControl("ProbOfSuccess");
                return new BernoulliRV(probOfSuccess, rnStream);
            }
        };
    }
}
