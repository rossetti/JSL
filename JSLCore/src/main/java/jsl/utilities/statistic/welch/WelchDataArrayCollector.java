package jsl.utilities.statistic.welch;

import jsl.utilities.statistic.Statistic;

import java.io.PrintWriter;
import java.util.Objects;

public class WelchDataArrayCollector extends AbstractWelchDataCollector {

    /**
     *  The maximum number of observations possible
     */
    private final int myMaxNumObs;

    /**
     *  The maximum number of replications possible
     */
    private final int myMaxNumReps;

    /**
     *  rows are the observations, columns are the replications
     */
    private final double[][] myData;

    /**
     *  The observed number of replications
     */
    private int myRepCount = 0;

    /**
     *  Used to count the number of observations when processing a replication
     */
    private int myRowCount = 0;

    public WelchDataArrayCollector(int maxNumObs, int maxNumReps, StatisticType statisticType,
                                   String name, double batchSize) {
        super(statisticType, name, batchSize);
        if (maxNumObs <= 0) {
            throw new IllegalArgumentException("The maximum number of observations must be > 0");
        }
        if (maxNumReps <= 0) {
            throw new IllegalArgumentException("The maximum number of replications must be > 0");
        }
        myMaxNumObs = maxNumObs;
        myMaxNumReps = maxNumReps;
        myData = new double[myMaxNumObs][myMaxNumReps];
    }

    /**
     * Sets all the data to Double.NaN
     */
    public final void clearData() {
        for (int r = 0; r < myData.length; r++) {
            for (int c = 0; c < myData[r].length; c++) {
                myData[r][c] = Double.NaN;
            }
        }
    }

    /**
     * Welch average is across each replication for each observation
     *
     * @return an array of the Welch averages
     */
    public final double[] getWelchAverages() {
        int nRows = getMinNumberOfRowsAcrossReplications();
//        System.out.println("nRows = " + nRows);
        double[] w = new double[nRows];
        Statistic s = new Statistic();
        for (int r = 0; r < w.length; r++) {
            s.collect(myData[r]);
            w[r] = s.getAverage();
            s.reset();
        }
        return w;
    }

    /**
     * Gets an array that contains the cumulative average over the Welch
     * Averages
     *
     * @return returns an array that contains the cumulative average
     */
    public final double[] getWelchCumulativeAverages() {
        double[] w = getWelchAverages();
        double[] cs = new double[w.length];
        Statistic s = new Statistic();
        for (int r = 0; r < w.length; r++) {
            s.collect(w[r]);
            cs[r] = s.getAverage();
        }
        return cs;
    }

    /**
     * Columns are the replications, rows are the data
     *
     * @return a copy of the data
     */
    public final double[][] getData() {
        int nRows = getMinNumberOfRowsAcrossReplications();
        int nCols = getNumberOfReplications();
        double[][] data = new double[nRows][nCols];
        for (int r = 0; r < data.length; r++) {
            System.arraycopy(myData[r], 0, data[r], 0, nCols);
        }
        return data;
    }

    /**
     * @param repNum the replication number 1, 2, etc
     * @return the within replication data for the indicated replication
     */
    public final double[] getReplicationData(int repNum) {
        if (repNum > getNumberOfReplications()) {
            return new double[0];
        }
        int nRows = getMinNumberOfRowsAcrossReplications();
        double[] data = new double[nRows];
        for (int r = 0; r < nRows; r++) {
            data[r] = myData[r][repNum - 1];
        }
        return data;
    }

    /**  If no replications have been completed this returns 0
     *
     * @return the minimum number of observations (rows) across all the collected replications
     */
    public final int getMinNumberOfRowsAcrossReplications(){
        long min = getMinNumberOfObservationsAcrossReplications();
        return Math.toIntExact(min);
    }

    /**
     * Writes out the number of observations to the supplied PrintWriter This
     * results in a comma separated value file that has each rows
     * containing each observation for each replication and each replication
     * as columns. The last two columns are avg is the average across the replications and cumAvg.
     * The file is flushed and closed.
     *
     * The header row is: Rep1, Rep2, ..., RepN, Avg, CumAvg
     *
     * @param out the PrintWriter
     */
    public final void writeCSVWelchData(PrintWriter out) {
        Objects.requireNonNull(out, "The PrintWriter was null");
        int nRows = getMinNumberOfRowsAcrossReplications();
        int nCols = getNumberOfReplications();

        for (int c = 0; c < nCols; c++) {
            out.print("Rep" + (c + 1));
            out.print(",");
        }
        out.print("Avg");
        out.print(", ");
        out.println("CumAvg");

        double[] w = getWelchAverages();
        double[] ca = getWelchCumulativeAverages();

        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < nCols; c++) {
                out.print(myData[r][c]);
                out.print(", ");
            }
            out.print(w[r]);
            out.print(", ");
            out.print(ca[r]);
            out.println();
        }
        out.flush();
        out.close();
    }

    /**
     * Writes out all of the observations to the supplied PrintWriter This
     * results in a comma separated value file that has two columns: Avg, CumAvg
     * containing each Welch plot data point for all of the observations.
     *
     * The file is flushed and closed.
     *
     * @param out  the PrintWriter
     */
    public final void writeCSVWelchPlotData(PrintWriter out) {
        Objects.requireNonNull(out, "The PrintWriter was null");
        double[] w = getWelchAverages();
        double[] ca = getWelchCumulativeAverages();
        out.print("Avg");
        out.print(", ");
        out.println("CumAvg");
        for (int i = 0; i < w.length; i++) {
            out.print(w[i]);
            out.print(", ");
            out.println(ca[i]);
        }
        out.flush();
        out.close();
    }

    @Override
    public void setUpCollector() {
        super.setUpCollector();
        myRepCount = 0;
        myRowCount = 0;
        clearData();
    }

    @Override
    public void beginReplication() {
        super.beginReplication();
        myRowCount = 0;
    }

    @Override
    public void collect(double time, double value) {
        if (myStatType == StatisticType.TALLY) {
            collectTallyObservations(time, value);
        } else {
            collectTimePersistentObservations(time, value);
        }
    }

    private void collectTallyObservations(double time, double value) {
        myWithinRepStats.collect(value); // collect with weight = 1.0
        if (myWithinRepStats.getCount() >= myBatchSize) {
            // form a batch, a batch represents an observation to write to the file
            //myObsCount++;
            // need to observe time between observations
            if (myWithinRepStats.getCount() >= 2) {
                // enough observations to collect time between
                myTBOStats.collect(time - myLastTime);
            }
            // need to save the observation
            myLastValue = myWithinRepStats.getAverage();
            myLastTime = time;
            saveObservation(myLastValue);
            // clear the batching
            myWithinRepStats.reset();
        }
    }

    private void saveObservation(double observation) {
        if ((myRowCount < myMaxNumObs) && (myRepCount < myMaxNumReps)) {
            myData[myRowCount][myRepCount] = observation;
            myRowCount++;
            myObsCount++;
        }
    }

    private void collectTimePersistentObservations(double time, double value) {
        // need to collected time weighted statistics
        // need current time minus previous time to start
        if (time <= 0.0) {
            // starting
            myLastTime = 0.0;
            myLastValue = value;
        } else {
            // first time has occurred
            // compute time of next batch, myBatchSize is deltaT, each obs is a batch of size deltaT
            double tb = (myObsCount+1) * myBatchSize;
            if (time > tb) {
                // then a batch can be formed
                // close out the batch at time tb
                updateTimeWeightedStatistic(tb);
                // an observation is a batch of size deltaT
                //myObsCount++;
                myTBOStats.collect(myBatchSize);
                // record the time average during the deltaT
                saveObservation(myWithinRepStats.getAverage());
                //reset the time average for the next interval
                myWithinRepStats.reset();
                // update the last time to the beginning of interval
                myLastTime = tb;
            }
            // continue collecting new value and new time for new interval
            updateTimeWeightedStatistic(time);
            // update for new value and new time
            myLastValue = value;
            myLastTime = time;
        }
    }

    private void updateTimeWeightedStatistic(double time) {
        double weight = time - myLastTime;
        if (weight < 0.0) {
            weight = 0.0;
        }
        // last value persisted for (time - myLastTime)
        myWithinRepStats.collect(myLastValue, weight); // collect weighted by time
    }

    @Override
    public void endReplication() {
        super.endReplication();
        myRepCount++;
    }

    @Override
    public void cleanUpCollector() {

    }


}
