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
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.queue.Queue;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;

/**
 *
 * @author rossetti
 */
public class WorkStation extends EntityReceiver {

    protected Queue<Entity> myQueue;

    protected Resource myResource;

    protected ResponseVariable mySystemTime;

    protected RandomVariable myServiceRV;

    protected EndServiceListener myEndServiceListener;

    public WorkStation(ModelElement parent) {
        this(parent, null, 1);
    }

    public WorkStation(ModelElement parent, String name) {
        this(parent, name, 1);
    }

    public WorkStation(ModelElement parent, String name, int numServers) {
        super(parent, name);
        setServiceTimeInitialRandomSource(ConstantRV.ONE);
        myQueue = new Queue<>(this, getName() + "_Q");
        myResource = new Resource(this, numServers, getName() + "_R");
        mySystemTime = new ResponseVariable(this, getName() + "_SystemTime");
        myEndServiceListener = new EndServiceListener();
    }

    /**
     * 
     * @param d
     */
    public final void setServiceTimeInitialRandomSource(RandomIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("Service Time Distribution was null!");
        }

        if (myServiceRV == null) { // not made yet
            myServiceRV = new RandomVariable(this, d);
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(d);
        }

    }

    @Override
    protected void receive(Entity entity) {
        // an entity is arriving to the workcenter
        // enqueue arriving customer

        myQueue.enqueue(entity);
        if (myResource.hasAvailableUnits()) {
            if (myQueue.peekNext() == entity) {
                myQueue.removeNext();
                myResource.allocate(entity);
                scheduleEvent(myEndServiceListener, myServiceRV, entity);
            }

        }
    }

    protected class EndServiceListener implements EventActionIfc<Entity> {

        public void action(JSLEvent<Entity> event) {

            // get the departing entity
            Entity departingEntity = event.getMessage();

            // get the time in the work center
            double ws = getTime() - departingEntity.getTimeEnteredQueue();
            mySystemTime.setValue(ws);

            // tell the resource to release the request
            departingEntity.release(myResource);

            if (myQueue.isNotEmpty()) {
                Entity e = (Entity) myQueue.removeNext();
                myResource.allocate(e);
                scheduleEvent(myEndServiceListener, myServiceRV, e);
            }

            // send the entity to its next receiver
            sendEntity(departingEntity);

        }
    }
}
