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
import jsl.utilities.random.rvariable.LognormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/** Models the lognormal distribution
 *  This distribution is commonly use to model the time of a task
 *
 */
public class Lognormal extends Distribution implements ContinuousDistributionIfc,
        LossFunctionDistributionIfc, InverseCDFIfc, GetRVariableIfc {

    private double myMean;

    private double myVar;

    private double myNormalMu;

    private double myNormalSigma;

    /** Constructs a lognormal distribution with mean 1.0 and variance 1.0
     */
    public Lognormal() {
        this(1.0, 1.0, null);
    }

    /** Constructs a lognormal distribution with
     * mean = parameters[0] and variance = parameters[1]
     * @param parameters An array with the mean and variance
     */
    public Lognormal(double[] parameters) {
        this(parameters[0], parameters[1], null);
    }

    /** Constructs a lognormal distribution with
     * mean and variance.  Note: these parameters are the
     * actual mean and variance of the lognormal, not the underlying
     * normal as in many other implementations.
     *
     * @param mean must be &gt; 0
     * @param variance must be &gt; 0
     */
    public Lognormal(double mean, double variance) {
        this(mean, variance, null);
    }

    /** Constructs a lognormal distribution with
     * mean and variance.  Note: these parameters are the
     * actual mean and variance of the lognormal, not the underlying
     * normal as in many other implementations.
     *
     * @param mean must be &gt; 0
     * @param variance must be &gt; 0
     * @param name an optional name/lable
     */
    public Lognormal(double mean, double variance, String name) {
        super(name);
        setParameters(mean, variance);
    }

    @Override
    public final Lognormal newInstance() {
        return (new Lognormal(getParameters()));
    }

    @Override
    public final Interval getDomain(){
        return new Interval(0, Double.POSITIVE_INFINITY);
    }

    /** Sets the parameters of a lognormal distribution to
     * mean and variance.  Note: these parameters are the
     * actual mean and variance of the lognormal, not the underlying
     * normal as in many other implementations.
     *
     * @param mean must be &gt; 0
     * @param variance must be &gt; 0
     */
    public final void setParameters(double mean, double variance) {
        if (mean <= 0) {
            throw new IllegalArgumentException("Mean must be positive");
        }
        myMean = mean;

        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        myVar = variance;

        double d = myVar + myMean * myMean;
        double t = myMean * myMean;

        myNormalMu = Math.log((t) / Math.sqrt(d));
        myNormalSigma = Math.sqrt(Math.log(d / t));
    }

    @Override
    public final double getMean() {
        return myMean;
    }

    /**
     *
     * @return the 3rd moment
     */
    public final double getMoment3() {
        double calculatingM = (-(1.0 / 2.0) * Math.log((myVar / (myMean * myMean * myMean * myMean)) + 1.0));
        double calculatingS = Math.log((myVar / (myMean * myMean)) + (myMean * myMean));

        return Math.exp((3.0 * calculatingM) + (9.0 * calculatingS / 2.0));
    }

    /**
     *
     * @return the 4th moment
     */
    public final double getMoment4() {
        double calculatingM = (-(1.0 / 2.0) * Math.log((myVar / (myMean * myMean * myMean * myMean)) + 1.0));
        double calculatingS = Math.log((myVar / (myMean * myMean)) + (myMean * myMean));

        return Math.exp((4.0 * calculatingM) + (8.0 * calculatingS));
    }

    @Override
    public final double getVariance() {
        return myVar;
    }

    /** Provides a normal distribution with correct parameters
     *  as related to this lognormal distribution
     * @return The Normal distribution
     */
    public final Normal getNormal() {
        return (new Normal(myNormalMu, myNormalSigma*myNormalSigma));
    }

    /** The mean of the underlying normal
     *
     * @return mean of the underlying normal
     */
    public final double getNormalMean() {
        return myNormalMu;
    }

    /** The variance of the underlying normal
     *
     * @return variance of the underlying normal
     */
    public final double getNormalVariance() {
        return myNormalSigma * myNormalSigma;
    }

    /** The standard deviation of the underlying normal
     *
     * @return standard deviation of the underlying normal
     */
    public final double getNormalStdDev() {
        return myNormalSigma;
    }

    @Override
    public final double cdf(double x) {
        if (x <= 0) {
            return (0.0);
        }
        double z = (Math.log(x) - myNormalMu) / myNormalSigma;
        return (Normal.stdNormalCDF(z));
    }

    @Override
    public final double invCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be [0,1)");
        }

        if (p <= 0.0) {
            return 0.0;
        }

        if (p >= 1.0) {
            return Double.POSITIVE_INFINITY;
        }

        double z = Normal.stdNormalInvCDF(p);
        double x = z * myNormalSigma + myNormalMu;
        return (Math.exp(x));
    }

    @Override
    public final double pdf(double x) {
        if (x <= 0) {
            return (0.0);
        }
        double z = (Math.log(x) - myNormalMu) / myNormalSigma;
        return (Normal.stdNormalPDF(z) / x);
    }

    /** Gets the skewness of the distribution
     * @return the skewness
     */
    public final double getSkewness() {
        double t = Math.exp(myNormalSigma * myNormalSigma);
        return (Math.sqrt(t - 1.0) * (t + 2.0));
    }

    /** Gets the kurtosis of the distribution
     * @return the kurtosis
     */
    public final double getKurtosis() {
        double t1 = Math.exp(4.0 * myNormalSigma * myNormalSigma);
        double t2 = Math.exp(3.0 * myNormalSigma * myNormalSigma);
        double t3 = Math.exp(2.0 * myNormalSigma * myNormalSigma);
        return (t1 + 2.0 * t2 + 3.0 * t3 - 6.0);
    }

    /** Sets the parameters for the distribution
     * mean = parameters[0] and variance = parameters[1]
     * @param parameters an array of doubles representing the parameters for
     * the distribution
     */
    @Override
    public final void setParameters(double[] parameters) {
        setParameters(parameters[0], parameters[1]);
    }

    /** Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    @Override
    public final double[] getParameters() {
        double[] param = new double[2];
        param[0] = myMean;
        param[1] = myVar;
        return (param);
    }

    @Override
    public double firstOrderLossFunction(double x) {
        if (x <= 0.0) {
            return getMean() - x;
        }

        double z = (Math.log(x) - myNormalMu) / myNormalSigma;
        double t1 = Normal.stdNormalCDF(myNormalSigma - z);
        double t2 = Normal.stdNormalCDF(-z);
        double f1 = getMean() * t1 - x * t2;
        return f1;
    }

    @Override
    public double secondOrderLossFunction(double x) {
        double m = getMean();
        double m2 = getVariance() + m * m;
        if (x <= 0.0) {
            double f2 = 0.5 * (m2 - 2.0 * x * m + x * x);
            return f2;
        } else {
            double z = (Math.log(x) - myNormalMu) / myNormalSigma;
            double t1 = Normal.stdNormalCDF(2.0 * myNormalSigma - z);
            double t2 = Normal.stdNormalCDF(myNormalSigma - z);
            double t3 = Normal.stdNormalCDF(-z);
            double f2 = 0.5 * (m2 * t1 - 2.0 * x * m * t2 + x * x * t3);
            return f2;
        }
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new LognormalRV(getMean(), getVariance(), rng);
    }
}
