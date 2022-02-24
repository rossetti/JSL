/*
 * Copyright (c) 2019. Manuel D. Rossetti, rossetti@uark.edu
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
package examples.general.models;

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 */
public class UpDownComponentV2 extends SchedulingElement {

    public static final int UP = 1;
    public static final int DOWN = 0;
    private RandomVariable myUpTime;
    private RandomVariable myDownTime;
    private TimeWeighted myState;
    private ResponseVariable myCycleLength;
    private Counter myCountFailures;
//    private final UpChangeAction myUpChangeAction = new UpChangeAction();
//    private final DownChangeAction myDownChangeAction = new DownChangeAction();
    private double myTimeLastUp;

    public UpDownComponentV2(ModelElement parent) {
        this(parent, null);
    }

    public UpDownComponentV2(ModelElement parent, String name) {
        super(parent, name);
        RVariableIfc utd = new ExponentialRV(1.0);
        RVariableIfc dtd = new ExponentialRV(2.0);
        myUpTime = new RandomVariable(this, utd, "up time");
        myDownTime = new RandomVariable(this, dtd, "down time");
        myState = new TimeWeighted(this, "state");
        myCycleLength = new ResponseVariable(this, "cycle length");
        myCountFailures = new Counter(this, "count failures");
    }

    @Override
    public void initialize() {
        // assume that the component starts in the UP state at time 0.0
        myTimeLastUp = 0.0;
        myState.setValue(UP);
        // schedule the time that it goes down
        schedule(this::downChangeAction).name("Down").in(myUpTime).units();
    }

    private void upChangeAction(JSLEvent event){
        // this event action represents what happens when the component goes up
        // record the cycle length, the time btw up states
        myCycleLength.setValue(getTime() - myTimeLastUp);
        // component has just gone up, change its state value
        myState.setValue(UP);
        // record the time it went up
        myTimeLastUp = getTime();
        // schedule the down state change after the uptime
        schedule(this::downChangeAction).name("Down").in(myUpTime).units();
    }

    private void downChangeAction(JSLEvent event){
        // component has just gone down, change its state value
        myCountFailures.increment();
        myState.setValue(DOWN);
        // schedule when it goes up afer the down time
        schedule(this::upChangeAction).name("Up").in(myDownTime).units();
    }

    public static void main(String[] args) {
        // create the simulation
        Simulation s = new Simulation("UpDownComponent");
        s.turnOnDefaultEventTraceReport();
        s.turnOnLogReport();
        // get the model associated with the simulation
        Model m = s.getModel();
        // create the model element and attach it to the model
        UpDownComponentV2 tv = new UpDownComponentV2(m);
        // make the simulation reporter
        SimulationReporter r = s.makeSimulationReporter();
        // set the running parameters of the simulation
        s.setNumberOfReplications(5);
        s.setLengthOfReplication(5000.0);
        // tell the simulation to run
        s.run();
        r.printAcrossReplicationSummaryStatistics();
    }
}
