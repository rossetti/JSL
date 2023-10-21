/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.general.cafeteria;

import java.io.PrintWriter;
import jsl.observers.variable.MultipleComparisonDataCollector;
import jsl.simulation.ExperimentGetIfc;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.utilities.reporting.JSL;
import jsl.utilities.statistic.MultipleComparisonAnalyzer;

/**
 *
 * @author rossetti
 */
public class ModelRunner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Cafeteria Simulation");
        Simulation s = new Simulation("Cafeteria_Simulation2");
        ExperimentGetIfc e = s.getExperiment();
        // create the model element and attach it to the main model
        Cafeteria c = new Cafeteria(s.getModel(), "Cafeteria");
        MultipleComparisonDataCollector mc = c.attachMCDataCollectorToSystemTime();

        // set the parameters of the simulation
        s.setNumberOfReplications(1000);
        s.setLengthOfReplication(1.5 * 60.0 * 60.0);

        // set up capture of statistics to files
        SimulationReporter r = s.makeSimulationReporter();
        r.turnOnReplicationCSVStatisticReporting();
        r.turnOnAcrossReplicationCSVStatisticReporting();

        // setup and run scenarios
        // changes to the model "accumulate"
        s.setExperimentName("2CS-1HF-1SS");
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-HFF-1SS");
        c.setHotFoodsCDFRange(25.0, 60.0);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-HFF-SSF");
        c.setHotFoodsCDFRange(25.0, 60.0);
        c.setSpecialtySandwichCDFRange(30.0, 90.0);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-1HF-SSF");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(30.0, 90.0);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-2HF-1SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setInitialNumberOfServersAtHotFoods(2);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-2HF-2SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setInitialNumberOfServersAtHotFoods(2);
        c.setInitialNumberOfServersAtSpecialtySandwiches(2);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("3CS-1HF-2SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setNumberCashierStations(3);
        c.setInitialNumberOfServersAtHotFoods(1);
        c.setInitialNumberOfServersAtSpecialtySandwiches(2);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("3CS-2HF-1SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setNumberCashierStations(3);
        c.setInitialNumberOfServersAtHotFoods(2);
        c.setInitialNumberOfServersAtSpecialtySandwiches(1);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("3CS-2HF-2SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setNumberCashierStations(3);
        c.setInitialNumberOfServersAtHotFoods(2);
        c.setInitialNumberOfServersAtSpecialtySandwiches(2);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-3HF-2SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setNumberCashierStations(2);
        c.setInitialNumberOfServersAtHotFoods(3);
        c.setInitialNumberOfServersAtSpecialtySandwiches(2);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-3HF-1SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setNumberCashierStations(2);
        c.setInitialNumberOfServersAtHotFoods(3);
        c.setInitialNumberOfServersAtSpecialtySandwiches(1);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        s.setExperimentName("2CS-4HF-1SS");
        c.setHotFoodsCDFRange(50.0, 120.0);
        c.setSpecialtySandwichCDFRange(60.0, 180.0);
        c.setNumberCashierStations(2);
        c.setInitialNumberOfServersAtHotFoods(4);
        c.setInitialNumberOfServersAtSpecialtySandwiches(1);
        s.run();
        r.writeAcrossReplicationSummaryStatistics(s.getExperimentName() + "_Results");
        r.writeAcrossReplicationCSVStatistics(s.getExperimentName() + "_Results.csv");
        System.out.println("Completed: " + s.getExperimentName());

        mc.writeDataAsCSVFile();

        MultipleComparisonAnalyzer mca = mc.getMultipleComparisonAnalyzer();
        PrintWriter out = JSL.getInstance().makePrintWriter("Cafeteria MCB Results.txt");
        out.println(mca);
        System.out.println("Done!");

    }

}
