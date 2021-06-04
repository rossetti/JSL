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
package jsl.simulation;

import jsl.utilities.statistic.*;
import jsl.utilities.*;

import java.util.Optional;

/**
 */
public class State implements IdentityIfc, StateAccessorIfc {

    /**
     * incremented to give a running total of the
     * number of states created
     */
    private static int myCounter_;

    /**
     * The id of the state, currently if
     * the state is the ith state created
     * then the id is equal to i
     */
    private final int myId;

    /**
     * A user defined integer label for the state
     */
    private final int myNumber;

    /**
     * The name of the state
     */
    protected String myName;

    /**
     * indicates whether or not currently in the state
     */
    protected boolean myInStateIndicator;

    /**
     * number of times the state was entered
     */
    protected double myNumTimesEntered;

    /**
     * number of times the state was exited
     */
    protected double myNumTimesExited;

    /**
     * time the state was last entered
     */
    protected double myEnteredTime;

    /**
     * time that the state was entered for the first time
     */
    protected double myTimeFirstEntered;

    /**
     * time the state was last exited
     */
    protected double myExitedTime;

    /**
     * Total time spent in state
     */
    protected double myTotalStateTime;

    /**
     * statistical collector
     */
    protected Statistic myStatistic;

    /**
     * Indicates whether or not statistics should be collected on
     * time spent in the state. The default is false
     */
    protected boolean myCollectSojournStatisticsFlag = false;

    public State() {
        this(null, myCounter_ + 1, false);
    }

    /**
     * Create a state with no name and
     * do not use a Statistic object to
     * collect additional statistics
     */
    public State(int number) {
        this(null, number, false);
    }

    /**
     * Create a state with given name and
     * do not use a Statistic object to
     * collect additional statistics
     *
     * @param name The name of the state
     */
    public State(String name, int number) {
        this(name, number, false);
    }

    public State(String name) {
        this(name, myCounter_ + 1, false);
    }

    public State(String name, boolean useStatistic) {
        this(name, myCounter_ + 1, useStatistic);
    }

    /**
     * Create a state with no name
     *
     * @param useStatistic True means collect additional statistics
     */
    public State(int number, boolean useStatistic) {
        this(null, number, useStatistic);
    }

    /**
     * Create a state with given name and
     * indicate usage of a Statistic object to
     * collect additional statistics
     *
     * @param name         The name of the state
     * @param number       a number assigned to the state for labeling purposes
     * @param useStatistic True means collect sojourn time statistics
     */
    public State(String name, int number, boolean useStatistic) {
        myCounter_ = myCounter_ + 1;
        myId = myCounter_;
        myNumber = number;
        setName(name);

        if (useStatistic) {
            turnOnSojournTimeCollection();
        }

        initialize();
    }

    /**
     * @return the number assigned to the state, by default getId() if never assigned
     */
    public final int getNumber() {
        return myNumber;
    }

    /**
     * Sets the name of this state
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            myName = s+":"+getNumber();
        } else {
            myName = str;
        }
    }

    /**
     * Gets this model element's name.
     *
     * @return The name of the model element.
     */
    @Override
    public final String getName() {
        return myName;
    }

    /**
     * Gets a uniquely assigned integer identifier for this state.
     * This identifier is assigned when the state is
     * created.  It may vary if the order of creation changes.
     *
     * @return The identifier for the state.
     */
    @Override
    public final int getId() {
        return (myId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("State{");
        sb.append("id=").append(myId);
        sb.append(", number=").append(myNumber);
        sb.append(", name='").append(myName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * Gets whether or not the state has been entered
     *
     * @return True means that the state has been entered
     */
    @Override
    public final boolean isEntered() {
        return myInStateIndicator;
    }

    /**
     * Causes the state to be entered
     * If the state has already been entered then nothing happens.
     * Preconditions: time must be &gt;= 0, must not be Double.NaN and must not
     * be Double.Infinity
     *
     * @param time The time that the state is being entered
     */
    public final void enter(double time) {
        if (Double.isNaN(time)) {
            throw new IllegalArgumentException("The supplied time was Double.NaN");
        }
        if (Double.isInfinite(time)) {
            throw new IllegalArgumentException("The supplied time was Double.Infinity");
        }
        if (time < 0.0) {
            throw new IllegalArgumentException("The supplied time was less than 0.0");
        }
        if (myInStateIndicator == true) {
            return;
        }

        myNumTimesEntered = myNumTimesEntered + 1.0;
        if (myNumTimesEntered == 1) {
            myTimeFirstEntered = time;
        }
        myEnteredTime = time;
        myInStateIndicator = true;
        onEnter();
    }

    /**
     * can be overwritten by subclasses to
     * perform work when the state is entered
     */
    protected void onEnter() {
    }

    /**
     * Causes the state to be exited
     *
     * @param time the time that the state was exited, must
     *             be &gt;= time entered, &gt;= 0, not Double.NaN not Double.Infinity
     * @return the time spent in the  state as a double
     */
    public final double exit(double time) {
        if (Double.isNaN(time)) {
            throw new IllegalArgumentException("The supplied time was Double.NaN");
        }
        if (Double.isInfinite(time)) {
            throw new IllegalArgumentException("The supplied time was Double.Infinity");
        }
        if (time < 0.0) {
            throw new IllegalArgumentException("The supplied time was less than 0.0");
        }

        if (time < myEnteredTime) {
            throw new IllegalArgumentException("The exit time = " + time + " was < enter time = " + myEnteredTime);
        }

        if (myInStateIndicator == false) {
            throw new IllegalStateException("Attempted to exit a state when not in the state:" + this);
        }

        myNumTimesExited = myNumTimesExited + 1.0;
        myExitedTime = time;
        double timeInState = myExitedTime - myEnteredTime;
        myTotalStateTime = myTotalStateTime + timeInState;
        myInStateIndicator = false;
        if (myCollectSojournStatisticsFlag == true) {
            myStatistic.collect(timeInState);
        }
        onExit();
        return (timeInState);
    }

    /**
     * can be overwritten by subclasses to
     * perform work when the state is exited
     */
    protected void onExit() {
    }

    /**
     * Initializes the state back to new
     * - not in state
     * - enter/exited time/time first entered = Double.NaN
     * - total time in state = 0.0
     * - enter/exited count = 0.0
     * - sojourn statistics reset if turned on
     */
    public final void initialize() {
        myInStateIndicator = false;
        myEnteredTime = Double.NaN;
        myExitedTime = Double.NaN;
        myTimeFirstEntered = Double.NaN;
        myTotalStateTime = 0.0;
        myNumTimesEntered = 0.0;
        myNumTimesExited = 0.0;
        if (myStatistic != null) {
            myStatistic.reset();
        }
    }

    /**
     * Indicates whether or not statistics should be collected on the
     * sojourn times within the state
     *
     * @return Returns the collect sojourn time flag.
     */
    public final boolean getSojournTimeCollectionFlag() {
        return myCollectSojournStatisticsFlag;
    }

    /**
     * Turns on statistical collection for the sojourn time in the state
     */
    public final void turnOnSojournTimeCollection() {
        myCollectSojournStatisticsFlag = true;
        if (myStatistic == null) {
            myStatistic = new Statistic(getName());
        }
    }

    /**
     * Turns off statistical collection of the sojourn times in the state
     */
    public final void turnOffSojournTimeCollection() {
        myCollectSojournStatisticsFlag = false;
    }

    /**
     * Resets the statistics collected on the sojourn time in the state
     */
    public final void resetSojournTimeStatistics() {
        if (myStatistic != null) {
            myStatistic.reset();
        }
    }

    /**
     * Resets the counters for the number of times a state
     * was entered, exited, and the total time spent in the state
     * This does not effect whether or the state has been entered,
     * the time it was last entered, or the time it was last exited.
     * To reset those quantities and the state counters use initialize()
     */
    public final void resetStateCollection() {
        myNumTimesEntered = 0.0;
        myNumTimesExited = 0.0;
        myTotalStateTime = 0.0;
    }

    /**
     * @return The time that the state was first entered
     */
    public final double getTimeFirstEntered() {
        return myTimeFirstEntered;
    }

    /**
     * Gets the time that the state was last entered
     *
     * @return A double representing the time that the state was last entered
     */
    @Override
    public final double getTimeStateEntered() {
        return myEnteredTime;
    }

    /**
     * Gets the time that the state was last exited
     *
     * @return A double representing the time that the state was last exited
     */
    @Override
    public final double getTimeStateExited() {
        return myExitedTime;
    }

    /**
     * Gets the number of times the state was entered
     *
     * @return A double representing the number of times entered
     */
    @Override
    public final double getNumberOfTimesEntered() {
        return myNumTimesEntered;
    }

    /**
     * Gets the number of times the state was exited
     *
     * @return A double representing the number of times exited
     */
    @Override
    public final double getNumberOfTimesExited() {
        return myNumTimesExited;
    }

    /**
     * Gets a statistic that collected sojourn times
     *
     * @return A statistic for sojourn times or null if
     * the statistics were never turned on
     */
    @Override
    public final Optional<Statistic> getSojournTimeStatistic() {
        return Optional.ofNullable(myStatistic);
    }

    /**
     * Gets the total time spent in the state
     *
     * @return a double representing the total sojourn time
     */
    @Override
    public final double getTotalTimeInState() {
        return myTotalStateTime;
    }
}
