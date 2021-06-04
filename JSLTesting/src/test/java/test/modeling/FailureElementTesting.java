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
package test.modeling;

import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.modeling.resource.ResourceUnit;
import jsl.utilities.random.rvariable.ConstantRV;

/**
 *
 * @author rossetti
 */
public class FailureElementTesting {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        test1();
        //test2();
    }

    public static void test1() {
        Simulation sim = new Simulation("Test FailureElement");
        Model model = sim.getModel();
        ResourceUnit resource = new ResourceUnit.Builder(model).
                allowFailuresToDelay().build();
        ConstantRV c1 = new ConstantRV(0.5);
        ConstantRV c2 = new ConstantRV(0.2);
        resource.addTimeBasedFailure(ConstantRV.TWO, c1);
        resource.addTimeBasedFailure(ConstantRV.ONE, c2);
        
        sim.setLengthOfReplication(20.0);
        sim.setNumberOfReplications(2);
        sim.run();
    }

    public static void test2() {
        Simulation sim = new Simulation("Test FailureElement");
        Model model = sim.getModel();
        ResourceUnit resource = new ResourceUnit.Builder(model).build();
        ConstantRV c1 = new ConstantRV(0.5);
        ConstantRV c2 = new ConstantRV(0.2);
        resource.addTimeBasedFailure(ConstantRV.TWO, c1);
        resource.addTimeBasedFailure(ConstantRV.ONE, c2);
        sim.setLengthOfReplication(20.0);
        sim.setNumberOfReplications(2);
        sim.run();
    }

}
