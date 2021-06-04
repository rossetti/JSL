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
package examples.resource;

import jsl.modeling.elements.entity.Allocation;
import jsl.modeling.elements.entity.Entity;
import jsl.modeling.elements.entity.Resource;
import jsl.simulation.*;

/**
 *
 * @author rossetti
 */
public class ResourceTester extends SchedulingElement {

    private Resource myResource;

    private EventAction1 myAction1;
    
    private EventAction2 myAction2;
    
    public ResourceTester(ModelElement parent) {
        this(parent, null);
    }

    public ResourceTester(ModelElement parent, String name) {
        super(parent, name);

        myResource = new Resource(this, getName() + "_R");
        myAction1 = new EventAction1();
        myAction2 = new EventAction2();
        
    }

    @Override
    protected void initialize() {
        scheduleEvent(myAction1, 10.0);
    }

    class EventAction1 extends EventAction {

        @Override
        public void action(JSLEvent event) {
            System.out.println(event);
            System.out.println("in action 1");
            Entity entity = createEntity();
            Allocation a = myResource.allocate(entity);
            scheduleEvent(myAction2, 3.0, a);
        }

    }

    class EventAction2 implements EventActionIfc<Allocation> {

        @Override
        public void action(JSLEvent<Allocation> evt) {
            System.out.println(evt);
            Allocation a = evt.getMessage();
            System.out.println(a);
            Resource r = a.getAllocatedResource();
            r.release(a);
            System.out.println(a);
            scheduleEvent(myAction1, 10.0);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Resource Testing Example");

        Simulation s = new Simulation("Resource Testing");

        // create the model element and attach it to the main model
        new ResourceTester(s.getModel());

        // set the parameters of the experiment
        s.setNumberOfReplications(1);
        s.setLengthOfReplication(200.0);
        //s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printAcrossReplicationSummaryStatistics();
        System.out.println("Done!");
    }

}
