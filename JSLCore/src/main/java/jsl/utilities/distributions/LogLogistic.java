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
import jsl.utilities.random.rvariable.LogLogisticRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;

/**
 * @author rossetti
 *
 */
public class LogLogistic extends Distribution implements ContinuousDistributionIfc, InverseCDFIfc, GetRVariableIfc {

    private double myShape; // alpha

    private double myScale;  // beta

    /**
     *
     * @param shape the shape parameter
     * @param scale the scale parameter
     */
    public LogLogistic(double shape, double scale) {
        this(shape, scale, null);
    }

    /**
     *
     * @param parameters the parameter array parameter[0] = shape, parameter[1] = scale
     */
    public LogLogistic(double[] parameters) {
        this(parameters[0], parameters[1], null);
    }

    /**
     *
     * @param shape the shape parameter
     * @param scale the scale parameter
     * @param name an optional label/name
     */
    public LogLogistic(double shape, double scale, String name) {
        super(name);
        setShape(shape);
        setScale(scale);
    }

    /** Returns a new instance of the random source with the same parameters
     *  but an independent generator
     *
     * @return
     */
    @Override
    public final LogLogistic newInstance() {
        return (new LogLogistic(getParameters()));
    }

    @Override
    public final Interval getDomain(){
        return new Interval(0, Double.POSITIVE_INFINITY);
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
    public final double pdf(double x) {
        if (x > 0.0) {
            double t1 = x / myScale;
            double n = myShape * Math.pow(t1, myShape - 1.0);
            double t2 = Math.pow(t1, myShape);
            double d = myScale * (1.0 + t2) * (1.0 + t2);
            return (n / d);
        } else {
            return 0.0;
        }
    }

    @Override
    public final double cdf(double x) {// alpha = shape, beta = scale
        if (x > 0.0) {
            double y = Math.pow(x / myScale, -myShape);
            return (1.0 / (1.0 + y));
        } else {
            return 0.0;
        }
    }

    @Override
    public final double getMean() {
        if (myShape <= 1.0) {
            return Double.NaN;
        }
        double theta = Math.PI / myShape;
        double csctheta = 1.0 / Math.sin(theta);
        return (myScale * theta * csctheta);
    }

    @Override
    public final double getVariance() {// alpha = shape, beta = scale
        if (myShape <= 2.0) {
            return Double.NaN;
        }
        double theta = Math.PI / myShape;
        double csctheta = 1.0 / Math.sin(theta);
        double csc2theta = 1.0 / Math.sin(2.0 * theta);
        return (myScale * myScale * theta * (2.0 * csc2theta - theta * csctheta * csctheta));
    }

    @Override
    public final double invCDF(double p) {// alpha = shape, beta = scale
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }

        if (p <= 0.0) {
            return 0.0;
        }

        if (p >= 1.0) {
            return Double.POSITIVE_INFINITY;
        }

        double c = p / (1.0 - p);
        return (myScale * Math.pow(c, 1.0 / myShape));
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

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new LogLogisticRV(myShape, myScale, rng);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        //		 alpha = shape, beta = scale
        LogLogistic x = new LogLogistic(3.0, 2.0);
        System.out.println(x);
        Statistic s = new Statistic();
        Statistic muhat = new Statistic("Estimated mean");
        Statistic varhat = new Statistic("Estimated variance");
        int m = 1000;
        int n = 1000;
        RVariableIfc rv = x.getRandomVariable();
        for (int j = 1; j <= m; j++) {
            for (int i = 1; i <= n; i++) {
                s.collect(rv.getValue());
            }
            muhat.collect(s.getAverage());
            varhat.collect(s.getVariance());
            s.reset();
        }
        System.out.println(muhat);
        System.out.println(varhat);
    }
}
