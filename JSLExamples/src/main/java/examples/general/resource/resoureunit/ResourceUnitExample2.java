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
package examples.general.resource.resoureunit;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.resource.Request;
import jsl.modeling.resource.RequestReactorAdapter;
import jsl.modeling.resource.ResourceUnit;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 *  This example illustrate the use of a ResourceUnit via a simple M/M/1 queue.
 *
 *  It is important to understand that a Request has everything it needs to work with
 *  the resource unit, including the service time.  When the request is allocated to the
 *  resource unit, the time required for the request is used to determine the amount
 *  of time that the resource is used and after that time the request is completed.
 *
 *  The resource unit causes state changes that can be reacted to via the
 *  RequestReactorIfc interface or the use of RequestReactorAdapter. If the resource unit is not
 *  available, the resource unit will automatically queue the request in an internal request queue
 *  associated with the resource unit. The request is placed in the waiting state. A state change for
 *  the request occurs when it is enqueued.  When the unit becomes available the request is removed from the
 *  waiting state (dequeued state change) and is allocated the resource, after allocation the request begins
 *  using the resource unit according to the time provided on the request. The request is consider to be
 *  in the allocated state.  After the requested time is over, the request is placed in the completed state.
 *
 *  Again, user of the Request can listen to the state changes via the RequestReactorIfc interface. An
 *  instance of a class implementing this interface must be supplied when the request is made so that
 *  the call-backs can occur during the state changes.  Whether or not the reactor does anything with
 *  the notification is up to the client.
 *
 *  This example, different from ResourceUnitExample1 illustrated the mirroring of the queueing by
 *  using the request reactor.  When a request is made, an object can be associated with the request.
 *  In this example code, a QObject representing a customer is associated with the request.
 *  The code then uses a separate Queue instance to enqueue and dequeue the customer when the customer's
 *  associated request experiences is notified that it is enqueue or dequeued.  From the statistics, you can
 *  see that the results are the same. Similarly a time weighted variable is used to track whether or
 *  not the resource is busy based on the allocated and completed state changes on the request.
 */
public class ResourceUnitExample2 extends SchedulingElement {

    private final Queue<QObject> myWaitingQ;
    private RandomIfc myServiceRS;
    private RandomIfc myArrivalRS;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private TimeWeighted myNumBusy;
    private TimeWeighted myNS;
    private ResponseVariable mySysTime;
    private ResourceUnit myResource;
    private final RequestInteraction myRequestInteraction = new RequestInteraction();

    public ResourceUnitExample2(ModelElement parent) {
        this(parent, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public ResourceUnitExample2(ModelElement parent, RandomIfc ad, RandomIfc sd) {
        super(parent);
        setServiceRS(sd);
        setArrivalRS(ad);
        myWaitingQ = new Queue<>(this, "ServerQ");
        myResource = new ResourceUnit.Builder(this)
                .name("Server")
                .collectRequestQStats()
                .collectStateStatistics()
                .build();
        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");
        myNS = new TimeWeighted(this, 0.0, "# in System");
        mySysTime = new ResponseVariable(this, "System Time");
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
        myNS.increment(); // new customer arrived
        QObject arrivingCustomer = new QObject(getTime());
        Request request = Request.builder()
                .createTime(getTime())
                .reactor(myRequestInteraction)
                .entity(arrivingCustomer)
                .duration(myServiceRS)
                .build();

        myResource.seize(request);
        schedule(this::arrival).in(myArrivalRV).units();

    }

    private class RequestInteraction extends RequestReactorAdapter {

        @Override
        public void dequeued(Request request, Queue<Request> queue) {
            myWaitingQ.remove((QObject) request.getAttachedObject());
        }

        @Override
        public void enqueued(Request request, Queue<Request> queue) {
            myWaitingQ.enqueue((QObject) request.getAttachedObject());
        }

        @Override
        public void allocated(Request request) {
            myNumBusy.increment();
        }

        @Override
        public void completed(Request request) {
            myNumBusy.decrement();
            mySysTime.setValue(getTime() - request.getCreateTime());
            myNS.decrement(); // customer left system  
        }

    }

    public static void main(String[] args) {
        Simulation sim = new Simulation("ResourceUnitExample1_Testing");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        ResourceUnitExample2 example1 = new ResourceUnitExample2(m);
        example1.setArrivalRS(new ExponentialRV(6.0));
        example1.setServiceRS(new ExponentialRV(5.0));

        // set the parameters of the experiment
        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);

        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation ended!");
        sim.printHalfWidthSummaryReport();
    }

}
