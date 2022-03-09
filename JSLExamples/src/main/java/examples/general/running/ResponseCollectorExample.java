package examples.general.running;

import examples.book.chapter6.DriveThroughPharmacyWithQ;
import jsl.observers.ControlVariateDataCollector;
import jsl.observers.ReplicationDataCollector;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jslx.statistics.ControlVariateEstimator;

import java.util.Arrays;
import java.util.List;

public class ResponseCollectorExample {

    public static void main(String[] args) {
        //responseCollectorDemo();
        controlVariateCollectorDemo();
    }

    public static void responseCollectorDemo(){
        Simulation sim = new Simulation("Drive Through Pharmacy");
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfWarmUp(1000.0);
        sim.setLengthOfReplication(3000.0);
        // define a list of the names of the responses
        List<String> responseNames = Arrays.asList("System Time", "# in System");
        ReplicationDataCollector dc = new ReplicationDataCollector(m);
        for(String s: responseNames){
            dc.addResponse(s);
        }
        dc.addCounterResponse("Num Served");
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        sim.printHalfWidthSummaryReport();
        System.out.println(dc);

    }

    public static void controlVariateCollectorDemo(){
        Simulation sim = new Simulation("Drive Through Pharmacy");
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfWarmUp(1000.0);
        sim.setLengthOfReplication(3000.0);

        ControlVariateDataCollector cv = new ControlVariateDataCollector(m);
        cv.addResponse("System Time");
        cv.addControlVariate("Arrival RV", 1.0);
        cv.addControlVariate("Service RV", 0.5);

        System.out.println(cv.getControlNames());
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        sim.printHalfWidthSummaryReport();
        System.out.println(cv);

        ControlVariateEstimator cve = new ControlVariateEstimator(cv);

        System.out.println(cve);

    }
}
