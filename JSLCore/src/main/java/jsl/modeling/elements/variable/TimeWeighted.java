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

/**
 *
 */
public class TimeWeighted extends ResponseVariable {

    /**
     * Creates a TimeWeighted with the given parent with initial value 0.0 over
     * the range [Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element.
     */
    public TimeWeighted(ModelElement parent) {
        this(parent, 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Creates a TimeWeighted with the given name and initial value over the
     * range [Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element.
     * @param initialValue The initial value of the variable.
     */
    public TimeWeighted(ModelElement parent, double initialValue) {
        this(parent, initialValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Creates a TimeWeighted with the given name and initial value, 0.0, over
     * the range [Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element.
     * @param name The name of the variable.
     */
    public TimeWeighted(ModelElement parent, String name) {
        this(parent, 0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, name);
    }

    /**
     * Creates a TimeWeighted with the given name and initial value over the
     * supplied range The default range is [Double.NEGATIVE_INFINITY,
     * Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param name The name of the variable.
     */
    public TimeWeighted(ModelElement parent, double initialValue, String name) {
        this(parent, initialValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, name);
    }

    /**
     * Creates a TimeWeighted with the given name and initial value over the
     * supplied range [lowerLimit, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     * @param name The name of the variable.
     */
    public TimeWeighted(ModelElement parent, double initialValue, double lowerLimit, String name) {
        this(parent, initialValue, lowerLimit, Double.POSITIVE_INFINITY, name);
    }

    /**
     * Creates a TimeWeighted with the initial value over the supplied range
     * [lowerLimit, Double.POSITIVE_INFINITY]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     */
    public TimeWeighted(ModelElement parent, double initialValue, double lowerLimit) {
        this(parent, initialValue, lowerLimit, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Creates a TimeWeighted with the initial value over the supplied range
     * [lowerLimit, upperLimit]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     * @param upperLimit the upper limit on the range for the variable
     */
    public TimeWeighted(ModelElement parent, double initialValue, double lowerLimit, double upperLimit) {
        this(parent, initialValue, lowerLimit, upperLimit, null);
    }

    /**
     * Creates a TimeWeighted with the given name and initial value over the
     * supplied range [lowerLimit, upperLimit]
     *
     * @param parent the variable's parent model element
     * @param initialValue The initial value of the variable. Must be within the
     * range.
     * @param lowerLimit the lower limit on the range for the variable, must be
     * &lt; upperLimit
     * @param upperLimit the upper limit on the range for the variable
     * @param name The name of the variable.
     */
    public TimeWeighted(ModelElement parent, double initialValue, double lowerLimit, double upperLimit, String name) {
        super(parent, initialValue, lowerLimit, upperLimit, name);
    }

    /**
     * Increments the value of the variable by 1 at the current time.
     */
    public final void increment() {
        increment(1.0);
    }

    /**
     * Increments the value of the variable by the amount supplied. Throws an
     * IllegalArgumentException if the value is negative.
     *
     * @param value The amount to increment by. Must be non-negative.
     */
    public final void increment(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Invalid argument. Attempted an negative increment.");
        }

        setValue(myValue + value);
    }

    /**
     * Decrements the value of the variable by 1 at the current time.
     */
    public final void decrement() {
        decrement(1.0);
    }

    /**
     * Decrements the value of the variable by the amount supplied. Throws an
     * IllegalArgumentException if the value is negative.
     *
     * @param value The amount to decrement by. Must be non-negative.
     */
    public final void decrement(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Invalid argument. Attempted an negative decrement.");
        }

        setValue(myValue - value);
    }

    /**
     * Sets the weight, the current time - the time of the last change
     */
    @Override
    protected final void setWeight() {
        myWeight = getTime() - myTimeOfChange;
        if (myWeight < 0) {
            myWeight = 0.0;
        }
    }

    @Override
    protected void collectStatistics() {
        myWithinRepStats.collect(getPreviousValue(), getWeight());
        if (myWithinIntervalStats != null) {
            myWithinIntervalStats.collect(getPreviousValue(), getWeight());
        }
    }

    /**
     * Initialize the value to the current value at this time
     */
    @Override
    protected void initialize() {
//		System.out.println("In TimeWeighted initialize() " + getName());
        super.initialize();
        // this is so at least two changes are recorded on the variable
        // to properly account for variables that have zero area throughout the replication
        //setValue(getInitialValue());
        setValue(getValue());
    }

    @Override
    protected void timedUpdate() {
        // this is to capture the area under the curve up to and including
        // the current time
        setValue(getValue());
        if (myWithinIntervalStats != null) {
            if (!((myLastUpdateTime < myTimeOfWarmUp) && (myTimeOfWarmUp < getTime()))) {
                // if the warm up did not occur during the interval, then collect the average
                myAcrossIntervalResponse.setValue(myWithinIntervalStats.getAverage());
            }
            myWithinIntervalStats.reset();
        }
        myLastUpdateTime = getTime();
    }

    /**
     * Schedules the batch events for after the warm up period Sets the value of
     * the variable to the current value at the current time Resets the time of
     * last change to the current time
     */
    @Override
    protected void warmUp() {
        super.warmUp();
        // make it think that it changed at the warm up time to the same value
        myTimeOfChange = getTime();
        // this is so at least two changes are recorded on the variable
        // to properly account for variables that have zero area throughout the replication
        setValue(getValue());
    }

    /**
     * Sets the value of the variable to the current value at the current time
     * to collect state to end of replication
     */
    @Override
    protected void replicationEnded() {
        super.replicationEnded();
//        System.out.println("in replication ended for " + getName());
//        System.out.println("time = " + getTime());
        // this allows time weighted to be collected all the way to end of simulation
        setValue(getValue());
    }
}
