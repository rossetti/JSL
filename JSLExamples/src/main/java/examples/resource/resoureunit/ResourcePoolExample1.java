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
 * This is a simple example of creating a pool of resource units. The pool is used
 * as a set of servers for a simple M/M/c queuing system. In this example, the
 * request queue for the pool is used as holding the customers.
 *
 */
public class ResourcePoolExample1 extends SchedulingElement {

    private final int myNumServers;
    private RandomIfc myServiceRS;
    private RandomIfc myArrivalRS;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private final TimeWeighted myNS;
    private final ResponseVariable mySysTime;
    private final ResourcePoolWithQ myResourcePool;
    private final RequestReactorIfc myRequestReactor = new RequestReactor();

    public ResourcePoolExample1(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public ResourcePoolExample1(ModelElement parent, int numServers) {
        this(parent, numServers, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public ResourcePoolExample1(ModelElement parent, int numServers,
                                RandomIfc ad, RandomIfc sd) {
        super(parent);
        setServiceRS(sd);
        setArrivalRS(ad);
        myNumServers = numServers;
        List<ResourceUnit> units = new ResourceUnit.Builder(this)
                .name("Server")
                .build(numServers);
        myResourcePool = new ResourcePoolWithQ(this, units, true, true,"Servers");
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
        myNS.increment(); // new customer arrived
        // make the request on the pool
        Request request = Request.builder().createTime(getTime())
                .reactor(myRequestReactor)
                .duration(myServiceRV)
                .build();
        // seize the pool with the request. Since this pool has a queue it
        // takes care of what happens when the pool does not have any units available
        myResourcePool.seize(request);
        schedule(this::arrival).in(myArrivalRV).units();

    }

    /**
     * This request reactor is only here to illustrate how to have some "event" occur
     * after the request is completed.  Here I just capture some statistics on
     * the completed event.  If we didn't have to collect these statistics
     * an instance of RequestReactorAdapter could be used, which does nothing.
     */
    private class RequestReactor extends RequestReactorAdapter {

        @Override
        public void completed(Request request) {
            mySysTime.setValue(getTime() - request.getCreateTime());
            myNS.decrement(); // customer left system  
        }

    }

    public static void main(String[] args) {
        Simulation sim = new Simulation("ResourcePoolExample1_Testing");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        int numServers = 2;
        ResourcePoolExample1 dtp = new ResourcePoolExample1(m, numServers);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(5.0));

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
