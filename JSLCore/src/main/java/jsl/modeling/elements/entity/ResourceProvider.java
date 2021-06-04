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

import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.Queue;
import jsl.modeling.queue.Queue.Discipline;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;

public class ResourceProvider extends SchedulingElement {

    /** The resource set used by this provider to ask for
     *  and and return idle resources
     */
    protected ResourceSet myResourceSet;

    /** A queue to hold waiting requests when an idle
     *  resource is not immediately available.
     */
    protected Queue<Entity> myEntityQ;

    /** The service time when using the provider
     *
     */
    protected RandomVariable myServiceRV;

    /** Can be used to supply a rule for how the requests
     *  are selected for allocation
     */
    protected EntitySelectionRuleIfc myInitialRequestSelectionRule;

    /** Can be used to supply a rule for how the requests
     *  are selected for allocation
     */
    protected EntitySelectionRuleIfc myEntitySelectionRule;

    private AllocationListener myResourceAllocationListener;

    //   protected ResourceReleasedListener myResourceReleasedListener;
    protected EndServiceListener myEndServiceListener;

    protected final TimeWeighted myNS;

    protected ResponseVariable myWs;

    /** Creates a ResourceProviderr that uses a FIFO queue discipline. An
     *  empty ResourceSet is created and must be filled
     *
     * @param parent
     */
    public ResourceProvider(ModelElement parent) {
        this(parent, null, null, null);
    }

    /** Creates a ResourceProviderr that uses a FIFO queue discipline. An
     *  empty ResourceSet is created and must be filled
     *
     * @param parent
     * @param name
     */
    public ResourceProvider(ModelElement parent, String name) {
        this(parent, name, null, null);
    }

    /** Creates a ResourceProvider that uses the supplied set and FIFO queue discipline
     *
     * @param parent
     * @param name
     * @param set
     */
    public ResourceProvider(ModelElement parent, String name, ResourceSet set) {
        this(parent, name, set, null);
    }

    /** Creates a ResourceProvider that uses the supplied set and FIFO queue discipline
     *
     * @param parent
     * @param set
     */
    public ResourceProvider(ModelElement parent, ResourceSet set) {
        this(parent, null, set, null);
    }

    /** Creates a ResourceProvider that uses the supplied set and queue discipline
     *
     * @param parent
     * @param set
     * @param discipline
     */
    public ResourceProvider(ModelElement parent, ResourceSet set, Discipline discipline) {
        this(parent, null, set, discipline);
    }

    /** Creates a ResourceProvider that uses the supplied set and queue discipline
     *
     * @param parent
     * @param name
     * @param set
     * @param discipline
     */
    public ResourceProvider(ModelElement parent, String name, ResourceSet set, Discipline discipline) {
        super(parent, name);

        if (discipline == null) {
            discipline = Queue.Discipline.FIFO;
        }

        myServiceRV = new RandomVariable(this, ConstantRV.ONE);
        myEntityQ = new Queue<>(this, getName() + "_Q", discipline);
        myNS = new TimeWeighted(this, 0.0, getName() + "_NS");
        myWs = new ResponseVariable(this, getName() + "_WS");
        myEndServiceListener = new EndServiceListener();
        myResourceAllocationListener = new AllocationListener();

//        myResourceAllocationListener = new ResourceAllocationListener();
//        myResourceReleasedListener = new ResourceReleasedListener();
        setResourceSet(set);

    }

    public void setServiceTime(RandomIfc d) {
        myServiceRV.setInitialRandomSource(d);
        if (d == null) {
            throw new IllegalArgumentException("Service Time Distribution was null!");
        }
        myServiceRV.setInitialRandomSource(d);
    }

    /** Returns a reference to the resource set used within the provider
     *
     * @return
     */
    public final ResourceSet getResourceSet() {
        return myResourceSet;
    }

    /** Adds a resource with unit capacity to the
     *  underlying resource set
     *
     * @return
     */
    public final Resource addResource() {
        return (addResource(1, null));
    }

    /** Adds a resource with the given capacity to the
     *  underlying resource set
     *
     * @param capacity
     * @return
     */
    public final Resource addResource(int capacity) {
        return (addResource(capacity, null));
    }

    /** Adds a resource with the given capacity and name to the
     *  underlying resource set
     *
     * @param capacity
     * @param name
     * @return
     */
    public Resource addResource(int capacity, String name) {
        return (myResourceSet.addResource(capacity, name));
    }

    /** Adds the given resource to the underlying set of the provider
     *
     * @param resource
     */
    public final void addResource(Resource resource) {
        myResourceSet.addResource(resource);
    }

    /** Returns the current number of idle resources within the underlying
     *  resource set
     *
     * @return
     */
    public final int getNumberOfIdleResources() {
        return (myResourceSet.getNumberAvailableResources());
    }

    /** This will change the queue discipline of the underlying Queue
     *
     * @param discipline
     */
    public final void changeQueueDiscipline(Discipline discipline) {
        myEntityQ.changeDiscipline(discipline);
    }

    /** Returns the initial discipline for the queue
     *
     * @return
     */
    public final Discipline getQueueInitialDiscipline() {
        return myEntityQ.getInitialDiscipline();
    }

    /** Sets the initial queue discipline
     *
     * @param discipline
     */
    public final void setQueueInitialDiscipline(Discipline discipline) {
        myEntityQ.setInitialDiscipline(discipline);
    }

    /** Returns the current number of requests in the queue
     *
     * @return
     */
    public final int getNumberInQueue() {
        return (myEntityQ.size());
    }

    /** Returns a reference to the request selection rule. May be null.
     *
     * @return
     */
    public final EntitySelectionRuleIfc getRequestSelectionRule() {
        return myEntitySelectionRule;
    }

    /** A request selection rule can be supplied to provide alternative behavior
     *  within the selectNextRequest() method. A request selection rule, provides
     *  a mechanism to select the next request from the queue of waiting requests
     *
     * @param rule
     */
    public final void setRequestSelectionRule(EntitySelectionRuleIfc rule) {
        myEntitySelectionRule = rule;
    }

    /** The rule to use when this provider is initialized
     *
     * @return
     */
    public final EntitySelectionRuleIfc getInitialRequestSelectionRule() {
        return myInitialRequestSelectionRule;
    }

    /** The rule to use when this provider is initialized
     *
     */
    public final void setInitialRequestSelectionRule(EntitySelectionRuleIfc rule) {
        myInitialRequestSelectionRule = rule;
    }

    protected void initialize() {
        setRequestSelectionRule(getInitialRequestSelectionRule());
    }

    protected final void setResourceSet(ResourceSet set) {

        if ((set == null) && (myResourceSet == null)) {
            myResourceSet = new ResourceSet(this, getName() + " ResourceSet");
        } else {
            myResourceSet = set;
        }

//        myResourceSet.addResourceProvider(this);

    }

    /** Selects a candidate request from the queue for allocation
     *  to one of the resources.  The selection process does not remove
     *  the request from the queue.
     *
     * @return The request that was selected to for a resource
     */
    protected Entity selectNextEntity() {
        Entity e = null;
        if (myEntitySelectionRule != null) {
            e = myEntitySelectionRule.selectNextEntity(myEntityQ, this);
        } else {
            e = (Entity) myEntityQ.peekNext();
        }
        return (e);
    }

    public void seize(Entity entity) {
        seize(entity, 1, Request.DEFAULT_PRIORITY, false);
    }

    public void seize(Entity entity, int amtNeeded) {
        seize(entity, amtNeeded, Request.DEFAULT_PRIORITY, false);
    }

    public void seize(Entity entity, int amtNeeded, int priority) {
        seize(entity, amtNeeded, priority, false);
    }

    /** The client asks the ResourceProvider to seize the given amount of the resource.
     *  The entity associated with the request immediately enters the providers queue.
     *  If there is an idle resource
     *  available that can be used to allocate to the request then the request is processed.
     *  If the request can be satisfied in full it is removed from the queue and the
     *  units of the resource are allocated to it.  If it cannot be allocated in full, it is
     *  checked to see if it allows partial filling, if so the available units of the resource
     *  are allocated to it, but it remains in the queue.
     *
     *  NOTE:  It is up to the ResourceAllocationListenerIfc to check if the request is
     *  still in the queue or if it has been satisfied before proceeding.
     *
     *  The amount requested must not exceed the maximum capacity of the underlying resource
     *  set.
     *
     * @param entity
     * @param amtNeeded
     * @param priority
     * @param partialFillFlag
     */
    public void seize(Entity entity, int amtNeeded, int priority, boolean partialFillFlag) {
        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null!");
        }

        if (amtNeeded > myResourceSet.getMaxCapacity()) {
            throw new IllegalArgumentException("The amount requested was > "
                    + "the maximum capacity of any resource in the set!");
        }

        myNS.increment();

        // queue the entity
        myEntityQ.enqueue(entity);

        myResourceSet.seize(entity, amtNeeded, priority, partialFillFlag,
                myResourceAllocationListener);

    }

    protected void allocated(Request request) {
        if (request.isSatisfied()) {
            Entity customer = request.getEntity();
            myEntityQ.remove(customer);
            Resource resource = request.getSeizedResource();
            Allocation a = resource.allocate(customer, request.getAmountNeeded());
            scheduleEvent(myEndServiceListener, myServiceRV, a);
        }
    }

    protected void endOfService(Allocation a) {
        myNS.decrement();
        Entity e = a.getEntity();
        double ws = getTime() - e.getTimeEnteredQueue();
        myWs.setValue(ws);
        // release the resource
        Resource resource = a.getAllocatedResource();
        resource.release(a);
    }

    class EndServiceListener implements EventActionIfc<Allocation> {

        public void action(JSLEvent<Allocation> event) {
            Allocation a = event.getMessage();
            ResourceProvider.this.endOfService(a);
        }
    }

    class AllocationListener implements AllocationListenerIfc {

        public void allocated(Request request) {
            ResourceProvider.this.allocated(request);
        }
    }
}
