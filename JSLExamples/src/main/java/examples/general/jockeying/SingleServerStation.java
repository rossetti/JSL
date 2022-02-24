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
package examples.general.jockeying;

import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.queue.QueueResponse;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;

import java.util.Optional;

/**
 */
public class SingleServerStation extends SchedulingElement {

    public final static int BUSY = 1;

    public final static int IDLE = 0;

    protected Queue<QObject> myQueue;

    protected TimeWeighted myServerStatus;

    protected ResponseVariable mySystemTime;

    protected EndServiceListener myEndServiceListener;

    protected RandomIfc myServiceDistribution;

    protected RandomVariable myServiceRV;

    protected FastFoodRestaurant myFastFoodRestaurant;

    public SingleServerStation(ModelElement parent, FastFoodRestaurant restaurant) {
        this(parent, restaurant, null);
    }

    public SingleServerStation(ModelElement parent, FastFoodRestaurant restaurant, String name) {
        super(parent, name);
        setServiceDistributionInitialRandomSource(ConstantRV.ONE);
        myFastFoodRestaurant = restaurant;
        myQueue = new Queue<>(this, getName() + " Queue");
        myServerStatus = new TimeWeighted(this, getName() + " Server");
        mySystemTime = new ResponseVariable(this, getName() + " System Time");
        myEndServiceListener = new EndServiceListener();
    }

    public final int getNumInQueue() {
        return (myQueue.size());
    }

    public final boolean isIdle() {
        return (myServerStatus.getValue() == IDLE);
    }

    protected final QObject removeLastCustomer() {
        QObject c = myQueue.peekLast();
        myQueue.remove(c, false);
        return (c);
    }

    public final Optional<QueueResponse<QObject>> getQueueResponses() {
        return myQueue.getQueueResponses();
    }

    public final void setServiceDistributionInitialRandomSource(RandomIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("Service Time Distribution was null!");
        }

        myServiceDistribution = d;

        if (myServiceRV == null) { // not made yet
            myServiceRV = new RandomVariable(this, myServiceDistribution);
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(myServiceDistribution);
        }

    }

    protected void receive(QObject customer) {
        if (customer == null) {
            throw new IllegalArgumentException("The custoemr must be non-null!");
        }

        // enqueue arriving customer
        myQueue.enqueue(customer);
        if (isIdle()) {
            if (myQueue.peekNext() == customer) {
                myQueue.removeNext();
                myServerStatus.setValue(BUSY);
                scheduleEvent(myEndServiceListener, myServiceRV, customer);
            }
        }

    }

    protected class EndServiceListener implements EventActionIfc<QObject> {

        public void action(JSLEvent<QObject> event) {
            QObject e = event.getMessage();
            // get the time
            double ws = getTime() - e.getTimeEnteredQueue();
            mySystemTime.setValue(ws);
            // tell the server to be released
            myServerStatus.setValue(IDLE);
            if (myQueue.isNotEmpty()) {
                QObject next = myQueue.removeNext();
                myServerStatus.setValue(BUSY);
                scheduleEvent(myEndServiceListener, myServiceRV, next);
            }

            // customer is departing a station, check for jockey opportunity
            myFastFoodRestaurant.checkForJockey(SingleServerStation.this);
        }
    }
}
