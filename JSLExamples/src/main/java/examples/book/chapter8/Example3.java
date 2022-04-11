package examples.book.chapter8;

import examples.book.chapter6.DriveThroughPharmacyWithQ;
import jsl.observers.ReplicationDataCollector;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.Statistic;

/**
 * This example illustrates how to implement common random numbers
 * and capture the simulation results to CSV files. The process is illustrated
 * using an M/M/1 queue versus an M/M/2 queue.
 * While the answer should be obvious, the code illustrates the procedure.
 */
public class Example3 {

    public static void main(String[] args) {
        Simulation sim = new Simulation("Chapter 8 Example 3");
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);

        // create the model elements for the model
        DriveThroughPharmacyWithQ dtp = new DriveThroughPharmacyWithQ(sim.getModel(), 1);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(3.0));

        // Make the reporter after setting up the model
        SimulationReporter reporter = sim.makeSimulationReporter();
        reporter.turnOnAcrossReplicationCSVStatisticReporting();
        reporter.turnOnReplicationCSVStatisticReporting();

        // set the name of the experiment
        sim.setExperimentName("MM1 Experiment");
        sim.run(); // run the first experiment
        sim.printHalfWidthSummaryReport(); // print out some results
        System.out.println();
        // setup model for new experiment
        dtp.setNumberOfPharmacists(2);
        // tell the simulation to reset its streams to the beginning to repeat random numbers used
        sim.setResetStartStreamOption(true);
        // change the name of the experiment within the CSV output
        sim.setExperimentName("MM2 Experiment");
        sim.run(); // run the 2nd experiment
        System.out.println("**** Completed the 2nd model ***");
        sim.printHalfWidthSummaryReport();// print out some results
        // the CSV files are found in jslOutput folder for this simulation
        // see file "Chapter 8 Example 3_ReplicationReport.csv"
    }
}
