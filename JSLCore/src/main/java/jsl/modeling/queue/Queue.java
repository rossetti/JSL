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

import jsl.simulation.ModelElement;
import jsl.modeling.elements.RandomElementIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.*;
import java.util.function.Predicate;

/**
 * The Queue class provides the ability to hold entities (QObjects) within the
 * model. Any object can be added to a Queue. When an object is added to a
 * Queue, the object is wrapped by a QObject which provides statistical
 * collection. In this way, objects that queue do not need additional behavior.
 * <p>
 * FIFODiscipline ensures first-in, first-out behavior. LIFODiscipoine ensures
 * last-in, last-out behavior. RankedDiscipline ensures that each new element is
 * added such that the priority is maintained from smallest first to largest
 * priority last using the compareTo method of the QObject. Ties in priority
 * give preference to time of creation, then to order of creation.
 * RandomDiscipline causes the elements to be randomly selected (uniformly).
 *
 * @param <T> queues must hold sub-types of QObject
 */
public class Queue<T extends QObject> extends ModelElement implements
        Iterable<T> {

    /**
     * ENQUEUED indicates that something was just enqueued DEQUEUED indicates
     * that something was just dequeued
     * <p>
     */
    public static enum Status {
        ENQUEUED, // something has entered the queue
        DEQUEUED, // something has exited the queue
        IGNORE // ignore the exit or entrance
    };

    public static enum Discipline {
        FIFO, // first-in, first out
        LIFO, // last-in, first out
        RANDOM, // randomly selected over elements
        RANKED // orderd by QObject priority
    }

    /**
     * The list of items in the queue.
     */
    protected List<T> myList;

    /**
     * The current QueueDiscipline for this Queue.
     */
    private QueueDiscipline myDiscipline;
    protected Discipline myCurDiscipline;

    /**
     * The initial QueueDiscipline for this Queue.
     */
    private QueueDiscipline myInitialDiscipline;
    protected Discipline myInitDiscipline;

    /**
     * Holds the listeners for this queue's enqueue and removeNext method use
     */
    protected List<QueueListenerIfc<T>> myQueueListeners;

    /**
     * Indicates whether something was just enqueued or dequeued
     */
    protected Status myStatus;

    /**
     * Collect statistics on time in queue and number in queue
     */
    protected QueueResponse<T> myResponses;

    /**
     * Constructs a Queue. The default will be a FIFO queue
     *
     * @param parent its parent
     */
    public Queue(ModelElement parent) {
        this(parent, null, Discipline.FIFO, true);
    }

    /**
     * Constructs a Queue with the given name. The queue will be a FIFO by
     * default.
     *
     * @param parent its parent
     * @param name The name of the queue
     */
    public Queue(ModelElement parent, String name) {
        this(parent, name, Discipline.FIFO, true);
    }

    /**
     * Constructs a Queue that follows the given queue discipline.
     *
     * @param parent its parent
     * @param discipline The queuing discipline to be followed
     */
    public Queue(ModelElement parent, Discipline discipline) {
        this(parent, null, discipline, true);
    }

    /**
     * Constructs a Queue with the given name that follows the given queue
     * discipline.
     *
     * @param parent its parent
     * @param name The name of the queue
     * @param discipline The queuing discipline to be followed
     */
    public Queue(ModelElement parent, String name, Discipline discipline) {
        this(parent, name, discipline, true);
    }

    /**
     * Constructs a Queue with the given name that follows the given queue
     * discipline.
     *
     * @param parent its parent
     * @param name The name of the queue
     * @param discipline The queuing discipline to be followed
     * @param statOption true turns on statistical response
     */
    public Queue(ModelElement parent, String name, Discipline discipline, boolean statOption) {
        super(parent, name);
        myList = new LinkedList<>();
        setInitialDiscipline(discipline);
        myDiscipline = myInitialDiscipline;
        myCurDiscipline = discipline;
        if (statOption) {
            myResponses = new QueueResponse<>(this);
            addQueueListener(myResponses);
        }
    }

    /**
     *
     * @return If the queue has responses then return them
     */
    public final Optional<QueueResponse<T>> getQueueResponses() {
        return Optional.ofNullable(myResponses);
    }

//    public static <T extends QObject> Builder<T> newQueue(ModelElement parent) {
//        return new Builder<>(parent);
//    }
//
//    public static class Builder<T extends QObject> {
//
//        private final ModelElement parent;
//        private Discipline discipline = Discipline.FIFO;
//        private boolean statOption = true;
//        private String name = null;
//
//        /**
//         * Makes a Builder for making new Queues
//         *
//         * @param parent the parent
//         */
//        public Builder(ModelElement parent) {
//            this.parent = parent;
//        }
//
//        /**
//         *
//         * @param val the name of the Queue
//         * @return the Builder
//         */
//        public Builder name(String val) {
//            name = val;
//            return this;
//        }
//
//        /**
//         *
//         * @param val the discipline of the queue
//         * @return the Builder
//         */
//        public Builder discipline(Discipline val) {
//            discipline = val;
//            return this;
//        }
//
//        /**
//         * Indicates no statistics
//         *
//         * @return the Builder
//         */
//        public Builder withoutStats() {
//            statOption = false;
//            return this;
//        }
//
//        /**
//         * Builds the new Queue
//         *
//         * @return the newly created Queue
//         */
//        public Queue<T> builder() {
//            return new Queue(parent, name, discipline, statOption);
//        }
//    }

    /**
     * can be called to initialize the queue The default behavior is to have the
     * queue cleared after the replication
     */
    @Override
    protected void initialize() {
        super.initialize();
        if (myCurDiscipline != myInitDiscipline) {
            changeDiscipline(myInitDiscipline);
        }
    }

    @Override
    protected void beforeExperiment() {
        super.beforeExperiment();
        myDiscipline.beforeExperiment();
    }

    @Override
    protected void afterReplication() {
        super.afterReplication();
        clear();
        myDiscipline.afterReplication();
    }

    @Override
    protected void removedFromModel() {
        super.removedFromModel();
        myList.clear();
        myList = null;
        myDiscipline = null;
        myInitialDiscipline = null;
        if (myQueueListeners != null) {
            myQueueListeners.clear();
        }
        myQueueListeners = null;
        myStatus = null;
    }

    /**
     *
     * @return true if queue statistics have been turned on
     */
    public final boolean getQueueStatsOption() {
        return myResponses != null;
    }

    /**
     *
     * @return a unmodifiable view of the underlying list for the Queue
     */
    public final List<T> getUnmodifiableList() {
        return Collections.unmodifiableList(myList);
    }

    /**
     * Adds the supplied listener to this queue
     *
     * @param listener Must not be null, cannot already be added
     * @return true if added
     */
    public final boolean addQueueListener(QueueListenerIfc<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null.");
        }

        if (myQueueListeners == null) {
            myQueueListeners = new ArrayList<>();
        }

        if (myQueueListeners.contains(listener)) {
            throw new IllegalArgumentException("The queue already has the supplied listener.");
        }

        return myQueueListeners.add(listener);
    }

    /**
     * Removes the supplied listener from this queue
     *
     * @param listener Must not be null
     * @return true if removed
     */
    public boolean removeQueueListener(QueueListenerIfc<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null.");
        }
        if (myQueueListeners == null) {
            return (false);
        }

        return myQueueListeners.remove(listener);
    }

    /**
     * Gets whether or not the last action was enqueue or dequeueing an object
     *
     * @return the status
     */
    public final Status getStatus() {
        return myStatus;
    }

    /**
     * Changes the queue's discipline to the given discipline.
     *
     * @param discipline An interface to a queue discipline
     */
    public final void changeDiscipline(Discipline discipline) {

        switch (discipline) {
            case FIFO:
                myDiscipline = new FIFODiscipline();
                break;
            case LIFO:
                myDiscipline = new LIFODiscipline();
                break;
            case RANDOM:
                myDiscipline = new RandomDiscipline();
                break;
            case RANKED:
                myDiscipline = new RankedDiscipline();
                break;
            default:
                throw new IllegalArgumentException("No such discipline");
        }
        myDiscipline.switchDiscipline();
        myCurDiscipline = discipline;
    }

    /**
     *
     * @return The current discipline for the queue
     */
    public final Discipline getCurrentDiscipline() {
        return myCurDiscipline;
    }

    /**
     * Changes the priority of the supplied QObject. May cause the queue to
     * reorder using its discipline
     *
     * @param qObject the sub-type of QObject that is getting its priority changed
     * @param priority the priority to change the value to
     */
    public final void changePriority(QObject qObject, int priority) {
        myDiscipline.changePriority(qObject, priority);
    }

    /**
     * Gets the initial queue discipline
     *
     * @return the initial queue discipline
     */
    public final Discipline getInitialDiscipline() {
        return myInitDiscipline;
    }

    /**
     * Sets the initial queue discipline
     *
     * @param discipline the discipline
     */
    public final void setInitialDiscipline(Discipline discipline) {
        switch (discipline) {
            case FIFO:
                myInitialDiscipline = new FIFODiscipline();
                break;
            case LIFO:
                myInitialDiscipline = new LIFODiscipline();
                break;
            case RANDOM:
                myInitialDiscipline = new RandomDiscipline();
                break;
            case RANKED:
                myInitialDiscipline = new RankedDiscipline();
                break;
            default:
                throw new IllegalArgumentException("No such discipline");
        }
        myInitDiscipline = discipline;
    }

    /**
     * Places the QObject in the queue, uses the priority associated with the QObject, which is 1 by default
     * Automatically, updates the number in queue response variable.
     *
     * @param queueingObject the QObject to enqueue
     */
    public final void enqueue(T queueingObject) {
        enqueue(queueingObject, queueingObject.getPriority(), queueingObject.getAttachedObject());
    }

    /**
     * Places the QObject in the queue, uses the priority associated with the QObject, which is 1 by default
     * Automatically, updates the number in queue response variable.
     * @param <S> The type of the object being attached to the QObject
     * @param queueingObject the sub-type of QObject to enqueue
     * @param obj an Object to be "wrapped" and queued while the QObject is in the queue
     */
    public final <S> void enqueue(T queueingObject, S obj) {
        enqueue(queueingObject, queueingObject.getPriority(), obj);
    }

    /**
     * Places the QObject in the queue, with the default priority of 1
     * Automatically, updates the number in queue response variable.
     *
     * @param queueingObject the QObject to enqueue
     * @param priority the priority for ordering the object, lower has more
     * priority
     */
    public final void enqueue(T queueingObject, int priority) {
        enqueue(queueingObject, priority, queueingObject.getAttachedObject());
    }

    /**
     * Places the QObject in the queue, with the specified priority
     * Automatically, updates the number in queue response variable.
     *
     * @param <S> The type of the object being attached to the QObject
     * @param qObject - the QObject to enqueue
     * @param priority - the priority for ordering the object, lower has more
     * priority
     * @param obj an Object to be "wrapped" and queued while the QObject is
     * queued
     */
    public <S> void enqueue(T qObject, int priority, S obj) {
        if (qObject == null) {
            throw new IllegalArgumentException("The QObject must be non-null");
        }
        qObject.enterQueue(this, getTime(), priority, obj);
        myDiscipline.add(qObject);
        myStatus = Status.ENQUEUED;
        notifyQueueListeners(qObject);
    }

    /**
     * Returns a reference to the QObject representing the item that is next to
     * be removed from the queue according to the queue discipline that was
     * specified.
     *
     * @return a reference to the QObject object next item to be removed, or
     * null if the queue is empty
     */
    public final T peekNext() {
        return (myDiscipline.peekNext());
    }

    /**
     * Removes the next item from the queue according to the queue discipline
     * that was specified. Returns a reference to the QObject representing the
     * item that was removed
     * <p>
     * Automatically, collects the time in queue for the item and includes it in
     * the time in queue response variable.
     * <p>
     * Automatically, updates the number in queue response variable.
     *
     * @return a reference to the QObject object, or null if the queue is empty
     */
    public final T removeNext() {
        T qObj = myDiscipline.removeNext();
        if (qObj != null) {
            qObj.exitQueue(getTime());
            myStatus = Status.DEQUEUED;
            notifyQueueListeners(qObj);
        }
        return (qObj);
    }

    /**
     * Returns true if this queue contains the specified element. More formally,
     * returns true if and only if this list contains at least one element e
     * such that (o==null ? e==null : o.equals(e)).
     * <p>
     * Throws an IllegalArgumentException if QObject qObj is null.
     *
     * @param qObj The object to be removed
     * @return True if the queue contains the specified element.
     */
    public final boolean contains(T qObj) {
        if (qObj == null) {
            throw new IllegalArgumentException("The QObject qObj must be non-null");
        }
        return (myList.contains(qObj));
    }

    /**
     * Returns true if this queue contains all of the elements in the specified
     * collection WARNING: The collection should contain references to QObject's
     * otherwise it will certainly return false.
     * <p>
     * Throws an IllegalArguementException if the Collection is null
     *
     * @param c Collection c of items to check
     * @return True if the queue contains all of the elements.
     */
    public final boolean contains(Collection<T> c) {
        if (c == null) {
            throw new IllegalArgumentException("The Collection c must be non-null");
        }
        return (myList.containsAll(c));
    }

    /**
     * Returns the index in this queue of the first occurrence of the specified
     * element, or -1 if the queue does not contain this element. More formally,
     * returns the lowest index i such that (o==null ? get(i)==null :
     * o.equals(get(i))), or -1 if there is no such index.
     * <p>
     * Throws an IllegalArgumentException if QObject qObj is null.
     *
     * @param qObj The object to be found
     * @return The index (zero based) of the element or -1 if not found.
     */
    public final int indexOf(T qObj) {
        if (qObj == null) {
            throw new IllegalArgumentException("The QObject qObj must be non-null");
        }
        return (myList.indexOf(qObj));
    }

    /**
     * Returns the index in this queue of the last occurrence of the specified
     * element, or -1 if the queue does not contain this element. More formally,
     * returns the lowest index i such that (o==null ? get(i)==null :
     * o.equals(get(i))), or -1 if there is no such index.
     * <p>
     * Throws an IllegalArgumentException if QObject qObj is null.
     *
     * @param qObj The object to be found
     * @return The (zero based) index or -1 if not found.
     */
    public final int lastIndexOf(T qObj) {
        if (qObj == null) {
            throw new IllegalArgumentException("The QObject qObj must be non-null");
        }
        return (myList.lastIndexOf(qObj));
    }

    /**
     * Finds all the QObjects in the Queue that satisfy the condition and adds
     * them to the foundItems collection
     *
     * @param condition the condition of the search
     * @param foundItems the items found
     * @return yields true if at least one was found, false otherwise
     */
    public final boolean find(Predicate<T> condition, Collection<T> foundItems) {
        boolean found = false;
        for (T qo : myList) {
            if (condition.test(qo)) {
                found = true;
                foundItems.add(qo);
            }
        }
        return (found);
    }

    /**
     * Finds the first QObject whose getQueuedObject().equals(object)
     *
     * @param object the object to look for
     * @return null if no QObject is found
     */
    public final T find(Object object) {
        for (T qo : myList) {
            if (qo.getAttachedObject().equals(object)) {
                return (qo);
            }
        }
        return (null);
    }

    /**
     * Finds all QObjects whose getQueuedObject().equals(object)
     *
     * @param foundQObjects all QObjects whose getQueuedObject().equals(object)
     * @param object the object to search
     * @return returns true if at least one match is found
     */
    public final boolean find(Collection<T> foundQObjects, Object object) {
        boolean found = false;
        for (T qo : myList) {
            if (qo.getAttachedObject().equals(object)) {
                foundQObjects.add(qo);
                found = true;
            }
        }
        return (found);
    }

    /**
     * Finds and removes all the QObjects in the Queue that satisfy the
     * condition and adds them to the deletedItems collection. Waiting time
     * statistics are automatically collected
     *
     * @param condition The condition to check
     * @param deletedItems Holds the items that were removed from the Queue
     * @return yields true if at least one was deleted, false otherwise
     */
    public final boolean remove(Predicate<T> condition, Collection<T> deletedItems) {
        return (remove(condition, deletedItems, true));
    }

    /**
     * Finds and removes all the QObjects in the Queue that satisfy the
     * condition and adds them to the deletedItems collection
     *
     * @param condition The condition to check
     * @param deletedItems Holds the items that were removed from the Queue
     * @param waitStats indicates whether or not waiting time statistics should
     * be collected
     * @return yields true if at least one was deleted, false otherwise
     */
    public final boolean remove(Predicate<T> condition, Collection<T> deletedItems, boolean waitStats) {
        boolean found = false;
        for (int i = 0; i < myList.size(); i++) {
            T qo = myList.get(i);
            if (condition.test(qo)) {
                found = true;
                deletedItems.add(qo);
                remove(qo, waitStats);
            }
        }
        return (found);
    }

    /**
     * Removes the first occurrence in the queue of the specified element
     * Automatically collects waiting time statistics and number in queue
     * statistics. If the queue does not contain the element then it is
     * unchanged and false is returned
     * <p>
     * Throws an IllegalArgumentException if QObject qObj is null.
     *
     * @param qObj
     * @return true if the item was removed
     */
    public final boolean remove(T qObj) {
        return (remove(qObj, true));
    }

    /**
     * Removes the first occurrence in the queue of the specified element
     * Automatically collects waiting time statistics and number in queue
     * statistics. If the queue does not contain the element then it is
     * unchanged and false is returned
     * <p>
     * Throws an IllegalArgumentException if QObject qObj is null.
     *
     * @param qObj The object to be removed
     * @param waitStats Indicates whether waiting time statistics should be
     * collected on the removed item, true means collect statistics
     * @return True if the item was removed.
     */
    public final boolean remove(T qObj, boolean waitStats) {
        if (qObj == null) {
            throw new IllegalArgumentException("The QObject qObj must be non-null");
        }

        if (myList.remove(qObj)) {
            if (waitStats) {
                myStatus = Status.DEQUEUED;
            } else {
                myStatus = Status.IGNORE;
            }
            qObj.exitQueue(getTime());
            notifyQueueListeners(qObj);
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Removes the element at the specified position in this queue. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     * <p>
     * Automatically, collects the time in queue for the item and includes it in
     * the time in queue response variable.
     * <p>
     * Automatically, updates the number in queue response variable.
     * <p>
     * Throws an IndexOutOfBoundsException if the specified index is out of
     * range {@literal (index < 0 || index >= size())}.
     *
     * @param index - the index of the element to be removed.
     * @return the element previously at the specified position
     */
    public final T remove(int index) {
        return (remove(index, true));
    }

    /**
     * Removes the element at the specified position in this queue. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     * <p>
     * Automatically, collects number in queue statistics. If waitStats flag is
     * true, then automatically collects the time in queue for the item and
     * includes it in the time in queue response variable.
     * <p>
     * Throws an IndexOutOfBoundsException if the specified index is out of
     * range {@literal (index < 0 || index >= size())}.
     *
     * @param index - the index of the element to be removed.
     * @param waitStats - true means collect waiting time statistics, false
     * means do not
     * @return the element previously at the specified position
     */
    public final T remove(int index, boolean waitStats) {
        T qObj = myList.remove(index);
        if (waitStats) {
            myStatus = Status.DEQUEUED;
        } else {
            myStatus = Status.IGNORE;
        }
        qObj.exitQueue(getTime());
        notifyQueueListeners(qObj);
        return (qObj);
    }

    /**
     * Removes the QObject at the front of the queue Uses remove(int index)
     * where index = 0
     *
     * @return The first QObject in the queue or null if the list is empty
     */
    public final T removeFirst() {
        if (myList.isEmpty()) {
            return (null);
        } else {
            return (remove(0));
        }
    }

    /**
     * Removes the QObject at the last index in the queue. Uses remove(int
     * index) where index is the size of the list - 1
     *
     * @return The last QObject in the queue or null if the list is empty
     */
    public final T removeLast() {
        if (myList.isEmpty()) {
            return (null);
        } else {
            return remove(myList.size() - 1);
        }
    }

    /**
     * Returns the QObject at the front of the queue Depending on the queue
     * discipline this may not be the next QObject
     *
     * @return The first QObject in the queue or null if the list is empty
     */
    public final T peekFirst() {
        if (myList.isEmpty()) {
            return (null);
        } else {
            return myList.get(0);
        }
    }

    /**
     * Returns the QObject at the last index in the queue.
     *
     * @return The last QObject in the queue or null if the list is empty
     */
    public final T peekLast() {
        if (myList.isEmpty()) {
            return (null);
        } else {
            return myList.get(myList.size() - 1);
        }
    }

    /**
     * Returns the QObject at the supplied index in the queue.
     * <p>
     * Throws an IndexOutOfBoundsException if the specified index is out of
     * range {@literal (index < 0 || index >= size())}.
     *
     * @param index the index to inspect
     * @return The QObject at index in the queue or null if the list is empty
     */
    public final T peekAt(int index) {
        if (myList.isEmpty()) {
            return (null);
        } else {
            return myList.get(index);
        }
    }

    /**
     * Removes from this queue all the elements that are contained in the
     * specified collection The collection should contain references to objects
     * of type QObject that had been enqueued in this queue; otherwise, nothing
     * will be removed.
     * <p>
     * Automatically, updates the number in queue variable and time in queue
     * statistics on removed items
     * <p>
     * Throws an IllegalArguementException if the Collection is null
     *
     * @param c The collection containing the QObject's to remove
     * @return true if the queue changed as a result of the call
     */
    public final boolean removeAll(Collection<T> c) {
        return (removeAll(c, true));
    }

    /**
     * Removes from this queue all the elements that are contained in the
     * specified collection The collection should contain references to objects
     * of type QObject that had been enqueued in this queue; otherwise, nothing
     * will be removed.
     * <p>
     * Automatically, updates the number in queue variable If statFlag is true
     * it automatically collects time in queue statistics on removed items
     * <p>
     * Throws an IllegalArgumentException if the Collection is null
     *
     * @param c The collection containing the QObject's to remove
     * @param statFlag true means collect statistics, false means do not
     * @return true if the queue changed as a result of the call
     */
    public final boolean removeAll(Collection<T> c, boolean statFlag) {
        if (c == null) {
            throw new IllegalArgumentException("The Collection c must be non-null");
        }

        boolean removedFlag = false;
        for (T qObj : c) {
            removedFlag = remove(qObj, statFlag);
        }
        return (removedFlag);
    }

    /**
     * Removes from this queue all the elements that are presented by iterating
     * through this iterator The iterator should be based on a collection that
     * contains references to objects of type QObject that had been enqueued in
     * this queue; otherwise, nothing will be removed.
     * <p>
     * Automatically, updates the number in queue variable and time in queue
     * statistics on removed items
     * <p>
     * Throws an IllegalArgumentException if the Iterator is null
     *
     * @param c The iterator over the collection containing the QObject's to
     * remove
     * @return true if the queue changed as a result of the call
     */
    public final boolean removeAll(Iterator<T> c) {
        return (removeAll(c, true));
    }

    /**
     * Removes from this queue all the elements that are presented by iterating
     * through this iterator The iterator should be based on a collection that
     * contains references to objects of type QObject that had been enqueued in
     * this queue; otherwise, nothing will be removed.
     * <p>
     * Automatically, updates the number in queue variable If statFlag is true
     * it automatically collects time in queue statistics on removed items
     * <p>
     * Throws an IllegalArguementException if the Iterator is null
     *
     * @param c The iterator over the collection containing the QObject's to
     * remove
     * @param statFlag true means collect statistics, false means do not
     * @return true if the queue changed as a result of the call
     */
    public final boolean removeAll(Iterator<T> c, boolean statFlag) {
        if (c == null) {
            throw new IllegalArgumentException("The iterator must be non-null");
        }

        boolean removedFlag = false;
        while (c.hasNext()) {
            T qo = c.next();
            removedFlag = remove(qo, statFlag);
        }

        return (removedFlag);
    }

    /**
     * Removes all of the elements from this collection
     * <p>
     * WARNING: This method DOES NOT record the time in queue for the cleared
     * items if the user wants this functionality, it can be accomplished using
     * the remove(int index) method, while looping through the items to remove
     * Listeners are notified of the queue change with IGNORE
     * <p>
     * This method simply clears the underlying data structure that holds the
     * objects
     */
    public final void clear() {
        for (T qObj : myList) {
            qObj.exitQueue(getTime());
        }
        myList.clear();
        myStatus = Queue.Status.IGNORE;
        notifyQueueListeners(null);
    }

    /**
     * Returns an iterator (as specified by Collection ) over the elements in
     * the queue in proper sequence. The elements will be ordered according to
     * the state of the queue given the specified queue discipline.
     * <p>
     * WARNING: The remove() method is not supported by this iterator. A call to
     * remove() with this iterator will result in an
     * UnsupportedOperationException
     *
     * @return an iterator over the elements in the queue
     */
    @Override
    public final Iterator<T> iterator() {
        return (new QueueListIterator());
    }

    /**
     * Returns an iterator (as specified by Collection ) over the elements in
     * the queue in proper sequence. The elements will be ordered according to
     * the state of the queue given the specified queue discipline.
     * <p>
     * WARNING: The add(), remove(), and set() methods are not supported by this
     * iterator. Calls to these methods will result in an
     * UnsupportedOperationException
     *
     * @return an iterator over the elements in the queue
     */
    public final ListIterator<T> listIterator() {
        return (new QueueListIterator());
    }

    /**
     * Gets the size (number of elements) of the queue.
     *
     * @return The number of items in the queue.
     */
    public final int size() {
        return (myList.size());
    }

    /**
     * Returns whether or not the queue is empty.
     *
     * @return True if the queue is empty.
     */
    public final boolean isEmpty() {
        return (myList.isEmpty());
    }

    /**
     * Returns true if the queue is not empty
     *
     * @return true if the queue is not empty
     */
    public final boolean isNotEmpty() {
        return (!isEmpty());
    }

    /**
     * Notifies any listeners that the queue changed
     *
     * @param qObject The qObject associated with the notification
     */
    protected void notifyQueueListeners(T qObject) {
        if (myQueueListeners == null) {
            return;
        }

        for (QueueListenerIfc<T> ql : myQueueListeners) {
            ql.update(qObject);
        }
    }

    /**
     * If the Queue uses randomness, this method will return a RandomElementIfc
     * that can be used to control the randomness according to the returned
     * interface.
     *
     * @return an Optional containing a RandomElementIfc or null
     */
    public Optional<RandomElementIfc> getRandomness() {
        return myDiscipline.getRandomness();
    }

    private class QueueListIterator implements ListIterator<T> {

        protected ListIterator<T> myIterator;

        protected QueueListIterator() {
            myIterator = myList.listIterator();
        }

        @Override
        public void add(T o) {
            throw new UnsupportedOperationException("The method add() is not supported for Queue iteration");
        }

        @Override
        public boolean hasNext() {
            return myIterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return myIterator.hasPrevious();
        }

        @Override
        public T next() {
            return myIterator.next();
        }

        @Override
        public int nextIndex() {
            return myIterator.nextIndex();
        }

        @Override
        public T previous() {
            return myIterator.previous();
        }

        @Override
        public int previousIndex() {
            return myIterator.previousIndex();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("The method remove() is not supported for Queue iteration");
        }

        @Override
        public void set(T o) {
            throw new UnsupportedOperationException("The method set() is not supported for Queue iteration");
        }
    }

    abstract private class QueueDiscipline {

        /**
         * Adds the specified element to the proper location in the supplied
         * list.
         *
         * @param qObject The element to be added to the supplied list
         */
        abstract protected void add(T qObject);

        /**
         * Returns a reference to the next QObjectIfc to be removed from the
         * queue. The item is not removed from the list.
         *
         * @return The QObject that is next, or null if the list is empty
         */
        abstract protected T peekNext();

        /**
         * Removes the next item from the supplied list according to the
         * discipline
         *
         * @return A reference to the QObject item that was removed or null
         * if the list is empty
         */
        abstract protected T removeNext();

        /**
         * Provides a "hook" method to be called when switching from one
         * discipline to another The implementor should use this method to
         * ensure that the underlying queue is in a state that allows it to be
         * managed by this queue discipline
         * <p>
         */
        protected void switchDiscipline() {
        }

        /**
         * Changes the priority of the QObject. Must also re-order the Queue as
         * necessary
         *
         * @param qObject the qObject
         * @param priority the priority to set to
         */
        protected void changePriority(QObject qObject, int priority) {
            qObject.setPriority_(priority);
        }

        public Optional<RandomElementIfc> getRandomness() {
            return Optional.empty();
        }

        protected void beforeExperiment() {
        }

        protected void afterReplication() {
        }

    }

    private class FIFODiscipline extends QueueDiscipline {

        @Override
        protected void add(T qObject) {
            myList.add(qObject);
        }

        @Override
        protected T peekNext() {
            if (myList.isEmpty()) {
                return null;
            }
            return myList.get(0);
        }

        @Override
        protected T removeNext() {
            if (myList.isEmpty()) {
                return null;
            }
            return myList.remove(0);
        }

    }

    private class LIFODiscipline extends QueueDiscipline {

        @Override
        protected void add(T qObject) {
            myList.add(qObject);
        }

        @Override
        protected T peekNext() {
            if (myList.isEmpty()) {
                return null;
            }
            return myList.get(myList.size() - 1);
        }

        @Override
        protected T removeNext() {
            if (myList.isEmpty()) {
                return null;
            }
            return myList.remove(myList.size() - 1);
        }

    }

    private class RankedDiscipline extends QueueDiscipline {

        @Override
        protected void add(T qObject) {

            // nothing in queue, just add it, and return
            if (myList.isEmpty()) {
                myList.add(qObject);
                return;
            }

            // might as well check for worse case, if larger than the largest then put it at the end and return
            if (qObject.compareTo(myList.get(myList.size() - 1)) >= 0) {
                myList.add(qObject);
                return;
            }

            // now iterate through the list
            for (ListIterator<T> i = myList.listIterator(); i.hasNext();) {
                if (qObject.compareTo(i.next()) < 0) {
                    // next() move the iterator forward, if it is < what was returned by next(), then it
                    // must be inserted at the previous index
                    myList.add(i.previousIndex(), qObject);
                    return;
                }
            }

        }

        @Override
        protected T peekNext() {
            if (myList.isEmpty()) {
                return null;
            }
            return myList.get(0);
        }

        @Override
        protected T removeNext() {
            if (myList.isEmpty()) {
                return null;
            }
            return myList.remove(0);
        }

        /**
         * Since regardless of the former queue discipline, the ranked queue
         * discipline must ensure that the underlying queue is in a ranked state
         * after the change over.
         * <p>
         */
        @Override
        protected void switchDiscipline() {
            Collections.sort(myList);
        }

        @Override
        protected void changePriority(QObject qObject, int priority) {
            super.changePriority(qObject, priority);
            Collections.sort(myList);
        }

    }

    private class RandomDiscipline extends QueueDiscipline implements RandomElementIfc {

        private boolean myResetStartStreamOption = true;
        private boolean myResetNextSubStreamOption = true;
        private int myNext;
        private RNStreamIfc myStream = JSLRandom.nextRNStream();

        @Override
        protected void add(T qObject) {
            myList.add(qObject);
        }

        @Override
        protected T peekNext() {

            if (myList.isEmpty()) {
                return (null);
            }

            if (myList.size() == 1) {
                myNext = 0;
            } else {
                myNext = myStream.randInt(0, myList.size() - 1);
            }
            return myList.get(myNext);// randomly pick it from the range available
        }

        @Override
        protected T removeNext() {

            if (myList.isEmpty()) {
                return (null);
            }

            peekNext(); // sets the next randomly

            return myList.remove(myNext); // now returns the next
        }

        @Override
        public Optional<RandomElementIfc> getRandomness() {
            return Optional.of(this);
        }

        @Override
        public void advanceToNextSubstream() {
            myStream.advanceToNextSubstream();
        }

        @Override
        public void resetStartStream() {
            myStream.resetStartStream();
        }

        @Override
        public void resetStartSubstream() {
            myStream.resetStartSubstream();
        }

        @Override
        public void setAntitheticOption(boolean flag) {
            myStream.setAntitheticOption(flag);
        }

        @Override
        public boolean getAntitheticOption() {
            return myStream.getAntitheticOption();
        }

        @Override
        public final boolean getResetStartStreamOption() {
            return myResetStartStreamOption;
        }

        @Override
        public final void setResetStartStreamOption(boolean b) {
            myResetStartStreamOption = b;
        }

        @Override
        public final boolean getResetNextSubStreamOption() {
            return myResetNextSubStreamOption;
        }

        @Override
        public final void setResetNextSubStreamOption(boolean b) {
            myResetNextSubStreamOption = b;
        }


        @Override
        protected void beforeExperiment() {
            if (getResetStartStreamOption()) {
                myStream.resetStartStream();
            }
        }

        @Override
        protected void afterReplication() {
            if (getResetNextSubStreamOption()) {
                myStream.advanceToNextSubstream();
            }
        }

        @Override
        public RNStreamIfc getRandomNumberStream() {
            return myStream;
        }

        @Override
        public void setRandomNumberStream(RNStreamIfc stream) {
            Objects.requireNonNull(stream,"The supplied stream was null");
            myStream = stream;
        }
    }
}
