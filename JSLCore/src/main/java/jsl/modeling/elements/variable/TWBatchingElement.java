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

import jsl.observers.variable.BatchStatisticObserver;
import jsl.simulation.*;
import jsl.utilities.statistic.BatchStatistic;
import jsl.utilities.statistic.WeightedStatistic;

/**
 * This class controls the batching of time weighted variables within the Model.
 *
 * The batch interval is used to schedule events during a replication and must
 * be the same throughout the replication. If the supplied interval is 0.0, then
 * the method getApproximateBatchInterval() will be used to determine the
 * interval for the replication.
 *
 * Time-based variables (TimeWeighted) are first discretized based on a batching
 * interval. The default batching interval is based on the value of the initial
 * number of batches. This is by default set to DEFAULT_NUM_TW_BATCHES = 512.
 * These initial batches are then rebatched according to the procedures within
 * BatchStatistic
 *
 * Use addTimeWeighted(TimeWeighted tw) to add TimeWeighted variables to the
 * batching.
 *
 * @author rossetti
 */
public class TWBatchingElement extends SchedulingElement {

    /**
     * A constant for the default batch interval for a replication If there is
     * no run length specified and the user turns on default batching, then the
     * time interval between batches will be equal to this value. The default
     * value is 10.0
     */
    public static final double DEFAULT_BATCH_INTERVAL = 10.0;

    /**
     * A constant for the default number of batches for TimeWeighted variables.
     * This value is used in the calculation of the approximate batching
     * interval if batching is turned on and there is a finite run length.
     *
     * If the run length is finite, then the batch interval is approximated as
     * follows:
     *
     * t = length of replication - length of warm up
     * n = getTimeWeightedStartingNumberOfBatches()
     *
     * batching interval = t/n
     *
     * DEFAULT_NUM_TW_BATCHES = 512.0
     *
     */
    public static final double DEFAULT_NUM_TW_BATCHES = 512.0;

    /**
     * A reference to the Batching event.
     */
    private JSLEvent myBatchEvent;

    /**
     * The priority for the batching events.
     */
    private int myBatchEventPriority = JSLEvent.DEFAULT_BATCH_PRIORITY;

    /**
     * The time interval between batching events.
     */
    private double myTimeBtwBatches = 0.0;

    /**
     * A time interval (in simulated time) that represents the default time
     * between batches The default is zero for no batching
     */
    private double myBatchInterval = 0;

    /**
     * The starting number of batches for time weighted batching. Used in
     * approximating a batch interval size
     */
    private double myNumTWBatches = DEFAULT_NUM_TW_BATCHES;

    /**
     * Holds the statistics across the time scheduled batches for the time
     * weighted variables
     *
     */
    private Map<TimeWeighted, TWBatchStatisticObserver> myBatchStats;

    private final EventHandler myEventHandler;

    /**
     * Creates a time weighted batching element
     *
     * @param modelElement the model element
     */
    public TWBatchingElement(ModelElement modelElement) {
        this(modelElement, 0.0, null);
    }

    /**
     * Creates a time weighted batching element
     *
     * @param modelElement the model element
     * @param interval the batching interval, must be greater than 0
     */
    public TWBatchingElement(ModelElement modelElement, double interval) {
        this(modelElement, interval, null);
    }

    /**
     * Creates a time weighted batching element
     *
     * @param modelElement the model element
     * @param interval the batching interval, must be greater than 0
     * @param name a name for the element
     */
    public TWBatchingElement(ModelElement modelElement, double interval, String name) {
        super(modelElement, name);
        myEventHandler = new EventHandler();
        setBatchInterval(interval);
        myBatchStats = new HashMap<>();
    }

    /** Look up the TWBatchStatisticObserver for the given TimeWeighted variable
     *
     * @param tw the TimeWeighted variable
     * @return the TWBatchStatisticObserver
     */
    public final TWBatchStatisticObserver getTWBatchStatisticObserver(TimeWeighted tw) {
        return myBatchStats.get(tw);
    }

    /**
     * Adds the supplied TimeWeighted variable to the batching
     *
     * @param tw the TimeWeighted variable to add
     * @return  the TWBatchStatisticObserver
     */
    public final TWBatchStatisticObserver add(TimeWeighted tw) {
        return add(tw, BatchStatistic.MIN_NUM_BATCHES, BatchStatistic.MIN_NUM_OBS_PER_BATCH,
                BatchStatistic.MAX_BATCH_MULTIPLE, tw.getName());
    }

    /**
     * Adds the supplied TimeWeighted variable to the batching
     *
     * @param tw the TimeWeighted variable to add
     * @param name name for BatchStatistic
     * @return the TWBatchStatisticObserver
     */
    public final TWBatchStatisticObserver add(TimeWeighted tw, String name) {
        return add(tw, BatchStatistic.MIN_NUM_BATCHES, BatchStatistic.MIN_NUM_OBS_PER_BATCH,
                BatchStatistic.MAX_BATCH_MULTIPLE, name);
    }

    /**
     * Adds the supplied TimeWeighted variable to the batching
     *
     * @param tw the TimeWeighted variable to add
     * @param minNumBatches minimum number of batches
     * @param minBatchSize minimum batch size
     * @param maxNBMultiple batch size multiple
     * @param name name for BatchStatistic
     * @return the TWBatchStatisticObserver
     */
    public final TWBatchStatisticObserver add(TimeWeighted tw, int minNumBatches, int minBatchSize,
            int maxNBMultiple, String name) {
        TWBatchStatisticObserver bo = new TWBatchStatisticObserver(tw,
                minNumBatches, minBatchSize, maxNBMultiple, name);
        myBatchStats.put(tw, bo);
        tw.addObserver(bo);
        return bo;
    }

    /**
     * Removes the supplied TimeWeighted variable from the batching
     *
     * @param tw the TimeWeighted to be removed
     */
    public final void remove(TimeWeighted tw) {
        TWBatchStatisticObserver bo = myBatchStats.get(tw);
        tw.deleteObserver(bo);
        myBatchStats.remove(tw);
    }

    /**
     * Removes all previously added TimeWeighted from the batching
     *
     */
    public final void removeAll() {
        // first remove all the observers, then clear the map
        for (TimeWeighted tw : myBatchStats.keySet()) {
            TWBatchStatisticObserver bo = myBatchStats.get(tw);
            tw.deleteObserver(bo);
        }
        myBatchStats.clear();
    }

    /**
     * Returns a statistical summary BatchStatistic on the TimeWeighted variable
     * across the observed batches This returns a copy of the summary
     * statistics.
     *
     * @param tw the TimeWeighted to look up
     * @return the returned BatchStatistic
     */
    public final BatchStatistic getBatchStatistic(TimeWeighted tw) {
        TWBatchStatisticObserver bo = myBatchStats.get(tw);
        if (bo == null) {
            return new BatchStatistic(tw.getName() + " Across Batch Statistics");
        } else {
            return bo.getBatchStatistics().newInstance();
        }
    }

    /**
     * Returns a list of summary statistics on all TimeWeighted variables. The
     * list is a copy of originals.
     *
     * @return the filled up list
     */
    public final List<BatchStatistic> getAllBatchStatisitcs() {
        List<BatchStatistic> list = new ArrayList<>();
        for (TimeWeighted tw : myBatchStats.keySet()) {
            list.add(getBatchStatistic(tw));
        }
        return list;
    }

    /**
     *
     * @return a map of all batch statistics with the TimeWeighted variable as the key
     */
    public final Map<TimeWeighted, BatchStatistic> getAllBatchStatisticsAsMap(){
        Map<TimeWeighted, BatchStatistic> map = new HashMap<>();

        for (TimeWeighted tw : myBatchStats.keySet()) {
            map.put(tw, myBatchStats.get(tw).getBatchStatistics());
        }
        return map;
    }

    /**
     * Sets the batch event priority.
     *
     * @param priority The batch event priority, lower means earlier
     */
    protected final void setBatchEventPriority(int priority) {
        myBatchEventPriority = priority;
    }

    /**
     * Gets the batch event priority
     *
     * @return The batch event priority
     */
    public final int getBatchEventPriority() {
        return (myBatchEventPriority);
    }

    /**
     * Gets the current batch interval length for time weighted variables
     *
     * @return The batch interval as time
     */
    public final double getBatchInterval() {
        return (myBatchInterval);
    }

    /**
     * The starting number of batches, used to determine the batch interval when
     * it is not explicitly set.
     *
     * @return number of batches
     */
    public final double getTimeWeightedStartingNumberOfBatches() {
        return myNumTWBatches;
    }

    /**
     * Sets the initial number of batches for time-weighted variables The number
     * of initial batches is not recommended to be less than 10.
     *
     * @param numBatches must be bigger than 0
     */
    public final void setTimeWeightedStartingNumberOfBatches(int numBatches) {
        if (numBatches <= 0) {
            throw new IllegalArgumentException("The number of batches must be >0");
        }
        if (numBatches < 10) {
            StringBuilder sb = new StringBuilder();
            sb.append("The number of initial batches < 10\n");
            sb.append("is not recommended for batching time-based variables\n");
            Simulation.LOGGER.warn(sb.toString());
            System.out.flush();
        }
        myNumTWBatches = numBatches;
    }

    /**
     * Sets the batch interval length. Changing this during a replication has no
     * effect. The batch interval is used to schedule events during a
     * replication and must be the same throughout the replication. If the
     * supplied interval is 0.0, then the method getApproximateBatchInterval()
     * will be used to determine the interval for the replication
     *
     * @param batchInterval The batch interval size in time units must be
     * &gt;=0, if it is larger than run length it will not occur
     */
    public final void setBatchInterval(double batchInterval) {
        if (batchInterval < 0.0) {
            throw new IllegalArgumentException("The batch interval cannot be less than zero");
        }
        myBatchInterval = batchInterval;
    }

    /**
     * Checks if a batching event has been scheduled for this model element
     *
     * @return True means that it has been scheduled.
     */
    public final boolean isBatchEventScheduled() {
        if (myBatchEvent == null) {
            return (false);
        } else {
            return (myBatchEvent.isScheduled());
        }
    }

    /**
     * This method returns a suggested batching interval based on the length of
     * the run, the warm up period, and default number of batches.
     *
     * @return a double representing an approximate batch interval
     */
    protected final double getApproximateBatchInterval() {
        ExperimentGetIfc e = getModel().getExperiment();
        if (e == null) {
            return DEFAULT_BATCH_INTERVAL;
        }
        return getApproximateBatchInterval(e.getLengthOfReplication(), e.getLengthOfWarmUp());
    }

    /**
     * This method returns a suggested batching interval based on the length of
     * of the replication and warm up length for TimeWeighted variables.
     *
     * This value is used in the calculation of the approximate batching
     * interval if batching is turned on and there is a finite run length.
     *
     * If the run length is finite, then the batch interval is approximated as
     * follows:
     *
     * t = length of replication - length of warm up n =
     * getTimeWeightedStartingNumberOfBatches()
     *
     * batching interval = t/n
     *
     * DEFAULT_NUM_TW_BATCHES = 512.0
     *
     * @param repLength the length of the replication
     * @param warmUp the warm up period for the replication
     * @return the recommended batching interval
     */
    public final double getApproximateBatchInterval(double repLength, double warmUp) {

        if (repLength <= 0.0) {
            throw new IllegalArgumentException("The length of the replication must be > 0");
        }

        if (warmUp < 0) {
            throw new IllegalArgumentException("The warm up length must be >= 0");
        }

        double deltaT = 0.0;

        if (Double.isInfinite(repLength)) {
            // runlength is infinite
            deltaT = DEFAULT_BATCH_INTERVAL;
        } else { // runlength is finite
            double t = repLength;
            t = t - warmUp; // actual observation length
            double n = getTimeWeightedStartingNumberOfBatches();
            deltaT = t / n;
        }
        return (deltaT);
    }

    @Override
    protected void beforeReplication() {
        if (getBatchInterval() == 0.0) {
            setBatchInterval(getApproximateBatchInterval());
        }
        myTimeBtwBatches = getBatchInterval();
    }

    @Override
    protected void initialize() {
        myBatchEvent = scheduleEvent(myEventHandler, myTimeBtwBatches, myBatchEventPriority);
    }

    /**
     * The batch method is called during each replication when the batching
     * event occurs This method ensures that each time weighted variable gets
     * within replication batch statistics collected across batches
     */
    protected void batch() {
        for (TimeWeighted tw : myBatchStats.keySet()) {
            tw.setValue(tw.getValue());
            TWBatchStatisticObserver bo = myBatchStats.get(tw);
            bo.batch();
        }
    }

    private class EventHandler extends EventAction {
        @Override
        public void action(JSLEvent<Object> event) {
            myBatchEvent = event;
            batch();
            rescheduleEvent(event, myTimeBtwBatches);
        }
    }

//    private class EventHandler implements EventActionIfc<Object> {
//
//        @Override
//        public void action(JSLEvent<Object> event) {
//            myBatchEvent = event;
//            batch();
//            rescheduleEvent(event, myTimeBtwBatches);
//        }
//    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------------------------");
        sb.append("\n");
        sb.append("Batch Statistics");
        sb.append("\n");
        sb.append("------------------------------------------------------------");
        sb.append("\n");
        sb.append("TimeWeighted Variables \n");
        sb.append("Batching based on ");
        sb.append("batch interval = ");
        sb.append(getBatchInterval());
        sb.append(" time units \n");
        sb.append("Initial number of batches = ");
        sb.append(getTimeWeightedStartingNumberOfBatches());
        sb.append("\n");
        for (TWBatchStatisticObserver bo : myBatchStats.values()) {
            sb.append(bo);
            sb.append("\n");
        }
        sb.append("------------------------------------------------------------");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Gets the CSV row for the TimeWeighted
     *
     * @param tw
     * @return the data as a string
     */
    public String getCSVRow(TimeWeighted tw) {
        StringBuilder row = new StringBuilder();
        row.append(tw.getModel().getName());
        row.append(",");
        row.append("TimeWeighted");
        row.append(",");
        BatchStatistic b = getBatchStatistic(tw);
        row.append(b.getCSVStatistic());
        return row.toString();
    }

    /**
     * Gets the CSV Header for the TimeWeighted
     *
     * @param tw the time weighted variable
     * @return the header
     */
    public String getCSVHeader(TimeWeighted tw) {
        StringBuilder header = new StringBuilder();
        header.append("Model,");
        header.append("StatType,");
        BatchStatistic b = getBatchStatistic(tw);
        header.append(b.getCSVStatisticHeader());
        return header.toString();
    }

    public class TWBatchStatisticObserver extends BatchStatisticObserver {

        protected WeightedStatistic myWithinBatchStats;
        protected TimeWeighted myTW;

        public TWBatchStatisticObserver(TimeWeighted tw) {
            this(tw, BatchStatistic.MIN_NUM_BATCHES, BatchStatistic.MIN_NUM_OBS_PER_BATCH,
                    BatchStatistic.MAX_BATCH_MULTIPLE, tw.getName());
        }

        public TWBatchStatisticObserver(TimeWeighted tw, String name) {
            this(tw, BatchStatistic.MIN_NUM_BATCHES, BatchStatistic.MIN_NUM_OBS_PER_BATCH,
                    BatchStatistic.MAX_BATCH_MULTIPLE, name);
        }

        public TWBatchStatisticObserver(TimeWeighted tw, int minNumBatches, int minBatchSize,
                int maxNBMultiple) {
            this(tw, minNumBatches, minBatchSize, maxNBMultiple, tw.getName());
        }

        public TWBatchStatisticObserver(TimeWeighted tw, int minNumBatches, int minBatchSize, int maxNBMultiple, String name) {
            super(minNumBatches, minBatchSize, maxNBMultiple, name);
            myWithinBatchStats = new WeightedStatistic();
            myTW = tw;
        }

        /**
         * Returns the observed TimeWeighted
         *
         * @return the observed TimeWeighted
         */
        protected final TimeWeighted getTimeWeighted() {
            return myTW;
        }

        @Override
        protected void beforeReplication(ModelElement m, Object arg) {
            super.beforeReplication(m, arg);
            myWithinBatchStats.reset();
        }

        @Override
        protected void update(ModelElement m, Object arg) {
            double weight = getTimeWeighted().getWeight();
            double prev = getTimeWeighted().getPreviousValue();
            myWithinBatchStats.collect(prev, weight);
        }

        @Override
        protected void warmUp(ModelElement m, Object arg) {
            super.warmUp(m, arg);
            myWithinBatchStats.reset();
        }

        /**
         * Causes the observer to collect the batch statistics
         *
         */
        protected void batch() {
            double avg = myWithinBatchStats.getAverage();
            myBatchStats.collect(avg);
            myWithinBatchStats.reset();
//            if (collectFlag == false) {
//                // stop the simulation
//                StringBuilder msg = new StringBuilder();
//                msg.append(getTimeWeighted().getName()).append(" stopped replication. ");
//                msg.append(myBatchStats.getCollectionRule());
//                msg.append(" criteria met.\n");
//                getTimeWeighted().stopExecutive(msg.toString());
//            }
        }

    }

}
