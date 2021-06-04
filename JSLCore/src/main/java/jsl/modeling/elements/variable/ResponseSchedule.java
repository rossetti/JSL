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

import java.lang.IllegalStateException;
import java.util.*;

import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;

/**
 * This class allows the creation of a schedule that represents a list of
 * intervals of time. The starting length of a schedule is
 * 0.0. The length of a schedule depends upon the intervals added to it.
 * The schedule's length encompasses the furthest interval added. If no
 * intervals are added, then the schedule only has its start time and no
 * response collection will occur.
 * <p>
 * The user adds intervals and responses for which statistics need to be collected during the intervals.
 * The intervals within the cycle may overlap in time. The start time
 * of an interval is specified relative to the beginning of the cycle.
 * The length of any interval must be finite.
 * <p>
 * The schedule can be started any time after the start of the simulation.
 * The default starting time of the schedule is time 0.0.
 * The schedule will start automatically at the designated
 * start time.
 * <p>
 * The schedule can be repeated after the cycle length of the schedule is
 * reached. The default is for the schedule to automatically repeat.
 * Note that depending on the simulation run length only a portion of the
 * scheduled intervals may be executed.
 * <p>
 * The classic use case of this class is to collect statistics for each hour of the day.
 * In this case, the user would use the addIntervals() method to add 24 intervals of 1 hour duration.
 * Then responses (response variables, time weighted variables, and counters) can be added
 * to the schedule. In which case, they will be added to each interval. Thus, interval statistics
 * for each of the 24 intervals will be collected for everyone of the added responses.  If more
 * than one day is simulated and the schedule is allowed to repeat, then statistics are collected
 * across the days.  That is, the statistics of hour 1 on day 1 are averaged with the
 * statistics of hour 1 on all subsequent days.
 * <p>
 *  This functionality is built on the ResponseInterval class, which can be used separately. In
 *  other words, response intervals do not have to be on a schedule. The schedule facilitates
 *  the collection of many responses across many intervals.
 * </p>
 *
 */
public class ResponseSchedule extends SchedulingElement {

    /**
     * Need to ensure that start event happens before interval responses
     */
    //public final int START_EVENT_PRIORITY = 1;
    public final int START_EVENT_PRIORITY = JSLEvent.DEFAULT_WARMUP_EVENT_PRIORITY - 1;
    /**
     * The time that the schedule should start
     */
    protected double myStartTime;

    /**
     * The time that the schedule started for its current cycle
     */
    protected double myCycleStartTime;

    /**
     * The schedule repeat flag controls whether or not the entire schedule will
     * repeat after its entire cycle has elapsed.  The default is
     * true.
     */
    protected boolean myScheduleRepeatFlag = true;

    /**
     * Represents the length of time of the schedule based on the intervals
     * added
     */
    protected double myLength;

    /**
     * Holds the intervals to be invoked on schedule
     */
    protected final List<ResponseScheduleItem> myScheduleItems;

    /**
     * Holds the set of intervals that have been scheduled
     */
    protected final Set<ResponseInterval> myScheduledIntervals;

    /**
     * Represents the event scheduled to start the schedule
     */
    protected JSLEvent myStartEvent;

    protected final StartScheduleAction myStartAction;

    /**
     * Indicates if the schedule has been scheduled to start
     */
    protected boolean myScheduledFlag;

    /**
     * @param parent the parent model element
     */
    public ResponseSchedule(ModelElement parent) {
        this(parent, 0.0, true, null);
    }

    /**
     * @param parent the parent model element
     * @param name   the name of the model element
     */
    public ResponseSchedule(ModelElement parent, String name) {
        this(parent, 0.0, true, name);
    }

    /**
     * @param parent    the parent model element
     * @param startTime the time to start the schedule, must be finite.  If negative, it will never occur.
     */
    public ResponseSchedule(ModelElement parent, double startTime) {
        this(parent, startTime, true, null);
    }

    /**
     * @param parent         the parent model element
     * @param startTime      the time to start the schedule, must be finite.  If negative, it will never occur.
     * @param repeatSchedule Whether or not the schedule will repeat
     */
    public ResponseSchedule(ModelElement parent, double startTime, boolean repeatSchedule) {
        this(parent, startTime, repeatSchedule, null);
    }

    /**
     * @param parent    the parent model element
     * @param startTime the time to start the schedule, must be finite.  If negative, it will never occur.
     * @param name      the name of the model element
     */
    public ResponseSchedule(ModelElement parent, double startTime, String name) {
        this(parent, startTime, true, name);
    }

    /**
     * @param parent         the parent model element
     * @param startTime      the time to start the schedule, must be finite.  If negative, it will never occur.
     * @param repeatSchedule Whether or not the schedule will repeat
     * @param name           the name of the model element
     */
    public ResponseSchedule(ModelElement parent, double startTime, boolean repeatSchedule,
                            String name) {
        super(parent, name);
        if (Double.isInfinite(startTime)) {
            throw new IllegalArgumentException("The start time cannot be infinity");
        }
        myLength = 0.0;
        myScheduledFlag = false;
        setScheduleRepeatFlag(repeatSchedule);
        myStartAction = new StartScheduleAction();
        myScheduleItems = new ArrayList<>();
        myScheduledIntervals = new HashSet<>();
        myStartTime = startTime;
        myCycleStartTime = Double.NaN;
    }

    /**
     * @return the time to start the schedule
     */
    public final double getStartTime() {
        return myStartTime;
    }

    /**
     * @return true if the schedule has been started
     */
    public final boolean isScheduled() {
        return myScheduledFlag;
    }


    public final void cancelScheduleStart() {
        if (isScheduled()) {
            myStartEvent.setCanceledFlag(true);
        }
    }

    /**
     * Returns the time that the schedule started its current cycle
     *
     * @return the cycle start time
     */
    public final double getCycleStartTime() {
        return myCycleStartTime;
    }

    /**
     * The time that has elapsed into the current cycle
     *
     * @return the time within the cycle
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
        return myCycleStartTime + myLength - getTime();
    }

    /**
     * Sets whether or not the schedule will repeat after it reaches it length
     *
     * @param flag true means repeats
     */
    public final void setScheduleRepeatFlag(boolean flag) {
        myScheduleRepeatFlag = flag;
    }

    /**
     * Returns whether or not the schedule will repeat after it reaches it
     * length
     *
     * @return true means it repeats
     */
    public final boolean getScheduleRepeatFlag() {
        return myScheduleRepeatFlag;
    }

    /**
     * Gets the total length of the schedule.
     *
     * @return the length
     */
    public final double getLength() {
        return myLength;
    }

    /**
     * The number of intervals in the schedule
     *
     * @return number of interval in the schedule
     */
    public final int getNumberOfIntervals() {
        return myScheduleItems.size();
    }

    /**
     * An unmodifiable list of the ResponseScheduleItems
     *
     * @return An unmodifiable list of the ResponseScheduleItems
     */
    public final List<ResponseScheduleItem> getResponseScheduleItems() {
        return Collections.unmodifiableList(myScheduleItems);
    }

    /**
     * Causes interval statistics to be collected for the response for every
     * interval in the schedule
     *
     * @param response the response to add
     */
    public final void addResponseToAllIntervals(ResponseVariable response) {
        Objects.requireNonNull(response, "The response must not be null");
        for (ResponseScheduleItem item : myScheduleItems) {
            item.getResponseInterval().addResponseToInterval(response);
        }
    }

    /**
     * There must not be any duplicates in the collection or null values. Causes
     * interval statistics to be collected for all the responses for every
     * interval in the schedule.
     *
     * @param responses a collection of unique ResponseVariable instances
     */
    public final void addResponsesToAllIntervals(Collection<ResponseVariable> responses) {
        Objects.requireNonNull(responses, "The collection must not be null");
        for (ResponseVariable c : responses) {
            addResponseToAllIntervals(c);
        }
    }

    /**
     * Causes interval statistics to be collected for the counter for every
     * interval in the schedule
     *
     * @param counter the counter to add
     */
    public final void addCounterToAllIntervals(Counter counter) {
        Objects.requireNonNull(counter, "The counter must not be null");
        for (ResponseScheduleItem item : myScheduleItems) {
            item.getResponseInterval().addCounterToInterval(counter);
        }
    }

    /**
     * There must not be any duplicates in the collection or null values.
     * Causes interval statistics to be collected for all the counters for every
     * interval in the schedule.
     *
     * @param counters a collection of unique Counter instances
     */
    public final void addCountersToAllIntervals(Collection<Counter> counters) {
        Objects.requireNonNull(counters, "The collection must not be null");
        for (Counter c : counters) {
            addCounterToAllIntervals(c);
        }
    }

    /**
     * Add an interval for collecting responses to the schedule.  If the start time plus the
     * duration reaches past the current schedule length, the schedule length is extended to
     * include the interval.
     *
     * @param startTime must be greater than or equal to zero. Represents start time relative to start of schedule
     * @param label     the label associated with the interval, must not be null
     * @param duration  duration of the interval, must be finite and strictly positive
     * @return the ResponseScheduleItem
     */
    public final ResponseScheduleItem addResponseInterval(double startTime,
                                                          double duration, String label) {
        int n = myScheduleItems.size() + 1;
        label = String.format("Interval:%02d",n) + ":" + label;
        ResponseScheduleItem item = new ResponseScheduleItem(this, startTime, duration,
                getName() + ":" + label);
        if (startTime + item.getDuration() > getLength()) {
            myLength = startTime + item.getDuration();
        }
        myScheduleItems.add(item);
        return item;
    }

    /**
     * Add non-overlapping, sequential intervals to the schedule, each having
     * the provided duration, starting 0 time units after the schedule starts
     *
     * @param numIntervals the number of intervals
     * @param duration     the duration of each interval
     * @param label        a base label for each interval, if null a label is created
     */
    public final void addIntervals(int numIntervals, double duration, String label) {
        addIntervals(0.0, numIntervals, duration, label);
    }

    /**
     * Add non-overlapping, sequential intervals to the schedule, each having
     * the provided duration, starting 0 time units after the schedule starts
     *
     * @param numIntervals the number of intervals
     * @param duration     the duration of each interval
     */
    public final void addIntervals(int numIntervals, double duration) {
        addIntervals(0.0, numIntervals, duration, null);
    }

    /**
     * Add non-overlapping, sequential intervals to the schedule, each having
     * the provided duration
     *
     * @param startTime    must not be negative. Represents start time of first interval
     *                     relative to the start time of the schedule
     * @param numIntervals the number of intervals
     * @param duration     the duration of each interval
     */
    public final void addIntervals(double startTime, int numIntervals, double duration) {
        addIntervals(startTime, numIntervals, duration, null);
    }

    /**
     * Add non-overlapping, sequential intervals to the schedule, each having
     * the provided duration
     *
     * @param startTime    must not be negative. Represents start time of first interval
     *                     relative to the start time of the schedule
     * @param numIntervals the number of intervals
     * @param duration     the duration of each interval
     * @param label        a base label for each interval, if null a label is created
     */
    public final void addIntervals(double startTime, int numIntervals, double duration,
                                   String label) {
        if (startTime < 0) {
            throw new IllegalArgumentException("The start time must be >= 0");
        }
        if (numIntervals < 1) {
            throw new IllegalArgumentException("The number of intervals must be >=1");
        }
        double t = startTime;
        String s;
        for (int i = 1; i <= numIntervals; i++) {
            if (label == null) {
                double t1 = t + getStartTime();
                s = String.format("[%.1f,%.1f]", t1, t1 + duration);
            } else {
                s = label + ":" + i;
            }
            addResponseInterval(t, duration, s);
            t = t + duration;
        }
    }

    /**
     * Includes the model name, the id, the model element name, the parent name, and parent id
     *
     * @return a string representing the model element
     */
    @Override
    public String toString() {
        return asString();
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(System.lineSeparator());
        sb.append("Repeats: ");
        sb.append(getScheduleRepeatFlag());
        sb.append(System.lineSeparator());
        sb.append("Start time: ");
        sb.append(getStartTime());
        sb.append(System.lineSeparator());
        sb.append("Length: ");
        sb.append(getLength());
        sb.append(System.lineSeparator());
        sb.append("#Intervals: ");
        sb.append(getNumberOfIntervals());
        sb.append(System.lineSeparator());
        sb.append("------");
        sb.append(System.lineSeparator());
        int i = 1;
        for (ResponseScheduleItem item : myScheduleItems) {
            sb.append("Item: ");
            sb.append(i);
            sb.append(System.lineSeparator());
            sb.append(item);
            sb.append(System.lineSeparator());
            i++;
        }
        sb.append("------");
        return sb.toString();
    }

    /**
     * Schedules the start of the schedule for t + time the schedule if it has
     * not already be started. This method will do nothing if the event scheduling
     * executive is not available.
     *
     * @param timeToStart the time to start
     */
    private final void scheduleStart(double timeToStart) {
        if (getExecutive() != null) {
            if (isScheduled()) {
                throw new IllegalStateException("The schedule as already been scheduled to start");
            }
            myScheduledFlag = true;
            myStartEvent = scheduleEvent(myStartAction, timeToStart, START_EVENT_PRIORITY);
        }
    }

    @Override
    protected void initialize() {
        if (getStartTime() >= 0.0) {
            myCycleStartTime = Double.NaN;
            scheduleStart(getStartTime());
        }
    }

    @Override
    protected void afterReplication() {
        super.afterReplication();
        myScheduledFlag = false;
        myStartEvent = null;
        myScheduledIntervals.clear();
    }

    /**
     * Used to communicate that the response interval ended
     *
     * @param responseInterval the interval that ended
     */
    protected void responseIntervalEnded(ResponseInterval responseInterval) {
        // cancel the interval so that it can be used again
        responseInterval.cancelInterval();
        myScheduledIntervals.remove(responseInterval);
        if (getScheduleRepeatFlag() == true) {
            if (myScheduledIntervals.isEmpty()) {
                // all response intervals have completed, safe to start again
                myScheduledFlag = false;
                scheduleStart(0.0);//zero is time NOW
            }
        }
    }

    protected class StartScheduleAction extends EventAction {

        @Override
        public void action(JSLEvent evt) {
            //System.out.println(getTime() + " > starting the schedule");
            myCycleStartTime = getTime();
            for (ResponseScheduleItem item : myScheduleItems) {
                item.scheduleResponseInterval();
                myScheduledIntervals.add(item.getResponseInterval());
            }
        }

    }

}
