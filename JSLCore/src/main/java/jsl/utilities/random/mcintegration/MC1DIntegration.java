package jsl.utilities.random.mcintegration;

import jsl.utilities.Interval;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;

import java.util.Objects;

/**
 * Provides for the integration of a 1-D function via Monte-Carlo sampling.
 * The user is responsible for providing a function that when evaluated at the
 * sample from the provided sampler will evaluate to the desired integral over
 * the specified interval.
 * <p>
 * The sampler must have the same range as the specified
 * interval and the function's domain (inputs) must be consistent with the range (output)
 * of the sampler.
 * <p>
 * As an example, suppose we want the evaluation of the integral of g(x) over the range from a to b.
 * If the user selects the sampler as U(a,b) then the function to supply for the integration is NOT g(x).
 * The function should be h(x) = (b-a)*g(x).
 * <p>
 * In general, if the sampler has pdf, w(x), over the range a to b. Then, the function to supply for integration
 * is h(x) = g(x)/w(x). Again, the user is responsible for providing a sampler that provides values over the interval
 * of integration.  And, the user is responsible for providing the appropriate function, h(x), that will result
 * in their desired integral.  This flexibility allows the user to specify h(x) in a factorization that supports an
 * importance sampling distribution as the sampler.
 * <p>
 * The user specifies a desired relative precision, an initial sample size, and a maximum sample size limit.  The initial
 * sample size is used to generate a pilot sample from which an estimate of the number of samples needed to meet
 * the relative precision criteria. Let's call estimated sample size, m.  If after the initial sample is taken, the relative precision is met the
 * evaluation stops; otherwise, the routine will sample until the relative precision criteria is met or the
 * min(m, maximum sample size limit).
 * <p>
 * The user can check if the relative precision criteria was met after the evaluation. If it is not met, the user can
 * adjust the initial sample size, desired relative precision, or maximum sample size and run another evaluation.
 * The statistics associated with the estimate are readily available. The evaluation will automatically utilize
 * antithetic sampling to reduce the variance of the estimates unless the user specifies not to do so. In the case of
 * using antithetic sampling, the sample size refers to the number of independent antithetic pairs observed. Thus, this
 * will require two function evaluations at each observation. The user can consider the implication of the cost of
 * function evaluation versus the variance reduction obtained. The user may
 * also reset the underlying random number stream if a reproducible result is desired within the same execution frame.
 * By default, the underlying random number stream is not reset with each invocation of the evaluate() method.
 * The default confidence level is 99 percent.
 */
public class MC1DIntegration {

    private int initialSampleSize = 50;
    private int maxSampleSize = 20000;
    private double desiredRelError = 0.00001;
    private boolean resetStreamOptionOn = false;
    private final Statistic statistic = new Statistic();
    private final Interval myInterval;
    private final MC1DFunctionIfc myFunction;
    private final MC1DRVariableIfc mySampler;
    private RVariableIfc myAntitheticSampler;

    /**
     *
     * @param interval the interval for the integration, must not be null
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     */
    public MC1DIntegration(Interval interval, MC1DFunctionIfc function, MC1DRVariableIfc sampler) {
        this(interval, function, sampler, true);
    }

    /**
     *
     * @param interval the interval for the integration, must not be null
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     * @param antitheticOptionOn  true represents use of antithetic sampling
     */
    public MC1DIntegration(Interval interval, MC1DFunctionIfc function, MC1DRVariableIfc sampler, boolean antitheticOptionOn) {
        Objects.requireNonNull(interval, "The interval was null!");
        Objects.requireNonNull(sampler, "The MC1DRVariableIfc was null!");
        Objects.requireNonNull(function, "The MC1DFunctionIfc was null!");
        if (!interval.equals(sampler.getRange())) {
            throw new IllegalArgumentException("The sampler does not have the same range as the integration interval!");
        }
        if (!sampler.getRange().equals(function.getDomain())) {
            throw new IllegalArgumentException("The sampler's range does not match the domain of the function being integrated!");
        }
        this.myInterval = interval;
        this.myFunction = function;
        this.mySampler = sampler;
        if (antitheticOptionOn) {
            myAntitheticSampler = mySampler.newAntitheticInstance();
        }
        setConfidenceLevel(0.99);
    }

    /**
     *
     * @param level the desired confidence level
     */
    public void setConfidenceLevel(double level) {
        statistic.setConfidenceLevel(level);
    }

    /**
     *  See page 513 of Law and Kelton
     *
     * @return the adjusted relative error
     */
    private double getAdjustedRelError() {
        return desiredRelError / (1.0 + desiredRelError);
    }

    /**
     *
     * @return true if the relative error meets the desired with the appropriate level of confidence
     */
    public boolean checkRelativeError() {
        if (statistic.getCount() < 2.0) {
            return false;
        }
        return statistic.getHalfWidth() <= Math.abs(statistic.getAverage()) * getAdjustedRelError();
    }

    /**
     *
     * @return the number of samples needed to meet relative error criteria with the specified level of confidence
     */
    public double estimateSampleSize() {
        if (statistic.getCount() < 2.0) {
            return Double.NaN;
        }
        double var = statistic.getVariance();
        double alpha = 1.0 - statistic.getConfidenceLevel();
        double ao2 = alpha / 2.0;
        double z = Normal.stdNormalInvCDF(1.0 - ao2);
        double dn = getAdjustedRelError() * statistic.getAverage();
        return Math.ceil(var * (z * z) / (dn * dn));
    }

    /**
     *
     * @return the estimated integral
     */
    public double evaluate() {
        statistic.reset();
        if (resetStreamOptionOn) {
            mySampler.resetStartStream();
        }
        double numNeeded = runInitialSample();
        int n = (int) Math.min(numNeeded, maxSampleSize);
        sample(n);
        return statistic.getAverage();
    }

    /**
     * The purpose of the initial sample is to estimate the variability
     * and determine an approximate number of additional samples needed
     * to meet the desired relative precision.
     *
     * @return the number of additional samples needed
     */
    protected double runInitialSample() {
        if (checkRelativeError()) {
            return 0.0;
        }
        final double n0 = sample(initialSampleSize);
        double m = estimateSampleSize();
        return m - n0;
    }

    /**
     * Sample for n evaluations or until relative precision criterion is met
     *
     * @param n the number of samples to take
     * @return the number of samples performed
     */
    protected double sample(int n) {
        double y;
        for (int i = 1; i <= n; i++) {
            if (isAntitheticOptionOn()) {
                double y1 = myFunction.fx(mySampler.sample());
                double y2 = myFunction.fx(myAntitheticSampler.sample());
                y = (y1 + y2) / 2.0;
            } else {
                y = myFunction.fx(mySampler.sample());
            }
            statistic.collect(y);
            if (checkRelativeError()) {
                return statistic.getCount();
            }
        }
        return statistic.getCount();
    }

    public int getInitialSampleSize() {
        return initialSampleSize;
    }

    public void setInitialSampleSize(int initialSampleSize) {
        if (initialSampleSize < 2.0) {
            throw new IllegalArgumentException("The initial sample size must be >= 2");
        }
        this.initialSampleSize = initialSampleSize;
    }

    public int getMaxSampleSize() {
        return maxSampleSize;
    }

    public void setMaxSampleSize(int maxSampleSize) {
        if (maxSampleSize < initialSampleSize) {
            throw new IllegalArgumentException("The maximum sample size must be >= " + initialSampleSize);
        }
        this.maxSampleSize = maxSampleSize;
    }

    public double getDesiredRelError() {
        return desiredRelError;
    }

    public void setDesiredRelError(double desiredRelError) {
        if (desiredRelError <= 0.0) {
            throw new IllegalArgumentException("The desired relative precision must be > 0.0");
        }
        this.desiredRelError = desiredRelError;
    }

    public boolean isResetStreamOptionOn() {
        return resetStreamOptionOn;
    }

    public void setResetStreamOption(boolean resetStreamOptionOn) {
        this.resetStreamOptionOn = resetStreamOptionOn;
    }

    public boolean isAntitheticOptionOn() {
        return myAntitheticSampler != null;
    }


}
