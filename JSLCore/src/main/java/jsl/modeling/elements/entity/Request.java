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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.modeling.elements.entity;

import jsl.simulation.JSLEvent;

/**
 *
 * @author rossetti
 */
public class Request implements Comparable<Request> {

    /** The default priority associated with Requests
     */
    public static final int DEFAULT_PRIORITY = JSLEvent.DEFAULT_PRIORITY;

    /**
     * Unallocated - A state representing when no units of a resource
     * have been allocated to the request
     *
     * PartiallyAllocated - A state representing when units of a resource
     * are allocated to the request
     *
     * FullyAllocated - A state representing when all required units of a resource
     * are allocated to the request
     *
     */
    public enum State {

        Unallocated, PartiallyAllocated, FullyAllocated

    };
    /**
     * The entity making the request.
     */
    private Entity myEntity;

    /** Called whenever units of a resource are allocated to a request
     *
     */
    private AllocationListenerIfc myResourceAllocationListener;

    /** Called whenever units of a resource are released by this request
     *
     */
    private ResourceReleaseListenerIfc myResourceReleaseListener;

    /**
     * The current resource that the request is allocated to, null if not in a
     * resource
     */
    private Resource mySeizedResource;

    /** Indicates the order of arrival to the request's resource
     *
     */
    private int myArrivalNumber;

    /** A priority for use in seizing the resource
     */
    private int myPriority;

    /** Indicates whether or not the request allows the
     *  amount requested to be partially allocated
     */
    private boolean myAllowPartialFillingFlag;

    /**
     * The amount of resource requested
     */
    private int myInitialAmountRequested;

    /**
     * The amount of resource allocated to the request
     */
    private int myAmountAllocated;

    /** Indicates if the amount allocated has reached
     *  the amount requested.  Then the request is completely
     *  allocated and this flag is true.
     */
    private boolean mySatisfiedFlag;

    /** Records the state of the request
     *
     */
    private State myState;

//    private SeizeRequirement mySeizeRequirement;

    /** If the Request is made on a ResourceSet
     *  this attribute holds the pertinent set
     *
     */
    private ResourceSet mySeizedResourceSet;

    /** If the Request is made on a ResourceSet
     *  this attribute holds the pertinent resource selection rule
     *
     */
    private ResourceSelectionRuleIfc myRule;

    private String myResourceSaveKey;

    /** Creates a single unit request
     *  Note: the request is not ready until it has
     *  been used within a resource. No partial filling
     *  Default seize priority
     *
     */
    public Request() {
        this(1, Request.DEFAULT_PRIORITY, false);
    }

    /** Creates a request with the supplied parameters
     *  Note: the request is not ready until it has
     *  been used within a resource. No partial filling
     *  Default seize priority
     *
     * @param amtRequested
     */
    public Request(int amtRequested) {
        this(amtRequested, Request.DEFAULT_PRIORITY, false);
    }

    /** Creates a request with the supplied parameters
     *  Note: the request is not ready until it has
     *  been used within a resource. No partial filling
     *
     * @param amtRequested
     * @param priority
     */
    public Request(int amtRequested, int priority) {
        this(amtRequested, priority, false);
    }

    /** Creates a request with the supplied parameters
     *  Note: the request is not ready until it has
     *  been used within a resource.
     *
     * @param amtRequested
     * @param priority
     * @param allowPartialFilling
     */
    public Request(int amtRequested, int priority,
            boolean allowPartialFilling) {
        setInitialAmountRequested(amtRequested);
        setPriority(priority);
        setPartialFillingOption(allowPartialFilling);
    }

    /** Sets all internal references to null.  Use only
     *  if the request is no longer needed.
     *
     */
    public void nullify() {
        mySeizedResource = null;
        myEntity = null;
        myState = null;
        myResourceAllocationListener = null;
        myResourceReleaseListener = null;
        myRule = null;
        mySeizedResourceSet = null;
//        mySeizeRequirement = null;
    }

    public final void setResourceAllocationListener(AllocationListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener was null");
        }
        myResourceAllocationListener = listener;
    }

    public final boolean hasResourceAllocationListener() {
        return (myResourceAllocationListener != null);
    }

    final void setEntity(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null");
        }
        myEntity = entity;
    }

    final void setPartialFillingOption(boolean option) {
        myAllowPartialFillingFlag = option;
    }

    final void setInitialAmountRequested(int amountRequested) {

        if (amountRequested <= 0) {
            throw new IllegalArgumentException("Amount requested was less or equal to zero!");
        }

        myInitialAmountRequested = amountRequested;
        myAmountAllocated = 0;
    }

    /** Sets the priority for this request
     *  Changing the priority while the request is waiting
     *  has no effect. This priority is only used to
     *  determine the ordering when the request has to initially
     *  wait for a resource
     *
     * @param priority, lower priority implies first to stop waiting
     */
    final void setPriority(int priority) {
        myPriority = priority;
    }

    final void setSeizedResource(Resource resource) {
        mySeizedResource = resource;
        if (mySeizedResourceSet != null){
            myEntity.addResourceSet(mySeizedResourceSet, mySeizedResource);
            if (myResourceSaveKey != null){
                myEntity.addResourceKey(myResourceSaveKey, mySeizedResource);
            }
        }
    }

    final void setSeizedResourceSet(ResourceSet set){
        mySeizedResourceSet = set;
    }

//    final void setSeizeRequirement(SeizeRequirement req){
//        mySeizeRequirement = req;
//    }

    final void setResourceSaveKey(String key){
        myResourceSaveKey = key;
    }

    final void setArrivalNumber(int num) {
        myArrivalNumber = num;
    }

    final void setAllocateAmount(int amtToAllocate) { //TODO

        if (amtToAllocate <= 0) {
            throw new IllegalArgumentException("Amount allocated was <= zero!");
        }

        if (myAmountAllocated + amtToAllocate > myInitialAmountRequested) {
            throw new IllegalArgumentException("Amount allocated was > the amount requested!");
        }

        if ((myAllowPartialFillingFlag == false) && (amtToAllocate != myInitialAmountRequested)) {
            throw new IllegalArgumentException("No partial filling, the amount allocated was != the amount requested!");
        }

        myAmountAllocated = myAmountAllocated + amtToAllocate;

        myState = State.PartiallyAllocated;

        if (myAmountAllocated == myInitialAmountRequested) {
            mySatisfiedFlag = true;
            myState = State.FullyAllocated;
            myEntity.requestFullyAllocated(this);
        }

        if (myResourceAllocationListener != null) {//TODO
            myResourceAllocationListener.allocated(this);
        }

    }

    /** A ResourceReleaseListener can be attached to the request and provide
     *  behavior right after the request has release units of a resource.
     *
     * @param releaseListener
     */
    public final void setResourceReleaseListener(ResourceReleaseListenerIfc releaseListener) {
        myResourceReleaseListener = releaseListener;
    }

    public final int getArrivalNumber() {
        return myArrivalNumber;
    }

    /** Gets the priority associated with the request
     * @return The priority as an int
     */
    public final int getPriority() {
        return (myPriority);
    }

    public String toString() {
        return (super.toString() + " AmtNeeded= " + getAmountNeeded());
    }

    /** The Entity associated with the request
     *
     * @return
     */
    public final Entity getEntity() {
        return myEntity;
    }

    /** Returns the amount needed by the request
     *
     */
    public final int getAmountNeeded() {
        return (myInitialAmountRequested - myAmountAllocated);
    }

    /** Gets the original amount requested.
     *
     * @return The original amount requested
     */
    public final int getInitialAmountRequested() {
        return (myInitialAmountRequested);
    }

    /** Gets the amount allocated by the resource to the request
     *
     * @return The amount of the resource given to the request
     */
    public final int getAmountAllocated() {
        return (myAmountAllocated);
    }

    /** Indicates that the request allows the amount
     *  requested to be partially filled
     *
     * @return Returns true if allowed
     */
    public final boolean allowsPartialFilling() {
        return myAllowPartialFillingFlag;
    }

    /** Gets the resource that the request is using
     *
     * @return The resource or null if the request has not been used to seize
     *  resource
     */
    public final Resource getSeizedResource() {
        return (mySeizedResource);
    }

    /** If the request was made on a ResourceSet then
     *  this returns the relevent set (or null)
     *
     * @return
     */
    public final ResourceSet getSeizedResourceSet(){
        return mySeizedResourceSet;
    }

//    public final SeizeRequirement getSeizeRequirement(){
//        return mySeizeRequirement;
//    }

    public final String getResourceSaveKey(){
        return myResourceSaveKey;
    }
    
    /** Indicates whether or not the request has been allocated
     *  its full amount, i.e. whether it has been satisfied
     *
     *  The request is satisfied if the amount requested equals
     *  the amount allocated, i.e. it has been allocated all that it
     *  has requested.
     *
     * @return true if the request is satisfied
     */
    public final boolean isSatisfied() {
        return (mySatisfiedFlag);
    }

    /** Indicates whether the request has not been allocated any
     *  units from a resource
     *
     * @return
     */
    public final boolean isUnallocated() {
        return myState == State.Unallocated;
    }

    /** Indicates whether the request has been partially allocated
     *  to a resource
     *
     * @return
     */
    public final boolean isPartiallyAllocated() {
        return myState == State.PartiallyAllocated;
    }

        public ResourceSelectionRuleIfc getResourceSelectionRule() {
        return myRule;
    }

    public void setResourceSelectionRule(ResourceSelectionRuleIfc rule) {
        if (rule == null) {
            throw new IllegalArgumentException("The ResourceSelectionRuleIfc was null");
        }
        myRule = rule;
    }

    /** Returns a negative integer, zero, or a positive integer
     * if this object is less than, equal to, or greater than the
     * specified object.
     *
     * Natural ordering:  priority, then order of creation
     *
     * Lower priority, lower order of creation goes first
     *
     * Throws ClassCastException if the specified object's type
     * prevents it from begin compared to this object.
     *
     * Throws RuntimeException if the id's of the objects are the same,
     * but the references are not when compared with equals.
     *
     * Note:  This class may have a natural ordering that is inconsistent
     * with equals.
     * @param req The requirement to compare this listener to
     * @return Returns a negative integer, zero, or a positive integer
     * if this object is less than, equal to, or greater than the
     * specified object.
     */
    @Override
    public int compareTo(Request req) {

        // check priorities

        if (myPriority < req.myPriority) {
            return (-1);
        }

        if (myPriority > req.myPriority) {
            return (1);
        }

        // priorities are equal, compare arrival numbers

        if (myArrivalNumber < req.myArrivalNumber) // lower id, implies arrived earlier
        {
            return (-1);
        }

        if (myArrivalNumber > req.myArrivalNumber) {
            return (1);
        }

        // if the id's are equal then the object references must be equal
        // if this is not the case there is a problem

        if (this.equals(req)) {
            return (0);
        } else {
            throw new RuntimeException("Id's were equal, but references were not, in Request compareTo");
        }

    }
}
