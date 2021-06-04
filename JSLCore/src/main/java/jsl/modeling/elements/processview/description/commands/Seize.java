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
package jsl.modeling.elements.processview.description.commands;

import jsl.simulation.ModelElement;
import jsl.modeling.elements.processview.description.ProcessCommand;

import jsl.modeling.elements.processview.description.ProcessExecutor;
import jsl.modeling.elements.entity.*;
import jsl.modeling.elements.variable.*;
import jsl.modeling.queue.Queue;

/**
 *
 */
public class Seize extends ProcessCommand {

    private Variable myAmtRequested;

    private Resource myResource;

    private int myPriority;

    private Queue<Entity> myQueue;

    private final AllocationListener myAllocationListener;

    public Seize(ModelElement parent, Variable amountRequested, Resource resource, Queue<Entity> queue) {
        this(parent, amountRequested, resource, queue, 1, null);
    }

    public Seize(ModelElement parent, Variable amountRequested, Resource resource, Queue<Entity> queue, int priority) {
        this(parent, amountRequested, resource, queue, priority, null);
    }

    public Seize(ModelElement parent, Variable amountRequested, Resource resource, Queue<Entity> queue, String name) {
        this(parent, amountRequested, resource, queue, 1, name);
    }

    public Seize(ModelElement parent, Variable amountRequested, Resource resource, Queue<Entity> queue, int priority, String name) {
        super(parent, name);
        myAllocationListener = new AllocationListener();
        setAmountRequested(amountRequested);
        setResource(resource);
        setQueue(queue);
        setPriority(priority);
    }

    /** Gets the queuing priority associated with this QObject
     * @return The priority as an integer
     */
    public final int getPriority() {
        return (myPriority);
    }

    /** Returns the queue that the QObject was last enqueued within
     *  
     * @return The Queue, or null if no queue
     */
    public final Queue getQueue() {
        return (myQueue);
    }

    @Override
    public void execute() {

        int amt = (int) myAmtRequested.getValue();
        Entity entity = getProcessExecutor().getCurrentEntity();
        
        // enqueue arriving entity
        myQueue.enqueue(entity);

        myResource.seize(entity, amt, myPriority, myAllocationListener);

        // check if entity was queued
        if (entity.isQueued()) { // suspend the executor at this seize command, otherwise continue
            getProcessExecutor().suspend();
        }
    }

    class AllocationListener implements AllocationListenerIfc {

        public void allocated(Request request) {
            if (request.isSatisfied()) {
                Entity entity = request.getEntity();
                myQueue.remove(entity);
                // get the entity's process executor
                ProcessExecutor pe = entity.getProcessExecutor();
                if (pe.isSuspended()) {
                    // schedule the entity's process executor to resume, now
                    scheduleResume(pe, 0.0, 1, "Resume Seize");
                }
            }
        }
    }

    /** Sets the priority for this Seize
     *  Changing the priority while the object is in a queue
     *  has no effect on the ordering of the queue.  This priority is
     *  only used to determine competition between multiple seizes
     *  of the same resource
     * 
     * @param priority lower priority implies earlier ranking in the queue
     */
    protected final void setPriority(int priority) {
        myPriority = priority;
    }

    protected final void setAmountRequested(Variable amountRequested) {

        if (amountRequested == null) {
            throw new IllegalArgumentException("Variable amountRequested was equal to null!");
        }

        if (amountRequested.getInitialValue() <= 0) {
            throw new IllegalArgumentException("Amount requested was less or equal to zero!");
        }

        myAmtRequested = amountRequested;
    }

    protected final void setResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource was equal to null!");
        }
        myResource = resource;
    }

    protected final void setQueue(Queue<Entity> queue) {
        if (queue == null) {
            throw new IllegalArgumentException("Queue was equal to null!");
        }
        myQueue = queue;
    }
}
