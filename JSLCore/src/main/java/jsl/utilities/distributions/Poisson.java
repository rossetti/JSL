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

import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.PoissonRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 * Represents a Poisson random variable. A Poisson random
 * variable represents the number of occurrences of an event with time or space.
 */
public class Poisson extends Distribution implements DiscreteDistributionIfc, LossFunctionDistributionIfc, GetRVariableIfc {

    /**
     * Used in the calculation of the incomplete gamma function
     */
    public final static int DEFAULT_MAX_ITERATIONS = 5000;

    /**
     * indicates whether or not pmf and cdf calculations are
     * done by recursive (iterative) algorithm based on logarithms
     * or via beta incomplete function and binomial coefficients.
     */
    private boolean myRecursiveAlgoFlag = true;

    /**
     * the mean (parameter) of the poisson
     */
    private double myMean;

    /**
     * Constructs a Poisson with mean rate parameter 1.0
     */
    public Poisson() {
        this(1.0, null);
    }

    /**
     * Constructs a Poisson using the supplied parameter
     *
     * @param parameters A array that holds the parameters, parameters[0] should be the mean rate
     */
    public Poisson(double[] parameters) {
        this(parameters[0], null);
    }

    /**
     * Constructs a Poisson using the supplied parameter
     *
     * @param mean the mean rate
     */
    public Poisson(double mean) {
        this(mean, null);
    }

    /**
     * Constructs a Poisson using the supplied parameter
     *
     * @param mean the mean rate
     * @param name an optional label/name
     */
    public Poisson(double mean, String name) {
        super(name);
        setMean(mean);
    }

    @Override
    public final Poisson newInstance() {
        return (new Poisson(getParameters()));
    }

    @Override
    public final double getMean() {
        return (myMean);
    }

    @Override
    public final double getVariance() {
        return (myMean);
    }

    /**
     * Sets the mean of the Poisson distribution
     *
     * @param mean the mean rate, mean must be &gt; 0
     */
    public final void setMean(double mean) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }
        myMean = mean;
    }

    /**
     * @return the mode of the distribution
     */
    public final int getMode() {
        if (Math.floor(myMean) == myMean) {
            return (int) myMean - 1;
        } else {
            return (int) Math.floor(myMean);
        }
    }

    public final double cdf(int x) {
        return poissonCDF(x, myMean, myRecursiveAlgoFlag);
    }

    public final double cdf(double x) {
        return (cdf((int) x));
    }

    public static boolean canMatchMoments(double... moments) {
        if (moments.length < 1) {
            throw new IllegalArgumentException("Must provide a mean.");
        }
        double mean = moments[0];
        if (mean > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static double[] getParametersFromMoments(double... moments) {
        if (!canMatchMoments(moments)) {
            throw new IllegalArgumentException("Mean must be positive. You provided " + moments[0] + ".");
        }
        return new double[]{moments[0]};
    }

    public static Poisson createFromMoments(double... moments) {
        double[] prob = getParametersFromMoments(moments);
        return new Poisson(prob);
    }

    @Override
    public final double firstOrderLossFunction(double x) {
        double mu = getMean();
        if (x < 0.0) {
            return (Math.floor(Math.abs(x)) + mu);
        } else if (x > 0.0) {
            double g0 = complementaryCDF(x);
            double g = pmf(x);
            double g1 = -1.0 * (x - mu) * g0 + mu * g;
            return (g1);
        } else // x== 0.0
        {
            return mu;
        }
    }

    @Override
    public final double secondOrderLossFunction(double x) {
        double mu = getMean();
        double sbm = 0.5 * (mu * mu);// 1/2 the 2nd binomial moment
        if (x < 0.0) {
            double s = 0.0;
            for (int y = 0; y > x; y--) {
                s = s + firstOrderLossFunction(y);
            }
            return (s + sbm);
        } else if (x > 0.0) {
            double g0 = complementaryCDF(x);
            double g = pmf(x);
            double g2 = 0.5 * (((x - mu) * (x - mu) + x) * g0 - mu * (x - mu) * g);
            return (g2);
        } else // x == 0.0
        {
            return sbm;
        }
    }

    public double thirdOrderLossFunction(double x) {
        double term1 = Math.pow(myMean, 3) * this.complementaryCDF(x - 3);
        double term2 = 3 * myMean * myMean * x * this.complementaryCDF(x - 2);
        double term3 = 3 * myMean * x * (x + 1) * this.complementaryCDF(x - 1);
        double term4 = x * (x + 1) * (x + 2) * this.complementaryCDF(x);
        return (term1 - term2 + term3 - term4) / 3.0;
    }

    @Override
    public final double invCDF(double prob) {
        if ((prob < 0.0) || (prob > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + prob + " Probability must be [0,1]");
        }

        if (prob <= 0.0) {
            return 0.0;
        }

        if (prob >= 1.0) {
            return Double.POSITIVE_INFINITY;
        }

        return poissonInvCDF(prob, myMean, myRecursiveAlgoFlag);
    }

    public final double pmf(int x) {
        return poissonPMF(x, myMean, myRecursiveAlgoFlag);
    }

    /**
     * If x is not and integer value, then the probability must be zero
     * otherwise pmf(int x) is used to determine the probability
     *
     * @param x the value to evaluate
     * @return the probability at the point
     */
    public final double pmf(double x) {
        if (Math.floor(x) == x) {
            return pmf((int) x);
        } else {
            return 0.0;
        }
    }

    /**
     * Sets the parameters for the distribution
     * parameters[0] should be the mean rate
     *
     * @param parameters an array of doubles representing the parameters for
     *                   the distribution
     */
    @Override
    public final void setParameters(double[] parameters) {
        setMean(parameters[0]);
    }

    /**
     * Gets the parameters for the distribution
     *
     * @return Returns an array of the parameters for the distribution
     */
    @Override
    public final double[] getParameters() {
        double[] param = new double[1];
        param[0] = myMean;
        return (param);
    }

    /**
     * Computes the probability mass function at j using a
     * recursive (iterative) algorithm using logarithms
     *
     * @param j    the value to evaluate
     * @param mean the mean
     * @return the PMF value
     */
    public static double recursivePMF(int j, double mean) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }

        if (j < 0) {
            return 0.0;
        }

        double lnp = -mean;
        double lnmu = Math.log(mean);

        for (int i = 1; i <= j; i++) {
            lnp = lnmu - Math.log(i) + lnp;
        }

        if (lnp > 0) {
            throw new IllegalArgumentException("Term overflow will cause probability > 1");
        }

        if (lnp <= JSLMath.getSmallestExponentialArgument()) {
            return 0.0;
        }

        return Math.exp(lnp);
    }

    /**
     * Computes the cdf at j using a
     * recursive (iterative) algorithm using logarithms
     *
     * @param j    the value to evaluate
     * @param mean the mean
     * @return the CDF value
     */
    public static double recursiveCDF(int j, double mean) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }

        if (j < 0) {
            return 0.0;
        }

        double lnp = -mean;
        if (j == 0) {
            if (lnp <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            } else {
                return Math.exp(lnp);
            }
        }

        double lnmu = Math.log(mean);
        double sum = 0.0;
        if (lnp <= JSLMath.getSmallestExponentialArgument()) {
            sum = 0.0;
        } else {
            sum = Math.exp(lnp);
        }

        for (int i = 1; i <= j; i++) {
            lnp = lnmu - Math.log(i) + lnp;
            if (lnp <= JSLMath.getSmallestExponentialArgument()) {
                sum = sum + 0.0;
            } else {
                sum = sum + Math.exp(lnp);
            }
        }

        return sum;
    }

    /**
     * Allows static computation of prob mass function
     * assumes that distribution's range is {0,1, ...}
     * Uses the recursive logarithmic algorithm
     *
     * @param j    value for which prob is needed
     * @param mean of the distribution
     * @return the PMF value
     */
    public static double poissonPMF(int j, double mean) {
        return poissonPMF(j, mean, true);
    }

    /**
     * Allows static computation of prob mass function
     * assumes that distribution's range is {0,1, ...}
     *
     * @param j         value for which prob is needed
     * @param mean      of the distribution
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the PMF value
     */
    public static double poissonPMF(int j, double mean, boolean recursive) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }

        if (j < 0) {
            return 0.0;
        }

        if (recursive) {
            return recursivePMF(j, mean);
        }

        if (j == 0) {
            double lnp = -mean;
            if (lnp <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            } else {
                return Math.exp(lnp);
            }
        }

        double lnp = j * Math.log(mean) - mean - Math.log(j) - Gamma.logGammaFunction(j);
        if (lnp <= JSLMath.getSmallestExponentialArgument()) {
            return 0.0;
        }

        return Math.exp(lnp);

    }

    /**
     * Allows static computation of cdf
     * assumes that distribution's range is {0,1, ...}
     * Uses the recursive logarithmic algorithm
     *
     * @param j    value for which prob is needed
     * @param mean of the distribution
     * @return the cdf value
     */
    public static double poissonCDF(int j, double mean) {
        return poissonPMF(j, mean, true);
    }

    /**
     * Allows static computation of cdf
     * assumes that distribution's range is {0,1, ...}
     * false indicated the use of the incomplete gamma function
     * It yields about 7 digits of accuracy, the recursive
     * algorithm has more accuracy
     *
     * @param j         value for which prob is needed
     * @param mean      of the distribution
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the cdf value
     */
    public static double poissonCDF(int j, double mean, boolean recursive) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }

        if (j < 0) {
            return 0.0;
        }

        if (recursive) {
            return recursiveCDF(j, mean);
        }

        double eps = JSLMath.getDefaultNumericalPrecision();
        double ccdf = Gamma.incompleteGammaFunction(mean, j + 1, DEFAULT_MAX_ITERATIONS, eps);

        return 1.0 - ccdf;

    }

    /**
     * Allows static computation of complementary cdf function
     * assumes that distribution's range is {0,1, ...}
     * Uses the recursive logarithmic algorithm
     *
     * @param j    value for which ccdf is needed
     * @param mean of the distribution
     * @return the complimentary CDF value
     */
    public static double poissonCCDF(int j, double mean) {
        return poissonCCDF(j, mean, true);
    }

    /**
     * Allows static computation of complementary cdf function
     * assumes that distribution's range is {0,1, ...}
     *
     * @param j         value for which ccdf is needed
     * @param mean      of the distribution
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the complimentary CDF value
     */
    public static double poissonCCDF(int j, double mean, boolean recursive) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }

        if (j < 0) {
            return (1.0);
        }

        return (1.0 - poissonCDF(j, mean, recursive));

    }

    /**
     * Computes the first order loss function for the
     * distribution function for given value of x, G1(x) = E[max(X-x,0)]
     *
     * @param x         The value to be evaluated
     * @param mean      of the distribution
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return The loss function value, E[max(X-x,0)]
     */
    public static double poissonLF1(double x, double mean, boolean recursive) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }
        if (x < 0.0) {
            return (Math.floor(Math.abs(x)) + mean);
        } else if (x > 0.0) {
            double g0 = poissonCCDF((int) x, mean, recursive);
            double g = poissonPMF((int) x, mean, recursive);
            double g1 = -1.0 * (x - mean) * g0 + mean * g;
            return (g1);
        } else // x== 0.0
        {
            return mean;
        }
    }

    /**
     * Computes the 2nd order loss function for the
     * distribution function for given value of x, G2(x) = (1/2)E[max(X-x,0)*max(X-x-1,0)]
     *
     * @param x         The value to be evaluated
     * @param mean      of the distribution
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return The loss function value, (1/2)E[max(X-x,0)*max(X-x-1,0)]
     */
    public static double poissonLF2(double x, double mean, boolean recursive) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }
        double sbm = 0.5 * (mean * mean);// 1/2 the 2nd binomial moment
        if (x < 0.0) {
            double s = 0.0;
            for (int y = 0; y > x; y--) {
                s = s + poissonLF1(y, mean, recursive);
            }
            return (s + sbm);
        } else if (x > 0.0) {
            double g0 = poissonCCDF((int) x, mean, recursive);
            double g = poissonPMF((int) x, mean, recursive);
            double g2 = 0.5 * (((x - mean) * (x - mean) + x) * g0 - mean * (x - mean) * g);
            return (g2);
        } else // x == 0.0
        {
            return sbm;
        }
    }

    /**
     * Returns the quantile associated with the supplied probablity, x
     * assumes that distribution's range is {0,1, ...}
     * Uses the recursive logarithmic algorithm
     *
     * @param p    The probability that the quantile is needed for
     * @param mean of the distribution
     * @return the quantile associated with the supplied probablity
     */
    public static int poissonInvCDF(double p, double mean) {
        return poissonInvCDF(p, mean, true);
    }

    /**
     * Returns the quantile associated with the supplied probablity, x
     * assumes that distribution's range is {0,1, ...}
     *
     * @param p         The probability that the quantile is needed for
     * @param mean      of the distribution
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the quantile associated with the supplied probablity
     */
    public static int poissonInvCDF(double p, double mean, boolean recursive) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }

        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be [0,1]");
        }

        if (p <= 0.0) {
            return 0;
        }

        if (p >= 1.0) {
            return Integer.MAX_VALUE;
        }

        // get approximate quantile from normal approximation
        // and Cornish-Fisher expansion
        int start = invCDFViaNormalApprox(p, mean);
        double cdfAtStart = poissonCDF(start, mean, recursive);

        //System.out.println("start = " + start);
        //System.out.println("cdfAtStart = " + cdfAtStart);
        //System.out.println("p = " + p);
        //System.out.println();

        if (p >= cdfAtStart) {
            return searchUpCDF(p, mean, start, cdfAtStart, recursive);
        } else {
            return searchDownCDF(p, mean, start, cdfAtStart, recursive);
        }
    }

    /**
     * @param p          the probability to search
     * @param mean       the mean of the distribution
     * @param start      the starting point of the search
     * @param cdfAtStart the CDF at the starting point
     * @param recursive  true indicates that the recursive logarithmic algorithm should be used
     * @return the found value
     */
    protected static int searchUpCDF(double p, double mean,
                                     int start, double cdfAtStart, boolean recursive) {
        int i = start;
        double cdf = cdfAtStart;
        while (p > cdf) {
            i++;
            cdf = cdf + poissonPMF(i, mean, recursive);
        }
        return i;
    }

    /**
     * @param p          the probability to search
     * @param mean       the mean of the distribution
     * @param start      the starting point of the search
     * @param cdfAtStart the CDF at the starting point
     * @param recursive  true indicates that the recursive logarithmic algorithm should be used
     * @return the found value
     */
    protected static int searchDownCDF(double p, double mean,
                                       int start, double cdfAtStart, boolean recursive) {
        int i = start;
        double cdfi = cdfAtStart;
        while (i > 0) {
            double cdfim1 = cdfi - poissonPMF(i, mean, recursive);
            if ((cdfim1 <= p) && (p < cdfi)) {
                if (JSLMath.equal(cdfim1, p))// must handle invCDF(cdf(x) = x)
                {
                    return i - 1;
                } else {
                    return i;
                }
            }
            cdfi = cdfim1;
            i--;
        }
        return i;
    }

    /**
     * @param p    the probability to search
     * @param mean the mean of the distribution
     * @return the inverse via a normal approximation
     */
    protected static int invCDFViaNormalApprox(double p, double mean) {
        if ((mean <= 0.0)) {
            throw new IllegalArgumentException("Mean must be > 0)");
        }
        /* y := approx.value (Cornish-Fisher expansion) :  */
        double z = Normal.stdNormalInvCDF(p);
        double sigma = Math.sqrt(mean);
        double g = 1.0 / sigma;
        double y = Math.floor(mean + sigma * (z + g * (z * z - 1.0) / 6.0) + 0.5);
        if (y < 0) {
            return 0;
        } else {
            return (int) y;
        }
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new PoissonRV(getMean(), rng);
    }
}
