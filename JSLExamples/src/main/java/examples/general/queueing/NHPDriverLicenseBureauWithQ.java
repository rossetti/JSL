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
package examples.general.queueing;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.elements.variable.nhpp.NHPPTimeBtwEventRV;
import jsl.modeling.elements.variable.nhpp.PiecewiseConstantRateFunction;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;

public class NHPDriverLicenseBureauWithQ extends SchedulingElement {

    private int myNumServers;

    private Queue<QObject> myWaitingQ;

    private RandomVariable myServiceRV;

    private NHPPTimeBtwEventRV myArrivalRV;

    private TimeWeighted myNumBusy;

    private TimeWeighted myNS;

    private ArrivalListener myArrivalListener;

    private EndServiceListener myEndServiceListener;

    public NHPDriverLicenseBureauWithQ(ModelElement parent) {
        this(parent, 1, null);
    }

    public NHPDriverLicenseBureauWithQ(ModelElement parent, int numServers, String name) {
        super(parent, name);

        setNumberOfServers(numServers);

        myServiceRV = new RandomVariable(this, new ExponentialRV(3));

        PiecewiseConstantRateFunction f = new PiecewiseConstantRateFunction(360.0, 0.006);
        f.addRateSegment(180.0, 0.033);
        f.addRateSegment(180.0, 0.041);
        f.addRateSegment(120.0, 0.194);
        f.addRateSegment(180.0, 0.079);
        f.addRateSegment(180.0, 0.167);
        f.addRateSegment(240.0, 0.05);

        // by default the schedule will rate function will repeat

        myArrivalRV = new NHPPTimeBtwEventRV(this, f);

        myWaitingQ = new Queue<>(this, "DriverLicenseQ");

        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");

        myNS = new TimeWeighted(this, 0.0, "NS");

        myArrivalListener = new ArrivalListener();

        myEndServiceListener = new EndServiceListener();
    }

    public int getNumberOfServers() {
        return (myNumServers);
    }

    protected void setNumberOfServers(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }

        myNumServers = n;
    }

    public void setServiceDistributionInitialRandomSource(RVariableIfc d) {
        myServiceRV.setInitialRandomSource(d);
    }

    @Override
    protected void initialize() {
        super.initialize();

        // start the arrivals
        scheduleEvent(myArrivalListener, myArrivalRV);
    }

    class ArrivalListener extends EventAction {

        @Override
        public void action(JSLEvent event) {
            myNS.increment(); // new customer arrived
            QObject arrival = createQObject();
            myWaitingQ.enqueue(arrival); // enqueue the newly arriving customer
            if (myNumBusy.getValue() < myNumServers) { // server available
                myNumBusy.increment(); // make server busy
                QObject customer = myWaitingQ.removeNext(); //remove the next customer
                //	schedule end of service, include the customer as the event's message
                scheduleEvent(myEndServiceListener, myServiceRV, customer);
            }
            //	always schedule the next arrival
            scheduleEvent(myArrivalListener, myArrivalRV);
        }
    }

    class EndServiceListener implements EventActionIfc<QObject> {

        @Override
        public void action(JSLEvent<QObject> event) {
            QObject leavingCustomer = event.getMessage();

            myNS.decrement(); // customer departed
            myNumBusy.decrement(); // customer is leaving server is freed

            if (!myWaitingQ.isEmpty()) { // queue is not empty
                QObject customer = myWaitingQ.removeNext(); //remove the next customer
                myNumBusy.increment(); // make server busy
                //	schedule end of service
                scheduleEvent(myEndServiceListener, myServiceRV, customer);
            }
        }
    }

    public static void main(String[] args) {
         test1();
         test2();

    }

    /** The service time is exponential with a mean of 3 minutes
     *  The arrival process is non-homogeneous Poisson for the following
     *
     *  Interval        duration (min)  rate per min
     *  12 am - 6 am    360             0.006
     *  6 am - 9 am     180             0.033
     *  9 am - 12 pm    180             0.041
     *  12 pm - 2 pm    120             0.194
     *  2 pm - 5 pm     180             0.079
     *  5 pm - 8 pm     180             0.167
     *  8 pm - 12 am    240             0.05
     *
     *
     *  Run for 10 days, no warm up
     */
    public static void test1() {
        System.out.println("NHP Driver License Bureau Test 1");
        // create the simulation to run the model
        Simulation s = new Simulation("NHP DLB Test 1");

        // create the model element and attach it to the main model
        NHPDriverLicenseBureauWithQ dlb = new NHPDriverLicenseBureauWithQ(s.getModel());

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(1440.0);

        // tell the simulation to run
        s.run();
        System.out.println("Done!");
        
        SimulationReporter r = new SimulationReporter(s);

        r.printAcrossReplicationSummaryStatistics();
    }

    /** The service time is exponential with a mean of 3 minutes
     *  The arrival process is non-homogeneous Poisson for the following
     *
     *  Interval        duration (min)  rate per min
     *  12 am - 6 am    360             0.006
     *  6 am - 9 am     180             0.033
     *  9 am - 12 pm    180             0.041
     *  12 pm - 2 pm    120             0.194
     *  2 pm - 5 pm     180             0.079
     *  5 pm - 8 pm     180             0.167
     *  8 pm - 12 am    240             0.05
     *
     *
     *  Run for 10 days, 1 day warm up, 1 day of observation
     */
    public static void test2() {
        System.out.println("NHP Driver License Bureau Test2");

        // create the simulation to run the model
        Simulation s = new Simulation("NHP DLB Test 2");

        // create the model element and attach it to the main model
        NHPDriverLicenseBureauWithQ dlb = new NHPDriverLicenseBureauWithQ(s.getModel());

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(2880.0);
        s.setLengthOfWarmUp(1440.0);

        // tell the experiment to run
        s.run();
        
        SimulationReporter r = new SimulationReporter(s);

        r.printAcrossReplicationSummaryStatistics();
        
        System.out.println("Done!");
    }
}
