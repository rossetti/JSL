package jsl.utilities.statistic.welch;

import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.observers.ModelElementObserver;
import jsl.simulation.ModelElement;
import jsl.utilities.JSLFileUtil;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Objects;

/**  Collects Welch data in to an array.  The size of the array must be specified
 *   when creating the observer.  Any data that is observed that results in more
 *   data than the array size is ignored (not stored).
 *
 *   Permits the creation of CSV files that hold the data and the Welch plotting data
 *
 *   This is essentially an in memory version of the WelchDataFileObserver
 *
 */
public class WelchDataArrayObserver extends ModelElementObserver {

    private final WelchDataArrayCollector myWelchDataArrayCollector;
    private final ResponseVariable myResponse;

    /** Defaults to a maximum number of observations of 50000 and maximum number of replications of 20
     *
     * @param responseVariable the ResponseVariable or TimeWeighted variable to observe
     * @param batchSize  the batch size for condensing the data
     */
    public WelchDataArrayObserver(ResponseVariable responseVariable, double batchSize){
        this(responseVariable, 50000, 20, batchSize);
    }

    /**
     *
     * @param responseVariable the ResponseVariable or TimeWeighted variable to observe
     * @param maxNumObs the limit on the number of observations in each replication to store
     * @param maxNumReps the limit on the number of replications
     * @param batchSize  the batch size for condensing the data
     */
    public WelchDataArrayObserver(ResponseVariable responseVariable, int maxNumObs, int maxNumReps, double batchSize){
        Objects.requireNonNull(responseVariable,"The response variable cannot be null");
        myResponse = responseVariable;
        StatisticType statType = null;
        if (responseVariable instanceof TimeWeighted){
            statType = StatisticType.TIME_PERSISTENT;
        } else {
            statType = StatisticType.TALLY;
        }
        myWelchDataArrayCollector = new WelchDataArrayCollector(maxNumObs, maxNumReps, statType,
                responseVariable.getName(), batchSize);
        responseVariable.addObserver(this);
    }

    /** Creates a WelchDataArrayObserver with batch size 1
     *
     *  Defaults to a maximum number of observations of 10000 and maximum number of replications of 20
     *
     * @param responseVariable the ResponseVariable to observe
     * @return the created WelchDataArrayObserver
     */
    public static WelchDataArrayObserver createWelchArrayObserver(ResponseVariable responseVariable){
        return new WelchDataArrayObserver(responseVariable,1.0);
    }

    /** Creates a WelchDataArrayObserver with batch size 1
     *
     * @param responseVariable the ResponseVariable or TimeWeighted variable to observe
     * @param maxNumObs the limit on the number of observations in each replication to store
     * @param maxNumReps the limit on the number of replications
     */
    public static WelchDataArrayObserver createWelchArrayObserver(ResponseVariable responseVariable, int maxNumObs, int maxNumReps){
        return new WelchDataArrayObserver(responseVariable, maxNumObs, maxNumReps, 1.0);
    }

    /** Creates a WelchDataArrayObserver with batch size 10. The discretizing interval of 10 time units
     *
     *  Defaults to a maximum number of observations of 10000 and maximum number of replications of 20
     *
     * @param timeWeighted the TimeWeighted to observe
     * @return the created WelchDataArrayObserver
     */
    public static WelchDataArrayObserver createWelchArrayObserver(TimeWeighted timeWeighted){
        return new WelchDataArrayObserver(timeWeighted,10.0);
    }

    /** Creates a WelchDataArrayObserver with batch size 10. The discretizing interval of 10 time units
     *
     *  Defaults to a maximum number of observations of 10000 and maximum number of replications of 20
     *
     * @param timeWeighted the TimeWeighted to observe
     * @param maxNumObs the limit on the number of observations in each replication to store
     * @param maxNumReps the limit on the number of replications
     * @return the created WelchDataArrayObserver
     */
    public static WelchDataArrayObserver createWelchArrayObserver(TimeWeighted timeWeighted, int maxNumObs, int maxNumReps, double deltaTInterval){
        return new WelchDataArrayObserver(timeWeighted, maxNumObs, maxNumReps, deltaTInterval);
    }

    /**
     * This results in a comma separated value file that has each row
     * containing each observation for each replication and each replication
     * as columns. The last two columns are avg is the average across the replications and cumAvg.
     * The file is flushed and closed. The file is stored in the base directory holding the
     * welch data files and has the name of the data with _WelchData.csv appended.
     *
     * The header row is: Rep1, Rep2, ..., RepN, Avg, CumAvg
     */
    public final void makeCSVWelchData() {
        Path outDir = myResponse.getSimulation().getOutputDirectory().getOutDir();
        String fName = myResponse.getName() + "_WelchData.csv";
        Path filePath = outDir.resolve(fName);
        PrintWriter out = JSLFileUtil.makePrintWriter(filePath);
        myWelchDataArrayCollector.writeCSVWelchData(out);
    }

    /**
     * Makes and writes out the welch plot data. Squelches inconvenient IOExceptions
     * The file is stored in the base directory holding the
     * welch data files and has the name of the data with _WelchPlotData.csv appended.
     *
     * The header row is Avg, CumAvg
     */
    public final void makeCSVWelchPlotData() {
        Path outDir = myResponse.getSimulation().getOutputDirectory().getOutDir();
        String fName = myResponse.getName() + "_WelchPlotData.csv";
        Path filePath = outDir.resolve(fName);
        PrintWriter out = JSLFileUtil.makePrintWriter(filePath);
        myWelchDataArrayCollector.writeCSVWelchPlotData(out);
    }

    public String getResponseName(){
        return myResponse.getName();
    }

    @Override
    public String toString(){
        return myWelchDataArrayCollector.toString();
    }

    public double getBatchSize() {
        return myWelchDataArrayCollector.getBatchSize();
    }

    public double getLastTime() {
        return myWelchDataArrayCollector.getLastTime();
    }

    public double getLastValue() {
        return myWelchDataArrayCollector.getLastValue();
    }

    public long[] getNumberOfObservationsForEachReplication() {
        return myWelchDataArrayCollector.getNumberOfObservationsForEachReplication();
    }

    public double[] getAvgTimeBtwObservationsForEachReplication() {
        return myWelchDataArrayCollector.getAvgTimeBtwObservationsForEachReplication();
    }

    public double[] getReplicationAverages() {
        return myWelchDataArrayCollector.getReplicationAverages();
    }

    public double[] getTimeOfLastObservationForReps() {
        return myWelchDataArrayCollector.getTimeOfLastObservationForReps();
    }

    public int getNumberOfReplications() {
        return myWelchDataArrayCollector.getNumberOfReplications();
    }

    /**
     *
     * @return the minimum number of observations across the replications
     */
    public long getMinNumberOfObservationsAcrossReplications() {
        return myWelchDataArrayCollector.getMinNumberOfObservationsAcrossReplications();
    }

    /**
     * Welch average is across each replication for each observation
     *
     * @return an array of the Welch averages
     */
    public double[] getWelchAverages() {
        return myWelchDataArrayCollector.getWelchAverages();
    }

    /**
     * Gets an array that contains the cumulative average over the Welch
     * Averages
     *
     * @return returns an array that contains the cumulative average
     */
    public double[] getWelchCumulativeAverages() {
        return myWelchDataArrayCollector.getWelchCumulativeAverages();
    }

    /** Columns are the replications, rows are the data
     *
     * @return a copy of the data
     */
    public double[][] getData() {
        return myWelchDataArrayCollector.getData();
    }

    /**
     *
     * @param repNum the replication number 1, 2, etc
     * @return the within replication data for the indicated replication
     */
    public double[] getReplicationData(int repNum) {
        return myWelchDataArrayCollector.getReplicationData(repNum);
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        myWelchDataArrayCollector.setUpCollector();
    }

    @Override
    protected void beforeReplication(ModelElement m, Object arg) {
        myWelchDataArrayCollector.beginReplication();
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        myWelchDataArrayCollector.endReplication();
    }

    @Override
    protected void update(ModelElement m, Object arg) {
        ResponseVariable rv = (ResponseVariable)m;
        myWelchDataArrayCollector.collect(rv.getTime(), rv.getValue());
    }

    @Override
    protected void afterExperiment(ModelElement m, Object arg) {
        myWelchDataArrayCollector.cleanUpCollector();
    }
}
