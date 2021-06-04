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

import jsl.observers.ObserverIfc;
import jsl.simulation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class ExecutiveTest {

    Executive e;
    Simulation s;
    ActionListener a;
    Model m;

    @BeforeEach
    public void setUp() {
        s = new Simulation();
        m = s.getModel();
        e = new Executive();
        a = new ActionListener();

        ExecutiveTrace trace = new ExecutiveTrace();
        e.addObserver(trace);

    }
    
    @Test
    public void test1() {
        System.out.println("Test 1");
        System.out.println("Event 6 & 7 should execute along with end event");
        System.out.println("because other events are scheduled after end event.");
        System.out.println();
        e.initialize();
        JSLEvent evt = e.scheduleEndEvent(0.9, m);
        e.scheduleEvent(a, 1.0, 1, null, "event 1", m);
        e.scheduleEvent(a, 1.1, 1, null, "event 2", m);
        e.scheduleEvent(a, 1.2, 1, null, "event 3", m);
        e.scheduleEvent(a, 1.3, 1, null, "event 4", m);
        e.scheduleEvent(a, 1.3, 1, null, "event 5", m);
        e.scheduleEvent(a, 0.5, 1, null, "event 6", m);
        e.scheduleEvent(a, 0.75, 1, null, "event 7", m);
        e.executeAllEvents();
        System.out.println();
        System.out.println("Done executing all events");
        System.out.println("Number of events executed = " + e.getTotalNumberEventsExecuted());
        System.out.println("Time executive actually ended = " + e.getActualEndingTime());
        System.out.println("End Event time = " + evt.getTime());
        System.out.println("*****************************");
        System.out.println();
        assertTrue(e.getTotalNumberEventsExecuted() == 3);
        assertTrue(evt.getTime() == e.getActualEndingTime());
    }

    @Test
    public void test2() {
        System.out.println("Test 2");
        System.out.println("All events should execute");
        e.initialize();
        e.scheduleEvent(a, 1.0, 1, null, "event 1", m);
        e.scheduleEvent(a, 1.1, 1, null, "event 2", m);
        e.scheduleEvent(a, 1.2, 1, null, "event 3", m);
        e.scheduleEvent(a, 1.3, 1, null, "event 4", m);
        e.scheduleEvent(a, 1.3, 1, null, "event 5", m);
        e.scheduleEvent(a, 0.5, 1, null, "event 6", m);
        e.scheduleEvent(a, 0.75, 1, null, "event 7", m);
        
        e.executeAllEvents();
        
        System.out.println();
        System.out.println("Done executing all events");
        System.out.println("Number of events executed = " + e.getTotalNumberEventsExecuted());
        System.out.println("Time executive actually ended = " + e.getActualEndingTime());
        System.out.println("*****************************");
        System.out.println();

        assertTrue(e.getTotalNumberEventsExecuted() == 7);
        assertTrue(1.3 == e.getActualEndingTime());
    }

    @Test
    public void test3() {
        System.out.println("Test 3");
        System.out.println("Only first 2 events should execute");
        //e.scheduleEndEvent(Double.POSITIVE_INFINITY);
        e.initialize();
        //JSLEvent evt = e.getEndEvent();
        e.scheduleEvent(a, 1.0, 1, null, "event 1", m);
        e.scheduleEvent(a, 1.1, 1, null, "event 2", m);
        e.scheduleEvent(a, 1.2, 1, null, "event 3", m);
        e.scheduleEvent(a, 1.3, 1, null, "event 4", m);
        e.scheduleEvent(a, 1.3, 1, null, "event 5", m);
        e.scheduleEvent(a, 0.5, 1, null, "event 6", m);
        e.scheduleEvent(a, 0.75, 1, null, "event 7", m);

        e.executeNextEvent();
        e.executeNextEvent();

        System.out.println(e);
        System.out.println();
        System.out.println("Number of events executed = " + e.getTotalNumberEventsExecuted());
        System.out.println("Executive actual ending time = " + e.getActualEndingTime());
        System.out.println("getTime() = " + e.getTime());
        System.out.println("*****************************");
        System.out.println();

        assertTrue(e.getTotalNumberEventsExecuted() == 2);
        assertTrue(e.getTime() == 0.75);
    }

    @Test
    public void test4() {
        System.out.println("Test 4");
        System.out.println("Only first 2 events should execute");

        e.initialize();

        e.scheduleEvent(a, 1.0, 1, null, "event 1", m);
        e.scheduleEvent(a, 1.1, 1, null, "event 2", m);

        e.executeNextEvent();
        System.out.println(e);
        e.executeNextEvent();
        System.out.println(e);
        
        boolean f = false;
        try {
            System.out.println("### Try to execute event that is not there");
            e.executeNextEvent();//should cause an error
        } catch (NoSuchStepException e) {
             System.out.println("### catch the error");
             System.out.println(e);
            f = true;
        }

        System.out.println();
        System.out.println("Done executing all events");
        System.out.println("Number of events executed = " + e.getTotalNumberEventsExecuted());
        System.out.println("Time executive actually ended = " + e.getActualEndingTime());
        System.out.println("getTime() = " + e.getTime());
        System.out.println("*****************************");
        System.out.println();

        assertTrue(e.getTotalNumberEventsExecuted() == 2);
        assertTrue(e.getTime() == 1.1);
        assertTrue(f);
        
        
        e.end("Ended in Test 4");
        assertTrue(e.getActualEndingTime() == 1.1);
                
    }

     @Test
    public void test5() {
        System.out.println("Test 5");
        //System.out.println("Only first 2 events should execute");

        e.initialize();
        e.executeAllEvents();
        assertTrue(e.noStepsExecuted());
     }
     
    public class ActionListener extends EventAction {

        @Override
        public void action(JSLEvent evt) {
            System.out.println(evt);
        }

        @Override
        public String toString() {
            return "ActionListener";
        }
    }

    public class ExecutiveTrace implements ObserverIfc {

        @Override
        public void update(Object subject, Object arg) {
            Executive exec = (Executive) subject;

            if (exec.getObserverState() == Executive.INITIALIZED) {
                System.out.println("Executive: Initialized before running.");
                System.out.println(exec);
            }

            if (exec.getObserverState() == Executive.AFTER_EXECUTION) {
                System.out.println("Executive: After execution");
                System.out.println(exec);
            }

            if (exec.getObserverState() == Executive.BEFORE_EVENT) {
                JSLEvent event = (JSLEvent) arg;
                System.out.println("Executive: Before event " + event.getName());
            }

            if (exec.getObserverState() == Executive.AFTER_EVENT) {
                JSLEvent event = (JSLEvent) arg;
                System.out.println("Executive: After event " + event.getName());
            }

        }
    }
}
