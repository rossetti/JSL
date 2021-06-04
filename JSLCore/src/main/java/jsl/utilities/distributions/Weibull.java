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
import jsl.utilities.random.rvariable.WeibullRV;

/** This class defines a Weibull distribution
 *
 */
public class Weibull extends Distribution implements ContinuousDistributionIfc,
        InverseCDFIfc, GetRVariableIfc {

    private double myShape; // alpha

    private double myScale;  // beta

    /** Creates new weibull with shape 1.0, scale 1.0
     */
    public Weibull() {
        this(1.0, 1.0, null);
    }

    /** Constructs a weibull distribution with
     * shape = parameters[0] and scale = parameters[1]
     * @param parameters An array with the shape and scale
     */
    public Weibull(double[] parameters) {
        this(parameters[0], parameters[1], null);
    }

    /** Constructs a weibull distribution with supplied shape and scale
     *
     * @param shape The shape parameter of the distribution
     * @param scale The scale parameter of the distribution
     */
    public Weibull(double shape, double scale) {
        this(shape, scale, null);
    }

    /** Constructs a weibull distribution with supplied shape and scale
     *
     * @param shape The shape parameter of the distribution
     * @param scale The scale parameter of the distribution
     * @param name an optional name/label
     */
    public Weibull(double shape, double scale, String name) {
        super(name);
        setParameters(shape, scale);
    }

    @Override
    public final Weibull newInstance() {
        return (new Weibull(getParameters()));
    }

    @Override
    public final Interval getDomain(){
        return new Interval(0, Double.POSITIVE_INFINITY);
    }

    /** Sets the parameters
     * @param shape The shape parameter must &gt; 0.0
     * @param scale The scale parameter must be &gt; 0.0
     */
    public final void setParameters(double shape, double scale) {
        setShape(shape);
        setScale(scale);
    }

    /** Sets the parameters for the distribution with
     * shape = parameters[0] and scale = parameters[1]
     *
     * @param parameters an array of doubles representing the parameters for
     * the distribution
     */
    @Override
    public final void setParameters(double[] parameters) {
        setShape(parameters[0]);
        setScale(parameters[1]);
    }

    /** Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    @Override
    public final double[] getParameters() {
        double[] param = new double[2];
        param[0] = myShape;
        param[1] = myScale;
        return (param);
    }

    /** Sets the shape parameter
     * @param shape The shape parameter must &gt; 0.0
     */
    public final void setShape(double shape) {
        if (shape <= 0) {
            throw new IllegalArgumentException("Shape parameter must be positive");
        }
        myShape = shape;
    }

    /** Sets the scale parameter
     * @param scale The scale parameter must be &gt; 0.0
     */
    public final void setScale(double scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale parameter must be positive");
        }
        myScale = scale;
    }

    /** Gets the shape
     * @return The shape parameter as a double
     */
    public final double getShape() {
        return myShape;
    }

    /** Gets the scale parameter
     * @return The scale parameter as a double
     */
    public final double getScale() {
        return myScale;
    }

    @Override
    public final double getMean() { // shape = alpha, scale = beta
        double ia = 1.0 / myShape;
        double gia = Gamma.gammaFunction(ia);
        double m = myScale * ia * gia;
        return (m);
    }

    @Override
    public final double getVariance() {
        double ia = 1.0 / myShape;
        double gia = Gamma.gammaFunction(ia);
        double g2ia = Gamma.gammaFunction(2.0 * ia);
        double v = myScale * myScale * ia * (2.0 * g2ia - ia * gia * gia);
        return (v);
    }

    @Override
    public final double cdf(double x) {
        if (x > 0.0) {
            return 1 - Math.exp(-Math.pow(x / myScale, myShape));
        } else {
            return (0.0);
        }
    }

    @Override
    public final double pdf(double x) {
        if (x <= 0) {
            return (0.0);
        }
        double e1 = -Math.pow(x / myScale, myShape);
        double f = myScale * Math.pow(myScale, -myShape);
        f = f * Math.pow(x, myShape - 1.0);
        f = f * Math.exp(e1);
        return (f);
    }

    @Override
    public final double invCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p
                    + " Probability must be [0,1]");
        }

        if (p <= 0.0) {
            return 0.0;
        }

        if (p >= 1.0) {
            return Double.POSITIVE_INFINITY;
        }

        return myScale * Math.pow(-Math.log(1.0 - p), 1.0 / myShape);
    }

    /**
     *
     * @return the 3rd moment
     */
    public final double getMoment3() {
        return Math.pow(myShape, 3) * Math.exp(Gamma.logGammaFunction(1 + (3 * (1 / myScale))));
    }

    /**
     *
     * @return the 4th moment
     */
    public final double getMoment4() {
        return Math.pow(myShape, 4) * Math.exp(Gamma.logGammaFunction(1 + (4 * (1 / myScale))));
    }

    /** Gets the kurtosis of the distribution
     * www.mathworld.wolfram.com/WeibullDistribution.html
     * @return the kurtosis
     */
    public final double kurtosis() {
        double c1 = (myShape + 1.0) / myShape;
        double c2 = (myShape + 2.0) / myShape;
        double c3 = (myShape + 3.0) / myShape;
        double c4 = (myShape + 4.0) / myShape;
        double gc1 = Gamma.gammaFunction(c1);
        double gc2 = Gamma.gammaFunction(c2);
        double gc3 = Gamma.gammaFunction(c3);
        double gc4 = Gamma.gammaFunction(c4);
        double n = -3.0 * gc1 * gc1 * gc1 * gc1 + 6.0 * gc1 * gc1 * gc2 - 4.0 * gc1 * gc3 + gc4;
        double d = (gc1 * gc1 - gc2) * (gc1 * gc1 - gc2);
        return ((n / d) - 3.0);
    }

    /** Gets the skewness of the distribution
     *  www.mathworld.wolfram.com/WeibullDistribution.html
     * @return the skewness
     */
    public final double skewness() {
        double c1 = (myShape + 1.0) / myShape;
        double c2 = (myShape + 2.0) / myShape;
        double c3 = (myShape + 3.0) / myShape;
        double gc1 = Gamma.gammaFunction(c1);
        double gc2 = Gamma.gammaFunction(c2);
        double gc3 = Gamma.gammaFunction(c3);
        double n = 2.0 * gc1 * gc1 * gc1 - 3.0 * gc1 * gc2 + gc3;
        double d = Math.sqrt((gc2 - gc1 * gc1) * (gc2 - gc1 * gc1) * (gc2 - gc1 * gc1));
        return (n / d);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new WeibullRV(myShape, myScale, rng);
    }
}
