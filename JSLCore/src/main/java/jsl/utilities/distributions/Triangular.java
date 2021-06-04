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
import jsl.utilities.random.rvariable.TriangularRV;

/** Represents the Triangular distribution with
 * parameters - minimum value, maximum value and most likely value
 */
public class Triangular extends Distribution implements
        ContinuousDistributionIfc, InverseCDFIfc, GetRVariableIfc {

    /**
     * myMin the minimum value of the distribution
     */
    private double myMin;

    /**
     * myMax the maximum value of the distribution
     */
    private double myMax;

    /**
     *myMax the maximum value of the distribution
     */
    private double myMode;

    /**
     *myRange = myMax - myMin
     */
    private double myRange;

    /** Constructs a Triangular distribution with
     * min = 0.0, mode = 0.0, and max = 1.0
     */
    public Triangular() {
        this(0.0, 0.0, 1.0, null);
    }

    /** Constructs a Triangular distribution with
     * min = parameters[0], mode = parameters[1], max = parameters[2]
     * @param parameters The array of parameters
     */
    public Triangular(double[] parameters) {
        this(parameters[0], parameters[1], parameters[2], null);
    }

    /** Constructs a Triangular distribution with min, mode, and max
     *
     * @param min The minimum value of the distribution
     * @param mode The mode of the distribution
     * @param max The maximum value of the distribution
     */
    public Triangular(double min, double mode, double max) {
        this(min, mode, max, null);
    }

    /** Constructs a Triangular distribution with min, mode, and max
     *
     * @param min The minimum value of the distribution
     * @param mode The mode of the distribution
     * @param max The maximum value of the distribution
     * @param name an optional label/name
     */
    public Triangular(double min, double mode, double max, String name) {
        super(name);
        setParameters(min, mode, max);
    }

    @Override
    public final Triangular newInstance() {
        return (new Triangular(getParameters()));
    }

    @Override
    public final Interval getDomain(){
        return new Interval(getMinimum(), getMaximum());
    }

    /** Sets the minimum, most likely and maximum value of the triangular distribution to the private data members myMin, myMode and myMax resp
     *  throws IllegalArgumentException when the min &gt;mode, min &gt;= max, mode &gt; max
     *
     * @param min The minimum value of the distribution
     * @param mode The mode of the distribution
     * @param max The maximum value of the distribution
     */
    public final void setParameters(double min, double mode, double max) {

        if (min > mode) {
            throw new IllegalArgumentException("min must be <= mode");
        }

        if (min >= max) {
            throw new IllegalArgumentException("min must be < max");
        }

        if (mode > max) {
            throw new IllegalArgumentException("mode must be <= max");
        }

        myMode = mode;
        myMin = min;
        myMax = max;
        myRange = myMax - myMin;
    }

    /** Returns true if the parameters are valid for the distribution
     *
     * min = param[0]
     * mode = param[1]
     *  max = param[2]
     * @param param the parameter array
     * @return true if the parameters are valid
     */
    public static boolean checkParameters(double[] param) {
        if (param == null) {
            return false;
        }
        if (param.length != 3) {
            return false;
        }
        double min = param[0];
        double mode = param[1];
        double max = param[2];

        if (min > mode) {
            return false;
        }

        if (min >= max) {
            return false;
        }

        if (mode > max) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return the minimum of the distribution
     */
    public final double getMinimum() {
        return (myMin);
    }

    /**
     *
     * @return the mode of the distribution
     */
    public final double getMode() {
        return (myMode);
    }

    /**
     *
     * @return the maximum of the distribution
     */
    public final double getMaximum() {
        return (myMax);
    }

    @Override
    public final double getMean() {
        return (myMin + myMax + myMode) / 3.0;
    }

    /**
     *
     * @return the 3rd moment
     */
    public final double getMoment3() {
        return (1.0 / 10.0) * ((myMin * myMin * myMin) + (myMode * myMode * myMode) + (myMax * myMax * myMax) + (myMin * myMin * myMode) + (myMin * myMin * myMax) + (myMode * myMode * myMin) + (myMode * myMode * myMax) + (myMax * myMax * myMin) + (myMax * myMax * myMode) + (myMin * myMode * myMax));
    }

    /**
     *
     * @return the 4th moment
     */
    public final double getMoment4() {
        return ((1.0 / 135.0) * ((myMin * myMin + myMode * myMode + myMax * myMax - myMin * myMode - myMin * myMax - myMode * myMax) * (myMin * myMin + myMode * myMode + myMax * myMax - myMin * myMode - myMin * myMax - myMode * myMax)) + 4 * ((1 / 270) * (myMin + myMode - (2 * myMax)) * (myMin + myMax - (2 * myMode)) * (myMode + myMax - (2 * myMin)) * ((myMin + myMode + myMax) / 3)) + ((1 / 3) * (myMin * myMin + myMode * myMode + myMax * myMax - myMin * myMode - myMin * myMax - myMode * myMax) * ((myMin + myMode + myMax) / 3) * ((myMin + myMode + myMax) / 3)) + ((myMin + myMode + myMax) / 3) * ((myMin + myMode + myMax) / 3) * ((myMin + myMode + myMax) / 3) * ((myMin + myMode + myMax) / 3));
    }

    @Override
    public final double getVariance() {
        return ((myMin * myMin) + (myMax * myMax) + (myMode * myMode) - (myMax * myMin) - (myMin * myMode) - (myMax * myMode)) / 18.0;
    }

    @Override
    public final double pdf(double x) {

        // Right triangular, mode = max
        if (myMode == myMax) {
            if ((myMin <= x) && (x <= myMax)) {
                return (2.0 * (x - myMin) / (myRange * myRange));
            } else {
                return (0.0);
            }
        }

        // Left triangular, min = mode
        if (myMin == myMode) {
            if ((myMin <= x) && (x <= myMax)) {
                return (2.0 * (myMax - x) / (myRange * myRange));
            } else {
                return (0.0);
            }
        }

        // regular triangular min < mode < max

        if ((myMin <= x) && (x <= myMode)) {
            return (2.0 * (x - myMin) / (myRange * (myMode - myMin)));
        } else if ((myMode < x) && (x <= myMax)) {
            return (2.0 * (myMax - x) / (myRange * (myMax - myMode)));
        } else {
            return (0.0);
        }
    }

    @Override
    public final double cdf(double x) {

        // Right triangular, mode = max
        if (myMode == myMax) {
            if (x < myMin) {
                return (0.);
            } else if ((myMin <= x) && (x <= myMax)) {
                double y = (x - myMin) / myRange;
                return (y * y);
            } else {
                return (1.0);
            }
        }

        // Left triangular, min = mode
        if (myMin == myMode) {
            if (x < myMin) {
                return (0.);
            } else if ((myMin <= x) && (x <= myMax)) {
                double y = (myMax - x) / myRange;
                return (1.0 - y * y);
            } else {
                return (1.0);
            }
        }

        // regular triangular min < mode < max

        if (x < myMin) {
            return (0.0);
        } else if ((myMin <= x) && (x <= myMode)) {
            return (((x - myMin) * (x - myMin)) / (myRange * (myMode - myMin)));
        } else if ((myMode < x) && (x <= myMax)) {
            return (1.0 - ((myMax - x) * (myMax - x)) / (myRange * (myMax - myMode)));
        } else {
            return (1.0);
        }

    }

    @Override
    public final double invCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }

        // if X ~ triang(0,(mode-min)/(max-min),1) then Y = min + (max-min)*X ~ triang(min, mode, max)
        // get parameters for triang(0,(mode-min)/(max-min),1)

        double c = (myMode - myMin) / myRange;

        // get the invCDF for a triang(0,c,1)

        double x;

        if (c == 0.0) { // left triangular, mode equals min
            x = 1.0 - Math.sqrt(1 - p);
        } else if (c == 1.0) { //right triangular, mode equals max
            x = Math.sqrt(p);
        } else {
            if (p < c) {
                x = Math.sqrt(c * p);
            } else {
                x = 1.0 - Math.sqrt((1.0 - c) * (1.0 - p));
            }
        }
        // scale it back to original scale
        return (myMin + myRange * x);
    }

    /** Gets the kurtosis of the distribution
     * mu4/mu2^2, www.mathworld.wolfram.com/Kurtosis.html
     * www.mathworld.wolfram.com/TriangularDistribution.html
     * @return the kurtosis
     */
    public final double getKurtosis() {
        return (2.4);
    }

    /** Gets the skewness of the distribution
     *  mu3/mu2^(3/2), www.mathworld.wolfram.com/Skewness.html
     *  www.mathworld.wolfram.com/TriangularDistribution.html
     * @return the skewness
     */
    public final double getSkewness() {
        double mu3 = -(myMin + myMax - 2.0 * myMode) * (myMin + myMode - 2.0 * myMax) * (myMax + myMode - 2.0 * myMin) / 270.0;
        double mu2 = getVariance();
        return (mu3 / Math.pow(mu2, (3.0 / 2.0)));
    }

    /** Sets the parameters for the distribution
     * parameters[0] min The minimum value of the distribution
     * parameters[1] mode The mode of the distribution
     * parameters[2] max The maximum value of the distribution
     *
     * @param parameters an array of doubles representing the parameters for
     * the distribution
     */
    public void setParameters(double[] parameters) {
        setParameters(parameters[0], parameters[1], parameters[2]);
    }

    /** Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    public double[] getParameters() {
        double[] param = new double[3];
        param[0] = myMin;
        param[1] = myMode;
        param[2] = myMax;
        return (param);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new TriangularRV(myMin, myMode, myMax, rng);
    }
}
