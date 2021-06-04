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
package jsl.utilities.distributions;

import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.ShiftedGeometricRV;

/**
 * The ShiftedeGeometric distribution is the probability distribution of
 * the number of Bernoulli trials needed to get one success.
 * supported on the set {1, 2, 3, ... }
 */
public class ShiftedGeometric extends Distribution implements DiscreteDistributionIfc,
        GetRVariableIfc {

    /**
     * The probability of success on a trial
     */
    private double myProbSuccess;

    /**
     * The probability of failure on a trial
     */
    private double myProbFailure;

    /**
     * Constructs a ShiftedGeometric with success probability = 0.5
     */
    public ShiftedGeometric() {
        this(0.5, null);
    }

    /**
     * Constructs a ShiftedGeometric using the supplied parameters array
     * parameters[0] is probability of success
     *
     * @param parameters
     */
    public ShiftedGeometric(double[] parameters) {
        this(parameters[0], null);
    }

    /**
     * Constructs a ShiftedGeometric using the supplied success probability
     *
     * @param prob the probability of success
     */
    public ShiftedGeometric(double prob) {
        this(prob, null);
    }

    /**
     * Constructs a ShiftedGeometric using the supplied success probability
     *
     * @param prob the probability of success
     * @param name an optional name/label
     */
    public ShiftedGeometric(double prob, String name) {
        super(name);
        setProbabilityOfSuccess(prob);
    }

    @Override
    public final ShiftedGeometric newInstance() {
        return (new ShiftedGeometric(getParameters()));
    }

    /**
     * Sets the probability of success
     *
     * @param prob the probability of success
     */
    public final void setProbabilityOfSuccess(double prob) {
        if ((prob < 0.0) || (prob > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }
        myProbSuccess = prob;
        myProbFailure = 1.0 - myProbSuccess;
    }

    /**
     * Gets the probability of success
     *
     * @return the probability of success
     */
    public final double getProbabilityOfSuccess() {
        return myProbSuccess;
    }

    @Override
    public final double getMean() {
        return (1.0 + ((myProbFailure) / myProbSuccess));
    }

    @Override
    public final double getVariance() {
        return (myProbFailure) / (myProbSuccess * myProbSuccess);
    }

    /**
     * Sets the parameters using the supplied array
     * parameters[0] is probability of success
     *
     * @param parameters the parameter array
     */
    @Override
    public final void setParameters(double[] parameters) {
        setProbabilityOfSuccess(parameters[0]);
    }

    /**
     * Gets the parameters as an array
     * parameters[0] is probability of success
     *
     * @return the parameter array
     */
    @Override
    public final double[] getParameters() {
        double[] param = new double[1];
        param[0] = myProbSuccess;
        return (param);
    }

    /**
     * computes the pmf of the distribution
     * f(x) = p(1-p)^(x-1.0)
     *
     * @param x the value to evaluate
     * @return the probability at x
     */
    public final double pmf(int x) {
        if (x < 1) {
            return 0.0;
        }
        return (myProbSuccess * Math.pow(myProbFailure, x - 1.0));
    }

    @Override
    public final double pmf(double x) {
        if (Math.floor(x) == x) {
            return pmf((int) x);
        } else {
            return 0.0;
        }
    }

    @Override
    public final double cdf(double x) {
        if (x < 1) {
            return 0.0;
        }
        double xx = Math.floor(x - 1.0) + 1.0;
        return (1 - Math.pow(myProbFailure, xx));
    }

    @Override
    public final double invCDF(double prob) {
        if ((prob < 0.0) || (prob > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + prob + " Probability must be [0,1)");
        }

        if (JSLMath.equal(prob, 1.0, JSLMath.getMachinePrecision())) {
            throw new IllegalArgumentException("Supplied probability was within machine precision of 1.0 Probability must be (0,1)");
        }

        if (JSLMath.equal(prob, 0.0, JSLMath.getMachinePrecision())) {
            throw new IllegalArgumentException("Supplied probability was within machine precision of 0.0 Probability must be (0,1)");
        }

        return (1.0 + Math.ceil((Math.log(1.0 - prob) / (Math.log(1.0 - myProbSuccess))) - 1.0));
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new ShiftedGeometricRV(myProbSuccess, rng);
    }
}
