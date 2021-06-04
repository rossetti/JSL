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

import java.text.DecimalFormat;
import jsl.modeling.elements.entity.Entity;
import jsl.utilities.GetNameIfc;

/**
 * This class represents a simulated event. This file contains the JSLEvent
 * class. It allows for the simulation of durations of simulated time. These
 * events are placed on the Executive and ordered by time, priority, and order
 * of creation. JSLEvents are the mechanism by which ModelElements notify each
 * other of work to be done.
 * @param <T> the type associated with the (optional) message sent with the event
 */
public class JSLEvent<T> implements Comparable<JSLEvent>, GetNameIfc {

    /**
     * Represents the default priority for events within the Executive
     * DEFAULT_PRIORITY = 10. Lower priority goes first. All integer priority
     * numbers can be used to set the priority of an event.
     */
    public static final int DEFAULT_PRIORITY = 10;

    /**
     * Default event priority for the end replication event
     */
    public static final int DEFAULT_END_REPLICATION_EVENT_PRIORITY = 10000;

    /**
     * A constant for the default warm up event priority
     */
    public static final int DEFAULT_WARMUP_EVENT_PRIORITY = 9000;

    /**
     * A constant for the default batch priority
     */
    public static final int DEFAULT_BATCH_PRIORITY = 8000;

    /**
     * The default type for an event is zero. It is just a number that can be
     * use to map over to event type
     */
    public static final int DEFAULT_TYPE = 0;

    /**
     * A generic object attached to the event that can be accessed as needed.
     */
    private T myMessage;

    /**
     * The action to be executed because of the event
     */
    private EventActionIfc<T> myAction;

    /**
     * A string descriptor for the event.
     * <p>
     */
    private String myName;

    /**
     * The time that the event is scheduled to execute
     */
    private double myTime;

    /**
     * The priority of the event. Lower is give execution priority.
     */
    private int myPriority;

    /**
     * A unique number assigned to the event for identification purposes
     */
    private long myId;

    /**
     * A number that can be used to distinguish events when choosing what action
     * to execute in handleEvent() methods
     */
    private int myType;

    /**
     * whether or not the event has been canceled
     */
    private boolean myCancelledFlag;

    /**
     * Whether or not the event is currently scheduled
     * <p>
     */
    private boolean myScheduledFlag;

    /**
     * Allows the association of an Entity with the event
     */
    private Entity myEntity;

    /**
     * The model element that was responsible for scheduling the event
     */
    private ModelElement myModelElement;

    /**
     * Constructs an instance of an event. This constructor has package scope
     * because only the Executive class can make events. The methods
     * <p>
     * setScheduledFlag() setCancelledFlag() setName() setListener() setTime()
     * setPriority() setMessage() setId()
     * <p>
     * must all be appropriately called by Executive to ensure a valid state for
     * the event.
     * <p>
     */
    JSLEvent() {
        myCancelledFlag = false;
        myScheduledFlag = false;
    }

    /**
     * Allows the setting of an entity that is sent with the event.
     *
     * @param e Represents the Entity
     */
    public final void setEntity(Entity e) {
        myEntity = e;
    }

    /**
     * Gets the entity attached to the event, if set May be null
     *
     * @return the entity attached to the event
     */
    public final Entity getEntity() {
        return myEntity;
    }

    /**
     * Returns the ModelElement that scheduled the event
     *
     * @return the ModelElement that scheduled the event
     */
    public final ModelElement getModelElement() {
        return myModelElement;
    }

    /**
     * Allows the setting of an object that is sent with the event. This object
     * can be used to pass information between the dispatcher and the listener
     *
     * @param obj Represents the Object acting as a message
     */
    final void setMessage(T obj) {
        myMessage = obj;
    }

    /**
     * Sets the name of the event. Useful for tracing events.
     *
     * @param name A String representing the name of the event
     */
    public final void setName(String name) {
        myName = name;
    }

    /**
     * Sets the type of the event. Useful for tracing events. or distinguishing
     * between events.
     *
     * @param type An integer representing the type of the event
     */
    public final void setType(int type) {
        myType = type;
    }

    /**
     * Sets the action for the event. The action of the event is an object that
     * implements the EventActionIfc interface. If null is passed in a
     * JSLEventException occurs. Every event must have an action. The user may
     * change the action after the event has been scheduled.
     *
     * @param action A instance of a class that implements the EventActionIfc
     * must be non-null or a JSLException occurs
     */
     final void setEventAction(EventActionIfc<T> action) {
        // only called from Executive, no need to allow public access
        // why would changing the action after scheduling be permitted?
        if (action == null) {
            throw new JSLEventException("No listener provided for the event");
        }
        myAction = action;
    }

    /**
     * Indicates to the scheduler that the event should be canceled or not. True
     * implies that the event is to be canceled. It is up to the scheduler to
     * handle the cancellation. If the event is canceled, it's execute method
     * will not be called when it becomes the current event. Thus, an event can
     * be canceled or uncanceled at any simulated time prior to when the event
     * is scheduled to occur. It is an error to attempt to cancel and event that is
     * not scheduled.
     *
     * @param b A boolean (true is canceled, false is not canceled)
     *
     */
    public final void setCanceledFlag(boolean b) {
        myCancelledFlag = b;
    }

    /**
     * Returns the object representing the message sent with the event
     *
     * @return Object representing the message sent with the event
     */
    public final T getMessage() {
        return (myMessage);
    }

    /**
     * Gets the EventListenerIfc
     *
     * @return The object representing the EventListenerIfc
     */
    public final EventActionIfc getEventAction() {
        return (myAction);
    }

    /**
     * Gets the name of the event
     *
     * @return The name of the event
     */
    @Override
    public final String getName() {
        return (myName);
    }

    /**
     * Gets the time that the event is schedule for.
     *
     * @return A double representing simulated time
     */
    public final double getTime() {
        return (myTime);
    }

    /**
     * Gets the priority of the event
     *
     * @return An int representing the priority. Lower is better.
     */
    public final int getPriority() {
        return (myPriority);
    }

    /**
     * Gets the the id assigned to the event by the scheduler. No two events
     * have the same id.
     *
     * @return A long representing the id
     */
    public final long getId() {
        return (myId);
    }

    /**
     * Gets the type of the event. Useful for tracing events. or distinguishing
     * between events.
     *
     * @return an int representing the type of the event.
     */
    public final int getType() {
        return (myType);
    }

    /**
     * Gets a flag indicating whether the event is to be canceled or not. True
     * implies that the event is to be canceled. It is up to the scheduler to
     * handle the cancellation. If the event is canceled, it's execute method
     * will not be called when it becomes the current event. Thus, an event can
     * be canceled or uncanceled at any simulated time prior to when the event
     * is scheduled to occur.
     *
     * @return b A boolean (true is canceled, false is not canceled)
     *
     */
    public final boolean getCanceledFlag() {
        return (myCancelledFlag);
    }

    /**
     * Gets a flag indicating whether the event is in the event calendar, i.e.
     * it has been scheduled.
     * <p>
     * True implies that the event has been scheduled. It is an error to attempt
     * to schedule an event that has already been scheduled.
     *
     * @return A boolean (true is scheduled, false is not scheduled)
     */
    public final boolean isScheduled() {
        return (myScheduledFlag);
    }

    /**
     * Provides a string representation for the event. Useful for tracing
     *
     * @return A String representing the event
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df = new DecimalFormat("0.####E0");
        sb.append(df.format(myTime));
        sb.append(" > Event = ");
        sb.append(myName);
        sb.append(" : ");
        sb.append("ID = ");
        sb.append(myId);
        sb.append(" : ");
        sb.append("Priority = ");
        sb.append(myPriority);
        sb.append(" : ");
        sb.append("Type = ");
        sb.append(myType);
        sb.append(" : ");
        sb.append("Scheduled by = ");
        sb.append(myModelElement.getName());
        // sb.append("\t");
        // sb.append("Listener = ");
        // sb.append(myListener);
        return (sb.toString());
    }

    /**
     * Sets the ModelElement that scheduled the event.
     *
     * @param mElement the model element that scheduled the event, cannot be null
     */
    protected final void setModelElement(ModelElement mElement) {
        if (mElement == null) {
            throw new IllegalArgumentException("The model element doing the scheduling was null");
        }
        myModelElement = mElement;
    }

    /**
     * Sets the id of the event, package scope because only the Scheduler should
     * be setting the id
     *
     * @param id Provided by the Scheduler class
     */
    protected final void setId(long id) {
        myId = id;
    }

    /**
     * Sets the scheduled flag of the event, package scope because only the
     * Scheduler should indicate if the event is scheduled
     *
     * @param flag Provided by the Scheduler class (true is scheduled)
     */
    final void setScheduledFlag(boolean flag) {
        myScheduledFlag = flag;
    }

    /**
     * Sets the scheduled time of the event, protected scope because only the
     * Scheduler should indicate when the event is scheduled
     *
     * @param t Provided by the Scheduler class. The time of the event.
     *
     */
    protected final void setTime(double t) {
        // if already scheduled
        if (isScheduled()) {
            throw new JSLEventException("Tried to change time of already scheduled event!");
        }
        myTime = t;
    }

    /**
     * Sets the scheduled priority of the event, package scope because only the
     * Scheduler should indicate the priority of the event
     *
     * @param p Provided by the Scheduler class
     */
    protected final void setPriority(int p) {
        // if already scheduled
        if (isScheduled()) {
            throw new JSLEventException("Tried to change priority of already scheduled event!");
        }

        myPriority = p;
    }

    /**
     * Called by the Executive class to cause the EventAction to have it's
     * action method invoked
     */
    protected final void execute() {
        myAction.action(this);
    }

    /**
     * Returns a negative integer, zero, or a positive integer if this object is
     * less than, equal to, or greater than the specified object.
     * <p>
     * Natural ordering: time, then priority, then order of creation
     * <p>
     * Lower time, lower priority, lower order of creation goes first
     * <p>
     * Throws ClassCastException if the specified object's type prevents it from
     * begin compared to this object.
     * <p>
     * Throws RuntimeException if the id's of the objects are the same, but the
     * references are not when compared with equals.
     * <p>
     * Note: This class may have a natural ordering that is inconsistent with
     * equals.
     *
     * @param event The event to compare this event to
     * @return Returns a negative integer, zero, or a positive integer if this
     * object is less than, equal to, or greater than the specified object.
     */
    @Override
    public final int compareTo(JSLEvent event) {

        // compare time first
        if (myTime < event.getTime()) {
            return (-1);
        }

        if (myTime > event.getTime()) {
            return (1);
        }

        // times are equal, check priorities
        if (myPriority < event.getPriority()) {
            return (-1);
        }

        if (myPriority > event.getPriority()) {
            return (1);
        }

        // time and priorities are equal, compare ids
        if (myId < event.getId()) // lower id, implies created earlier
        {
            return (-1);
        }

        if (myId > event.getId()) {
            return (1);
        }

        // if the id's are equal then the object references must be equal
        // if this is not the case there is a problem
        if (this.equals(event)) {
            return (0);
        } else {
            throw new RuntimeException("Id's were equal, but references were not, in JSLEvent compareTo");
        }

    }
}
