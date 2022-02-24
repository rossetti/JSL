package examples.book.chapter6;

import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;

/**
 *  This example illustrates the simulation of a Poisson process
 *  using the JSL Simulation class and a constructed ModelElement
 *  (SimplePoissonProcess).
 */
public class Example2 {
    public static void main(String[] args) {
        Simulation s = new Simulation("Simple PP");
        new SimplePoissonProcess(s.getModel());
        s.setLengthOfReplication(20.0);
        s.setNumberOfReplications(50);
        s.run();
        SimulationReporter r = s.makeSimulationReporter();
        r.printAcrossReplicationSummaryStatistics();
        System.out.println("Done!");
    }
}
