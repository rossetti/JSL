package jsl.observers;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.simulation.Model;
import jsl.utilities.JSLArrayUtil;

import java.util.*;

/**
 *  Defines responses and controls for a control variate experiment. Collects the
 *  replication responses for the responses and for the controls.
 *  Must be created prior to running the simulation to actually
 *  collect any data. Uses a ReplicationDataCollector
 */
public class ControlVariateDataCollector {

    private final ReplicationDataCollector myResponseCollector;
    private final List<ResponseVariable> myResponses;
    private final Map<String, Double> myControls;
    private final Model myModel;

    /**
     *
     * @param model the model to collect on, must not be null
     */
    public ControlVariateDataCollector(Model model) {
        Objects.requireNonNull(model, "The model must not be null");
        myModel = model;
        myResponseCollector = new ReplicationDataCollector(model);
        myControls = new LinkedHashMap<>();
        myResponses = new ArrayList<>();
    }

    /**
     *
     * @param responseName the name of the response to add for collection
     */
    public final void addResponse(String responseName) {
        ResponseVariable responseVariable = myModel.getResponseVariable(responseName);
        addResponse(responseVariable);
    }

    /**
     *
     * @param response the response to add for collection
     */
    public final void addResponse(ResponseVariable response) {
        Objects.requireNonNull(response, "Attempted to add a null response");
        myResponses.add(response);
        myResponseCollector.addResponse(response);
    }

    /**  If the RanddomVariable doesn't exist in the model then no control is set up
     *
     * @param randomVariableName the name of the RandomVariable to add as a control
     * @param meanValue the mean of the RandomVariable
     * @return the name of the control response or null
     */
    public final String addControlVariate(String randomVariableName, double meanValue) {
        Objects.requireNonNull(randomVariableName, "Attempted to add a null random variable name as a control variate");
        RandomVariable randomVariable = myModel.getRandomVariable(randomVariableName);
        if (randomVariable == null){
            return null;
        }
        randomVariable.turnOnResponseCapture();
        ResponseVariable responseVariable = randomVariable.getCapturedResponse().get();
        myResponseCollector.addResponse(responseVariable);
        myControls.put(responseVariable.getName(), meanValue);
        return responseVariable.getName();
    }

    /** If the RandomVariable doesn't exist in the model then no control is set up
     *
     * @param rv the RandomVariable to add as a control
     * @param meanValue the mean of the RandomVariable
     * @return the name of the control response or null
     */
    public final String addControlVariate(RandomVariable rv, double meanValue) {
        Objects.requireNonNull(rv, "Attempted to add a null random variable as a control variate");
        return addControlVariate(rv.getName(), meanValue);
    }

    /**
     * @return the number of responses
     */
    public final int getNumberOfResponses() {
        return myResponses.size();
    }

    /**
     * @return the number of controls
     */
    public final int getNumberOfControlVariates() {
        return myControls.size();
    }

    /**
     *
     * @return a list holding the names of the responses
     */
    public final List<String> getResponseNames(){
        List<String> list = new ArrayList<>();
        for (ResponseVariable r : myResponses) {
            list.add(r.getName());
        }
        return list;
    }

    /**
     * The control names
     *
     * @return a copy of the names of the controls
     */
    public final List<String> getControlNames(){
        List<String> list = new ArrayList<>();
        for(String name: myControls.keySet()){
            list.add(name);
        }
        return list;
    }

    /**
     * @param responseName the name of the response
     * @return the collected replication averages, each row is a replication
     */
    public final double[] getResponseReplicationData(String responseName) {
        return myResponseCollector.getReplicationData(responseName);
    }

    /**
     * @param controlName the name of the control
     * @return the collected replication averages minus the control mean, each row is a replication
     */
    public final double[] getControlReplicationData(String controlName) {
        double[] data = myResponseCollector.getReplicationData(controlName);
        double mean = myControls.get(controlName);
        return JSLArrayUtil.subtractConstant(data, mean);
    }

    /** The replications are the rows. The columns are ordered first with response names
     *  and then with control names based on the order from getResponseNames() and
     *  getControlNames()
     *
     * @return the response and control data from each replication
     */
    public final double[][] getData(){
        int numRows = myResponseCollector.getNumReplications();
        int numCols = getNumberOfResponses() + getNumberOfControlVariates();
        double[][] data = new double[numRows][numCols];
        int j = 0;
        for(ResponseVariable r : myResponses){
            double[] src = getResponseReplicationData(r.getName());
            JSLArrayUtil.fillColumn(j, src, data);
            j++;
        }
        List<String> controlNames = getControlNames();
        for(String name: controlNames){
            double[] src = getControlReplicationData(name);
            JSLArrayUtil.fillColumn(j, src, data);
            j++;
        }
        return data;
    }

    /**
     *
     * @return a map holding the response and control names as keys and replication averages as an array
     */
    public final Map<String, double[]> getDataAsMap(){
        Map<String, double[]> dataMap = new LinkedHashMap<>();
        for (ResponseVariable r : myResponses) {
            double[] x = getResponseReplicationData(r.getName());
            dataMap.put(r.getName(), x);
        }
        List<String> controlNames = getControlNames();
        for(String name: controlNames){
            double[] x = getControlReplicationData(name);
            dataMap.put(name, x);
        }
        return dataMap;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        Map<String, double[]> dataAsMap = getDataAsMap();
        for (String name : dataAsMap.keySet()) {
            double[] x = dataAsMap.get(name);
            fmt.format("%-20s %-5s", name, "|");
            for(double v: x){
                fmt.format("%10.3f %-3s", v, "|");
            }
            fmt.format("%n");
        }
        return sb.toString();
    }
}
