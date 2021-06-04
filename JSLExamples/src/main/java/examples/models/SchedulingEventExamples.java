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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.models;

import jsl.simulation.*;

/**
 * @author rossetti
 */
public class SchedulingEventExamples extends SchedulingElement {

    private final EventActionOne myEventActionOne;
    private final EventActionTwo myEventActionTwo;

    public SchedulingEventExamples(ModelElement parent) {
        this(parent, null);
    }

    public SchedulingEventExamples(ModelElement parent, String name) {
        super(parent, name);
        myEventActionOne = new EventActionOne();
        myEventActionTwo = new EventActionTwo();
    }

    @Override
    protected void initialize() {
        // schedule a type 1 event at time 10.0
        scheduleEvent(myEventActionOne, 10.0);
        // schedule an event that uses myEventAction for time 20.0
        scheduleEvent(myEventActionTwo, 20.0);
    }

    private class EventActionOne extends EventAction {

        public void action(JSLEvent event) {
            System.out.println("EventActionOne at time : " + getTime());
            // schedule a type 2 event for time t + 5
            //scheduleEvent(myEventActionTwo, 5.0);
        }
    }

    private class EventActionTwo extends EventAction {

        @Override
        public void action(JSLEvent<Object> jsle) {
            //TODO why is Object needed?
            System.out.println("EventActionTwo at time : " + getTime());
            // schedule a type 1 event for time t + 15
            scheduleEvent(myEventActionOne, 15.0);
            // reschedule the EventAction event for t + 20
            rescheduleEvent(jsle, 20.0);
        }
    }

    public static void main(String[] args) {
        Simulation s = new Simulation("Scheduling Example");
        new SchedulingEventExamples(s.getModel());
        s.setLengthOfReplication(100.0);
        s.run();
    }
}
