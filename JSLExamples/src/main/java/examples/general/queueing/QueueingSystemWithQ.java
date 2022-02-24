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
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.queue.QueueResponse;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.StatisticAccessorIfc;

import java.io.PrintWriter;
import java.util.Optional;

public class QueueingSystemWithQ extends SchedulingElement {

    private int myNumServers;

    private Queue<QObject> myWaitingQ;

    private RVariableIfc myServiceDistribution;

    private RVariableIfc myArrivalDistribution;

    private RandomVariable myServiceRV;

    private RandomVariable myArrivalRV;

    private TimeWeighted myNumBusy;

    private TimeWeighted myNS;

    private ArrivalAction myArrivalAction;

    private EndServiceAction myEndServiceAction;

    private ResponseVariable mySysTime;

    public QueueingSystemWithQ(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.5));
    }

    public QueueingSystemWithQ(ModelElement parent, int numServers, RVariableIfc ad, RVariableIfc sd) {
        super(parent);

        setNumberOfServers(numServers);
        setServiceDistributionInitialRandomSource(sd);
        setArrivalDistributionInitialRandomSource(ad);

        myWaitingQ = new Queue<>(this, getName() + "_Q");

        myNumBusy = new TimeWeighted(this, 0.0, getName() + "_NumBusy");

        myNS = new TimeWeighted(this, 0.0, getName() + "_NS");

        mySysTime = new ResponseVariable(this, getName() + "_System Time");

        myArrivalAction = new ArrivalAction();
        myEndServiceAction = new EndServiceAction();
    }

    public int getNumberOfServers() {
        return (myNumServers);
    }

    public final void setNumberOfServers(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }

        myNumServers = n;
    }

    public final void setServiceDistributionInitialRandomSource(RVariableIfc d) {

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

    public final void setArrivalDistributionInitialRandomSource(RVariableIfc d) {

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

    public final Optional<QueueResponse<QObject>> getQueueResponses() {
        return myWaitingQ.getQueueResponses();
    }

    public final StatisticAccessorIfc getNBAcrossReplicationStatistic() {
        return myNumBusy.getAcrossReplicationStatistic();
    }

    public final StatisticAccessorIfc getNSAcrossReplicationStatistic() {
        return myNS.getAcrossReplicationStatistic();
    }

    public boolean isAvailable() {
        return myNumBusy.getValue() < myNumServers;
    }

    @Override
    protected void initialize() {
        super.initialize();

        // start the arrivals
        scheduleEvent(myArrivalAction, myArrivalRV.getValue());
    }

    class ArrivalAction extends EventAction {

        public void action(JSLEvent event) {
            myNS.increment(); // new customer arrived
            QObject arrival = createQObject();
            myWaitingQ.enqueue(arrival); // enqueue the newly arriving customer
            if (isAvailable()) { // server available
                myNumBusy.increment(); // make server busy
                QObject customer = myWaitingQ.removeNext(); //remove the next customer
                //	schedule end of service, include the customer as the event's message
                scheduleEvent(myEndServiceAction, myServiceRV, customer);
            }
            //	always schedule the next arrival
            scheduleEvent(myArrivalAction, myArrivalRV);
        }
    }

    class EndServiceAction implements EventActionIfc<QObject> {

        public void action(JSLEvent<QObject> event) {
            QObject leavingCustomer = event.getMessage();
            mySysTime.setValue(getTime() - leavingCustomer.getCreateTime());
            myNS.decrement(); // customer departed
            myNumBusy.decrement(); // customer is leaving server is freed

            if (!myWaitingQ.isEmpty()) { // queue is not empty
                QObject customer = myWaitingQ.removeNext(); //remove the next customer
                myNumBusy.increment(); // make server busy
                //	schedule end of service
                scheduleEvent(myEndServiceAction, myServiceRV, customer);
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("QueueingSystemWithQ Model");

        runExperiment();

        System.out.println("Done!");

    }

    public static void runReplication() {

        Simulation sim = new Simulation("QueueingSystemWithQ");

        // create the model element and attach it to the main model
        new QueueingSystemWithQ(sim.getModel());

        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);

        sim.run();

        SimulationReporter r = sim.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();

        System.out.println(r);

    }

    public static void runBatchReplication() {

        Simulation sim = new Simulation("QueueingSystemWithQ");

        // create the model element and attach it to the main model
        new QueueingSystemWithQ(sim.getModel());

        sim.turnOnStatisticalBatching();
        StatisticalBatchingElement be = sim.getStatisticalBatchingElement().get();

        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        sim.run();

        System.out.println(be);
        System.out.println(sim);
        sim.getOutputDirectory().makePrintWriter("BatchStatistics.csv");
        PrintWriter w = sim.getOutputDirectory().makePrintWriter("BatchStatistics.csv");

        StatisticReporter statisticReporter = be.getStatisticReporter();
        StringBuilder csvStatistics = statisticReporter.getCSVStatistics(true);
        w.print(csvStatistics);

    }

    public static void runExperiment() {

        Simulation sim = new Simulation("QueueingSystemWithQ");

        // create the model element and attach it to the main model
        new QueueingSystemWithQ(sim.getModel());

        // set the parameters of the experiment
        sim.setNumberOfReplications(30);

        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);

        SimulationReporter r = sim.makeSimulationReporter();
        r.turnOnReplicationCSVStatisticReporting("ReplicationData");
        // tell the experiment to run
        sim.run();
        System.out.println(sim);
        r.printAcrossReplicationSummaryStatistics();
    }
}
