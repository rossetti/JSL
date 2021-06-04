package jslx;

import jsl.observers.ReplicationDataCollector;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.reporting.JSL;
import jslx.dbutilities.JSLDatabase;
import jslx.dbutilities.dbutil.DatabaseIfc;
import jslx.excel.ExcelUtil;
import jslx.excel.ExcelWorkbookAsCSV;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static jslx.excel.ExcelUtil.writeCell;

/**
 * The purpose of this class is to facilitate interaction with Tablesaw. It provides some minimal ability to
 *  read a database table into a Tablesaw Table, read an Excel sheet into a Table, and to write a Table to an
 *  Excel sheet.
 */
public class TablesawUtil {

    /**
     * Used to assign unique enum constants
     */
    private static int myEnumCounter_;

    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Should be used by subclasses to get the next constant
     * so that unique constants can be used
     *
     * @return the constant
     */
    public static int getNextEnumConstant() {
        return (++myEnumCounter_);
    }

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

    /** Makes a Tablesaw table from a sheet within an Excel workbook. There are some assumptions here.
     * 1) the sheet contains columns of data with each column holding the same data type
     * 2) that the sheet can be converted to a valid csv file format
     *
     * If something goes wrong an empty table is create and a warning is logged.
     *
     * If you have trouble with this, then you might first translate the Excel workbook to a csv file
     * and then use the many options available within Tablesaw to import the data from csv.
     *
     * @param pathToWorkbook must not be null, the path to the Excel workbook
     * @param sheetName must not be null, must be a valid sheet name within the workbook
     * @return the table, will have the same name as the sheet
     */
    public static Table makeTable(Path pathToWorkbook, String sheetName){
        return makeTable(pathToWorkbook, sheetName, null);
    }

    /** Makes a Tablesaw table from a sheet within an Excel workbook. There are some assumptions here.
     * 1) the sheet contains columns of data with each column holding the same data type
     * 2) that the sheet can be converted to a valid csv file format.
     *
     * If something goes wrong an empty table is create and a warning is logged.
     *
     * If you have trouble with this, then you might first translate the Excel workbook to a csv file
     * and then use the many options available within Tablesaw to import the data from csv.
     *
     * @param pathToWorkbook must not be null, the path to the Excel workbook
     * @param sheetName must not be null, must be a valid sheet name within the workbook
     * @param tableName the name that you want for the created table, if null it will have the sheet name
     * @return the table
     */
    public static Table makeTable(Path pathToWorkbook, String sheetName, String tableName){
        Objects.requireNonNull(sheetName, "The Excel sheet name was null");
        try {
            // convert sheet to temporary csv file
            File csv = JSL.getInstance().makeFile(tableName+".csv");
            ExcelWorkbookAsCSV ewb = new ExcelWorkbookAsCSV(pathToWorkbook);
            ewb.writeXSSFSheetToCSV(sheetName, csv.toPath());
            // create the table from csv
            Table table = Table.read().csv(csv);
            if (tableName == null){
                tableName = sheetName;
            }
            table.setName(tableName);
            // delete the temporary csv file
            csv.delete();
            return table;
        } catch (IOException e) {
            LOGGER.warn("Attempted to make Tablesaw table {} from Excel workbook {} from sheet {} failed, returned an empty Table",
                    tableName, pathToWorkbook.toString(), sheetName);
        }
        // if we get here there was a problem, make an empty table
        return Table.create(tableName);
    }

    /** Writes the table to a workbook. A new workbook is created and a sheet with the same
     *  name as the table is created. The data is written to the sheet.
     *
     * @param table the Tablesaw Table to write out, must not be null
     * @param wbName the name to the workbook, must not be null. If there already is a workbook file at the path
     *                 location, then it is over written
     */
    public static void writeTableToExcelWorkbook(Table table, String wbName){
        Objects.requireNonNull(wbName, "The supplied file name was null");
        Path path = JSL.getInstance().getExcelDir().resolve(wbName);
       writeTableToExcelWorkbook(table, path);
    }

    /** Writes the table to a new workbook. A new workbook is created and a sheet with the same
     *  name as the table is created. The data is written to the sheet.
     *
     * @param table the Tablesaw Table to write out, must not be null
     * @param pathToWb the path to the workbook, must not be null. If there already is a workbook file at the path
     *                 location, then it is over written
     */
    public static void writeTableToExcelWorkbook(Table table, Path pathToWb){
        Objects.requireNonNull(table, "The Tablesaw table was null");
        Objects.requireNonNull(pathToWb, "The path to the new workbook was null");
        if (Files.exists(pathToWb)) {
            try {
                boolean b = Files.deleteIfExists(pathToWb);
                ExcelUtil.LOG.info("The file {} was written over to with table {}", pathToWb, table.name());
            } catch (IOException e) {
                ExcelUtil.LOG.error("There was an error deleting file {} when trying to write table {}", pathToWb, table.name());
            }
        }
        Workbook wb = new XSSFWorkbook();
        LOGGER.info("Created workbook {} for writing table", pathToWb);
        String sheetName = table.name();
        if (sheetName == null){
            sheetName = "TablesawData" + getNextEnumConstant();
        }
        Sheet sheet = ExcelUtil.createSheet(wb, sheetName);
        writeTableAsExcelSheet(table, sheet);
        ExcelUtil.LOG.info("Wrote {} Tablesaw rows to sheet {} in workbook {}", table.rowCount(), sheetName, pathToWb);
        try (OutputStream fileOut = new FileOutputStream(pathToWb.toString())) {
            wb.write(fileOut);
            ExcelUtil.LOG.info("Wrote workbook {} to file.", pathToWb);
            wb.close();
        }catch (FileNotFoundException e) {
            ExcelUtil.LOG.error("FileNotFoundException {} ", pathToWb, e);
            e.printStackTrace();
        } catch (IOException e) {
            ExcelUtil.LOG.error("IOException {} ", pathToWb, e);
            e.printStackTrace();
        }
    }
    /**
     * Writes the contents from a Tablesaw Table to the Excel sheet. Includes the column names as the first row of
     * the sheet.
     *
     * @param table the Tablesaw Table, must not be null
     * @param sheet the Excel sheet to write to, must not be null
     */
    public static void writeTableAsExcelSheet(Table table, Sheet sheet) {
        Objects.requireNonNull(table, "The Tablesaw table must not be null");
        Objects.requireNonNull(sheet, "The workbook sheet must not be null");

        List<String> columnNames = table.columnNames();
        Row header = sheet.createRow(0);
        int i = 0;
        for (String name : columnNames) {
            Cell cell = header.createCell(i);
            cell.setCellValue(name);
            sheet.setColumnWidth(i, (name.length() + 2) * 256);
            i++;
        }
        // make all of the rows and their cells
        int nCols = columnNames.size();
        int nRows = getMaxColumnSize(table);
        Cell[][] cells = new Cell[nRows][nCols];
        int rowCnt = 1;
        // this makes cells even if the cell might not hold data
        for (int r = 0; r < nRows; r++) {
            Row excelRow = sheet.createRow(rowCnt);
            for (int c = 0; c < nCols; c++) {
                cells[r][c] = excelRow.createCell(c, CellType.BLANK);
            }
            rowCnt++;
        }
        // now fill the cells from the rows of the columns of the table
        for (int c = 0; c < nCols; c++) {
            for (int r = 0; r < table.column(c).size(); r++) {
                writeCell(cells[r][c], table.column(c).get(r));
            }
        }
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
