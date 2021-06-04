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
 *  NegativeBinomial(probability of success, number of successes)
 */
public final class NegativeBinomialRV extends AbstractRVariable {

    private double myProbSuccess;

    private double myNumSuccesses;

    public NegativeBinomialRV(double prob, double numSuccess){
        this(prob, numSuccess, JSLRandom.nextRNStream());
    }

    public NegativeBinomialRV(double prob, double numSuccess, int streamNum){
        this(prob, numSuccess, JSLRandom.rnStream(streamNum));
    }

    public NegativeBinomialRV(double prob, double numSuccess, RNStreamIfc rng){
        super(rng);
        if ((prob < 0.0) || (prob > 1.0)) {
            throw new IllegalArgumentException("Success Probability must be [0,1]");
        }
        if (numSuccess <= 0) {
            throw new IllegalArgumentException("Number of successes must be > 0");
        }
        myProbSuccess = prob;
        myNumSuccesses = numSuccess;
    }

    /**
     *
     * @param rng the RngIfc to use
     * @return a new instance with same parameter value
     */
    public final NegativeBinomialRV newInstance(RNStreamIfc rng){
        return new NegativeBinomialRV(this.myProbSuccess, this.myNumSuccesses, rng);
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
    public final double getProbabilityOfSuccess() {
        return (myProbSuccess);
    }

    /**
     * Gets the desired number of successes
     *
     * @return the number of success
     */
    public final double getDesiredNumberOfSuccesses() {
        return (myNumSuccesses);
    }


    @Override
    protected final double generate() {
        double v = JSLRandom.rNegBinomial(myProbSuccess, myNumSuccesses, myRNStream);
        return v;
    }

    /**
     * The keys are "ProbOfSuccess", the default value is 0.5 and
     * "NumSuccesses" with default value 1.
     *
     * @return a control for Negative Binomial random variables
     */
    public static RVControls makeControls() {
        return new RVControls(RVariableIfc.RVType.NegativeBinomial) {
            @Override
            protected final void fillControls() {
                addDoubleControl("ProbOfSuccess", 0.5);
                addIntegerControl("NumSuccesses", 1);
                setName(RVariableIfc.RVType.NegativeBinomial.name());
            }

            public final RVariableIfc makeRVariable(RNStreamIfc rnStream) {
                double probOfSuccess = getDoubleControl("ProbOfSuccess");
                double numSuccesses = getDoubleControl("NumSuccesses");
                return new NegativeBinomialRV(probOfSuccess, numSuccesses, rnStream);
            }
        };
    }
}
