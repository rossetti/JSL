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
import jsl.simulation.SimulationReporter;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.BatchStatistic;
import jslx.charts.JSLChartUtil;
import jsl.utilities.statistic.welch.*;
import tech.tablesaw.plotly.components.Figure;

import java.io.IOException;

/**
 * Illustrates the use of the classes in the jslx.statistics.welch package
 *
 * @author rossetti
 */
public class CaptureWarmUpData {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        //illustrateTracingResponseVariables();
 //       illustrateWelchDataArrayProcessing();
 //       illustrateWelchDataFileProcessing();
       illustrateInitializationBiasTestResults();

//        illustrateJavaFxWelchPlot();
    }

    /**
     *  Illustrate how to trace a response variable observations to a CSV file
     */
    public static void illustrateTracingResponseVariables() {
        Simulation sim = new Simulation("DTP_TraceResponse");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(1.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(0.7));
        // get a reference to the response, in order to turn on the trace
        ResponseVariable rv = m.getResponseVariable("System Time");
        // captures every observation for every experiment to a CSV file
        rv.turnOnTrace(true);
        // set the parameters of the experiment
        sim.setNumberOfReplications(5);
        sim.setLengthOfReplication(20.0);
        sim.setLengthOfWarmUp(5.0);
        SimulationReporter r = sim.makeSimulationReporter();

        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        r.printAcrossReplicationSummaryStatistics();
    }

    /**
     *  Illustrate the collection of Welch data into an in-memory array
     */
    public static void illustrateWelchDataArrayProcessing() {
        Simulation sim = new Simulation("DTP_WelchArray");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(1.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(0.7));
        ResponseVariable rv = m.getResponseVariable("System Time");
        rv.turnOnTrace(true);
        TimeWeighted tw = m.getTimeWeighted("# in System");
        tw.turnOnTrace(true);
        // this creates observers to capture welch data from the responses into arrays
        WelchDataArrayObserver rv_welch = new WelchDataArrayObserver(rv, 1.0);
        WelchDataArrayObserver tw_welch = new WelchDataArrayObserver(tw, 10.0);

        // set the parameters of the experiment
        sim.setNumberOfReplications(5);
        sim.setLengthOfReplication(50000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        r.printAcrossReplicationSummaryStatistics();

        // write out all collected data to CSV files
        rv_welch.makeCSVWelchData();
        tw_welch.makeCSVWelchData();
        // write out Welch plot data to CSV files
        rv_welch.makeCSVWelchPlotData();
        tw_welch.makeCSVWelchPlotData();

        Figure figure = JSLChartUtil.makeWelchPlotFigure(rv_welch);
        JSLChartUtil.showPlot(figure);
    }

    /** Shows how to get the initialization bias test results and plot the
     * partial sum plot
     *
     * @throws IOException if something goes wrong with the files
     */
    public static void illustrateInitializationBiasTestResults() throws IOException {
        Simulation sim = new Simulation("DTP");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(1.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(0.7));
        // get access to the response variables
        ResponseVariable stRV = m.getResponseVariable("System Time");
        TimeWeighted nisTW = m.getTimeWeighted("# in System");
        // create the observers for the responses
        WelchFileObserver rv_welch = new WelchFileObserver(stRV, 1.0);
        WelchFileObserver tw_welch = new WelchFileObserver(nisTW, 10.0);

        // set up the simulation and run it
        sim.setNumberOfReplications(10);
        sim.setLengthOfReplication(30000.0);
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        System.out.println();

        // print out some stuff just to show it
        System.out.println(rv_welch);
        System.out.println(tw_welch);

        // make the Welch data file analyzers
        WelchDataFileAnalyzer stWDFA = rv_welch.makeWelchDataFileAnalyzer();
        WelchDataFileAnalyzer nisWDFA = tw_welch.makeWelchDataFileAnalyzer();
        // make CSV files based on the data
        stWDFA.makeCSVWelchPlotDataFile();
        nisWDFA.makeCSVWelchPlotDataFile();

        BatchStatistic batchWelchAverages = stWDFA.batchWelchAverages();
        System.out.println(batchWelchAverages);

        double ts1 = WelchDataCollectorIfc.getNegativeBiasTestStatistic(batchWelchAverages);
        System.out.println("neg bias test statistic = " + ts1);

        double ts2 = WelchDataCollectorIfc.getPositiveBiasTestStatistic(batchWelchAverages);
        System.out.println("pos bias test statistic = " + ts2);
        
        double[] psums = WelchDataCollectorIfc.getPartialSums(batchWelchAverages);

        Figure figure = JSLChartUtil.makePartialSumsPlotFigure(stWDFA.getResponseName(), psums);
        JSLChartUtil.showPlot(figure,"IBFile");

    }


    /**  Illustrates how to collect Welch data to files
     *
     * @throws IOException if something goes wrong with the files
     */
    public static void illustrateWelchDataFileProcessing() throws IOException {
        // create the simulation
        Simulation sim = new Simulation("DTP_WelchFile");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(1.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(0.7));
        // get references to the responses that need analysis
        ResponseVariable rv = m.getResponseVariable("System Time");
        TimeWeighted tw = m.getTimeWeighted("# in System");
        // create the observers for the responses
        WelchFileObserver rv_welch = new WelchFileObserver(rv, 1.0);
        WelchFileObserver tw_welch = new WelchFileObserver(tw, 10.0);
        // set the parameters of the experiment
        sim.setNumberOfReplications(5);
        sim.setLengthOfReplication(5000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        r.printAcrossReplicationSummaryStatistics();
        // get the WelchDataFileAnalyzer for the system time
        WelchDataFileAnalyzer rvWDFAnalyzer = rv_welch.makeWelchDataFileAnalyzer();
        // write out the plot data to CSV
        rvWDFAnalyzer.makeCSVWelchPlotDataFile();
        // write out the full data to CSV
        rvWDFAnalyzer.makeCSVWelchDataFile();
        // get the WelchDataFileAnalyzer for the # in system
        WelchDataFileAnalyzer twWDFAnalyzer = tw_welch.makeWelchDataFileAnalyzer();
        // write out the plot data to CSV
        twWDFAnalyzer.makeCSVWelchPlotDataFile();
        // write out the full data to CSV
        twWDFAnalyzer.makeCSVWelchDataFile();
    }
}
