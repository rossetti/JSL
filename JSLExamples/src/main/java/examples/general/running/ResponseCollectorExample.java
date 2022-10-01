package examples.general.running;

import examples.book.chapter6.DriveThroughPharmacyWithQ;
import jsl.observers.ControlVariateDataCollector;
import jsl.observers.ReplicationDataCollector;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.reporting.JSL;
import jslx.CSVUtil;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ResponseCollectorExample {

    public static void main(String[] args) {
        responseCollectorDemo();
//        controlVariateCollectorDemo();
    }

    public static void responseCollectorDemo() {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(1.0, 1));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(0.7, 2));
        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfWarmUp(1000.0);
        sim.setLengthOfReplication(3000.0);
        // define a list of the names of the responses
        List<String> responseNames = Arrays.asList("System Time", "# in System");
        ReplicationDataCollector dc = new ReplicationDataCollector(m);
        for (String s : responseNames) {
            dc.addResponse(s);
        }
        dc.addCounterResponse("Num Served");
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        sim.printHalfWidthSummaryReport();
        System.out.println();
        System.out.println(dc);

    }

    public static void controlVariateCollectorDemo() {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DriveThroughPharmacyWithQ driveThroughPharmacy = new DriveThroughPharmacyWithQ(m);
        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfWarmUp(1000.0);
        sim.setLengthOfReplication(3000.0);

        ControlVariateDataCollector cv = new ControlVariateDataCollector(m);
        // add the response, must use the name from within the model
        cv.addResponse("System Time");
        // add the controls, must use the names from within the model
        cv.addControlVariate("Arrival RV", 1.0);
        cv.addControlVariate("Service RV", 0.5);

        System.out.println(cv.getControlNames());
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");

        sim.printHalfWidthSummaryReport();
        System.out.println();
        System.out.println(cv);
        // write the data to a file
        PrintWriter writer = JSL.getInstance().makePrintWriter("CVData.csv");
        writer.printf("%s,%s, %s %n", "System Time", "Arrival RV", "Service RV");
        double[][] cvData = cv.getData();
        for (int i = 0; i < cvData.length; i++) {
            writer.printf("%f, %f, %f %n", cvData[i][0], cvData[i][1], cvData[i][2]);
        }

        Path pathToFile = JSL.getInstance().getOutDir().resolve("Another_CVData.csv");
        CSVUtil.writeArrayToCSVFile(cv.getAllNames(), cv.getData(), pathToFile);
    }
}
