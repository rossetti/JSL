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

import jsl.simulation.ModelElement;
import jsl.simulation.ModelElementState;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.utilities.misc.OrderedList;

import java.util.*;

/** A Resource has a given capacity of units that can be requested and allocated.
 * 
 *  A Resource can be in any of four states (Busy, Idle, Failed, Inactive).
 *  A resource is in the failed state if it is not in the other states and has
 *  become unavailable because of a failure.
 *  A resource is in the inactive state if it is not in the other states and
 *  has become unavailable because of a capacity change.
 *  A resource is in the idle state if it is not in the other states and has
 *  (some) units available for use.
 *  A resource is in the busy state if it is not in the other states and it
 *  has no available units for use.
 *
 *  Units of a resource are allocated to an instance of Entity via the allocate() method.
 *  When units of a resource are allocated, an instance of an Allocation is created.
 *  An allocation notes that an entity has so many units of a resource.
 * 
 *  When all the units of a resource have been allocated, it is considered busy
 *  (as a whole). If it is failed, it is considered failed as a whole, i.e.
 *  no units are available.  If it is inactive, it is considered inactive as a
 *  whole (i.e. no units are available).
 *
 *  Units may be obtained from a resource via the seize() method. If enough
 *  units are available to fill the request the required number of units are
 *  allocated to the request.  If the request cannot be totally filled, it
 *  waits until additional units become available. The default waiting protocol
 *  is determined by the priority of the request and the order of seize attempts.
 *  A resource cannot allocate more units than its currently
 *  available capacity.  Resources are intimately tied to the Request class.
 *  If units of a resource are allocated, they are associated with the request.
 *  The only way to return the units of the resource is to use the instance of
 *  Request and the release() methods of Resource. An instance of a Request
 *  can only be using the units of one resource at a time.  A request
 *  might not have all of its requested units allocated, see allocate(Request, int)
 *  because of partial filling.
 *
 *  NOTE: failed and inactive are not currently implemented within this package
 */
public class Resource extends SchedulingElement implements SeizeIfc {

    /** The busy state, keeps track of when all units are busy
     *
     */
    private ModelElementState myBusyState;

    /** The idle state, keeps track of when there are idle units
     *  i.e. if any unit is idle then the resource as a whole is
     *  considered idle
     */
    private ModelElementState myIdleState;

    /** The failed state, keeps track of when no units
     *  are available because the resource is failed
     *
     */
    private ModelElementState myFailedState;

    /** The inactive state, keeps track of when no units
     *  are available because the resource is inactive
     */
    private ModelElementState myInactiveState;

    /** The current state of the resource
     *
     */
    private ModelElementState myState;

    /** The previous state of the resource
     *
     */
    private ModelElementState myPrevState;

    /** The listeners for state changes on the resource
     * 
     */
    private Set<ResourceStateChangeListenerIfc> myStateChangeListeners;

    /**
     *  The maximum possible capacity at any time t.
     *  Since the capacity of the resource can vary with time
     *  This variable represents the maximum value
     *  for the capacity. Any request for the resource
     *  cannot exceed this amount, since it can never be
     *  satisfied at any time t
     */
    private int myMaxCapacity;//TODO not sure if needed

    /**
     *  The initial capacity of the resource at
     *  time just prior to 0.0
     */
    private int myInitialCapacity;

    /**
     *  The capacity of the resource at time any time t
     */
    private int myCapacity;//TODO consider a TimeWeighted

    /** Counts the number of times that the resource
     *  has been seized
     *
     */
//    private int myNumTimesSeized;//TODO could be a Counter
    /** Counts the number of requests that the
     *  resource received
     */
    private int myNumRequestsReceived;

    /**
     *  The current number of busy units for the resource
     */
    private TimeWeighted myNumBusy;

    /**
     *  The allocations that currently using 
     *  this resource
     */
    protected List<Allocation> myAllocations;

    /** Holds requests that are waiting for some
     *  units of the resource
     *
     */
    protected OrderedList<Request> myWaitingRequests;

    /** The set of resource sets currently holding this resource
     */
    protected Set<ResourceSet> myResourceSets;

    public Resource(ModelElement parent) {
        this(parent, 1, null);
    }

    public Resource(ModelElement parent, String name) {
        this(parent, 1, name);
    }

    public Resource(ModelElement parent, int initialCapacity) {
        this(parent, initialCapacity, null);
    }

    public Resource(ModelElement parent, int initialCapacity, String name) {
        super(parent, name);
        setInitialCapacity(initialCapacity);
        myAllocations = new LinkedList<Allocation>();
        myWaitingRequests = new OrderedList<Request>();

        myBusyState = new ModelElementState(this, getName() + " _Busy");
        myIdleState = new ModelElementState(this, getName() + " _Idle");
        myFailedState = new ModelElementState(this, getName() + " _Failed");
        myInactiveState = new ModelElementState(this, getName() + " _Inactive");
        // assume no units are busy and initial state is idle
        myNumBusy = new TimeWeighted(this, getName() + " #Busy Units");
        myState = myIdleState;
        myPrevState = null;
    }

    @Override
    protected void initialize() {//TODO finish initialize
        myNumRequestsReceived = 0;
        for (Allocation a : myAllocations) {
            a.nullify();
        }
        myAllocations.clear();
        for (Request r : myWaitingRequests) {
            r.nullify();
        }
        myWaitingRequests.clear();
        // start of replication with full capacity (no busy units)
        myCapacity = getInitialCapacity();
        // assume start at time just prior to 0.0 that resource is in idle state
        myPrevState = null;
        myState = myIdleState;
        myState.enter();
        notifyResourceSets();
        notifyStateChangeListeners();
    }

    public void detachStateChangeListener(ResourceStateChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null");
        }
        if (myStateChangeListeners == null) {
            return;
        }
        myStateChangeListeners.remove(listener);
    }

    public void attachStateChangeListener(ResourceStateChangeListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null");
        }
        if (myStateChangeListeners == null) {
            myStateChangeListeners = new LinkedHashSet<ResourceStateChangeListenerIfc>();
        }

        myStateChangeListeners.add(listener);
    }

    protected void setState(ModelElementState state) {
        myPrevState = myState;
        myState.exit();
        myState = state;
        myState.enter();
        notifyResourceSets();
        notifyStateChangeListeners();
    }

    protected void notifyResourceSets() {
        if (myResourceSets == null) {
            return;
        }

        for (ResourceSet s : myResourceSets) {
            if ((myPrevState == myIdleState) && (myState != myIdleState)) {
                s.resourceBecameUnavailable(this);
            } else if ((myPrevState != myIdleState) && (myState == myIdleState)) {
                s.resourceBecameAvailable(this);
            }
        }

        if ((myPrevState != myIdleState) && (myState == myIdleState)) {
            for (ResourceSet s : myResourceSets) {
                s.processWaitingRequests();
            }
        }

    }

    protected void notifyStateChangeListeners() {
        if (myStateChangeListeners == null) {
            return;
        }
        for (ResourceStateChangeListenerIfc listener : myStateChangeListeners) {
            listener.resourceStateChange(this);
        }
    }

    protected final void attachResourceSet(ResourceSet set) {
        if (set == null) {
            throw new IllegalArgumentException("The set was null");
        }
        if (myResourceSets == null) {
            myResourceSets = new LinkedHashSet<ResourceSet>();
        }

        myResourceSets.add(set);
    }

    protected final void detachResourceSet(ResourceSet set) {
        if (set == null) {
            throw new IllegalArgumentException("The set was null");
        }
        if (myResourceSets == null) {
            return;
        }
        myResourceSets.remove(set);
    }

    protected void addAllocation(Allocation a) {
        myAllocations.add(a);
    }

    protected void removeAllocation(Allocation a) {
        myAllocations.remove(a);
    }

    protected void incrementNumberBusy(int amt) {
        // increase value and check state
        myNumBusy.increment(amt);
        // if the state is not idle it must be failed or inactive
        // it should stay failed or inactive
        // if the state is idle, check if increase
        // in number of busy made the resource busy
        if (isIdle()) {
            if (getCapacity() == (int) myNumBusy.getValue()) {
                setState(myBusyState);
            }
        }

    }

    protected void decrementNumberBusy(int amt) {
        // decrease value and check state
        myNumBusy.decrement(amt);
        // if the state is not busy it must be failed, inactive, or idle
        // it should stay failed, inactive, or idle
        // if the state is busy, check if decrease
        // in number of busy made the resource idle
        if (isBusy()) {
            if ((int) myNumBusy.getValue() < getCapacity()) {
                setState(myIdleState);
            }
        }

    }

    /** Sets the initial capacity of the resource. This only changes it for
     *  when the resource is initialized.
     *
     * @param capacity
     */
    protected void setMaximumCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Attempted to set resource capacity less or equal to zero!");
        }
        myMaxCapacity = capacity;
    }

    /** Creates a new Entity and allocates 1 unit of the resource to it.
     *  Conditions for using this method include:
     *  1) getNumberAvailable() must be > 0
     *  The returned Allocation can be used to access the newly created
     *  entity.
     * 
     * @return
     */
 //   public Allocation allocate() {
 //       return allocate(createEntity());
 //   }

    /** Allocates one unit to the supplied entity. Conditions
     *  for using this method include:
     *  1) entity must not be null
     *  2) getNumberAvailable() must be &gt; 0
     *
     * @param entity

     * @return The Allocation that associates the entity with this resource
     */
    public Allocation allocate(Entity entity) {
        return allocate(entity, 1);
    }

    /** Allocates the amount indicated to the supplied entity. Conditions
     *  for using this method include:
     *  1) entity must not be null
     *  2) the amount to allocate must be &gt; 0
     *  3) the amount to allocate must be &lt;= getNumberAvailable()
     *
     * @param entity
     * @param amtToAllocate
     * @return The Allocation that associates the entity with this resource
     */
    public Allocation allocate(Entity entity, int amtToAllocate) {
        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null!");
        }

        if (amtToAllocate <= 0) {
            throw new IllegalArgumentException("The amount to allocate was <= 0 !");
        }

        if (amtToAllocate > getNumberAvailable()) {
            throw new IllegalArgumentException("The amount was for more than the number of available units for this resource.");
        }

        Allocation a = null;
        if (entity.containsAllocation(this)) {
            // units were previously allocated to the entity
            // look up the allocation
            a = entity.getAllocation(this);
        } else {
            // no previous allocation, need to make new allocation
            a = new Allocation(entity, this);
            addAllocation(a);
        }
        // add to the amount allocated
        incrementNumberBusy(amtToAllocate);
        a.increaseAllocation(amtToAllocate);

        return a;
    }

    /** Adds the request to the list of waiting requests
     *  based on the Comparable interface for Request
     *
     * @param request
     */
    protected void addWaitingRequest(Request request) {
        myWaitingRequests.add(request);
    }

    /** Removes the request from the list of waiting requests
     *
     * @param request
     */
    protected void removeWaitingRequest(Request request) {
        myWaitingRequests.remove(request);
    }

    /** Returns the next request without removing it
     *
     * @return
     */
    protected Request peekNextRequest() {
        return myWaitingRequests.peekNext();
    }

    /** Removes the next request from the waiting list of requests
     *
     * @return
     */
    protected Request removeNextRequest() {
        return myWaitingRequests.removeNext();
    }

    /** Sub-classes can implement a mechanism to give
     *  a partially fillable request, some units of the
     *  resource.  The default is not to partially fill.
     *  In other words, nothing happens in this method.
     *  Implementors must ensure:
     *  1) the resource that request can eventually be
     *     satisfied by the resource
     *  2) the same resource is used to give all desired
     *     units to the request
     *
     * @param request
     */
    protected void partiallyAllocate(Request request) {
    }

    /** Seizes the resource using the request.
     *  Conditions:
     *  1) request must not be null
     *  2) request.getEntity() must not be null
     *  3) The request must not have been seized with another resource
     *  4) The request must have a ResourceAllocationListener attached.
     *
     * @param request
     */
    @Override
    public final void seize(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("The supplied request was null!");
        }

        if (request.getEntity() == null) {
            throw new IllegalArgumentException("The supplied request is not associated with an entity");
        }

        if ((request.getSeizedResource() != this) && (request.getSeizedResource() != null)) {
            throw new IllegalArgumentException("The supplied request seized on a different resource");
        }

        if (!request.hasResourceAllocationListener()) {
            throw new IllegalArgumentException("No ResourceAllocationListenerIfc supplied for the request");
        }

        myNumRequestsReceived = myNumRequestsReceived + 1;
        request.setArrivalNumber(myNumRequestsReceived);
        request.setSeizedResource(this);

        // always add the request to the waiting list
        addWaitingRequest(request);

        // the arriving request might not be the next request
        // because of priority or other list processing rules
        if (hasAvailableUnits()) {
            if (request == peekNextRequest()) {
                //request is next, can attempt to process it
                if (request.getAmountNeeded() <= getNumberAvailable()) {
                    // request can be fully satisfied
                    removeNextRequest();
                    //give request all that it needs
                    //allocation listener handles the allocation
                    request.setAllocateAmount(request.getAmountNeeded());
                } else {
                    // request must continue to wait, but
                    // might be partially filled
                    if (request.allowsPartialFilling()) {
                        partiallyAllocate(request);
                    }
                }
            }
        }

    }

    /** Seizes the resource and associates the entity and listener with
     *  the request. No partial filling, 1 unit, default seize priority
     *
     * @param entity
     * @param listener
     * @return
     */
    public final Request seize(Entity entity, AllocationListenerIfc listener) {
        return seize(entity, 1, Request.DEFAULT_PRIORITY, false, listener);
    }

    /** Seizes the resource and associates the entity and listener with
     *  the request. No partial filling
     *
     * @param entity
     * @param amtNeeded
     * @param priority
     * @param listener
     * @return
     */
    public final Request seize(Entity entity, int amtNeeded, int priority,
            AllocationListenerIfc listener) {
        return seize(entity, amtNeeded, priority, false, listener);
    }

    /** Seizes the resource and associates the entity and listener with
     *  the request. Default seize priority and no partial filling
     *
     * @param entity
     * @param amtNeeded
     * @param listener
     * @return
     */
    public final Request seize(Entity entity, int amtNeeded, AllocationListenerIfc listener) {
        return seize(entity, amtNeeded, Request.DEFAULT_PRIORITY, false, listener);
    }

    /** Seizes the resource and associates the entity and listener with
     *  the request
     *
     * @param entity
     * @param amtNeeded
     * @param priority
     * @param partialFillFlag
     * @param listener
     * @return
     */
    public final Request seize(Entity entity, int amtNeeded, int priority,
            boolean partialFillFlag, AllocationListenerIfc listener) {

        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null!");
        }

        Request request = new Request(amtNeeded, priority, partialFillFlag);
        request.setResourceAllocationListener(listener);
        request.setEntity(entity);
        seize(request);

        return request;
    }

    /** Releases allocation, calls release(a.getEntity(), a.getAmountAllocated())
     *
     * @param a
     */
    public final void release(Allocation a) {
        if (a == null) {
            throw new IllegalArgumentException("The supplied allocation was null");
        }

        release(a.getEntity(), a.getAmountAllocated());
    }

    /** Releases all of the units of this resource that were
     *  previously allocated to the supplied entity.
     *
     *  1) The supplied entity must not be null.
     *  2) The supplied entity must have units of this resource allocated
     *     to it
     *
     * @param entity
     * @return
     */
    public final Allocation release(Entity entity) {

        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null!");
        }

        if (!entity.containsAllocation(this)) {
            throw new IllegalArgumentException("The supplied entity has no allocation from this resource!");
        }

        Allocation a = entity.getAllocation(this);

        return release(entity, a.getAmountAllocated());
    }

    /** Releases the specified number of units of this resource that were
     *  previously allocated to the supplied entity.
     *
     *  1) The supplied entity must not be null.
     *  2) The supplied entity must have units of this resource allocated
     *     to it
     *  3) The release amount must be &gt; 0 and &lt;= the amount allocated to
     *     the entity
     *
     * @param entity
     * @param releaseAmount
     * @return
     */
    public final Allocation release(Entity entity, int releaseAmount) {
        if (releaseAmount <= 0) {
            throw new IllegalArgumentException("The amount being release is <=0!");
        }

        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null!");
        }

        if (!entity.containsAllocation(this)) {
            throw new IllegalArgumentException("The supplied entity has no allocation from this resource!");
        }

        Allocation a = entity.getAllocation(this);

        if (releaseAmount > a.getAmountAllocated()) {
            throw new IllegalArgumentException("Tried to release more units than was allocated to the entity");
        }

        a.decreaseAllocation(releaseAmount);

        if (a.isDeallocated()) {
            // remove from resource
            removeAllocation(a);
            // remove from entity
            entity.removeAllocation(a);
            //TODO nullify allocation???
        }

        decrementNumberBusy(releaseAmount);

        processWaitingRequests();

        return a;
    }

    /** Processes any waiting requests. 
     *
     */
    protected void processWaitingRequests() {

        if (!myWaitingRequests.isEmpty()) {
            ListIterator<Request> iterator = myWaitingRequests.listIterator();
            while (iterator.hasNext()) {
                Request r = iterator.next();
                if (r.getAmountNeeded() <= getNumberAvailable()) {
                    // request can be fully satisfied
                    iterator.remove();
                    //give request all that it needs
                    //allocation listener handles the allocation
                    r.setAllocateAmount(r.getAmountNeeded());
                } else {
                    // request must continue to wait
                    // but might get some units
                    if (r.allowsPartialFilling()) {
                        partiallyAllocate(r);
                    }
                }

                // check if no more can be allocated
                if (getNumberAvailable() == 0) {
                    // stop processing requests
                    break;
                }
            }
        }
    }

    @Override
    public String asString(){
        String s = getName() + ": state = " + myState;
        return (s);
    }

    /** Checks if the resource is idle.  
     *
     * @return true if idle, false otherwise
     */
    public final boolean isIdle() {
        return (myState == myIdleState);
    }

    /** Checks to see if the resource is busy
     *
     * @return
     */
    public final boolean isBusy() {
        return (myState == myBusyState);
    }

    /** Checks if the resource is failed
     *
     * @return true if idle, false otherwise
     */
    public final boolean isFailed() {
        return (myState == myFailedState);
    }

    /** Checks to see if the resource is inactive
     *
     * @return
     */
    public final boolean isInactive() {
        return (myState == myInactiveState);
    }

    /** Gets the initial capacity of the resource
     *
     * @return
     */
    public final int getMaximumCapacity() {
        return myMaxCapacity;
    }

    /** Gets the initial capacity of the resource
     *
     * @return
     */
    public final int getInitialCapacity() {
        return myInitialCapacity;
    }

    /** Sets the initial capacity of the resource. This only changes it for
     *  when the resource is initialized.
     *
     * @param capacity
     */
    public void setInitialCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Attempted to set resource capacity less or equal to zero!");
        }
        myInitialCapacity = capacity;
    }

    /** The current capacity of the resource.  The maximum number
     *  of units that are currently scheduled as active
     * @return
     */
    public final int getCapacity() {
        return myCapacity;
    }

    /** Returns the number of units that
     *  are currently available for use
     *
     * @return
     */
    public final int getNumberAvailable() {
        if (isBusy() || isFailed() || isInactive()) {
            return 0;
        } else {
            return getCapacity() - (int) myNumBusy.getValue();
        }
    }

    /** Returns true if getNumberAvailable() &gt; 0
     *
     * @return
     */
    public final boolean hasAvailableUnits() {
        return (getNumberAvailable() > 0);
    }
}
