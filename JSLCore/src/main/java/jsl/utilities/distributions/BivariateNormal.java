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

import jsl.utilities.random.ParametersIfc;
import jsl.utilities.random.rng.RNStreamControlIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

/** Allows for the generation of bivariate normal
 *  random variables
 *
 * @author rossetti
 */
public class BivariateNormal implements RNStreamControlIfc, ParametersIfc {

    protected RNStreamIfc myRNG;

    protected double myMu1;

    protected double myVar1;

    protected double myMu2;

    protected double myVar2;

    protected double myRho;

    /** Constructs a standard bivariate normal with no correlation
     *
     */
    public BivariateNormal() {
        this(0.0, 1.0, 0.0, 1.0, 0.0, JSLRandom.nextRNStream());
    }

    /** Constructs a standard bivariate normal with no correlation
     *
     */
    public BivariateNormal(RNStreamIfc rng) {
        this(0.0, 1.0, 0.0, 1.0, 0.0, rng);
    }

    /**
     *
     * @param mean1
     * @param var1
     * @param mean2
     * @param var2
     * @param rho
     */
    public BivariateNormal(double mean1, double var1, double mean2, double var2, double rho) {
        this(mean1, var1, mean2, var2, rho, JSLRandom.nextRNStream());
    }

    /** Interprets the array of parameters as the parameters
     *  param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     *
     * @param param
     */
    public BivariateNormal(double[] param) {
        this(param[0], param[1], param[2], param[3], param[4], JSLRandom.nextRNStream());
    }

    /** Interprets the array of parameters as the parameters
     *  param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     *
     * @param param
     * @param rng
     */
    public BivariateNormal(double[] param, RNStreamIfc rng) {
        this(param[0], param[1], param[2], param[3], param[4], rng);
    }

    /** Constructs a bivariate normal with the provided parameters
     *
     * @param mean1
     * @param var1
     * @param mean2
     * @param var2
     * @param rho
     * @param rng
     */
    public BivariateNormal(double mean1, double var1, double mean2, double var2, double rho, RNStreamIfc rng) {
        setParameters(mean1, var1, mean2, var2, rho);
        setRandomNumberGenerator(rng);
    }

    public void setAntitheticOption(boolean flag) {
        myRNG.setAntitheticOption(flag);
    }

    public void resetStartSubstream() {
        myRNG.resetStartSubstream();
    }

    public void resetStartStream() {
        myRNG.resetStartStream();
    }

    public void advanceToNextSubstream() {
        myRNG.advanceToNextSubstream();
    }

    /** Sets all the  parameters
     *
     * @param mean1
     * @param var1
     * @param mean2
     * @param var2
     * @param rho
     */
    public final void setParameters(double mean1, double var1, double mean2, double var2, double rho) {
        setMean1(mean1);
        setVariance1(var1);
        setMean2(mean2);
        setVariance2(var2);
        setCorrelation(rho);
    }

    /** Interprets the array of parameters as the parameters
     *  param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     *
     * @param param
     */
    public final void setParameters(double[] param) {
        setParameters(param[0], param[1], param[2], param[3], param[4]);
    }

    /** Returns the parameters as an array
     *
     *   param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     *
     * @return
     */
    public final double[] getParameters() {
        double[] param = new double[5];
        param[0] = myMu1;
        param[1] = myVar1;
        param[2] = myMu2;
        param[3] = myVar2;
        param[4] = myRho;
        return param;
    }

    /** Sets the first mean
     *
     * @param mean of the distribution
     */
    public final void setMean1(double mean) {
        myMu1 = mean;
    }

    /** Gets the first mean
     *
     * @return
     */
    public final double getMean1() {
        return myMu1;
    }

    /** Sets the first variance
     *
     * @param variance of the distribution, must be &gt; 0
     */
    public final void setVariance1(double variance) {
        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        myVar1 = variance;
    }

    /** Gets the first variance
     *
     * @return
     */
    public final double getVariance1() {
        return myVar1;
    }

    /** Sets the second mean
     *
     * @param mean
     */
    public final void setMean2(double mean) {
        myMu2 = mean;
    }

    /** Gets the second mean
     *
     * @return
     */
    public final double getMean2() {
        return myMu2;
    }

    /** Sets the 2nd variance
     *
     * @param variance of the distribution, must be &gt; 0
     */
    public final void setVariance2(double variance) {
        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        myVar2 = variance;
    }

    /** Gets the 2nd variance
     *
     * @return
     */
    public final double getVariance2() {
        return myVar2;
    }

    /** Sets the correlation
     *
     * @param rho
     */
    public final void setCorrelation(double rho) {
        if ((rho < -1.0) || (rho > 1.0)) {
            throw new IllegalArgumentException("The correlation must be within [-1,1]");
        }
        myRho = rho;
    }

    /** Gets the correlation
     *
     * @return
     */
    public final double getCorrelation() {
        return myRho;
    }

    /** Returns the distributions underlying random number generator
     *
     * @return
     */
    public RNStreamIfc getRandomNumberGenerator() {
        return (myRNG);
    }

    /** Sets the underlying random number generator for the distribution
     * Throws a NullPointerException if rng is null
     * @param rng the reference to the random number generator
     */
    public void setRandomNumberGenerator(RNStreamIfc rng) {
        if (rng == null) {
            throw new NullPointerException("RngIfc rng must be non-null");
        }
        myRNG = rng;
    }

    /** Fills the supplied array with 2 values
     *  As a convenience also returns the array
     *
     * @param x Must be of size 2 or larger
     * @return
     */
    public double[] getValues(double[] x) {
        if (x.length < 2) {
            throw new IllegalArgumentException("The array must have at least 2 elements");
        }

        double z0 = Normal.stdNormalInvCDF(myRNG.randU01());
        double z1 = Normal.stdNormalInvCDF(myRNG.randU01());
        double s1 = Math.sqrt(myVar1);
        double s2 = Math.sqrt(myVar2);
        x[0] = myMu1 + s1 * z0;
        x[1] = myMu2 + s2 * (myRho * z0 + Math.sqrt(1.0 - myRho * myRho) * z1);
        return x;
    }

    /** Returns an array containing the bivariate pair
     *  x[0] = 1st marginal
     *  x[1] = 2nd marginal
     *
     * @return
     */
    public double[] getValues() {
        return (getValues(new double[2]));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bivariate Normal\n");
        sb.append("mean 1 = " + myMu1 + "\n");
        sb.append("variance 1 = " + myVar1 + "\n");
        sb.append("mean 2 = " + myMu2 + "\n");
        sb.append("variance 2 = " + myVar2 + "\n");
        sb.append("correlation = " + myRho + "\n");
        return sb.toString();
    }

    public boolean getAntitheticOption() {
        return myRNG.getAntitheticOption();
    }
}
