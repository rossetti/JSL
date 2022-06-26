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
package jsl.observers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsl.observers.ReplicationDataCollector;
import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.ModelElementObserver;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.WeightedStatistic;

/**
 * The purpose of this class is to store  replication data across a set of experiments
 * <p>
 * This class should be attached to the simulation Model prior to running the
 * simulation in order to observe the data. For each replication, for each
 * response variable the replication data is collected using a ReplicationDataCollector
 * <p>
 * If a simulation with an experiment name is run that has not been previously observed,
 * then the data from that experiment is stored.  If an experiment with a name
 * that has already been executed is run, then any previous data is replaced with
 * the newly observed results.
 *
 * @author rossetti
 */
public class ExperimentDataCollector {

    /**
     * First key, is for the experiment. The value holds the collected replication data
     */
    private final Map<String, ReplicationDataCollector> myExpData;

    private final Model myModel;

    private final ModelObserver modelObserver;

    public ExperimentDataCollector(Model m) {
        myModel = m;
        myExpData = new HashMap<>();
        modelObserver = new ModelObserver();
        myModel.addObserver(modelObserver);
    }

    /**
     * Start observing the model
     */
    public void startObserving() {
        if (!myModel.contains(modelObserver)) {
            myModel.addObserver(modelObserver);
        }
    }

    /**
     * Stop observing the model
     */
    public void stopObserving() {
        if (myModel.contains(modelObserver)) {
            myModel.deleteObserver(modelObserver);
        }
    }

    private class ModelObserver extends ModelElementObserver {
        private ReplicationDataCollector rdc;

        @Override
        protected void beforeExperiment(ModelElement m, Object arg) {
            String name = m.getExperiment().getExperimentName();
            rdc = new ReplicationDataCollector(myModel, true);
            myExpData.put(name, rdc);
            rdc.stopObserving();// stop it so manual observing can be done
            rdc.beforeExperiment();
        }

        @Override
        protected void afterReplication(ModelElement m, Object arg) {
            rdc.afterReplication();
        }
    }

    public ReplicationDataCollector getExperimentData(String expName) {
        return myExpData.get(expName);
    }

    /**
     * Clears all saved data
     */
    public void clearAllResponseData() {
        myExpData.clear();
    }
}
