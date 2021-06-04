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

import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import jsl.utilities.statistic.Statistic;

/** ModelElementState models a state that can be entered and exited with
 *  statistics tabulated.  It represents a "permanent" state as it is
 *  part of the model element hierarchy.
 *
 */
public class ModelElementState extends ModelElement implements StateAccessorIfc {

    protected State myState;

    protected SortedSet<StateEnteredListenerIfc> myStateEnteredListeners;

    public ModelElementState(ModelElement parent) {
        this(parent, null, false);
    }

    public ModelElementState(ModelElement parent, String name) {
        this(parent, name, false);
    }

    public ModelElementState(ModelElement parent, String name, boolean useStatistic) {
        super(parent, name);

        myState = new State(name, useStatistic);
    }

    public void attachStateEnteredListener(StateEnteredListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null");
        }

        if (myStateEnteredListeners == null) {
            myStateEnteredListeners = new TreeSet<StateEnteredListenerIfc>();
        }
        myStateEnteredListeners.add(listener);
    }

    public void detachStateEnteredListener(StateEnteredListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null");
        }
        if (myStateEnteredListeners == null) {
            return;
        }
        myStateEnteredListeners.remove(listener);
    }

    protected void notifyStateEnteredListeners(){
        if (myStateEnteredListeners == null)
            return;
        for(StateEnteredListenerIfc listener: myStateEnteredListeners){
            listener.update(this);
        }
    }

    /** Allows the accumulated state information to be accessed
     *
     * @return the StateAccessorIfc
     */
    public final StateAccessorIfc getStateAccessor() {
        return (this);
    }

    /** Causes the state to be entered at the
     *  current simulation time
     *
     */
    public final void enter() {
        enter(getTime());
    }

    /** Causes the state to be entered
     *  with the time entered set to the supplied value
     * @param time  the time entered
     */
    public final void enter(double time) {
        onEnter();
        myState.enter(time);
        notifyStateEnteredListeners();
    }

    /** Causes the state to be exited at the
     *  current simulation time
     *
     * @return the time of exit
     */
    public final double exit() {
        return exit(getTime());
    }

    /** Causes the state to be exited
     *  with the time exited recorded as the supplied
     *  time
     * 
     *  @param time the time exited
     * @return the time spent in the  state as a double
     */
    public final double exit(double time) {
        onExit();
        return (myState.exit(time));
    }

    /** Indicates whether or not statistics should be collected on the
     *  sojourn times within the state
     *
     * @return Returns the collect sojourn time flag.
     */
    public final boolean getSojournTimeCollectionFlag() {
        return myState.getSojournTimeCollectionFlag();
    }

    /** Turns on statistical collection for the sojourn time in the state
     */
    public final void turnOnSojournTimeCollection() {
        myState.turnOnSojournTimeCollection();
    }

    /** Turns off statistical collection of the sojourn times in the state
     */
    public final void turnOffSojournTimeCollection() {
        myState.turnOffSojournTimeCollection();
    }

    /** Resets the statistics collected on the sojourn time in the state
     */
    public final void resetSojournTimeStatistics() {
        myState.resetSojournTimeStatistics();
    }

    /** Resets the counters for the number of times a state
     *  was entered, exited, and the total time spent in the state
     */
    public final void resetStateCollection() {
        myState.resetStateCollection();
    }

    @Override
    public final double getNumberOfTimesEntered() {
        return myState.getNumberOfTimesEntered();
    }

    @Override
    public final double getNumberOfTimesExited() {
        return myState.getNumberOfTimesExited();
    }

    @Override
    public final Optional<Statistic> getSojournTimeStatistic() {
        return myState.getSojournTimeStatistic();
    }

    @Override
    public final double getTimeStateEntered() {
        return myState.getTimeStateEntered();
    }

    @Override
    public final double getTimeStateExited() {
        return myState.getTimeStateExited();
    }

    @Override
    public final double getTotalTimeInState() {
        return myState.getTotalTimeInState();
    }

    @Override
    public final boolean isEntered() {
        return myState.isEntered();
    }

    @Override
    protected void initialize() {
        myState.initialize();
    }

    @Override
    protected void warmUp() {
        super.warmUp(); 
        resetStateCollection();
        resetSojournTimeStatistics();
    }

    /** can be overwritten by subclasses to
     *  perform work when the state is entered
     */
    protected void onEnter() {
    }

    /** can be overwritten by subclasses to
     *  perform work when the state is exited
     */
    protected void onExit() {
    }
}
