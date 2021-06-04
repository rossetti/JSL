package jsl.utilities.statistic.welch;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.WeightedStatistic;

import java.util.ArrayList;
import java.util.Objects;

/**
 * An abstract base class for building collectors of Welch data
 */
abstract public class AbstractWelchDataCollector implements WelchDataCollectorIfc {

    /**
     * Counts the observations when processing a replication
     */
    protected long myObsCount = 0;

    /**
     * Holds the number of observations for each of the replications, zero is the
     * first replication
     */
    protected final ArrayList<Long> myObsCountsForReps;

    /**
     * Holds the average time between observations for each replication
     */
    protected final ArrayList<Double> myAvgTBOForReps;

    /**
     * Holds the time of the last observation for each replication
     */
    protected final ArrayList<Double> myTimeOfLastObsForReps;

    /**
     * Used to collect the average when processing a replication
     */
    protected final WeightedStatistic myWithinRepStats;

    /**
     * Used to collect the overall sample average across observations
     */
    protected final Statistic myRepStat;

    /**
     * Used to collect the time between observations when processing a replication
     */
    protected final WeightedStatistic myTBOStats;

    /**
     * Holds the average of the observations for each replication
     */
    protected final ArrayList<Double> myAveragesForReps;

    /**
     * The time that the last observation occurred. The last observed time.
     */
    protected double myLastTime = Double.NaN;

    /**
     * The observation at the last observed time.
     */
    protected double myLastValue = Double.NaN;

    /**
     * The size associated with batching the within replication observations.
     * If the data is tally based, then it is the number of observations per batch.
     * If the data is observation-based, then it is the time period over which
     * the time average is computed.
     */
    protected final double myBatchSize;

    protected final StatisticType myStatType;

    protected final String myName;

    /**
     * @param statisticType the type of statistic TALLY or TIME_PERSISTENT
     * @param name          the name of the observations being collected
     * @param batchSize     the amount of batching to perform on the observations within a replication
     */
    public AbstractWelchDataCollector(StatisticType statisticType, String name, double batchSize) {
        Objects.requireNonNull(statisticType, "The type of statistic was null");
        Objects.requireNonNull(name, "The name of the observations was null");
        myName = name;
        if (batchSize <= 0.0) {
            throw new IllegalArgumentException("The batch size must be > 0.0");
        }
        myStatType = statisticType;
        myBatchSize = batchSize;
        myObsCountsForReps = new ArrayList<>();
        myAvgTBOForReps = new ArrayList<>();
        myAveragesForReps = new ArrayList<>();
        myTimeOfLastObsForReps = new ArrayList<>();
        //       myAvgNumObsPerBatchForReps = new ArrayList<>();
        myWithinRepStats = new WeightedStatistic();
        myTBOStats = new WeightedStatistic();
        myRepStat = new Statistic();
    }

    @Override
    public final double getBatchSize() {
        return myBatchSize;
    }

    @Override
    public final double getLastTime() {
        return myLastTime;
    }

    @Override
    public final double getLastValue() {
        return myLastValue;
    }

    @Override
    public final long[] getNumberOfObservationsForEachReplication() {
        return JSLArrayUtil.toPrimitiveLong(myObsCountsForReps);
    }

    @Override
    public final double[] getAvgTimeBtwObservationsForEachReplication() {
        return JSLArrayUtil.toPrimitiveDouble(myAvgTBOForReps);
    }

    public final double[] getReplicationAverages() {
        return JSLArrayUtil.toPrimitiveDouble(myAveragesForReps);
    }

    public final double[] getTimeOfLastObservationForReps() {
        return JSLArrayUtil.toPrimitiveDouble(myTimeOfLastObsForReps);
    }

    @Override
    public final int getNumberOfReplications() {
        return myObsCountsForReps.size();
    }

    @Override
    public void setUpCollector() {
        myObsCountsForReps.clear();
        myAvgTBOForReps.clear();
        myAveragesForReps.clear();
        myTimeOfLastObsForReps.clear();
        myObsCount = 0;
        myLastTime = Double.NaN;
        myLastValue = Double.NaN;
        myWithinRepStats.reset();
        myTBOStats.reset();
        myRepStat.reset();
    }

    @Override
    public void beginReplication() {
        myObsCount = 0;
        myLastTime = Double.NaN;
        myLastValue = Double.NaN;
        myWithinRepStats.reset();
        myTBOStats.reset();
        myRepStat.reset();
    }

    @Override
    public void endReplication() {
        myObsCountsForReps.add(myObsCount);
        if (myObsCount > 0) {
            myAvgTBOForReps.add(myTBOStats.getAverage());
            myAveragesForReps.add(myRepStat.getAverage());
            myTimeOfLastObsForReps.add(myLastTime);
        } else {
            myAvgTBOForReps.add(Double.NaN);
            myAveragesForReps.add(Double.NaN);
            myTimeOfLastObsForReps.add(Double.NaN);
        }
    }

}
