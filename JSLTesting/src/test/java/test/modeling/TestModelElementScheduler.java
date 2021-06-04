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

import jsl.simulation.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class TestModelElementScheduler {

    public TestModelElementScheduler() {
    }


    @Test
    public void test1() {
        System.out.println("Begin Test 1 -----------------------------------");
        Simulation mySim = new Simulation();
        Model myModel = mySim.getModel();
        new TestME(myModel);
        //myModel.setTimeUnit(ModelElement.TIME_UNIT_DAY);
        //myModel.setTimeUnit(ModelElement.TIME_UNIT_HOUR);
        //myModel.setTimeUnit(ModelElement.TIME_UNIT_HOUR);
        System.out.println("time units = " + myModel.getTimeUnit());
        System.out.println("millisecond = " + myModel.millisecond());
        System.out.println("second = " + myModel.second());
        System.out.println("minute = " + myModel.minute());
        System.out.println("hour = " + myModel.hour());
        System.out.println("day = " + myModel.day());
        System.out.println("week = " + myModel.week());
        mySim.setNumberOfReplications(2);
        mySim.setLengthOfReplication(200.0);

        System.out.println("Test new event scheduling");
        mySim.run();
    }

    // @Test
    public void test2() {
        Simulation mySim = new Simulation();
        Model myModel = mySim.getModel();
        //myModel.setTimeUnit(ModelElement.TIME_UNIT_DAY);
        //myModel.setTimeUnit(ModelElement.TIME_UNIT_HOUR);
        myModel.setTimeUnit(ModelElement.TIME_UNIT_HOUR);
        System.out.println("time units = " + myModel.getTimeUnit());
        System.out.println("millisecond = " + myModel.millisecond());
        System.out.println("second = " + myModel.second());
        System.out.println("minute = " + myModel.minute());
        System.out.println("hour = " + myModel.hour());
        System.out.println("day = " + myModel.day());
        System.out.println("week = " + myModel.week());
    }

    public class TestME extends ModelElement {

        private TestAction myAction;

        public TestME(ModelElement parent) {
            super(parent);
            myAction = new TestAction();
        }

        @Override
        protected void initialize() {
            //schedule(myAction).in(100).units();
            schedule(myAction).now();
            schedule(this::doSomething).name("test").in(1).units();
            //schedule(myAction).in(1).hours();
        }

        public void doSomething(JSLEvent evt) {
            System.out.println(getTime() + " In doSomething");
            System.out.println("event name = " + evt.getName() );
        }

        private class TestAction extends EventAction {

            @Override
            public void action(JSLEvent evt) {
                schedule(myAction).in(20).units();
                //schedule(myAction).in(20).hours();
                //schedule(myAction).in(1).days();
                //schedule(myAction).in(1).minutes();
                System.out.println(getTime() + " In the action");
            }

        }
    }

    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
