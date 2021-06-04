package jsl.utilities.statistic.welch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.JSLFileUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 *  A data class that holds information about Welch data files in a form that facilitates
 *  translation to JSON.
 *
 */
public class WelchFileMetaDataBean {

    private String dataName;

    private String pathToFile;

    private int numberOfReplications = 0;

    private long[] numObsInEachReplication;

    private double[] timeOfLastObsInEachReplication;

    private long minNumObsForReplications = 0;

    private double[] endReplicationAverages;

    private double[] timeBtwObsInEachReplication;

    private double batchSize = 0.0;

    private StatisticType statisticType;

    public WelchFileMetaDataBean() {
    }

    public boolean isValid(){
        if (dataName == null){
            return false;
        }
        if (pathToFile == null){
            return false;
        }
        Path path = Paths.get(pathToFile);
        String fileName = path.getFileName().toString();
        Optional<String> name = JSLFileUtil.getExtensionByStringFileName(fileName);
        String fileExtension = name.orElse("");
        if (!fileExtension.equals("wdf")){
            throw new IllegalArgumentException("The supplied file string does not have extension wdf");
        }
        if(numberOfReplications <= 0){
            return false;
        }
        if (numObsInEachReplication == null){
            return false;
        }
        if (timeOfLastObsInEachReplication == null){
            return false;
        }
        if (endReplicationAverages == null){
            return false;
        }
        if (timeBtwObsInEachReplication == null){
            return false;
        }
        if (minNumObsForReplications <= 0){
            return false;
        }
        if (batchSize <= 0.0){
            return false;
        }
        if (statisticType == null){
            return false;
        }
        return true;
    }

    /**
     *
     * @param pathToJSONFile the path to the JSON file holding the bean data, must not be null
     * @return an optional holding the bean or null if something went wrong
     */
    public static Optional<WelchFileMetaDataBean> makeFromJSONNE(Path pathToJSONFile){
        try {
            return Optional.ofNullable(makeFromJSON(pathToJSONFile));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     *
     * @param pathToJSONFile the path to the JSON file holding the bean data, must not be null
     * @return the deserialized bean
     * @throws FileNotFoundException  if the file is not found
     * @throws UnsupportedEncodingException if the UTF-8 encoding is not found
     */
    public static WelchFileMetaDataBean makeFromJSON(Path pathToJSONFile)
            throws FileNotFoundException, UnsupportedEncodingException {
        Objects.requireNonNull(pathToJSONFile, "The path was null");
        String fileName = pathToJSONFile.getFileName().toString();
        Optional<String> name = JSLFileUtil.getExtensionByStringFileName(fileName);
        String fileExtension = name.orElse("");
        if (!fileExtension.equals("json")){
            throw new IllegalArgumentException("The supplied file string does not have extension json");
        }
        FileInputStream in = new FileInputStream(pathToJSONFile.toFile());
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        Gson gson = new Gson();
        return gson.fromJson(reader, WelchFileMetaDataBean.class);
    }

    /**
     *
     * @return returns a pretty printed JSON representation as a String
     */
    public String toJSON(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    /**
     *
     * @return the type of statistic held in the data, TALLY or TIME_PERSISTENT
     */
    public StatisticType getStatisticType() {
        return statisticType;
    }

    public void setStatisticType(StatisticType statisticType) {
        Objects.requireNonNull(statisticType,"The statistic type cannot be null");
        this.statisticType = statisticType;
    }

    /**
     *
     * @return the name of the statistic or performance measure held in the file
     */
    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        Objects.requireNonNull(dataName,"The data name cannot be null");
        this.dataName = dataName;
    }

    /** The file will have the wdf extension
     *
     * @return a valid string representation of the path to the file holding the data
     */
    public String getPathToFile() {
        return pathToFile;
    }

    /**
     *
     * @return a string representation of the path to the JSON file related to this bean
     */
    public String getPathToJSONFile(){
        return JSLFileUtil.removeLastFileExtension(pathToFile) + ".json";
    }

    /**
     *
     * @param strPathToFile the path to the wdf file
     */
    public void setPathToFile(String strPathToFile) {
        Objects.requireNonNull(strPathToFile,"The path to the file cannot be null");
        //check if file has the wdf extension
        Path path = Paths.get(strPathToFile);
        String fileName = path.getFileName().toString();
        Optional<String> name = JSLFileUtil.getExtensionByStringFileName(fileName);
        String fileExtension = name.orElse("");
        if (!fileExtension.equals("wdf")){
            throw new IllegalArgumentException("The supplied file string does not have extension wdf");
        }
        this.pathToFile = strPathToFile;
    }

    /**
     *
     * @return the number of replications held in the file
     */
    public int getNumberOfReplications() {
        return numberOfReplications;
    }

    public void setNumberOfReplications(int numberOfReplications) {
        if (numberOfReplications <= 0){
            throw new IllegalArgumentException("The number of replications must be >= 1");
        }
        this.numberOfReplications = numberOfReplications;
    }

    /**
     *
     * @return the number of observations in each of the replications, index 0 is replication 1
     */
    public long[] getNumObsInEachReplication() {
        return numObsInEachReplication;
    }

    public void setNumObsInEachReplication(long[] numObsInEachReplication) {
        Objects.requireNonNull(numObsInEachReplication,"The array cannot be null");
        if (JSLArrayUtil.getMin(numObsInEachReplication) <= 0){
            throw new IllegalArgumentException("Some replication had zero observations");
        }
        this.numObsInEachReplication = Arrays.copyOf(numObsInEachReplication,numObsInEachReplication.length);
     }

    /**
     *
     * @return the time that the last observation occurred in each replication, index 0 is replication 1
     */
    public double[] getTimeOfLastObsInEachReplication() {
        return timeOfLastObsInEachReplication;
    }

    public void setTimeOfLastObsInEachReplication(double[] timeOfLastObsInEachReplication) {
        Objects.requireNonNull(timeOfLastObsInEachReplication,"The array cannot be null");
        if (JSLArrayUtil.getMin(timeOfLastObsInEachReplication) <= 0.0){
            throw new IllegalArgumentException("Some replication had minimum time <= 0.0");
        }
        this.timeOfLastObsInEachReplication = Arrays.copyOf(timeOfLastObsInEachReplication,timeOfLastObsInEachReplication.length);
    }

    /**
     *
     * @return the minimum of getNumObsInEachReplication()
     */
    public long getMinNumObsForReplications() {
        return minNumObsForReplications;
    }

    public void setMinNumObsForReplications(long minNumObsForReplications) {
        if (minNumObsForReplications < 0){
            throw new IllegalArgumentException("The number of replications must be >= 0");
        }
        this.minNumObsForReplications = minNumObsForReplications;
    }

    /**
     *
     * @return the statistical average of all the observations in each replication, index 0 is replication 1
     */
    public double[] getEndReplicationAverages() {
        return endReplicationAverages;
    }

    public void setEndReplicationAverages(double[] endReplicationAverages) {
        Objects.requireNonNull(endReplicationAverages,"The array cannot be null");
        for(double avg: endReplicationAverages){
            if (Double.isNaN(avg) || Double.isInfinite(avg)){
                throw new IllegalArgumentException("Some average in the end replication averages array was NaN or Inf");
            }
        }
        this.endReplicationAverages = Arrays.copyOf(endReplicationAverages,endReplicationAverages.length);
    }

    /**
     *
     * @return the average time between observations for each replication, index 0 is replication 1
     */
    public double[] getTimeBtwObsInEachReplication() {
        return timeBtwObsInEachReplication;
    }

    public void setTimeBtwObsInEachReplication(double[] timeBtwObsInEachReplication) {
        Objects.requireNonNull(timeBtwObsInEachReplication,"The array cannot be null");
        for(double avg: timeBtwObsInEachReplication){
            if (Double.isNaN(avg) || Double.isInfinite(avg)){
                throw new IllegalArgumentException("Some average in the average time between observations array was NaN or Inf");
            }
        }
        this.timeBtwObsInEachReplication = Arrays.copyOf(timeBtwObsInEachReplication,timeBtwObsInEachReplication.length);
    }

    /**
     *
     * @return the size of each batch if batching has been applied.
     */
    public double getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(double batchSize) {
        if (batchSize <= 0){
            throw new IllegalArgumentException("The batch size must be >= 1");
        }
        this.batchSize = batchSize;
    }
}
