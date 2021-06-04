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
import jsl.utilities.random.rvariable.BinomialRV;
import jsl.utilities.random.rvariable.GetRVariableIfc;
import jsl.utilities.random.rvariable.RVariableIfc;

import java.util.Objects;

/** Represents a Binomial distribution. A binomial random
 * variable represents the number of successes out of n trials
 * with each trial having a probability (p) of success.
 *
 * The pmf and cdf are computed using an iterative algorithm that
 * relies on logarithms to avoid the large factorial associated
 * with binomial coefficients.  Thus it is suitable for
 * reasonably large values for the number of trials.
 * This appears to give about 14 decimal places of accuracy when compared to
 * the statistical package R.
 * The inverse CDF uses a normal approximation with a Cornish-Fisher
 * expansion to approximate the quantile and then a search
 * via the inverse transform method.  
 */
public class Binomial extends Distribution implements DiscreteDistributionIfc, LossFunctionDistributionIfc,
        GetRVariableIfc {

    /** The probability of success
     *
     */
    private double myProbSuccess;

    /** The number of trials
     *
     */
    private int myNumTrials;

    /** indicates whether or not pmf and cdf calculations are
     *  done by recursive (iterative) algorithm based on logarithms
     *  or via beta incomplete function and binomial coefficients.
     *
     */
    private boolean myRecursiveAlgoFlag = true;

    // constructors
    /** Constructs a Binomial with n=1, p=0.5
     */
    public Binomial() {
        this(0.5, 1, null);
    }

    /** Constructs a Binomial using the supplied parameters
     * @param parameters A array that holds the parameters, parameter[0] should be
     * the probability (p) and parameter[1] should be
     * the number of trials
     */
    public Binomial(double[] parameters) {
        this(parameters[0], (int) parameters[1], null);
    }

    /** Constructs a binomial with p probability of success
     * based on n trials
     * @param prob The success probability
     * @param numTrials The number of trials
     */
    public Binomial(double prob, int numTrials) {
        this(prob, numTrials, null);
    }

    /** Constructs a binomial with p probability of success
     * based on n trials
     * @param prob The success probability
     * @param numTrials The number of trials
     * @param name an optional label/name
     */
    public Binomial(double prob, int numTrials, String name) {
        super(name);
        setProbabilityOfSuccess(prob);
        setNumberOfTrials(numTrials);
    }

    /** indicates whether or not pmf and cdf calculations are
     *  done by recursive (iterative) algorithm based on logarithms
     *  or via beta incomplete function and binomial coefficients.
     *
     * @return true if the recursive algo is being used
     */
    public final boolean getRecursiveAlgorithmFlag() {
        return myRecursiveAlgoFlag;
    }

    /** indicates whether or not pmf and cdf calculations are
     *  done by recursive (iterative) algorithm based on logarithms
     *  or via beta incomplete function and binomial coefficients.
     *
     * @param flag true means recursive algorithm is used
     */
    public final void setRecursiveAlgorithmFlag(boolean flag) {
        myRecursiveAlgoFlag = flag;
    }

    @Override
    public final Binomial newInstance() {
        return (new Binomial(getParameters()));
    }

    /** Gets the success probability
     * @return The success probability
     */
    public final double getProb() {
        return (myProbSuccess);
    }

    /** Gets the number of trials
     * @return the number of trials
     */
    public final int getTrials() {
        return (myNumTrials);
    }

    /** Sets the number of trials and success probability
     * @param prob the success probability
     * @param numTrials the number of trials
     */
    public final void setParameters(double prob, int numTrials) {
        setProbabilityOfSuccess(prob);
        setNumberOfTrials(numTrials);
    }

    @Override
    public final double cdf(double x) {
        return cdf((int) x);
    }

    public final double cdf(int x) {
        return binomialCDF(x, myNumTrials, myProbSuccess, myRecursiveAlgoFlag);
    }

    @Override
    public final double invCDF(double prob) {
        return binomialInvCDF(prob, myNumTrials, myProbSuccess, myRecursiveAlgoFlag);
    }

    @Override
    public final double getMean() {
        return (myNumTrials * myProbSuccess);
    }

    @Override
    public final double getVariance() {
        return (myNumTrials * myProbSuccess * (1.0 - myProbSuccess));
    }

    private void setNumberOfTrials(int numTrials) {
        if (numTrials <= 0) {
            throw new IllegalArgumentException("Number of trials must be >= 1");
        }
        myNumTrials = numTrials;
    }

    private void setProbabilityOfSuccess(double prob) {
        if ((prob <= 0.0) || (prob >= 1.0)) {
            throw new IllegalArgumentException("Probability must be in (0,1)");
        }
        myProbSuccess = prob;
    }

    /** If x is not and integer value, then the probability must be zero
     *  otherwise pmf(int x) is used to determine the probability
     *
     * @param x value to evaluate
     * @return the associated probability
     */
    @Override
    public final double pmf(double x) {
        if (Math.floor(x) == x) {
            return pmf((int) x);
        } else {
            return 0.0;
        }
    }

    /**
     *
     * @param x value to evaluate
     * @return the associated probability
     */
    public final double pmf(int x) {
        return binomialPMF(x, myNumTrials, myProbSuccess, myRecursiveAlgoFlag);
    }

    /**
     * @param parameters A array that holds the parameters, parameter[0] should be
     * the probability (p) and parameter[1] should be
     * the number of trials
     */
    @Override
    public void setParameters(double[] parameters) {
        setParameters(parameters[0], (int) parameters[1]);
    }

    /**
     *  @return A array that holds the parameters, parameter[0] should be
     * the probability (p) and parameter[1] should be
     * the number of trials
     */
    @Override
    public double[] getParameters() {
        double[] param = new double[2];
        param[0] = myProbSuccess;
        param[1] = myNumTrials;
        return (param);
    }

    /**
     *
     * @param moments the mean is in element 0 and the variance is in element 1, must not be null
     * @return true if n and p can be set to match te moments
     */
    public static boolean canMatchMoments(double... moments) {
        Objects.requireNonNull(moments, "The moment array was null");
        if (moments.length < 2) {
            throw new IllegalArgumentException("Must provide a mean and a variance. You provided " + moments.length + " moments.");
        }
        double mean = moments[0];
        double var = moments[1];
        boolean validN = var >= mean * (1 - mean);

        if (var > 0 && var < mean && validN) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param moments the mean is in element 0 and the variance is in element 1, must not be null
     * @return the values of n and p that match the moments with p as element 0 and n as element 1
     */
    public static double[] getParametersFromMoments(double... moments) {
        if (!canMatchMoments(moments)) {
            throw new IllegalArgumentException("Mean and variance must be positive, mean > variance, and variance >= mean*(1-mean). Your mean: " + moments[0] + " and variance: " + moments[1]);
        }
        double mean = moments[0];
        double var = moments[1];
        int n = (int) (mean * mean / (mean - var) + 0.5);
        double p = mean / n;
        return new double[]{p, n};
    }

    /**
     *
     * @param moments the mean is in element 0 and the variance is in element 1, must not be null
     * @return if the moments can be matched a properly configured Binomial is returned
     */
    public static Binomial createFromMoments(double... moments) {
        double[] param = getParametersFromMoments(moments);
        return new Binomial(param);
    }

    /** Computes the first order loss function for the
     * distribution function for given value of x, G1(x) = E[max(X-x,0)]
     * @param x The value to be evaluated
     * @return The loss function value, E[max(X-x,0)]
     */
    @Override
    public final double firstOrderLossFunction(double x) {
        if (x < 0.0) {
            return (Math.floor(Math.abs(x)) + getMean());
        } else if (x > 0.0) {
            return (getMean() - sumCCDF(x));
        } else // x== 0.0
        {
            return getMean();
        }
    }

    /** Computes the 2nd order loss function for the
     * distribution function for given value of x, G2(x) = (1/2)E[max(X-x,0)*max(X-x-1,0)]
     * @param x The value to be evaluated
     * @return The loss function value, (1/2)E[max(X-x,0)*max(X-x-1,0)]
     */
    @Override
    public final double secondOrderLossFunction(double x) {
        double mu = getMean();
        double g2 = 0.5 * (getVariance() + mu * mu - mu);// 1/2 the 2nd binomial moment

        if (x < 0.0) {
            double s = 0.0;
            for (int y = 0; y > x; y--) {
                s = s + firstOrderLossFunction(y);
            }
            return (s + g2);
        } else if (x > 0.0) {
            return (g2 - sumFirstLoss(x));
        } else // x == 0.0
        {
            return g2;
        }
    }

    /** Returns the sum of the complementary CDF
     *  from 0 up to but not including x
     *
     * @param x the value to evaluate
     * @return the sum of the complementary CDF
     */
    private double sumCCDF(double x) {
        if (x <= 0.0) {
            return 0.0;
        }

        if (x > getTrials()) {
            x = getTrials();
        }

        double c = 0.0;
        for (int i = 0; i < x; i++) {
            c = c + complementaryCDF(i);
        }
        return c;
    }

    /** Sums the first order loss function from
     *  1 up to and including x. x is interpreted
     *  as an integer
     *
     * @param x the x to evaluate
     * @return the first order loss functio
     */
    private double sumFirstLoss(double x) {
        int n = (int) x;
        double sum = 0.0;
        for (int i = 1; i <= n; i++) {
            sum = sum + firstOrderLossFunction(i);
        }
        return sum;
    }

    /** Computes the probability mass function at j using a
     *  recursive (iterative) algorithm using logarithms
     *
     * @param j the value to evaluate
     * @param n number of trials
     * @param p success probability
     * @return probability of j
     */
    public static double recursivePMF(int j, int n, double p) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trials must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return 0.0;
        }

        if (j > n) {
            return 0.0;
        }

        //TODO figure out why this doesn't work
/*       // use BN(j;n,p) = BN(n-j;n,q) to save iterations
        if (j > (n / 2)) {
        j = n - j;
        p = 1.0 - p;
        }
         */
        double q = 1.0 - p;
        double lnq = Math.log(q);
        double f = n * lnq;

        if (j == 0) {
            if (f <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            } else {
                return Math.exp(f);
            }
        }

        double lnp = Math.log(p);

        if (j == n) {
            double g = n * lnp;
            if (g <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            } else {
                return Math.exp(g);
            }
        }

        double c = lnp - lnq;
        for (int i = 1; i <= j; i++) {
            f = c + Math.log(n - i + 1.0) - Math.log(i) + f;
        }

        if (f >= JSLMath.getLargestExponentialArgument()) {
            throw new IllegalArgumentException("Term overflow due to input parameters");
        }

        if (f <= JSLMath.getSmallestExponentialArgument()) {
            return 0.0;
        }

        return Math.exp(f);
    }

    /** Computes the probability mass function at j using a
     *  recursive (iterative) algorithm using logarithms
     *
     * @param j the value to evaluate
     * @param n number of trials
     * @param p success probability
     * @return cumulative probability of j
     */
    public static double recursiveCDF(int j, int n, double p) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trials must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return 0.0;
        }

        if (j >= n) {
            return 1.0;
        }

        //TODO figure out why this doesn't work
/*
        // use BN(j;n,p) = BN(n-j;n,q) to save iterations
        if (j > (n / 2)) {
        j = n - j;
        p = 1.0 - p;
        }
         */

        double q = 1.0 - p;
        double lnq = Math.log(q);
        double f = n * lnq;

        if (j == 0) {
            if (f <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            } else {
                return Math.exp(f);
            }
        }

        double lnp = Math.log(p);
        double c = lnp - lnq;
        double sum = Math.exp(f);

        for (int i = 1; i <= j; i++) {
            f = c + Math.log(n - i + 1.0) - Math.log(i) + f;
            if (f >= JSLMath.getLargestExponentialArgument()) {
                throw new IllegalArgumentException("Term overflow due to input parameters");
            }
            if (f <= JSLMath.getSmallestExponentialArgument()) {
                continue;
            } else {
                sum = sum + Math.exp(f);
            }
        }

        return sum;
    }

    /** Allows static computation of prob mass function
     *  assumes that distribution's range is {0,1, ..., n}
     *  Uses the recursive logarithmic algorithm
     *
     * @param j value for which prob is needed
     * @param n num of trials
     * @param p prob of success
     * @return the probability at j
     */
    public static double binomialPMF(int j, int n, double p) {
        return binomialPMF(j, n, p, true);
    }

    /** Allows static computation of prob mass function
     *  assumes that distribution's range is {0,1, ..., n}
     *
     * @param j value for which prob is needed
     * @param n num of successes
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the probability at j
     */
    public static double binomialPMF(int j, int n, double p, boolean recursive) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trials must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return (0.0);
        }

        if (j > n) {
            return 0.0;
        }

        if (recursive) {
            return recursivePMF(j, n, p);
        }

        double q = 1.0 - p;
        double lnq = Math.log(q);
        double f = n * lnq;

        if (j == 0) {
            if (f <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            } else {
                return Math.exp(f);
            }
        }

        double lnp = Math.log(p);

        if (j == n) {
            double g = n * lnp;
            if (g <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            } else {
                return Math.exp(g);
            }
        }

        double lnpj = j * lnp;
        if (lnpj <= JSLMath.getSmallestExponentialArgument()) {
            return 0.0;
        }

        double lnqnj = (n - j) * lnq;
        if (lnqnj <= JSLMath.getSmallestExponentialArgument()) {
            return 0.0;
        }

        double c = JSLMath.binomialCoefficient(n, j);
        double pj = Math.exp(lnpj);
        double qnj = Math.exp(lnqnj);
        return (c * pj * qnj);

    }

    /** Allows static computation of the CDF
     *  assumes that distribution's range is {0,1, ...,n}
     *  Uses the recursive logarithmic algorithm
     *
     * @param j value for which cdf is needed
     * @param n num of trials
     * @param p prob of success
     * @return the cumulative probability at j
     */
    public static double binomialCDF(int j, int n, double p) {
        return binomialCDF(j, n, p, true);
    }

    /** Allows static computation of the CDF
     *  assumes that distribution's range is {0,1, ..., n}
     *
     * @param j value for which cdf is needed
     * @param n num of trials
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the cumulative probability at j
     */
    public static double binomialCDF(int j, int n, double p, boolean recursive) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trials must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return 0.0;
        }

        if (j >= n) {
            return 1.0;
        }

        if (recursive) {
            return recursiveCDF(j, n, p);
        }

        return Beta.regularizedIncompleteBetaFunction(1.0 - p, n - j, j + 1);
    }

    /** Allows static computation of complementary cdf function
     *  assumes that distribution's range is {0,1, ..., n}
     *  Uses the recursive logarithmic algorithm
     *
     * @param j value for which ccdf is needed
     * @param n num of trials
     * @param p prob of success
     * @return the complementary CDF at j
     */
    public static double binomialCCDF(int j, int n, double p) {
        return binomialCCDF(j, n, p, true);
    }

    /** Allows static computation of complementary cdf function
     *  assumes that distribution's range is {0,1, ...,n}
     *
     * @param j value for which ccdf is needed
     * @param n num of trials
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the complementary CDF at j
     */
    public static double binomialCCDF(int j, int n, double p, boolean recursive) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trials must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return (1.0);
        }

        if (j >= n) {
            return 0.0;
        }

        return (1.0 - binomialCDF(j, n, p, recursive));

    }

    /** Allows static computation of 1st order loss function
     *  assumes that distribution's range is {0,1, ..., n}
     *  Uses the recursive logarithmic algorithm
     *
     * @param j value for which 1st order loss function is needed
     * @param n num of trial
     * @param p prob of success
     * @return the first order loss function at j
     */
    public static double binomialLF1(double j, int n, double p) {
        return binomialLF1(j, n, p, true);
    }

    /** Allows static computation of 1st order loss function
     *  assumes that distribution's range is {0,1, ...,n}
     *
     * @param j value for which 1st order loss function is needed
     * @param n num of trials
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the first order loss function at j
     */
    public static double binomialLF1(double j, int n, double p, boolean recursive) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trial must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        double mu = n * p;// the mean

        if (j < 0) {
            return (Math.floor(Math.abs(j)) + mu);
        } else if (j > 0) {
            return (mu - sumCCDF_(j, n, p, recursive));
        } else { // j == 0
            return mu;
        }
    }

    /** Returns the sum of the complementary CDF
     *  from 0 up to but not including x
     *
     * @param x the value to evaluate
     * @param n the number of trials
     * @param recursive the flag to use the recursive algorithm
     * @param p the probability of success
     * @return the sum of the complementary CDF
     */
    protected static double sumCCDF_(double x, int n, double p, boolean recursive) {
        if (x <= 0.0) {
            return 0.0;
        }

        if (x > n) {
            x = n;
        }

        double c = 0.0;
        for (int i = 0; i < x; i++) {
            c = c + binomialCCDF(i, n, p, recursive);
        }
        return c;
    }

    /** Allows static computation of 2nd order loss function
     *  assumes that distribution's range is {0,1, ..., n}
     *  Uses the recursive logarithmic algorithm
     *
     * @param j value for which 2nd order loss function is needed
     * @param n num of trials
     * @param p prob of success
     * @return the 2nd order loss function at j
     */
    public static double binomialLF2(double j, int n, double p) {
        return binomialLF2(j, n, p, true);
    }

    /** Allows static computation of 2nd order loss function
     *  assumes that distribution's range is {0,1, ...,n}
     *
     * @param j value for which 2nd order loss function is needed
     * @param n num of trials
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the 2nd order loss function at j
     */
    public static double binomialLF2(double j, int n, double p, boolean recursive) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trials must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        double mu = n * p;
        double var = n * p * (1.0 - p);
        double sbm = 0.5 * (var + mu * mu - mu);// 1/2 the 2nd binomial moment

        if (j < 0) {
            double s = 0.0;
            for (int y = 0; y > j; y--) {
                s = s + binomialLF1(y, n, p, recursive);
            }
            return (s + sbm);
        } else if (j > 0) {
            return (sbm - sumFirstLoss_(j, n, p, recursive));
        } else {// j== 0
            return sbm;
        }
    }

    /** Sums the first order loss function from
     *  1 up to and including x. x is interpreted
     *  as an integer
     *
     * @param x the value to evaluate
     * @param n the number of trials
     * @param p the probability of success
     * @param recursive true if recursive algorithm is to be used
     * @return the sum
     */
    protected static double sumFirstLoss_(double x, int n, double p, boolean recursive) {
        int k = (int) x;
        double sum = 0.0;
        for (int i = 1; i <= k; i++) {
            sum = sum + binomialLF1(i, n, p, recursive);
        }
        return sum;
    }

    /** Returns the quantile associated with the supplied probability, x
     *  assumes that distribution's range is {0,1, ..., n}
     *  Uses the recursive logarithmic algorithm
     *
     * @param x The probability that the quantile is needed for
     * @param n The number of trials
     * @param p The probability of success, must be in range [0,1)
     * @return the quantile associated with the supplied probability
     */
    public static int binomialInvCDF(double x, int n, double p) {
        return binomialInvCDF(x, n, p, true);
    }

    /** Returns the quantile associated with the supplied probability, x
     *  assumes that distribution's range is {0,1, ...,n}
     *
     * @param x The probability that the quantile is needed for
     * @param n The number of trials
     * @param p The probability of success, must be in range [0,1)
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the quantile associated with the supplied probability
     */
    public static int binomialInvCDF(double x, int n, double p, boolean recursive) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if ((x < 0.0) || (x > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + x + " Probability must be [0,1]");
        }

        if (x <= 0.0) {
            return 0;
        }

        if (x >= 1.0) {
            return n;
        }

        // get approximate quantile from normal approximation
        // and Cornish-Fisher expansion
        int start = invCDFViaNormalApprox(x, n, p);
        double cdfAtStart = binomialCDF(start, n, p, recursive);

        //System.out.println("start = " + start);
        //System.out.println("cdfAtStart = " + cdfAtStart);
        //System.out.println("p = " + p);
        //System.out.println();

        if (x >= cdfAtStart) {
            return searchUpCDF(x, n, p, start, cdfAtStart, recursive);
        } else {
            return searchDownCDF(x, n, p, start, cdfAtStart, recursive);
        }

    }

    /** Approximates the quantile of x using a normal distribution
     *
     * @param x the value to evaluate
     * @param n the number of trials
     * @param p the probability of success
     * @return the approximate inverse CDF value
     */
    public static int invCDFViaNormalApprox(double x, int n, double p) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number of trial must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if ((x < 0.0) || (x > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + x + " Probability must be [0,1]");
        }

        if (x <= 0.0) {
            return 0;
        }

        if (x >= 1.0) {
            return n;
        }

        double q = 1.0 - p;
        double mu = n * p;
        double sigma = Math.sqrt(mu * q);
        double g = (q - p) / sigma;

        /* y := approx.value (Cornish-Fisher expansion) :  */
        double z = Normal.stdNormalInvCDF(x);
        double y = Math.floor(mu + sigma * (z + g * (z * z - 1.0) / 6.0) + 0.5);

        if (y < 0) {
            return 0;
        }

        if (y > n) {
            return n;
        }

        return (int) y;
    }

    protected static int searchUpCDF(double x, int n, double p,
            int start, double cdfAtStart, boolean recursive) {
        int i = start;
        double cdf = cdfAtStart;
        while (x > cdf) {
            i++;
            cdf = cdf + binomialPMF(i, n, p, recursive);
        }
        return i;
    }

    protected static int searchDownCDF(double x, int n, double p,
            int start, double cdfAtStart, boolean recursive) {
        int i = start;
        double cdfi = cdfAtStart;
        while (i > 0) {
            double cdfim1 = cdfi - binomialPMF(i, n, p, recursive);
            if ((cdfim1 <= x) && (x < cdfi)) {
                if (JSLMath.equal(cdfim1, x))// must handle invCDF(cdf(x) = x)
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

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new BinomialRV(myProbSuccess, myNumTrials, rng);
    }
}
