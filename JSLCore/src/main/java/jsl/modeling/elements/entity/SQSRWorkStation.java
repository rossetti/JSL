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

import jsl.simulation.ModelElement;
import jsl.modeling.queue.Queue;
import jsl.modeling.elements.entity.Delay.DelayOption;
import jsl.utilities.random.RandomIfc;

/**
 *
 * @author rossetti
 */
public class SQSRWorkStation extends EntityReceiver {

    protected Queue<Entity> myQueue;

    protected Resource myResource;

    protected Delay myDelay;

    /** Default allocation listener for single resource
     *  or single resource set requirements
     *
     */
    protected AllocationListener myAllocationListener;

    public SQSRWorkStation(ModelElement parent) {
        this(parent, 1, null);
    }

    public SQSRWorkStation(ModelElement parent, String name){
        this(parent, 1, name);
    }

    public SQSRWorkStation(ModelElement parent, int numServers, String name) {
        super(parent, name);
        myQueue = new Queue<>(this, getName() + "_Q");
        myResource = new Resource(this, numServers, getName() + "_R");
        myDelay = new Delay(this, getName() + "_Delay");
        myDelay.setDirectEntityReceiver(new Release());
        myAllocationListener = new AllocationListener();
    }

    @Override
    protected void receive(Entity entity) {
        myQueue.enqueue(entity);
        myResource.seize(entity, myAllocationListener);
    }

    protected void startUsingResource(Entity entity) {
        myQueue.remove(entity);
        myDelay.receive(entity);
    }

    protected void endUsingResource(Entity entity) {
        entity.release(myResource);
        entity.setCurrentReceiver(this);
        sendEntity(entity);
    }

    protected class AllocationListener implements AllocationListenerIfc {

        public void allocated(Request request) {
            if (request.isSatisfied()) {
                Entity e = request.getEntity();
                Resource r = request.getSeizedResource();
                Allocation a = r.allocate(e, request.getAmountAllocated());
                startUsingResource(e);
            }
        }
    }

    protected class Release extends EntityReceiverAbstract {

        protected void receive(Entity entity) {
            endUsingResource(entity);
        }
    }

    public final void setDelayTime(RandomIfc distribution) {
        myDelay.setDelayTime(distribution);
    }

    public final void setDelayOption(DelayOption option) {
        myDelay.setDelayOption(option);
    }

    public final Delay getDelay(){
        return myDelay;
    }
}
