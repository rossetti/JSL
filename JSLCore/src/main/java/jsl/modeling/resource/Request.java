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
package jsl.modeling.resource;

import jsl.simulation.JSLEvent;
import jsl.simulation.State;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.utilities.GetValueIfc;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rvariable.ConstantRV;

import java.util.Objects;
import java.util.Optional;

/**
 * A Request represents a notification that the ResourceUnit is needed for
 * allocation for a period of time. A builder pattern is available to facilitate
 * construction of requests.
 *
 * The request of the resourceUnit may be preempted or not. The preemption rule
 * determines how the request is handled if the resourceUnit attempts a preemption
 * because of a failure or inactive period.
 *
 * The rules are:
 *
 * NONE = do not allow preemption, the request cannot be preempted
 *
 * RESUMABLE = request resumes with the time remaining after preemption
 *
 * RESTART = the request restarts using its original time after preemption
 *
 * CANCEL = the request should be canceled upon preemption
 *
 * A request can be in 1 of 7 states:
 *
 * CreatedState = newly made, can only transition to ReadyState or RejectedState
 *
 * ReadyState = ready to do something, can only transition to WaitingState,
 * CanceledState, or AllocatedState
 *
 * WaitingState = waiting for resourceUnit, can only transition to ReadyState
 *
 * RejectedState = rejected after creation, no further transitions permitted
 *
 * CanceledState = can cancel from ReadyState or AllocatedState, no further
 * transitions
 *
 * AllocatedState = using the resourceUnit, may preempted, canceled, or completed
 *
 * Preempted = preempted from using the resourceUnit, can resumed or cancel
 *
 * Completed = finished its life-cycle, no further transitions.
 *
 * @author rossetti
 */
public class Request extends QObject {
    
    private final RejectedState myRejectedState = new RejectedState();
    private final CanceledState myCanceledState = new CanceledState();
    private final Completed myCompletedState = new Completed();
    private final AllocatedState myAllocatedState = new AllocatedState();
    private final WaitingState myWaitingState = new WaitingState();
    private final CreatedState myCreatedState = new CreatedState();
    private final PreparedState myPreparedState = new PreparedState();
    private final Preempted myPreemptedState = new Preempted();
    
    public enum PreemptionRule {
        NONE, // not allowed
        RESUME, // resumes with time remaining
        CANCEL, // must be canceled, can't be finished
        RESTART // resumeable, but with fresh time
    }
    
    private final PreemptionRule myPreemptionRule;
    private final RequestReactorIfc myReactor;
    private ResourceUnit myResourceUnit;
    private final GetValueIfc myDuration;
    private final double myInitialRequestTime;
    private final double myTimeUnits;
    
    private RequestState myState;
    private RequestState myPrevState;
    private double myTimeRemaining;

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request. Time timeUnits is 1.0, priority = JSLEvent.DEFAULT_PRIORITY,
     * PreemptionRule.RESUME, name = null, ResourceUnit = null,
     * attached object = null, request time = Constant.POSITIVE_INFINITY
     *
     * @param creationTime the time the request was created, must be positive
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     */
    public Request(double creationTime, RequestReactorIfc reactor) {
        this(creationTime, null, null, reactor, null, null,
                PreemptionRule.RESUME, JSLEvent.DEFAULT_PRIORITY, 1.0);
    }

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request. Time timeUnits is 1.0, priority = JSLEvent.DEFAULT_PRIORITY,
     * PreemptionRule.RESUME, name = null, ResourceUnit = null,
     * attached object = null
     *
     * @param creationTime the time the request was created, must be positive
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     * @param duration the time duration for the request, determines the time of
     * the request, must not be null
     */
    public Request(double creationTime,
            RequestReactorIfc reactor, GetValueIfc duration) {
        this(creationTime, null, null, reactor, duration, null,
                PreemptionRule.RESUME, JSLEvent.DEFAULT_PRIORITY, 1.0);
    }

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request. Time timeUnits is 1.0, priority = JSLEvent.DEFAULT_PRIORITY,
     * PreemptionRule.RESUME, name = null, ResourceUnit = null
     *
     * @param creationTime the time the request was created, must be positive
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     * @param duration the time duration for the request, determines the time of
     * the request, must not be null
     * @param entity the object (entity) attached to the request, can be null
     */
    public Request(double creationTime,
            RequestReactorIfc reactor, GetValueIfc duration, Object entity) {
        this(creationTime, null, null, reactor, duration, entity,
                PreemptionRule.RESUME, JSLEvent.DEFAULT_PRIORITY, 1.0);
    }

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request. Time timeUnits is 1.0, priority = JSLEvent.DEFAULT_PRIORITY,
     * PreemptionRule.RESUME, name = null
     *
     * @param creationTime the time the request was created, must be positive
     * @param resourceUnit the resource unit, may be null
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     * @param duration the time duration for the request, determines the time of
     * the request, must not be null
     * @param entity the object (entity) attached to the request, can be null
     */
    public Request(double creationTime, ResourceUnit resourceUnit,
            RequestReactorIfc reactor, GetValueIfc duration, Object entity) {
        this(creationTime, null, resourceUnit, reactor, duration, entity,
                PreemptionRule.RESUME, JSLEvent.DEFAULT_PRIORITY, 1.0);
    }

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request. Time timeUnits is 1.0, priority = JSLEvent.DEFAULT_PRIORITY,
     * PreemptionRule.RESUME
     *
     * @param creationTime the time the request was created, must be positive
     * @param name the name, may be null
     * @param resourceUnit the resource unit, may be null
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     * @param duration the time duration for the request, determines the time of
     * the request, must not be null
     * @param entity the object (entity) attached to the request, can be null
     */
    public Request(double creationTime, String name, ResourceUnit resourceUnit,
            RequestReactorIfc reactor, GetValueIfc duration, Object entity) {
        this(creationTime, name, resourceUnit, reactor, duration, entity,
                PreemptionRule.RESUME, JSLEvent.DEFAULT_PRIORITY, 1.0);
    }

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request. Time timeUnits is 1.0, priority = JSLEvent.DEFAULT_PRIORITY
     *
     * @param creationTime the time the request was created, must be positive
     * @param name the name, may be null
     * @param resourceUnit the resource unit, may be null
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     * @param duration the time duration for the request, determines the time of
     * the request, must not be null
     * @param entity the object (entity) attached to the request, can be null
     * @param rule the request preemption rule, if null it is RESUME
     */
    public Request(double creationTime, String name, ResourceUnit resourceUnit,
            RequestReactorIfc reactor, GetValueIfc duration, Object entity,
            PreemptionRule rule) {
        this(creationTime, name, resourceUnit, reactor, duration, entity,
                rule, JSLEvent.DEFAULT_PRIORITY, 1.0);
    }

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request. Time timeUnits is 1.0
     *
     * @param creationTime the time the request was created, must be positive
     * @param name the name, may be null
     * @param resourceUnit the resource unit, may be null
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     * @param duration the time duration for the request, determines the time of
     * the request, must not be null
     * @param entity the object (entity) attached to the request, can be null
     * @param rule the request preemption rule, if null it is RESUME
     * @param priority the priority of the request within the queues that
     * allocate
     * priority
     */
    public Request(double creationTime, String name, ResourceUnit resourceUnit,
            RequestReactorIfc reactor, GetValueIfc duration, Object entity,
            PreemptionRule rule, int priority) {
        this(creationTime, name, resourceUnit, reactor, duration, entity,
                rule, priority, 1.0);
    }

    /**
     * The builder provides a more semantically meaningful way of constructing
     * a request
     *
     * @param creationTime the time the request was created, must be positive
     * @param name the name, may be null
     * @param resourceUnit the resource unit, may be null
     * @param reactor the RequestReactorIfc used to react to request changes,
     * must not be null
     * @param duration the time duration for the request, determines the time of
     * the request, if null it is set to Constant.POSITIVE_INFINITY
     * @param entity the object (entity) attached to the request, can be null
     * @param rule the request preemption rule, if null it is RESUME
     * @param priority the priority of the request within the queues that
     * holds requests to be allocated for usage
     * @param timeUnits how the request time will be interpreted, see
     * ModelElement, the default is 1
     */
    public Request(double creationTime, String name, ResourceUnit resourceUnit,
            RequestReactorIfc reactor, GetValueIfc duration, Object entity,
            PreemptionRule rule, int priority, double timeUnits) {
        super(creationTime, name);
        myResourceUnit = resourceUnit;
        if (timeUnits <= 0.0) {
            timeUnits = 1.0;
        }
        myTimeUnits = timeUnits;
        if (reactor == null) {
            throw new IllegalArgumentException("The RequestReactorIfc was null.");
        }
        myReactor = reactor;
        if (duration == null) {
            duration = ConstantRV.POSITIVE_INFINITY;
//            StringBuilder sb = new StringBuilder();
//            sb.append("The request's time setter was set to Constant.POSITIVE_INFINITY").append(System.lineSeparator());
//            JSL.LOGGER.warning(sb.toString());
        }
        myDuration = duration;
        setAttachedObject(entity);
        if (rule == null) {
            rule = PreemptionRule.RESUME;
        }
        myPreemptionRule = rule;
        setPriority(priority);
        myInitialRequestTime = myDuration.getValue();
        myTimeRemaining = myInitialRequestTime;
        myState = myCreatedState;
        myState.enter(getCreateTime());
        myPrevState = null;
    }

    /**
     * Used when the request is allocated to a ResourceUnit
     *
     * @param resource the resource associated with the request
     */
    private void setResourceUnit(ResourceUnit resource) {
        Objects.requireNonNull(resource, "The resource was null");
        myResourceUnit = resource;
    }

    /**
     * Creates a Builder that can be used to step through the construction
     * of a Request. The request must have a creation time and a
     * RequestReactorIfc
     * associated with it.
     *
     * @return the builder
     */
    public static CreateTimeStep builder() {
        return new Builder();
    }
    
    public static interface CreateTimeStep {

        /**
         *
         * @param time the time that the request was made (created)
         * @return the BuildStep
         */
        ReactorStep createTime(double time);
    }
    
    public static interface ReactorStep {

        /**
         *
         * @param reactor the thing that reacts to request state changes
         * @return the BuildStep
         */
        BuildStep reactor(RequestReactorIfc reactor);
    }
    
    public static interface BuildStep {

        /**
         *
         * @param entity the object to attach
         * @return the BuildStep
         */
        BuildStep entity(Object entity);

        /**
         *
         * @param timeSetter the duration of the request
         * @return the BuildStep
         */
        BuildStep duration(GetValueIfc timeSetter);

//        BuildStep resource(ResourceUnit resource);
        /**
         *
         * @param name the name of the request
         * @return the BuildStep
         */
        BuildStep name(String name);

        /**
         *
         * @param priority the priority of the request
         * @return the BuildStep
         */
        BuildStep priority(int priority);

        /**
         *
         * @param rule the preemption rule
         * @return the BuildStep
         */
        BuildStep rule(PreemptionRule rule);

        /**
         *
         * @param timeUnits the time units to interpret the request time
         * @return the BuildStep
         */
        BuildStep timeUnits(double timeUnits);

        /**
         *
         * @return the built Request
         */
        Request build();
    }
    
    protected static class Builder implements CreateTimeStep,
            ReactorStep, BuildStep {
        
        private ResourceUnit resourceUnit;
        private int priority = JSLEvent.DEFAULT_PRIORITY;
        private GetValueIfc timeSetter = ConstantRV.POSITIVE_INFINITY;
        private String name;
        private PreemptionRule rule = PreemptionRule.RESUME;
        private double timeUnits = 1;
        private RequestReactorIfc reactor;
        private Object entity;
        private double creationTime;
        
        @Override
        public ReactorStep createTime(double creationTime) {
            if (creationTime < 0) {
                throw new IllegalArgumentException("The creation time must be > 0.");
            }
            this.creationTime = creationTime;
            return this;
        }

//        @Override
//        public BuildStep resource(ResourceUnit resource) {
//            if (resource == null) {
//                throw new IllegalArgumentException("The resource was null");
//            }
//            this.resourceUnit = resource;
//            return this;
//        }
        @Override
        public BuildStep entity(Object entity) {
            this.entity = entity;
            return this;
        }
        
        @Override
        public BuildStep reactor(RequestReactorIfc reactor) {
            if (reactor == null) {
                throw new IllegalArgumentException("The RequestReactorIfc was null.");
            }
            this.reactor = reactor;
            return this;
        }
        
        @Override
        public BuildStep priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        @Override
        public BuildStep name(String name) {
            this.name = name;
            return this;
        }
        
        @Override
        public BuildStep rule(PreemptionRule rule) {
            this.rule = rule;
            return this;
        }
        
        @Override
        public BuildStep duration(GetValueIfc timeSetter) {
            this.timeSetter = timeSetter;
            return this;
        }
        
        @Override
        public final BuildStep timeUnits(double timeUnits) {
            this.timeUnits = timeUnits;
            return this;
        }
        
        @Override
        public final Request build() {
            //return new Request<>(this);
            return new Request(creationTime, name, resourceUnit, reactor,
                    timeSetter, entity, rule, priority, timeUnits);
        }
        
    }

    /**
     *
     * @return The addFactor representing the time timeUnits of the request
     */
    public final double getTimeUnits() {
        return myTimeUnits;
    }

    /**
     *
     * @return number of times that the request was preempted
     */
    public final double getNumPreemptions() {
        return myPreemptedState.getNumberOfTimesEntered();
    }

    /**
     *
     * @return the amount of time that the request spent preempted
     */
    public final double getTotalPreemptionTime() {
        return myPreemptedState.getTotalTimeInState();
    }

    /**
     * If the request has not be allocate to a resource unit yet,
     * then the resource unit will be null.
     *
     * @return the resourceUnit associated with the request
     */
    public final Optional<ResourceUnit> getResourceUnit() {
        return Optional.ofNullable(myResourceUnit);
    }

    /**
     *
     * @return true if in the created state
     */
    public final boolean isCreated() {
        return myState == myCreatedState;
    }

    /**
     *
     * @return true if in the ready state
     */
    public final boolean isReady() {
        return myState == myPreparedState;
    }

    /**
     *
     * @return true if in the rejected state
     */
    public final boolean isRejected() {
        return myState == myRejectedState;
    }

    /**
     *
     * @return rue if in the canceled state
     */
    public final boolean isCanceled() {
        return myState == myCanceledState;
    }

    /**
     * Indicates that the request has been finished. That is, whether or not
     * the request is finished using the allocated resource unit
     *
     * @return true if finished
     */
    public final boolean isFinished() {
        return myState == myCompletedState;
    }

    /**
     *
     * @return true if in the waiting state
     */
    public final boolean isWaiting() {
        return myState == myWaitingState;
    }

    /**
     *
     * @return true if in the using state
     */
    public final boolean isAllocated() {
        return myState == myAllocatedState;
    }

    /**
     *
     * @return true if in the preempted state
     */
    public final boolean isPreempted() {
        return myState == myPreemptedState;
    }

    /**
     *
     * @return true if previous state was created
     */
    public final boolean isPreviousStateReady() {
        return myPrevState == myPreparedState;
    }

    /**
     *
     * @return true if previous state was created
     */
    public final boolean isPreviousStateCreated() {
        return myPrevState == myCreatedState;
    }

    /**
     *
     * @return true if previous state was waiting
     */
    public final boolean isPreviousStateWaiting() {
        return myPrevState == myWaitingState;
    }

    /**
     *
     * @return true if previous state was using
     */
    public final boolean isPreviousStateAllocated() {
        return myPrevState == myAllocatedState;
    }

    /**
     *
     * @return true if previous state was preempted
     */
    public final boolean isPreviousStatePreempted() {
        return myPrevState == myPreemptedState;
    }

    /**
     * May return Double.NaN if state was never entered
     *
     * @return the time the current state was entered
     */
    public final double getTimeEnteredCurrentState() {
        return myState.getTimeStateEntered();
    }

    /**
     * May return Double.NaN if state was never exited
     *
     * @return the time that the current state was exited
     */
    public final double getTimeExitedCurrentState() {
        return myState.getTimeStateExited();
    }

    /**
     * May return Double.NaN if state was never entered
     *
     * @return the time that the previous state was entered
     */
    public final double getTimeEnteredPreviousState() {
        return myPrevState.getTimeStateEntered();
    }

    /**
     * May return Double.NaN if state was never exited
     *
     * @return the time that the previous state was exited
     */
    public final double getTimeExitedPreviousState() {
        return myPrevState.getTimeStateExited();
    }

    /**
     *
     * @return the time from when the request tried to allocate the ResourceUnit
     * until it actually started using the unit
     */
    public final double getWaitingTime() {
        return getTimeFirstEnteredUsingState() - getTimeFirstEnteredWaitingState();
    }

    /**
     *
     * @return the time that the request started using the resourceUnit
     */
    public final double getTimeFirstEnteredUsingState() {
        return myAllocatedState.getTimeFirstEntered();
    }

    /**
     *
     * @return the time remaining on the request
     */
    public final double getTimeRemaining() {
        return myTimeRemaining;
    }

    /**
     *
     * @return the time that the request first entered the wait state
     */
    public final double getTimeFirstEnteredWaitingState() {
        return myWaitingState.getTimeFirstEntered();
    }

    /**
     * The request must be allocate to a resource unit.
     *
     * @return the current simulation time
     */
    protected final double getTime() {
        if (getResourceUnit().isPresent()) {
            return getResourceUnit().get().getTime();
        }
        throw new IllegalStateException("The getTime() cannot be used "
                + " because the request has not yet been allocated to a resource unit.");
    }

    /**
     *
     * @return the time from when the request tried to allocate the ResourceUnit
     * until it finally finished its allocate of the unit.
     */
    public final double getTimeUntilCompletion() {
        return getTimeCompleted() - getTimeFirstEnteredWaitingState();
    }

    /**
     *
     * @return the time that the request was finished or Double.NaN if not yet
     * finished
     */
    public final double getTimeCompleted() {
        return myCompletedState.getTimeStateEntered();
    }

    /**
     * The initial time associated with the request
     *
     * @return time associated with the request
     */
    public final double getInitialRequestTime() {
        return myInitialRequestTime;
    }

    /**
     * If the request can be preempted during its allocate of the resourceUnit
     *
     * @return the rule
     */
    public final PreemptionRule getPreemptionRule() {
        return myPreemptionRule;
    }

    /**
     *
     * @return true if the request does not allow preemption, i.e. the
     * PreemptionRule is NONE
     */
    public final boolean doesNotAllowPreemption() {
        return myPreemptionRule == PreemptionRule.NONE;
    }

    /**
     *
     * @return the thing that determines the time of the request
     */
    public final GetValueIfc getTimeSetter() {
        return myDuration;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", time = ").append(getInitialRequestTime());
        sb.append(", rule = ").append(getPreemptionRule());
        sb.append(", state = ").append(myState.getName());
        sb.append(", remaining = ").append(getTimeRemaining());
        return sb.toString();
    }

    /**
     * Causes the request to be canceled by the resource
     */
    public final void cancel() {
        if (getResourceUnit().isPresent()) {
            getResourceUnit().get().cancel(this);
        } else {
            throw new IllegalStateException("The request cannot be canceled"
                    + " because it has not yet been allocated to a resource unit.");
        }
    }

    /**
     * Causes the request to be released by the resource. This is immediate.
     * The resource must be busy with this request. If a usage time
     * has been scheduled (internally) to the resource, it is canceled.
     * The request must be in the isAllocated() state.
     */
    public final void release() {
        if (getResourceUnit().isPresent()) {
            getResourceUnit().get().release(this);
        } else {
            throw new IllegalStateException("The request cannot be released"
                    + " because it has not yet been allocated to a resource unit.");
        }
    }

    /**
     * Can only be rejected from the CreatedState state
     */
    void reject(double time) {
        myState.reject(time);
    }

    /**
     * Can only enterWaitingState from the ReadyState state
     */
    void enterWaitingState(Queue<Request> queue, double time) {
        myState.queue(queue, time);
    }

    /**
     * Can only exitWaitingState() from the WaitingState
     */
    void exitWaitingState(Queue<Request> queue, double time) {
        myState.dequeue(queue, time);
    }

    /**
     * Can only makeReady() from CreatedState
     */
    void makeReady(double time) {
        myState.prepare(time);
    }

    /**
     * Can only allocate from the ReadyState state
     */
    void allocate(ResourceUnit unit, double time) {
        myState.allocate(unit, time);
    }

    /**
     * Can cancel from WaitingState, AllocatedState, Preempted
     */
    void becomeCanceled(double time) {
        myState.cancel(time);
    }

    /**
     * Can only resumed from Preempted
     */
    void resume(double time) {
        myState.resume(time);
    }

    /**
     * Can only preempted from AllocatedState
     */
    void preempt(double time) {
        myState.preempt(time);
    }

    /**
     * Can only be finished from AllocatedState
     */
    void finish(double time) {
        myState.complete(time);
    }

    /**
     * Meant to facilitate garbage collection. See nullify() on QObject. Calls
     * nullify() on QObject.
     */
    void dispose() {
        super.nullify();
        myState = null;
        myPrevState = null;
    }

    /**
     * Sets the state. Notification of state change is embedded within
     * inner RequestState sub-classes.
     *
     * @param state the new state
     * @param time the time of the state change
     */
    protected void setState(RequestState state, double time) {
        myPrevState = myState;
        myPrevState.exit(time);
        myState = state;
        myState.enter(time);
    }
    
    protected class RequestState extends State {
        
        protected RequestState(String name) {
            super(name);
        }
        
        protected void dequeue(Queue<Request> queue, double time) {
            throw new IllegalStateException("Tried to exit queue from an illegal state: " + myName);
        }
        
        protected void prepare(double time) {
            throw new IllegalStateException("Tried to become prepared from an illegal state: " + myName);
        }
        
        protected void queue(Queue<Request> queue, double time) {
            throw new IllegalStateException("Tried to enter queue from an illegal state: " + myName);
        }
        
        protected void reject(double time) {
            throw new IllegalStateException("Tried to reject from an illegal state: " + myName);
        }
        
        protected void allocate(ResourceUnit unit, double time) {
            throw new IllegalStateException("Tried to use from an illegal state: " + myName);
        }
        
        protected void cancel(double time) {
            throw new IllegalStateException("Tried to cancel from an illegal state: " + myName);
        }
        
        protected void resume(double time) {
            throw new IllegalStateException("Tried to resume from an illegal state: " + myName);
        }
        
        protected void preempt(double time) {
            throw new IllegalStateException("Tried to preempt from an illegal state: " + myName);
        }
        
        protected void complete(double time) {
            throw new IllegalStateException("Tried to complete from an illegal state: " + myName);
        }
        
    }

    /**
     * Represents a freshly made Request. A request that is
     * created can transition to ready or be rejected
     */
    protected class CreatedState extends RequestState {
        
        public CreatedState() {
            super("Created");
        }
        
        @Override
        protected void prepare(double time) {
            setState(myPreparedState, time);
            myReactor.prepared(Request.this);
        }
        
        @Override
        protected void reject(double time) {
            setState(myRejectedState, time);
            myReactor.rejected(Request.this);
            if (getResourceUnit().isPresent()){
                getResourceUnit().get().collectFinalRequestStatistics(Request.this);
            }
        }
        
    }

    /**
     * A Request that is prepared can wait, be canceled, or transition
     * to being in allocate.
     */
    protected class PreparedState extends RequestState {
        
        public PreparedState() {
            super("Ready");
        }
        
        @Override
        protected void prepare(double time) {
            // nothing to do. already in the ready state
        }
        
        @Override
        protected void queue(Queue<Request> queue, double time) {
            setState(myWaitingState, time);
            myReactor.enqueued(Request.this, queue);
        }
        
        @Override
        protected void allocate(ResourceUnit unit, double time) {
            setResourceUnit(unit);
            setState(myAllocatedState, time);
            myReactor.allocated(Request.this);
        }
        
        @Override
        protected void cancel(double time) {
            setState(myCanceledState, time);
            myReactor.canceled(Request.this);
            if (getResourceUnit().isPresent()){
                getResourceUnit().get().collectFinalRequestStatistics(Request.this);
            }
        }
        
    }
    
    protected class WaitingState extends RequestState {
        
        public WaitingState() {
            super("Waiting");
        }
        
        @Override
        protected void dequeue(Queue<Request> queue, double time) {
            setState(myPreparedState, time);
            myReactor.dequeued(Request.this, queue);
        }
        
    }
    
    protected class RejectedState extends RequestState {
        
        public RejectedState() {
            super("Rejected");
        }
    }
    
    protected class Completed extends RequestState {
        
        public Completed() {
            super("Completed");
        }
    }
    
    protected class CanceledState extends RequestState {
        
        public CanceledState() {
            super("Canceled");
        }
    }
    
    protected class AllocatedState extends RequestState {
        
        public AllocatedState() {
            super("Allocated");
        }
        
        private void setTimeRemaining() {
            if (getPreemptionRule() == PreemptionRule.RESTART) {
                myTimeRemaining = getInitialRequestTime();
            } else {
                //TODO should double check this logic
                // can total time in allocated state be used
                double delta = getTime() - getTimeStateEntered();
                myTimeRemaining = Math.max(0.0, myTimeRemaining - delta);
                if (JSLMath.equal(0.0, myTimeRemaining)) {
                    myTimeRemaining = 0.0;
                }
            }
        }
        
        @Override
        protected void complete(double time) {
            setTimeRemaining();
            setState(myCompletedState, time);
            myReactor.completed(Request.this);
            myResourceUnit.collectFinalRequestStatistics(Request.this);
        }
        
        @Override
        protected void preempt(double time) {
            setTimeRemaining();
            switch (getPreemptionRule()) {
                case RESUME:
                    setState(myPreemptedState, time);
                    myReactor.preempted(Request.this);
                    break;
                case RESTART:
                    setState(myPreemptedState, time);
                    myReactor.preempted(Request.this);
                    break;
                case CANCEL:
                    setState(myCanceledState, time);
                    myReactor.canceled(Request.this);
                    myResourceUnit.collectFinalRequestStatistics(Request.this);
                    break;
                case NONE:
                    throw new IllegalStateException("Tried to preempt non-preemptable request");
            }
        }
        
        @Override
        protected void cancel(double time) {
            setTimeRemaining();
            setState(myCanceledState, time);
            myReactor.canceled(Request.this);
            myResourceUnit.collectFinalRequestStatistics(Request.this);
        }
    }
    
    protected class Preempted extends RequestState {
        
        public Preempted() {
            super("Preempted");
        }
        
        @Override
        protected void resume(double time) {
            if (getPreemptionRule() == PreemptionRule.NONE) {
                throw new IllegalStateException("Tried to resume non-preemptable request");
            } else if (getPreemptionRule() == PreemptionRule.CANCEL) {
                throw new IllegalStateException("Tried to resume a request that must be canceled");
            } else {
                setState(myAllocatedState, time);
                myReactor.resumed(Request.this);
            }
        }
        
        @Override
        protected void cancel(double time) {
            setState(myCanceledState, time);
            myReactor.canceled(Request.this);
            if (getResourceUnit().isPresent()){
                getResourceUnit().get().collectFinalRequestStatistics(Request.this);
            }
        }
    }
}
