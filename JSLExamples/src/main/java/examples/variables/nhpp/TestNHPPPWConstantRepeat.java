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
package examples.variables.nhpp;

import java.util.ArrayList;
import java.util.List;

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.nhpp.*;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.modeling.elements.EventGeneratorActionIfc;

/**
 * @author rossetti
 *
 */
public class TestNHPPPWConstantRepeat extends ModelElement {

    protected NHPPEventGenerator myNHPPGenerator;

    protected EventListener myListener = new EventListener();

    protected List<Counter> myCountersFC;

    protected List<Counter> myCountersSC;

    protected PiecewiseRateFunction myPWRF;

    public TestNHPPPWConstantRepeat(ModelElement parent, PiecewiseRateFunction f) {
        this(parent, f, null);
    }

    public TestNHPPPWConstantRepeat(ModelElement parent, PiecewiseRateFunction f, String name) {
        super(parent, name);
        myNHPPGenerator = new NHPPEventGenerator(this, f, myListener);
        myPWRF = f;
        myCountersFC = new ArrayList<Counter>();
        int n = f.getNumberSegments();
        for (int i = 0; i < n; i++) {
            Counter c = new Counter(this, "Interval FC " + i);
            myCountersFC.add(c);
        }
        myCountersSC = new ArrayList<Counter>();
        for (int i = 0; i < n; i++) {
            Counter c = new Counter(this, "Interval SC " + i);
            myCountersSC.add(c);
        }


    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        // create the experiment to run the model
        Simulation s = new Simulation("TestNHPP");
        PiecewiseConstantRateFunction f = new PiecewiseConstantRateFunction(15.0, 1.0);

        f.addRateSegment(20.0, 2.0);
        f.addRateSegment(15.0, 1.0);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        new TestNHPPPWConstantRepeat(s.getModel(), f);
        // set the parameters of the experiment
        // set the parameters of the experiment
        s.setNumberOfReplications(10000);
        s.setLengthOfReplication(100.0);

        // tell the simulation to run
        s.run();

        SimulationReporter r = new SimulationReporter(s);

        r.printAcrossReplicationSummaryStatistics();

    }

    protected class EventListener implements EventGeneratorActionIfc {

        public void generate(EventGenerator generator, JSLEvent event) {

            double t = getTime();

            if (t <= 50.0) {
                //System.out.println("event at time: " + t);				
                int i = myPWRF.findTimeInterval(t);
                //System.out.println("occurs in interval: " + i);				
                myCountersFC.get(i).increment();
            } else {
                //System.out.println("event at time: " + t);				
                int i = myPWRF.findTimeInterval(t - 50.0);
                //System.out.println("occurs in interval: " + i);				
                myCountersSC.get(i).increment();
            }

        }
    }
}
