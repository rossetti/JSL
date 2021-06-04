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
package jsl.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.ResponseVariableBatchingElement;
import jsl.modeling.elements.variable.TWBatchingElement;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.observers.variable.BatchStatisticObserver;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.BatchStatistic;
import jsl.utilities.statistic.StatisticAccessorIfc;

/**
 * When added to a Model, this class will cause batch statistics to be collected
 * for ResponseVariables and TimeWeighted variables. It uses the
 * TWBatchingElement and the ResponseVariableBatchingElement to perform this
 * functionality.
 *
 * Time weighted variables are first discretized using a supplied batch interval. Then,
 * observation based batching is applied to the discretized batches.  Response variables
 * are batched by observation number.
 *
 *
 * @author rossetti
 */
public class StatisticalBatchingElement extends ModelElement {

    private final TWBatchingElement myTWBatcher;

    private final ResponseVariableBatchingElement myRVBatcher;

    /**
     * Creates a StatisticalBatchingElement using the default discretizing
     * interval defined in TWBatchingElement
     *
     * @param model the model for the batching
     */
    public StatisticalBatchingElement(Model model) {
        this(model, 0.0, null);
    }

    /**
     * Creates a StatisticalBatchingElement
     *
     * @param model the model for the batching
     * @param batchInterval the discretizing interval for TimeWeighted variables
     */
    public StatisticalBatchingElement(Model model, double batchInterval) {
        this(model, batchInterval, null);
    }

    /**
     * Creates a StatisticalBatchingElement
     *
     * @param model the model for the batching
     * @param batchInterval the discretizing interval for TimeWeighted variables
     * @param name the name of the model element
     */
    public StatisticalBatchingElement(Model model, double batchInterval, String name) {
        super(model, name);
        myTWBatcher = new TWBatchingElement(this, batchInterval);
        myRVBatcher = new ResponseVariableBatchingElement(this);
    }

    /**
     *
     * @return a map of all batch statistics with the ResponseVariable variable as the key
     */
    public final Map<ResponseVariable, BatchStatistic> getAllResponseVariableBatchStatisticsAsMap(){
        return myRVBatcher.getAllBatchStatisticsAsMap();
    }

    /**
     *
     * @return a map of all batch statistics with the TimeWeighted variable as the key
     */
    public final Map<TimeWeighted, BatchStatistic> getAllTimeWeightedBatchStatisticsAsMap(){
        return myTWBatcher.getAllBatchStatisticsAsMap();
    }

    /**
     * Look up the BatchStatisticObserver for the ResponseVariable
     *
     * @param key the ResponseVariable to look up
     * @return the BatchStatisticObserver
     */
    public final BatchStatisticObserver getBatchStatisticObserver(ResponseVariable key) {
        if (key instanceof TimeWeighted) {
            return myTWBatcher.getTWBatchStatisticObserver((TimeWeighted) key);
        } else {
            return myRVBatcher.getBatchStatisticObserver(key);
        }
    }

    /**
     * Removes the supplied ResponseVariable variable from the batching
     *
     * @param r the ResponseVariable to be removed
     */
    public final void remove(ResponseVariable r) {
        if (r instanceof TimeWeighted) {
            myTWBatcher.remove((TimeWeighted) r);
        } else {
            myRVBatcher.remove(r);
        }
    }

    /**
     * Removes all previously added ResponseVariable from the batching
     *
     */
    public final void removeAll() {
        myTWBatcher.removeAll();
        myRVBatcher.removeAll();
    }

    /**
     * Returns a statistical summary BatchStatistic on the ResponseVariable
     * variable across the observed batches This returns a copy of the summary
     * statistics.
     *
     * @param r the ResponseVariable to look up
     * @return the returned BatchStatistic
     */
    public final BatchStatistic getBatchStatistic(ResponseVariable r) {
        if (r instanceof TimeWeighted) {
            return myTWBatcher.getBatchStatistic((TimeWeighted) r);
        } else {
            return myRVBatcher.getBatchStatistic(r);
        }
    }

    /**
     * Returns a list of summary statistics on all ResponseVariable variables
     * The list is a copy of originals.
     *
     * @return the filled up list
     */
    public final List<BatchStatistic> getAllBatchStatistcs() {
        List<BatchStatistic> list = myTWBatcher.getAllBatchStatisitcs();
        list.addAll(myRVBatcher.getAllBatchStatisitcs());
        return list;
    }

    /**
     * Returns a list of the batch statistics in the form of
     * StatisticAccessorIfc
     *
     * @return the list
     */
    public final List<StatisticAccessorIfc> getAllStatistics() {
        List<StatisticAccessorIfc> list = new ArrayList<>();
        List<BatchStatistic> allBatchStatisitcs = getAllBatchStatistcs();
        list.addAll(allBatchStatisitcs);
        return list;
    }

    /**
     * Returns a StatisticReporter for reporting the statistics across the
     * batches.
     *
     * @return the reporter
     */
    public final StatisticReporter getStatisticReporter() {
        StatisticReporter sr = new StatisticReporter(getAllStatistics());
        sr.setReportTitle("Batch Summary Report");
        return sr;
    }

    @Override
    protected void beforeExperiment() {
        removeAll();
        // now add all appropriate responses to the batching
        Model m = getModel();
        List<ResponseVariable> list = m.getResponseVariables();
        for (ResponseVariable r : list) {
            if (r instanceof TimeWeighted) {
                myTWBatcher.add((TimeWeighted) r);
            } else {
                myRVBatcher.add(r);
            }
        }
    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append(myTWBatcher.asString());
        sb.append(myRVBatcher.asString());
        return sb.toString();
    }
}
