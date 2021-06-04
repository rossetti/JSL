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
package jsl.modeling.elements.variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jsl.simulation.ModelElement;
import jsl.observers.variable.BatchStatisticObserver;
import jsl.utilities.statistic.BatchStatistic;

/** Controls the batching of ResponseVariables within the Model. Used by
 *  StatisticalBatchingElement.
 *
 * @author rossetti
 */
public class ResponseVariableBatchingElement extends ModelElement {

    /**
     * Holds the statistics across the time scheduled batches for the time
     * weighted variables
     *
     */
    private Map<ResponseVariable, BatchStatisticObserver> myBatchStats;

    /**
     * Creates a time weighted batching element
     *
     * @param modelElement the model element
     */
    public ResponseVariableBatchingElement(ModelElement modelElement) {
        this(modelElement, null);
    }

    /**
     * Creates a time weighted batching element
     *
     * @param modelElement the model element
     * @param name a name for the element
     */
    public ResponseVariableBatchingElement(ModelElement modelElement, String name) {
        super(modelElement, name);
        myBatchStats = new HashMap<>();
    }

    /**
     * Adds the supplied ResponseVariable variable to the batching
     *
     * @param r the ResponseVariable variable to add
     * @return the BatchStatisticObserver
     */
    public final BatchStatisticObserver add(ResponseVariable r) {
        return add(r, BatchStatistic.MIN_NUM_BATCHES, BatchStatistic.MIN_NUM_OBS_PER_BATCH,
                BatchStatistic.MAX_BATCH_MULTIPLE, r.getName());
    }

    /**
     * Adds the supplied ResponseVariable variable to the batching
     *
     * @param r the ResponseVariable variable to add
     * @param name name for BatchStatistic
     * @return the BatchStatisticObserver
     */
    public final BatchStatisticObserver add(ResponseVariable r, String name) {
        return add(r, BatchStatistic.MIN_NUM_BATCHES, BatchStatistic.MIN_NUM_OBS_PER_BATCH,
                BatchStatistic.MAX_BATCH_MULTIPLE, name);
    }

    /**
     * Adds the supplied ResponseVariable variable to the batching
     *
     * @param r the ResponseVariable variable to add
     * @param minNumBatches minimum number of batches
     * @param minBatchSize minimum batch size
     * @param maxNBMultiple batch size multiple
     * @param name name for BatchStatistic
     * @return the BatchStatisticObserver
     */
    public final BatchStatisticObserver add(ResponseVariable r, int minNumBatches, int minBatchSize,
            int maxNBMultiple, String name) {
        BatchStatisticObserver bo = new BatchStatisticObserver(minNumBatches,
                minBatchSize, maxNBMultiple, name);
        myBatchStats.put(r, bo);
        r.addObserver(bo);
        return bo;
    }

    /**
     * Look up the BatchStatisticObserver for the ResponseVariable
     *
     * @param key the ResponseVariable to look up
     * @return the BatchStatisticObserver
     */
    public final BatchStatisticObserver getBatchStatisticObserver(ResponseVariable key) {
        return myBatchStats.get(key);
    }

    /**
     * Removes the supplied ResponseVariable variable from the batching
     *
     * @param r the ResponseVariable to be removed
     */
    public final void remove(ResponseVariable r) {
        BatchStatisticObserver bo = myBatchStats.get(r);
        r.deleteObserver(bo);
        myBatchStats.remove(r);
    }

    /**
     * Removes all previously added ResponseVariable from the batching
     *
     */
    public final void removeAll() {
        // first remove all the observers, then clear the map
        for (ResponseVariable r : myBatchStats.keySet()) {
            BatchStatisticObserver bo = myBatchStats.get(r);
            r.deleteObserver(bo);
        }
        myBatchStats.clear();
    }

    /**
     * Returns a statistical summary BatchStatistic on the ResponseVariable
     * variable across the observed batches. This returns a copy of the summary
     * statistics.
     *
     * @param r the ResponseVariable to look up
     * @return the returned BatchStatistic
     */
    public final BatchStatistic getBatchStatistic(ResponseVariable r) {
        BatchStatisticObserver bo = myBatchStats.get(r);
        if (bo == null) {
            return new BatchStatistic(r.getName() + " Across Batch Statistics");
        } else {
            return bo.getBatchStatistics().newInstance();
        }
    }

    /**
     * Returns a list of summary statistics on all ResponseVariable variables
     * The list is a copy of originals.
     *
     * @return the filled up list
     */
    public final List<BatchStatistic> getAllBatchStatisitcs() {
        List<BatchStatistic> list = new ArrayList<>();
        for (ResponseVariable r : myBatchStats.keySet()) {
            list.add(getBatchStatistic(r));
        }
        return list;
    }

    /**
     *
     * @return a map of all batch statistics with the ResponseVariable variable as the key
     */
    public final Map<ResponseVariable, BatchStatistic> getAllBatchStatisticsAsMap(){
        Map<ResponseVariable, BatchStatistic> map = new HashMap<>();

        for (ResponseVariable tw : myBatchStats.keySet()) {
            map.put(tw, myBatchStats.get(tw).getBatchStatistics());
        }
        return map;
    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------------------------");
        sb.append("\n");
        sb.append("Batch Statistics");
        sb.append("\n");
        sb.append("------------------------------------------------------------");
        sb.append("\n");
        sb.append("Response Variables \n");
        for (BatchStatisticObserver bo : myBatchStats.values()) {
            sb.append(bo);
            sb.append("\n");
        }
        sb.append("------------------------------------------------------------");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Gets the CSV row for the ResponseVariable
     *
     * @param r
     * @return the data as a string
     */
    public String getCSVRow(ResponseVariable r) {
        StringBuilder row = new StringBuilder();
        row.append(r.getModel().getName());
        row.append(",");
        row.append("ResponseVariable");
        row.append(",");
        BatchStatistic b = getBatchStatistic(r);
        row.append(b.getCSVStatistic());
        return row.toString();
    }

    /**
     * Gets the CSV Header for the ResponseVariable
     *
     * @param r
     * @return the header
     */
    public String getCSVHeader(ResponseVariable r) {
        StringBuilder header = new StringBuilder();
        header.append("Model,");
        header.append("StatType,");
        BatchStatistic b = getBatchStatistic(r);
        header.append(b.getCSVStatisticHeader());
        return header.toString();
    }
}
