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

import jsl.modeling.elements.Schedule;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.resource.Request;
import jsl.modeling.resource.Request.PreemptionRule;
import jsl.modeling.resource.RequestReactorIfc;
import jsl.modeling.resource.ResourceUnit;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.ExponentialRV;

import java.util.Objects;

/**
 *  This example uses a RequestPool w/o a default queue.  Thus, a separate queue is
 *  used to hold the objects associated with the requests.  This is an example of
 *  a simple M/M/c queuing situation. In this example, additional (not really needed)
 *  statistics are collected in order to further illustrate the request reactor concept.
 *
 *  This example has both failures and schedules attached to the resource pool.
 */
public class ResourcePoolExample3 extends SchedulingElement {

    private int myNumServers;
    private Queue<QObject> myWaitingQ;
    private RandomIfc myServiceRS;
    private RandomIfc myArrivalRS;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private TimeWeighted myNumBusy;
    private TimeWeighted myNS;
    private ResponseVariable mySysTime;
    private ResourceUnit myResource;
    private Request myPreemptedRequest;
    private final RequestReactor myRequestUser = new RequestReactor();

    public ResourcePoolExample3(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public ResourcePoolExample3(ModelElement parent, int numServers, RandomIfc ad, RandomIfc sd) {
        super(parent);
        setNumberOfPharmacists(numServers);
        setServiceRS(sd);
        setArrivalRS(ad);
        myWaitingQ = new Queue<>(this, "ServerQ");
        myResource = new ResourceUnit.Builder(this)
                .name("Server")
                .collectRequestQStats()
                .allowInactivePeriodsToDelay()
                .allowFailuresToDelay()
                .collectStateStatistics()
                .collectRequestStatistics()
                .build();

        Schedule s = new Schedule.Builder(this).startTime(0).length(480).build();
        s.addItem(60 * 2.0, 15, "break1");
        s.addItem(60 * 4, 30, "lunch");
        s.addItem(60 * 6, 15, "break2");
        myResource.useSchedule(s);

        ConstantRV c1 = new ConstantRV(100.0);
        myResource.addTimeBasedFailure(ConstantRV.TWO, c1);
        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");
        myNS = new TimeWeighted(this, 0.0, "# in System");
        mySysTime = new ResponseVariable(this, "System Time");
    }

    public int getNumberOfServers() {
        return (myNumServers);
    }

    private void setNumberOfPharmacists(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        myNumServers = n;
    }

    public final void setServiceRS(RandomIfc d) {
        Objects.requireNonNull(d,"Service Time RV was null!");
        myServiceRS = d;
        if (myServiceRV == null) { // not made yet
            myServiceRV = new RandomVariable(this, myServiceRS, "Service RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(myServiceRS);
        }
    }

    public final void setArrivalRS(RandomIfc d) {
        Objects.requireNonNull(d,"Arrival Time Distribution was null!");
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
                .reactor(myRequestUser)
                .entity(arrivingCustomer)
                .duration(myServiceRS)
                .rule(PreemptionRule.NONE)
                .build();

        Request seize = myResource.seize(request);
        schedule(this::arrival).in(myArrivalRV).units();
    }

   private class RequestReactor implements RequestReactorIfc {

        @Override
        public void prepared(Request request) {
//            JSL.out.println(getTime() + "> Request " + request + " is prepared.");
        }

        @Override
        public void dequeued(Request request, Queue<Request> queue) {
            myWaitingQ.remove((QObject)request.getAttachedObject());
//            JSL.out.println(getTime() + "> Request " + request + " exited queue.");
        }

        @Override
        public void enqueued(Request request, Queue<Request> queue) {
            myWaitingQ.enqueue((QObject)request.getAttachedObject());
//            JSL.out.println(getTime() + "> Request " + request + " entered queue.");
        }

        @Override
        public void canceled(Request request) {
            // if canceled the resource is no longer busy
            myNumBusy.decrement();
//            JSL.out.println(getTime() + "> Request " + request + " was canceled.");
        }

        @Override
        public void preempted(Request request) {
            // if preempted the resource is no longer busy
            myNumBusy.decrement();
            myPreemptedRequest = request;
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
//        JSL.out.OUTPUT_ON = false;
        Simulation sim = new Simulation("New Queue Testing");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        ResourcePoolExample3 driveThroughPharmacy = new ResourcePoolExample3(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(6.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(3.0));

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
