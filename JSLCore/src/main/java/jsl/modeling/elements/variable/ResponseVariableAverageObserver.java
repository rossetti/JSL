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
import jsl.observers.ObserverIfc;
import jsl.utilities.PreviousValueIfc;
import jsl.utilities.statistic.WeightedStatisticIfc;

/**
 * @author rossetti
 *
 */
public class ResponseVariableAverageObserver extends Aggregate {

    /** This is used to remember the aggregate value
     *  when any of its aggregatable's change
     *
     */
    protected ResponseVariable myAggResponse;

    /**
     * @param parent
     */
    public ResponseVariableAverageObserver(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public ResponseVariableAverageObserver(ModelElement parent, String name) {
        super(parent, name);
        myAggResponse = new ResponseVariable(this, getName() + " : Aggregate");
        // the aggregate variable does not get initialized by the model" : " + 
        // it must be initialized after all its observed variables are initialized
        myAggResponse.setInitializationOption(false);
        // the aggregate variable does not get warmed up by the model
        // it must be warmed up after all its observed variables are warmed up
        myAggResponse.setWarmUpOption(false);
    }

    @Override
    public void addObserver(ObserverIfc observer) {
        myAggResponse.addObserver(observer);
    }

    @Override
    public void deleteObserver(ObserverIfc observer) {
        myAggResponse.deleteObserver(observer);
    }

    @Override
    public void deleteObservers() {
        myAggResponse.deleteObservers();
    }

    @Override
    public int countObservers() {
        return myAggResponse.countObservers();
    }

    /**
     * @return
     * @see jsl.modeling.elements.variable.ResponseVariable#getDefaultReportingOption()
     */
    public boolean getDefaultReportingOption() {
        return myAggResponse.getDefaultReportingOption();
    }

    /**
     * @param flag
     * @see jsl.modeling.elements.variable.ResponseVariable#setDefaultReportingOption(boolean)
     */
    public void setDefaultReportingOption(boolean flag) {
        myAggResponse.setDefaultReportingOption(flag);
    }

    /**
     * @return
     * @see PreviousValueIfc#getPreviousValue()
     */
    public double getPreviousValue() {
        return myAggResponse.getPreviousValue();
    }

    /**
     * @return
     * @see jsl.modeling.elements.variable.ResponseVariable#getValue()
     */
    public double getValue() {
        return myAggResponse.getValue();
    }

    /* (non-Javadoc)
     * @see jsl.modeling.elements.variable2.Aggregate#valueChanged(jsl.modeling.elements.variable2.Aggregatable)
     */
    @Override
    protected void valueChangedBeforeReplication(Aggregatable variable) {
        // no need to do anything since it is before the replication
    }

    /* (non-Javadoc)
     * @see jsl.modeling.elements.variable2.Aggregate#variableAdded(jsl.modeling.elements.variable2.Aggregatable)
     */
    @Override
    protected void variableAddedBeforeReplication(Aggregatable variable) {
        // A new variable has been added for aggregating
        // no need to do anything since it is the value of the variable
        // when it changes that is important.  The average
        // across all presented doesn't change when a new variable is added
    }

    /* (non-Javadoc)
     * @see jsl.modeling.elements.variable2.Aggregate#variableRemoved(jsl.modeling.elements.variable2.Aggregatable)
     */
    @Override
    protected void variableRemovedBeforeReplication(Aggregatable variable) {
        // A new variable has been removed from aggregating
        // no need to do anything since it is the value of the variable
        // when it changes that is important.  The average
        // across all presented doesn't change when a new variable is added
    }

    @Override
    protected void valueChangedDuringReplication(Aggregatable variable) {
        myAggResponse.setValue(variable.getValue());
        notifyAggregatesOfValueChange();
    }

    @Override
    protected void variableAddedDuringReplication(Aggregatable variable) {
        // A new variable has been added for aggregating
        // no need to do anything since it is the value of the variable
        // when it changes that is important.  The average
        // across all presented doesn't change when a new variable is added
    }

    @Override
    protected void variableRemovedDuringReplication(Aggregatable variable) {
        // A new variable has been removed from aggregating
        // no need to do anything since it is the value of the variable
        // when it changes that is important.  The average
        // across all presented doesn't change when a new variable is added
    }

    @Override
    protected void initializeAggregate() {
        myAggResponse.initialize_();
        myAggResponse.initialize();
    }

    @Override
    protected void warmUpAggregate() {
        myAggResponse.warmUp_();
    }

    protected void removedFromModel() {
        super.removedFromModel();
        myAggResponse = null;
    }

    public WeightedStatisticIfc getWithinReplicationStatistic() {
        return myAggResponse.getWithinReplicationStatistic();
    }
}
