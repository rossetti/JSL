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
package jsl.modeling.elements.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import jsl.modeling.elements.processview.description.ProcessExecutor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import jsl.modeling.queue.QObject;
import jsl.utilities.GetValueIfc;

/**
 *
 */
public class Entity extends QObject implements EntityReceiverIteratorIfc, List<Entity> {

    /** used to identify the type of entity
     */
    private EntityType myEntityType;

    /** A reference to the entity's current process executor
     */
    private ProcessExecutor myProcessExecutor;

    /** Records the usage (allocation) of units of
     *  a resource to the entity
     */
    private Map<Resource, Allocation> myAllocations;

    /** Records the set of resources being used
     *  by the entity within a ResourceSet
     *
     */
    private Map<ResourceSet, LinkedList<Resource>> myRSetUsage;

    /** Allows the entity to remember which resource was
     *  selected from a ResourceSet
     *
     */
    private Map<String, Resource> mySavedResources;

    /** A map to hold the attached named attributes
     *
     */
    private Map<String, AttributeIfc> myAttributes;

    /** Used to determine the next receiver for the entity
     *
     */
    private EntityReceiverIteratorIfc myEntityReceiverIterator;

    /**  Can be used to attach information/objects to an entity
     */
    private Object myMessage;

    /** Used to indicate whether the entity will supply
     *  the duration of an activity, delay, etc.
     *
     */
    private boolean myUseDurationFlag = false;

    /** The amount of the current duration, this
     *  can be changed as the entity is processed
     */
    private double myDurationTime;

    /** Used to indicate whether the entity will supply
     *  the amount associated with a resource usage
     *
     */
    private boolean myUseAmountFlag = false;

    /** The amount of the current use of a resource if set, this
     *  can be changed as the entity is processed
     */
    private int myAmount = 1;

    /** Holds the receiver that has most recently received
     *  the entity.
     */
    private EntityReceiverAbstract myCurrentReceiver;

    /** Holds the receiver that will be receiving
     *  the entity
     */
    private EntityReceiverAbstract myPlannedReceiver;

    /** Used to mark the time that the entity enters
     *  a receiver
     *
     */
    private double myEnterReceiverTime;

    /** Entities can hold other entities.  This provides a set to hold
     *  the entities.
     * 
     */
    private List<Entity> myEntityList;

    /** Creates an Entity with the given name
     *  and the creation time set to the current simulation time
     * @param entityType 
     * @param name The name of the entity
     */
    protected Entity(EntityType entityType, String name) {
        super(entityType.getTime(), name);
        setType(entityType);
        myEntityList = new LinkedList<Entity>();
        myAllocations = new HashMap<Resource, Allocation>();
    }

    @Override
    public void nullify() {
        super.nullify();
        myCurrentReceiver = null;
        myPlannedReceiver = null;
        myMessage = null;
        myEntityReceiverIterator = null;
        if (myAttributes != null) {
            myAttributes.clear();
        }
        myAttributes = null;

        for (Allocation a : myAllocations.values()) {
            a.nullify();
        }
        myAllocations.clear();
        myAllocations = null;

        if (mySavedResources != null) {
            mySavedResources.clear();
        }

        mySavedResources = null;

        if (myRSetUsage != null) {
            for (LinkedList<Resource> list : myRSetUsage.values()) {
                list.clear();
            }
            myRSetUsage.clear();
        }

        myRSetUsage = null;
        myProcessExecutor = null;
        myEntityType = null;
        myEntityList.clear();
        myEntityList = null;
    }

    /** Returns the time that the entity was last
     *  received by a receiver.  Useful in determining
     *  the total time an entity was at a receiver
     *
     * @return
     */
    public final double getTimeEnteredReceiver() {
        return myEnterReceiverTime;
    }

    /** Used to set the time that the entity last
     *  entered a receiver
     *
     * @param time
     */
    protected final void setTimeEnteredReceiver(double time) {
        myEnterReceiverTime = time;
    }

    /**
     * @return Returns the type.
     */
    public final EntityType getType() {
        return myEntityType;
    }

    /** Gets a reference to the process executor that is
     *  currently executing with the entity.
     * @return A reference to the process executor.
     */
    public final ProcessExecutor getProcessExecutor() {
        return (myProcessExecutor);
    }

    /** Sets the type of the entity.
     *  Side effects:
     *  1. ALL previously supplied attributes will be cleared, even user defined attributes.
     *	   The attributes of the entity will be assigned based on the newly supplied entity type.
     *
     * @param entityType The type to set, must not be null
     */
    protected final void setType(EntityType entityType) {

        if (entityType == null) {
            throw new IllegalArgumentException("The EntityType was null");
        }

        if (myAttributes != null) { // if they exist, we don't need attributes anymore
            myAttributes.clear();
        }

        myEntityType = entityType;

        for (AttributeType at : myEntityType.getAttributeTypes()) {
            Attribute a = new Attribute(at.getName());
            addAttribute(a.getName(), a);
        }

        myEntityReceiverIterator = myEntityType.getSequenceIterator();

    }

    /** Adds the named attribute to the entity as a valid attribute
     *  Arguments must be non-null, or exceptions will be thrown.
     *  The named attribute must not already have been added to the entity
     *  or an exception will be thrown.
     *
     * @param attributeName
     * @param attribute
     */
    public final void addAttribute(String attributeName, AttributeIfc attribute) {
        if (attributeName == null) {
            throw new IllegalArgumentException("Attribute name was null!");
        }

        if (attribute == null) {
            throw new IllegalArgumentException("Attribute was null!");
        }

        if (myAttributes == null) {
            myAttributes = new LinkedHashMap<String, AttributeIfc>();
        }

        if (myAttributes.containsKey(attributeName)) {
            throw new IllegalArgumentException("The Entity already has an "
                    + "attribute named: " + attributeName);
        }

        myAttributes.put(attributeName, attribute);
    }

    /** Returns whether or not the attribute has been named for
     *  this entity
     *
     * @param attributeName
     * @return true if already added, false otherwise
     */
    public final boolean containsAttribute(String attributeName) {
        if (myAttributes == null) {
            return (false);
        }

        if (attributeName == null) {
            return (false);
        } else {
            return (myAttributes.containsKey(attributeName));
        }
    }

    /** Allows the user to set the value of the named attribute
     *  to the given value
     *
     * @param attributeName the attribute name must be present for the entity or an exception is thrown
     * @param value
     */
    public final void setAttributeValue(String attributeName, double value) {
        if (myAttributes == null) {
            throw new IllegalArgumentException("The Entity does not have an "
                    + "attribute named: " + attributeName);
        }

        if (!myAttributes.containsKey(attributeName)) {
            throw new IllegalArgumentException("The Entity does not have an "
                    + "attribute named: " + attributeName);
        }

        AttributeIfc attribute = (AttributeIfc) myAttributes.get(attributeName);
        attribute.setValue(value);
    }

    /** Gets the value of the attribute using the GetValueIfc
     *
     * @param attributeName
     * @return
     */
    public final double getAttributeValue(String attributeName) {
        if (myAttributes == null) {
            throw new IllegalArgumentException("The Entity does not have an "
                    + "attribute named: " + attributeName);
        }

        if (!myAttributes.containsKey(attributeName)) {
            throw new IllegalArgumentException("The Entity does not have an "
                    + "attribute named: " + attributeName);
        }

        AttributeIfc attribute = (AttributeIfc) myAttributes.get(attributeName);
        return (attribute.getValue());
    }

    /** This is used by the ProcessExecutor to tell the entity
     *  what process executor it is currently executing within.
     *
     * @param processExecutor The ProcessExecutor
     */
    public final void setProcessExecutor(ProcessExecutor processExecutor) {
        if (processExecutor == null) {
            throw new IllegalArgumentException("ProcessExecutor was null!");
        }
        myProcessExecutor = processExecutor;
    }

    final void addAllocation(Allocation a) {
        Resource r = a.getAllocatedResource();
        myAllocations.put(r, a);
    }

    final void removeAllocation(Allocation a) {
        Resource r = a.getAllocatedResource();
        myAllocations.remove(r);
    }
    /*
    public void addAllocations(Set<SeizeRequirement> req, Set<Allocation> allocations) {
    if (myRequirements == null) {
    myRequirements = new HashMap<Set<SeizeRequirement>, Set<Allocation>>();
    }
    if (myRequirements.containsKey(req)) {
    throw new IllegalArgumentException("The requirement is already associated with the entity");
    }
    
    myRequirements.put(req, allocations);
    }
    
    public Set<Allocation> removeAllocations(Set<SeizeRequirement> req) {
    if (myRequirements == null) {
    return null;
    }
    return myRequirements.remove(req);
    }
    
    public void release(Set<SeizeRequirement> req) {
    if (req == null) {
    throw new IllegalArgumentException("The supplied set of requirements was null");
    }
    if (!myRequirements.containsKey(req)) {
    throw new IllegalArgumentException("Tried to release a set of requirements " +
    "for the entity does not have a set of allocations for the requirements");
    }
    Set<Allocation> allocations = removeAllocations(req);
    for (Allocation a : allocations) {
    a.releaseResource();
    }
    }
     */

    /** Checks if the entity has any allocations from resources
     *
     * @return
     */
    public final boolean hasAllocations() {
        return !myAllocations.isEmpty();
    }

    /** Checks if the entity has the supplied allocation
     *
     * @param a
     * @return
     */
    public final boolean containsAllocation(Allocation a) {
        return myAllocations.containsValue(a);
    }

    /** Checks of the entity has an allocation for the
     *  supplied resource
     *
     * @param r
     * @return
     */
    public final boolean containsAllocation(Resource r) {
        return myAllocations.containsKey(r);
    }

    /** Gets the allocation for the supplied resource
     *  or null if no allocation exists
     *
     * @param r
     * @return
     */
    public final Allocation getAllocation(Resource r) {
        return myAllocations.get(r);
    }

    protected void setCurrentReceiver(GetEntityReceiverIfc getter) {
        setCurrentReceiver(getter.getEntityReceiver());
    }

    /** Sets the current receiver
     *
     * @param receiver must not be null
     */
    protected void setCurrentReceiver(EntityReceiverAbstract receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("Tried to set the current "
                    + "receiver to null");
        }
        myCurrentReceiver = receiver;
    }

    protected void setPlannedReceiver(GetEntityReceiverIfc getter) {
        setPlannedReceiver(getter.getEntityReceiver());
    }

    /** Sets the current receiver
     *
     * @param receiver must not be null
     */
    protected void setPlannedReceiver(EntityReceiverAbstract receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("Tried to set the planned "
                    + "receiver to null");
        }
        myPlannedReceiver = receiver;
    }

    /** A convenience method that allows an entity to release
     *  a resource that it has previously had allocated.  Releases
     *  all units of the resource that were allocated.
     * 
     *  Throws an IllegalArgumentException if the supplied
     *  resource has not be allocated to the entity
     *
     * @param resource
     */
    public final void release(Resource resource) {
        if (!myAllocations.containsKey(resource)) {
            throw new IllegalArgumentException("Tried to release a resource that"
                    + " was not allocated to the entity");
        }
        Allocation a = getAllocation(resource);
        resource.release(a);
    }

    /**
     * @return Returns the Message.
     */
    public final Object getMessage() {
        return myMessage;
    }

    /**
     * @param message The Message to set.
     */
    public final void setMessage(Object message) {
        myMessage = message;
    }

    /** Returns the duration time.  This can be used by clients
     *  to set a duration time and be used within activities, processes,
     *  time events, etc.
     *
     * @return
     */
    public final double getDurationTime() {
        return myDurationTime;
    }

    /** Sets the duration time.  This can be used by clients
     *  to set a duration time and be used within activities, processes,
     *  time events, etc.
     *
     * @param durationTime
     */
    public final void setDurationTime(double durationTime) {
        myDurationTime = durationTime;
    }

    /** This can be used by clients to indicate whether they
     *  should use getDurationTime().  By default it is false
     *  (do not use the duration time).
     *
     * @return
     */
    public final boolean getUseDurationFlag() {
        return myUseDurationFlag;
    }

    /** This can be used by clients to indicate whether they
     *  should used getDurationTime().  By default it is false
     *  (do not use the duration time).
     * @param flag
     */
    public final void setUseDurationFlag(boolean flag) {
        myUseDurationFlag = flag;
    }

    /** This can be used by clients to set an amount, generally
     *  used when seizing resources.
     *
     * @return
     */
    public final int getAmount() {
        return myAmount;
    }

    /** This can be used by clients to set an amount, generally
     *  used when seizing resources.
     *
     * @param amount must be &gt; 0
     */
    public final void setAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("The amount was <= zero.");
        }
        myAmount = amount;
    }

    /** This can be used by clients to indicate whether they
     *  should used getAmount().  By default it is false
     *  (do not use the getAmount()).
     *
     * @return
     */
    public final boolean getUseAmountFlag() {
        return myUseAmountFlag;
    }

    /** This can be used by clients to indicate whether they
     *  should used getAmount().  By default it is false
     *  (do not use the getAmount()).
     *
     * @param flag
     */
    public final void setUseAmountFlag(boolean flag) {
        myUseAmountFlag = flag;
    }

    /** Returns true if the entity was supplied a receiver
     *  sequence from its entity type
     * 
     * @return
     */
    public final boolean hasReceiverSequence() {
        return (myEntityReceiverIterator == null);
    }

    /** Resets the entity's receiver sequence to the beginning
     *  Returns true if a receiver sequence exists
     * @return
     */
    public final boolean resetReceiverSequence() {
        myEntityReceiverIterator = myEntityType.getSequenceIterator();
        return hasReceiverSequence();
    }

    /** Gets the next receiver for this entity
     *  Returns null if no next receiver
     *
     * @return, the receiver or null if none
     */
    @Override
    public final EntityReceiverAbstract nextEntityReceiver() {
        if (myEntityReceiverIterator == null) {
            return (null);
        } else {
            return (myEntityReceiverIterator.nextEntityReceiver());
        }
    }

    /** Peeks at the next receiver, without advancing to the next receiver
     *  Returns null if no receiver is next
     *
     * @return
     */
    public final EntityReceiverAbstract peekNextEntityReceiver() {
        if (myEntityReceiverIterator == null) {
            return (null);
        } else {
            int index = myEntityReceiverIterator.nextEntityReceiverIndex();
            return (myEntityType.getEntityReceiver(index));
        }
    }

    /** Peeks at the previous receiver, without moving to the previous
     *  Returns null if no previous receiver is available
     *
     * @return
     */
    public final EntityReceiverAbstract peekPreviousEntityReceiver() {
        if (myEntityReceiverIterator == null) {
            return (null);
        } else {
            int index = myEntityReceiverIterator.previousEntityReceiverIndex();
            return (myEntityType.getEntityReceiver(index));
        }
    }

    /** Returns the EntityReceiver at the supplied index for the
     *  EntityType.  Returns null if no sequence or the index
     *  is out of bounds for the sequence.
     *
     * @param index
     * @return
     */
    public final EntityReceiverAbstract getEntityReceiverAt(int index) {
        return (myEntityType.getEntityReceiver(index));
    }

    /** Gets the previous receiver for this entity
     *  Returns null if no previous receiver
     *
     * @return the receiver or null if none
     */
    @Override
    public final EntityReceiverAbstract previousEntityReceiver() {
        if (myEntityReceiverIterator == null) {
            return (null);
        } else {
            return myEntityReceiverIterator.previousEntityReceiver();
        }
    }

    /** Returns the index of the next entity receiver in the iterator
     *  sequence.
     *
     * @return (-1 if no sequence iterator, size() if at end of list
     */
    @Override
    public int nextEntityReceiverIndex() {
        if (myEntityReceiverIterator == null) {
            return -1;
        } else {
            return myEntityReceiverIterator.nextEntityReceiverIndex();
        }
    }

    /** Returns the index of the next entity receiver in the iterator
     *  sequence.
     *
     * @return (-1 if no sequence iterator or at the beginning of the list
     */
    @Override
    public int previousEntityReceiverIndex() {
        if (myEntityReceiverIterator == null) {
            return -1;
        } else {
            return myEntityReceiverIterator.previousEntityReceiverIndex();
        }
    }

    /** Checks if there is a next entity receiver for this entity
     *  Returns false if no next receiver
     *
     * @return true if there is one, false otherwise
     */
    @Override
    public final boolean hasNextEntityReceiver() {
        if (myEntityReceiverIterator == null) {
            return (false);
        } else {
            return myEntityReceiverIterator.hasNextEntityReceiver();
        }
    }

    /** Checks if there is a previous entity receiver for this entity
     *  Returns false if no previous receiver
     *
     * @return true if there is one, false otherwise
     */
    @Override
    public final boolean hasPreviousEntityReceiver() {
        if (myEntityReceiverIterator == null) {
            return (false);
        } else {
            return myEntityReceiverIterator.hasPreviousEntityReceiver();
        }
    }

    /** Gets the receiver that most recently received the entity
     *  This may be null if the entity has never been received
     *
     * @return
     */
    public final EntityReceiverAbstract getCurrentReceiver() {
        return myCurrentReceiver;
    }

    /** Causes the entity to be sent to the receiver. It
     *  arrives instantaneously.
     *
     * @param receiver
     */
    public final void sendViaReceiver(EntityReceiverAbstract receiver) {
        sendViaReceiver(receiver, 0.0);
    }
    
    /** Causes the entity to be sent to the receiver. It arrives after the
     *  specified amount of time
     *
     * @param receiver
     * @param time
     */
    public final void sendViaReceiver(EntityReceiverAbstract receiver,
            GetValueIfc time) {
        myEntityType.sendToReceiver(this, receiver, time.getValue());
    }
    
    /** Causes the entity to be sent to the receiver. It arrives after the
     *  specified amount of time
     *
     * @param receiver
     * @param time
     */
    public final void sendViaReceiver(EntityReceiverAbstract receiver,
            double time) {
        myEntityType.sendToReceiver(this, receiver, time);
    }

    /** Causes the entity to be sent to the next receiver in its sequence
     *  (i.e. to nextEntityReceiver()).
     *  It arrives instantaneously.
     *
     */
    public final void sendViaSequence() {
        sendViaSequence(0.0);
    }

    /** Causes the entity to be sent to the next receiver in its sequence 
     *  (i.e. to nextEntityReceiver()).
     *  It arrives after the specified amount of time
     *
     * @param time
     */
    public final void sendViaSequence(double time) {
        sendViaReceiver(nextEntityReceiver(), time);
    }

    /** Causes the entity to be sent to a destination (receiver)
     *  as specified by its EntityType based on its current
     *  location (receiver)
     *
     */
    public final void sendViaEntityType() {
        myEntityType.sendToDestination(this);
    }

    /** Causes the entity to be disposed.  It can no longer
     *  be used.  The entity must not have any outstanding requests.
     *  This can be checked by using hasAllocations().
     *  If the entity is carrying any entities in its list, those
     *  entities will also be disposed
     *
     */
    public final void dispose() {
        if (hasAllocations()) {
            throw new DisposeEntityException("Attempt to dispose the entity "
                    + "when it still had allocations with resources");
        }

        for(Entity e: myEntityList){
            e.dispose();
        }
//TODO
        /*
        if (myRequirements != null) {
        if (!myRequirements.isEmpty()) {
        throw new DisposeEntityException("Attempt to dispose the entity " +
        "when it still had allocations for requirements");
        }
        }
         */
        myEntityType.dispose(this);
        nullify();
    }

    public List<SeizeRequirement> getSeizeRequirements() {
        throw new UnsupportedOperationException("Not yet implemented");//TODO
    }

    public SortedSet<ReleaseRequirement> getReleaseRequirements() {//TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }
        
    /** This method can be overridden to react to the request receiving
     *  its full allocation of the desired resource
     *
     * @param request
     */
    protected void requestFullyAllocated(Request request) {
    }

    /** This method can be overridden to react to the request releasing its
     *  associated resource
     * 
     * @param request
     */
    protected void requestReleased(Request request) {
    }

    void releaseFirstMemberSeized(ResourceSet set, int releaseAmount) {
        if (!myRSetUsage.containsKey(set)) {
            throw new IllegalArgumentException("Attempted to release from "
                    + "ResourceSet that was not seize");
        }
        LinkedList<Resource> list = myRSetUsage.get(set);
        Resource r = list.peekFirst();
        Allocation a = r.release(this, releaseAmount);
        if (a.isDeallocated()) {
            list.remove(r);
            if (list.isEmpty()) {
                myRSetUsage.remove(set);
                list = null;
            }
        }

    }

    void releaseLastMemberSeized(ResourceSet set, int releaseAmount) {
        if (!myRSetUsage.containsKey(set)) {
            throw new IllegalArgumentException("Attempted to release from "
                    + "ResourceSet that was not seize");
        }
        LinkedList<Resource> list = myRSetUsage.get(set);
        Resource r = list.peekLast();
        Allocation a = r.release(this, releaseAmount);
        if (a.isDeallocated()) {
            list.remove(r);
            if (list.isEmpty()) {
                myRSetUsage.remove(set);
                list = null;
            }
        }
    }

    void releaseSpecificMember(ResourceSet set, String key, int releaseAmount) {
        if (!myRSetUsage.containsKey(set)) {
            throw new IllegalArgumentException("Attempted to release from "
                    + "ResourceSet that was not seized");
        }
        if (!mySavedResources.containsKey(key)) {
            throw new IllegalArgumentException("Attempted to release a specific "
                    + "member from a ResourceSet with invalid save key");
        }
        Resource r = mySavedResources.get(key);
        Allocation a = r.release(this, releaseAmount);
        if (a.isDeallocated()) {
            LinkedList<Resource> list = myRSetUsage.get(set);
            list.remove(r);
            if (list.isEmpty()) {
                myRSetUsage.remove(set);
                list = null;
            }
        }

    }

    void addResourceSet(ResourceSet set, Resource resource) {
        if (myRSetUsage == null) {
            myRSetUsage = new HashMap<ResourceSet, LinkedList<Resource>>();
        }
        if (!myRSetUsage.containsKey(set)) {
            myRSetUsage.put(set, new LinkedList<Resource>());
        }
        LinkedList<Resource> list = myRSetUsage.get(set);
        if (!list.contains(resource)) {
            list.addLast(resource);
        }
    }

    void addResourceKey(String key, Resource resource) {
        if (mySavedResources == null) {
            mySavedResources = new HashMap<String, Resource>();
        }
        if (mySavedResources.containsKey(key)) {
            throw new IllegalArgumentException("Attempted to save a "
                    + "resource to the same key");
        }
        mySavedResources.put(key, resource);
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return myEntityList.toArray(ts);
    }

    @Override
    public Object[] toArray() {
        return myEntityList.toArray();
    }

    @Override
    public List<Entity> subList(int i, int i1) {
        return myEntityList.subList(i, i1);
    }

    @Override
    public int size() {
        return myEntityList.size();
    }

    /** Preconditions: e must not be null, and must not already be in the group
     * 
     * @param i
     * @param e
     * @return 
     */
    @Override
    public Entity set(int i, Entity e) {
        if (e == null) {
            throw new IllegalArgumentException("Attempted to add a null entity to the list of entity, " + this);
        }
        if (myEntityList.contains(e)) {
             throw new IllegalArgumentException("Enity " + e + " already in list of entity, " + this);
        }
        return myEntityList.set(i, e);
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        return myEntityList.retainAll(clctn);
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        return myEntityList.removeAll(clctn);
    }

    @Override
    public Entity remove(int i) {
        return myEntityList.remove(i);
    }

    @Override
    public boolean remove(Object o) {
        return myEntityList.remove(o);
    }

    @Override
    public ListIterator<Entity> listIterator(int i) {
        return myEntityList.listIterator(i);
    }

    @Override
    public ListIterator<Entity> listIterator() {
        return myEntityList.listIterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return myEntityList.lastIndexOf(o);
    }

    @Override
    public Iterator<Entity> iterator() {
        return myEntityList.iterator();
    }

    @Override
    public boolean isEmpty() {
        return myEntityList.isEmpty();
    }

    @Override
    public int indexOf(Object o) {
        return myEntityList.indexOf(o);
    }

    @Override
    public Entity get(int i) {
        return myEntityList.get(i);
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        return myEntityList.containsAll(clctn);
    }

    @Override
    public boolean contains(Object o) {
        return myEntityList.contains(o);
    }

    @Override
    public void clear() {
        myEntityList.clear();
    }

    /** The collection must not have any null elements or any elements that
     *  are already part of the entity's group
     * 
     * @param i
     * @param clctn
     * @return 
     */
    @Override
    public boolean addAll(int i, Collection<? extends Entity> clctn) {
        for (Entity e : clctn) {
            if (e == null) {
                throw new IllegalArgumentException("Attempt to add a null entity to entity, " + getName() + "'s, group");
            }
            if (myEntityList.contains(e)) {
                throw new IllegalArgumentException("Enity " + e.getName() + " already in entity, " + getName() + "'s, group");
            }
        }
        return myEntityList.addAll(i, clctn);
    }

    /** The collection must not have any null elements or any elements that
     *  are already part of the entity's group
     * 
     * @param clctn
     * @return 
     */
    @Override
    public boolean addAll(Collection<? extends Entity> clctn) {
        for (Entity e : clctn) {
            if (e == null) {
                throw new IllegalArgumentException("Attempt to add a null entity to entity, " + getName() + "'s, group");
            }
            if (myEntityList.contains(e)) {
                throw new IllegalArgumentException("Enity " + e.getName() + " already in entity, " + getName() + "'s, group");
            }
        }
        return myEntityList.addAll(clctn);
    }

    /** Adds an entity to the entity's group
     * Preconditions: e must not be null, e must not already be in group
     * @param i 
     * @param e
     */
    @Override
    public void add(int i, Entity e) {
        if (e == null) {
            throw new IllegalArgumentException("Attempt to add a null entity to entity, " + getName() + "'s, group");
        }
        if (myEntityList.contains(e)) {
            throw new IllegalArgumentException("Enity " + e.getName() + " already in entity, " + getName() + "'s, group");
        }
        myEntityList.add(i, e);
    }

    /** Adds an entity to the entity's group
     * Preconditions: e must not be null, e must not already be in group
     * @param e
     * @return 
     */
    @Override
    public boolean add(Entity e) {
        if (e == null) {
            throw new IllegalArgumentException("Attempt to add a null entity to entity, " + getName() + "'s, group");
        }
        if (myEntityList.contains(e)) {
            throw new IllegalArgumentException("Enity " + e.getName() + " already in entity, " + getName() + "'s, group");
        }
        return myEntityList.add(e);
    }
}
