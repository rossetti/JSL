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
package jsl.modeling.elements.variable;

import jsl.simulation.ModelElement;
import jsl.observers.ObserverIfc;
import jsl.utilities.statistic.WeightedStatisticIfc;

/**
 * @author rossetti
 *
 */
public class AggregateTimeWeightedVariable extends Aggregate {

    /**
     * This is used to remember the aggregate value when any of its
     * aggregatable's change
     *
     */
    protected TimeWeighted myAggTW;

    /**
     * @param parent
     */
    public AggregateTimeWeightedVariable(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public AggregateTimeWeightedVariable(ModelElement parent, String name) {
        super(parent, name);
        myAggTW = new TimeWeighted(this, getName() + " : Aggregate");
        // the aggregate variable does not get initialized by the model
        // it must be initialized after all its observed variables are initialized
        myAggTW.setInitializationOption(false);
        // the aggregate variable does not get warmed up by the model
        // it must be warmed up after all its observed variables are warmed up
        myAggTW.setWarmUpOption(false);
    }

    @Override
    public void addObserver(ObserverIfc observer) {
        myAggTW.addObserver(observer);
    }

    @Override
    public void deleteObserver(ObserverIfc observer) {
        myAggTW.deleteObserver(observer);
    }

    @Override
    public void deleteObservers() {
        myAggTW.deleteObservers();
    }

    @Override
    public int countObservers() {
        return myAggTW.countObservers();
    }

    /**
     * @return @see
     * jsl.modeling.elements.variable.ResponseVariable#getDefaultReportingOption()
     */
    @Override
    public boolean getDefaultReportingOption() {
        return myAggTW.getDefaultReportingOption();
    }

    /**
     * @param flag
     * @see
     * jsl.modeling.elements.variable.ResponseVariable#setDefaultReportingOption(boolean)
     */
    @Override
    public void setDefaultReportingOption(boolean flag) {
        myAggTW.setDefaultReportingOption(flag);
    }

    public final double getInitialValue() {
        return myAggTW.getInitialValue();
    }

    /**
     * @return @see
     * jsl.utilities.PreviousValueIfc#getPreviousValue()
     */
    @Override
    public double getPreviousValue() {
        return myAggTW.getPreviousValue();
    }

    /**
     * @return @see jsl.modeling.elements.variable.ResponseVariable#getValue()
     */
    @Override
    public double getValue() {
        return myAggTW.getValue();
    }

    public final void turnOnTrace() {
        myAggTW.turnOnTrace();
    }

    public final void turnOnTrace(boolean header) {
        myAggTW.turnOnTrace(header);
    }

    public final void turnOnTrace(String fileName) {
        myAggTW.turnOnTrace(fileName);
    }

    public final void turnOnTrace(String name, boolean header) {
        myAggTW.turnOnTrace(name, header);
    }

    public final void turnOffTrace() {
        myAggTW.turnOffTrace();
    }

    @Override
    protected void variableAddedBeforeReplication(Aggregatable variable) {
//		System.out.println("################################");
//		System.out.println("A variable is being added.");
//		System.out.println("Aggegate name: " + getName());
//		System.out.println("Aggegatable's name: " + variable.getName());
//		System.out.println(variable.getName() + "  value = " + variable.getValue());

        // a new variable has just been attached to the aggregate
        // either before the start of the replication
        myAggTW.assignInitialValue(sumValues());

//		System.out.println(getName() + " previous value = " + getPreviousValue());
//		System.out.println(getName() + " new value = " + getValue());
        notifyAggregatesOfValueChange();

//		System.out.println("################################");
    }

    @Override
    protected void variableAddedDuringReplication(Aggregatable variable) {
//		System.out.println("################################");
//		System.out.println("A variable is being added.");
//		System.out.println("Aggegate name: " + getName());
//		System.out.println("Aggegatable's name: " + variable.getName());
//		System.out.println(variable.getName() + "  value = " + variable.getValue());

        // a new variable has just been attached to the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggTW.setValue(sumValues());

//		System.out.println(getName() + " previous value = " + getPreviousValue());
//		System.out.println(getName() + " new value = " + getValue());
        notifyAggregatesOfValueChange();
//		System.out.println("################################");
    }

    @Override
    protected void variableRemovedBeforeReplication(Aggregatable variable) {
        // a new variable has just been removed from the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggTW.assignInitialValue(sumValues());
        notifyAggregatesOfValueChange();
    }

    @Override
    protected void variableRemovedDuringReplication(Aggregatable variable) {
        // a new variable has just been removed from the aggregate
        // either before or after the start of a replication
        // still need to make the variable have the correct value
        myAggTW.setValue(sumValues());
        notifyAggregatesOfValueChange();
    }

    @Override
    protected void valueChangedBeforeReplication(Aggregatable variable) {
        double sum = getValue(); // current value

//		System.out.println("---------------------------");
//		System.out.println("In Aggregate's valueChangedBeforeReplication(Aggregatable variable)");
//		System.out.println("Time> " + getTime());
//		System.out.println("Aggegate name: " + getName());
//		System.out.println("Aggegatable's name: " + variable.getName());
//		System.out.println(getName() + " current value = " + getValue());
//		System.out.println(variable.getName() + " prev value = " + variable.getPreviousValue());
//		System.out.println(variable.getName() + " new value = " + variable.getValue());
        sum = sum - variable.getPreviousValue(); // subtract off the old value
//		System.out.println("after subtracting prev value: sum = " + sum);

        sum = sum + variable.getValue(); // add on the new value
//		System.out.println("after adding the new value: sum = " + sum);
//		System.out.println();
        if (sum < 0) {
            throw new IllegalStateException("The sum was negative");
        }

        myAggTW.assignInitialValue(sumValues());
//		myAggTW.setValue(sumValues());

        notifyAggregatesOfValueChange();

//		System.out.println(getName() + " final value = " + getValue());
//		System.out.println("---------------------------");
    }

    @Override
    protected void valueChangedDuringReplication(Aggregatable variable) {
        double sum = getValue(); // current value

//		System.out.println("---------------------------");
//		System.out.println("In Aggregate's valueChanged()");
//		System.out.println("Time> " + getTime());
//		System.out.println("Aggegate name: " + getName());
//		System.out.println("Aggegatable's name: " + variable.getName());
//		System.out.println(getName() + " current value = " + sum);
//		System.out.println(variable.getName() + " prev value = " + variable.getPreviousValue());
//		System.out.println(variable.getName() + " new value = " + variable.getValue());
        sum = sum - variable.getPreviousValue(); // subtract off the old value
//		System.out.println("after subtracting prev value: sum = " + sum);

        sum = sum + variable.getValue(); // add on the new value
//		System.out.println("after adding the new value: sum = " + sum);
//		System.out.println();
        if (sum < 0) {
//			System.out.println("Replication: " + this.getModel().getReplication().getReplicationNumber());
//			System.out.println("Time> " + getTime());
            throw new IllegalStateException("The sum was negative");
        }
        myAggTW.setValue(sum);
        notifyAggregatesOfValueChange();
//		System.out.println(getName() + " final value = " + getValue());
//		System.out.println("---------------------------");
    }

    @Override
    protected void initializeAggregate() {
        myAggTW.initialize_();// does not call initialize() because initialize is off
        myAggTW.initialize();
    }

    @Override
    protected void warmUpAggregate() {
        myAggTW.warmUp_();// already calls warmUp()
    }

    @Override
    protected void removedFromModel() {
        super.removedFromModel();
        myAggTW = null;
    }

    public WeightedStatisticIfc getWithinReplicationStatistic() {
        return myAggTW.getWithinReplicationStatistic();
    }

//    @Override
//    protected void replicationEnded() {
//        super.replicationEnded();
//        System.out.println("Aggregate: " + getName() + " replicationEnded(). # observing: " + myVariables.size());
//        System.out.println(myAggTW);
//    }
}
