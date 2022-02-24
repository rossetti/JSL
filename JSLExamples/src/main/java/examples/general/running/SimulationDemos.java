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

import examples.book.chapter6.DriveThroughPharmacy;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.observers.textfile.CSVResponseReport;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.reporting.JSL;
import jsl.utilities.reporting.StatisticReporter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author rossetti
 */
public class SimulationDemos {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        replicationDeletion();
        //runEachRepSeparately();
        //halfWidthSequentialSampling();
       // demoResponseReport();
    }

    /**
     *  Runs a standard replication deletion simulation and illustrates some of
     *  the reporting methods.  Look in jslOutput for the created files
     */
    public static void replicationDeletion() {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        // getTWBatchStatisticObserver the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacy driveThroughPharmacy = new DriveThroughPharmacy(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(1.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(0.7));
        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfReplication(30000.0);
        sim.setLengthOfWarmUp(10000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        // causes all replication results to be written to a csv file that is named based on simulation name
        r.turnOnReplicationCSVStatisticReporting();// found in jslOutput director
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        // can tell the reporter to print out results
        r.printAcrossReplicationSummaryStatistics();
        // or can tell simulation to print out the results
        sim.printHalfWidthSummaryReport();
        // can create a StatisticReporter based on the statistics captured from the reporter
        StatisticReporter sr = new StatisticReporter(r.getAcrossReplicationStatisticsList());
        // can print out from the StatisticReporter
        System.out.println(sr.getHalfWidthSummaryReport());
        // can write latex tables of results
        r.writeAcrossReplicationSummaryStatisticsAsLaTeX();
//        r.showAcrossReplicationSummaryStatisticsAsPDF("/Library/TeX/texbin/pdflatex");
        // can also print them out
        System.out.println(sr.getHalfWidthSummaryReportAsLaTeXTabular());
    }

    /**
     * Shows how to run each replication separately.
     */
    public static void runEachRepSeparately() {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        // getTWBatchStatisticObserver the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacy driveThroughPharmacy = new DriveThroughPharmacy(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(6.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(3.0));
        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        // must initialize the simulation run
        sim.initialize();
        // this just iterates through each replication and runs the next one
        while(sim.hasNextReplication()){
            sim.runNext();
            // could print out information or do something for each replication here
        }
        
        System.out.println("Simulation completed.");
        r.printAcrossReplicationSummaryStatistics();
    }

    /**
     *  Shows how to create a CSVResponseReport with specific responses
     */
    public static void demoResponseReport(){
        Simulation sim = new Simulation("Drive Through Pharmacy");
         Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacy driveThroughPharmacy = new DriveThroughPharmacy(m);
        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfWarmUp(1000.0);
        sim.setLengthOfReplication(3000.0);
        // define a list of the names of the responses
        List<String> responseNames = Arrays.asList("System Time", "# in System", "Num Served");
        // create a named report to capture the named responses
        Path path = JSL.getInstance().getOutDir().resolve("DTPResponseReport");
        CSVResponseReport report = new CSVResponseReport(path, responseNames);
        // add the report to the model as an observer
        m.addObserver(report);
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        // multiple runs continue to collect the responses
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

    }
}
