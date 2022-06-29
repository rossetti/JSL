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
package jsl.modeling.elements.station;

import jsl.simulation.ModelElement;
import jsl.simulation.ModelElementState;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.utilities.statistic.StatisticAccessorIfc;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A SResource represents a simple resource that can have units become busy. A
 * resource is considered busy when it has 1 or more units busy. A resource is
 * considered idle when all available units are idle. A resource has an initial
 * capacity, which represents the units that can be allocated.
 *
 * The capacity of the resource represents the maximum number of units available
 * for use. For example, if the resource has capacity 3, it may have 2 units busy
 * and 1 unit idle. A resource cannot have more units busy than the capacity.
 *
 * @author rossetti
 */
public class SResource extends SchedulingElement {

    /**
     * Indicates that the resource was seized for state change purposes That is,
     * units of the resource became busy.
     */
    public static final int SEIZE = ModelElement.getNextEnumConstant();

    /**
     * Indicates that the resource was released for state change purposes That
     * is, units of resource became idle.
     */
    public static final int RELEASE = ModelElement.getNextEnumConstant();
    /**
     * The initial capacity of the resource at time just prior to 0.0
     */
    private int myInitialCapacity;

    /**
     * The capacity of the resource at time any time t
     */
    private int myCapacity;

    /**
     * Counts how many times the resource has units become busy
     */
    private int myNumTimesSeized;

    /**
     * Counts how many times the resource has units become idle
     */
    private int myNumTimesReleased;

    /**
     * The busy state, keeps track of when all units are busy
     *
     */
    private ModelElementState myBusyState;

    /**
     * The idle state, keeps track of when there are idle units i.e. if any unit
     * is idle then the resource as a whole is considered idle
     */
    private ModelElementState myIdleState;

    /**
     * The current state of the resource
     *
     */
    private ModelElementState myState;

    /**
     * The previous state of the resource
     *
     */
    private ModelElementState myPrevState;

    /**
     * The current number of busy units for the resource
     */
    private TimeWeighted myNumBusy;

    /**
     * The current fraction of busy units
     */
    private TimeWeighted myUtil;

    /**
     * The listeners for state changes on the resource
     *
     */
    private Set<SResourceStateChangeListenerIfc> myStateChangeListeners;

    /**
     * The capacity is set to 1
     *
     * @param parent the parent model element
     */
    public SResource(ModelElement parent) {
        this(parent, 1, null);
    }

    /**
     * The capacity is set to 1
     *
     * @param parent the parent model element
     * @param name a unique name for the resource
     */
    public SResource(ModelElement parent, String name) {
        this(parent, 1, name);
    }

    /**
     * The capacity represents the maximum number of units available to use when
     * the resource is idle.
     *
     * @param parent the parent model element
     * @param capacity the initial capacity of the resource
     */
    public SResource(ModelElement parent, int capacity) {
        this(parent, capacity, null);
    }

    /**
     * The capacity represents the maximum number of units available to use when
     * the resource is idle.
     *
     * @param parent the parent model element
     * @param capacity the initial capacity of the resource
     * @param name a unique name for the resource
     */
    public SResource(ModelElement parent, int capacity, String name) {
        super(parent, name);
        setInitialCapacity(capacity);
        myBusyState = new ModelElementState(this, getName() + ":Busy");
        myIdleState = new ModelElementState(this, getName() + ":Idle");
        myUtil = new TimeWeighted(this, 0.0, getName() + ":Util");
        // assume no units are busy and initial state is idle
        myNumBusy = new TimeWeighted(this, 0.0, getName() + ":BusyUnits");
        myState = myIdleState;
        myPrevState = null;
    }

    @Override
    protected void initialize() {
        super.initialize();
        myNumTimesSeized = 0;
        myNumTimesReleased = 0;
        // start of replication with full capacity (no busy units)
        myCapacity = getInitialCapacity();
        // assume start at time just prior to 0.0 that resource is in idle state
        myPrevState = null;
        myState = myIdleState;
        myState.enter();
    }

    /**
     * Returns how many times the resource has had units used.
     *
     * @return how many times the resource has had units used.
     */
    public final int getNumberTimesSeized() {
        return myNumTimesSeized;
    }

    /**
     * Returns how many times the resource has had units returned from use.
     *
     * @return how many times the resource has had units returned from use
     */
    public final int getNumberTimesReleased() {
        return myNumTimesReleased;
    }

    /**
     * The fraction of the capacity that is currently busy
     *
     * @return fraction of the capacity that is currently busy
     */
    public final double getFractionBusy() {
        return (myNumBusy.getValue() / getCapacity());
    }

    /**
     * The number of units that are currently busy
     *
     * @return number of units that are currently busy
     */
    public final int getNumBusy() {
        return (int) myNumBusy.getValue();
    }

    /**
     * Seizes 1 unit of the resource. If the resource has no units available,
     * then an exception occurs. If getNumberAvailable() = 0 then an exception
     * occurs.
     *
     */
    public final void seize() {
        SResource.this.seize(1);
    }

    /**
     * Seizes amt units of the resource. If amt = 0, then an exception occurs. If
     * the resource has no units available, then an exception occurs. If the amt
     * &gt; getNumberAvailable() then an exception occurs.
     *
     * @param amt the amount to seize
     */
    public void seize(int amt) {
        if (amt == 0) {
            throw new IllegalArgumentException("Tried to increment by 0");
        }
        if (!hasAvailableUnits()) {
            String s = "Tried to increment number busy with no units available";
            throw new IllegalStateException(s);
        }

        if (amt > getNumberAvailable()) {
            String s;
            s = "Tried to increment number busy by " + amt + " with " + getNumberAvailable() + " units available";
            throw new IllegalStateException(s);
        }
        myNumBusy.increment(amt);
        myUtil.setValue(getFractionBusy());
        myNumTimesSeized++;
        updateState(SEIZE);
    }

    protected void updateState(int change) {
        if (change == SEIZE) {
            // units being seized
            // could be busy and remain busy
            // could have been idle and now should be busy
            if (isIdle()) {
                // was idle, could now be busy
                if (getNumBusy() > 0) {
                    // does not have available units, is now busy
                    setState(myBusyState);
                }
            }
        } else if (change == RELEASE) {
            // units being released
            // must have been busy, could now be idle
            if (isBusy()) {
                // was busy, becomes idle if no units are busy
                if (getNumBusy() == 0) {
                    setState(myIdleState);
                }
            }
        }
    }

    /**
     * Releases 1 unit of the resource. If the number busy is zero, then an
     * exception occurs.
     *
     */
    public final void release() {
        SResource.this.release(1);
    }

    /**
     * Releases amt units of the resource. If amt is 0 then an exception occurs.
     * If the amt is &gt; getNumBusy() then an exception occurs
     *
     * @param amt the amount to release
     */
    public void release(int amt) {
        if (amt == 0) {
            throw new IllegalArgumentException("Tried to decrement by 0");
        }
        if (amt > getNumBusy()) {
            String s;
            s = "Tried to decrement number busy by " + amt + " with " + getNumBusy() + " units busy.";
            throw new IllegalStateException(s);
        }
        myNumBusy.decrement(amt);
        myUtil.setValue(getFractionBusy());
        myNumTimesReleased++;
        updateState(RELEASE);
    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append("\n");
        sb.append("State = ");
        sb.append(myState);
        sb.append("\n");
        sb.append("Capacity = ");
        sb.append(getCapacity());
        sb.append("\t");
        sb.append("Number Available = ");
        sb.append(getNumberAvailable());
        sb.append("\t");
        sb.append("Number Busy = ");
        sb.append(getNumBusy());
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Checks if the resource is idle. The resource is idle if all available
     * units are idle.
     *
     * @return true if idle, false otherwise
     */
    public final boolean isIdle() {
        return (myState == myIdleState);
    }

    /**
     * Checks to see if the resource is busy. The resource is busy if 1 or more
     * of its units are busy.
     *
     * @return true if busy
     */
    public final boolean isBusy() {
        return (myState == myBusyState);
    }

    /**
     * The current capacity of the resource.
     *
     * @return the capacity
     */
    public final int getCapacity() {
        return myCapacity;
    }

    /**
     * Returns the number of units that are currently available for use
     *
     * @return the number of units that are currently available for use
     */
    public int getNumberAvailable() {
        return getCapacity() - getNumBusy();
    }

    /**
     * Returns true if getNumberAvailable() &gt; 0
     *
     * @return true if getNumberAvailable() &gt; 0
     */
    public final boolean hasAvailableUnits() {
        return (getNumberAvailable() > 0);
    }

    /**
     * Gets the initial capacity of the resource
     *
     * @return the initial capacity of the resource
     */
    public final int getInitialCapacity() {
        return myInitialCapacity;
    }

    /**
     * Sets the initial capacity of the resource. This only changes it for when
     * the resource is initialized.
     *
     * @param capacity the initial capacity of the resource
     */
    public final void setInitialCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Attempted to set resource capacity less or equal to zero!");
        }
        myInitialCapacity = capacity;
    }

    public void detachStateChangeListener(SResourceStateChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null");
        }
        if (myStateChangeListeners == null) {
            return;
        }
        myStateChangeListeners.remove(listener);
    }

    public void attachStateChangeListener(SResourceStateChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null");
        }
        if (myStateChangeListeners == null) {
            myStateChangeListeners = new LinkedHashSet<>();
        }

        myStateChangeListeners.add(listener);
    }

    protected void notifyStateChangeListeners() {
        if (myStateChangeListeners == null) {
            return;
        }
        for (SResourceStateChangeListenerIfc listener : myStateChangeListeners) {
            listener.stateChange(this);
        }
    }

    protected void setState(ModelElementState state) {
        setPreviousState(myState);
        myState.exit();
        myState = state;
        myState.enter();
        notifyStateChangeListeners();
    }

    protected ModelElementState getPreviousState() {
        return myPrevState;
    }

    protected void setPreviousState(ModelElementState state) {
        myPrevState = state;
    }

    /**
     * Across replication statistics on the number busy
     *
     * @return
     */
    public final StatisticAccessorIfc getNBAcrossReplicationStatistic() {
        return myNumBusy.getAcrossReplicationStatistic();
    }

    /**
     * Across replication statistics on the utilization
     *
     * @return
     */
    public final StatisticAccessorIfc getUtilAcrossReplicationStatistic() {
        return myUtil.getAcrossReplicationStatistic();
    }
}
