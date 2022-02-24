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
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.StatisticAccessorIfc;

import java.util.Objects;

public class DriverLicenseBureau extends SchedulingElement {

    private int myNumServers;

    private RandomIfc myServiceDistribution;

    private RandomIfc myArrivalDistribution;

    private RandomVariable myServiceRV;

    private RandomVariable myArrivalRV;

    private final TimeWeighted myNumBusy;

    private final TimeWeighted myNQ;

    private final TimeWeighted myNS;

    private final ArrivalEventAction myArrivalEventAction;

    private final EndServiceEventAction myEndServiceEventAction;

    public DriverLicenseBureau(ModelElement parent) {
        this(parent, 1, new ExponentialRV(1.0), new ExponentialRV(0.8));
    }

    public DriverLicenseBureau(ModelElement parent, int numServers,
                               RVariableIfc ad, RVariableIfc sd) {
        super(parent);

        setNumberOfServers(numServers);
        setServiceDistributionInitialRandomSource(sd);
        setArrivalDistributionInitialRandomSource(ad);

        myNumBusy = new TimeWeighted(this, 0.0, "NumBusy");

        myNQ = new TimeWeighted(this, 0.0, "NQ");

        myNS = new TimeWeighted(this, 0.0, "NS");

        myArrivalEventAction = new ArrivalEventAction();
        myEndServiceEventAction = new EndServiceEventAction();
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
        Objects.requireNonNull(d, "Service Time Distribution was null!");
        myServiceDistribution = d;
        if (myServiceRV == null) { // not made yet
            myServiceRV = new RandomVariable(this, myServiceDistribution, "Service RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myServiceRV.setInitialRandomSource(myServiceDistribution);
        }
    }

    public final void setArrivalDistributionInitialRandomSource(RandomIfc d) {
        Objects.requireNonNull(d, "Arrival Time Distribution was null!");
        myArrivalDistribution = d;
        if (myArrivalRV == null) { // not made yet
            myArrivalRV = new RandomVariable(this, myArrivalDistribution, "Arrival RV");
        } else { // already had been made, and added to model
            // just change the distribution
            myArrivalRV.setInitialRandomSource(myArrivalDistribution);
        }
    }

    public final StatisticAccessorIfc getNQAcrossReplicationStatistic() {
        return myNQ.getAcrossReplicationStatistic();
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
        scheduleEvent(myArrivalEventAction, myArrivalRV.getValue());
    }

    private class ArrivalEventAction extends EventAction {

        public void action(JSLEvent event) {

            myNS.increment(); // new customer arrived
            if (myNumBusy.getValue() < myNumServers) { // server available
                myNumBusy.increment(); // make server busy
                //	schedule end of service
                scheduleEvent(myEndServiceEventAction, myServiceRV.getValue());
            } else { // no server available
                myNQ.increment(); // place customer in queue
            }

            //	always schedule the next arrival
            scheduleEvent(myArrivalEventAction, myArrivalRV.getValue());
        }
    }

    private class EndServiceEventAction extends EventAction {

        public void action(JSLEvent event) {

            myNS.decrement(); // customer departed
            myNumBusy.decrement(); // customer is leaving server is freed

            if (myNQ.getValue() > 0) { // queue is not empty
                myNQ.decrement(); // remove from queue
                myNumBusy.increment(); // make server busy
                //	schedule end of service
                scheduleEvent(myEndServiceEventAction, myServiceRV.getValue());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Driver License Bureau Test");
        // Create the simulation
        Simulation sim = new Simulation("DLB Sim");
        // get the containing model
        Model m = sim.getModel();
        // create the model element and attach it to the main model
        new DriverLicenseBureau(m);
        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfReplication(200000.0);
        sim.setLengthOfWarmUp(50000.0);
        // run the simulation
        sim.run();
        sim.printHalfWidthSummaryReport();

    }
}
