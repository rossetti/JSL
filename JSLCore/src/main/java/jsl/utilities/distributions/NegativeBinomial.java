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
import jsl.utilities.random.rvariable.NegativeBinomialRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/** The number of failures (=0) before the rth success (=1) in a sequence of independent Bernoulli trials
 *  with probability p of success on each trial.  The range of this random variable is {0, 1, 2, ....}
 *  
 * 
 */
public class NegativeBinomial extends Distribution implements DiscreteDistributionIfc,
        LossFunctionDistributionIfc, GetRVariableIfc {

    /** the probability of success, p
     */
    private double myProbSuccess;

    /** the probability of failure, 1-p
     */
    private double myProbFailure;

    /** the desired number of successes to wait for
     */
    private double myDesiredNumSuccesses;

    /** indicates whether or not pmf and cdf calculations are
     *  done by recursive (iterative) algorithm based on logarithms
     *  or via beta incomplete function and binomial coefficients.
     *
     */
    private boolean myRecursiveAlgoFlag = true;

    /**
     * Constructs a NegativeBinomial with n=1, p=0.5
     */
    public NegativeBinomial() {
        this(0.5, 1, null);
    }

    /**
     * Constructs a NegativeBinomial using the supplied parameters
     *
     * @param parameter parameter[0] should be the probability (p) and parameter[1]
     * should be the desired number of successes
     */
    public NegativeBinomial(double[] parameter) {
        this(parameter[0], (int) parameter[1], null);
    }

    /**
     * Constructs a NegativeBinomial with p probability of success based on n
     * success
     *
     * @param prob The success probability
     * @param numSuccess The desired number of successes
     */
    public NegativeBinomial(double prob, double numSuccess) {
        this(prob, numSuccess, null);
    }

    /**
     * Constructs a NegativeBinomial with p probability of success based on n
     * success
     *
     * @param prob The success probability
     * @param numSuccess The desired number of successes
     * @param name an optional name/label
     */
    public NegativeBinomial(double prob, double numSuccess, String name) {
        super(name);
        setParameters(prob, numSuccess);
    }

    /** indicates whether or not pmf and cdf calculations are
     *  done by recursive (iterative) algorithm based on logarithms
     *  or via beta incomplete function and binomial coefficients.
     *
     * @return true if used
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
    public final NegativeBinomial newInstance() {
        return (new NegativeBinomial(getParameters()));
    }

    /**
     * Sets the number of success and success probability
     *
     * @param prob The success probability
     * @param numSuccess The desired number of successes
     */
    public final void setParameters(double prob, double numSuccess) {
        setProbabilityOfSuccess(prob);
        setDesiredNumberOfSuccesses(numSuccess);
    }

    /** Gets the mode of the distribution
     *
     * @return the mode of the distribution
     */
    public final int getMode() {
        if (myDesiredNumSuccesses > 1.0) {
            return (int) Math.floor((myDesiredNumSuccesses - 1.0) * myProbFailure / myProbSuccess);
        } else {
            return 0;
        }
    }

    /** Sets the parameters as an array parameters[0] is probability of success
     *  parameters[1] is number of desired successes
     *
     */
    @Override
    public final void setParameters(double[] parameters) {
        setParameters(parameters[0], parameters[1]);
    }

    /**
     * Gets the parameters as an array parameters[0] is probability of success
     * parameters[1] is number of desired successes
     *
     */
    @Override
    public final double[] getParameters() {
        double[] param = new double[2];
        param[0] = myProbSuccess;
        param[1] = myDesiredNumSuccesses;
        return (param);
    }

    /**
     * Gets the success probability
     *
     * @return The success probability
     */
    public final double getProbabilityOfSuccess() {
        return (myProbSuccess);
    }

    /**
     * Gets the desired number of successes
     *
     * @return the number of success
     */
    public final double getDesiredNumberOfSuccesses() {
        return (myDesiredNumSuccesses);
    }

    /**
     * Sets the number of success
     *
     * @param numSuccess The desired number of successes
     */
    private void setDesiredNumberOfSuccesses(double numSuccess) {
        if (numSuccess <= 0) {
            throw new IllegalArgumentException("The desired number of successes must be > 0");
        }

        myDesiredNumSuccesses = numSuccess;
    }

    /**
     * Sets the probability throws IllegalArgumentException when probability is
     * outside the range (0,1)
     *
     * @param prob the probability of success
     */
    private void setProbabilityOfSuccess(double prob) {
        if ((prob <= 0.0) || (prob >= 1.0)) {
            throw new IllegalArgumentException("Probability must be in (0,1)");
        }
        myProbSuccess = prob;
        myProbFailure = 1.0 - myProbSuccess;
    }

    @Override
    public final double getMean() {
        return (myDesiredNumSuccesses * myProbFailure) / myProbSuccess;
    }

    @Override
    public final double getVariance() {
        return (myDesiredNumSuccesses * myProbFailure) / (myProbSuccess * myProbSuccess);
    }

    @Override
    public final double pmf(double x) {
        if (Math.floor(x) == x) {
            return pmf((int) x);
        } else {
            return 0.0;
        }
    }

    /**
     * Returns the prob of getting x failures before the rth success where r is
     * the desired number of successes parameter
     *
     * @param j the value to evaluate
     * @return the probability
     */
    public final double pmf(int j) {
        return negBinomialPMF(j, myDesiredNumSuccesses, myProbSuccess, myRecursiveAlgoFlag);
    }

    @Override
    public final double cdf(double x) {
        return cdf((int) x);
    }

    /**
     * Computes the cumulative probability distribution function for given value
     * of failures
     *
     * @param j The value to be evaluated
     * @return The probability, P{X &lt;=j}
     */
    public final double cdf(int j) {
        return negBinomialCDF(j, myDesiredNumSuccesses, myProbSuccess, myRecursiveAlgoFlag);
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

        // check for geometric case
        if (JSLMath.equal(myDesiredNumSuccesses, 1.0)) {
            double x = (Math.ceil((Math.log(1.0 - prob) / (Math.log(1.0 - myProbSuccess))) - 1.0));
            return (0.0 + x);
        }

        return negBinomialInvCDF(prob, myDesiredNumSuccesses, myProbSuccess, myRecursiveAlgoFlag);

    }

    /** Computes the binomial coefficient.  Computes the number of combinations of size k
     * that can be formed from n distinct objects.
     * @param n The total number of distinct items
     * @param k The number of subsets
     * @return the coefficient
     */
    private static double binomialCoefficient(double n, double k) {
        return Math.exp(logFactorial(n) - logFactorial(k) - logFactorial(n - k));
    }

    /** Computes the natural logarithm of the factorial operator.
     * ln(n!)
     * @param n The value to be operated on.
     * @return the natural log of the factorial
     */
    private static double logFactorial(double n) {
        return (Gamma.logGammaFunction(n + 1.0));
    }

    /** Computes the probability mass function at j using a
     *  recursive (iterative) algorithm using logarithms
     *
     * @param j the value to be evaluated
     * @param r number of successes
     * @param p the probability
     * @return the probability
     */
    public static double recursivePMF(int j, double r, double p) {
        if (r <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return 0.0;
        }

        double y = r * Math.log(p);
        double lnq = Math.log(1.0 - p);

        for (int i = 1; i <= j; i++) {
            y = Math.log(r - 1.0 + i) - +Math.log(i) + lnq + y;
        }

        if (y >= JSLMath.getLargestExponentialArgument()) {
            throw new IllegalArgumentException("Term overflow due to input parameters");
        }

        if (y <= JSLMath.getSmallestExponentialArgument()) {
            return 0.0;
        }

        return Math.exp(y);
    }

    /** Computes the cdf at j using a recursive (iterative) algorithm using logarithms
     *
     * @param j the value to be evaluated
     * @param r number of successes
     * @param p the probability
     * @return the probability
     */
    public static double recursiveCDF(int j, double r, double p) {
        if (r <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return (0.0);
        }

        double y = r * Math.log(p);
        if (j == 0) {
            if (y >= JSLMath.getLargestExponentialArgument()) {
                throw new IllegalArgumentException("Term overflow due to input parameters");
            }

            if (y <= JSLMath.getSmallestExponentialArgument()) {
                return 0.0;
            }
        }
        double lnq = Math.log(1.0 - p);
        double sum = Math.exp(y);

        for (int i = 1; i <= j; i++) {
            y = Math.log(r - 1.0 + i) - +Math.log(i) + lnq + y;
            if (y >= JSLMath.getLargestExponentialArgument()) {
                throw new IllegalArgumentException("Term overflow due to input parameters");
            }

            if (y <= JSLMath.getSmallestExponentialArgument()) {
                continue;
            } else {
                sum = sum + Math.exp(y);
            }
        }

        return sum;
    }

    /** Allows static computation of prob mass function
     *  assumes that distribution's range is {0,1, ...}
     *  Uses the recursive logarithmic algorithm
     * 
     * @param j value for which prob is needed
     * @param r num of successes
     * @param p prob of success
     * @return the probability mass function evaluated at j
     */
    public static double negBinomialPMF(int j, double r, double p) {
        return negBinomialPMF(j, r, p, true);
    }

    /** Allows static computation of prob mass function
     *  assumes that distribution's range is {0,1, ...}
     *
     * @param j value for which prob is needed
     * @param r num of successes
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the probability
     */
    public static double negBinomialPMF(int j, double r, double p, boolean recursive) {
        if (r <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return (0.0);
        }

        if (recursive) {
            return recursivePMF(j, r, p);
        }

        double k = r - 1.0;
        double bc = binomialCoefficient(j + k, k);
        double lny = r * Math.log(p) + j * Math.log(1.0 - p);
        double y = Math.exp(lny);
        return bc * y;

    }

    /** Allows static computation of the CDF
     *  assumes that distribution's range is {0,1, ...}
     *  Uses the recursive logarithmic algorithm
     * 
     * @param j value for which cdf is needed
     * @param r num of successes
     * @param p prob of success
     * @return the probability
     */
    public static double negBinomialCDF(int j, double r, double p) {
        return negBinomialCDF(j, r, p, true);
    }

    /** Allows static computation of the CDF
     *  assumes that distribution's range is {0,1, ...}
     *
     * @param j value for which cdf is needed
     * @param r num of successes
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the probability
     */
    public static double negBinomialCDF(int j, double r, double p, boolean recursive) {
        if (r <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return 0.0;
        }

        if (recursive) {
            return recursiveCDF(j, r, p);
        }

        return Beta.regularizedIncompleteBetaFunction(p, r, j + 1);
    }

    /** Allows static computation of complementary cdf function
     *  assumes that distribution's range is {0,1, ...}
     *  Uses the recursive logarithmic algorithm
     *
     * @param j value for which ccdf is needed
     * @param r num of successes
     * @param p prob of success
     * @return the probability
     */
    public static double negBinomialCCDF(int j, double r, double p) {
        return negBinomialCCDF(j, r, p, true);
    }

    /** Allows static computation of complementary cdf function
     *  assumes that distribution's range is {0,1, ...}
     *
     * @param j value for which ccdf is needed
     * @param r num of successes
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the probability
     */
    public static double negBinomialCCDF(int j, double r, double p, boolean recursive) {
        if (r <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        if (j < 0) {
            return (1.0);
        }

        return (1.0 - negBinomialCDF(j, r, p, recursive));

    }

    /** Allows static computation of 1st order loss function
     *  assumes that distribution's range is {0,1, ...}
     *  Uses the recursive logarithmic algorithm
     *
     * @param j value for which 1st order loss function is needed
     * @param r num of successes
     * @param p prob of success
     * @return the loss function value
     */
    public static double negBinomialLF1(int j, double r, double p) {
        return negBinomialLF1(j, r, p, true);
    }

    /** Allows static computation of 1st order loss function
     *  assumes that distribution's range is {0,1, ...}
     *
     * @param j value for which 1st order loss function is needed
     * @param r num of successes
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the loss function value
     */
    public static double negBinomialLF1(int j, double r, double p, boolean recursive) {
        if (r <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        double mu = r * (1.0 - p) / p;// the mean

        if (j < 0) {
            return (Math.floor(Math.abs(j)) + mu);
        } else if (j > 0) {
            double b, g0, g, g1;
            b = (1.0 - p) / p;
            g = negBinomialPMF(j, r, p, recursive);
            g0 = negBinomialCCDF(j, r, p, recursive);
            g1 = -1.0 * (j - r * b) * g0 + (j + r) * b * g;
            return g1;
        } else { // j == 0
            return mu;
        }
    }

    /** Allows static computation of 2nd order loss function
     *  assumes that distribution's range is {0,1, ...}
     *  Uses the recursive logarithmic algorithm
     * 
     * @param j value for which 2nd order loss function is needed
     * @param r num of successes
     * @param p prob of success
     * @return the loss function value
     */
    public static double negBinomialLF2(int j, double r, double p) {
        return negBinomialLF2(j, r, p, true);
    }

    /** Allows static computation of 2nd order loss function
     *  assumes that distribution's range is {0,1, ...}
     * 
     * @param j value for which 2nd order loss function is needed
     * @param r num of successes
     * @param p prob of success
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the loss function value
     */
    public static double negBinomialLF2(int j, double r, double p, boolean recursive) {
        if (r <= 0) {
            throw new IllegalArgumentException("The number of successes must be > 0");
        }

        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("Success Probability must be in (0,1)");
        }

        double mu = r * (1.0 - p) / p;
        double var = mu / p;
        double sbm = 0.5 * (var + mu * mu - mu);// 1/2 the 2nd binomial moment

        if (j < 0) {
            double s = 0.0;
            for (int y = 0; y > j; y--) {
                s = s + negBinomialLF1(y, r, p, recursive);
            }
            return (s + sbm);
        } else if (j > 0) {
            double b, g0, g, g2;
            b = (1.0 - p) / p;
            if (j < 0) {
                g0 = 1.0;
                g = 0.0;
            } else {
                g = negBinomialPMF(j, r, p, recursive);
                g0 = negBinomialCCDF(j, r, p, recursive);
            }
            g2 = (r * (r + 1) * b * b - 2.0 * r * b * j + j * (j + 1)) * g0;
            g2 = g2 + ((r + 1) * b - j) * (j + r) * b * g;
            g2 = 0.5 * g2;
            return g2;
        } else {// j== 0
            return sbm;
        }
    }

    /** Returns the quantile associated with the supplied probability, x
     *  assumes that distribution's range is {0,1, ...}
     *  Uses the recursive logarithmic algorithm
     *
     * @param x The probability that the quantile is needed for
     * @param r The number of successes parameter
     * @param p The probability of success, must be in range [0,1)
     * @return the inverse CDF value
     */
    public static int negBinomialInvCDF(double x, double p, double r) {
        return negBinomialInvCDF(x, p, r, true);
    }

    /** Returns the quantile associated with the supplied probability, x
     *  assumes that distribution's range is {0,1, ...}
     * 
     * @param x The probability that the quantile is needed for
     * @param r The number of successes parameter
     * @param p The probability of success, must be in range [0,1)
     * @param recursive true indicates that the recursive logarithmic algorithm should be used
     * @return the inverse CDF value
     */
    public static int negBinomialInvCDF(double x, double p, double r, boolean recursive) {
        if (r <= 0) {
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
            return Integer.MAX_VALUE;
        }

        // check for geometric case
        if (JSLMath.equal(r, 1.0)) {
            return ((int) Math.ceil((Math.log(1.0 - x) / (Math.log(1.0 - p))) - 1.0));
        }

        // get approximate quantile from normal approximation
        // and Cornish-Fisher expansion
        int start = invCDFViaNormalApprox(x, r, p);
        double cdfAtStart = negBinomialCDF(start, r, p, recursive);

        //System.out.println("start = " + start);
        //System.out.println("cdfAtStart = " + cdfAtStart);
        //System.out.println("p = " + p);
        //System.out.println();

        if (x >= cdfAtStart) {
            return searchUpCDF(x, r, p, start, cdfAtStart, recursive);
        } else {
            return searchDownCDF(x, r, p, start, cdfAtStart, recursive);
        }

    }

    /**
     *
     * @param x the value to evaluate
     * @param r the trial number
     * @param p the probability of success
     * @param start the starting place for search
     * @param cdfAtStart the CDF at the starting place
     * @param recursive true for using recursive algorithm
     * @return the found value
     */
    protected static int searchUpCDF(double x, double r, double p,
            int start, double cdfAtStart, boolean recursive) {
        int i = start;
        double cdf = cdfAtStart;
        while (x > cdf) {
            i++;
            cdf = cdf + negBinomialPMF(i, r, p, recursive);
        }
        return i;
    }

    /**
     *
     * @param x the value to evaluate
     * @param r the trial number
     * @param p the probability of success
     * @param start the starting place for search
     * @param cdfAtStart the CDF at the starting place
     * @param recursive true for using recursive algorithm
     * @return the found value
     */
    protected static int searchDownCDF(double x, double r, double p,
            int start, double cdfAtStart, boolean recursive) {
        int i = start;
        double cdfi = cdfAtStart;
        while (i > 0) {
            double cdfim1 = cdfi - negBinomialPMF(i, r, p, recursive);
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

    /**
     *
     * @param x the value to evaluate
     * @param r the trial number
     * @param p the probability of success
     * @return the inverse value at the point x
     */
    protected static int invCDFViaNormalApprox(double x, double r, double p) {
        if (r <= 0) {
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
            return Integer.MAX_VALUE;
        }

        double dQ = 1.0 / p;
        double dP = (1.0 - p) * dQ;
        double mu = r * dP;
        double sigma = Math.sqrt(r * dP * dQ);
        double g = (dQ + dP) / sigma;

        /* y := approx.value (Cornish-Fisher expansion) :  */
        double z = Normal.stdNormalInvCDF(x);
        double y = Math.floor(mu + sigma * (z + g * (z * z - 1.0) / 6.0) + 0.5);
        if (y < 0) {
            return 0;
        } else {
            return (int) y;
        }
    }

    public static boolean canMatchMoments(double... moments) {
        if (moments.length < 2) {
            throw new IllegalArgumentException("Must provide a mean and a variance. You provided " + moments.length + " moments.");
        }
        double mean = moments[0];
        double var = moments[1];
        if (mean > 0 && var > 0 && mean < var) {
            return true;
        } else {
            return false;
        }
    }

    public static double[] getParametersFromMoments(double... moments) {
        if (!canMatchMoments(moments)) {
            throw new IllegalArgumentException("Mean and variance must be positive and mean < variance. Your mean: " + moments[0] + " and variance: " + moments[1]);
        }
        double mean = moments[0];
        double var = moments[1];
        double vmr = var / mean;
        return new double[]{1 / vmr, mean / (vmr - 1)};
    }

    public static NegativeBinomial createFromMoments(double... moments) {
        double[] param = getParametersFromMoments(moments);
        return new NegativeBinomial(param[0], param[1]);
    }

    @Override
    public double firstOrderLossFunction(double x) {
        double mu = getMean();
        if (x < 0.0) {
            return (Math.floor(Math.abs(x)) + mu);
        } else if (x > 0.0) {
            double b, g0, g, g1, p;
            double r = myDesiredNumSuccesses;
            p = myProbSuccess;
            b = (1.0 - p) / p;
            g0 = complementaryCDF(x);
            g = pmf(x);
            g1 = -1.0 * (x - r * b) * g0 + (x + r) * b * g;
            return g1;
        } else // x== 0.0
        {
            return mu;
        }
    }

    @Override
    public double secondOrderLossFunction(double x) {
        double mu = getMean();
        double sbm = 0.5 * (getVariance() + mu * mu - mu);// 1/2 the 2nd binomial moment
        if (x < 0.0) {
            double s = 0.0;
            for (int y = 0; y > x; y--) {
                s = s + firstOrderLossFunction(y);
            }
            return (s + sbm);
        } else if (x > 0.0) {
            double b, g0, g, g2, p;
            double r = myDesiredNumSuccesses;
            p = myProbSuccess;
            b = (1.0 - p) / p;
            g0 = complementaryCDF(x);
            g = pmf(x);

            g2 = (r * (r + 1) * b * b - 2.0 * r * b * x + x * (x + 1)) * g0;
            g2 = g2 + ((r + 1) * b - x) * (x + r) * b * g;
            g2 = 0.5 * g2;
            return g2;
        } else // x == 0.0
        {
            return sbm;
        }
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new NegativeBinomialRV(myProbSuccess,myDesiredNumSuccesses, rng);
    }
}
