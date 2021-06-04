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

public class DTPQueueResourcePoolWithQModel extends SchedulingElement {

    private int myNumPharmacists;
    private Queue<QObject> myWaitingQ;
    private RandomIfc myServiceRS;
    private RandomIfc myArrivalRS;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private TimeWeighted myNumBusy;
    private TimeWeighted myNS;
    private ResponseVariable mySysTime;
    private ResourcePoolWithQ myResourcePool;
    private final RequestReactorIfc myRequestReactor = new RequestReactor();

    public DTPQueueResourcePoolWithQModel(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public DTPQueueResourcePoolWithQModel(ModelElement parent, int numServers) {
        this(parent, numServers, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public DTPQueueResourcePoolWithQModel(ModelElement parent, int numServers,
            RandomIfc ad, RandomIfc sd) {
        super(parent);
        setServiceRS(sd);
        setArrivalRS(ad);
        myWaitingQ = new Queue<>(this, "PharmacyQ");
        List<ResourceUnit> units = new ResourceUnit.Builder(this)
                .name("Server")
                .collectRequestQStats()
                .build(numServers);
        myResourcePool = new ResourcePoolWithQ(this, units, true, true, "Pharmacists");
        // myResource = new ResourceUnit.Builder(this).name("Server").builder();
        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");
        myNS = new TimeWeighted(this, 0.0, "# in System");
        mySysTime = new ResponseVariable(this, "System Time");
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
                .reactor(myRequestReactor)
                .entity(arrivingCustomer)
                .duration(myServiceRS)
                .build();

        myResourcePool.seize(request);

//        seize(myResource).using(myRequestReactor).with(arrivingCustomer)
//                .forDuration(myServiceRS).timeUnits();
//        
        schedule(this::arrival).in(myArrivalRV).units();

    }

    private class RequestReactor extends RequestReactorAdapter {

        @Override
        public void dequeued(Request request, Queue<Request> queue) {
            if (myResourcePool.isQueue(queue)) {
                myWaitingQ.remove((QObject) request.getAttachedObject());
            }
            JSL.getInstance().out.println(getTime() + "> Request " + request + " exited queue.");
        }

        @Override
        public void enqueued(Request request, Queue<Request> queue) {
            if (myResourcePool.isQueue(queue)) {
                myWaitingQ.enqueue((QObject) request.getAttachedObject());
            }
            JSL.getInstance().out.println(getTime() + "> Request " + request + " entered queue.");
        }

        @Override
        public void allocated(Request request) {
            myNumBusy.increment();
            JSL.getInstance().out.println(getTime() + "> Request " + request + " allocated.");
        }

        @Override
        public void completed(Request request) {
            JSL.getInstance().out.println(getTime() + "> Request " + request + " completed.");
            myNumBusy.decrement();
            mySysTime.setValue(getTime() - request.getCreateTime());
            myNS.decrement(); // customer left system  
        }

    }

    public static void main(String[] args) {
        JSL.getInstance().out.OUTPUT_ON = false;
        Simulation sim = new Simulation("New Queue Testing");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        int numServers = 1;
        DTPQueueResourcePoolWithQModel dtp = new DTPQueueResourcePoolWithQModel(m, numServers);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(5.0));

        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
//        sim.setNumberOfReplications(2);
//        sim.setLengthOfReplication(1000.0);
//        sim.setLengthOfWarmUp(5.0);

        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        r.printAcrossReplicationSummaryStatistics();
    }

}
