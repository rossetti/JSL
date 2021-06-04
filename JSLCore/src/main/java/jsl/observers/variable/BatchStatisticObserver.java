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

import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.ModelElementObserver;
import jsl.utilities.statistic.BatchStatistic;
import jsl.utilities.statistic.Statistic;

/** A observer for batching of statistics on ResponseVariables
 *  The user can control the collection rule and the batching criteria
 *  of the underlying BatchStatistic
 *
 * @author rossetti
 */
public class BatchStatisticObserver extends ModelElementObserver {

    /**
     * The underlying BatchStatistic
     */
    protected BatchStatistic myBatchStats;
    
    /**
     *  false means no warm up event in parent hierarchy
     */
    protected boolean myWarmUpEventCheckFlag = false;

    public BatchStatisticObserver(String name) {
        this(BatchStatistic.MIN_NUM_BATCHES, BatchStatistic.MIN_NUM_OBS_PER_BATCH,
                BatchStatistic.MAX_BATCH_MULTIPLE, name);
    }

    public BatchStatisticObserver(int minNumBatches, int minBatchSize,
            int maxNBMultiple) {
        this(minNumBatches, minBatchSize, maxNBMultiple, null);
    }

    public BatchStatisticObserver(int minNumBatches, int minBatchSize,
            int maxNBMultiple, String name) {
        super(name);
        myBatchStats = new BatchStatistic(minNumBatches, minBatchSize,
                maxNBMultiple, name);
    }

    /** Sets confidence level on underlying BatchStatistic
     *
     * @param alpha the confidence level between 0 and 1
     */
    public final void setConfidenceLevel(double alpha) {
        myBatchStats.setConfidenceLevel(alpha);
    }
    
    /**
     * The collected BatchStatistic
     *
     * @return the collected BatchStatistic
     */
    public final BatchStatistic getBatchStatistics() {
        return myBatchStats;
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg){
        myBatchStats.reset();
        myWarmUpEventCheckFlag = false;
    }
    
    @Override
    protected void beforeReplication(ModelElement m, Object arg) {
        myBatchStats.reset();
        ResponseVariable r = (ResponseVariable) m;
        ModelElement mElement = r.findModelElementWithWarmUpEvent();
        if (mElement == null){
            myWarmUpEventCheckFlag = false;
            // no warm up event, set BatchStatistic to desired checking
        } else {
            myWarmUpEventCheckFlag = true;
        }
    }

    @Override
    protected void update(ModelElement m, Object arg) {
        ResponseVariable r = (ResponseVariable) m;
        myBatchStats.collect(r);
//        if (collect == false){
//            // stop the simulation
//            StringBuilder msg = new StringBuilder();
//            msg.append(r.getName()).append(" stopped replication. ");
//            msg.append(myBatchStats.getCollectionRule());
//            msg.append(" criteria met.\n");
//            r.stopExecutive(msg.toString());
//        }
    }

    @Override
    protected void warmUp(ModelElement m, Object arg) {
        myBatchStats.reset();
    }

    @Override
    public String toString() {
        return myBatchStats.toString();
    }

}
