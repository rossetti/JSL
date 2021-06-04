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
package examples.variables;

import examples.queueing.DriverLicenseBureauWithQ;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.modeling.elements.variable.ResponseInterval;
import jsl.modeling.elements.variable.ResponseSchedule;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;

/**
 *
 * @author rossetti
 */
public class TestIntervalSchedule {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation("DLB_with_Q");
        Model m = sim.getModel();
        // create the model element and attach it to the main model
        new DriverLicenseBureauWithQ(m);
        ResponseVariable rs = m.getResponseVariable("System Time");
        TimeWeighted tw = m.getTimeWeighted("NS");
        //ResponseSchedule sched = new ResponseSchedule(m, 5.0);
        ResponseSchedule sched = new ResponseSchedule(m, 0.0);
        //sched.addConsecutiveIntervals(2, 5, "Interval");
        sched.addIntervals(5.0, 2, 5);
        sched.addResponseToAllIntervals(rs);
        sched.addResponseToAllIntervals(tw);
       // sched.setStartTime(5.0);
        //sched.setScheduleRepeatFlag(true);
        sched.setScheduleRepeatFlag(false);

        ResponseInterval ri = new ResponseInterval(m, 1.0, "Hourly");
        ri.setRepeatFlag(true);
        ri.setStartTime(0.0);
        ri.addResponseToInterval(rs, true);
        ri.addResponseToInterval(tw, true);

        System.out.println(sched);
        sim.setNumberOfReplications(2);
        //sim.setLengthOfReplication(20.0);
        sim.setLengthOfReplication(20.0);
        sim.setLengthOfWarmUp(5);
        //sim.setLengthOfWarmUp(1000);
        SimulationReporter r = sim.makeSimulationReporter();

        //System.out.println(sim);
        // tell the simulation to run
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        r.printAcrossReplicationSummaryStatistics();
    }

}
