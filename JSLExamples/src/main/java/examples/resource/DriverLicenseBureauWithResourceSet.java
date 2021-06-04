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
package examples.resource;

import jsl.modeling.queue.Queue;
import jsl.modeling.elements.entity.Allocation;
import jsl.modeling.elements.entity.Entity;
import jsl.modeling.elements.entity.Request;
import jsl.modeling.elements.entity.Resource;
import jsl.modeling.elements.entity.AllocationListenerIfc;
import jsl.modeling.elements.entity.ResourceSet;
import jsl.modeling.elements.variable.*;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;

public class DriverLicenseBureauWithResourceSet extends SchedulingElement {

    private ResourceSet myResourceSet;

    private Queue<Entity> myWaitingQ;

    private RVariableIfc myServiceDistribution;

    private RVariableIfc myArrivalDistribution;

    private RandomVariable myServiceRV;

    private RandomVariable myArrivalRV;

    private TimeWeighted myNS;

    private ArrivalEventAction myArrivalEventAction;

    private AllocationListener myBeginServiceListener;

    private EndServiceEventAction myEndServiceEventAction;

    public DriverLicenseBureauWithResourceSet(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public DriverLicenseBureauWithResourceSet(ModelElement parent, int numServers,
                                              RVariableIfc ad, RVariableIfc sd) {
        super(parent);

        setServiceDistributionInitialRandomSource(sd);
        setArrivalDistributionInitialRandomSource(ad);

        myWaitingQ = new Queue<>(this, "DriverLicenseQ");
        myResourceSet = new ResourceSet(this, "ResourceSet");
        myResourceSet.addResource(numServers, "Clerks");
        myNS = new TimeWeighted(this, 0.0, "NS");

        myArrivalEventAction = new ArrivalEventAction();
        myEndServiceEventAction = new EndServiceEventAction();
        myBeginServiceListener = new AllocationListener();
    }

    public void setServiceDistributionInitialRandomSource(RVariableIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("Service Time Distribution was null!");
        }

        myServiceDistribution = d;

        if (myServiceRV == null) { // not made yet
            myServiceRV = new RandomVariable(this, myServiceDistribution, "Service RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(myServiceDistribution);
        }

    }

    public void setArrivalDistributionInitialRandomSource(RVariableIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("Arrival Time Distribution was null!");
        }

        myArrivalDistribution = d;

        if (myArrivalRV == null) { // not made yet
            myArrivalRV = new RandomVariable(this, myArrivalDistribution, "Arrival RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myArrivalRV.setInitialRandomSource(myArrivalDistribution);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        // start the arrivals
        scheduleEvent(myArrivalEventAction, myArrivalRV);
    }

    class ArrivalEventAction extends EventAction {

        @Override
        public void action(JSLEvent event) {
            myNS.increment(); // new customer arrived
            // create the customer
            Entity customer = createEntity();

            // enqueue arriving customer
            myWaitingQ.enqueue(customer);

            myResourceSet.seize(customer, myBeginServiceListener);

            //	always schedule the next arrival
            scheduleEvent(myArrivalEventAction, myArrivalRV);
        }
    }

    class AllocationListener implements AllocationListenerIfc {

        @Override
        public void allocated(Request request) {
            if (request.isSatisfied()) {
                Entity customer = request.getEntity();
                myWaitingQ.remove(customer);
                Resource resource = request.getSeizedResource();
                Allocation a = resource.allocate(customer, request.getAmountAllocated());
                scheduleEvent(myEndServiceEventAction, myServiceRV, a);
            }
        }
    }

    class EndServiceEventAction implements EventActionIfc<Allocation> {

        public void action(JSLEvent<Allocation> event) {
            //System.out.println(" in end service listener");
            myNS.decrement(); // customer is departing
            Allocation a = event.getMessage();
            Resource r = a.getAllocatedResource();
            r.release(a);
        }
    }

    public static void main(String[] args) {
        System.out.println("Driver License Bureau With Resource Set Example");

        Simulation s = new Simulation("DLB with Resource");

        // create the model element and attach it to the main model
        new DriverLicenseBureauWithResourceSet(s.getModel());

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        // tell the experiment to run
        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printAcrossReplicationSummaryStatistics();

        System.out.println("Done!");

    }
}
