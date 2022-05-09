package jsl.utilities.random.mcintegration;

import jsl.utilities.distributions.Normal;
import jsl.utilities.statistic.Statistic;

/**
 * Provides for the integration of a multi-dimensional function via Monte-Carlo sampling.
 * The user is responsible for providing a function that when evaluated at the
 * sample from the provided sampler will evaluate to the desired integral over
 * the range of possible values of the sampler.
 * <p>
 * The sampler must have the same range as the desired integral and the function's domain (inputs) must be consistent
 * with the range (output) of the sampler.
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
 * The user specifies a desired error bound, an initial sample size, and a maximum sample size limit.  The initial
 * sample size is used to generate a pilot sample from which an estimate of the number of samples needed to meet
 * the relative precision criteria. Let's call estimated sample size, m.  If after the initial sample is taken, the error is met the
 * evaluation stops; otherwise, the routine will sample until the error criteria is met or the
 * min(m, maximum sample size limit).
 * <p>
 * The user can check if the error criteria was met after the evaluation. If it is not met, the user can
 * adjust the initial sample size, desired error, or maximum sample size and run another evaluation.
 * The statistics associated with the estimate are readily available. The user may
 * reset the underlying random number stream if a reproducible result is desired within the same execution frame.
 * By default, the underlying random number stream is not reset with each invocation of the evaluate() method.
 * The default confidence level is 99 percent.
 *
 * Be aware that small desired absolute error may result in large execution times.
 */
public abstract class MCIntegration implements MCIntegrationIfc {
    protected final Statistic statistic = new Statistic();
    protected int initialSampleSize = 100;
    protected int maxSampleSize = 100000;
    protected double desiredAbsError = 0.0001;
    protected boolean resetStreamOptionOn = false;

    @Override
    public void setConfidenceLevel(double level) {
        statistic.setConfidenceLevel(level);
    }

    @Override
    public boolean checkStoppingCriteria() {
        if (statistic.getCount() < 2.0) {
            return false;
        }
        return statistic.getHalfWidth() <= getDesiredAbsError();
    }

    @Override
    public double estimateSampleSize() {
        if (statistic.getCount() < 2.0) {
            return Double.NaN;
        }
        long sampleSize = statistic.estimateSampleSize(getDesiredAbsError());
        return (double) sampleSize;
    }

    @Override
    public double estimateSampleSizeForRelativeError(double relativeError) {
        if (statistic.getCount() < 2.0) {
            return Double.NaN;
        }
        if (relativeError <= 0.0) {
            throw new IllegalArgumentException("The relative error bound must be > 0.0");
        }
        double adjRE = relativeError / (1.0 + relativeError);
        double var = statistic.getVariance();
        double alpha = 1.0 - statistic.getConfidenceLevel();
        double ao2 = alpha / 2.0;
        double z = Normal.stdNormalInvCDF(1.0 - ao2);
        double dn = adjRE * statistic.getAverage();
        return Math.ceil(var * (z * z) / (dn * dn));
    }

    @Override
    public double evaluate() {
        double numNeeded = runInitialSample();
        int n = (int) Math.min(numNeeded, maxSampleSize);
        sample(n);
        return statistic.getAverage();
    }

    @Override
    public Statistic getStatistic() {
        return statistic.newInstance();
    }

    @Override
    public double runInitialSample() {
        if (checkStoppingCriteria()) {
            return 0.0;
        }
        statistic.reset();
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
    protected abstract double sample(int n);

    @Override
    public int getInitialSampleSize() {
        return initialSampleSize;
    }

    @Override
    public void setInitialSampleSize(int initialSampleSize) {
        if (initialSampleSize < 2.0) {
            throw new IllegalArgumentException("The initial sample size must be >= 2");
        }
        this.initialSampleSize = initialSampleSize;
    }

    @Override
    public long getMaxSampleSize() {
        return maxSampleSize;
    }

    @Override
    public void setMaxSampleSize(int maxSampleSize) {
        if (maxSampleSize < initialSampleSize) {
            throw new IllegalArgumentException("The maximum sample size must be >= " + initialSampleSize);
        }
        this.maxSampleSize = maxSampleSize;
    }

    @Override
    public double getDesiredAbsError() {
        return desiredAbsError;
    }

    @Override
    public void setDesiredAbsError(double desiredAbsError) {
        if (desiredAbsError <= 0.0) {
            throw new IllegalArgumentException("The desired relative precision must be > 0.0");
        }
        this.desiredAbsError = desiredAbsError;
    }

    @Override
    public boolean isResetStreamOptionOn() {
        return resetStreamOptionOn;
    }

    @Override
    public void setResetStreamOption(boolean resetStreamOptionOn) {
        this.resetStreamOptionOn = resetStreamOptionOn;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Monte Carlo Integration Results");
        sb.append(System.lineSeparator());
        sb.append("initial Sample Size = ").append(initialSampleSize);
        sb.append(System.lineSeparator());
        sb.append("max Sample Size = ").append(maxSampleSize);
        sb.append(System.lineSeparator());
        sb.append("desired Abs Error = ").append(desiredAbsError);
        sb.append(System.lineSeparator());
        sb.append("reset Stream OptionOn = ").append(resetStreamOptionOn);
        sb.append(System.lineSeparator());
        sb.append("Absolute error criterion check? = ");
        sb.append(checkStoppingCriteria());
        sb.append(System.lineSeparator());
        sb.append("Estimated sample size needed to meet criteria = ");
        sb.append(estimateSampleSize());
        sb.append(System.lineSeparator());
        sb.append("**** Sampling results ****");
        sb.append(System.lineSeparator());
        sb.append(statistic);
        return sb.toString();
    }
}
