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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.book.chapter6;

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 * A simple class to illustrate how to simulate a Poisson process.
 * The class defines uses an inner class that extends the EventAction
 * class to provide a callback for the events associated with
 * the Poisson process.  An instance of RandomVariable is used to hold
 * the exponential random variable and a JSL Counter is used to collect
 * statistics on the number of arrivals.
 *
 * @author rossetti
 */
public class SimplePoissonProcess extends SchedulingElement {

    private final RandomVariable myTBE;
    private final Counter myCount;
    private final EventHandler myEventHandler;

    public SimplePoissonProcess(ModelElement parent) {
        this(parent, null);
    }

    public SimplePoissonProcess(ModelElement parent, String name) {
        super(parent, name);
        myTBE = new RandomVariable(this, new ExponentialRV(1.0));
        myCount = new Counter(this, "Counts events");
        myEventHandler = new EventHandler();
    }

    @Override
    protected void initialize() {
        super.initialize();
        scheduleEvent(myEventHandler, myTBE.getValue());
    }

    private class EventHandler extends EventAction {
        @Override
        public void action(JSLEvent evt) {
            myCount.increment();
            scheduleEvent(myEventHandler, myTBE.getValue());
        }
    }

}
