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
package examples.book.chapter6;

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 * An UpDownComponent is a model element that has two states UP = 1 and DOWN = 0.
 * This class models the random time spent in the up and down states and collects
 * statistics on the number of failures (down events), the average time spent in
 * the up state, and the average length of the cycles. Two events and their
 * actions are defined to model the up state change and the down state change.
 */
public class UpDownComponent extends SchedulingElement {

    public static final int UP = 1;
    public static final int DOWN = 0;
    private final RandomVariable myUpTime;
    private final RandomVariable myDownTime;
    private final TimeWeighted myState;
    private final ResponseVariable myCycleLength;
    private final Counter myCountFailures;
    private final UpChangeAction myUpChangeAction = new UpChangeAction();
    private final DownChangeAction myDownChangeAction = new DownChangeAction();
    private double myTimeLastUp;

    public UpDownComponent(ModelElement parent) {
        this(parent, null);
    }

    public UpDownComponent(ModelElement parent, String name) {
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
        scheduleEvent(myDownChangeAction, myUpTime.getValue());
        //schedule(myDownChangeAction).name("Down").in(myUpTime).units();
    }

    private class UpChangeAction extends EventAction {

        @Override
        public void action(JSLEvent event) {
            // this event action represents what happens when the component goes up
            // record the cycle length, the time btw up states
            myCycleLength.setValue(getTime() - myTimeLastUp);
            // component has just gone up, change its state value
            myState.setValue(UP);
            // record the time it went up
            myTimeLastUp = getTime();
            // schedule the down state change after the uptime
            scheduleEvent(myDownChangeAction, myUpTime.getValue());
        }
    }

    private class DownChangeAction extends EventAction {

        @Override
        public void action(JSLEvent event) {
            // component has just gone down, change its state value
            myCountFailures.increment();
            myState.setValue(DOWN);
            // schedule when it goes up after the downtime
            scheduleEvent(myUpChangeAction, myDownTime.getValue());
        }
    }
}
