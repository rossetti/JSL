package examples.book.chapter8;

import examples.book.chapter6.DriveThroughPharmacyWithQ;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.variable.AntitheticEstimator;
import jsl.observers.variable.MultipleComparisonDataCollector;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 * This example illustrates how to turn on antithetic variates when
 * running a simulation
 */
public class AntitheticExample {

    public static void main(String[] args) {
        Simulation sim = new Simulation("Antithetic Example");
        // specify an even number of replication and the antithetic flag as true
        sim.setNumberOfReplications(20, true);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        Model model = sim.getModel();
        // create the model elements for the model
        DriveThroughPharmacyWithQ dtp = new DriveThroughPharmacyWithQ(model, 1);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(3.0));

        ResponseVariable systemTime = model.getResponseVariable("System Time");
        AntitheticEstimator avSystemTime = new AntitheticEstimator("AV System Time");
        systemTime.addObserver(avSystemTime);

        // can capture the responses to a CSV file
        SimulationReporter reporter = sim.makeSimulationReporter();
        reporter.turnOnReplicationCSVStatisticReporting();

        // set the name of the experiment
        sim.run(); // run the  experiment
//         sim.printHalfWidthSummaryReport();

        System.out.println();

        System.out.println(avSystemTime.getStatistic());

    }
}
