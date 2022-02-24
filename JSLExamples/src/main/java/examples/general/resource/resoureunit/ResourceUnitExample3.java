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
import jsl.modeling.resource.*;
import jsl.modeling.resource.Request.PreemptionRule;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.UniformRV;

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
 *  This example, different from ResourceUnitExample2 by illustrating how to model a resource that
 *  experiences failures. In the results you can see that some failure time has accumulated for the server.
 *
 */
public class ResourceUnitExample3 extends SchedulingElement {

    private final Queue<QObject> myWaitingQ;
    private RandomIfc myServiceRS;
    private RandomIfc myArrivalRS;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private final TimeWeighted myNumBusy;
    private final TimeWeighted myNS;
    private final ResponseVariable mySysTime;
    private final ResourceUnit myResource;
    private Request myPreemptedRequest;
    private final RequestInteraction myRequestInteraction = new RequestInteraction();

    public ResourceUnitExample3(ModelElement parent) {
        this(parent, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public ResourceUnitExample3(ModelElement parent, RandomIfc ad, RandomIfc sd) {
        super(parent);
        setServiceRS(sd);
        setArrivalRS(ad);
        myWaitingQ = new Queue<>(this, "ServerQ");
        myResource = new ResourceUnit.Builder(this)
                .name("Server")
                .collectRequestQStats()
                //.allowFailuresToDelay()
                .collectStateStatistics()
                .build();

        // c1 represents the time to first failure and the time between failures
        ConstantRV c1 = new ConstantRV(3.0);
        // add the failure element to the resource as a time based failure
        // a time based failure can start at a time in this case at time 3, and last
        // for a duration, in this case 2 time units, and the reoccur
        TimeBasedFailure timeBasedFailure = myResource.addTimeBasedFailure(ConstantRV.TWO, c1, c1);
        // here I'm creating a failure event that occurs only once for a particular duration
        RandomIfc duration = new UniformRV(4, 5);
        RandomIfc timeToFailure = new UniformRV(5, 7);
        ResourceSingleFailureEvent mfe = new ResourceSingleFailureEvent(this, duration, timeToFailure);
        mfe.addResourceUnit(myResource);
        mfe.addFailureEventListener(new FailureEventListener());

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

    /**
     *  You can listen for the failure events and react to them if needed
     */
    protected class FailureEventListener implements FailureEventListenerIfc{

        @Override
        public void failureStarted() {
            //JSL.out.println("The failureStarted event occurred!");
        }

        @Override
        public void failureCompleted() {
            //JSL.out.println("The failureCompleted event occurred!");
        }
    }

    @Override
    protected void initialize() {
        super.initialize();
        schedule(this::arrival).in(myArrivalRV).units();
    }

    private void arrival(JSLEvent<QObject> evt) {
        myNS.increment(); // new customer arrived
        QObject arrivingCustomer = new QObject(getTime());
        // because we have failure we need to specify how a request will react if
        // there is a failure during its usage of the resource
        Request request = Request.builder()
                .createTime(getTime())
                .reactor(myRequestInteraction)
                .entity(arrivingCustomer)
                .duration(myServiceRS)
                .rule(PreemptionRule.RESUME)
                .build();

        Request seize = myResource.seize(request);
        schedule(this::arrival).in(myArrivalRV).units();

    }

    /**
     *  Shows all the state changes that can be reacted to on a request
     */
    private class RequestInteraction implements RequestReactorIfc {

        @Override
        public void prepared(Request request) {
//            JSL.out.println(getTime() + "> Request " + request + " is prepared.");
        }

        @Override
        public void dequeued(Request request, Queue<Request> queue) {
            myWaitingQ.remove((QObject) request.getAttachedObject());
//            JSL.out.println(getTime() + "> Request " + request + " exited queue.");
        }

        @Override
        public void enqueued(Request request, Queue<Request> queue) {
            myWaitingQ.enqueue((QObject) request.getAttachedObject());
//            JSL.out.println(getTime() + "> Request " + request + " entered queue.");
        }

        @Override
        public void canceled(Request request) {
            // if canceled from allocated state then resource is no longer busy
            if (request.isPreviousStateAllocated()) {
                myNumBusy.decrement();
            }
//            JSL.out.println(getTime() + "> Request " + request + " was canceled.");
        }

        @Override
        public void preempted(Request request) {
            // if preempted the resource is no longer busy
            myNumBusy.decrement();
            myPreemptedRequest = request; // could capture the request for later processing
//            JSL.out.println(getTime() + "> Request " + request + " was preempted.");
        }

        @Override
        public void resumed(Request request) {
            myNumBusy.increment();
            myPreemptedRequest = null;
//            JSL.out.println(getTime() + "> Request " + request + " resumed using resource.");
        }

        @Override
        public void allocated(Request request) {
            myNumBusy.increment();
//            JSL.out.println(getTime() + "> Request " + request + " allocated resource.");
        }

        @Override
        public void completed(Request request) {
            myNumBusy.decrement();
//            JSL.out.println(getTime() + "> Request " + request + " completed.");
            mySysTime.setValue(getTime() - request.getCreateTime());
            myNS.decrement(); // customer left system  
        }

    }

    public static void main(String[] args) {
        Simulation sim = new Simulation("ResourceUnitExample3_Testing");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        ResourceUnitExample3 example3 = new ResourceUnitExample3(m);
        example3.setArrivalRS(new ExponentialRV(6.0));
        example3.setServiceRS(new ExponentialRV(3.0));

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
