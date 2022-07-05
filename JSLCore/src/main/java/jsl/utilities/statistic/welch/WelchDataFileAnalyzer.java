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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.utilities.statistic.welch;

import jsl.observers.ObservableComponent;
import jsl.observers.ObservableIfc;
import jsl.observers.ObserverIfc;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.JSLFileUtil;
import jsl.utilities.reporting.JSL;
import jsl.utilities.statistic.BatchStatistic;
import jsl.utilities.statistic.Statistic;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * This class knows how to process data collected by the WelchDataFileCollector
 * class and produce "Welch Data". That is for every observation, this file will
 * average across the replications and compute the average across the
 * replications and compute the cumulative sum over the averages.
 *
 * It can make "wpdf" files which are binary DataOutputStream files holding the
 * welch average and the cumulative average for each of the observations.
 *
 * wpdf = Welch Plot Data File
 *
 * It can make a csv file that holds the welch average and cumulative average
 *
 * An Observer can be attached.  It will be notified when a call to get() 
 * occurs. getLastDataPoint(), getLastObservationIndex(), and getLastReplicationIndex()
 * can be used by the observer to determine the value, observation number,
 * and replication of the observation after notification.
 *
 * Unless specifically redirected, files produced by the operation of this class are stored in the same directory
 * (getBaseDirectory()) that the wdf is stored as specified by the supplied WelchFileMetaDataBean information.
 *
 * @author rossetti
 */
public class WelchDataFileAnalyzer implements ObservableIfc {

    public static final int NUMBYTES = 8;

    public static final int MIN_BATCH_SIZE = 10;

    protected final WelchFileMetaDataBean myWFMDBean;

    protected String myBaseName;

    protected final ObservableComponent myObsComponent;

    protected final long[] myObsCounts;

    protected final double[] myTimePerObs;

    protected final double[] myRepAvgs;

    protected final double[] myTimeRepsEnd;

    protected final long myMinObsCount;

    protected final double[] myRowData;

    protected final Statistic myAcrossRepStat;

    protected final Path myPathToWDF;

    protected RandomAccessFile myWDFDataFile;

    private double myLastDataPoint = Double.NaN;

    private long myLastObsIndex = Long.MIN_VALUE;

    private long myLastRepIndex = Long.MIN_VALUE;

    public WelchDataFileAnalyzer(WelchFileMetaDataBean bean) {
        Objects.requireNonNull(bean,"The welch file meta data bean was null");
        if (!bean.isValid()){
            throw new IllegalArgumentException("The supplied WelchFileMetaDataBean did not have valid fields");
        }
        myWFMDBean = bean;
        // set up internal state
        myObsComponent = new ObservableComponent();
        myBaseName = bean.getDataName();
        myObsCounts = bean.getNumObsInEachReplication();
        myTimePerObs = bean.getTimeBtwObsInEachReplication();
        myRepAvgs = bean.getEndReplicationAverages();
        myTimeRepsEnd = bean.getTimeOfLastObsInEachReplication();
        myMinObsCount = bean.getMinNumObsForReplications();
        myRowData = new double[myObsCounts.length];
        myAcrossRepStat = new Statistic();
        // get the path to the data file
        String strPath = bean.getPathToFile();
        myPathToWDF = Paths.get(strPath);
        File wdfDataFile = myPathToWDF.toFile();
        // connect the analyzer to the data in the file
        try {
            myWDFDataFile = new RandomAccessFile(wdfDataFile, "r");
        } catch (IOException ex) {
            String str = "Problem creating RandomAccessFile for " + wdfDataFile.getAbsolutePath();
            JSL.getInstance().LOGGER.error(str, ex);
        }
    }

    /**
     *
     * @param pathToWelchFileMetaDataBeanJson must not be null, must be JSON, must represent WelchFileMetaDataBean
     * @return an optional holding the WelchDataFileAnalyzer
     */
    public static Optional<WelchDataFileAnalyzer> makeFromJSON(Path pathToWelchFileMetaDataBeanJson){
        Optional<WelchFileMetaDataBean> beanOptional = WelchFileMetaDataBean.makeFromJSONNE(pathToWelchFileMetaDataBeanJson);
        if (beanOptional.isPresent()) {
            WelchFileMetaDataBean metaDataBean = beanOptional.get();
            return Optional.of(new WelchDataFileAnalyzer(metaDataBean));
        }
        return Optional.empty();
    }

    /**
     *
     * @return  the meta data bean for the welch file
     */
    public final WelchFileMetaDataBean getWelchFileMetaDataBean(){
        return myWFMDBean;
    }

    /**
     *
     * @return a path to the directory (folder) that holds the analysis files
     */
    public final Path getBaseDirectory(){
        return myPathToWDF.getParent();
    }

    /**
     *
     * @return the base name of the wdf file used in the analysis
     */
    public final Path getBaseFileName(){
        return myPathToWDF.getFileName();
    }

    /**
     *
     * @return the name of the response
     */
    public final String getResponseName(){
        return myBaseName;
    }

    @Override
    public final void addObserver(ObserverIfc observer) {
        myObsComponent.addObserver(observer);
    }

    @Override
    public final void deleteObserver(ObserverIfc observer) {
        myObsComponent.deleteObserver(observer);
    }

    @Override
    public final void deleteObservers() {
        myObsComponent.deleteObservers();
    }

    @Override
    public final boolean contains(ObserverIfc observer) {
        return myObsComponent.contains(observer);
    }

    @Override
    public final int countObservers() {
        return myObsComponent.countObservers();
    }

    /**
     * Returns the last data point read or Double.NaN if none read. Can be used
     * by Observers when data is read.
     *
     * @return the last data point
     */
    public final double getLastDataPoint() {
        return myLastDataPoint;
    }

    /**
     * Makes a file and writes out the welch data to the DataOutputStream. This
     * produces a file with the "wpdf" extension. All observations are written
     * Squelches inconvenient IOExceptions
     *
     * @return the file reference
     */
    public final File makeWelchPlotDataFile() {
        return makeWelchPlotDataFile(myMinObsCount);
    }

    /**
     * Makes a file and writes out the welch data to the DataOutputStream. This
     * produces a file with the "wpdf" extension. wpdf = Welch Plot Data File
     * Squelches inconvenient IOExceptions
     *
     * @param numObs number of observations to write out
     * @return the file reference
     */
    public final File makeWelchPlotDataFile(long numObs) {
        Path path = getBaseDirectory().resolve(myBaseName + ".wpdf");
        File wpdf = JSLFileUtil.makeFile(path);
        try {
            FileOutputStream fout = new FileOutputStream(wpdf);
            DataOutputStream out = new DataOutputStream(fout);
            writeWelchPlotData(out, numObs);
        } catch (IOException ex) {
            JSL.getInstance().LOGGER.error("Unable to make welch data plot file ", ex);
        }
        return wpdf;
    }

    /** This produces a file with the "wpdf" extension. wpdf = Welch Plot Data File
     *
     * Writes out the welch plot data, xbar, cumxbar to the supplied
     * DataOutputStream. The file is flushed and closed.
     *
     * @param out the stream to write to
     * @param numObs number of observations to write out
     * @throws IOException could not write the data to the file for some reason
     */
    public final void writeWelchPlotData(DataOutputStream out, long numObs) throws IOException {
        Objects.requireNonNull(out, "The DataOutputStream was null");
        long n = Math.min(numObs, myMinObsCount);
        Statistic s = new Statistic();
        for (long i = 1; i <= n; i++) {
            double x = getAcrossReplicationAverage(i);
            s.collect(x);
            out.writeDouble(x);
            out.writeDouble(s.getAverage());
        }
        out.flush();
        out.close();
    }

    /**
     * Makes and writes out the welch plot data. Squelches inconvenient IOExceptions
     * The file is stored in the base directory holding the
     * welch data files and has the name of the data with _WelchPlotData.csv appended.
     *
     * The header row is Avg, CumAvg
     *
     * @return the File reference
     */
    public final File makeCSVWelchPlotDataFile() {
        return makeCSVWelchPlotDataFile(myMinObsCount);
    }

    /**
     * Makes and writes out the welch plot data. Squelches inconvenient IOExceptions
     * The file is stored in the base directory holding the
     * welch data files and has the name of the data with _WelchPlotData.csv appended.
     *
     * The header row is Avg, CumAvg
     *
     * @param numObs number of observations to write
     * @return the File reference
     */
    public final File makeCSVWelchPlotDataFile(long numObs) {
        Path path = getBaseDirectory().resolve(myBaseName + "_WelchPlotData.csv");
        File file = JSLFileUtil.makeFile(path);
        PrintWriter pw = JSLFileUtil.makePrintWriter(file);
        try {
            writeCSVWelchPlotData(pw, numObs);
        } catch (IOException ex) {
            JSL.getInstance().LOGGER.error("Unable to make CSV welch data plot file ", ex);
        }
        return file;
    }

    /**
     * Writes out all of the observations to the supplied PrintWriter This
     * results in a comma separated value file that has x_bar and cum_x_bar
     * where x_bar is the average across the replications
     * The header row is Avg, CumAvg
     *
     * @param out the PrintWriter
     * @throws IOException if problem writing
     */
    public final void writeCSVWelchPlotData(PrintWriter out) throws IOException {
        writeCSVWelchPlotData(out, myMinObsCount);
    }

    /**
     * Writes out the number of observations to the supplied PrintWriter This
     * results in a comma separated value file that has x_bar and cum_x_bar
     * where x_bar is the average across the replications. The file is flushed
     * and closed.
     *
     * The header row is Avg, CumAvg
     *
     * @param out the PrintWriter
     * @param numObs how many to write
     * @throws IOException if problem writing
     */
    public final void writeCSVWelchPlotData(PrintWriter out, long numObs) throws IOException {
        Objects.requireNonNull(out, "The PrintWriter was null");
        long n = Math.min(numObs, myMinObsCount);
        out.print("Avg");
        out.print(",");
        out.println("CumAvg");
        Statistic s = new Statistic();
        for (long i = 1; i <= n; i++) {
            double x = getAcrossReplicationAverage(i);
            s.collect(x);
            out.print(x);
            out.print(",");
            out.println(s.getAverage());
        }
        out.flush();
        out.close();
    }

    /**
     * This results in a comma separated value file that has each row
     * containing each observation for each replication and each replication
     * as columns. The last two columns are avg is the average across the replications and cumAvg.
     * The file is flushed and closed. The file is stored in the base directory holding the
     * welch data files and has the name of the data with _WelchData.csv appended.
     *
     * The header row is: Rep1, Rep2, ..., RepN, Avg, CumAvg
     *
     * @throws IOException if problem writing
     */
    public final File makeCSVWelchDataFile() throws IOException {
        return makeCSVWelchDataFile(myMinObsCount);
    }

    /**
     * This results in a comma separated value file that has each rows
     * containing each observation for each replication and each replication
     * as columns. The last two columns are avg is the average across the replications and cumAvg.
     * The file is flushed and closed. The file is stored in the base directory holding the
     * welch data files and has the name of the data with _WelchData.csv appended.
     *
     * The header row is: Rep1, Rep2, ..., RepN, Avg, CumAvg
     *
     * @param numObs how many to write
     * @throws IOException if problem writing
     */
    public final File makeCSVWelchDataFile(long numObs) throws IOException {
        Path path = getBaseDirectory().resolve(myBaseName + "_WelchData.csv");
        File file = JSLFileUtil.makeFile(path);
        PrintWriter pw = JSLFileUtil.makePrintWriter(file);
        writeCSVWelchData(pw, numObs);
        return file;
    }

    /**
     * Writes out the number of observations to the supplied PrintWriter. This
     * results in a comma separated value file that has each rows
     * containing each observation for each replication and each replication
     * as columns. The last two columns are avg is the average across the replications and cumAvg.
     * The file is flushed and closed.
     *
     * The header row is: Rep1, Rep2, ..., RepN, Avg, CumAvg
     *
     * @param out the PrintWriter
     * @throws IOException if problem writing
     */
    public final void writeCSVWelchData(PrintWriter out) throws IOException {
        writeCSVWelchData(out, myMinObsCount);
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
     * @param numObs how many to write
     * @throws IOException if problem writing
     */
    public final void writeCSVWelchData(PrintWriter out, long numObs) throws IOException {
        Objects.requireNonNull(out, "The PrintWriter was null");
        long n = Math.min(numObs, myMinObsCount);
        int nReps = getNumberOfReplications();
        // make the header
        StringJoiner joiner = new StringJoiner(", ");
        for(int i=1;i<=nReps;i++){
            joiner.add("Rep"+i);
        }
        joiner.add("Avg");
        joiner.add("CumAvg");
        out.println(joiner);
        // write each row
        Statistic stat = new Statistic();
        for (long i=1; i<=n; i++){
            getAcrossReplicationData(i,myRowData);
            String row = JSLArrayUtil.toCSVString(myRowData);
            myAcrossRepStat.reset();
            myAcrossRepStat.collect(myRowData);
            double avg = myAcrossRepStat.getAverage();
            stat.collect(avg);
            out.print(row);
            out.print(", ");
            out.print(avg);
            out.print(", ");
            out.println(stat.getAverage());
        }
    }

    /**
     * Returns an array of the Welch averages. Since the number of observations
     * in the file may be very large, this may have memory implications.
     *
     * @param numObs the number of observations to get
     * @return the array of data
     * @throws IOException if there was a problem accessing the file
     */
    public final double[] getWelchAverages(int numObs) throws IOException {
        int n;
        if (numObs <= myMinObsCount) {
            n = numObs;
        } else {
            n = Math.toIntExact(myMinObsCount);
        }
        double[] x = new double[n];
        for (int i = 1; i <= n; i++) {
            x[i - 1] = getAcrossReplicationAverage(i);
        }
        return x;
    }

    /**
     * Returns an array of the Welch averages. Since the number of observations
     * in the file may be very large, this may have memory implications.
     *
     * Squelches any IOExceptions
     *
     * @param numObs the number of observations to get
     * @return the array of data
     */
    public final double[] getWelchAveragesNE(int numObs){
        double[] avgs = new double[0];
        try {
            avgs = getWelchAverages(numObs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return avgs;
    }

    /**
     * Returns an array of the cumulative Welch averages. Since the number of observations
     * in the file may be very large, this may have memory implications.
     *
     * Squelches any IOExceptions
     *
     * @param numObs the number of observations to get
     * @return the array of data
     */
    public final double[] getCumulativeWelchAverages(int numObs){
        double[] avgs = getWelchAveragesNE(numObs);
        double[] cumAvgs = new double[avgs.length];
        Statistic s = new Statistic();
        for (int i=0; i< avgs.length; i++){
            s.collect(avgs[i]);
            cumAvgs[i] = s.getAverage();
        }
        return cumAvgs;
    }

    /**
     * Creates a BatchStatistic that batches the Welch averages according to the
     * batching parameters. Uses the number of observations via
     * getMinNumObservationsInReplications() to determine the number of batches
     * based on MIN_BATCH_SIZE. No data is deleted.
     *
     * @return A BatchStatistic
     * @throws IOException if there was a problem accessing the file
     */
    public final BatchStatistic batchWelchAverages() throws IOException {
        return batchWelchAverages(0, MIN_BATCH_SIZE);
    }

    /**
     * Creates a BatchStatistic that batches the Welch averages according to the
     * batching parameters. Uses the number of observations via
     * getMinNumObservationsInReplications() to determine the number of batches
     * based on MIN_BATCH_SIZE.
     *
     * @param deletePt the number of observations to delete at beginning of
     * series
     * @return A BatchStatistic
     * @throws IOException if there was a problem accessing the file
     */
    public final BatchStatistic batchWelchAverages(int deletePt) throws IOException {
        return batchWelchAverages(deletePt, MIN_BATCH_SIZE);
    }

    /**
     * Creates a BatchStatistic that batches the Welch averages according to the
     * batching parameters. Uses the number of observations via
     * getMinNumObservationsInReplications() to determine the number of batches
     * based on the supplied batch size.
     *
     * @param deletePt the number of observations to delete at beginning of
     * series
     * @param minBatchSize the size of the batches, must be GT 1
     * @return A BatchStatistic
     * @throws IOException if there was a problem accessing the file
     */
    public final BatchStatistic batchWelchAverages(int deletePt, int minBatchSize) throws IOException {
        if (minBatchSize <= 1) {
            throw new IllegalArgumentException("Batch size must be >= 2");
        }
        long n = getMinNumObservationsInReplications();
        long k = n / minBatchSize;
        int minNumBatches = Math.toIntExact(k);
        return batchWelchAverages(deletePt, minNumBatches, minBatchSize, 2);
    }

    /**
     * Creates a BatchStatistic that batches the Welch averages according to the
     * batching parameters. If the minNumBatches x minBatchSize = number of
     * observations then the maxNBMultiple does not matter. Uses a batch
     * multiple of 2.
     *
     * @param deletePt the number of observations to delete at beginning of
     * series
     * @param minNumBatches the minimum number of batches to make
     * @param minBatchSize the minimum batch size
     * @return a BatchStatistic
     * @throws IOException if there was a problem accessing the file
     */
    public final BatchStatistic batchWelchAverages(int deletePt, int minNumBatches, int minBatchSize) throws IOException {
        return batchWelchAverages(deletePt, minNumBatches, minBatchSize, 2);
    }

    /**
     * Creates a BatchStatistic that batches the Welch averages according to the
     * batching parameters. If the minNumBatches x minBatchSize = number of
     * observations then the maxNBMultiple does not matter.
     *
     * @param deletePt the number of observations to delete at beginning of
     * series
     * @param minNumBatches the minimum number of batches to make
     * @param minBatchSize the minimum batch size
     * @param maxNBMultiple the batch means multiple
     * @return the BatchStatistic
     * @throws IOException if there was a problem accessing the file
     */
    public final BatchStatistic batchWelchAverages(int deletePt, int minNumBatches,
            int minBatchSize, int maxNBMultiple) throws IOException {
        if (deletePt < 0) {
            deletePt = 0;
        }
        int k = deletePt + 1;
        BatchStatistic b = new BatchStatistic(minNumBatches, minBatchSize, maxNBMultiple);
        for (long i = k; i <= myMinObsCount; i++) {
            b.collect(getAcrossReplicationAverage(i));
        }
        return b;
    }

    /**
     * The number of observations in each replication
     *
     * @return number of observations in each replication
     */
    public final long[] getObservationCounts() {
        return Arrays.copyOf(myObsCounts, myObsCounts.length);
    }

    /**
     * Returns the average amount of time taken per observation in each of the
     * replications
     *
     * @return the average amount of time taken per observation in each of the
     *       replications
     */
    public final double[] getTimePerObservation() {
        return Arrays.copyOf(myTimePerObs, myTimePerObs.length);
    }

    /**
     * Returns the average within each replication. That is, the average of the
     * observations within each replication. zero is the first replication
     *
     * @return the average within each replication
     */
    public final double[] getReplicationAverages() {
        return Arrays.copyOf(myRepAvgs, myRepAvgs.length);
    }

    /**
     * The average time between observations in the simulation across all the
     * replications. This can be used to determine a warmup period in terms of
     * time.
     *
     * @return average time between observations
     */
    public final double getAverageTimePerObservation() {
        return Statistic.collectStatistics(myTimePerObs).getAverage();
    }

    /**
     * The number of observations across the replications
     *
     * @return number of observations across the replications
     */
    public final long getMinNumObservationsInReplications() {
        return myMinObsCount;
    }

    /**
     * Computes and returns the across replication average for ith row of
     * observations
     *
     * @param i row number
     * @return  the across replication average for ith row
     * @throws IOException if there was trouble with the file
     */
    public final double getAcrossReplicationAverage(long i) throws IOException {
        myAcrossRepStat.reset();
        myAcrossRepStat.collect(getAcrossReplicationData(i, myRowData));
        return myAcrossRepStat.getAverage();
    }

    /**
     * Fills the supplied array with a row of observations across the
     * replications
     *
     * @param i row number
     * @param x array to hold across replication observations
     * @return the array of filled observations
     * @throws IOException if there was trouble with the file
     */
    public final double[] getAcrossReplicationData(long i, double[] x) throws IOException {
        if (x == null) {// make it if it is not supplied
            x = new double[myObsCounts.length];
        }
        if (x.length != myObsCounts.length) {
            throw new IllegalArgumentException("The supplied array's length was not " + myObsCounts.length);
        }
        if (i > getMinNumObservationsInReplications()) {
            throw new IllegalArgumentException("The desired row is larger than " + getMinNumObservationsInReplications());
        }
        for (int j = 1; j <= x.length; j++) {
            x[j - 1] = get(i, j);
        }
        return x;
    }

    /**
     * The number of replications
     *
     * @return The number of replications
     */
    public final int getNumberOfReplications() {
        return myObsCounts.length;
    }

    /**
     * Returns the ith observation in the jth replication
     *
     * @param i ith observation
     * @param j jth replication
     * @return the ith observation in the jth replication
     * @throws IOException if there was trouble with the file
     */
    public final double get(long i, int j) throws IOException {
        setPosition(i, j);
        return get();
    }

    /**
     * Returns the value at the current position
     *
     * @return the value at the current position
     * @throws IOException if there was trouble with the file
     */
    public final double get() throws IOException {
        myLastDataPoint = myWDFDataFile.readDouble();
        myObsComponent.notifyObservers(this, null);
        return myLastDataPoint;
    }

    /**
     * Moves the file pointer to the position associated with the ith
     * observation in the jth replication
     *
     * @param i ith observation
     * @param j jth replication
     * @throws IOException if there was trouble with the file
     */
    public final void setPosition(long i, int j) throws IOException {
        myWDFDataFile.seek(getPosition(i, j));
    }

    /**
     * Gets the position in the file relative to the beginning of the file of
     * the ith observation in the jth replication. This assumes that the data is
     * a double 8 bytes stored in column major form
     *
     * @param i the index to the ith observation
     * @param j the index to the jth replication
     * @return the position in the file relative to the beginning of the file of
     *      the ith observation in the jth replication
     */
    public final long getPosition(long i, int j) {
        if ((i < 1) || (j < 1) || (j > myObsCounts.length) || (i > myObsCounts[j - 1])) {
            throw new IllegalArgumentException("Invalid observation# or replication#");
        }
        myLastObsIndex = i;
        myLastRepIndex = j;
        long pos = 0;
        for (int n = 0; n < j - 1; n++) {
            pos = pos + myObsCounts[n];
        }
        pos = pos + (i - 1);
        return pos * NUMBYTES;
    }

    /** Returns the last observation index asked for.  Can be used by observers
     *  Returns Integer.MIN_VALUE if no observations have been read
     * @return  the last observation index asked for.
     */
    public final long getLastObservationIndex() {
        return myLastObsIndex;
    }

    /** Returns the last replication index asked for.  Can be used by observers
     *  Returns Integer.MIN_VALUE if no observations have been read
     * @return  last replication index asked for
     */
    public final long getLastReplicationIndex() {
        return myLastRepIndex;
    }

    @Override
    public String toString() {
        return myWFMDBean.toJSON();
    }

    public static void main(String args[]) {
        double[] y = {1.0, 2.0, 3.0, 4.0, 5.0};
        Statistic stat = new Statistic(y);
        double avg = stat.getAverage();
        double[] partialSums = WelchDataCollectorIfc.getPartialSums(avg, y);
        System.out.println("avg = " + avg);
        System.out.println(Arrays.toString(partialSums));
    }
}
