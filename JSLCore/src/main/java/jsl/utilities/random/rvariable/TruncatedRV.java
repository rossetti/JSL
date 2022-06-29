package jsl.utilities.random.rvariable;

import jsl.utilities.distributions.DistributionIfc;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rng.RNStreamIfc;

public class TruncatedRV extends RVariable {

    protected final DistributionIfc myDistribution;

    protected final double myLowerLimit;

    protected final double myUpperLimit;

    protected final double myCDFLL;

    protected final double myCDFUL;

    protected final double myFofLL;

    protected final double myFofUL;

    protected final double myDeltaFUFL;

    /**
     * Constructs a truncated random variable based on the provided distribution
     *
     * @param distribution the distribution to truncate, must not be null
     * @param cdfLL        The lower limit of the range of support of the distribution
     * @param cdfUL        The upper limit of the range of support of the distribution
     * @param truncLL      The truncated lower limit (if moved in from cdfLL), must be &gt;= cdfLL
     * @param truncUL      The truncated upper limit (if moved in from cdfUL), must be &lt;= cdfUL
     */
    public TruncatedRV(DistributionIfc distribution, double cdfLL, double cdfUL,
                       double truncLL, double truncUL) {
        this(distribution, cdfLL, cdfUL, truncLL, truncUL, JSLRandom.nextRNStream());
    }

    /**
     * Constructs a truncated random variable based on the provided distribution
     *
     * @param distribution the distribution to truncate, must not be null
     * @param cdfLL        The lower limit of the range of support of the distribution
     * @param cdfUL        The upper limit of the range of support of the distribution
     * @param truncLL      The truncated lower limit (if moved in from cdfLL), must be &gt;= cdfLL
     * @param truncUL      The truncated upper limit (if moved in from cdfUL), must be &lt;= cdfUL
     * @param streamNum    A positive integer to identify the stream
     */
    public TruncatedRV(DistributionIfc distribution, double cdfLL, double cdfUL,
                       double truncLL, double truncUL, int streamNum) {
        this(distribution, cdfLL, cdfUL, truncLL, truncUL, JSLRandom.rnStream(streamNum));
    }

    /**
     * Constructs a truncated random variable based on the provided distribution
     *
     * @param distribution the distribution to truncate, must not be null
     * @param cdfLL        The lower limit of the range of support of the distribution
     * @param cdfUL        The upper limit of the range of support of the distribution
     * @param truncLL      The truncated lower limit (if moved in from cdfLL), must be &gt;= cdfLL
     * @param truncUL      The truncated upper limit (if moved in from cdfUL), must be &lt;= cdfUL
     */
    public TruncatedRV(DistributionIfc distribution, double cdfLL, double cdfUL,
                       double truncLL, double truncUL, RNStreamIfc rng) {
        super(rng);
        if (distribution == null) {
            throw new IllegalArgumentException("The distribution must not be null");
        }
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
        myDistribution = distribution;
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
    protected double generate() {
        double v = myFofLL + myDeltaFUFL * myRNStream.randU01();
        return myDistribution.invCDF(v);
    }

    @Override
    public RVariableIfc newInstance(RNStreamIfc rng) {
        return new TruncatedRV(myDistribution, myCDFLL, myCDFUL, myLowerLimit, myUpperLimit, rng);
    }
}
