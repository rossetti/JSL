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
package examples.resource.resoureunit;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.resource.*;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.reporting.JSL;

import java.util.List;

/**
 *  This example uses a RequestPool w/o a default queue.  Thus, a separate queue is
 *  used to hold the objects associated with the requests.  This is an example of
 *  a simple M/M/c queuing situation. In this example, additional (not really needed)
 *  statistics are collected in order to further illustrate the request reactor concept.
 *
 *  This example illustrates how the reactor for the request can be used to react to state
 *  changes on the request.
 */
public class ResourcePoolExample2 extends SchedulingElement {

    private final int myNumServers;
    private final Queue<QObject> myWaitingQ;
    private RandomIfc myServiceRS;
    private RandomIfc myArrivalRS;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private final TimeWeighted myNumBusy;
    private final TimeWeighted myNS;
    private final ResponseVariable mySysTime;
    private final ResourcePool myResourcePool;
    private final RequestReactorIfc myRequestReactor = new RequestReactor();

    public ResourcePoolExample2(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public ResourcePoolExample2(ModelElement parent, int numServers) {
        this(parent, numServers, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public ResourcePoolExample2(ModelElement parent, int numServers,
                                RandomIfc ad, RandomIfc sd) {
        super(parent);
        setServiceRS(sd);
        setArrivalRS(ad);
        myNumServers = numServers;
        myWaitingQ = new Queue<>(this, "ServersQ");
        List<ResourceUnit> units = new ResourceUnit.Builder(this)
                .name("Server")
                .build(numServers);
        myResourcePool = new ResourcePool(this, units, true, "Servers");
        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");
        myNS = new TimeWeighted(this, 0.0, "# in System");
        mySysTime = new ResponseVariable(this, "System Time");
    }

    public int getNumberOfServers() {
        return (myNumServers);
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
        schedule(this::arrival).in(myArrivalRV).units();
    }

    private void arrival(JSLEvent<QObject> evt) {
        // new customer arrived
        myNS.increment();
        // create a customer that "makes the request" for service
        QObject arrivingCustomer = new QObject(getTime());
        // put the customer in the queue
        myWaitingQ.enqueue(arrivingCustomer);
        // check if units are available
        if (myResourcePool.hasIdleUnits()) {
            // immediately remove the customer from the queue
            myWaitingQ.remove(arrivingCustomer);
            // get the idle unit from the pool
            ResourceUnit ru = myResourcePool.selectResourceUnit();
            // directly seize the resource unit and associate the customer with the request
            ru.seize(myRequestReactor, myServiceRS, arrivingCustomer);
        }
        schedule(this::arrival).in(myArrivalRV).units();

    }

    /**
     *  A request reactor "reacts" to a number of state changes on the request such
     *  as starting service and completing service
     */
    private class RequestReactor extends RequestReactorAdapter {

        @Override
        public void allocated(Request request) {
            // indicate that the shadow variable tracking number busy is busy
            myNumBusy.increment();
        }

        @Override
        public void completed(Request request) {
            myNumBusy.decrement();
            // request is done, check if customer is waiting
            if (myWaitingQ.isNotEmpty()) {
                // get the next customer
                QObject nextCustomer = myWaitingQ.removeNext();
                // get the resource unit
                ResourceUnit ru = myResourcePool.selectResourceUnit();
                // seize the unit and associated the customer with the request
                ru.seize(myRequestReactor, myServiceRS, nextCustomer);
                // we don't increment number busy here because that is done when the request is allocated
            }
            // collect statistics on the departing customer
            QObject departingCustomer = (QObject)request.getAttachedObject();
            mySysTime.setValue(getTime() - departingCustomer.getCreateTime());
            myNS.decrement(); // customer left system  
        }

    }

    public static void main(String[] args) {
        Simulation sim = new Simulation("ResourcePoolExample2_Testing");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        int numServers = 2;
        ResourcePoolExample2 dtp = new ResourcePoolExample2(m, numServers);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(5.0));

        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);

        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation ended!");
        // we should get the same results as ResourcePoolExample1
        sim.printHalfWidthSummaryReport();
    }

}
