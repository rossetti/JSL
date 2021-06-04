package jsl.observers;

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.utilities.math.JSLMath;

import java.util.*;

/**
 * Collects and stores the replication average for each specified response or final value for each counter. Must
 * be created prior to running the simulation for any data to be collected.  The added responses or counters
 * must already be part of the model.  This is important. Only those responses or counters that already exist
 * in the model hierarchy will be added automatically if you use that automatic add option.
 *
 * The collector collects data at the end of each replication.  Running the simulation multiple times
 * within the same execution will record over any data from a previous simulation run.  Use the
 * various methods to save the data if it is needed prior to running the simulation again. Or, remove
 * the collector as an observer of the model prior to running subsequent simulations.
 */
public class ReplicationDataCollector extends ModelElementObserver {

    private final List<ResponseVariable> myResponses;

    private final List<Counter> myCounters;

    private final Model myModel;

    private final Map<String, double[]> myResponseData;

    private int myNumReplications = 0;

    /** Creates a ReplicationDataCollector and does not automatically add response or counters.
     *
     * @param model the model that has the responses, must not be null
     */
    public ReplicationDataCollector(Model model) {
        this(model, false);
    }

    /**
     * @param model the model that has the responses, must not be null
     * @param addAll if true then ALL currently defined response variables and counters within the
     *               model will be automatically added to the data collector
     */
    public ReplicationDataCollector(Model model, boolean addAll) {
        Objects.requireNonNull(model, "The model must not be null");
        myModel = model;
        myResponses = new ArrayList<>();
        myCounters = new ArrayList<>();
        myResponseData = new LinkedHashMap<>();
        model.addObserver(this);
        if (addAll){
            addAllResponsesAndCounters();
        }
    }

    /**
     *  Adds all response variables and counters that are in the model to the data collector
     */
    public final void addAllResponsesAndCounters(){
        List<Counter> counterList = myModel.getCounters();
        for(Counter counter: counterList){
            addCounterResponse(counter);
        }
        List<ResponseVariable> responseVariables = myModel.getResponseVariables();
        for(ResponseVariable r: responseVariables){
            addResponse(r);
        }
    }

    /**
     * @param responseName the name of the response within the model, must be in the model
     */
    public final void addResponse(String responseName) {
        ResponseVariable responseVariable = myModel.getResponseVariable(responseName);
        addResponse(responseVariable);
    }

    /**
     * @param response the response within the model to collect and store data for, must
     *                 not be null
     */
    public final void addResponse(ResponseVariable response) {
        Objects.requireNonNull(response, "Attempted to add a null response");
        myResponses.add(response);
    }

    /**
     * @param counterName the name of the counter within the model, must be in the model
     */
    public final void addCounterResponse(String counterName) {
        Counter counter = myModel.getCounter(counterName);
        addCounterResponse(counter);
    }

    /**
     * @param counter the counter within the model to collect and store data for, must
     *                 not be null
     */
    public final void addCounterResponse(Counter counter) {
        Objects.requireNonNull(counter, "Attempted to add a null counter");
        myCounters.add(counter);
    }

    /**
     * @return the number of responses to collect
     */
    public final int getNumberOfResponses() {
        return myResponses.size() + myCounters.size();
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        myNumReplications = 0;
        myResponseData.clear();
        int numRows = m.getSimulation().getNumberOfReplications();
        for (ResponseVariable r : myResponses) {
            myResponseData.put(r.getName(), new double[numRows]);
        }
        for (Counter c : myCounters) {
            myResponseData.put(c.getName(), new double[numRows]);
        }
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        myNumReplications = m.getCurrentReplicationNumber();
        int row = myNumReplications - 1;
        for (ResponseVariable r : myResponses) {
            double[] data = myResponseData.get(r.getName());
            data[row] = r.getWithinReplicationStatistic().getAverage();
        }
        for (Counter c : myCounters) {
            double[] data = myResponseData.get(c.getName());
            data[row] = c.getValue();
        }
    }

    /**
     *
     * @return a list holding the names of the responses and counters
     */
    public final List<String> getResponseNames(){
        List<String> list = new ArrayList<>();
        for (ResponseVariable r : myResponses) {
            list.add(r.getName());
        }
        for (Counter c : myCounters) {
            list.add(c.getName());
        }
        return list;
    }

    /**
     *
     * @param responseName the name of the response or counter in the model
     * @return true if the name is present, false otherwise
     */
    public final boolean contains(String responseName){
        return myResponseData.containsKey(responseName);
    }

    /** If the response name does not exist in the collector a zero length array is returned.
     *
     * @param responseName the name of the response or counter, must be in the model
     * @return the replication averages for the named response
     */
    public final double[] getReplicationData(String responseName) {
        double[] data = myResponseData.get(responseName);
        if (data == null){
            return new double[0];
        }
        return Arrays.copyOf(data, data.length);
    }

    /**
     * @param responseVariable the response variable, must not be null and must be in model
     * @return the replication averages for the named response
     */
    public final double[] getReplicationAverages(ResponseVariable responseVariable) {
        return getReplicationData(responseVariable.getName());
    }

    /**
     * @param counter the counter, must not be null and must be in model
     * @return the replication averages for the named response
     */
    public final double[] getFinalReplicationValues(Counter counter) {
        return getReplicationData(counter.getName());
    }

    /**
     * @return the number of replications collected so far
     */
    public final int getNumReplications() {
        return myNumReplications;
    }

    /**
     * The responses are ordered in the same order as returned by getResponseNames() and
     * are the columns, each row for a column is the replication average, row 0 is replication 1
     *
     * @return the replication averages for each response or value for counter for each replication
     */
    public final double[][] getAllReplicationData() {
        double[][] data = new double[myResponses.size()][myNumReplications];
        int column = 0;
        List<String> names = getResponseNames();
        for(String name: names){
            double[] x = myResponseData.get(name);
            for (int i = 0; i < x.length; i++) {
                data[i][column] = x[i];
            }
            column++;
        }
        return data;
    }

    /**
     *
     * @return a map holding the response name as key and the end replication data as an array
     */
    public final Map<String, double[]> getAllReplicationDataAsMap(){
        Map<String, double[]> dataMap = new LinkedHashMap<>();
        List<String> names = getResponseNames();
        for(String name: names){
            double[] x = myResponseData.get(name);
            dataMap.put(name, x);
        }
        return dataMap;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        List<String> responseNames = getResponseNames();
        for (String name : responseNames) {
            double[] x = myResponseData.get(name);
            fmt.format("%-20s %-5s", name, "|");
            for(double v: x){
                fmt.format("%10.3f %-3s", v, "|");
            }
            fmt.format("%n");
        }
        return sb.toString();
    }
}
