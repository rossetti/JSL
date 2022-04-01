package jsl.utilities.statistic.welch;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.statistic.BatchStatistic;
import jsl.utilities.statistic.Statistic;

import java.util.Arrays;
import java.util.Objects;

/**
 *  The purpose of this interface is to define the behavior of implementations that collect data for
 *  the making of Welch plots.  The collection should work for either observation or time-persistent
 *  data and should not depend upon whether or not the data is produced directly from a running
 *  JSL model.  This is to facilitate the reuse of code for data that is generated from other sources
 *  such as files or other simulation models.  The collection model assumes the following behavior:
 *
 *  1) There is a setup phase to prepare the collector.
 *  2) There is a need to indicate the beginning of a replication.
 *  3) There is a requirement to collect/record the individual observations during a replication.
 *  4) There is a requirement that the collected observations be stored for processing. The frequency of
 *  storage may be different than the frequency of collected observations. For example, the raw observations
 *  may be batched in order to facilitate analysis and plotting.
 *  5) There is a need to indicate the ending of a replication.
 *  6) There is a cleanup phase to close up the collector.
 *
 */
public interface WelchDataCollectorIfc {

    /**
     * Gets an array of the partial sum process for the provided data Based on
     * page 2575 Chapter 102 Nelson Handbook of Industrial Engineering,
     * Quantitative Methods in Simulation for producing a partial sum plot The
     * batch means array is used as the data
     *
     * @param bm The BatchStatistic
     * @return n array of the partial sums
     */
    static double[] getPartialSums(BatchStatistic bm) {
        Objects.requireNonNull(bm,"The BatchStatistic was null");
        double avg = bm.getAverage();
        double[] data = bm.getBatchMeanArrayCopy();
        return getPartialSums(avg, data);
    }

    /**
     * Gets an array of the partial sum process for the provided data Based on
     * page 2575 Chapter 102 Nelson Handbook of Industrial Engineering,
     * Quantitative Methods in Simulation for producing a partial sum plot
     *
     * @param avg the average of the supplied data array
     * @param data the data
     * @return the array of partial sums
     */
    static double[] getPartialSums(double avg, double[] data) {
        Objects.requireNonNull(data,"The data array was null");
        int n = data.length;
        double[] s = new double[n + 1];
        if (n == 1) {
            s[0] = 0.0;
            s[1] = 0.0;
            return s;
        }
        // first pass computes cum sums
        s[0] = 0.0;
        for (int j = 1; j <= n; j++) {
            s[j] = s[j - 1] + data[j - 1];
        }
        // second pass computes partial sums
        for (int j = 1; j <= n; j++) {
            s[j] = j * avg - s[j];
        }
        return s;
    }

    /**
     * Uses the batch means array from the BatchStatistic to compute the
     * positive bias test statistic
     *
     * @param bm the BatchStatistic
     * @return the positive bias test statistic
     */
    static double getPositiveBiasTestStatistic(BatchStatistic bm) {
        Objects.requireNonNull(bm,"The BatchStatistic was null");
        double[] data = bm.getBatchMeanArrayCopy();
        return getPositiveBiasTestStatistic(data);
    }

    /**
     * Computes initialization bias (positive) test statistic based on algorithm
     * on page 2580 Chapter 102 Nelson Handbook of Industrial Engineering,
     * Quantitative Methods in Simulation
     *
     * @param data the data
     * @return test statistic to be compared with F distribution
     */
    static double getPositiveBiasTestStatistic(double[] data) {
        Objects.requireNonNull(data,"The data array was null");
        //find min and max of partial sum series!
        int n = data.length / 2;
        double[] x1 = Arrays.copyOfRange(data, 0, n);
        double[] x2 = Arrays.copyOfRange(data, n + 1, 2 * n);
        Statistic s = new Statistic();
        s.collect(x1);
        double a1 = s.getAverage();
        s.reset();
        s.collect(x2);
        double a2 = s.getAverage();
        double[] ps1 = getPartialSums(a1, x1);
        double[] ps2 = getPartialSums(a2, x2);
        int mi1 = Statistic.getIndexOfMax(ps1);
        double max1 = Statistic.getMax(ps1);
        int mi2 = Statistic.getIndexOfMax( ps2);
        double max2 = Statistic.getMax(ps2);
        double num = mi2 * (n - mi2) * max1 * max1;
        double denom = mi1 * (n - mi1) * max2 * max2;
        if (max2 == 0.0) {
            return Double.NaN;
        }
        if (denom == 0.0) {
            return Double.NaN;
        }
        return num / denom;
    }

    /**
     * Uses the batch means array from the BatchStatistic to compute the
     * positive bias test statistic
     *
     * @param bm the BatchStatistic
     * @return the computed test statistic
     */
    static double getNegativeBiasTestStatistic(BatchStatistic bm) {
        Objects.requireNonNull(bm,"The BatchStatistic was null");
        double[] data = bm.getBatchMeanArrayCopy();
        return getNegativeBiasTestStatistic(data);
    }

    /**
     * Computes initialization bias (negative) test statistic based on algorithm
     * on page 2580 Chapter 102 Nelson Handbook of Industrial Engineering,
     * Quantitative Methods in Simulation
     *
     * @param data the data to test
     * @return test statistic to be compared with F distribution
     */
    static double getNegativeBiasTestStatistic(double[] data) {
        Objects.requireNonNull(data,"The data array was null");
        //find min and max of partial sum series!
        int n = data.length / 2;
        double[] x1 = Arrays.copyOfRange(data, 0, n);
        double[] x2 = Arrays.copyOfRange(data, n + 1, 2 * n);
        Statistic s = new Statistic();
        s.collect(x1);
        double a1 = s.getAverage();
        s.reset();
        s.collect(x2);
        double a2 = s.getAverage();
        double[] ps1 = getPartialSums(a1, x1);
        double[] ps2 = getPartialSums(a2, x2);
        int mi1 = Statistic.getIndexOfMin(ps1);
        double min1 = Statistic.getMin(ps1);
        int mi2 = Statistic.getIndexOfMin(ps2);
        double min2 = Statistic.getMin(ps2);
        double num = mi2 * (n - mi2) * min1 * min1;
        double denom = mi1 * (n - mi1) * min2 * min2;
        if (min2 == 0.0) {
            return Double.NaN;
        }
        if (denom == 0.0) {
            return Double.NaN;
        }
        return num / denom;
    }

    /**
     * The number of full replications observed
     *
     * @return the number of replications observed
     */
    int getNumberOfReplications();

    /**
     * The average time between observations in each replication returned as an
     * array. 0 element is the first replication observed. If no replications
     * have been observed then the array will be empty.
     *
     * @return the average time between observations
     */
    double[] getAvgTimeBtwObservationsForEachReplication();

    /**
     * The number of observations in each replication returned as an array. 0
     * element is the first replication count. If no replications
     * have been observed then the array will be empty.
     *
     * @return the number of observations for each replication
     */
    long[] getNumberOfObservationsForEachReplication();

    /** If the have been no replications, then this returns 0
     *
     * @return the minimum number of observations across the replications
     */
    default long getMinNumberOfObservationsAcrossReplications(){
        long[] counts = getNumberOfObservationsForEachReplication();
        if (counts.length == 0){
            return 0;
        }
        return JSLArrayUtil.getMin(counts);
    }

    /** The size of a batch can be considered either in terms of the number of observations
     *  in each batch or as the amount of time covered with each batch
     *
     * @return the size of a batch
     */
    double getBatchSize();

    /**
     *  Should be executed once prior to any collection and should be used to clear
     *  any statistical collection and prepare the collector for collecting data.
     */
    void setUpCollector();

    /**
     *  Should be executed prior to each replication
     */
    void beginReplication();

    /**
     *
     * @param time the time that the observation occurred
     * @param value the value of the observation at the observed time
     */
    void collect(double time, double value);

    /** The time of the observation recorded
     *
     *  @return Returns the time of the last collected value.
     */
    double getLastTime();

    /** The value of the observation recorded
     *
     *  @return Returns the previously collected value.
     */
    double getLastValue();

    /**
     *  Should be executed after each replication
     */
    void endReplication();

    /**
     *  Should be executed once after all replications have been observed
     */
    void cleanUpCollector();
}
