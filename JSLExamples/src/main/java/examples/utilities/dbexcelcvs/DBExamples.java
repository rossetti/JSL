package examples.utilities.dbexcelcvs;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.statistic.Statistic;
import jslx.dbutilities.dbutil.Database;
import jslx.dbutilities.dbutil.DatabaseFactory;
import jslx.dbutilities.dbutil.DatabaseIfc;
import jslx.dbutilities.dbutil.DbCreateTask;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * The purpose of this class is to provide a series of examples that utilize some of the functionality within
 * the dbutil package
 */
public class DBExamples {

    static Path pathToWorkingDir = Paths.get("").toAbsolutePath();
    static Path pathToDbExamples = pathToWorkingDir.resolve("dbExamples");

    public static void main(String[] args) throws IOException {
        // This example creates a Derby database called SP_Example_Db within the dbExamples folder
        System.out.println();
        System.out.println("*** example1 output:");
        example1();
        // This example creates a Derby database called SP_To_Excel within the dbExamples folder and exports it to Excel
        System.out.println();
        System.out.println("*** exampleDbToExcelExport output:");
        exampleDbToExcelExport();
        // This example reads an Excel work book holding the SP database information and makes a Derby database
        System.out.println();
        System.out.println("*** exampleExcelDbImport output:");
        exampleExcelDbImport();
        // This example creates the SP database and prints out the SP table
        System.out.println();
        System.out.println("*** exampleSPCreationFromFullScript output:");
        exampleSPCreationFromFullScript();
    }

    /**
     * This example shows how to create a new database from a creation script and perform some simple
     * operations on the database
     */
    public static void example1() {
        // This is an embedded Derby database which resides on the disk
        // Derby holds the database (its files) in a directory.
        // This is the full path to where the database will be held.
        // Define the path to the database.
        Path pathToDb = pathToDbExamples.resolve("SP_Example_Db");
        // Specify the path as a datasource with true indicating that new database will be created (even if old exists)
        DataSource dataSource = DatabaseFactory.createEmbeddedDerbyDataSource(pathToDb, true);
        // Now, make the database from the data source
        Database db = new Database("SP_Example_Db", dataSource, SQLDialect.DERBY);
        // We have only established the database, but there isn't anything in it.
        // Specify the path to the full SQL script file that will create the database structure and fill it.
        Path script = pathToDbExamples.resolve("SPDatabase_FullCreate.sql");
        // Create a database creation execution task and execute it.
        DbCreateTask task = db.create().withCreationScript(script).execute();
        // You can print out the task to illustrate what it is
        // System.out.println(task);
        // You can even print out the script commands
        task.getCreationScriptCommands().forEach(System.out::println);
        // Perform a simple select * command on the table SP
        db.selectAll("SP").format(System.out);
        // Do a regular SQL select statement as a string and print the results
        Result<Record> records = db.fetchResults("select * from s");
        // Print them all out
        records.format(System.out);
        // iterate through each record, get field data using field name and convert to correct data type
        for (Record r : records) {
            int status = r.get("STATUS", Integer.class);
            System.out.println(status);
        }
        // Get the status data as an Integer array
        Integer[] array = records.intoArray("STATUS", Integer.class);
        // Convert it to a double array if you want
        double[] data = JSLArrayUtil.toDouble(array);
        // compute some statistics on it
        System.out.println(Statistic.collectStatistics(data));
    }


    /** Shows how to make a SP database from scripts and then writes the database to an Excel workbook
     *
     * @throws IOException an exception
     */
    public static void exampleDbToExcelExport() throws IOException {
        String dbName = "SP_To_Excel";
        // make the database
        DatabaseIfc db = DatabaseFactory.createEmbeddedDerbyDatabase(dbName, pathToDbExamples);
        // builder the creation task
        Path tables = pathToDbExamples.resolve("SPDatabase_Tables.sql");
        Path inserts = pathToDbExamples.resolve("SPDatabase_Insert.sql");
        Path alters = pathToDbExamples.resolve("SPDatabase_Alter.sql");
        DbCreateTask task = db.create().withTables(tables)
                .withInserts(inserts)
                .withConstraints(alters)
                .execute();

        System.out.println(task);
        db.writeDbToExcelWorkbook("APP", pathToDbExamples);
    }


    /** Shows how to create the SP database by importing from an Excel workbook
     *
     * @throws IOException the IO exception
     */
    public static void exampleExcelDbImport() throws IOException {
        String dbName = "SP_From_Excel";
        // make the database
        DatabaseIfc db = DatabaseFactory.createEmbeddedDerbyDatabase(dbName, pathToDbExamples);

        // builder the creation task
        Path tables = pathToDbExamples.resolve("SPDatabase_Tables.sql");
        Path inserts = pathToDbExamples.resolve("SPDatabase_Insert.sql");
        Path alters = pathToDbExamples.resolve("SPDatabase_Alter.sql");

        Path wbPath = pathToDbExamples.resolve("SP_To_DB.xlsx");

        db.create().withTables(tables)
                .withExcelData(wbPath, Arrays.asList("S", "P", "SP"))
                .withConstraints(alters)
                .execute();

        db.printAllTablesAsText("APP");
    }

    /**
     * This example shows how to create a SP database from a creation script and perform a simple
     * operation on the database.
     *
     * @return the created database
     */
    public static DatabaseIfc exampleSPCreationFromFullScript() {
        // This is an embedded Derby database which resides on the disk
        // Derby holds the database (its files) in a directory.
        // This is the full path to where the database will be held.
        // Define the path to the database.
        Path pathToDb = pathToDbExamples.resolve("SP_FullCreate_Db");
        // Specify the path as a datasource with true indicating that new database will be created (even if old exists)
        DataSource dataSource = DatabaseFactory.createEmbeddedDerbyDataSource(pathToDb, true);
        // Now, make the database from the data source
        Database db = new Database("SP_FullCreate_Db", dataSource, SQLDialect.DERBY);
        // We have only established the database, but there isn't anything in it.
        // Specify the path to the full SQL script file that will create the database structure and fill it.
        Path script = pathToDbExamples.resolve("SPDatabase_FullCreate.sql");
        // Create a database creation execution task and execute it.
        DbCreateTask task = db.create().withCreationScript(script).execute();
        // Perform a simple select * command on the table SP
        db.selectAll("SP").format(System.out);
        return db;
    }
}
