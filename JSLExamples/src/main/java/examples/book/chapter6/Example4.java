package examples.book.chapter6;

import jsl.simulation.Simulation;
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
        runModel(1);
        //runModel(2);
    }

    public static void runModel(int numServers) {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacy dtp = new DriveThroughPharmacy(sim.getModel(), numServers);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(3.0));

        sim.run();
        sim.printHalfWidthSummaryReport();
    }

}
