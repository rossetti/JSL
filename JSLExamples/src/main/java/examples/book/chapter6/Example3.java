package examples.book.chapter6;

import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;

/**
 *  This example illustrates the simulation of the up/down component model
 *  and the turning on of event tracing and the log report.
 */
public class Example3 {
    public static void main(String[] args) {
        // create the simulation
        Simulation s = new Simulation("UpDownComponent");
        s.turnOnDefaultEventTraceReport();
        s.turnOnLogReport();
        // get the model associated with the simulation
        Model m = s.getModel();
        // create the model element and attach it to the model
        UpDownComponent tv = new UpDownComponent(m);
        // make the simulation reporter
        SimulationReporter r = s.makeSimulationReporter();
        // set the running parameters of the simulation
        s.setNumberOfReplications(5);
        s.setLengthOfReplication(5000.0);
        // tell the simulation to run
        s.run();
        r.printAcrossReplicationSummaryStatistics();
    }
}
