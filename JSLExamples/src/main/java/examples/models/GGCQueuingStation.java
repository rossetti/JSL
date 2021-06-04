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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.models;

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.modeling.elements.station.ReceiveQObjectIfc;
import jsl.modeling.elements.station.SResource;
import jsl.modeling.elements.station.SingleQueueStation;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 *
 * @author rossetti
 */
public class GGCQueuingStation extends ModelElement {

    private final EventGenerator myArrivalGenerator;
    private final SingleQueueStation mySQS;
    private ResponseVariable mySystemTime;
    private TimeWeighted myNumInSystem;
    private final SResource myServers;
    private final RandomVariable mySTRV;

    public GGCQueuingStation(ModelElement parent, RandomIfc tba, RandomIfc st,
            int numServers) {
        this(parent, tba, st, numServers, null);
    }

    public GGCQueuingStation(ModelElement parent, RandomIfc tba, RandomIfc st,
                             int numServers, String name) {
        super(parent, name);
        myArrivalGenerator = new EventGenerator(this, new Arrivals(), tba, tba);
        myServers = new SResource(this, numServers, "Servers");
        mySTRV = new RandomVariable(this, st);
        mySQS = new SingleQueueStation(this, myServers, mySTRV, "Station");
        mySQS.setNextReceiver(new Dispose());
        mySystemTime = new ResponseVariable(this, "System Time");
        myNumInSystem = new TimeWeighted(this, "Num in System");
    }

    private class Arrivals implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator generator, JSLEvent event) {
            myNumInSystem.increment();
            mySQS.receive(new QObject(getTime()));
        }

    }

    protected class Dispose implements ReceiveQObjectIfc {

        @Override
        public void receive(QObject qObj) {
            // collect final statistics
            myNumInSystem.decrement();
            mySystemTime.setValue(getTime() - qObj.getCreateTime());
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation("M/M/2");
        // get the model
        Model m = sim.getModel();
        // add system to the main model
        ExponentialRV tba = new ExponentialRV(1);
        ExponentialRV st = new ExponentialRV(.8);
        int ns = 2;
        GGCQueuingStation system = new GGCQueuingStation(m, tba, st, ns);
        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
 //       sim.setNumberOfReplications(2);
 //       sim.setLengthOfReplication(20.0);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        r.printAcrossReplicationSummaryStatistics();
    }

}
