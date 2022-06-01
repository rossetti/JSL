package jsl.utilities.random.mcintegration;

import jsl.utilities.distributions.Normal;
import jsl.utilities.statistic.Statistic;

/**
 * Provides for the running of a monte-carlo experiment.
 *
 * The simulation is performed in two loops: an outer loop called the macro replications and an inner loop called the micro replications.
 * The user specifies a desired (half-width) error bound, an initial sample size (k), and a maximum sample size limit (M) for
 * the macro replications.  The initial sample size is used to generate a pilot sample from which an estimate of the number of
 * samples needed to meet the absolute precision criteria. Let's call the estimated sample size, m.  If m > k, then an
 * additional (m-k) samples will be taken or until the error criteria is met or the maximum number of samples M is reached.
 * Thus, if m > M, and the error criterion is not met during the macro replications a total of M observations will be observed.
 * Thus, the total number of macro replications will not exceed M.  If the error criteria is met before M is reached, the
 * number of macro replications will be somewhere between k and M. The total number of macro replications can be
 * found by using the getStatistic() method to get the macro replication statistics and using the getCount() method.
 * Let's call the total number of macro replications executed, n.  The reason that the simulation occurs in two loops is
 * to make it more likely that the observed values for the macro replications are normally distributed because they will
 * be the sample average across the micro replications.  Thus, the theory for the stopping criteria and estimation of
 * the number of needed samples will be more likely to be valid.
 * <p>
 * For each of the n, macro replications, a set of micro replications will be executed. Let r be the number of micro replications.
 * The micro replications represent the evaluation of r observations of the Monte-Carlo evaluation of the function supplied
 * to estimate the integral.
 * <p>
 * Thus, the total number of observations of the function representing the integral will be n x r. The number of
 * micro replications is controlled by the user via the setMicroRepSampleSize() method. There is no error criteria checking for
 * the micro replications.
 * <p>
 * By default, the number of macro replications should be relatively small and the number of micro
 * replications large.  Specific settings will be problem dependent.  The default initial sample size, k is 10, with a
 * maximum number of macro replications of M = 100.  The default half-width error bound is 0.0001.  The default setting
 * of the number of micro replications, r, is 1000.  Again, these are all adjustable by the user.
 * <p>
 * The user can check if the error criteria was met after the evaluation. If it is not met, the user can
 * adjust the initial sample size, desired error, maximum sample size, or number of micro replications and run another evaluation.
 * <p>
 * The statistics associated with the estimate are readily available. The user may
 * reset the underlying random number stream if a reproducible result is desired within the same execution frame.
 * <p>
 * By default, the underlying random number stream is not reset with each invocation of the evaluate() method.
 * The default confidence level is 95 percent.
 * <p>
 * Be aware that small desired absolute error may result in large execution times.
 * <p>
 * Implementors of sub-classes of this abstract base class are responsible for implementing the abstract method,
 * replication(int j). This method is responsible for computing a single evaluation of the simulation model.
 */
public abstract class MCExperiment implements MCExperimentIfc {
    protected final Statistic macroReplicationStatistics = new Statistic();
    protected final Statistic replicationStatistics = new Statistic();
    protected int initialSampleSize = 10;
    protected int maxMacroRepSampleSize = 100;
    protected double desiredHWErrorBound = 0.0001;
    protected boolean resetStreamOptionOn = false;
    protected int microRepSampleSize = 1000;

    @Override
    public int getMicroRepSampleSize() {
        return microRepSampleSize;
    }

    @Override
    public void setMicroRepSampleSize(int microRepSampleSize) {
        if (microRepSampleSize <= 0.0) {
            throw new IllegalArgumentException("The micro replication sample size must be >= 1");
        }
        this.microRepSampleSize = microRepSampleSize;
    }

    @Override
    public void setConfidenceLevel(double level) {
        macroReplicationStatistics.setConfidenceLevel(level);
    }

    @Override
    public boolean checkStoppingCriteria() {
        if (macroReplicationStatistics.getCount() < 2.0) {
            return false;
        }
        return macroReplicationStatistics.getHalfWidth() <= getDesiredHWErrorBound();
    }

    @Override
    public double estimateSampleSize() {
        if (macroReplicationStatistics.getCount() < 2.0) {
            return Double.NaN;
        }
        long sampleSize = macroReplicationStatistics.estimateSampleSize(getDesiredHWErrorBound());
        return (double) sampleSize;
    }

    @Override
    public double estimateSampleSizeForRelativeError(double relativeError) {
        if (macroReplicationStatistics.getCount() < 2.0) {
            return Double.NaN;
        }
        if (relativeError <= 0.0) {
            throw new IllegalArgumentException("The relative error bound must be > 0.0");
        }
        double adjRE = relativeError / (1.0 + relativeError);
        double var = macroReplicationStatistics.getVariance();
        double alpha = 1.0 - macroReplicationStatistics.getConfidenceLevel();
        double ao2 = alpha / 2.0;
        double z = Normal.stdNormalInvCDF(1.0 - ao2);
        double dn = adjRE * macroReplicationStatistics.getAverage();
        return Math.ceil(var * (z * z) / (dn * dn));
    }

    @Override
    public double runSimulation() {
        macroReplicationStatistics.reset();
        double numNeeded = runInitialSample();
        int k = maxMacroRepSampleSize - initialSampleSize;
        int m = (int) Math.min(numNeeded, k);
        // error criterion may have been met by initial sample,
        // in which case m = 0 and no further micro-replications will be run
        beforeMacroReplications();
        boolean converged = false;
        for (int i = 1; i <= m; i++) {
            macroReplicationStatistics.collect(runMicroReplications());
            if (checkStoppingCriteria()) {
                converged = true;
                break;
            }
        }
        if (!converged){
            // ran the estimated m, but did not meet the criteria, continue running up to max
            for (int i = 1; i <= (k - m); i++) {
                macroReplicationStatistics.collect(runMicroReplications());
                if (checkStoppingCriteria()) {
                    converged = true;
                    break;
                }
            }
        }
        afterMacroReplications();
        return macroReplicationStatistics.getAverage();
    }

    /**
     * Allows insertion of code before the macro replication loop
     */
    protected void beforeMacroReplications() {

    }

    /**
     *  Allows insertion of code before the macro replications run
     */
    protected void afterMacroReplications() {

    }

    @Override
    public Statistic getStatistic() {
        return macroReplicationStatistics.newInstance();
    }

    @Override
    public double runInitialSample() {
        macroReplicationStatistics.reset();
        for (int i = 1; i <= initialSampleSize; i++) {
            macroReplicationStatistics.collect(runMicroReplications());
            if (checkStoppingCriteria()) {
                return 0.0; // met criteria, no more needed
            }
        }
        // ran through entire initial sample, estimate requirement
        double m = estimateSampleSize();
        // m is the estimated total needed assuming no sampling has been done
        // it could be possible that m is estimated less than the initial sample size
        // handle that case with max
        return Math.max(0, m - initialSampleSize);
    }

    /**
     * @return returns the sample average across the replications
     */
    protected double runMicroReplications() {
        replicationStatistics.reset();
        beforeMicroReplications();
        for (int r = 1; r <= microRepSampleSize; r++) {
            replicationStatistics.collect(replication(r));
        }
        afterMicroReplications();
        return replicationStatistics.getAverage();
    }

    /**
     *  Allows insertion of code before the micro replications run
     */
    protected void beforeMicroReplications(){

    }

    /**
     *  Allows insertion of code after the micro replications run
     */
    protected void afterMicroReplications(){

    }

    /**
     * Runs the rth replication for a sequence of replications
     * r = 1, 2, ... , getMicroRepSampleSize()
     *
     * @param r the number of the replication in the sequence of replications
     * @return the simulated results from the replication
     */
    abstract protected double replication(int r);

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
        return maxMacroRepSampleSize;
    }

    @Override
    public void setMaxSampleSize(int maxSampleSize) {
        if (maxSampleSize < initialSampleSize) {
            throw new IllegalArgumentException("The maximum sample size must be >= " + initialSampleSize);
        }
        this.maxMacroRepSampleSize = maxSampleSize;
    }

    @Override
    public double getDesiredHWErrorBound() {
        return desiredHWErrorBound;
    }

    @Override
    public void setDesiredHWErrorBound(double desiredHWErrorBound) {
        if (desiredHWErrorBound <= 0.0) {
            throw new IllegalArgumentException("The desired relative precision must be > 0.0");
        }
        this.desiredHWErrorBound = desiredHWErrorBound;
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
        sb.append("max Sample Size = ").append(maxMacroRepSampleSize);
        sb.append(System.lineSeparator());
        sb.append("reset Stream OptionOn = ").append(resetStreamOptionOn);
        sb.append(System.lineSeparator());
        sb.append("Estimated sample size needed to meet criteria = ");
        sb.append(estimateSampleSize());
        sb.append(System.lineSeparator());
        sb.append("desired half-width error bound = ").append(desiredHWErrorBound);
        sb.append(System.lineSeparator());
        double hw = macroReplicationStatistics.getHalfWidth();
        sb.append("actual half-width = ").append(hw);
        sb.append(System.lineSeparator());
        sb.append("error gap (hw - bound) = ").append(hw - desiredHWErrorBound);
        sb.append(System.lineSeparator());
        sb.append("-----------------------------------------------------------");
        sb.append(System.lineSeparator());
        boolean converged = checkStoppingCriteria();
        if (!converged){
            sb.append("The half-width criteria was not met!");
            sb.append(System.lineSeparator());
            sb.append("The user should consider one of the following:");
            sb.append(System.lineSeparator());
            sb.append("1. increase the desired error bound using setDesiredHWErrorBound()");
            sb.append(System.lineSeparator());
            sb.append("2. increase the number of macro replications using setMaxSampleSize()");
            sb.append(System.lineSeparator());
            sb.append("2. increase the number of micro replications using setMicroRepSampleSize()");
        } else {
            sb.append("The half-width criteria was met!");
        }
        sb.append(System.lineSeparator());
        sb.append("-----------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("**** Sampling results ****");
        sb.append(System.lineSeparator());
        sb.append("Number of macro replications executed = ");
        sb.append(macroReplicationStatistics.getCount());
        sb.append(System.lineSeparator());
        sb.append("Number of micro replications per macro replication = ");
        sb.append(microRepSampleSize);
        sb.append(System.lineSeparator());
        sb.append("Total number of observations = ");
        sb.append(macroReplicationStatistics.getCount()*microRepSampleSize);
        sb.append(System.lineSeparator());
        if (macroReplicationStatistics.getCount() == 0.0){
            sb.append("**** There were no macro replications executed for results.");
        } else {
            sb.append(macroReplicationStatistics);
        }
        return sb.toString();
    }
}
