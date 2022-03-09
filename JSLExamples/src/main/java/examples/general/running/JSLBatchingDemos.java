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
package examples.general.running;

import examples.book.chapter6.DriveThroughPharmacyWithQ;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.simulation.StatisticalBatchingElement;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TWBatchingElement;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.BatchStatistic;

import java.util.Arrays;

/**
 * Illustrates performing a batch means analysis
 *
 * @author rossetti
 */
public class JSLBatchingDemos {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //runBatchingExample();
        //sequentialBatchingExample();
        batchingASingleResponse();
    }

    /**
     *  Creates a single StatisticalBatchingElement for the simulation. This causes all
     *  responses within the simulation to be batched.  This uses the default settings
     *  for the batching.  A StatisticalReporter is created after the simulation to
     *  report the batch results.  Using the name of a response variable, specific
     *  batch results/data can be accessed.
     */
    public static void runBatchingExample() {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(1.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(0.7));

        // create the batching element for the simulation
        StatisticalBatchingElement be = new StatisticalBatchingElement(m);

        // set the parameters of the experiment
        sim.setNumberOfReplications(1);
        sim.setLengthOfReplication(1300000.0);
        sim.setLengthOfWarmUp(100000.0);
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        // get a StatisticReport from the batching element
        StatisticReporter statisticReporter = be.getStatisticReporter();

        // print out the report
        System.out.println(statisticReporter.getHalfWidthSummaryReport());
        //System.out.println(be.asString());

        // use the name of a response to get a reference to a particular response variable
        ResponseVariable systemTime = m.getResponseVariable("System Time");
        // access the batch statistic from the batching element
        BatchStatistic batchStatistic = be.getBatchStatistic(systemTime);
        // get the actual batch mean values
        double[] batchMeanArrayCopy = batchStatistic.getBatchMeanArrayCopy();
        System.out.println(Arrays.toString(batchMeanArrayCopy));
    }

    /**
     *  Shows how to batch a single response by creating a single batching element.
     */
    public static void batchingASingleResponse() {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        // getTWBatchStatisticObserver the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(6.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(3.0));

        // make a time weighted batching element, accepting the default batching parameters
        TWBatchingElement twbe = new TWBatchingElement(driveThroughPharmacy);
        // get the reference to the response
        TimeWeighted tw = m.getTimeWeighted("# in System");
        // tell the batching element to observe the time weighted variable
        twbe.add(tw);

        // set the parameters of the experiment
        sim.setNumberOfReplications(1);
        sim.setLengthOfReplication(200000.0);
        sim.setLengthOfWarmUp(5000.0);
//        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        sim.printHalfWidthSummaryReport();
        System.out.println(twbe.asString());

    }
}
