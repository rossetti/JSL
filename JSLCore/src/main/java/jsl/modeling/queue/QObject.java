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
package jsl.modeling.queue;

import jsl.utilities.*;
import jsl.simulation.IllegalStateException;
import jsl.simulation.State;
import jsl.simulation.StateAccessorIfc;
import jsl.utilities.GetValueIfc;

import java.util.Optional;

/**
 * QObject can be used as a base class for objects that need to be placed in
 * queues on a regular basis.  A QObject can be in one and only one Queue at a time. 
 * An arbitrary object can be associated with the QObject. The user is
 * responsible for managing the type of the attached object.
 *
 */
public class QObject implements GetNameIfc, Comparable<QObject> {

    /**
     * incremented to give a running total of the number of model QObject
     * created
     */
    private static long myCounter_;

    /**
     * The id of the QObject, currently if the QObject is the ith QObject
     * created then the id is equal to i
     */
    private long myId;

    /**
     * The name of the QObject
     */
    private String myName;

    /**
     * A state representing that the QObject was created
     */
    private double myCreationTime;

    /**
     * A state representing when the QObject was queued
     */
    private State myQueuedState;

    /**
     * A priority for use in queueing
     */
    private int myPriority;

    /**
     * The current queue that the QObject is in, null if not in a queue
     */
    private Queue myQueue;

    /**
     * A reference to an object that can be attached to the QObject when queued
     */
    private Object myAttachedObject;

    /**
     * can be used to time stamp the qObject
     */
    protected double myTimeStamp;

    /**
     * Generic attribute that can be used to return a value
     */
    protected GetValueIfc myValue;

    /**
     * Creates a QObject with name "null" and the creation time set to the
     * supplied value
     *
     * @param creationTime the time created
     */
    public QObject(double creationTime) {
        this(creationTime, null);
    }

    /**
     * Creates an QObject with the given name and the creation time set to the
     * supplied value
     *
     * @param creationTime the time created
     * @param name The name of the QObject
     */
    public QObject(double creationTime, String name) {
        initialize(creationTime, name);
    }

    /**
     * Returns the value determined by the object supplied from setValue(). The
     * object returned may be null
     *
     * @return an implementation of GetValueIfc or null
     */
    public Optional<GetValueIfc> getValueObject() {
        return Optional.ofNullable(myValue);
    }

    /**
     * Allows for a generic value to be held by the QObject whose value will be
     * return by getValue() It can be null, in which case, getValue() will
     * return null
     *
     * @param value the value object
     */
    public void setValueObject(GetValueIfc value) {
        myValue = value;
    }

    @Override
    public String toString() {
        return ("ID= " + getId() + ", name= " + getName());
    }

    /**
     * Gets this QObject's name.
     *
     * @return The name of the QObject.
     */
    @Override
    public final String getName() {
        return myName;
    }

    /**
     * Gets a uniquely assigned identifier for this QObject. This
     * identifier is assigned when the QObject is created. It may vary if the
     * order of creation changes.
     *
     * @return The identifier for the entity.
     */
    public final long getId() {
        return (myId);
    }

    /**
     * Gets the time that the QObject was created
     *
     * @return A double representing simulated creation time
     */
    public final double getCreateTime() {
        return (myCreationTime);
    }

    /**
     * Gets the queueing priority associated with this QObject
     *
     * @return The priority as an int
     */
    public final int getPriority() {
        return (myPriority);
    }

    /**
     * Returns the queue that the QObject was last enqueued within
     *
     * @return The Queue, or null if no queue
     */
    public final Queue getQueue() {
        return (myQueue);
    }

    /**
     * Gets the time the QObject was LAST enqueued
     *
     * @return A double representing the time the QObject was enqueued
     */
    public final double getTimeEnteredQueue() {
        return (myQueuedState.getTimeStateEntered());
    }

    /**
     * Gets the time the QObject LAST exited a queue
     *
     * @return A double representing the time the QObject last exited a QObject
     */
    public final double getTimeExitedQueue() {
        return (myQueuedState.getTimeStateExited());
    }

    /**
     * Gets the time the QObject spent in the Queue based on the LAST time
     * dequeued
     *
     * @return the most recent time spend in a queue
     */
    public final double getTimeInQueue() {
        return (myQueuedState.getTotalTimeInState());
    }

    /**
     * This method can be used to get direct access to the State that represents
     * when the object was queued. This allows access to the total time in the
     * queued state as well as other statistical accumulation of state
     * statistics
     *
     * @return Returns the QueuedState.
     */
    public final StateAccessorIfc getQueuedState() {
        return myQueuedState;
    }

    /**
     * If there is no attached object then this method returns null
     *
     * @return A reference to the attached object
     */
    public final Object getAttachedObject() {
        return (myAttachedObject);
    }

    /**
     * Checks if the QObject is queued
     *
     * @return true if it is queued
     */
    public final boolean isQueued() {
        return (myQueuedState.isEntered());
    }

    /**
     * Sets the priority to the supplied value If the QObject is queued, the
     * queue's changePriority() method is called (possibly causing a reordering
     * of the queue) which may cause significant reordering overhead otherwise
     * the priority is directly changed Changing this value only changes how the
     * QObjects are compared and may or may not change how they are ordered in
     * the queue, depending on the queue discipline used
     *
     * @param priority An integer representing the priority of the QObject
     */
    public final void setPriority(int priority) {
        if (isQueued()) {
            myQueue.changePriority(this, priority);
        } else {
            setPriority_(priority);
        }
    }

    /**
     * Sets the name of this QObject
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        myName = str;
    }

    /**
     * Causes the QObject to look like new, gets a new name, number, priority is
     * reset to 1, states are initialized, and starts in created state. As if
     * newly, created. Useful if reusing QObjects
     *
     * @param time used to set the creation time of the QObject
     * @param name the name
     */
    protected final void initialize(double time, String name) {
        if (time < 0){
            throw new IllegalArgumentException("The creation time must be > 0.0");
        }
        myCounter_ = myCounter_ + 1;
        myId = myCounter_;
        setName(name);
        myPriority = 1;
        setQueue(null);
        myAttachedObject = null;
        myValue = null;
        myCreationTime = time;

        if (myQueuedState == null) {
            myQueuedState = new State(getName() + "Queued");
        } else {
            myQueuedState.initialize();
        }

    }

    /**
     * Used to make the QObject not have any references, e.g. to a Queue and to
     * an Object that was queued
     * <p>
     */
    protected void setNulls() {
        myAttachedObject = null;
        myValue = null;
        setQueue(null);
        myName = null;
        myQueuedState = null;
    }

    /**
     * Causes all references to objects from this QObject to be set to null and
     * all internal objects to be set to null including State information
     * <p>
     * Meant primarily to facilitate garbage collection. After this call, the
     * object should not be used.
     * <p>
     */
    public void nullify() {
        setNulls();
    }

    /**
     * Used by Queue to indicate that the QObject has entered the queue
     *
     * @param queue the queue entered
     * @param time the time
     * @param priority the priority
     * @param obj an object to attach
     */
    protected final void enterQueue(Queue queue, double time, int priority, Object obj) {
        if (queue == null) {
            throw new IllegalArgumentException("The Queue must be non-null");
        }
        if (myQueuedState == null) {
            throw new NullPointerException("myQueuedState was null!");
        }
        if (myQueuedState.isEntered()) {
            throw new IllegalStateException("The QObject was already queued!");
        }
        myQueuedState.enter(time);
        setQueue(queue);
        setPriority_(priority);
        setAttachedObject(obj);
    }

    /**
     * Indicates that the QObject exited the queue
     *
     * @param time The time QObject exited the queue
     */
    protected final void exitQueue(double time) {
        if (!myQueuedState.isEntered()) {
            throw new IllegalStateException("The QObject was not in a queue!");
        }
        myQueuedState.exit(time);
        setQueue(null);
    }

    /**
     * Sets the queueing priority for this QObject Changing the priority while
     * the object is in a queue has no effect on the ordering of the queue. This
     * priority is only used to determine the ordering in the queue when the
     * item enters the queue.
     *
     * @param priority lower priority implies earlier ranking in the queue
     */
    protected final void setPriority_(int priority) {
        myPriority = priority;
    }

    /**
     * Sets the queue that the QObject is enqueued
     *
     * @param queue The Queue that the object is enqueued
     */
    protected void setQueue(Queue queue) {
        myQueue = queue;
    }

    /**
     * Sets an object that can be attached to the QObject
     *
     * @param obj The attached object
     */
    public final void setAttachedObject(Object obj) {
        myAttachedObject = obj;
    }

    //  ===========================================
    //        Comparable Interface
    //  ===========================================
    /**
     * Returns a negative integer, zero, or a positive integer if this object is
     * less than, equal to, or greater than the specified object.
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
     * @param obj The object to compare to
     * @return Returns a negative integer, zero, or a positive integer if this
     * object is less than, equal to, or greater than the specified object.
     */
    @Override
    public final int compareTo(QObject obj) {
        QObject qObj = (QObject) obj;

        // compare the priorities
        if (myPriority < qObj.getPriority()) {
            return (-1);
        }

        if (myPriority > qObj.getPriority()) {
            return (1);
        }

        // priorities are equal, compare time stamps
        if (getTimeEnteredQueue() < qObj.getTimeEnteredQueue()) {
            return (-1);
        }

        if (getTimeEnteredQueue() > qObj.getTimeEnteredQueue()) {
            return (1);
        }

        // time stamps are equal, compare ids
        if (myId < qObj.getId()) // lower id, implies created earlier
        {
            return (-1);
        }

        if (myId > qObj.getId()) {
            return (1);
        }

        // if the id's are equal then the object references must be equal
        // if this is not the case there is a problem
        if (this.equals(obj)) {
            return (0);
        } else {
            throw new RuntimeException("Id's were equal, but references were not, in QObject compareTo");
        }

    }

    /**
     * @return Returns the TimeStamp.
     */
    public final double getTimeStamp() {
        return myTimeStamp;
    }

    /**
     * @param timeStamp The timeStamp to set.
     */
    public final void setTimeStamp(double timeStamp) {
        if (timeStamp < 0.0) {
            throw new IllegalArgumentException("The time stamp was less than 0.0");
        }
        myTimeStamp = timeStamp;
    }

}
