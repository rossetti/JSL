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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;

/** A Schedule represents a known set of events that can occur according to a pattern.
 *  A schedule contains one or more instances of ScheduleItem.  A ScheduleItem represents an item on a
 *  Schedule. It has a start time, relative to the start of the Schedule and a duration.
 *  If more than one schedule item needs to start at
 *  the same time, then a priority can be provided to determine the ordering (smallest priority goes first).
 *  ScheduleItems are not scheduled to occur until the Schedule actually starts.
 *
 *  A Schedule has an auto start flag, which controls whether or not the schedule should start automatically
 *  upon initialization (at the start of the simulation). The default is to start automatically.
 *
 *  A Schedule has an initial start time, which represents the amount of time after the beginning of
 *  the simulation that the schedule is to start. The default start time is zero (at the beginning of the simulation).
 *
 *  A Schedule as a length (or duration) that represents the total time associated with the schedule. After this
 *  time has elapsed the entire schedule can repeat if the repeat option is on. The default length of a schedule
 *  is infinite.  The total or maximum duration of scheduled items cannot exceed the schedule duration if it is finite.
 *
 *  A Schedule has a repeat flag that controls whether or not it will repeat after its duration has elapsed. The
 *  default is to repeat the schedule and is only relevant if the schedule duration (length) is finite.
 *
 *  A Schedule has a cycle start time that represents when the schedule started its current cycle. Again, this
 *  is only relevant if the repeat flag is true and the schedule duration is finite. If there is only one cycle, it is
 *  the time that the schedule started.
 *
 *  A builder is used to configure the schedule and then items are added to the schedule. If no items are added
 *  to the schedule, then there will still be an event to start the schedule.
 *
 *  To make a Schedule useful, instances of the ScheduleChangeListenerIfc interface should be added to
 *  listen for changes in the schedule.  Instances of ScheduleChangeListenerIfc are notified in the order
 *  in which they are added to the schedule.  Instances of ScheduleChangeListenerIfc are notified when the
 *  schedule starts, when it ends, and when any ScheduleItem starts and ends.  It is up to the instance
 *  of ScheduleChangeListenerIfc to react to the schedule changes that it needs to react to and ignore those
 *  that it does not care about.
 *
 * @author rossetti
 */
public class Schedule extends SchedulingElement {

    private long idCounter = 0;

    /**
     * Indicates whether or not the schedule should be started
     * automatically upon initialization, default is true
     */
    private final boolean myAutoStartFlag;

    /**
     * The time from the beginning of the replication
     * to the time that the schedule is to start
     */
    private final double myInitialStartTime;

    /**
     * Represents the total length of time of the schedule.
     * The total of the durations added to the schedule cannot exceed this
     * amount.
     * After this time has elapsed the entire schedule can repeat if the
     * schedule repeat flag is true. The default is infinite.
     * <p>
     */
    private final double myScheduleLength;

    /**
     * The time that the schedule started for its current cycle
     * <p>
     */
    private double myCycleStartTime;

    /**
     * The schedule repeat flag controls whether or not
     * the entire schedule will repeat after its entire duration
     * has elapsed. The default is to repeat the schedule. The
     * use of this flag only makes sense if a finite schedule length is
     * specified
     * <p>
     */
    private final boolean myScheduleRepeatFlag;

    protected final List<ScheduleItem<?>> myItems;
    protected final List<ScheduleChangeListenerIfc> myChangeListeners;
    protected JSLEvent myStartScheduleEvent;
    private final int myItemStartEventPriority;
    private final int myScheduleStartPriority;

    private Schedule(Builder builder) {
        super(builder.parent, builder.name);
        myAutoStartFlag = builder.autoStartFlag;
        myInitialStartTime = builder.startTime;
        myScheduleRepeatFlag = builder.repeatable;
        myScheduleLength = builder.length;
        myScheduleStartPriority = builder.priority;
        myItemStartEventPriority = builder.itemPriority;
        myItems = new LinkedList<>();
        myChangeListeners = new ArrayList<>();
    }

    /**
     *  A builder for configuring the set up of a Schedule
     */
    public static class Builder {

        private final ModelElement parent;
        private String name = null;
        private boolean autoStartFlag = true;
        private double startTime = 0.0;
        private double length = Double.POSITIVE_INFINITY;
        private boolean repeatable = true;
        private int priority = JSLEvent.DEFAULT_PRIORITY - 5;
        private int itemPriority = JSLEvent.DEFAULT_PRIORITY - 4;

        /**
         *
         * @param parent the parent of the schedule
         */
        public Builder(ModelElement parent) {
            this.parent = parent;
        }

        /**
         *
         * @param name the name of the schedule
         * @return the builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Specifies that the schedule should not auto start
         *
         * @return the builder
         */
        public Builder noAutoStart() {
            autoStartFlag = false;
            return this;
        }

        /**
         * Specifies that the schedule should not repeat
         *
         * @return the builder
         */
        public Builder noRepeats() {
            repeatable = false;
            return this;
        }

        /**
         *
         * @param startTime the time relative to the start of the simulation
         * that indicates when the schedule should start, must be greater than
         * or equal to zero. The default is zero.
         * @return the builder
         */
        public Builder startTime(double startTime) {
            if (startTime < 0.0) {
                throw new IllegalArgumentException("The start time must be >= 0.0");
            }
            this.startTime = startTime;
            return this;
        }

        /**
         *
         * @param length the total length or duration of the schedule
         * @return the Builder
         */
        public Builder length(double length) {
            if (length <= 0.0) {
                throw new IllegalArgumentException("The length of schedule must be > 0.0");
            }
            this.length = length;
            return this;
        }

        /**
         *
         * @param priority the priority of the schedule's start event
         * @return the Builder
         */
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         *
         * @param priority the default priority associated with the item's
         * start events
         * @return
         */
        public Builder itemPriority(int priority) {
            this.itemPriority = priority;
            return this;
        }

        /**
         * Builds the schedule
         *
         * @return the Schedule
         */
        public Schedule build() {
            return new Schedule(this);
        }
    }

    /**
     *
     * @return true if the schedule starts automatically
     */
    public final boolean isAutoStartFlag() {
        return myAutoStartFlag;
    }

    /**
     *
     * @return the priority associated with the schedule's start event
     */
    public final int getStartEventPriority() {
        return myScheduleStartPriority;
    }

    /**
     *
     * @return the priority associated with the item's start event
     */
    public final int getItemStartEventPriority() {
        return myItemStartEventPriority;
    }

    /**
     *
     * @return the time relative to the start of the simulation that indicates
     * when the schedule should start
     */
    public final double getInitialStartTime() {
        return myInitialStartTime;
    }

    /**
     *
     * @return true if the schedule repeats after completing
     */
    public final boolean isScheduleRepeatable() {
        return myScheduleRepeatFlag;
    }

    /**
     *
     * @return the length or duration of the schedule
     */
    public final double getScheduleLength() {
        return myScheduleLength;
    }

    /**
     *
     * @return when the schedule actually starts, if repeated the time it last
     * started
     */
    public final double getCycleStartTime() {
        return myCycleStartTime;
    }

    /**
     * If scheduled to start, this cancels the start of the schedule.
     */
    public final void cancelScheduleStart() {
        if (myStartScheduleEvent != null) {
            myStartScheduleEvent.setCanceledFlag(true);
        }
    }

    /**
     * The same listener cannot be added more than once. Listeners are
     * notified of schedule changes in the sequence by which they were added.
     *
     * @param listener the listener to add to the schedule
     */
    public void addScheduleChangeListener(ScheduleChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to attach a null observer");
        }
        if (myChangeListeners.contains(listener)) {
            throw new IllegalArgumentException("The supplied listener is already attached");
        }

        myChangeListeners.add(listener);
    }

    /**
     *
     * @param listener the listener to delete from the schedule
     */
    public void deleteScheduleChangeListener(ScheduleChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to delete a null listener");
        }
        myChangeListeners.remove(listener);
    }

    /**
     * Deletes all listeners
     */
    public void deleteScheduleChangeListeners() {
        myChangeListeners.clear();
    }

    /**
     *
     * @param listener the listener to check
     * @return true if the listener is already added
     */
    public boolean contains(ScheduleChangeListenerIfc listener) {
        return myChangeListeners.contains(listener);
    }

    /**
     *
     * @return the number of listeners
     */
    public int countScheduleChangeListeners() {
        return myChangeListeners.size();
    }

    /**
     * Adds an item to the schedule with priority JSLEvent.DEFAULT_PRIORITY and
     * default start time of 0.0 (i.e. when the schedule starts)
     *
     * @param duration the duration of the item
     * @return the created ScheduleItem
     */
    public ScheduleItem addItem(double duration) {
        return addItem(0.0, duration, getItemStartEventPriority(), null);
    }

    /**
     * Adds an item to the schedule with priority JSLEvent.DEFAULT_PRIORITY
     *
     * @param startTime the time past the start of the schedule to start the
     * item
     * @param duration the duration of the item
     * @return the created ScheduleItem
     */
    public ScheduleItem addItem(double startTime, double duration) {
        return addItem(startTime, duration, getItemStartEventPriority(), null);
    }

    /**
     * Adds an item to the schedule
     *
     * @param startTime the time past the start of the schedule to start the
     * item
     * @param duration the duration of the item
     * @param priority the priority, among items, if items start at the same
     * time
     * @return the created ScheduleItem
     */
    public ScheduleItem addItem(double startTime, double duration, int priority) {
        return addItem(startTime, duration, priority, null);
    }

    /**
     * Adds an item to the schedule with priority JSLEvent.DEFAULT_PRIORITY
     *
     * @param <T> the type of the message
     * @param startTime the time past the start of the schedule to start the
     * item
     * @param duration the duration of the item
     * @param message a message or datum to attach to the item
     * @return the created ScheduleItem
     */
    public <T> ScheduleItem<T> addItem(double startTime, double duration, T message) {
        return addItem(startTime, duration, getItemStartEventPriority(), message);
    }

    /**
     * Adds an item to the schedule
     *
     * @param <T> the type of the message
     * @param startTime the time past the start of the schedule to start the
     * item
     * @param duration the duration of the item
     * @param priority the priority, (among items) if items start at the same
     * time
     * @param message a message or datum to attach to the item
     * @return the created ScheduleItem
     */
    public <T> ScheduleItem<T> addItem(double startTime, double duration, int priority, T message) {
        ScheduleItem<T> aItem = new ScheduleItem<>(startTime, duration, priority, message);

        if (aItem.getEndTime() > getInitialStartTime() + getScheduleLength()) {
            throw new IllegalArgumentException("The item's end time is past the schedule's end.");
        }

        // nothing in the list, just add to beginning
        if (myItems.isEmpty()) {
            myItems.add(aItem);
            return aItem;
        }
        // might as well check for worse case, if larger than the largest
        // then put it at the end and return
        if (aItem.compareTo(myItems.get(myItems.size() - 1)) >= 0) {
            myItems.add(aItem);
            return aItem;
        }

        // now iterate through the list
        for (ListIterator<ScheduleItem<?>> i = myItems.listIterator(); i.hasNext();) {
            if (aItem.compareTo(i.next()) < 0) {
                // next() move the iterator forward, if it is < what was returned by next(), then it
                // must be inserted at the previous index
                myItems.add(i.previousIndex(), aItem);
                break;
            }
        }
        return aItem;
    }

    /** Removes the item from the schedule. If the item is null or not on this
     * schedule nothing happens.
     *
     * @param item the item to remove
     */
    public void removeItem(ScheduleItem item){
        if (item == null){
            return;
        }
        myItems.remove(item);
    }

    /**
     *  Removes all schedule items from the schedule
     */
    public void clearSchedule(){
        myItems.clear();
    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule: ");
        sb.append(getName());
        sb.append(System.lineSeparator());
        sb.append("Initial Start Time = ").append(getInitialStartTime());
        sb.append(System.lineSeparator());
        sb.append("Length = ").append(getScheduleLength());
        sb.append(System.lineSeparator());
        sb.append("Auto start = ").append(isAutoStartFlag());
        sb.append(System.lineSeparator());
        sb.append("Repeats = ").append(isScheduleRepeatable());
        sb.append(System.lineSeparator());
        sb.append("Start event priority = ").append(getStartEventPriority());
        sb.append(System.lineSeparator());
        sb.append("Item Start event priority = ").append(getItemStartEventPriority());
        sb.append(System.lineSeparator());
        sb.append("Items:");
        sb.append(System.lineSeparator());
        sb.append("----------------------------------------------------------");
        sb.append(System.lineSeparator());
        for (ScheduleItem i : myItems) {
            sb.append(i).append(System.lineSeparator());
        }
        sb.append("----------------------------------------------------------");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    protected void notifyScheduleChangeListenersScheduleStarted() {
        for (ScheduleChangeListenerIfc listener : myChangeListeners) {
            listener.scheduleStarted(this);
        }
    }

    protected void notifyScheduleChangeListenersScheduleEnded() {
        for (ScheduleChangeListenerIfc listener : myChangeListeners) {
            listener.scheduleEnded(this);
        }
    }

    protected void notifyScheduleChangeListenersScheduleItemEnded(ScheduleItem<?> item) {
        for (ScheduleChangeListenerIfc listener : myChangeListeners) {
            listener.scheduleItemEnded(item);
        }
    }

    protected void notifyScheduleChangeListenersScheduleItemStarted(ScheduleItem<?> item) {
        for (ScheduleChangeListenerIfc listener : myChangeListeners) {
            listener.scheduleItemStarted(item);
        }
    }

    @Override
    protected void initialize() {
        myCycleStartTime = Double.NaN;
        if (isAutoStartFlag()) {
            scheduleStart();
        }
    }

    @Override
    protected void afterReplication() {
        super.afterReplication();
        myStartScheduleEvent = null;
    }

    /**
     * Schedules the start of the schedule for the start time of the schedule
     * if it has not already be started
     */
    public final void scheduleStart() {
        if (myStartScheduleEvent == null) {
            // priority for starting the schedule must be lower than the first
            // item on the schedule to ensure it goes first
            int priority = getStartEventPriority();
            if (!myItems.isEmpty()) {
                int p = myItems.get(0).getPriority();
                if (p < priority) {
                    priority = p - 1;
                }
            }
            myStartScheduleEvent = scheduleEvent(this::startSchedule,
                    getInitialStartTime(), priority);
        }
    }

    protected void startSchedule(JSLEvent evt) {
        myCycleStartTime = getTime();
        // logic for what to do when schedule is started
        notifyScheduleChangeListenersScheduleStarted();
        for (ScheduleItem item : myItems) {
            scheduleItemStart(item);
        }
        scheduleEndOfSchedule();
    }

    protected void endSchedule(JSLEvent evt) {
        notifyScheduleChangeListenersScheduleEnded();
        if (isScheduleRepeatable()) {
            startSchedule(evt);
        }
    }

    protected void startItem(JSLEvent evt) {
        ScheduleItem item = (ScheduleItem) evt.getMessage();
        notifyScheduleChangeListenersScheduleItemStarted(item);
        scheduleItemEnd(item);
    }

    protected void endItem(JSLEvent evt) {
        ScheduleItem item = (ScheduleItem) evt.getMessage();
        notifyScheduleChangeListenersScheduleItemEnded(item);
    }

    protected final void scheduleEndOfSchedule() {
        // priority for end the schedule must be lower than the first
        // item and lower than the start of the schedule to ensure it goes first
        int priority = getStartEventPriority();
        if (!myItems.isEmpty()) {
            int p = myItems.get(0).getPriority();
            if (p < priority) {
                priority = p - 2;
            }
        }
        scheduleEvent(this::endSchedule,
                getScheduleLength(), priority);
    }

    protected final void scheduleItemStart(ScheduleItem item) {
        // if the item's start time is 0.0 relative to the start of 
        // the schedule its priority must be after the start of schedule
        int priority = item.getPriority();
        if (item.getStartTime() == 0.0) {
            // check priority
            if (item.getPriority() <= getStartEventPriority()) {
                priority = getStartEventPriority() + 1;
            }
        }
        JSLEvent<ScheduleItem> e = scheduleEvent(this::startItem,
                item.getStartTime(), priority, item);
        //e.setMessage(item);
        item.myStartEvent = e;
    }

    protected final void scheduleItemEnd(ScheduleItem item) {
        // if the item's end time is at the same time as the end of the
        // schedule then it's priority needs to be before the priority
        // of the end of the schedule
        int priority = item.getPriority();
        if (item.getEndTime() == getScheduleLength()) {
            // need to adjust priority, compute end priority
            int endPriority = getStartEventPriority();
            if (!myItems.isEmpty()) {
                int p = myItems.get(0).getPriority();
                if (p < endPriority) {
                    endPriority = p - 2;
                }
            }
            priority = endPriority - 1;
        }
        JSLEvent<ScheduleItem> event = scheduleEvent(this::endItem,
                item.getDuration(), priority - 1, item);
        //event.setMessage(item);
        item.myEndEvent = event;
    }

    /** A ScheduleItem represents an item on a Schedule. It has a start time, relative to the
     *  start of the Schedule and a duration. If more than one schedule item needs to start at
     *  the same time, then a priority can be provided to determine the ordering (smallest priority goes first).
     *
     * @param <T> a general message or other object that can be associated with the ScheduleItem
     */
    public class ScheduleItem<T> implements Comparable<ScheduleItem> {

        private String myName;
        private final double myStartTime;
        private final double myDuration;
        private final int myPriority;
        private JSLEvent myStartEvent;
        private JSLEvent myEndEvent;
        private final long myId;
        private final Schedule mySchedule;
        private final T myMessage;

        public ScheduleItem(double startTime, double duration, int priority, T message) {
            idCounter = idCounter + 1;
            myId = idCounter;
            myName = "Item:" + myId;
            if (startTime < 0.0) {
                throw new IllegalArgumentException("The start time must be >= 0.0");
            }
            if (duration <= 0.0) {
                throw new IllegalArgumentException("The duration must be > 0.0");
            }
            myStartTime = startTime;
            myDuration = duration;
            myPriority = priority;
            myMessage = message;
            mySchedule = Schedule.this;
        }

        public String getName() {
            return myName;
        }

        public void setName(String name) {
            myName = name;
        }

        public T getMessage() {
            return myMessage;
        }

        public double getStartTime() {
            return myStartTime;
        }

        public final double getDuration() {
            return myDuration;
        }

        public final double getEndTime() {
            return getStartTime() + getDuration();
        }

        public final int getPriority() {
            return myPriority;
        }

        public final long getId() {
            return myId;
        }

        public final Schedule getSchedule() {
            return mySchedule;
        }

        @Override
        public final String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ID = ");
            sb.append(myId);
            sb.append("name = ");
            sb.append(myName);
            sb.append(" : Priority = ");
            sb.append(myPriority);
            sb.append(" : Start time = ");
            sb.append(myStartTime);
            sb.append(" : Duration = ");
            sb.append(myDuration);
            if (myStartEvent != null){
              sb.append(" : Start event priority = ");    
              sb.append(myStartEvent.getPriority());
            }
            if (myEndEvent != null){
              sb.append(" : End event priority = ");    
              sb.append(myEndEvent.getPriority());
            }
            return sb.toString();
        }

        /**
         * Returns a negative integer, zero, or a positive integer if this
         * object is
         * less than, equal to, or greater than the specified object.
         * <p>
         * Natural ordering: time, then priority, then order of creation
         * <p>
         * Lower time, lower priority, lower order of creation goes first
         * <p>
         * Throws ClassCastException if the specified object's type prevents it
         * from
         * begin compared to this object.
         * <p>
         * Throws RuntimeException if the id's of the objects are the same, but
         * the
         * references are not when compared with equals.
         * <p>
         * Note: This class may have a natural ordering that is inconsistent
         * with
         * equals.
         *
         * @param item The event to compare this event to
         * @return Returns a negative integer, zero, or a positive integer if
         * this
         * object is less than, equal to, or greater than the specified object.
         */
        @Override
        public final int compareTo(ScheduleItem item) {

            // compare time first
            if (myStartTime < item.myStartTime) {
                return (-1);
            }

            if (myStartTime > item.myStartTime) {
                return (1);
            }

            // times are equal, check priorities
            if (myPriority < item.myPriority) {
                return (-1);
            }

            if (myPriority > item.myPriority) {
                return (1);
            }

            // time and priorities are equal, compare ids
            if (myId < item.myId) // lower id, implies created earlier
            {
                return (-1);
            }

            if (myId > item.myId) {
                return (1);
            }

            // if the id's are equal then the object references must be equal
            // if this is not the case there is a problem
            if (this.equals(item)) {
                return (0);
            } else {
                throw new RuntimeException("Id's were equal, but references were not, in ScheduleItem compareTo");
            }

        }
    }

}
