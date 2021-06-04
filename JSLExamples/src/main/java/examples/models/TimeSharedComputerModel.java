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
package examples.models;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/** 
 *  Timesharing computer model
 */
public class TimeSharedComputerModel extends SchedulingElement {

    private Queue<ComputerJob> myUsingCPUQ;

    private Queue<ComputerJob> myCPUJobQ;

    private int myNumTerminals;

    private double myQuantum;

    private double mySwapTime;

    private int myNumJobs;

    private RVariableIfc myThinkTimeDistribution;

    private RVariableIfc myServiceDistribution;

    private RandomVariable myServiceRV;

    private RandomVariable myThinkTimeRV;

    private TimeWeighted myNumTerminalsThinking;

    private ResponseVariable myResponseTime;

    private ArrivalListener myArrivalListener;

    private EndServiceListener myEndServiceListener;
    
    public TimeSharedComputerModel(ModelElement parent){
        this(parent, 1000, 80, new ExponentialRV(25.0),
                new ExponentialRV(0.8), 0.1, 0.015);
    }

    public TimeSharedComputerModel(ModelElement parent, int numJobs,
                                   int numTerminals, RVariableIfc thinking, RVariableIfc service,
                                   double quantum, double swaptime) {
        super(parent);
        setServiceDistributionInitialRandomSource(service);
        setThinkTimeDistributionInitialRandomSource(thinking);
        setNumberOfTerminals(numTerminals);
        // holds jobs waiting for CPU
        myCPUJobQ = new Queue<>(this, "CPU Job Q");
        // holds job that is using the CPU
        myUsingCPUQ = new Queue<>(this, "Using CPU Q");
        // all terminals are thinking
        myNumTerminalsThinking = new TimeWeighted(this, numTerminals, "Number Thinking");
        // used to record the response time
        myResponseTime = new ResponseVariable(this, "Response Time");
        // used to stop after myNumJobs are completed
        setQuantum(quantum);
        setNumJobs(numJobs);
        setSwapTime(swaptime);
        // listens for jobs to arrive and reacts
        myArrivalListener = new ArrivalListener();
        // listens for jobs to complete service increment
        myEndServiceListener = new EndServiceListener();
    }

    public final int getNumJobs() {
        return myNumJobs;
    }

    public final void setNumJobs(int numJobs) {
        myResponseTime.setCountBasedStopLimit(numJobs);
        myNumJobs = numJobs;
    }

    public final double getQuantum() {
        return myQuantum;
    }

    public final void setQuantum(double quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("The quantum must be > 0");
        }
        myQuantum = quantum;
    }

    public final double getSwapTime() {
        return mySwapTime;
    }

    public final void setSwapTime(double swapTime) {
        if (swapTime <= 0) {
            throw new IllegalArgumentException("The swapTime must be > 0");
        }
        mySwapTime = swapTime;
    }

    public RVariableIfc getArrivalDistribution() {
        return (myThinkTimeDistribution);
    }

    public RVariableIfc getServiceDistribution() {
        return (myServiceDistribution);
    }

    public final void setNumberOfTerminals(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }

        myNumTerminals = n;
    }

    public final void setServiceDistributionInitialRandomSource(RVariableIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("Service Time Distribution was null!");
        }

        myServiceDistribution = d;

        if (myServiceRV == null) {// not made yet
            myServiceRV = new RandomVariable(this, myServiceDistribution, "Service RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(myServiceDistribution);
        }

    }

    public final void setThinkTimeDistributionInitialRandomSource(RVariableIfc d) {

        if (d == null) {
            throw new IllegalArgumentException("ThinkTime Distribution was null!");
        }

        myThinkTimeDistribution = d;

        if (myThinkTimeRV == null) {// not made yet
            myThinkTimeRV = new RandomVariable(this, myThinkTimeDistribution, "ThinkTime RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myThinkTimeRV.setInitialRandomSource(myThinkTimeDistribution);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        // start the terminals thinking
        for (int i = 1; i <= myNumTerminals; i++) {
            ComputerJob job = new ComputerJob();
            double t = myThinkTimeRV.getValue();
            scheduleEvent(myArrivalListener, t, job);
        }
    }

    private void serveNextJob() {
        // get the next qobject from the cpu waiting Q
        ComputerJob job = myCPUJobQ.removeNext();
        // determine the cpu run time for this pass, including the swap time
        double runtime = 0.0;
        if (myQuantum < job.myRemainingServiceTime) // can only do the quantum
        {
            runtime = myQuantum + mySwapTime;
        } else // can do the full service time
        {
            runtime = job.myRemainingServiceTime + mySwapTime;
        }
        // adjust the remaining service, subtract quantum (don't include swap time)
        // if less than zero, this indicates no service left
        job.myRemainingServiceTime = job.myRemainingServiceTime - myQuantum;
        // place the job into the cpu
        myUsingCPUQ.enqueue(job);
        // schedule the job to run
        scheduleEvent(myEndServiceListener, runtime);
    }

    class ArrivalListener implements EventActionIfc<ComputerJob> {

        @Override
        public void action(JSLEvent<ComputerJob> event) {
            // get the job from the event's message
            ComputerJob job = event.getMessage();
            // set the job's arrival time
            job.myArrivalTime = getTime();
            // set the job's service time
            job.myRemainingServiceTime = myServiceRV.getValue();
            // no longer thinking, decrement the number thinking
            myNumTerminalsThinking.decrement();
            // enqueue the job for the cpu
            myCPUJobQ.enqueue(job);
            // check if cpu is idle, if so serve the next job
            if (myUsingCPUQ.isEmpty()) { // no job using CPU, thus idle
                serveNextJob();
            }
        }
    }

    class EndServiceListener extends EventAction {

        @Override
        public void action(JSLEvent event) {
            // remove the qobject from the using cpu queue
            ComputerJob job = myUsingCPUQ.removeNext();
            if (job.myRemainingServiceTime > 0.0) { // job requires more service
                // place job in cpu's waiting queue
                myCPUJobQ.enqueue(job);
                // just finished a job so must be idle and should start next job
                serveNextJob();
            } else {
                // job done, get the response time
                double ws = getTime() - job.myArrivalTime;
                // record the response time using the response variable
                myResponseTime.setValue(ws);
                // increment the number thinking
                myNumTerminalsThinking.increment();
                // generate the thinking time
                double t = myThinkTimeRV.getValue();
                // schedule the end of the thinking time
                scheduleEvent(myArrivalListener, t, job);
                // if more jobs waiting for cpu, then serve them
                if (myCPUJobQ.size() > 0) {
                    serveNextJob();
                }
            }
        }
    }

    class ComputerJob extends QObject {

        public ComputerJob() {
            super(getTime());
        }

        private double myArrivalTime = 0.0;

        private double myRemainingServiceTime = 0.0;
    }

    public static void main(String[] args) {
        System.out.println("Time Shared Computer");
        Simulation s = new Simulation("Time Shared Computer");

        // create the containing model
        Model m = s.getModel();

        // create the model element and attach it to the main model
        TimeSharedComputerModel ts = new TimeSharedComputerModel(m);

        // set the parameters of the simulation
        s.setNumberOfReplications(200);
        //ts.setNumJobs(20);
        // tell the simulation to run
        //s.turnOnDefaultEventTraceReport();
        s.run();
        
        SimulationReporter r = s.makeSimulationReporter();
        r.printAcrossReplicationSummaryStatistics();
        r.writeFullAcrossReplicationStatistics("TS Computer Model");
        r.writeAcrossReplicationCSVStatistics("TS Computer Model");
        System.out.println("Done!");
    }
}
