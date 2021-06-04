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

import jsl.utilities.Interval;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.UniformRV;

/** Defines a uniform distribution over the given range.
 *
 */
public class Uniform extends Distribution implements ContinuousDistributionIfc,
        InverseCDFIfc, GetRVariableIfc {

    private double myMin;

    private double myMax;

    private double myRange;

    /** Constructs a uniform distribution over the range (0,1)
     */
    public Uniform() {
        this(0.0, 1.0, null);
    }

    /** Constructs a uniform distribution with
     * lower limit = parameters[0], upper limit = parameters[1]
     * @param parameters The array of parameters
     */
    public Uniform(double[] parameters) {
        this(parameters[0], parameters[1], null);
    }

    /** Constructs a uniform distribution over the provided range
     *
     * @param lowerLimit limit of the distribution
     * @param upperLimit limit of the distribution
     */
    public Uniform(double lowerLimit, double upperLimit) {
        this(lowerLimit, upperLimit, null);
    }

    /** Constructs a uniform distribution over the provided range
     *
     * @param lowerLimit limit of the distribution
     * @param upperLimit limit of the distribution
     * @param name an optional name/label
     */
    public Uniform(double lowerLimit, double upperLimit, String name) {
        super(name);
        setRange(lowerLimit, upperLimit);
    }

    @Override
    public final Uniform newInstance() {
        return (new Uniform(getParameters()));
    }

    @Override
    public final Interval getDomain(){
        return new Interval(getMinimum(), getMaximum());
    }

    /** Gets the lower limit for the distribution
     * @return The lower limit
     */
    public final double getMinimum() {
        return (myMin);
    }

    /** Gets the upper limit of the distribution
     * @return The upper limit
     */
    public final double getMaximum() {
        return (myMax);
    }

    /** Sets the minimum and maximum value of the distribution
     *  throws IllegalArgumentException when if min &gt;= max
     *
     * @param min The minimum value of the distribution
     * @param max The maximum value of the distribution
     */
    public final void setParameters(double min, double max) {
        setRange(min, max);
    }

    /** Sets the range
     * @param min The lower limit for the distribution
     * @param max The upper limit for the distribution
     */
    public final void setRange(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("Lower limit must be < upper limit. lower limit = " + min + " upper limit = " + max);
        }
        myMin = min;
        myMax = max;
        myRange = myMax - myMin;
    }

    /**
     *
     * @return the range (max - min)
     */
    public final double getRange() {
        return (myRange);
    }

    @Override
    public final double cdf(double x) {
        if (x < myMin) {
            return 0.0;
        } else if ((x >= myMin) && (x <= myMax)) {
            return ((x - myMin) / myRange);
        } else //if (x > myMax)
        {
            return 1.0;
        }
    }

    @Override
    public final double invCDF(double prob) {
        if ((prob < 0.0) || (prob > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }

        return (myMin + myRange * prob);
    }

    @Override
    public final double pdf(double x) {
        if ((x < myMin) || (x > myMax)) {
            return (0.0);
        }

        return (1.0 / myRange);
    }

    @Override
    public final double getMean() {
        return ((myMin + myMax) / 2.0);
    }

    /**
     *
     * @return the 3rd moment
     */
    public final double getMoment3() {
        return (1.0 / 4.0) * ((myMin + myMax) * ((myMin * myMin) + (myMax * myMax)));
    }

    /**
     *
     * @return the 4th moment
     */
    public final double getMoment4() {
        double min2 = myMin * myMin;
        double max2 = myMax * myMax;
        return (1.0 / 5.0) * ((min2 * min2) + (min2 * myMin * myMax) + (min2 * max2) + (myMin * myMax * max2) + (max2 * max2));
    }

    @Override
    public final double getVariance() {
        return ((myRange * myRange) / 12.0);
    }

    /** Gets the kurtosis of the distribution
     * www.mathworld.wolfram.com/UniformDistribution.html
     * @return the kurtosis
     */
    public final double getKurtosis() {
        return (-6.0 / 5.0);
    }

    /** Gets the skewness of the distribution
     *  www.mathworld.wolfram.com/UniformDistribution.html
     * @return the skewness
     */
    public final double getSkewness() {
        return (0.0);
    }

    /** Sets the parameters for the distribution where parameters[0] is the
     *  lowerlimit and parameters[1] is the upper limit of the range.
     *  the lower limit must be &lt; upper limit
     * 
     * @param parameters an array of doubles representing the parameters for
     * the distribution
     */
    @Override
    public final void setParameters(double[] parameters) {
        setRange(parameters[0], parameters[1]);
    }

    /** Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    @Override
    public final double[] getParameters() {
        double[] param = new double[2];
        param[0] = myMin;
        param[1] = myMax;
        return (param);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng){
        return new UniformRV(getMinimum(), getMaximum(), rng);
    }
}
