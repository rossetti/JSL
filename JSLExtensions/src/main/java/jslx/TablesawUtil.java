package jslx;

import jsl.observers.ReplicationDataCollector;
import jsl.utilities.JSLArrayUtil;
import jslx.dbutilities.JSLDatabase;
import jslx.dbutilities.dbutil.DatabaseIfc;
import jslx.tabularfiles.TabularInputFile;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The purpose of this class is to facilitate interaction with Tablesaw. It provides some minimal ability to
 *  read a database table into a Tablesaw Table, read an Excel sheet into a Table, and to write a Table to an
 *  Excel sheet.
 */
public class TablesawUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** Makes a Tablesaw Table that has columns representing each response that was added to
     * the ReplicationDataCollector.  The values of the columns represent the within replication
     * final average in the case of a ResponseVariable or TimeWeightedVariable for all replications. In the case of a
     * Counter is the final value of the counter for all replications. Each row is a replication, with the
     * first row being the first replication, etc.
     *
     * @param dataCollector a replication data collector, must not be null
     * @return the created Tablesaw Table
     */
    public static Table makeTable(ReplicationDataCollector dataCollector){
        Objects.requireNonNull(dataCollector, "The database was null");
        Table table = Table.create();
        List<String> responseNames = dataCollector.getResponseNames();
        for(String name: responseNames){
            double[] values = dataCollector.getReplicationData(name);
            DoubleColumn column = DoubleColumn.create(name, values);
            table.addColumns(column);
        }
        return table;
    }

    /**
     * Makes a Tablesaw table based on the data within the table of the database. If the table is not
     * contained in the database an empty Table is returned (no columns). If there is an exception or
     * other issue accessing the data in the table, then an empty Table is returned (no columns). In
     * either case a warning message is logged.  Any exceptions are squelched.
     *
     * @param db        the database that holds the table, must not be null
     * @param tableName the name of the table in the database, must not null
     * @return the Tablesaw Table
     */
    public static Table makeTable(DatabaseIfc db, String tableName) {
        Objects.requireNonNull(db, "The database was null");
        Objects.requireNonNull(tableName, "The name of the table was null");
        if (!db.containsTable(tableName)) {
            LOGGER.warn("Attempted to create Tablesaw table {} from database {} when table does not exist in db, returned an empty Table",
                    tableName, db.getLabel());
            return Table.create(tableName);
        }
        Result<Record> records = db.selectAll(tableName);
        ResultSet resultSet = records.intoResultSet();
        Table db1 = null;
        try {
            db1 = Table.read().db(resultSet, tableName);
        } catch (SQLException e) {
            LOGGER.warn("There was a SQL exception when creating Tablesaw table {} from database {}, returned an empty table",
                    tableName, db.getLabel());
            return Table.create(tableName);
        }
        return db1;
    }

    /**
     * Makes a Tablesaw table based on the data within the tabular file. If there is an exception or
     * other issue accessing the data in the table, then an empty Table is returned (no columns). In
     * either case a warning message is logged.  Any exceptions are squelched.
     *
     * @param tabularInputFile the tabular file to convert, must not be null
     * @return the Tablesaw table
     */
    public static Table makeTable(TabularInputFile tabularInputFile){
        Objects.requireNonNull(tabularInputFile, "The TabularInputFile was null");
        // get it as a database
        String fileName = tabularInputFile.getPath().getFileName().toString();
        String fixedFileName = fileName.replaceAll("[^a-zA-Z]", "");
        String dataTableName = fixedFileName.concat("_Data");
        try {
            DatabaseIfc database = tabularInputFile.asDatabase();
            Table tbl = makeTable(database, dataTableName);
            // delete the database, no longer needed
            Path parent = tabularInputFile.getPath().getParent();
            Path dbFile = parent.resolve(fixedFileName+".sqlite");
            Files.deleteIfExists(dbFile);
            return tbl;
        } catch (IOException e) {
            LOGGER.warn("There was a exception when creating Tablesaw table {} from tabular file {}, returned an empty table",
                    dataTableName, tabularInputFile.getPath());
            return Table.create(dataTableName);
        }
    }

     /** Makes a Tablesaw table based on the data within the table of the JSLDatabase. If the table is not
     * contained in the database an empty Table is returned (no columns). If there is an exception or
     * other issue accessing the data in the table, then an empty Table is returned (no columns). In
     * either case a warning message is logged.  Any exceptions are squelched.
     *
     * @param db        the JSLDatabase that holds the table, must not be null
     * @param tableName the name of the table in the database, must not null
     * @return the Tablesaw Table
     */
    public static Table makeTable(JSLDatabase db, String tableName){
        Objects.requireNonNull(db, "The JSL database was null");
        return makeTable(db.getDatabase(), tableName);
    }

    /** Returns a map that holds Tablesaw table representations of all of the JSLDatabase views.
     *  These tables can be used for post processing data associated with one or more simulation runs that
     *  were stored within the JSLDatabase
     *
     * @param db an instance of a JSLDatabase
     * @return the map
     */
    public static Map<String, Table> makeJSLDatabaseViewTables(JSLDatabase db){
        Objects.requireNonNull(db, "The JSL database was null");
        Map<String, Table> tables = new HashMap<>();
        List<String> jslViewNames = JSLDatabase.getJSLViewNames();
        for(String name: jslViewNames){
            Table table = makeTable(db.getDatabase(), name);
            tables.put(name, table);
        }
        return tables;
    }

    /**
     * @param table the Tablesaw Table, must not be null
     * @return the sizes of all of the columns of the table (i.e. the number of elements in each column) as an array
     */
    public static int[] getColumnSizes(Table table) {
        Objects.requireNonNull(table, "The Tablesaw table must not be null");
        List<Column<?>> columns = table.columns();
        int[] sizes = new int[columns.size()];
        int i = 0;
        for (Column c : columns) {
            sizes[i] = c.size();
            i++;
        }
        return sizes;
    }

    /**
     * @param table the Tablesaw Table, must not be null
     * @return the size of the column that has the most elements
     */
    public static int getMaxColumnSize(Table table) {
        return JSLArrayUtil.getMax(getColumnSizes(table));
    }
}
