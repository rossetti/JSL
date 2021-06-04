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

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.DUniformRV;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.RVariableIfc;

/** Models discrete random variables that are uniformly distributed
 * over a contiguous range of integers.
 */
public class DUniform extends Distribution implements DiscreteDistributionIfc, GetRVariableIfc {

    private int myMinimum;

    private int myMaximum;

    private int myRange;

    /** Constructs a discrete uniform over the range {0,1}
     */
    public DUniform() {
        this(0, 1, null);
    }

    /** Constructs a discrete uniform where parameter[0] is the
     * lower slimit and parameter[1] is the upper limit of the range.
     *  the lower limit must be &lt; upper limit
     * @param parameters A array containing the lower limit and upper limit
     */
    public DUniform(double[] parameters) {
        this((int) parameters[0], (int) parameters[1], null);
    }

    /** Constructs a discrete uniform over the supplied range
     *  the lower limit must be &lt; upper limit
     * @param minimum The lower limit of the range
     * @param maximum The upper limit of the range
     */
    public DUniform(int minimum, int maximum) {
        this(minimum, maximum, null);
    }

    /** Constructs a discrete uniform over the supplied range
     *  the lower limit must be &lt; upper limit
     * @param minimum The lower limit of the range
     * @param maximum The upper limit of the range
     * @param name an optional name/label
     */
    public DUniform(int minimum, int maximum, String name) {
        super(name);
        setRange(minimum, maximum);
    }

    /** Returns a new instance of the random source with the same parameters
     *  but an independent generator
     *
     * @return
     */
    @Override
    public final DUniform newInstance() {
        return (new DUniform(getParameters()));
    }

    /** Gets the distribution's lower limit
     * @return The lower limit
     */
    public final int getMinimum() {
        return (myMinimum);
    }

    /** Gets the distribution's upper limit
     * @return The upper limit
     */
    public final int getMaximum() {
        return (myMaximum);
    }

    /** Sets the range for the distribution
     *  the lower limit must be &lt; upper limit
     * @param minimum The lower limit for the range
     * @param maximum The upper limit for the range
     */
    public final void setRange(int minimum, int maximum) {
        if (minimum >= maximum) {
            throw new IllegalArgumentException("Lower limit must be < upper limit.");
        }
        myMinimum = minimum;
        myMaximum = maximum;
        myRange = myMaximum - myMinimum + 1;
    }

    /** The discrete maximum - minimum + 1
     * 
     * @return the returned range
     */
    public final int getRange(){
        return (int)myRange;
    }
    
    @Override
    public final double cdf(double x) {
        if (x < myMinimum) {
            return 0.0;
        } else if ((x >= myMinimum) && (x <= myMaximum)) {
            return ((Math.floor(x) - myMinimum + 1) / myRange);
        } else //if (x > myMaximum)
        {
            return 1.0;
        }
    }

    /** Provides the inverse cumulative distribution function for the distribution
     * @param prob The probability to be evaluated for the inverse, prob must be [0,1] or
     * an IllegalArgumentException is thrown
     */
    @Override
    public final double invCDF(double prob) {
        if ((prob < 0.0) || (prob > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }
        return (myMinimum + Math.floor(myRange * prob));
    }

    /** If x is not and integer value, then the probability must be zero
     *  otherwise pmf(int x) is used to determine the probability
     *
     * @param x the value to evaluate
     * @return the associated probability
     */
    @Override
    public final double pmf(double x) {
        if (Math.floor(x) == x) {
            return pmf((int) x);
        } else {
            return 0.0;
        }
    }

    @Override
    public final double getMean() {
        return ((myMinimum + myMaximum) / 2.0);
    }

    @Override
    public final double getVariance() {
        return ((myRange * myRange - 1) / 12.0);
    }

    /** Returns the probability associated with x
     *
     * @param x the value to evaluate
     * @return the associated probability
     */
    public final double pmf(int x) {
        if ((x < myMinimum) || (x > myMaximum)) {
            return (0.0);
        }
        return (1.0 / myRange);
    }

    /** Sets the parameters for the distribution where parameters[0] is the
     * lowerlimit and parameters[1] is the upper limit of the range.
     *  the lower limit must be &lt; upper limit
     *
     * @param parameters an array of doubles representing the parameters for
     * the distribution
     */
    @Override
    public final void setParameters(double[] parameters) {
        setRange((int) parameters[0], (int) parameters[1]);
    }

    /** Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    @Override
    public final double[] getParameters() {
        double[] param = new double[2];
        param[0] = myMinimum;
        param[1] = myMaximum;
        return (param);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new DUniformRV(myMinimum, myMaximum, rng);
    }
}
