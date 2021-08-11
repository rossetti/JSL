package examples.utilities.tablesaw;

import examples.queueing.DriverLicenseBureauWithQ;
import jsl.observers.ReplicationDataCollector;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.utilities.reporting.JSL;
import jslx.TablesawUtil;
import jslx.dbutilities.JSLDatabase;
import jslx.dbutilities.JSLDatabaseObserver;
import jslx.excel.ExcelUtil;
import tech.tablesaw.api.Table;

import java.nio.file.Path;

/**
 *  The purpose of this example is to illustrate and test the functionality in TablesawUtil
 *  Shows how to:
 *   1) attach a ReplicationDataCollector to a JSL Model
 *   2) attach a JSLDatabaseObserver that creates a JSLDatabase
 *   3) run the simulation
 *   4) make a Tablesaw Table from the ReplicationDataCollector
 *   5) make a Tablesaw Table from a table in the JSLDatabase
 *   6) write a Tablesaw Table to an Excel workbook
 *   7) read in and create a Tablesaw Table from an Excel workbook sheet
 *
 */
public class TablesawUtilExamples {

    private static JSLDatabase db;
    private static ReplicationDataCollector dataCollector;

    public static void main(String[] args) {
        // called to create a JSLDatabase and a ReplicationDataCollector
        setupExample();
        // now use them to illustrate Tablesaw
        // make a Table from the ReplicationDataCollector
        Table table = TablesawUtil.makeTable(dataCollector);
        table.setName("DLBWithQData");
        System.out.println();
        System.out.println(table.printAll());
        System.out.println();
        // make a Table from the across_rep_view table from within the JSLDatabase
        Table table1 = TablesawUtil.makeTable(db, "across_rep_view");
        System.out.println(table1.printAll());
        System.out.println();
        // write the Tablesaw table to an excel workbook in the JSL.ExcelDir directory
        ExcelUtil.writeTableToExcelWorkbook(table, "TableData.xlsx");
        // Get the path to that workbook
        Path path = JSL.getInstance().getExcelDir().resolve("TableData.xlsx");
        // make a new Tablesaw table from the workbook
        Table table2 = ExcelUtil.makeTable(path, "DLBWithQData", "Table2");
        System.out.println(table2.printAll());
    }

    public static void setupExample(){
        // make the simulation
        Simulation sim = new Simulation("DLB_with_Q");
        // set up a database for the simulation, make sure to set up before running the simulation
        JSLDatabaseObserver jslDatabaseObserver = JSLDatabaseObserver.createJSLDatabaseObserver(sim);

        // set the parameters of the experiment
        sim.setNumberOfReplications(10);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);

        // create the model element and attach it to the main model
        new DriverLicenseBureauWithQ(sim.getModel());

        // add the data collector to observe the model and add all responses to the data collector
        dataCollector = new ReplicationDataCollector(sim.getModel(), true);

        // tell the simulation to run
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        SimulationReporter r = sim.makeSimulationReporter();
        r.printAcrossReplicationSummaryStatistics();

        // get the JSL database
        db = jslDatabaseObserver.getJSLDatabase();
    }
}
