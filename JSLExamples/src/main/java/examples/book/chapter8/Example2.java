package examples.book.chapter8;

import examples.book.chapter6.DriveThroughPharmacyWithQ;
import jsl.observers.ReplicationDataCollector;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.Statistic;

/**
 * This example illustrates how to implement common random numbers using
 * a ReplicationDataCollector. A simple comparison will be made between
 * an M/M/1 queue and a M/M/2 queue based on the waiting time in the queue.
 * While the answer should be obvious, the code illustrates the procedure.
 */
public class Example2 {

    public static void main(String[] args) {
        Simulation sim = new Simulation("Chapter 8 Example 2");
        sim.setNumberOfReplications(30);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ dtp = new DriveThroughPharmacyWithQ(sim.getModel(), 1);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(3.0));
        // attach a ReplicationDataCollector before running the model and capture only the waiting time in queue
        ReplicationDataCollector dc = new ReplicationDataCollector(sim.getModel());
        // use the name of the response to add it to the collector
        dc.addResponse("PharmacyQ:TimeInQ");
        sim.run();
        // get the data from the collector
        final double[] mm1Data = dc.getReplicationData("PharmacyQ:TimeInQ");
        System.out.println("**** Completed the 1st model ***");
        sim.printHalfWidthSummaryReport();
        System.out.println();
        // setup model for new experiment
        dtp.setNumberOfPharmacists(2);
        // tell the simulation to reset its streams to the beginning to repeat random numbers used
        // this facilitates CRN
        sim.setResetStartStreamOption(true);
        sim.run();
        // get the data from the collector
        System.out.println("**** Completed the 2nd model ***");
        final double[] mm2Data = dc.getReplicationData("PharmacyQ:TimeInQ");
        sim.printHalfWidthSummaryReport();
        System.out.println();
        // take the difference between the responses
        Statistic diff = new Statistic("Diff. MM1 - MM2");
        for(int i=0; i<mm1Data.length; i++){
            diff.collect(mm1Data[i] - mm2Data[i]);
        }
        System.out.println(diff);
    }
}
