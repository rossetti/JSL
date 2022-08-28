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
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 *  This model element illustrates how to model a simple multiple server
 *  queueing system. The number of servers can be supplied. In
 *  addition, the user can supply the distribution associated with the time
 *  between arrivals and the service time distribution.
 *  Statistics are collected on the average number of busy servers,
 *  the average number of customers in the system, the average system
 *  time, the average number of customers waiting, the average waiting
 *  time of the customers, and the number of customers served.
 */
public class DriveThroughPharmacyWithQ extends SchedulingElement {

    private int myNumPharmacists;
    private final Queue<QObject> myWaitingQ;
    private RandomIfc myServiceRS;
    private RandomIfc myArrivalRS;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private final TimeWeighted myNumBusy;
    private final TimeWeighted myNS;
    private final ResponseVariable mySysTime;
    private final ArrivalEventAction myArrivalEventAction;
    private final EndServiceEventAction myEndServiceEventAction;
    private final Counter myNumCustomers;

    public DriveThroughPharmacyWithQ(ModelElement parent) {
        this(parent, 1,
                new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public DriveThroughPharmacyWithQ(ModelElement parent, int numServers) {
        this(parent, numServers, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public DriveThroughPharmacyWithQ(ModelElement parent, int numServers, RandomIfc ad, RandomIfc sd) {
        super(parent);
        setNumberOfPharmacists(numServers);
        setServiceRS(sd);
        setArrivalRS(ad);
        myWaitingQ = new Queue<>(this, "PharmacyQ");
        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");
        myNS = new TimeWeighted(this, 0.0, "# in System");
        mySysTime = new ResponseVariable(this, "System Time");
        myNumCustomers = new Counter(this, "Num Served");
        myArrivalEventAction = new ArrivalEventAction();
        myEndServiceEventAction = new EndServiceEventAction();
    }

    public ResponseVariable getSystemTimeResponse() {
        return mySysTime;
    }

    public TimeWeighted getNumInSystemResponse() {
        return myNS;
    }

    public int getNumberOfServers() {
        return (myNumPharmacists);
    }

    public final void setNumberOfPharmacists(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }

        myNumPharmacists = n;
    }

    public final void setServiceRS(RandomIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("Service Time RV was null!");
        }

        myServiceRS = d;

        if (myServiceRV == null) { // not made yet
            myServiceRV = new RandomVariable(this, myServiceRS, "Service RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(myServiceRS);
        }

    }

    public final void setArrivalRS(RandomIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("Arrival Time Distribution was null!");
        }

        myArrivalRS = d;

        if (myArrivalRV == null) { // not made yet
            myArrivalRV = new RandomVariable(this, myArrivalRS, "Arrival RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myArrivalRV.setInitialRandomSource(myArrivalRS);
        }
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
            //	 schedule the next arrival
            scheduleEvent(myArrivalEventAction, myArrivalRV);
            enterSystem();
        }
    }

    private void enterSystem() {
        myNS.increment(); // new customer arrived
        QObject arrivingCustomer = new QObject(getTime());

        myWaitingQ.enqueue(arrivingCustomer); // enqueue the newly arriving customer
        if (myNumBusy.getValue() < myNumPharmacists) { // server available
            myNumBusy.increment(); // make server busy
            QObject customer = myWaitingQ.removeNext(); //remove the next customer
            // schedule end of service, include the customer as the event's message
            scheduleEvent(myEndServiceEventAction, myServiceRV, customer);
        }
    }

    private class EndServiceEventAction implements EventActionIfc<QObject> {

        @Override
        public void action(JSLEvent<QObject> event) {
            myNumBusy.decrement(); // customer is leaving server is freed
            if (!myWaitingQ.isEmpty()) { // queue is not empty
                QObject customer = myWaitingQ.removeNext(); //remove the next customer
                myNumBusy.increment(); // make server busy
                // schedule end of service
                scheduleEvent(myEndServiceEventAction, myServiceRV, customer);
            }
            departSystem(event.getMessage());
        }
    }

    private void departSystem(QObject departingCustomer) {
        mySysTime.setValue(getTime() - departingCustomer.getCreateTime());
        myNS.decrement(); // customer left system
        myNumCustomers.increment();
    }

    public static void main(String[] args) {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ dtp = new DriveThroughPharmacyWithQ(sim.getModel(), 1);
        dtp.setArrivalRS(new ExponentialRV(6.0, 1));
        dtp.setServiceRS(new ExponentialRV(3.0, 2));
        sim.run();
        SimulationReporter reporter = sim.makeSimulationReporter();
        reporter.printAcrossReplicationSummaryStatistics();
    }
}
