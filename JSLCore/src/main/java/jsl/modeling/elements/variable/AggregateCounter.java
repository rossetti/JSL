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

/**
 * 
 */
package jsl.modeling.elements.variable;

import jsl.simulation.ModelElement;
import jsl.utilities.statistic.StatisticAccessorIfc;

/**
 * @author rossetti
 *
 */
public class AggregateCounter extends Aggregate implements CounterActionIfc {

    /** This is used to remember the aggregate value
     *  when any of its aggregatable's change
     *
     */
    protected Counter myAggCounter;

    /**
     * @param parent
     */
    public AggregateCounter(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public AggregateCounter(ModelElement parent, String name) {
        super(parent, name);
        myAggCounter = new Counter(this, getName() + " : Aggregate");
        // the aggregate variable does not get initialized by the model
        // it must be initialized after all its observed variables are initialized
        myAggCounter.setInitializationOption(false);
        // the aggregate variable does not get warmed up by the model
        // it must be warmed up after all its observed counters are warmed up
        myAggCounter.setWarmUpOption(false);
    }

    @Override
    public boolean getDefaultReportingOption() {
        return myAggCounter.getDefaultReportingOption();
    }

    @Override
    public void setDefaultReportingOption(boolean flag) {
        myAggCounter.setDefaultReportingOption(flag);
    }

    @Override
    public double getPreviousValue() {
        return myAggCounter.getPreviousValue();
    }

    @Override
    public double getValue() {
        return myAggCounter.getValue();
    }

    /* (non-Javadoc)
     * @see jsl.modeling.elements.variable.Aggregate#valueChanged(jsl.modeling.elements.variable.Aggregatable)
     */
    @Override
    protected void valueChangedBeforeReplication(Aggregatable variable) {
        // a new variable has just been attached to the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggCounter.setInitialValue((long) sumValues());
        myAggCounter.notifyUpdateObservers();
        notifyAggregatesOfValueChange();
    }

    /* (non-Javadoc)
     * @see jsl.modeling.elements.variable.Aggregate#variableAdded(jsl.modeling.elements.variable.Aggregatable)
     */
    @Override
    protected void variableAddedBeforeReplication(Aggregatable variable) {
        // a new variable has just been attached to the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggCounter.setInitialValue((long) sumValues());
        myAggCounter.notifyUpdateObservers();
        notifyAggregatesOfValueChange();
    }

    /* (non-Javadoc)
     * @see jsl.modeling.elements.variable.Aggregate#variableRemoved(jsl.modeling.elements.variable.Aggregatable)
     */
    @Override
    protected void variableRemovedBeforeReplication(Aggregatable variable) {
        // a new variable has just been attached to the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggCounter.setInitialValue((long) sumValues());
        myAggCounter.notifyUpdateObservers();
        notifyAggregatesOfValueChange();
    }

    @Override
    public boolean addCounterActionListener(CounterActionListenerIfc action) {
        return myAggCounter.addCounterActionListener(action);
    }

    @Override
    public void addStoppingAction() {
        myAggCounter.addStoppingAction();
    }

    @Override
    public boolean checkForCounterLimitReachedState() {
        return myAggCounter.checkForCounterLimitReachedState();
    }

    @Override
    public double getCounterActionLimit() {
        return myAggCounter.getCounterActionLimit();
    }

    @Override
    public boolean removeCounterActionListener(CounterActionListenerIfc action) {
        return myAggCounter.removeCounterActionListener(action);
    }

    /**
     * @return a statistical accessor for across replication statistics
     * @see jsl.modeling.elements.variable.AcrossReplicationStatisticIfc#getAcrossReplicationStatistic()
     */
    public StatisticAccessorIfc getAcrossReplicationStatistic() {
        return myAggCounter.getAcrossReplicationStatistic();
    }

    @Override
    protected void valueChangedDuringReplication(Aggregatable variable) {
        // get the amount incremented for changed counter
        double sum = variable.getValue() - variable.getPreviousValue();
        // now increment aggregate
        myAggCounter.increment((long) sum);
        notifyAggregatesOfValueChange();
    }

    @Override
    protected void variableAddedDuringReplication(Aggregatable variable) {
        // a new variable has just been attached to the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggCounter.setValue((long) sumValues());
        myAggCounter.notifyUpdateObservers();
        notifyAggregatesOfValueChange();
    }

    @Override
    protected void variableRemovedDuringReplication(Aggregatable variable) {
        // a new variable has just been attached to the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggCounter.setValue((long) sumValues());
        myAggCounter.notifyUpdateObservers();
        notifyAggregatesOfValueChange();
    }

    @Override
    protected void initializeAggregate() {
        myAggCounter.initialize_();
        myAggCounter.initialize();
    }

    @Override
    protected void warmUpAggregate() {
        myAggCounter.warmUp_();
    }

    @Override
    protected void removedFromModel() {
        super.removedFromModel();
        myAggCounter = null;
    }
}
