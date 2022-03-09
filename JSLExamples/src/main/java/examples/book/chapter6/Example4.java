package examples.book.chapter6;

import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 *  This example illustrates the running of the DriveThroughPharmacy instance.
 *  The model is run for 30 replications, of length 20,000 minutes, with a
 *  warmup of 5000.0 minutes. The number of servers can be supplied. In
 *  addition, the user can supply the distribution associated with the time
 *  between arrivals and the service time distribution.
 */
public class Example4 {
    public static void main(String[] args) {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacy dtp = new DriveThroughPharmacy(sim.getModel(), 1);
        dtp.setTimeBtwArrivalRandomSource(new ExponentialRV(6.0));
        dtp.setServiceTimeRandomSource(new ExponentialRV(3.0));
        sim.run();
        SimulationReporter reporter = sim.makeSimulationReporter();
        reporter.printAcrossReplicationSummaryStatistics();
    }
}
