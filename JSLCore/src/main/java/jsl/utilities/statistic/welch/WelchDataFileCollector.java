package jsl.utilities.statistic.welch;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.JSLFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jsl.simulation.Simulation.LOGGER;

public class WelchDataFileCollector extends AbstractWelchDataCollector {

    protected File myDataFile;

    //TODO does not have to be RandomAccessFile
    protected RandomAccessFile myData;

    protected File myMetaDataFile;
    protected PrintWriter myMetaData;

    private final File myDirectory;
    private final String myFileName;

    public WelchDataFileCollector(Path pathToDirectory, StatisticType statisticType, String name, double batchSize) {
        super(statisticType, name, batchSize);
        Objects.requireNonNull(pathToDirectory, "The path to the directory was null!");
        // make the directory
        try {
            Files.createDirectories(pathToDirectory);
        } catch (IOException e) {
            String str = "Problem creating directory for " + pathToDirectory;
            LOGGER.error(str, e);
            e.printStackTrace();
        }
        // now make the file to hold the observations within the directory
        myDirectory = pathToDirectory.toFile();
        // make a name for the file based on provided name
        myFileName = name + "_" + statisticType.name();
        myDataFile = JSLFileUtil.makeFile(pathToDirectory.resolve(myFileName + ".wdf"));
        myMetaDataFile = JSLFileUtil.makeFile(pathToDirectory.resolve(myFileName + ".json"));
        myMetaData = JSLFileUtil.makePrintWriter(myMetaDataFile);
        try {
            myData = new RandomAccessFile(myDataFile, "rw");
        } catch (IOException ex) {
            String str = "Problem creating RandomAccessFile for " + myDataFile.getAbsolutePath();
            LOGGER.error(str, ex);
        }
    }

    /**
     * The directory for the files
     *
     * @return the directory for the file
     */
    public File getDirectory() {
        return myDirectory;
    }

    /**
     * The base file name for the files
     *
     * @return the base file name
     */
    public String getFileName() {
        return myFileName;
    }

    /**
     * Makes a WelchDataFileAnalyzer based on the file in this collector
     *
     * @return a WelchDataFileAnalyzer
     */
    public WelchDataFileAnalyzer makeWelchDataFileAnalyzer() {
        return new WelchDataFileAnalyzer(makeWelchFileMetaDataBean());
    }

    /**
     * The file made for the raw data
     *
     * @return a file with the raw data
     */
    public File getDataFile() {
        return myDataFile;
    }

    /**
     * The file handle for the meta data file. The meta data file contains the
     * number of replications as the first line, and the number of observations
     * in each of the replications as the subsequent lines
     *
     * @return the meta data file
     */
    public File getMetaDataFile() {
        return myMetaDataFile;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------------");
        sb.append(System.lineSeparator());
        sb.append("Welch Data File Collector");
        sb.append(System.lineSeparator());
        sb.append(this.getWelchFileMetaDataBeanAsJson());
        sb.append(System.lineSeparator());
        sb.append("-------------------");
        sb.append(System.lineSeparator());
        return sb.toString();
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
            myObsCount++;
            // need to observe time between observations
            if (myObsCount >= 2) {
                // enough observations to collect time between
                myTBOStats.collect(time - myLastTime);
            }
            // need to save the observation
            myLastValue = myWithinRepStats.getAverage();
            myLastTime = time;
            writeObservation(myLastValue);
            // clear the batching
            myWithinRepStats.reset();
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
            double tb = (myObsCount + 1) * myBatchSize;
            if (time > tb) {
                // then a batch can be formed
                // close out the batch at time tb
                updateTimeWeightedStatistic(tb);
                // an observation is a batch of size deltaT
                myObsCount++;
                myTBOStats.collect(myBatchSize);
                // record the time average during the deltaT
                writeObservation(myWithinRepStats.getAverage());
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
        if (weight <= 0.0) {
            weight = 0.0;
        }
        // last value persisted for (time - myLastTime)
        myWithinRepStats.collect(myLastValue, weight); // collect weighted by time
    }

    private void writeObservation(double observation) {
        try {
            myData.writeDouble(observation);
            myRepStat.collect(observation);
//            if (myStatType == StatisticType.TIME_PERSISTENT){
//                JSL.out.println(observation);
//            }
        } catch (IOException ex) {
            Logger.getLogger(WelchDataFileCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public WelchFileMetaDataBean makeWelchFileMetaDataBean() {
        WelchFileMetaDataBean w = new WelchFileMetaDataBean();
        w.setBatchSize(myBatchSize);
        w.setDataName(myName);
        w.setNumberOfReplications(getNumberOfReplications());
        w.setPathToFile(myDataFile.getAbsolutePath());
        w.setStatisticType(myStatType);
        w.setEndReplicationAverages(getReplicationAverages());
        w.setTimeBtwObsInEachReplication(getAvgTimeBtwObservationsForEachReplication());
        w.setNumObsInEachReplication(getNumberOfObservationsForEachReplication());
        w.setTimeOfLastObsInEachReplication(getTimeOfLastObservationForReps());
        w.setMinNumObsForReplications(JSLArrayUtil.getMin(getNumberOfObservationsForEachReplication()));
        return w;
    }

    public String getWelchFileMetaDataBeanAsJson() {
        WelchFileMetaDataBean bean = makeWelchFileMetaDataBean();
        return bean.toJSON();
    }

    @Override
    public void cleanUpCollector() {
        myMetaData.println(getWelchFileMetaDataBeanAsJson());
    }
}
