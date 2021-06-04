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

/**  Allows for the generation of bivariate lognormal random variables.
 *
 *   Note that this class takes in the actual parameters of the bivariate
 *   lognormal and computes the necessary parameters for the underlying bivariate normal
 *
 * @author rossetti
 */
public class BivariateLogNormal implements RNStreamControlIfc, ParametersIfc {

    protected BivariateNormal myBVN;

    protected double myMu1;

    protected double myVar1;

    protected double myMu2;

    protected double myVar2;

    protected double myRho;

    /** Constructs a bivariate lognormal with mean's = 1.0, variance = 1.0. correlation = 0.0
     * 
     */
    public BivariateLogNormal() {
        this(1.0, 1.0, 1.0, 1.0, 0.0, JSLRandom.nextRNStream());
    }

    /** Constructs a bivariate lognormal with mean's = 1.0, variance = 1.0. correlation = 0.0
     *
     * @param rng a random number generator
     */
    public BivariateLogNormal(RNStreamIfc rng) {
        this(1.0, 1.0, 1.0, 1.0, 0.0, rng);
    }

    /**
     *
     * @param mean1 the first mean
     * @param var1 the first variance
     * @param mean2 the second mean
     * @param var2 the second variance
     * @param rho the corrlation
     */
    public BivariateLogNormal(double mean1, double var1, double mean2, double var2, double rho) {
        this(mean1, var1, mean2, var2, rho, JSLRandom.nextRNStream());
    }

    /** Interprets the array of parameters as the parameters
     *  param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     *
     * @param param the parameter array
     */
    public BivariateLogNormal(double[] param) {
        this(param[0], param[1], param[2], param[3], param[4],
                JSLRandom.nextRNStream());
    }

    /** Interprets the array of parameters as the parameters
     *  param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     *
     * @param param the parameter array
     * @param rng a random number generator
     */
    public BivariateLogNormal(double[] param, RNStreamIfc rng) {
        this(param[0], param[1], param[2], param[3], param[4], rng);
    }

    /** These parameters are the parameters of the lognormal (not the bivariate normal)
     *
     * @param mean1 lognormal mean
     * @param var1 lognormal variance
     * @param mean2 lognormal 2nd mean
     * @param var2 lognormal 2nd variance
     * @param rho correlation of lognormals
     * @param rng a random number generator
     */
    public BivariateLogNormal(double mean1, double var1, double mean2, double var2, double rho, RNStreamIfc rng) {
        myBVN = new BivariateNormal(rng);
        setParameters(mean1, var1, mean2, var2, rho);
    }

    /** Takes in the parameters of the bivariate lognormal and sets
     *  the parameters of the underlying bivariate normal
     * 
     * @param m1 the first mean
     * @param v1 the first variance
     * @param m2 the second mean
     * @param v2 the second variance
     * @param r the correlation
     */
    public void setParameters(double m1, double v1, double m2, double v2, double r) {
        if (m1 <= 0) {
            throw new IllegalArgumentException("Mean 1 must be positive");
        }
        if (m2 <= 0) {
            throw new IllegalArgumentException("Mean 1 must be positive");
        }
        if (v1 <= 0) {
            throw new IllegalArgumentException("Variance 1 must be positive");
        }
        if (v2 <= 0) {
            throw new IllegalArgumentException("Variance 2 must be positive");
        }
        if ((r < -1.0) || (r > 1.0)) {
            throw new IllegalArgumentException("The correlation must be within [-1,1]");
        }
        // set the parameters
        myMu1 = m1;
        myVar1 = v1;
        myMu2 = m2;
        myVar2 = v2;
        myRho = r;
        // calcuate parameters of underlying bivariate normal
        // get the means
        double mean1 = Math.log((m1 * m1) / Math.sqrt(m1 * m1 + v1));
        double mean2 = Math.log((m2 * m2) / Math.sqrt(m2 * m2 + v2));
        // get the variances
        double var1 = Math.log(1.0 + (v1 / Math.abs(m1 * m1)));
        double var2 = Math.log(1.0 + (v2 / Math.abs(m2 * m2)));
        // calculate the correlation

        double cov = Math.log(1.0 + ((r * Math.sqrt(v1 * v2)) / Math.abs(m1 * m2)));
        double rho = cov / Math.sqrt(var1 * var2);
        // set the parameters of the underlying bivariate normal
        myBVN.setParameters(mean1, var1, mean2, var2, rho);
    }

    /** Interprets the array of parameters as the parameters
     *  param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     *
     * @param param the parameter array
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
     * @return the parameters
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

    /** Returns the parameters of the underlying bivariate normal
     *   param[0] = mean 1;
     *  param[1] = variance 1;
     *  param[2] = mean 2;
     *  param[3] = variance 2;
     *  param[4] = correlation;
     * 
     * @return the computed parameters
     */
    public final double[] getBiVariateNormalParameters() {
        return myBVN.getParameters();
    }

    /** Takes in the parameters of a bivariate normal and returns
     *  the corresponding parameters for the bivariate lognormal
     *
     *   param[0] = mean 1 of bivariate lognormal
     *  param[1] = variance 1 of bivariate lognormal
     *  param[2] = mean 2 of bivariate lognormal
     *  param[3] = variance 2 of bivariate lognormal
     *  param[4] = correlation of bivariate lognormal
     *
     * @param m1 mean 1 of bivariate normal
     * @param v1 variance 1 of bivariate normal
     * @param m2 mean 1 of bivariate normal
     * @param v2 variance 2 of bivariate normal
     * @param r correlation of bivariate normal
     * @return the computed parameters
     */
    public static double[] getBVLNParametersFromBVNParameters(double m1, double v1, double m2, double v2, double r) {
        if (v1 <= 0) {
            throw new IllegalArgumentException("Variance 1 must be positive");
        }
        if (v2 <= 0) {
            throw new IllegalArgumentException("Variance 2 must be positive");
        }
        if ((r < -1.0) || (r > 1.0)) {
            throw new IllegalArgumentException("The correlation must be within [-1,1]");
        }

        double[] x = new double[5];
        x[0] = Math.exp(m1 + (v1 / 2.0));// mu 1 of LN
        x[1] = Math.exp(2.0 * m1 + v1) * (Math.exp(v1) - 1.0);// var 1 of LN
        x[2] = Math.exp(m2 + (v2 / 2.0)); // mu 2 of LN
        x[3] = Math.exp(2.0 * m2 + v2) * (Math.exp(v2) - 1.0);// var 2 of LN
        // compute covariance of normal
        double cov = r * Math.sqrt(v1) * Math.sqrt(v2);
        double n = Math.exp(cov) - 1.0;
        double d = Math.sqrt((Math.exp(v1) - 1.0) * (Math.exp(v2) - 1.0));
        x[4] = n / d;
        return x;
    }

    /** Takes in the parameters of a bivariate normal and returns
     *  the corresponding parameters for the bivariate lognormal
     *
     *   x[0] = mean 1 of bivariate lognormal
     *  x[1] = variance 1 of bivariate lognormal
     *  x[2] = mean 2 of bivariate lognormal
     *  x[3] = variance 2 of bivariate lognormal
     *  x[4] = correlation of bivariate lognormal
     *
     * @param param array of parameters representing the bivariate normal
     * @return the computed parameters
     */
    public static double[] getBVLNParametersFromBVNParameters(double[] param) {
        return getBVLNParametersFromBVNParameters(param[0], param[1], param[2], param[3], param[4]);
    }

    /** Sets the first mean
     *
     * @param mean of the distribution
     */
    public final void setMean1(double mean) {
        setParameters(mean, myVar1, myMu2, myVar2, myRho);
    }

    /** Gets the first mean
     *
     * @return the first mean
     */
    public final double getMean1() {
        return myMu1;
    }

    /** Sets the first variance
     *
     * @param variance of the distribution, must be &gt; 0
     */
    public final void setVariance1(double variance) {
        setParameters(myMu1, variance, myMu2, myVar2, myRho);
    }

    /** Gets the first variance
     *
     * @return the first variance
     */
    public final double getVariance1() {
        return myVar1;
    }

    /** Sets the second mean
     *
     * @param mean the second mean
     */
    public final void setMean2(double mean) {
        setParameters(myMu1, myVar1, mean, myVar2, myRho);
    }

    /** Gets the second mean
     *
     * @return the second mean
     */
    public final double getMean2() {
        return myMu2;
    }

    /** Sets the 2nd variance
     *
     * @param variance of the distribution, must be &gt; 0
     */
    public final void setVariance2(double variance) {
        setParameters(myMu1, myVar1, myMu2, variance, myRho);
    }

    /** Gets the 2nd variance
     *
     * @return the 2nd variance
     */
    public final double getVariance2() {
        return myVar2;
    }

    /** Sets the correlation
     *
     * @param rho the correlation
     */
    public final void setCorrelation(double rho) {
        setParameters(myMu1, myVar1, myMu2, myVar2, rho);
    }

    /** Gets the correlation
     *
     * @return the correlation
     */
    public final double getCorrelation() {
        return myRho;
    }

    public void setAntitheticOption(boolean flag) {
        myBVN.setAntitheticOption(flag);
    }

    public void resetStartSubstream() {
        myBVN.resetStartSubstream();
    }

    public void resetStartStream() {
        myBVN.resetStartStream();
    }

    public void advanceToNextSubstream() {
        myBVN.advanceToNextSubstream();
    }

    public boolean getAntitheticOption() {
        return myBVN.getAntitheticOption();
    }

    /** Fills the supplied array with 2 values
     *  As a convenience also returns the array
     *
     * @param x Must be of size 2 or larger
     * @return bivariate values
     */
    public double[] getValues(double[] x) {
        // generate the bivariate normals
        x = myBVN.getValues(x);
        // transform them to bivariate lognormal
        x[0] = Math.exp(x[0]);
        x[1] = Math.exp(x[1]);
        return x;
    }

    /** Returns an array containing the bivariate pair
     *  x[0] = 1st marginal
     *  x[1] = 2nd marginal
     *
     * @return the values
     */
    public double[] getValues() {
        return (getValues(new double[2]));
    }

    public RNStreamIfc getRandomNumberGenerator() {
        return myBVN.getRandomNumberGenerator();
    }

    public void setRandomNumberGenerator(RNStreamIfc rng) {
        myBVN.setRandomNumberGenerator(rng);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bivariate LogNormal\n");
        sb.append("mean 1 = " + myMu1 + "\n");
        sb.append("variance 1 = " + myVar1 + "\n");
        sb.append("mean 2 = " + myMu2 + "\n");
        sb.append("variance 2 = " + myVar2 + "\n");
        sb.append("correlation = " + myRho + "\n");
        sb.append("Underlying bivariate normal\n");
        sb.append(myBVN);
        return sb.toString();
    }

    public static void main(String[] args) {
        // set the parameters of bivariate normal
        double m1 = -0.288879;
        double v1 = Math.pow(0.399334, 2.0);
        double m2 = 0.2965255;
        double v2 = Math.pow(0.7731198, 2.0);
        double r = 0.8512;

        BivariateNormal g = new BivariateNormal(m1, v1, m2, v2, r);
        System.out.println(g);

        System.out.println("");
        System.out.println("Computing bivariate lognormal parameters from bivariate normal parameters");
        double[] params = BivariateLogNormal.getBVLNParametersFromBVNParameters(m1, v1, m2, v2, r);
        System.out.println("Bivariate Lognormal Parameters: ");
        System.out.println("myMu1\tmyVar1\tmyMu2\tmyVar2\tmyRho");
        System.out.println(+params[0] + " " + params[1] + " " + params[2] + " " + params[3] + " " + params[4]);

        BivariateLogNormal bvln = new BivariateLogNormal(params);
        System.out.println(bvln);

        System.out.println("i\tX1\tX2");
        for (int i = 1; i <= 10; i++) {

            double[] pair = bvln.getValues();
            System.out.println(+i + " " + pair[0] + " " + pair[1]);

        }
        System.out.println("");



        System.out.println("done!");
    }
}
