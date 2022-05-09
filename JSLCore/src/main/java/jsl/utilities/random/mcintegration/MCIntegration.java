package jsl.utilities.random.mcintegration;

import jsl.utilities.Interval;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rvariable.MVIndependentRV;
import jsl.utilities.random.rvariable.MVRVariableIfc;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides for the integration of a multi-dimensional function via Monte-Carlo sampling.
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
 * The user specifies a desired error bound, an initial sample size, and a maximum sample size limit.  The initial
 * sample size is used to generate a pilot sample from which an estimate of the number of samples needed to meet
 * the relative precision criteria. Let's call estimated sample size, m.  If after the initial sample is taken, the error is met the
 * evaluation stops; otherwise, the routine will sample until the error criteria is met or the
 * min(m, maximum sample size limit).
 * <p>
 * The user can check if the error criteria was met after the evaluation. If it is not met, the user can
 * adjust the initial sample size, desired error, or maximum sample size and run another evaluation.
 * The statistics associated with the estimate are readily available. The evaluation will automatically utilize
 * antithetic sampling to reduce the variance of the estimates unless the user specifies not to do so. In the case of
 * using antithetic sampling, the sample size refers to the number of independent antithetic pairs observed. Thus, this
 * will require two function evaluations at each observation. The user can consider the implication of the cost of
 * function evaluation versus the variance reduction obtained. The user may
 * also reset the underlying random number stream if a reproducible result is desired within the same execution frame.
 * By default, the underlying random number stream is not reset with each invocation of the evaluate() method.
 * The default confidence level is 99 percent.
 *
 * Be aware that small desired absolute error may result in large execution times.
 */
public class MCIntegration {

    private int initialSampleSize = 100;
    private int maxSampleSize = 100000;
    private double desiredAbsError = 0.0001;
    private boolean resetStreamOptionOn = false;
    private final Statistic statistic = new Statistic();
    private final List<Interval> myIntervals;
    private final FunctionMVIfc myFunction; //TODO generalize to check domain/range
    private final MVRVariableIfc mySampler; //TODO generalize to check domain/range
    private MVRVariableIfc myAntitheticSampler;

    /**
     *
     * @param intervals the intervals for the integration, must not be null
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     */
    public MCIntegration(List<Interval> intervals, FunctionMVIfc function, MVRVariableIfc sampler) {
        this(intervals, function, sampler, true);
    }

    /**
     *
     * @param intervals the intervals for the integration, must not be null
     * @param function the representation of h(x), must not be null
     * @param sampler  the sampler over the interval, must not be null
     * @param antitheticOptionOn  true represents use of antithetic sampling
     */
    public MCIntegration(List<Interval> intervals, FunctionMVIfc function, MVRVariableIfc sampler, boolean antitheticOptionOn) {
        Objects.requireNonNull(intervals, "The interval was null!");
        Objects.requireNonNull(sampler, "The MC1DRVariableIfc was null!");
        Objects.requireNonNull(function, "The MC1DFunctionIfc was null!");
        myIntervals = new ArrayList<>();
        for(Interval i: intervals){
            if (i == null){
                throw new IllegalArgumentException("The list of intervals had a null member!");
            }
            myIntervals.add(i);
        }
//        if (!interval.equals(sampler.getRange())) {
//            throw new IllegalArgumentException("The sampler does not have the same range as the integration interval!");
//        }
//        if (!sampler.getRange().equals(function.getDomain())) {
//            throw new IllegalArgumentException("The sampler's range does not match the domain of the function being integrated!");
//        }
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
     *
     * @return true if the relative error meets the desired with the appropriate level of confidence
     */
    public boolean checkRelativeError() {
        if (statistic.getCount() < 2.0) {
            return false;
        }
        return statistic.getHalfWidth() <= getDesiredAbsError() ;
    }

    /**
     *
     * @return the number of samples needed to meet the half-width error criteria with the specified level of confidence
     */
    public double estimateSampleSize() {
        if (statistic.getCount() < 2.0) {
            return Double.NaN;
        }
        long sampleSize = statistic.estimateSampleSize(getDesiredAbsError());
        return (double)sampleSize;
    }

    /**  See page 513 of Law & Kelton
     *
     * @param relativeError a relative error bound
     * @return the recommended sample size
     */
    public double estimateSampleSizeForRelativeError(double relativeError) {
        if (statistic.getCount() < 2.0) {
            return Double.NaN;
        }
        if (relativeError <= 0.0){
            throw new IllegalArgumentException("The relative error bound must be > 0.0");
        }
        double adjRE = relativeError/(1.0 + relativeError);
        double var = statistic.getVariance();
        double alpha = 1.0 - statistic.getConfidenceLevel();
        double ao2 = alpha / 2.0;
        double z = Normal.stdNormalInvCDF(1.0 - ao2);
        double dn = adjRE* statistic.getAverage();
        return Math.ceil(var * (z * z) / (dn * dn));
    }

    /**
     *
     * @return the estimated integral
     */
    public double evaluate() {
        double numNeeded = runInitialSample();
        int n = (int) Math.min(numNeeded, maxSampleSize);
        sample(n);
        return statistic.getAverage();
    }

    /**
     *
     * @return the sampling statistics
     */
    public Statistic getStatistic(){
        return statistic.newInstance();
    }

    /**
     * The purpose of the initial sample is to estimate the variability
     * and determine an approximate number of additional samples needed
     * to meet the desired absolute error.
     *
     * @return the number of additional samples needed
     */
    public double runInitialSample() {
        if (checkRelativeError()) {
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
    protected double sample(int n) {
        if (resetStreamOptionOn) {
            mySampler.resetStartStream();
            if(isAntitheticOptionOn()){
                myAntitheticSampler.resetStartStream();
            }
        }
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

    public long getMaxSampleSize() {
        return maxSampleSize;
    }

    public void setMaxSampleSize(int maxSampleSize) {
        if (maxSampleSize < initialSampleSize) {
            throw new IllegalArgumentException("The maximum sample size must be >= " + initialSampleSize);
        }
        this.maxSampleSize = maxSampleSize;
    }

    public double getDesiredAbsError() {
        return desiredAbsError;
    }

    public void setDesiredAbsError(double desiredAbsError) {
        if (desiredAbsError <= 0.0) {
            throw new IllegalArgumentException("The desired relative precision must be > 0.0");
        }
        this.desiredAbsError = desiredAbsError;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MC1DIntegration");
        sb.append(System.lineSeparator());
        sb.append("initial Sample Size = ").append(initialSampleSize);
        sb.append(System.lineSeparator());
        sb.append("max Sample Size = ").append(maxSampleSize);
        sb.append(System.lineSeparator());
        sb.append("desired Abs Error = ").append(desiredAbsError);
        sb.append(System.lineSeparator());
        sb.append("reset Stream OptionOn = ").append(resetStreamOptionOn);
        sb.append(System.lineSeparator());
        sb.append("Integration Intervals = ");
        sb.append(System.lineSeparator());
        int k = 1;
        for(Interval i: myIntervals){
            sb.append("interval " + i + " : ");
            sb.append(i).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append("Was absolute error criterion met? = ");
        sb.append(checkRelativeError());
        sb.append(System.lineSeparator());
        sb.append("Estimated sample size needed to meet criteria = ");
        sb.append(estimateSampleSize());
        sb.append(System.lineSeparator());
        sb.append("**** Sampling results ****");
        sb.append(System.lineSeparator());
        sb.append(statistic.toString());
        return sb.toString();
    }

    public static void main(String[] args) {
        double a = 0.0;
        double b = 1.0;

        class TestFunc implements FunctionMVIfc {

            public double fx(double[] x) {
                return (4.0*x[0]*x[0]*x[1] + x[1]*x[1]);
            }

        }

        TestFunc f = new TestFunc();
        Interval xInterval = new Interval(a, b);
        Interval yInterval = new Interval(a, b);
        List<Interval> intervalList = new ArrayList<>();
        intervalList.add(xInterval);
        intervalList.add(yInterval);
        MVIndependentRV sampler = new MVIndependentRV(2, new UniformRV(0.0, 1.0));
        MCIntegration mc = new MCIntegration(intervalList, f, sampler);
        mc.setConfidenceLevel(0.99);
        mc.setDesiredAbsError(0.01);

        mc.runInitialSample();
        System.out.println(mc);
        System.out.println();
        mc.evaluate();
        System.out.println(mc);

    }


}
