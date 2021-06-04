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
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.TruncatedRV;

/**
 */
public class TruncatedDistribution extends Distribution implements GetRVariableIfc {

    protected DistributionIfc myDistribution;

    protected double myLowerLimit;

    protected double myUpperLimit;

    protected double myCDFLL;

    protected double myCDFUL;

    protected double myFofLL;

    protected double myFofUL;

    protected double myDeltaFUFL;

    /**
     * Constructs a truncated distribution based on the provided distribution
     *
     * @param distribution the distribution to truncate, must not be null
     * @param cdfLL        The lower limit of the range of support of the distribution
     * @param cdfUL        The upper limit of the range of support of the distribution
     * @param truncLL      The truncated lower limit (if moved in from cdfLL), must be &gt;= cdfLL
     * @param truncUL      The truncated upper limit (if moved in from cdfUL), must be &lt;= cdfUL
     */
    public TruncatedDistribution(DistributionIfc distribution, double cdfLL, double cdfUL,
                                 double truncLL, double truncUL) {
        this(distribution, cdfLL, cdfUL, truncLL, truncUL, null);
    }

    /**
     * Constructs a truncated distribution based on the provided distribution
     *
     * @param distribution the distribution to truncate, must not be null
     * @param cdfLL        The lower limit of the range of support of the distribution
     * @param cdfUL        The upper limit of the range of support of the distribution
     * @param truncLL      The truncated lower limit (if moved in from cdfLL), must be &gt;= cdfLL
     * @param truncUL      The truncated upper limit (if moved in from cdfUL), must be &lt;= cdfUL
     * @param name         an optional name/label
     */
    public TruncatedDistribution(DistributionIfc distribution, double cdfLL, double cdfUL,
                                 double truncLL, double truncUL, String name) {
        super(name);
        setDistribution(distribution, cdfLL, cdfUL, truncLL, truncUL);
    }

    /**
     * Returns a new instance of the random source with the same parameters
     *
     * @return the new instance
     */
    public final TruncatedDistribution newInstance() {
        DistributionIfc d = (DistributionIfc) myDistribution.newInstance();
        return (new TruncatedDistribution(d, myCDFLL, myCDFUL, myLowerLimit, myUpperLimit));
    }

    /**
     *
     * @param distribution the distribution to truncate, must not be null
     * @param cdfLL        The lower limit of the range of support of the distribution
     * @param cdfUL        The upper limit of the range of support of the distribution
     * @param truncLL      The truncated lower limit (if moved in from cdfLL), must be &gt;= cdfLL
     * @param truncUL      The truncated upper limit (if moved in from cdfUL), must be &lt;= cdfUL
     */
    public final void setDistribution(DistributionIfc distribution, double cdfLL, double cdfUL,
                                      double truncLL, double truncUL) {
        if (distribution == null) {
            throw new IllegalArgumentException("The distribution must not be null");
        }
        myDistribution = distribution;
        setLimits(cdfLL, cdfUL, truncLL, truncUL);
    }

    /**
     *
     * @param cdfLL        The lower limit of the range of support of the distribution
     * @param cdfUL        The upper limit of the range of support of the distribution
     * @param truncLL      The truncated lower limit (if moved in from cdfLL), must be &gt;= cdfLL
     * @param truncUL      The truncated upper limit (if moved in from cdfUL), must be &lt;= cdfUL
     */
    public final void setLimits(double cdfLL, double cdfUL, double truncLL, double truncUL) {
        if (truncLL >= truncUL) {
            throw new IllegalArgumentException("The lower limit must be < the upper limit");
        }

        if (truncLL < cdfLL) {
            throw new IllegalArgumentException("The lower limit must be >= " + cdfLL);
        }

        if (truncUL > cdfUL) {
            throw new IllegalArgumentException("The upper limit must be <= " + cdfUL);
        }

        if ((truncLL == cdfLL) && (truncUL == cdfUL)) {
            throw new IllegalArgumentException("There was no truncation over the interval of support");
        }

        myLowerLimit = truncLL;
        myUpperLimit = truncUL;
        myCDFLL = cdfLL;
        myCDFUL = cdfUL;
        if ((truncLL > cdfLL) && (truncUL < cdfUL)) {
            // truncation on both ends
            myFofUL = myDistribution.cdf(myUpperLimit);
            myFofLL = myDistribution.cdf(myLowerLimit);
        } else if (truncUL < cdfUL) { // truncation on upper tail
            // must be that upperLimit < UL, and lowerLimit == LL
            myFofUL = myDistribution.cdf(myUpperLimit);
            myFofLL = 0.0;
        } else { //truncation on the lower tail
            // must be that upperLimit == UL, and lowerLimit > LL
            myFofUL = 1.0;
            myFofLL = myDistribution.cdf(myLowerLimit);
        }

        myDeltaFUFL = myFofUL - myFofLL;

        if (JSLMath.equal(myDeltaFUFL, 0.0)) {
            throw new IllegalArgumentException("The supplied limits have no probability support (F(upper) - F(lower) = 0.0)");
        }
    }

    /**
     * Sets the parameters of the truncated distribution
     * cdfLL = parameter[0]
     * cdfUL = parameters[1]
     * truncLL = parameters[2]
     * truncUL = parameters[3]
     * <p>
     * any other values in the array should be interpreted as the parameters
     * for the underlying distribution
     */
    public final void setParameters(double[] parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("The parameters array was null");
        }

        setLimits(parameters[0], parameters[1], parameters[2], parameters[3]);
        double[] y = new double[parameters.length - 4];
        for (int i = 0; i < y.length; i++) {
            y[i] = parameters[i + 4];
        }
        myDistribution.setParameters(y);
    }

    /**
     * Get the parameters for the truncated distribution
     * <p>
     * cdfLL = parameter[0]
     * cdfUL = parameters[1]
     * truncLL = parameters[2]
     * truncUL = parameters[3]
     * <p>
     * any other values in the array should be interpreted as the parameters
     * for the underlying distribution
     */
    public final double[] getParameters() {
        double[] x = myDistribution.getParameters();
        double[] y = new double[x.length + 4];

        y[0] = myCDFLL;
        y[1] = myCDFUL;
        y[2] = myLowerLimit;
        y[3] = myUpperLimit;
        for (int i = 0; i < x.length; i++) {
            y[i + 4] = x[i];
        }
        return y;
    }

    /**
     * The CDF's original lower limit
     *
     * @return CDF's original lower limit
     */
    public final double getCDFLowerLimit() {
        return (myCDFLL);
    }

    /**
     * The CDF's original upper limit
     *
     * @return CDF's original upper limit
     */
    public final double getCDFUpperLimit() {
        return (myCDFUL);
    }

    /**
     * The lower limit for the truncated distribution
     *
     * @return lower limit for the truncated distribution
     */
    public final double getTruncatedLowerLimit() {
        return (myLowerLimit);
    }

    /**
     * The upper limit for the trunctated distribution
     *
     * @return upper limit for the trunctated distribution
     */
    public final double getTruncatedUpperLimit() {
        return (myUpperLimit);
    }

    @Override
    public final double cdf(double x) {
        if (x < myLowerLimit) {
            return 0.0;
        } else if ((x >= myLowerLimit) && (x <= myUpperLimit)) {
            double F = myDistribution.cdf(x);
            return ((F - myFofLL) / myDeltaFUFL);
        } else //if (x > myUpperLimit)
        {
            return 1.0;
        }
    }

    @Override
    public final double getMean() {
        double mu = myDistribution.getMean();
        return (mu / myDeltaFUFL);
    }

    @Override
    public final double getVariance() {
        // Var[X] = E[X^2] - E[X]*E[X]
        // first get 2nd moment of truncated distribution
        // E[X^2] = 2nd moment of original cdf/(F(b)-F(a)
        double mu = myDistribution.getMean();
        double s2 = myDistribution.getVariance();
        // 2nd moment of original cdf
        double m2 = s2 + mu * mu;
        // 2nd moment of truncated
        m2 = m2 / myDeltaFUFL;
        // mean of truncated
        mu = getMean();
        return (m2 - mu * mu);
    }

    @Override
    public double invCDF(double p) {
        double v = myFofLL + myDeltaFUFL * p;
        return myDistribution.invCDF(v);
    }

    @Override
    public final RVariableIfc getRandomVariable(RNStreamIfc rng) {
        return new TruncatedRV(myDistribution, myCDFLL, myCDFUL, myLowerLimit, myUpperLimit, rng);
    }

}
