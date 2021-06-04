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
import jsl.modeling.elements.Schedule;
import jsl.modeling.elements.ScheduleChangeListenerIfc;

/**
 *
 * @author rossetti
 */
public class ScheduleTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation("Test Schedule");
        Model model = sim.getModel();
        Schedule s = new Schedule.Builder(model).startTime(10).length(100).build();
        s.addItem(0.0, 5);
        s.addItem(5, 10, "message 1");
        s.addItem(30, 40, "message 2");
        s.addScheduleChangeListener(new ScheduleListener());
        System.out.println(s);
        sim.setLengthOfReplication(200);
        sim.setNumberOfReplications(2);
        sim.run();
    }
    
    public static class ScheduleListener implements ScheduleChangeListenerIfc {

        @Override
        public void scheduleStarted(Schedule schedule) {
            System.out.println(schedule.getTime() + "> The schedule started");
        }

        @Override
        public void scheduleEnded(Schedule schedule) {
            System.out.println(schedule.getTime() + "> The schedule ended");
        }

        @Override
        public void scheduleItemStarted(Schedule.ScheduleItem item) {
            double t = item.getSchedule().getTime();
            System.out.println(t + " > Item started: " + item.getId());
            System.out.println(item);
         }

        @Override
        public void scheduleItemEnded(Schedule.ScheduleItem item) {
            double t = item.getSchedule().getTime();
            System.out.println(t + " > Item ended: " + item.getId());
            System.out.println(item);
         }
        
    }
    
}
