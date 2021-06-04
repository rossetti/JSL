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

import jsl.simulation.NoSuchStepException;
import jsl.simulation.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class SimulationTest {

    private Simulation mySim;

    //private ExperimentParameters myExp;
    //private Model myModel;
    @BeforeEach
    public void setUp() {

        mySim = new Simulation();
        //myExp = mySim.getExperiment();
        //myModel = mySim.getModel();

    }

    @Test
    public void runTwoIndivReps() {
        mySim.setNumberOfReplications(2);
        System.out.println("Begin Test 1 ------------------------------------------");
        System.out.println("Run two individual reps using runNext()");
        mySim.initialize();
        mySim.runNext();
        mySim.runNext();
        if (mySim.hasNextReplication()) {
            mySim.runNext();
        }
        System.out.println(mySim);
        System.out.println("End Test 1 ------------------------------------------");
        assertTrue(mySim.getCurrentReplicationNumber() == 2);
    }

    @Test
    public void runTwoThenStopReps() {
        mySim.setNumberOfReplications(4);
        System.out.println("Begin Test 2 ------------------------------------------");
        System.out.println("Run two individual reps using runNext() then stop");
        mySim.initialize();
        System.out.println(mySim);
        mySim.runNext();
        System.out.println(mySim);
        mySim.runNext();
        System.out.println(mySim);
        mySim.end();
        System.out.println(mySim);
        System.out.println("End Test 2 ------------------------------------------");
        assertTrue(mySim.isDone());
        assertTrue(mySim.isEnded());
    }

    @Test
    public void runTwoIndivRepsWithError() {
        mySim.setNumberOfReplications(2);
        System.out.println("Begin Test 3 ------------------------------------------");
        System.out.println("Run two individual reps using runNext()");
        mySim.initialize();
        mySim.runNext();
        mySim.runNext();
        boolean f = false;
        try {
            mySim.runNext();//should cause an error
        } catch (NoSuchStepException e) {
             System.out.println("### catch the error");
             System.out.println(e);
             f = true;
        }
        System.out.println(mySim);
        System.out.println("End Test 3 ------------------------------------------");
        assertTrue(f);
    }

    @Test
    public void runAllReps() {
        int r = 5;
        mySim.setNumberOfReplications(r);
        System.out.println("Begin Test 4 ------------------------------------------");
        System.out.println("Run " + r + " reps Via run()");
        //mySim.initialize();
        mySim.run();
        System.out.println(mySim);
        System.out.println("End Test 4 ------------------------------------------");
        assertTrue(mySim.getCurrentReplicationNumber() == r);
    }

    @Test
    public void runOneRep() {
        int r = 1;
        mySim.setNumberOfReplications(r);
        System.out.println("Begin Test 5 ------------------------------------------");
        System.out.println("Run " + r + " reps Via run()");
       // mySim.initialize();
        mySim.run();
        System.out.println(mySim);
        System.out.println("End Test 5 ------------------------------------------");
        assertTrue(mySim.getCurrentReplicationNumber() == r);
    }
}
