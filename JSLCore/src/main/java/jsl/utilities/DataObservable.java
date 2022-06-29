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
package jsl.utilities;

import jsl.observers.ObservableComponent;
import jsl.observers.ObservableIfc;
import jsl.observers.ObserverIfc;
import jsl.simulation.ModelElement;

/**
 * @author rossetti
 *
 */
public abstract class DataObservable implements ObservableIfc, GetValueIfc, PreviousValueIfc, IdentityIfc {

    /** A counter to count the number of created to assign "unique" ids
     */
    protected static int myIdCounter_;

    /** An "enum" to indicate that a new value has just been made available to observers
     */
    public static final int NEW_VALUE = ModelElement.getNextEnumConstant();

    /**
     * helper for observable pattern
     *
     */
    private final ObservableComponent myObservableComponent;

    /** Keeps track of the current state for observers
     */
    private int myObserverState = 0;

    /** Keeps track of the previous type of state change for observers
     */
    private int myPreviousObserverState = 0;

    /** The id of this object
     */
    protected int myId;

    /** Holds the name of the statistic for reporting purposes.
     */
    protected String myName;

    /** The current value
     * 
     */
    private double myValue = Double.NaN;

    /** The previous value of the variable.
     */
    private double myPrevValue = Double.NaN;

    /**
     *
     */
    public DataObservable() {
        this(null);
    }

    /**
     *
     */
    public DataObservable(String name) {
        setId();
        setName(name);
        myObservableComponent = new ObservableComponent();
    }

    /** Gets the name.
     * @return The name of object.
     */
    public final String getName() {
        return myName;
    }

    /** Sets the name
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            myName = s;
        } else {
            myName = str;
        }
    }

    /** Returns the id for this data source
     *
     * @return
     */
    public final int getId() {
        return (myId);
    }

    /** Every data source must implement the getValue method.  This method simply
     * returns the current value.  Returns Double.NaN if no current value
     *  is available
     *  
     * @return The value.
     */
    public double getValue() {
        return (myValue);
    }

    /** Returns the previous value for this data source if there was one
     *  returns Double.NaN if no previous value is available.
     * 
     * @return
     */
    public double getPreviousValue() {
        return (myPrevValue);
    }

    /** Checks to see if the technique is in the given observer state.
     *  This method can be used by observers that are interested in reacting to the
     *  action associated with this state for the technique.
     *
     *  NEW_VALUE
     *
     * @return True means that this DataSource is in the given state.
     */
    public final boolean checkObserverState(int observerState) {
        return (myObserverState == observerState);
    }

    /**  Returns an integer representing the state of the technique
     *   This can be used by Observers to find out what occurred for the technique
     *
     * @return The current state
     */
    public final int getObserverState() {
        return (myObserverState);
    }

    /**  Returns an integer representing the previous state of the DataSource
     *   This can be used by Observers to find out which action occurred prior to the current state change
     *
     * @return The previous state
     */
    public final int getPreviousObserverState() {
        return (myPreviousObserverState);
    }

    protected void setId() {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
    }

    /** Properly assigns the value and remembers previous value
     *  notifies any observers of the change
     * 
     * @param value
     */
    protected void setValue(double value) {
        // remember the old values
        myPrevValue = myValue;
        // record the new value and time
        myValue = value;
        notifyObservers(NEW_VALUE);
    }

    /** Used to notify observers that this data source has entered the given state.
     *  Valid values for observerState include:
     *
     *  NEW_VALUE
     *
     * @param observerState
     */
    protected final void notifyObservers(int observerState) {
        setObserverState(observerState);
        notifyObservers(this, null);
    }

    /**
     * @param observerState
     */
    protected final void setObserverState(int observerState) {
        myPreviousObserverState = myObserverState;
        myObserverState = observerState;
    }

    @Override
    public final void addObserver(ObserverIfc observer) {
        myObservableComponent.addObserver(observer);
    }

    @Override
    public final void deleteObserver(ObserverIfc observer) {
        myObservableComponent.deleteObserver(observer);
    }

    @Override
    public final void deleteObservers() {
        myObservableComponent.deleteObservers();
    }

    @Override
    public final boolean contains(ObserverIfc observer) {
        return myObservableComponent.contains(observer);
    }

    @Override
    public final int countObservers() {
        return myObservableComponent.countObservers();
    }

    protected final void notifyObservers(Object theObserved, Object arg) {
        myObservableComponent.notifyObservers(theObserved, arg);
    }
}
