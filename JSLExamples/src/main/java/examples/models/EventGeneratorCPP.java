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

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 * Arrivals are governed by a compound Poisson process. An EventGenerator is used
 *
 * @author rossetti
 */
public class EventGeneratorCPP extends SchedulingElement {

    protected EventGenerator myArrivalGenerator;
    protected Counter myEventCounter;
    protected Counter myArrivalCounter;
    protected RandomVariable myTBA;
    protected RandomVariable myNumArrivals;

    public EventGeneratorCPP(ModelElement parent) {
        this(parent, 1.0, null);
    }

    public EventGeneratorCPP(ModelElement parent, double tba, String name) {
        super(parent, name);
        double[] values = {1, 2, 3};
        double[] cdf = {0.2, 0.5, 1.0};
        myNumArrivals = new RandomVariable(this, new DEmpiricalRV(values, cdf));
        myTBA = new RandomVariable(this, new ExponentialRV(tba));
        myEventCounter = new Counter(this, "Counts Events");
        myArrivalCounter = new Counter(this, "Counts Arrivals");
        myArrivalGenerator = new EventGenerator(this, new Arrivals(), myTBA, myTBA);
    }
    
    public void setInitialMaximumNumberOfEvents(long n){
        myArrivalGenerator.setInitialMaximumNumberOfEvents(n);
    }

    protected class Arrivals implements EventGeneratorActionIfc {
        @Override
        public void generate(EventGenerator generator, JSLEvent event) {
            myEventCounter.increment();
            int n = (int)myNumArrivals.getValue();
            myArrivalCounter.increment(n);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation s = new Simulation("Poisson Process Example");
        EventGeneratorCPP pp = new EventGeneratorCPP(s.getModel());
        //pp.setInitialMaximumNumberOfEvents(2);
        s.setLengthOfReplication(20.0);
        s.setNumberOfReplications(50);
        SimulationReporter r = s.makeSimulationReporter();
        s.run();
        r.printAcrossReplicationSummaryStatistics();
    }

}
