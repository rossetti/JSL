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
package examples.general.variables.nhpp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.nhpp.NHPPEventGenerator;
import jsl.modeling.elements.variable.nhpp.PiecewiseConstantRateFunction;
import jsl.modeling.elements.variable.nhpp.PiecewiseLinearRateFunction;
import jsl.modeling.elements.variable.nhpp.PiecewiseRateFunction;
import jsl.observers.variable.CounterTraceTextReport;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.modeling.elements.EventGeneratorActionIfc;

/**
 * @author rossetti
 *
 */
public class TestNHPP extends ModelElement {

    protected NHPPEventGenerator myNHPPGenerator;

    protected EventListener myListener = new EventListener();

    protected List<Counter> myCounters;

    protected PiecewiseRateFunction myPWRF;

    public TestNHPP(ModelElement parent, PiecewiseRateFunction f) {
        this(parent, f, null);
    }

    public TestNHPP(ModelElement parent, PiecewiseRateFunction f, String name) {
        super(parent, name);
        myNHPPGenerator = new NHPPEventGenerator(this, f, myListener);
        myPWRF = f;
        myCounters = new ArrayList<Counter>();
        int n = f.getNumberSegments();
        for (int i = 0; i < n; i++) {
            Counter c = new Counter(this, "Interval " + i);
            myCounters.add(c);
            Path path= getSimulation().getOutputDirectoryPath().resolve(c.getName()+".csv");
            System.out.println(path);
            c.addObserver(new CounterTraceTextReport(path, true));
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

		runModel1();
//        runModel2();

    }

    public static void runModel1() {
        // create the experiment to run the model
        Simulation s = new Simulation("TestNHPP");

        PiecewiseConstantRateFunction f = new PiecewiseConstantRateFunction(15.0, 1.0);

        f.addRateSegment(20.0, 2.0);
        f.addRateSegment(15.0, 1.0);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        new TestNHPP(s.getModel(), f);

        // set the parameters of the experiment
        // set the parameters of the experiment
        s.setNumberOfReplications(2);
        s.setLengthOfReplication(100.0);

        // tell the simulation to run
        s.run();

        SimulationReporter r = new SimulationReporter(s);

        r.printAcrossReplicationSummaryStatistics();

    }

    public static void runModel2() {
        Simulation s = new Simulation("TestNHPP");

        PiecewiseRateFunction f = new PiecewiseLinearRateFunction(0.5, 200.0, 0.5);

        f.addRateSegment(400.0, 0.9);
        f.addRateSegment(400.0, 0.9);
        f.addRateSegment(200.0, 1.2);
        f.addRateSegment(300.0, 0.9);
        f.addRateSegment(500.0, 0.5);

        System.out.println("-----");
        System.out.println("intervals");
        System.out.println(f);

        new TestNHPP(s.getModel(), f);

        // set the parameters of the experiment
        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(2000.0);

        // tell the simulation to run
        s.run();

        SimulationReporter r = new SimulationReporter(s);

        r.printAcrossReplicationSummaryStatistics();
    }

    protected class EventListener implements EventGeneratorActionIfc {

        public void generate(EventGenerator generator, JSLEvent event) {

            double t = getTime();

            //System.out.println("event at time: " + t);

            int i = myPWRF.findTimeInterval(t);
            //System.out.println("occurs in interval: " + i);
            if (i < 0)
                return;
            myCounters.get(i).increment();
        }
    }
}
