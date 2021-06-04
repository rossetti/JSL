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
package jsl.observers.variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.ModelElementObserver;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.WeightedStatistic;

//TODO return copies of the data
//TODO organize data into better data structures
//TODO if within rep summary stats are captured, the across rep stats can be computed when needed
//TODO needs to handle counters
/**
 * The purpose of this class is to store within replication and across
 * replication summary statistics for post processing.
 *
 * This class should be attached to the simulation Model prior to running the
 * simulation in order to observe the data. For each replication, for each
 * response variable the within replication summary statistics are
 * captured. In addition, the summary statistics across all the replications is
 * captured.
 *
 * If the user needs to collect the statistics across multiple experiments of
 * the same model, then use setClearAfterExpFlag(true). In this situation, make
 * sure that the experiments have unique names before running the simulation. 
 * The data for each run will be stored based on the name of the experiment.
 *
 * @author rossetti
 */
public class SimulationResponseDataCollector extends ModelElementObserver {

    /**
     * This flag indicates whether or not the data should be cleared if another
     * experiment is run on the same model. The default is true. The data will
     * be cleared across experiments
     */
    private boolean myClearAfterExpFlag;

    /**
     * First key, is for the experiment. The list holds the data for each
     * replication for each experiment
     *
     */
    private Map<String, List<Map<String, WeightedStatistic>>> myWithinRepStats;

    /**
     * First key, is for experiment, second key is for response variable
     *
     */
    private Map<String, Map<String, Statistic>> myAcrossRepStats;

    private Model myModel;

    /**
     * The within replication data for the currently executing experiment for
     * the model that is being observed. Each element of the list holds the data
     * for one replication
     *
     */
    private List<Map<String, WeightedStatistic>> myCurrentWithinRepStats;

    public SimulationResponseDataCollector(Model m) {
        this(m, null);
    }

    public SimulationResponseDataCollector(Model m, String name) {
        super(name);
        myModel = m;
        myClearAfterExpFlag = true;
        myWithinRepStats = new HashMap<>();
        myAcrossRepStats = new HashMap<>();
        myModel.addObserver(this);
    }

    public final boolean getClearBeforeExpFlag() {
        return myClearAfterExpFlag;
    }

    public final void setClearBeforeExpFlag(boolean flag) {
        myClearAfterExpFlag = flag;
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        if (getClearBeforeExpFlag()){
            clearAllResponseData();
        }
        // get the experiment's name
        String name = m.getExperiment().getExperimentName();
        // create the list to hold the within rep data
        myCurrentWithinRepStats = new ArrayList<>();
        // add the list to the across experiment map
        myWithinRepStats.put(name, myCurrentWithinRepStats);
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        // create the map to hold the within replication data 
        Map<String, WeightedStatistic> wrd = new HashMap<>();
        // for each response variable get the within rep stats and add to map
        List<ResponseVariable> list = myModel.getResponseVariables();
        for (ResponseVariable r : list) {
            WeightedStatistic w = (WeightedStatistic) r.getWithinReplicationStatistic();
            wrd.put(r.getName(), w.newInstance());
        }
        // add the map to the replication data to the list
        myCurrentWithinRepStats.add(wrd);
    }

    @Override
    protected void afterExperiment(ModelElement m, Object arg) {
        // get the experiment's name
        String name = m.getExperiment().getExperimentName();
        // create the map to hold the within replication data 
        Map<String, Statistic> wrd = new HashMap<>();
        // for each response variable get the within rep stats and add to map
        List<ResponseVariable> list = myModel.getResponseVariables();
        for (ResponseVariable r : list) {
            Statistic s = (Statistic) r.getAcrossReplicationStatistic();
            wrd.put(r.getName(), s.newInstance());
        }
        List<Counter> counters = myModel.getCounters();
        for(Counter c: counters){
            Statistic s = (Statistic)c.getAcrossReplicationStatistic();
            wrd.put(c.getName(), s.newInstance());
        }
        // add the map to the replication data to the list
        myAcrossRepStats.put(name, wrd);
    }

    /** Returns a list of maps. Each map represents the within replication
     *  statistics for each response variable in the model for a replication. 
     *  The keys to the map are the names of the response variables.
     *  Each element of the list is a different replication. The 0th element is 
     *  the first replication
     *
     * @param expName the name of the experiment
     * @return  Returns a list of maps.
     */
    public List<Map<String, WeightedStatistic>> getAllWithinReplicationStatistics(String expName) {
        return myWithinRepStats.get(expName);
    }

    /** The map represents the across replication
     *  statistics for each response variable in the model. The keys to the map
     *  are the names of the response variables.
     *
     * @param expName the name of the experiment
     * @return the map of across replication statistics for each response
     */
    public Map<String, Statistic> getAcrossReplicationStatistics(String expName) {
        return myAcrossRepStats.get(expName);
    }
    
    /** Clears all saved data
     * 
     */
    public void clearAllResponseData(){
        for(List list: myWithinRepStats.values()){
            list.clear();
        }
        myWithinRepStats.clear();
        for(Map m: myAcrossRepStats.values()){
            m.clear();
        }
        myAcrossRepStats.clear();
    }
}
