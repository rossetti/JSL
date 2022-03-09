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
package examples.book.chapter6;

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;

import java.util.Objects;

/**
 *  This model element illustrates how to model a simple multiple server
 *  queueing system. The number of servers can be supplied. In
 *  addition, the user can supply the distribution associated with the time
 *  between arrivals and the service time distribution.
 *  Statistics are collected on the average number of busy servers,
 *  the average number of customers in the system, the average number of customers waiting,
 *  and the number of customers served.
 */
public class DriveThroughPharmacy extends SchedulingElement {

    private int myNumPharmacists;
    private final RandomVariable myServiceRV;
    private final RandomVariable myArrivalRV;
    private final TimeWeighted myNumBusy;
    private final TimeWeighted myNS;
    private final TimeWeighted myQ;
    private final ArrivalEventAction myArrivalEventAction;
    private final EndServiceEventAction myEndServiceEventAction;
    private final Counter myNumCustomers;

    public DriveThroughPharmacy(ModelElement parent) {
        this(parent, 1,
                new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public DriveThroughPharmacy(ModelElement parent, int numPharmacists) {
        this(parent, numPharmacists, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public DriveThroughPharmacy(ModelElement parent, int numPharmacists,
                                RandomIfc timeBtwArrivals, RandomIfc serviceTime) {
        super(parent);
        Objects.requireNonNull(timeBtwArrivals, "The time between arrivals must not be null");
        Objects.requireNonNull(serviceTime, "The service time must not be null");
        if (numPharmacists <= 0){
            throw new IllegalArgumentException("The number of pharmacists must be >= 1");
        }
        myNumPharmacists = numPharmacists;
        myArrivalRV = new RandomVariable(this, timeBtwArrivals, "Arrival RV");
        myServiceRV = new RandomVariable(this, serviceTime, "Service RV");
        myQ = new TimeWeighted(this, "PharmacyQ");
        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");
        myNS = new TimeWeighted(this, 0.0, "# in System");
        myNumCustomers = new Counter(this, "Num Served");
        myArrivalEventAction = new ArrivalEventAction();
        myEndServiceEventAction = new EndServiceEventAction();
    }

    public final int getNumberOfPharmacists() {
        return (myNumPharmacists);
    }

    public final void setNumberOfPharmacists(int numPharmacists) {
        if (numPharmacists <= 0){
            throw new IllegalArgumentException("The number of pharmacists must be >= 1");
        }
        myNumPharmacists = numPharmacists;
    }

    public final void setServiceTimeRandomSource(RandomIfc serviceTime) {
        Objects.requireNonNull(serviceTime, "The service time source must not be null");
        myServiceRV.setInitialRandomSource(serviceTime);
    }

    public final void setTimeBtwArrivalRandomSource(RandomIfc timeBtwArrivals) {
        Objects.requireNonNull(timeBtwArrivals, "The time between arrivals source must not be null");
        myArrivalRV.setInitialRandomSource(timeBtwArrivals);
    }

    @Override
    protected void initialize() {
        super.initialize();
        // start the arrivals
        scheduleEvent(myArrivalEventAction, myArrivalRV);
    }

    private class ArrivalEventAction extends EventAction {

        @Override
        public void action(JSLEvent event) {
            myNS.increment(); // new customer arrived
            if (myNumBusy.getValue() < myNumPharmacists) { // server available
                myNumBusy.increment(); // make server busy
                // schedule end of service
                scheduleEvent(myEndServiceEventAction, myServiceRV);
            } else {
                myQ.increment(); // customer must wait
            }
            // always schedule the next arrival
            scheduleEvent(myArrivalEventAction, myArrivalRV);
        }
    }


    private class EndServiceEventAction extends EventAction {

        @Override
        public void action(JSLEvent event) {
            myNumBusy.decrement(); // customer is leaving server is freed
            if (myQ.getValue() > 0) { // queue is not empty
                myQ.decrement();//remove the next customer
                myNumBusy.increment(); // make server busy
                // schedule end of service
                scheduleEvent(myEndServiceEventAction, myServiceRV);
            }
            myNS.decrement(); // customer left system
            myNumCustomers.increment();
        }
    }

}
