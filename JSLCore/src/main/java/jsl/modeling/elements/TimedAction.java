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
package jsl.modeling.elements;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/** A TimedAction represents a set of actions associated with an event that
 *  can be repeated at regular intervals.  Each TimedAction
 *  can have any number of TimedActionListeners attached
 *  that represent the actions that should occur when the
 *  event occurs.  The attached listeners are called in the
 *  order defined by their priority.
 *
 *  By default, the event will be automatically scheduled
 *  to occur at the start of each replication.  By default
 *  the event will automatically repeat according to the provided
 *  interval.
 *
 */
public class TimedAction extends SchedulingElement {

    /** incremented to give a running total of the
     *  number of listeners attached
     */
    private static int myCounter_;

    private boolean myScheduleOnInitFlag;

    private boolean myRepeatActionFlag;

    private RandomVariable myActionTimeRV;

    private List<TimedActionListener> myTimedActionListeners;

    private JSLEvent myTimedActionEvent;

    private final EventHandler myEventHandler;

    /**
     * @param parent the parent model element
     */
    public TimedAction(ModelElement parent) {
        this(parent, ConstantRV.POSITIVE_INFINITY, true, true, null);
    }

    /**
     * @param parent the parent model element
     * @param time the time between actions
     */
    public TimedAction(ModelElement parent, double time) {
        this(parent, new ConstantRV(time), true, true, null);
    }

    /**
     * @param parent the parent model element
     * @param time the time between actions
     *  @param name the name of the model element
     */
    public TimedAction(ModelElement parent, double time, String name) {
        this(parent, new ConstantRV(time), true, true, name);
    }

    /**
     * @param parent the parent model element
     * @param actionTime the time between actions
     */
    public TimedAction(ModelElement parent, RandomIfc actionTime) {
        this(parent, actionTime, true, true, null);
    }

    /** Creates a Timed Action
     *
     * @param parent the parent model element
     * @param actionTime the time between actions
     * @param schedInitFlag Indicates whether to schedule initial action at
     * initialization
     * @param repeatActionFlag Indicates whether the actions will automatically
     * repeat after their first occurrence
     * @param name the name of the model element
     */
    public TimedAction(ModelElement parent, RandomIfc actionTime,
            boolean schedInitFlag, boolean repeatActionFlag, String name) {
        super(parent, name);
        myEventHandler = new EventHandler();
        myScheduleOnInitFlag = schedInitFlag;
        myRepeatActionFlag = repeatActionFlag;
        setTimeBetweenActionsInitialRandomSource(actionTime);
        myTimedActionListeners = new LinkedList<TimedActionListener>();
    }

    /**
     * @return Returns the actionTime.
     */
    public final RandomIfc getTimeBetweenActions() {
        return myActionTimeRV.getRandomSource();
    }

    /**
     * @return Returns the actionTime.
     */
    public final RandomIfc getTimeBetweenActionsForReplications() {
        return myActionTimeRV.getInitialRandomSource();
    }

    /** Sets the time between actions to be a constant time
     *  for all replications
     * 
     * @param time the initial time between actions
     */
    public final void setTimeBetweenActionsInitialRandomSource(double time) {
        setTimeBetweenActionsInitialRandomSource(new ConstantRV(time));
    }

    /** Sets the time between actions for all replications
     *
     * @param actionTime The actionTime to set.
     */
    public final void setTimeBetweenActionsInitialRandomSource(RandomIfc actionTime) {
        if (actionTime == null) {
            throw new IllegalArgumentException("The action time must not be null.");
        }

        if (myActionTimeRV == null) {
            myActionTimeRV = new RandomVariable(this, actionTime);
        } else {
            myActionTimeRV.setInitialRandomSource(actionTime);
        }

    }

    /** Sets the time between actions to be a constant time
     *  for the current replication
     *
     * @param time the time between actions
     */
    public final void setTimeBetweenActions(double time) {
        setTimeBetweenActions(new ConstantRV(time));
    }

    /** Sets the time between actions for the current replication
     *
     * @param actionTime The actionTime to set.
     */
    public final void setTimeBetweenActions(RandomIfc actionTime) {
        if (actionTime == null) {
            throw new IllegalArgumentException("The action time must not be null.");
        }

        if (myActionTimeRV == null) {
            myActionTimeRV = new RandomVariable(this, actionTime);
        } else {
            myActionTimeRV.setRandomSource(actionTime);
        }

    }

    /** This flag indicates whether or not the action will automatically
     *  be scheduled when initialize() is called.
     *  By default this option is false.
     *
     * @return true means that it is automatic
     */
    public boolean getScheduleOnInitializeFlag() {
        return myScheduleOnInitFlag;
    }

    /** Sets the flag that indicates whether or not the action will 
     *  be automatically scheduled when initialize() is called.
     *  WARNING:  If this is changed, it affects the next
     *  replication to be initialized() and all subsequent replications.
     *  Thus, the initialization of replications may not be the
     *  same.  The recommended use is prior to any replications
     *  being executed.
     *
     * @param flag true means it will be scheduled
     */
    public void setScheduleOnInitializeFlag(boolean flag) {
        myScheduleOnInitFlag = flag;
    }

    /** Causes the event associated with the actions to be canceled
     *  if it had been scheduled.  The next scheduled action will
     *  not occur.
     *
     */
    public final void cancelAction() {
        if (myTimedActionEvent != null) {
            cancelEvent(myTimedActionEvent);
        }
    }

    /** True means the action will repeat.  By default the
     *  action will repeat.
     * @return Returns the repeatActionFlag.
     */
    public final boolean getRepeatActionFlag() {
        return myRepeatActionFlag;
    }

    /** True means the action will repeat. By default the
     *  action will repeat.
     *
     *  WARNING:  If this is changed, this
     *  controls whether the action will be repeated after
     *  the next event occurs. The actions will not be repeated
     *  for the rest of the replication and for any subsequent
     *  replications. Thus, replications may not be the
     *  same.  The recommended use is prior to any replications
     *  being executed.
     *
     * @param repeatActionFlag The repeatActionFlag to set.
     */
    public final void setRepeatActionFlag(boolean repeatActionFlag) {
        myRepeatActionFlag = repeatActionFlag;
    }

    /** Adds a listener to react to the action event
     *  Listeners are ordered by their compareTo() method
     *
     * @param listener the timed action listener
     */
    protected void attachTimedActionListener(TimedActionListener listener) {

        myCounter_ = myCounter_ + 1;
        listener.setId(myCounter_);

        // nothing in list, just add it, and return
        if (myTimedActionListeners.isEmpty()) {
            myTimedActionListeners.add(listener);
            return;
        }

        if (myTimedActionListeners.contains(listener)) {
            return;
        }

        // might as well check for worse case, if larger than the largest
        // then put it at the end and return
        if (listener.compareTo(myTimedActionListeners.get(myTimedActionListeners.size() - 1)) >= 0) {
            myTimedActionListeners.add(listener);
            return;
        }

        for (ListIterator<TimedActionListener> i = myTimedActionListeners.listIterator(); i.hasNext();) {
            if (listener.compareTo(i.next()) < 0) {
                // next() move the iterator forward
                // if it is less than what was returned by next(), then it
                // must be inserted at the previous index
                myTimedActionListeners.add(i.previousIndex(), listener);
                return;
            }
        }
    }

    /** Removes the listener so that it does not react to the action event
     * @param listener, the listener to be removed
     * @return  true if it was removed
     */
    public boolean removeTimedActionListener(TimedActionListener listener) {
        return myTimedActionListeners.remove(listener);
    }

    /** If the action has not been previously scheduled this
     *  method causes the action to be scheduled according to
     *  it's action time distribution. If the action has
     *  been canceled, then the action is scheduled for
     *  a new time according to it's action time. If the
     *  action is already scheduled and not canceled,
     *  this method has no effect.
     *
     */
    public final void scheduleAction() {
        if (myTimedActionEvent == null) {
            double t = myActionTimeRV.getValue();
            if (Double.isInfinite(t)) {
                return;
            }
            myTimedActionEvent = scheduleEvent(myEventHandler, t);
        } else {
            if (myTimedActionEvent.getCanceledFlag()) {
                // make a new event
                double t = myActionTimeRV.getValue();
                if (Double.isInfinite(t)) {
                    return;
                }
                myTimedActionEvent = scheduleEvent(myEventHandler, t);
            }
        }
    }

    @Override
    protected void initialize() {
        if (myScheduleOnInitFlag) {
            scheduleAction();
        }
    }

    @Override
    protected void afterReplication() {
        super.afterReplication();
        myTimedActionEvent = null;
    }

    protected final void notifyTimedActionListeners(JSLEvent event) {
        for (TimedActionListener a : myTimedActionListeners) {
            if (a.getCallActionFlag()) {
                a.action(event);
            }
        }
    }

    private class EventHandler extends EventAction {

        @Override
        public void action(JSLEvent<Object> event) {
            notifyTimedActionListeners(event);
            if (myRepeatActionFlag == true) {
                rescheduleEvent(event, myActionTimeRV);
            } else {
                myTimedActionEvent = null;
            }
        }
    }
}
