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
package test.modeling;

import jsl.simulation.ExperimentGetIfc;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class SchedulingTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void test1() {
        Simulation mySim = new Simulation();
        ExperimentGetIfc myExp = mySim.getExperiment();
        Model myModel = mySim.getModel();

        mySim.setNumberOfReplications(2);

        System.out.println("Begin Test 1 ------------------------------------------");
        System.out.println("Test scheduling in the constructor");

        ScheduleInConstructor s = new ScheduleInConstructor(myModel);

        mySim.run();

        System.out.println(mySim);
        System.out.println(mySim.getExecutive());
        System.out.println("End Test 1 ------------------------------------------");
        assertTrue(true);
    }

    @Test
    public void test2() {
        Simulation mySim = new Simulation();
        ExperimentGetIfc myExp = mySim.getExperiment();
        Model myModel = mySim.getModel();

        mySim.setNumberOfReplications(2);
        mySim.setLengthOfReplication(25.0);

        System.out.println();
        System.out.println("Begin Test 2 ------------------------------------------");
        System.out.println("Test scheduling in the constructor");

        ScheduleInConstructor s = new ScheduleInConstructor(myModel);

        mySim.run();

        System.out.println(mySim);
        System.out.println(mySim.getExecutive());
        System.out.println("End Test 2 ------------------------------------------");
        assertTrue(true);
    }
}
