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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;

/**
 * This class allows the creation of a schedule that represents a list of
 * actions with the time between each action specified. The user adds a ScheduleAction
 * or a duration/action pair and can specify when the schedule should start invoking
 * the actions with a start time.  The actions can be repeated after all duration/action
 * pairs have been invoked only if their total duration does not exceed the time
 * remaining until the end of the action schedule's cycle length.
 *
 * Model m = Model.createModel();
 *
 * ActionSchedule s = new ActionSchedule(m);
 *
* s.addScheduledAction(eventAction1, 20, "action 1");
 * s.addScheduledAction(eventAction1, 15, "action 2");
 *
 * After 20 time units, action 1 will be invoked, after another 15 time units action 2 will be invoked. This
 * pattern will be repeated. Thus the duration represents the time until the action is
 * invoked after the previous action (or start of the schedule).
 * <p>
 * The entire schedule has a cycle length. The total of all the durations added must be less than
 * the action schedule's cycle length.  The schedule can be repeated after all actions have been completed.
 * The default length of a schedule's cycle length is positive infinity
 */
public class ActionSchedule extends SchedulingElement {

    /**
     * Indicates whether or not the schedule should be started
     * automatically upon initialization, default is true
     */
    protected boolean myAutomaticStartFlag = true;

    /**
     * Indicates whether or not the actions on the schedule should be repeated
     * after completing all the scheduled actions.  The
     * default is to repeat the actions
     */
    protected boolean myActionRepeatFlag = true;

    /**
     * The time from the beginning of the replication
     * to the time that the schedule is to start
     */
    protected double myInitialStartTime = 0.0;

    /**
     * The time that the schedule started for its current cycle
     */
    protected double myCycleStartTime;

    /**
     * The schedule repeat flag controls whether or not
     * the entire schedule will repeat after its entire duration
     * has elapsed. The default is to repeat the schedule.  The
     * use of this flag only makes sense if a finite schedule length is specified
     */
    protected boolean myScheduleRepeatFlag = true;

    /**
     * Represents the total length of time of the schedule.
     * The total of the durations added to the schedule cannot exceed this amount
     * After this time has elapsed the entire schedule can repeat if the
     * schedule repeat flag is true.  The default is infinite.
     */
    protected double myScheduleLength = Double.POSITIVE_INFINITY;

    /**
     * Keeps track of the total of the durations that have
     * been added to the schedule.
     */
    protected double myDurationTotal = 0.0;

    /**
     * Holds the actions to be invoked by time
     */
    protected List<ScheduledAction> myActions;

    /**
     * Represents the event scheduled to start the schedule
     */
    protected JSLEvent<Object> myStartEvent;

    /**
     * Represents the event for the actions on the schedule
     */
    protected JSLEvent<Object> myActionEvent;

    /**
     * Represents the event for the end of the schedule
     */
    protected JSLEvent<Object> myEndEvent;

    /**
     * An iterator over the actions
     */
    protected Iterator<ScheduledAction> myActionIterator;

    /**
     * The action that is currently scheduled, next to occur
     */
    protected ScheduledAction myNextScheduledAction;

    private final ActionEventHandler myActionEventHandler;

    private final StartEventHandler myStartEventHandler;

    private final EndEventHandler myEndEventHandler;

    /**
     * @param parent the parent model element
     */
    public ActionSchedule(ModelElement parent) {
        this(parent, 0.0, Double.POSITIVE_INFINITY, true, true, true, null);
    }

    /**
     * @param parent the parent model element
     * @param name the name of the ActionSchedule as a model element, must be unique
     */
    public ActionSchedule(ModelElement parent, String name) {
        this(parent, 0.0, Double.POSITIVE_INFINITY, true, true, true, name);
    }

    /**
     * @param parent the parent model element
     * @param scheduleLength The total time available for the schedule
     */
    public ActionSchedule(ModelElement parent, double scheduleLength) {
        this(parent, 0.0, scheduleLength, true, true, true, null);
    }

    /**
     * @param parent the parent model element
     * @param startTime      The time that the schedule should start
     * @param scheduleLength The total time available for the schedule
     */
    public ActionSchedule(ModelElement parent, double startTime, double scheduleLength) {
        this(parent, startTime, scheduleLength, true, true, true, null);
    }

    /**
     * @param parent the parent model element
     * @param scheduleLength The total time available for the schedule
     * @param name the name of the ActionSchedule as a model element, must be unique
     */
    public ActionSchedule(ModelElement parent, double scheduleLength, String name) {
        this(parent, 0.0, scheduleLength, true, true, true, name);
    }

    /**
     * @param parent the parent model element
     * @param startTime      The time that the schedule should start
     * @param scheduleLength The total time available for the schedule
     * @param name the name of the ActionSchedule as a model element, must be unique
     */
    public ActionSchedule(ModelElement parent, double startTime, double scheduleLength, String name) {
        this(parent, startTime, scheduleLength, true, true, true, name);
    }

    /**
     * @param parent the parent model element
     * @param startTime          The time that the schedule should start
     * @param scheduleLength     The total time available for the schedule
     * @param repeatSchedule     Whether or not the schedule will repeat
     * @param repeatActions      Whether or not the actions may repeat
     * @param startAutomatically Whether or not the start of the schedule is scheduled automatically
     * @param name the name of the ActionSchedule as a model element, must be unique
     */
    public ActionSchedule(ModelElement parent, double startTime, double scheduleLength,
                          boolean repeatSchedule, boolean repeatActions, boolean startAutomatically, String name) {
        super(parent, name);
        myActionEventHandler = new ActionEventHandler();
        myStartEventHandler = new StartEventHandler();
        myEndEventHandler = new EndEventHandler();
        setInitialStartTime(startTime);
        setScheduleLength(scheduleLength);
        setScheduleRepeatFlag(repeatSchedule);
        setActionRepeatFlag(repeatActions);
        setAutomaticStartFlag(startAutomatically);
        myActions = new ArrayList<>();
    }

    /**
     * Sets the flag that indicates whether or not the first action will automatically
     * schedule when initialize() is called.
     *
     * @param flag true means start automatically
     */
    public final void setAutomaticStartFlag(boolean flag) {
        myAutomaticStartFlag = flag;
    }

    /**
     * This flag indicates whether or not the action will automatically
     * scheduled when initialize() is called.  By default this option is
     * true.
     *
     * @return true means it starts automatically
     */
    public final boolean getAutomaticStartFlag() {
        return myAutomaticStartFlag;
    }

    /**
     * True means the scheduled actions will repeat after all actions have been invoked. By default the
     * scheduled actions will repeat.
     *
     * @return Returns the repeatActionFlag.
     */
    public final boolean getActionRepeatFlag() {
        return myActionRepeatFlag;
    }

    /**
     * True means the scheduled actions will repeat after all actions have been invoked. By default the
     * scheduled actions will repeat.
     *
     * @param repeatActionFlag The repeatActionFlag to set.
     */
    public final void setActionRepeatFlag(boolean repeatActionFlag) {
        myActionRepeatFlag = repeatActionFlag;
    }

    /**
     * Gets the time after the beginning of the replication that represents
     * the starting time for the schedule
     *
     * @return the initial start time
     */
    public final double getInitialStartTime() {
        return (myInitialStartTime);
    }

    /**
     * Sets the starting time after the beginning of the replication for the
     * schedule to start.
     *
     * @param startTime Must be &gt;= zero
     */
    protected final void setInitialStartTime(double startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("The start time must be >= 0");
        }
        myInitialStartTime = startTime;
    }

    /**
     * Returns the time that the schedule started its current cycle
     *
     * @return the time that the schedule started its current cycle
     */
    public final double getCycleStartTime() {
        return myCycleStartTime;
    }

    /**
     * The time that has elapsed into the current cycle
     *
     * @return time that has elapsed into the current cycle
     */
    public final double getElapsedCycleTime() {
        return getTime() - myCycleStartTime;
    }

    /**
     * The time remaining within the current cycle
     *
     * @return time remaining within the current cycle
     */
    public final double getRemainingCycleTime() {
        return myCycleStartTime + myScheduleLength - getTime();
    }

    /**
     * Sets whether or not the schedule will repeat after it reaches
     * it length
     *
     * @param flag true indicates it will repeat
     */
    public final void setScheduleRepeatFlag(boolean flag) {
        myScheduleRepeatFlag = flag;
    }

    /**
     * Returns whether or not the schedule will repeat after it reaches
     * it length
     *
     * @return true indicates that it will repeat
     */
    public final boolean getScheduleRepeatFlag() {
        return myScheduleRepeatFlag;
    }

    /**
     * Sets the total length of the schedule
     *
     * @param scheduleLength Must be &gt; 0
     */
    protected final void setScheduleLength(double scheduleLength) {
        if (scheduleLength <= 0) {
            throw new IllegalArgumentException("The schedule length must be > 0");
        }
        myScheduleLength = scheduleLength;
    }

    /**
     * Gets the total length of the schedule.  The total length of the
     * schedule must be &gt;= the total of the durations on the schedule
     *
     * @return the total length of the schedule.
     */
    public final double getScheduleLength() {
        return myScheduleLength;
    }

    /**
     * Returns the amount of time left within the schedule cycle length for possible duration/actions
     *
     * @return the amount of time left within the schedule cycle length for possible duration/actions
     */
    public final double getDurationRemainingOnSchedule() {
        return myScheduleLength - myDurationTotal;
    }

    /**
     * Returns how much of the schedule's length has been covered by actions
     *
     * @return how much of the schedule's length has been covered by actions
     */
    public final double getDurationTotal() {
        return myDurationTotal;
    }

    /**
     * The number of actions that have been scheduled
     *
     * @return number of actions that have been scheduled
     */
    public final int getNumberOfActions() {
        return myActions.size();
    }

    /**
     *
     * @param duration the duration from the start of the schedule, must be positive
     * @param eventAction the event action that will be invoked, cannot be null
     */
    public final void addScheduledAction(EventAction eventAction, double duration){
        addScheduledAction(eventAction, duration, null);
    }
    /**
     *
     * @param duration the duration from the start of the schedule, must be positive
     * @param eventAction the event action that will be invoked, cannot be null
     * @param name the name of the scheduled action
      */
    public final void addScheduledAction(EventAction eventAction, double duration, String name){
        if (duration + myDurationTotal > myScheduleLength) {
            throw new IllegalArgumentException("The supplied scheduled action has a duration that overflows the time available on the schedule");
        }
        ScheduledAction action = new ScheduledAction(eventAction, duration, name);
        myActions.add(action);
        myDurationTotal = myDurationTotal + duration;
    }

    /**
     * Schedules the start of the schedule for the start time of the schedule
     * if it has not already be started
     */
    public final void scheduleStart() {
        if (myStartEvent == null) {
            myStartEvent = scheduleEvent(myStartEventHandler, getInitialStartTime());
        }
    }

    @Override
    protected void initialize() {
        if (myAutomaticStartFlag) {
            scheduleStart();
        }
    }

    @Override
    protected void afterReplication() {
        super.afterReplication();
        myStartEvent = null;
        myNextScheduledAction = null;
        myActionIterator = null;
        myActionEvent = null;
        myEndEvent = null;
    }

    protected void scheduleNextAction() {
        myNextScheduledAction = myActionIterator.next();
        rescheduleEvent(myActionEvent, myNextScheduledAction.getDuration());
    }

    private class StartEventHandler extends EventAction {

        @Override
        public void action(JSLEvent<Object> event) {
            myCycleStartTime = getTime();
            // get iterator to actions
            myActionIterator = myActions.iterator();
            if (myActionIterator.hasNext()) {
                //System.out.println(getTime() + "> " + "scheduling the first action");
                // schedule first action
                myNextScheduledAction = myActionIterator.next();
                myActionEvent = scheduleEvent(myActionEventHandler, myNextScheduledAction.getDuration());
            }

            if (myScheduleLength < Double.POSITIVE_INFINITY) {
                myEndEvent = scheduleEvent(myEndEventHandler, myScheduleLength);
            }
        }
    }

    private class ActionEventHandler extends EventAction {

        @Override
        public void action(JSLEvent<Object> event) {
            myNextScheduledAction.action(event);
            if (myActionIterator.hasNext()) {
                scheduleNextAction();
            } else {
                //System.out.println(getTime() + "> " + "all actions have completed within the schedule");
                // all actions have completed within the schedule
                // check if actions should repeat
                if (myActionRepeatFlag == true) {
                    // actions are allowed to repeat only if
                    // all the actions can complete before the end of the current cycle
                    if (myDurationTotal < getRemainingCycleTime()) {
                        //System.out.println(getTime() + "> " + "actions are repeating");
                        myActionIterator = myActions.iterator();
                        if (myActionIterator.hasNext()) {
                            scheduleNextAction();
                        }
                    }
                }
            }
        }
    }


    private class EndEventHandler extends EventAction {

        @Override
        public void action(JSLEvent event) {
            if (myScheduleRepeatFlag == true) {
                //System.out.println(getTime() + "> " + "Schedule is repeating...");
                myCycleStartTime = getTime();
                // get iterator to actions
                myActionIterator = myActions.iterator();
                if (myActionIterator.hasNext()) {
                    // schedule first action
                    myNextScheduledAction = myActionIterator.next();
                    myActionEvent = scheduleEvent(myActionEventHandler, myNextScheduledAction.getDuration());
                }

                if (myScheduleLength < Double.POSITIVE_INFINITY) {
                    myEndEvent = scheduleEvent(myEndEventHandler, myScheduleLength);
                }
            }
        }
    }

}
