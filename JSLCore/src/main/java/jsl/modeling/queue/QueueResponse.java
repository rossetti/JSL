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
package jsl.modeling.queue;

import jsl.modeling.elements.variable.Aggregate;
import jsl.modeling.elements.variable.AggregateTimeWeightedVariable;
import jsl.modeling.elements.variable.AveragePerTimeWeightedVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.ResponseVariableAverageObserver;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.observers.ObserverIfc;
import jsl.utilities.statistic.StatisticAccessorIfc;
import jsl.utilities.statistic.WeightedStatisticIfc;

import java.util.Objects;

/**
 *
 * @author rossetti
 */
public class QueueResponse<T extends QObject> implements QueueListenerIfc<T> {

    private final Queue myQueue;

    /**
     * Tracks the number in queue.
     */
    private final TimeWeighted myNumInQ;

    /**
     * Tracks the time in queue.
     */
    private final ResponseVariable myTimeInQ;

    QueueResponse(Queue queue) {
        Objects.requireNonNull(queue, "The supplied Queue must not be null");
//        super(parent, name);
        myQueue = queue;
        myNumInQ = new TimeWeighted(queue, 0.0, myQueue.getName() + ":NumInQ");
        myTimeInQ = new ResponseVariable(queue, myQueue.getName() + ":TimeInQ");
    }

    @Override
    public void update(T qObject) {
        switch (myQueue.getStatus()) {
            case ENQUEUED:
                myNumInQ.setValue(myQueue.size());
                break;
            case DEQUEUED:
                myNumInQ.setValue(myQueue.size());
                double wTime = qObject.getTimeExitedQueue() - qObject.getTimeEnteredQueue();
                myTimeInQ.setValue(wTime);
//                myTimeInQ.setValue(qObject.getTimeInQueue());
                break;
            case IGNORE:
                myNumInQ.setValue(myQueue.size());
                break;
            default:
                throw new IllegalStateException("Queue Status was not ENQUEUED, IGNORE, or DEQUEUED");
        }
    }

    /**
     * Turns on the tracing to a text file of the times in queue.
     */
    public final void turnOnTimeInQTrace() {
        myTimeInQ.turnOnTrace();
    }

    /**
     * Turns on the tracing to a text file of the times in queue.
     *
     * @param header the header
     */
    public final void turnOnTimeInQTrace(boolean header) {
        myTimeInQ.turnOnTrace(header);
    }

    /**
     * Turns on the tracing to a text file of the times in queue.
     *
     * @param fileName the file name
     */
    public final void turnOnTimeInQTrace(String fileName) {
        myTimeInQ.turnOnTrace(fileName);
    }

    /**
     * Turns on the tracing to a text file of the times in queue.
     *
     * @param fileName the file name
     * @param header the header
     */
    public final void turnOnTimeInQTrace(String fileName, boolean header) {
        myTimeInQ.turnOnTrace(fileName, header);
    }

    /**
     * Turns on the tracing to a text file the number in queue for each state
     * change.
     */
    public final void turnOnNumberInQTrace() {
        myNumInQ.turnOnTrace();
    }

    /**
     * Turns on the tracing to a text file the number in queue for each state
     * change.
     *
     * @param header the header
     */
    public final void turnOnNumberInQTrace(boolean header) {
        myNumInQ.turnOnTrace(header);
    }

    /**
     * Turns on the tracing to a text file the number in queue for each state
     * change.
     *
     * @param fileName the file name
     */
    public final void turnOnNumberInQTrace(String fileName) {
        myNumInQ.turnOnTrace(fileName);
    }

    /**
     * Turns on the tracing to a text file the number in queue for each state
     * change.
     *
     * @param fileName the file name
     * @param header the header
     */
    public final void turnOnNumberInQTrace(String fileName, boolean header) {
        myNumInQ.turnOnTrace(fileName, header);
    }

    /**
     * Turns off the tracing of the times in queue.
     */
    public final void turnOffTimeInQTrace() {
        myTimeInQ.turnOffTrace();
    }

    /**
     * Turns off the tracing of the number in queue.
     */
    public final void turnOffNumberInQTrace() {
        myNumInQ.turnOffTrace();
    }

    /**
     * Get the number in queue across replication statistics
     *
     * @return the statistic
     */
    public final StatisticAccessorIfc getNumInQAcrossReplicationStatistic() {
        return myNumInQ.getAcrossReplicationStatistic();
    }

    /**
     * Get the time in queue across replication statistics
     *
     * @return the statistic
     */
    public final StatisticAccessorIfc getTimeInQAcrossReplicationStatistic() {
        return myTimeInQ.getAcrossReplicationStatistic();
    }

    /**
     * Within replication statistics for time in queue
     *
     * @return Within replication statistics for time in queue
     */
    public final WeightedStatisticIfc getTimeInQWithinReplicationStatistic() {
        return myTimeInQ.getWithinReplicationStatistic();
    }

    /**
     * Within replication statistics for number in queue
     *
     * @return the within replication statistics for number in queue
     */
    public final WeightedStatisticIfc getNumInQWithinReplicationStatistic() {
        return myNumInQ.getWithinReplicationStatistic();
    }

    /**
     * Allows access to across interval response for number in queue if turned
     * on
     *
     * @return the across interval response
     */
    public final ResponseVariable getNumInQAcrossIntervalResponse() {
        return myNumInQ.getAcrossIntervalResponse();
    }

    /**
     * Allows access to across interval response for time in queue if turned on
     *
     * @return the across interval response
     */
    public final ResponseVariable getTimeInQAcrossIntervalResponse() {
        return myTimeInQ.getAcrossIntervalResponse();
    }

    /**
     * Allows for the collection of across replication statistics on the average
     * maximum time spent in queue
     *
     */
    public final void turnOnAcrossReplicationMaxTimeInQueueCollection() {
        myTimeInQ.turnOnAcrossReplicationMaxCollection();
    }

    /**
     * Allows for the collection of across replication statistics on the average
     * maximum number in queue
     *
     */
    public final void turnOnAcrossReplicationMaxNumInQueueCollection() {
        myNumInQ.turnOnAcrossReplicationMaxCollection();
    }

    /**
     * A convenience method to turn on collection of both the maximum time in
     * queue and the maximum number in queue
     *
     */
    public final void turnOnAcrossReplicationMaxCollection() {
        turnOnAcrossReplicationMaxTimeInQueueCollection();
        turnOnAcrossReplicationMaxNumInQueueCollection();
    }

    /**
     * Allows an observer to be attached to the time in queue response variable
     *
     * @param observer the observer
     */
    public final void addTimeInQueueObserver(ObserverIfc observer) {
        myTimeInQ.addObserver(observer);
    }

    /**
     * Allows an observer to be removed from the time in queue response variable
     *
     * @param observer the observer
     */
    public final void removeTimeInQueueObserver(ObserverIfc observer) {
        myTimeInQ.deleteObserver(observer);
    }

    /**
     * Allows an observer to be attached to the number in queue time weighted
     * variable
     *
     * @param observer the observer
     */
    public final void addNumberInQueueObserver(ObserverIfc observer) {
        myNumInQ.addObserver(observer);
    }

    /**
     * Allows an observer to be removed from the number in queue time weighted
     * variable
     *
     * @param observer the observer
     */
    public final void removeNumberInQueueObserver(ObserverIfc observer) {
        myNumInQ.deleteObserver(observer);
    }

    /**
     * Causes the supplied AggregateTimeWeightedVariable to
     * be subscribed to the number in queue variable
     *
     * @param aggregate the aggregate
     */
    public void subscribe(AggregateTimeWeightedVariable aggregate) {
        aggregate.subscribeTo(myNumInQ);
    }

    /**
     * Causes the supplied AggregateTimeWeightedVariable to
     * be unsubscribed from the number in queue variable
     *
     * @param aggregate the aggregate
     */
    public void unsubscribe(AggregateTimeWeightedVariable aggregate) {
        aggregate.unsubscribeFrom(myNumInQ);
    }

    /**
     * Causes the supplied AveragePerTimeWeightedVariable to
     * be subscribed to the number in queue variable
     *
     * @param aggregate the aggregate
     */
    public void subscribe(AveragePerTimeWeightedVariable aggregate) {
        aggregate.subscribeTo(myNumInQ);
    }

    /**
     * Causes the supplied AveragePerTimeWeightedVariable to
     * be unsubscribed from the number in queue variable
     *
     * @param aggregate the aggregate
     */
    public void unsubscribe(AveragePerTimeWeightedVariable aggregate) {
        aggregate.unsubscribeFrom(myNumInQ);
    }

    /**
     * Causes the supplied ResponseVariableAverageObserver to
     * be subscribed to the time in queue variable
     *
     * @param aggregate the aggregate
     */
    public void subscribe(ResponseVariableAverageObserver aggregate) {
        aggregate.subscribeTo(myTimeInQ);
    }

    /**
     * Causes the supplied ResponseVariableAverageObserver to
     * be unsubscribed from the time in queue variable
     *
     * @param aggregate the aggregate
     */
    public void unsubscribe(ResponseVariableAverageObserver aggregate) {
        aggregate.unsubscribeFrom(myTimeInQ);
    }

    /**
     * Allows an Aggregate to subscribe to the time in queue variable
     *
     * @param aggregate the aggregate
     */
    public final void subscribeToTimeInQueue(Aggregate aggregate) {
        aggregate.subscribeTo(myTimeInQ);
    }

    /**
     * Allows an Aggregate to unsubscribe from the time in queue variable
     *
     * @param aggregate the aggregate
     */
    public final void unsubscribeFromTimeInQueue(Aggregate aggregate) {
        aggregate.unsubscribeFrom(myTimeInQ);
    }

    /**
     * Allows an Aggregate to subscribe to the number in queue variable
     *
     * @param aggregate the aggregate
     */
    public final void subscribeToNumberInQueue(Aggregate aggregate) {
        aggregate.subscribeTo(myNumInQ);
    }

    /**
     * Allows an Aggregate to unsubscribe from the number in queue variable
     *
     * @param aggregate the aggregate
     */
    public final void unsubscribeFromNumberInQueue(Aggregate aggregate) {
        aggregate.unsubscribeFrom(myNumInQ);
    }

}
