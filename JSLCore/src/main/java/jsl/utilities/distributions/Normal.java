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
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/** Models normally distributed random variables
 *
 */
public class Normal extends Distribution implements ContinuousDistributionIfc,
        LossFunctionDistributionIfc, InverseCDFIfc, GetRVariableIfc {

    private double myMean;

    private double myVar;

    private double myStdDev;

    private static final double baseNorm = Math.sqrt(2.0 * Math.PI);

    private static final double errorFunctionConstant = 0.2316419;

    private static final double[] coeffs = {0.31938153, -0.356563782, 1.781477937, -1.821255978, 1.330274429};

    private static final double[] a = {-3.969683028665376e+01, 2.209460984245205e+02,
        -2.759285104469687e+02, 1.383577518672690e+02,
        -3.066479806614716e+01, 2.506628277459239e+00};

    private static final double[] b = {-5.447609879822406e+01, 1.615858368580409e+02,
        -1.556989798598866e+02, 6.680131188771972e+01, -1.328068155288572e+01};

    private static final double[] c = {-7.784894002430293e-03, -3.223964580411365e-01,
        -2.400758277161838e+00, -2.549732539343734e+00,
        4.374664141464968e+00, 2.938163982698783e+00};

    private static final double[] d = {7.784695709041462e-03, 3.224671290700398e-01,
        2.445134137142996e+00, 3.754408661907416e+00};

    /** Constructs a normal distribution with mean 0.0 and variance 1.0
     */
    public Normal() {
        this(0.0, 1.0, null);
    }

    /** Constructs a normal distribution with
     * mean = parameters[0] and variance = parameters[1]
     * @param parameters An array with the mean and variance
     */
    public Normal(double[] parameters) {
        this(parameters[0], parameters[1], null);
    }

    /** Constructs a normal distribution with mean and variance.
     *
     * @param mean of the distribution
     * @param variance must be &gt; 0
     */
    public Normal(double mean, double variance) {
        this(mean, variance, null);
    }

    /** Constructs a normal distribution with mean and variance.
     *
     * @param mean of the distribution
     * @param variance must be &gt; 0
     * @param name an optional name/label
     */
    public Normal(double mean, double variance, String name) {
        super(name);
        setMean(mean);
        setVariance(variance);
    }

    @Override
    public final Normal newInstance() {
        return (new Normal(getParameters()));
    }

    @Override
    public final Interval getDomain(){
        return new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /** Sets the mean of this normal distribution
     *
     * @param mean of the distribution
     */
    public final void setMean(double mean) {
        myMean = mean;
    }

    @Override
    public final double getMean() {
        return myMean;
    }

    /** Sets the variance of this normal distribution
     *
     * @param variance of the distribution, must be &gt; 0
     */
    public final void setVariance(double variance) {
        if (variance <= 0) {
            throw new IllegalArgumentException("Variance must be positive");
        }
        myVar = variance;
        myStdDev = getStandardDeviation();
    }

    @Override
    public final double getVariance() {
        return myVar;
    }

    /** Computes the cumulative distribution function for a standard
     *  normal distribution
     *  from Abramovitz  and Stegun, see also Didier H. Besset
     *  Object-oriented Implementation of Numerical Methods, Morgan-Kaufmann (2001)
     *
     * @param z the z-ordinate to be evaluated
     * @return the P(Z&lt;=z) for standard normal
     */
    public static double stdNormalCDFAbramovitzAndStegun(double z) {

        if (z == 0) {
            return 0.5;
        } else if (z > 0) {
            return (1 - stdNormalCDFAbramovitzAndStegun(-z));
        }

        double t = 1 / (1 - errorFunctionConstant * z);

        double phi = coeffs[0] + t * (coeffs[1] + t * (coeffs[2] + t * (coeffs[3] + t * coeffs[4])));

        return (t * phi * stdNormalPDF(z));
    }

    /** Computes the cumulative distribution function for a standard
     *  normal distribution using Taylor approximation.
     *
     *  The approximation is accurate to absolute error less than 8 * 10^(-16).
     *  *  Reference: Evaluating the Normal Distribution by George Marsaglia.
     *  *  http://www.jstatsoft.org/v11/a04/paper
     *
     * @param z the z-ordinate to be evaluated
     * @return the P(Z&lt;=z) for standard normal
     */
    public static double stdNormalCDF(double z) {
        if (z < -8.0) return 0.0;
        if (z >  8.0) return 1.0;
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum  = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * stdNormalPDF(z);
    }

    /** Computes the pdf function for a standard normal distribution
     *  from Abramovitz and Stegun, see also Didier H. Besset
     *  Object-oriented Implementation of Numerical Methods, Morgan-Kaufmann (2001)
     *
     * @param z the z-ordinate to be evaluated
     * @return the f(z) for standard normal
     */
    public static double stdNormalPDF(double z) {
        return (Math.exp(-0.5 * z * z) / baseNorm);
    }

    /** Computes the inverse cumulative distribution function for a standard
     *  normal distribution
     * see, W. J. Cody, Rational Chebyshev approximations for the error function
     * Math. Comp. pp 631-638
     * this is without the extra refinement and has relative error of 1.15e-9
     * {@literal http://www.math.uio.no/~jacklam/notes/invnorm/ }
     * @param p the probability to be evaluated, p must be within [0,1]
     * p = 0.0 returns Double.NEGATIVE_INFINTITY
     * p = 1.0 returns Double.POSITIVE_INFINITY
     * @return the "z" value associated with the p
     */
    public static double stdNormalInvCDF(double p) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be (0,1)");
        }

        if (p <= 0.0) {
            return Double.NEGATIVE_INFINITY;
        }

        if (p >= 1.0) {
            return Double.POSITIVE_INFINITY;
        }

        // define the breakpoints
        double plow = 0.02425;
        double phigh = 1 - plow;

        double r = 0.0;
        double q = 0.0;
        double z = 0.0;
        double x = 0.0;
        double y = 0.0;

        if (p < plow) {// rational approximation for the lower region
            q = Math.sqrt(-2 * Math.log(p));
            x = (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]);
            y = ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
            z = x / y;
            return (z);
        }

        if (phigh < p) {// rational approximation for upper region
            q = Math.sqrt(-2 * Math.log(1.0 - p));
            x = (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]);
            y = ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1);
            z = -x / y;
            return (z);
        }

        // rational approximation for central region
        q = p - 0.5;
        r = q * q;
        x = (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q;
        y = (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1);
        z = x / y;

        return (z);
    }

    /** Computes the complementary cumulative probability for the standard normal
     * distribution function for given value of z
     * @param z The value to be evaluated
     * @return The probability, 1-P{X&lt;=z}
     */
    public static double stdNormalComplementaryCDF(double z) {
        return (1.0 - stdNormalCDF(z));
    }

    /** Computes the first order loss function for the standard normal
     * distribution function for given value of x, G1(z) = E[max(Z-z,0)]
     * @param z The value to be evaluated
     * @return The loss function value, E[max(Z-z,0)]
     */
    public static double stdNormalFirstOrderLossFunction(double z) {
        return (-z * stdNormalComplementaryCDF(z) + stdNormalPDF(z));
    }

    /** Computes the 2nd order loss function for the standard normal
     * distribution function for given value of z, G2(z) = (1/2)E[max(Z-z,0)*max(Z-z-1,0)]
     * @param z The value to be evaluated
     * @return The loss function value, (1/2)E[max(Z-z,0)*max(Z-z-1,0)]
     */
    public static double stdNormalSecondOrderLossFunction(double z) {
        return (0.5 * ((z * z + 1.0) * stdNormalComplementaryCDF(z) - z * stdNormalPDF(z)));
    }

    @Override
    public final double cdf(double x) {
        return (stdNormalCDF((x - myMean) / myStdDev));
    }

    @Override
    public final double pdf(double x) {
        double z = (x - myMean)/ myStdDev;
        return (stdNormalPDF(z)/myStdDev);
    }

    @Override
    public final double invCDF(double p) {
        double z = stdNormalInvCDF(p);
        return (z * myStdDev + myMean);
    }

    /** Gets the kurtosis of the distribution
     * @return the kurtosis
     */
    public final double getKurtosis() {
        return (0.0);
    }

    /** Gets the skewness of the distribution
     * @return the skewness
     */
    public final double getSkewness() {
        return (0.0);
    }

    @Override
    public double complementaryCDF(double x) {
        return (stdNormalComplementaryCDF((x - myMean) / myStdDev));
    }

    @Override
    public double firstOrderLossFunction(double x) {
        return (myStdDev * stdNormalFirstOrderLossFunction((x - myMean) / myStdDev));
    }

    @Override
    public double secondOrderLossFunction(double x) {
        return (myVar * stdNormalSecondOrderLossFunction((x - myMean) / myStdDev));
    }

    /** Sets the parameters for the distribution
     * mean = parameters[0] and variance = parameters[1]
     * @param parameters an array of doubles representing the parameters for
     * the distribution
     */
    @Override
    public void setParameters(double[] parameters) {
        setMean(parameters[0]);
        setVariance(parameters[1]);
    }

    /** Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    @Override
    public double[] getParameters() {
        double[] param = new double[2];
        param[0] = getMean();
        param[1] = getVariance();
        return (param);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new NormalRV(getMean(), getVariance(), rng);
    }
}
