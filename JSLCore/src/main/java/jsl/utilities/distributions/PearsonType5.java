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
import jsl.utilities.random.rvariable.PearsonType5RV;
import jsl.utilities.random.rvariable.RVariableIfc;

/** Represents a Pearson Type V distribution, 
 *  see Law (2007) Simulation Modeling and Analysis, McGraw-Hill, pg 293
 * 
 *  Code contributed by Seda Gumrukcu
 *
 */
public class PearsonType5 extends Distribution implements ContinuousDistributionIfc, InverseCDFIfc, GetRVariableIfc {

    private double myShape;

    private double myScale;

    private Gamma myGammaCDF;

    private double myGAlpha;

    /** Creates a PearsonType5 distribution
     *
     * shape = 1.0
     * scale = 1.0
     */
    public PearsonType5() {
        this(1.0, 1.0, null);
    }

    /** Creates a PearsonType5 distribution
     * parameters[0] = shape
     * parameters[1] = scale
     *
     * @param parameters the parameter array
     */
    public PearsonType5(double[] parameters) {
        this(parameters[0], parameters[1], null);
    }

    /** Creates a PearsonType5 distribution
     *
     * @param shape must be &gt;0
     * @param scale must be &gt; 0
     */
    public PearsonType5(double shape, double scale) {
        this(shape, scale, null);
    }

    /** Creates a PearsonType5 distribution
     *
     * @param shape must be &gt;0
     * @param scale must be &gt; 0
     * @param name an optional label/name
     */
    public PearsonType5(double shape, double scale, String name) {
        super(name);
        setParameters(shape, scale);
    }

    @Override
    public final PearsonType5 newInstance() {
        return (new PearsonType5(getParameters()));
    }

    /** Sets the shape and scale parameters
     *
     * @param shape must be &gt; 0
     * @param scale must be &gt; 0
     */
    public final void setParameters(double shape, double scale) {
        if (shape <= 0) {
            throw new IllegalArgumentException("Alpha (shape parameter) should be > 0");
        }

        if (scale <= 0) {
            throw new IllegalArgumentException("Beta (scale parameter) should > 0");
        }

        myShape = shape;
        myGAlpha = Gamma.gammaFunction(shape);
        myScale = scale;

        if (myGammaCDF == null) {
            myGammaCDF = new Gamma(myShape, 1.0 / myScale);
        } else {
            myGammaCDF.setShape(shape);
            myGammaCDF.setScale(1.0 / scale);
        }
    }

    @Override
    public final Interval getDomain(){
        return new Interval(0, Double.POSITIVE_INFINITY);
    }

    /** Gets the shape parameter
     *
     * @return the shape parameter
     */
    public final double getShape() {
        return myShape;
    }

    /** Gets the scale parameter
     *
     * @return the scale parameter
     */
    public final double getScale() {
        return myScale;
    }

    @Override
    public double cdf(double x) {
        if (x > 0) {
            return 1 - myGammaCDF.cdf(1 / x);
        }

        return 0.0;
    }

    /** 
     *
     * @return If shape &lt;= 1.0, returns Double.NaN, otherwise, returns the mean
     */
    @Override
    public double getMean() {
        if (myShape <= 1.0) {
            return Double.NaN;
        }

        return (myScale / (myShape - 1.0));
    }

    /** Gets the parameters
     * parameters[0] = shape
     * parameters[1] = scale
     *
     */
    @Override
    public double[] getParameters() {
        double[] param = new double[2];
        param[0] = myShape;
        param[1] = myScale;
        return (param);
    }

    /** 
     *
     * @return If shape &lt;= 2.0, returns Double.NaN, otherwise returns the variance
     */
    @Override
    public double getVariance() {
        if (myShape <= 2.0) {
            return Double.NaN;
        }

        return (myScale * myScale) / ((myShape - 2.0) * (myShape - 1.0) * (myShape - 1.0));
    }

    @Override
    public double invCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Probability must be [0,1]");
        }
        return 1.0 / (myGammaCDF.invCDF(p));
    }

    @Override
    public double pdf(double x) {
        if (x > 0.0) {
            return ((Math.pow(x, -(myShape + 1.0))) * (Math.exp(-myScale / x))) / (Math.pow(myScale, -myShape) * myGAlpha);
        }

        return 0.0;
    }

    /** Sets the parameters
     * parameters[0] = shape
     * parameters[1] = scale
     *
     * @param parameters the parameter array
     */
    @Override
    public void setParameters(double[] parameters) {
        setParameters(parameters[0], parameters[1]);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new PearsonType5RV(myShape, myScale, rng);
    }
}
