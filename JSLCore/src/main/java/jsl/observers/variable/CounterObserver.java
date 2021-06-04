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
package jsl.observers.variable;

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.Counter;
import jsl.observers.ModelElementObserver;
import jsl.utilities.statistic.*;

/**
 *
 */
public class CounterObserver extends ModelElementObserver {

    /** The Counter that is being observed
     */
    protected Counter myCounter;

    /** The Model that holds the Counter being observed
     */
    protected Model myModel;

    /** Statistic used to collect the across replication statistics on the final
     *  value of the counter during each replication
     */
    protected Statistic myAcrossRepStat;

    /** A Counter can have a timed update action during a replication
     *  This Statistic collects the average of the total count
     *  during each timed update interval.  For example, a timed
     *  update interval of 1 hour can be set.  Each hour this statistic
     *  will observe the count for that hour and collect averages across
     *  the timede update intervals
     */
    protected Statistic myAcrossTimedUpdateStatistic;

    /** If the timed update statistics are collected during a replication
     *  This Statistic will be used to collect the average across the
     *  averages over the update intervals
     */
    protected Statistic myAcrossRepTimedUpdateStatistic;

    /**
     *
     */
    public CounterObserver() {
        this(null);
    }

    /**
     * @param name the name
     */
    public CounterObserver(String name) {
        super(name);
        myAcrossRepStat = new Statistic();
    }

    /** Resets any statistics collected across the timed update intervals
     *  for within a replication
     */
    public final void resetTimedUpdateStatistics() {

        if (myAcrossTimedUpdateStatistic != null) {
            myAcrossTimedUpdateStatistic.reset();
        }
    }

    /** Returns a StatisticAccessorIfc for the statistics collected across
     *  timed update intervals within a replication.
     *
     * @return Returns the statistic
     */
    public final StatisticAccessorIfc getAcrossTimedUpdateStatistic() {
        return myAcrossTimedUpdateStatistic;
    }

    /** Returns a StatisticAccessorIfc for the statistics collected across
     *  timed update intervals across the replications
     *
     * @return Returns the statistic
     */
    public final StatisticAccessorIfc getAcrossRepTimedUpdateStatistic() {
        return myAcrossRepTimedUpdateStatistic;
    }

    /** Gets the statistics that have been accumulated across all replications
     *  for this counter.
     *
     * @return A StatisticAccessorIfc representing the across replication statistics.
     */
    public final StatisticAccessorIfc getAcrossReplicationStatistic() {
        return (myAcrossRepStat);
    }

    /** A convenience method to set the name of the underlying Statistic
     *  for tabulating across replication statistics
     *
     * @param name the name
     */
    public void setAcrossReplicationStatisticName(String name) {
        myAcrossRepStat.setName(name);
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();

        if (myAcrossRepStat.getCount() >= 2.0) {
            sb.append("-------------------------------------------------\n");
            sb.append("Across Replication Statistic:\n");
            sb.append("-------------------------------------------------\n");
            sb.append(myAcrossRepStat);
            sb.append("-------------------------------------------------\n");
            sb.append("\n");
        }

        return (sb.toString());
    }

    protected void beforeExperiment(ModelElement m, Object arg) {
        myCounter = (Counter) m;
        myModel = myCounter.getModel();
        myAcrossRepStat.setName("Across Rep Stat " + myCounter.getStringLabel());
        myAcrossRepStat.reset();
        resetTimedUpdateStatistics();
        if (myCounter.getTimedUpdateInterval() > 0) { // if timed update option is on, create the statistic for collecting on the intervals
            // counter uses a statistic to collect across timed updates
            myAcrossTimedUpdateStatistic = new Statistic("AcrossTimedUpdate Stat " + myCounter.getStringLabel());
            myAcrossRepTimedUpdateStatistic = new Statistic("AcrossRepTimedUpdate Stat " + myCounter.getStringLabel());
        }
    }

    protected void beforeReplication(ModelElement m, Object arg) {
        myCounter = (Counter) m;
        myModel = myCounter.getModel();
        resetTimedUpdateStatistics();
    }

    protected void initialize(ModelElement m, Object arg) {
        myCounter = (Counter) m;
        myModel = myCounter.getModel();
        resetTimedUpdateStatistics();
    }

    protected void warmUp(ModelElement m, Object arg) {
        resetTimedUpdateStatistics();
    }

    protected void timedUpdate(ModelElement m, Object arg) {
        // determine the count since last timed update
        if (myAcrossTimedUpdateStatistic != null) {
            Counter c = (Counter) m;
            double count = c.getTotalDuringTimedUpdate();
            myAcrossTimedUpdateStatistic.collect(count);
        }
    }

    protected void afterReplication(ModelElement m, Object arg) {
        Counter c = (Counter) m;
        myAcrossRepStat.collect(c.getValue());
        if (myAcrossRepTimedUpdateStatistic != null) {
            myAcrossRepTimedUpdateStatistic.collect(myAcrossTimedUpdateStatistic.getAverage());
        }
    }

    protected void afterExperiment(ModelElement m, Object arg) {
//	    if(myAcrossTimedUpdateStatistic != null){
//	    	JSL.out.println("Timed update results for Counter .....");
//	    	JSL.out.println(myAcrossRepTimedUpdateStatistic);
//	    }
        //JSL.out.println(toString());
    }

    protected void removedFromModel(ModelElement m, Object arg) {
        myCounter = null;
        myAcrossRepStat = null;
        myAcrossTimedUpdateStatistic = null;
        myAcrossRepTimedUpdateStatistic = null;
        myModel = null;
    }
}
