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

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.queue.QueueResponse;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.StatisticAccessorIfc;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;

public class DriverLicenseBureauWithQJ8 extends SchedulingElement {

    private int myNumServers;
    private Queue<QObject> myWaitingQ;
    private RandomIfc myServiceDistribution;
    private RandomIfc myArrivalDistribution;
    private RandomVariable myServiceRV;
    private RandomVariable myArrivalRV;
    private TimeWeighted myNumBusy;
    private TimeWeighted myNS;
    private Counter myNumServed;
    private ResponseVariable mySysTime;

    public DriverLicenseBureauWithQJ8(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.8));
    }

    public DriverLicenseBureauWithQJ8(ModelElement parent, int numServers, RandomIfc ad, RandomIfc sd) {
        super(parent);
        setNumberOfServers(numServers);
        setServiceDistributionInitialRandomSource(sd);
        setArrivalDistributionInitialRandomSource(ad);
        myWaitingQ = new Queue<>(this, "DriverLicenseQ");
        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");
        myNS = new TimeWeighted(this, 0.0, "NS");
        myNumServed = new Counter(this, "Num Served");
        mySysTime = new ResponseVariable(this, "System Time");
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

    public final void setServiceDistributionInitialRandomSource(RandomIfc d) {
        Objects.requireNonNull(d, "The supplied service time distribution was null");
        myServiceDistribution = d;
        if (myServiceRV == null) { // not made yet
            myServiceRV = new RandomVariable(this, myServiceDistribution, "Service RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(myServiceDistribution);
        }
    }

    public final void setArrivalDistributionInitialRandomSource(RandomIfc d) {
        Objects.requireNonNull(d, "The supplied arrival time distribution was null");
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

    @Override
    protected void initialize() {
        super.initialize();
        // start the arrivals
        scheduleEvent(this::arrival, myArrivalRV);
    }

    private void arrival(JSLEvent<QObject> event){
        myNS.increment(); // new customer arrived
        QObject arrival = createQObject();
        myWaitingQ.enqueue(arrival); // enqueue the newly arriving customer
        if (myNumBusy.getValue() < myNumServers) { // server available
            myNumBusy.increment(); // make server busy
            QObject customer = myWaitingQ.removeNext(); //remove the next customer
            //	schedule end of service, include the customer as the event's message
            scheduleEvent(this::endService, myServiceRV, customer);
        }
        //	always schedule the next arrival
        scheduleEvent(this::arrival, myArrivalRV);
    }

    private void endService(JSLEvent<QObject> event){
        QObject leavingCustomer = event.getMessage();
        mySysTime.setValue(getTime() - leavingCustomer.getCreateTime());
        myNS.decrement(); // customer departed
        myNumBusy.decrement(); // customer is leaving server is freed
        myNumServed.increment();

        if (!myWaitingQ.isEmpty()) { // queue is not empty
            QObject customer = myWaitingQ.removeNext(); //remove the next customer
            myNumBusy.increment(); // make server busy
            //	schedule end of service
            scheduleEvent(this::endService, myServiceRV, customer);
        }
    }

    public static void runReplication() {
        Simulation sim = new Simulation("DLB_with_Q_1_REP");

        // create the model element and attach it to the main model
        new DriverLicenseBureauWithQJ8(sim.getModel());

        sim.setLengthOfReplication(20.0);
        sim.setLengthOfWarmUp(5.0);

        //sim.turnOnDefaultEventTraceReport("check events");
        System.out.println("Running single replication");
        sim.run();
        System.out.println("Completed one single replication");

        SimulationReporter r = sim.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();

        System.out.println(r);

    }

    public static void runBatchReplication() {

        Simulation sim = new Simulation("DLB_with_Q_BATCH");
        // create the model element and attach it to the main model
        new DriverLicenseBureauWithQJ8(sim.getModel());

        sim.turnOnStatisticalBatching();
        StatisticalBatchingElement be = sim.getStatisticalBatchingElement().get();

        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        sim.run();

        System.out.println(be);
        System.out.println(sim);

        PrintWriter w = sim.getOutputDirectory().makePrintWriter("BatchStatistics.csv");
        StatisticReporter statisticReporter = be.getStatisticReporter();
        StringBuilder csvStatistics = statisticReporter.getCSVStatistics(true);
        w.print(csvStatistics);

    }

    public static void runExperiment() {
        Simulation sim = new Simulation("DLB_with_Q");
        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        // create the model element and attach it to the main model
        new DriverLicenseBureauWithQJ8(sim.getModel());
        // tell the simulation to run
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        System.out.println(sim);
        sim.printHalfWidthSummaryReport();
        SimulationReporter r = new SimulationReporter(sim);
        System.out.println(r.getAcrossReplicationStatisticsAsLaTeXTables());
    }

    public static void main(String[] args) {

        runExperiment();

        System.out.println("Done!");

    }
}
