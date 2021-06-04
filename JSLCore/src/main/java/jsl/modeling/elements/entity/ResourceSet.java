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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.utilities.misc.OrderedList;

//TODO does not consider time varying capacity changes
/** A ResourceSet holds a set of resources so that they can be selected
 *  for allocation via a ResouceSelectionRuleIfc.  A ResouceSet can have
 *  many resources and a resource can be in many resource sets.  The
 *  selectIdleResource(Request r) method uses the ResourceSelectionRuleIfc (if provided)
 *  to select the next idle resource for a request.  The default is to cycle
 *  through the resources to select the resources in the order in which they were
 *  released.
 */
public class ResourceSet extends ModelElement implements SeizeIfc {

    public static final ResourceSelectionRuleIfc CYCLICAL = new CyclicResourceSelectionRule();

    protected List<Resource> myResources;

    protected List<Resource> myAvailableResources;

    protected ResourceSelectionRuleIfc myResourceSelectionRule;

    protected TimeWeighted myNumAvailableResources;

    private int myMaxCapacity;

    protected OrderedList<Request> myWaitingRequests;

    /** Counts the number of requests that the
     *  resource received
     */
    private int myNumRequestsReceived;

    /** Creates a resource set with the given model element as a parent
     *  and assigned a default name.
     *
     * @param parent
     */
    public ResourceSet(ModelElement parent) {
        this(parent, null);
    }

    /** Creates a resource set with the given model element as a parent and the
     *  given name.
     *
     * @param parent
     * @param name
     */
    public ResourceSet(ModelElement parent, String name) {
        super(parent, name);
        myWaitingRequests = new OrderedList<Request>();
        myResources = new LinkedList<Resource>();
        myAvailableResources = new LinkedList<Resource>();
        myNumAvailableResources = new TimeWeighted(this, getName() + " #Available");
    }

    protected void initialize() {
        for (Request r : myWaitingRequests) {
            r.nullify();
        }
        myWaitingRequests.clear();

        for (Resource r : myResources) {
            if (r.hasAvailableUnits()) {
                // add it only if it is not already in the available list
                if (!myAvailableResources.contains(r)) {
                    if (myResourceSelectionRule != null) {
                        myResourceSelectionRule.addAvailableResource(myAvailableResources, r);
                    } else {
                        myAvailableResources.add(r);
                    }
                }
            }
        }

        myNumAvailableResources.setInitialValue(myAvailableResources.size());

    }

    /**
     * @return Returns the MaxCapacity.
     */
    public final int getMaxCapacity() {
        return myMaxCapacity;
    }

    /** Creates and adds a resource to the set
     *
     * @return
     */
    public final Resource addResource() {
        return (addResource(1));
    }

    /** Creates and adds a resource to the set
     *
     * @param capacity
     * @return
     */
    public final Resource addResource(int capacity) {
        Resource resource = new Resource(this, capacity);
        addResource(resource);
        return (resource);
    }

    /** Creates and adds a resource to the set
     *
     * @param capacity
     * @param name
     * @return
     */
    public final Resource addResource(int capacity, String name) {
        Resource resource = new Resource(this, capacity, name);
        addResource(resource);
        return (resource);
    }

    /** Adds a resource to the set.
     *
     * @param resource
     */
    public final void addResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("The supplied resource was null!");
        }
        int capacity = resource.getInitialCapacity();
        if (capacity > myMaxCapacity) {
            myMaxCapacity = capacity;
        }
        myResources.add(resource);
        resource.attachResourceSet(this);
    }

    /** Returns an iterator to the resources in this set
     *
     * @return
     */
    public final ListIterator<Resource> getResourceIterator() {
        return (myResources.listIterator());
    }

    /** Returns the next available resource to be used for allocating
     *  to requests, null if none are found that can satisfy the request.
     *  Uses the supplied rule to select the resource
     *  
     *
     * @param amtNeeded
     * @return
     */
    public Resource selectAvailableResource(int amtNeeded, ResourceSelectionRuleIfc rule) {

        if (amtNeeded <= 0) {
            throw new IllegalArgumentException("The amount needed was <= 0!");
        }

        if (amtNeeded > getMaxCapacity()) {
            throw new IllegalArgumentException("The amount needed was > the maximum capacity of any resource in the set!");
        }

        if (myAvailableResources.isEmpty()) {
            return (null);
        }

        if (rule == null) {
            throw new IllegalArgumentException("The supplied rule was null");
        }

        return rule.selectAvailableResource(myAvailableResources, amtNeeded);

    }

    /** Selects an available resource or null if none are available
     *  The default is to find the first available resource
     *  that has the maximum available units. To change this
     *  either override this method or supply a ResourceSelectionRuleIfc
     *
     * @return
     */
    public Resource selectAvailableResource(ResourceSelectionRuleIfc rule) {
        if (rule == null) {
            throw new IllegalArgumentException("The supplied rule was null");
        }
        if (myAvailableResources.isEmpty()) {
            return (null);
        }
        return rule.selectAvailableResource(myAvailableResources);

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

    public final void seize(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("The supplied request was null!");
        }

        if (request.getEntity() == null) {
            throw new IllegalArgumentException("The supplied request is not associated with an entity");
        }

        if (request.getSeizedResource() != null) {
            throw new IllegalArgumentException("The supplied request seized on a different resource");
        }

        if (!request.hasResourceAllocationListener()) {
            throw new IllegalArgumentException("No ResourceAllocationListenerIfc supplied for the request");
        }

        if (request.getResourceSelectionRule() == null) {
            throw new IllegalArgumentException("No ResourceSelectionRuleIfc supplied for the request");
        }

        myNumRequestsReceived = myNumRequestsReceived + 1;
        request.setArrivalNumber(myNumRequestsReceived);
        request.setSeizedResourceSet(this);
        //always add the arriving request
        addWaitingRequest(request);

        // the arriving request might not be the next request
        // because of priority or other list processing rules
        if (hasAvailableResources()) {
            if (request == peekNextRequest()) {
                //arriving request is next, check for allocation
                Resource resource = selectAvailableResource(request.getAmountNeeded(),
                        request.getResourceSelectionRule());
                if (resource != null) {
                    removeNextRequest();
                    resource.seize(request);
                } else {
                    // check for partial filling
                    if (request.allowsPartialFilling()) {
                        Resource r = selectAvailableResource(request.getResourceSelectionRule());
                        if (r != null) {
                            removeNextRequest();
                            r.seize(request);
                        }
                    }
                }
            }
        }
    }

    public final Request seize(Entity entity, AllocationListenerIfc listener) {
        return seize(entity, 1, Request.DEFAULT_PRIORITY, false, listener,
                ResourceSet.CYCLICAL);
    }

    public final Request seize(Entity entity, int amtNeeded, int priority,
            AllocationListenerIfc listener) {
        return seize(entity, amtNeeded, priority, false, listener,
                ResourceSet.CYCLICAL);
    }

    public final Request seize(Entity entity, int amtNeeded, AllocationListenerIfc listener) {
        return seize(entity, amtNeeded, Request.DEFAULT_PRIORITY, false,
                listener, ResourceSet.CYCLICAL);
    }

    public final Request seize(Entity entity, int amtNeeded,
            boolean partialFillFlag, AllocationListenerIfc listener) {
        return seize(entity, amtNeeded, Request.DEFAULT_PRIORITY, partialFillFlag,
                listener, ResourceSet.CYCLICAL);
    }

    public final Request seize(Entity entity, int amtNeeded, int priority,
            boolean partialFillFlag, AllocationListenerIfc listener) {
        return seize(entity, amtNeeded, priority, partialFillFlag,
                listener, ResourceSet.CYCLICAL);
    }

    public final Request seize(Entity entity, int amtNeeded, int priority,
            boolean partialFillFlag, AllocationListenerIfc listener,
            ResourceSelectionRuleIfc rule) {

        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null!");
        }

        Request request = new Request(amtNeeded, priority,
                partialFillFlag);
        request.setResourceSelectionRule(rule);
        request.setResourceAllocationListener(listener);
        request.setEntity(entity);
        seize(request);

        return request;
    }

    /** Returns the current number of idle resources in the set
     *
     * @return
     */
    public final int getNumberAvailableResources() {
        return (myAvailableResources.size());
    }

    public final boolean hasAvailableResources() {
        return (getNumberAvailableResources() > 0);
    }

    @Override
    public String asString(){
        StringBuilder sb = new StringBuilder();
        for(Resource r: myResources) {
            sb.append(r.asString());
        }
        return sb.toString();
    }

    /** Returns the current resource selection rule or null if none
     * 
     * @return
     */
    public final ResourceSelectionRuleIfc getResourceSelectionRule() {
        return myResourceSelectionRule;
    }

    /** Sets the resource selection rule.  The supplied rule is responsible for
     *  both recommending an idle resource (not removing it) and for returning an idle
     *  resource back to the list.  This allows the rule to maintain the list in an
     *  order if necessary.
     *
     * @param rule
     */
    public final void setResourceSelectionRule(ResourceSelectionRuleIfc rule) {
        myResourceSelectionRule = rule;
    }

    /** If a resource within the set becomes unavailable (busy, failed, inactive)
     *  it should be removed from the potential list of availabe resources.
     * 
     * @param resource
     */
    protected void resourceBecameUnavailable(Resource resource) {

        if (resource == null) {
            throw new IllegalArgumentException("The supplied resource was null!");
        }

        // if a resource has available units, it must be available
        if (resource.hasAvailableUnits()) {
            throw new IllegalArgumentException("The supplied resource has available units!");
        }

        if (!myResources.contains(resource)) {
            throw new IllegalArgumentException("The supplied resource is not a member of this resource set!");
        }

        // if there are no idle resources, the resource can't be removed
        // this check is probably not needed
        if (myAvailableResources.isEmpty()) {
            return;
        }

        if (!myAvailableResources.contains(resource)) {
            throw new IllegalArgumentException("The supplied resource is not a member of this idle resource set!");
        }

        myAvailableResources.remove(resource);

        myNumAvailableResources.setValue(myAvailableResources.size());

    }

    /** When a resource becomes available it needs to be added to the list
     *  of available resources
     *
     * @param resource
     */
    protected void resourceBecameAvailable(Resource resource) {

        if (resource == null) {
            throw new IllegalArgumentException("The supplied resource was null!");
        }

        // if the resource does not have available units it can't be available
        if (!resource.hasAvailableUnits()) {
            throw new IllegalArgumentException("The supplied resource is not available!");
        }

        if (!myResources.contains(resource)) {
            throw new IllegalArgumentException("The supplied resource is not a member of this resource set!");
        }

        // add it only if it is not already in the idle list
        if (!myAvailableResources.contains(resource)) {
            if (myResourceSelectionRule != null) {
                myResourceSelectionRule.addAvailableResource(myAvailableResources, resource);
            } else {
                myAvailableResources.add(resource);
            }

            myNumAvailableResources.setValue(myAvailableResources.size());

        }

        //processWaitingRequests();
    }

    /** This method processes any waiting requests whenever a resource
     *  within the set has notified the set that it has become available
     *  Any waiting requests have not previously used a resource because
     *  they wait if and only if a resource is not available from
     *  the set when they make their seize.
     *
     */
    protected void processWaitingRequests() {

        if (!myWaitingRequests.isEmpty()) {
            ListIterator<Request> iterator = myWaitingRequests.listIterator();
            while (iterator.hasNext()) {
                Request r = iterator.next();

                Resource resource = selectAvailableResource(r.getAmountNeeded(),
                        r.getResourceSelectionRule());

                if (resource != null) {
                    iterator.remove();
                    resource.seize(r);
                } else {
                    if (r.allowsPartialFilling()) {
                        Resource pr = selectAvailableResource(r.getResourceSelectionRule());
                        if (pr != null) {
                            iterator.remove();
                            pr.seize(r);
                        }
                    }
                }
                // check if no more can be allocated
                if (!hasAvailableResources()) {
                    // stop processing requests
                    break;
                }
            }
        }
    }
}
