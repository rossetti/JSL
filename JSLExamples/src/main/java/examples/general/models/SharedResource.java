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
package examples.general.models;

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.modeling.elements.station.SResource;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 *
 * @author rossetti
 */
public class SharedResource extends SchedulingElement {

    private final EventGenerator myTypeAGenerator;
    private final EventGenerator myTypeBGenerator;
    private final TimeWeighted myNumInSystem;
    private final ResponseVariable mySystemTime;
    private final SResource myServers;
    private final Queue<QObject> myTypeAWaitingQ;
    private final Queue<QObject> myTypeBWaitingQ;
    private final EndServiceEventAction myEndServiceEventAction;
    private final RandomVariable myServiceRVTypeA;
    private final RandomVariable myServiceRVTypeB;

    public SharedResource(ModelElement parent, int numServers, RVariableIfc tbaA,
                          RVariableIfc tbaB, RVariableIfc stA, RVariableIfc stB, String name) {
        super(parent, name);
        myTypeAGenerator = new EventGenerator(this, new TypeAArrivals(), tbaA, tbaA);
        myTypeBGenerator = new EventGenerator(this, new TypeBArrivals(), tbaB, tbaB);
        myServiceRVTypeA = new RandomVariable(this, stA, "Service RV A");
        myServiceRVTypeB = new RandomVariable(this, stB, "Service RV B");
        myServers = new SResource(this, numServers, "Servers");
        myTypeAWaitingQ = new Queue<>(this, getName() + "_QA");
        myTypeBWaitingQ = new Queue<>(this, getName() + "_QB");
        mySystemTime = new ResponseVariable(this, "System Time");
        myNumInSystem = new TimeWeighted(this, "Num in System");
        myEndServiceEventAction = new EndServiceEventAction();
    }

    private class TypeAArrivals implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator generator, JSLEvent event) {
            myNumInSystem.increment();
            myTypeAWaitingQ.enqueue(new QObject(getTime()));
            if (myServers.hasAvailableUnits()) { // server available
                serveNext();
            }
        }

    }

    private class TypeBArrivals implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator generator, JSLEvent event) {
            myNumInSystem.increment();
            myTypeBWaitingQ.enqueue(new QObject(getTime()));
            if (myServers.hasAvailableUnits()) { // server available
                serveNext();
            }
        }

    }

    private void serveNext() {
        //logic to choose next from queues
        // if both have waiting parts, assume part type A has priority
        if (myTypeAWaitingQ.isNotEmpty()) {
            QObject partA = myTypeAWaitingQ.removeNext(); //remove the next customer
            myServers.seize();
            // schedule end of service
            scheduleEvent(myEndServiceEventAction, myServiceRVTypeA, partA);
        } else if (myTypeBWaitingQ.isNotEmpty()) {
            QObject partB = myTypeBWaitingQ.removeNext(); //remove the next customer
            myServers.seize();
            // schedule end of service
            scheduleEvent(myEndServiceEventAction, myServiceRVTypeB, partB);
        }
    }

    private boolean checkQueues() {
        return (myTypeAWaitingQ.isNotEmpty() || myTypeBWaitingQ.isNotEmpty());
    }

    private class EndServiceEventAction implements EventActionIfc<QObject> {

        @Override
        public void action(JSLEvent<QObject> event) {
            QObject leavingPart = event.getMessage();
            myServers.release();
            if (checkQueues()) { // queue is not empty
                serveNext();
            }
            departSystem(leavingPart);
        }
    }

    private void departSystem(QObject leavingPart) {
        mySystemTime.setValue(getTime() - leavingPart.getCreateTime());
        myNumInSystem.decrement(); // part left system      
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation("Shared Resource Example");
        // get the model
        Model m = sim.getModel();
        // add to the main model
        RVariableIfc tbaA = new ExponentialRV(4.0);
        RVariableIfc tbaB = new ExponentialRV(6.0);
        RVariableIfc stA = new ExponentialRV(3.0);
        RVariableIfc stB = new ExponentialRV(5.0);
        SharedResource sr = new SharedResource(m, 2, tbaA, tbaB, stA, stB, "SR");
        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        r.printAcrossReplicationSummaryStatistics();
    }

}
