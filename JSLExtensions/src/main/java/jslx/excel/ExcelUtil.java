/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jslx.excel;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import jsl.utilities.reporting.JSL;
import jslx.TablesawUtil;
import jslx.dbutilities.dbutil.DatabaseIfc;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


/** A utility class for reading and writing to Excel from various formats.
 *
 * @author rossetti
 */
public class ExcelUtil {

    public final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public final static int DEFAULT_MAX_CHAR_IN_CELL = 512;

    final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    /**
     * Used to assign unique enum constants
     */
    private static int myEnumCounter_;

    /**
     * Runs writeDBAsExcelWorkbook() to write the supplied database to an Excel workbook with one sheet for
     * every table, squelching all exceptions. The workbook will have the same
     * name as the database
     *
     * @param db         the database to read data from
     * @param tableNames the list of names of tables in the database to write to Excel, must not be null
     */
    public static void runWriteDBAsExcelWorkbook(DatabaseIfc db, List<String> tableNames) {
        Objects.requireNonNull(db, "The supplied DatabaseIfc reference was null");
        Objects.requireNonNull(tableNames, "The supplied list of table names was null");
        runWriteDBAsExcelWorkbook(db, tableNames, Paths.get(db.getLabel()));
    }

    /**
     * Runs writeDBAsExcelWorkbook() to write the supplied database to an Excel workbook with one sheet for
     * every table, squelching all exceptions
     *
     * @param db             the database to read data from
     * @param tableNames     the list of names of tables in the database to write to Excel, must not be null
     * @param pathToWorkbook the name of the workbook that is to be made
     */
    public static void runWriteDBAsExcelWorkbook(DatabaseIfc db, List<String> tableNames, Path pathToWorkbook) {
        try {
            writeDBAsExcelWorkbook(db, tableNames, pathToWorkbook);
        } catch (FileNotFoundException ex) {
            LOG.error("FileNotFoundException {} ", pathToWorkbook, ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            LOG.error("Error in {} runWriteDBAsExcelWorkbook()", pathToWorkbook, ex);
            ex.printStackTrace();
        }

    }

    /**
     * Runs writeWorkbookToDatabase() squelching all exceptions. Read the workbook and writes it
     * into the database.  The first row of each sheet is skipped.
     *
     * @param pathToWorkbook the path to the workbook. Must be valid workbook with .xlsx extension
     * @param db             the database to write to
     * @param tableNames     the names of the sheets and tables in the order that needs to be written
     */
    public static void runWriteWorkbookToDatabase(Path pathToWorkbook, DatabaseIfc db,
                                                  List<String> tableNames) {
        Objects.requireNonNull(db, "The supplied DatabaseIfc reference was null");
        Objects.requireNonNull(tableNames, "The supplied list of table names was null");
        runWriteWorkbookToDatabase(pathToWorkbook, true, db, tableNames);
    }

    /**
     * Runs writeWorkbookToDatabase() squelching all exceptions. Read the workbook and writes it
     * into the database.  The first row of each sheet is skipped.
     *
     * @param pathToWorkbook the path to the workbook. Must be valid workbook with .xlsx extension
     * @param skipFirstRow   if true the first row of each sheet is skipped
     * @param db             the database to write to
     * @param tableNames     the names of the sheets and tables in the order that needs to be written
     */
    public static void runWriteWorkbookToDatabase(Path pathToWorkbook, boolean skipFirstRow, DatabaseIfc db,
                                                  List<String> tableNames) {
        try {
            writeWorkbookToDatabase(pathToWorkbook, skipFirstRow, db, tableNames);
        } catch (IOException e) {
            LOG.error("IOException {} ", pathToWorkbook, e);
            e.printStackTrace();
        }
    }

    /**
     * Writes the supplied database to an Excel workbook with one sheet for
     * every table. This will produce an Excel file with the same name as the
     * database in the current working directory.
     *
     * @param db         the database to read data from
     * @param tableNames the list of names of tables in the database to write to Excel, must not be null
     * @throws IOException io exception
     */
    public static void writeDBAsExcelWorkbook(DatabaseIfc db, List<String> tableNames)
            throws IOException {
        writeDBAsExcelWorkbook(db, tableNames, Paths.get(db.getLabel()));
    }

    /**
     * Writes the supplied database to an Excel workbook with one sheet for
     * every table. This will produce an Excel file with the supplied name in
     * the current working directory. Each sheet of the workbook will have
     * the field names as the first row in the sheet.
     *
     * @param db             the database to read data from, must not be null
     * @param tableNames     the list of names of tables in the database to write to Excel, must not be null
     * @param pathToWorkbook the name of the workbook that was made
     * @throws IOException io exception
     */
    public static void writeDBAsExcelWorkbook(DatabaseIfc db, List<String> tableNames, Path pathToWorkbook)
            throws IOException {
        //  if null make the name of the workbook the same as the database name
        if (pathToWorkbook == null) {
            Path currentDir = Paths.get(".");
            pathToWorkbook = currentDir.resolve(db.getLabel() + ".xlsx");
        }
        LOG.info("Writing database {} to Excel workbook file {}.", db.getLabel(), pathToWorkbook);

        // XSSFWorkbook workbook = new XSSFWorkbook();
        // using SXSSFWorkbook to speed up processing
        // https://poi.apache.org/components/spreadsheet/how-to.html#sxssf
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        fillWorkbookFromDatabase(db, tableNames, workbook);
        FileOutputStream out = new FileOutputStream(pathToWorkbook.toFile());
        workbook.write(out);
        workbook.close();
        out.close();
        workbook.dispose();
    }

    /**
     * Fills the supplied workbook from the database with one sheet for
     * every table.  Each sheet of the workbook will have the field names as the first row in the sheet.
     * If none of the tables are in the database then no sheets are written.  The workbook is just
     * filled. It is not written to a file. The workbook object can continue to be used for additional
     * development before being written to a file.
     *
     * @param db         the database to read data from, must not be null
     * @param tableNames the list of names of tables in the database to write to the workbook, must not be null
     * @param workbook   the workbook to fill, must not be null
     */
    public static void fillWorkbookFromDatabase(DatabaseIfc db, List<String> tableNames, Workbook workbook) {
        Objects.requireNonNull(db, "The supplied Workbook reference was null");
        Objects.requireNonNull(db, "The supplied DatabaseIfc reference was null");
        Objects.requireNonNull(tableNames, "The supplied list of table names was null");
        if (tableNames.isEmpty()) {
            LOG.warn("The supplied list of table names was empty");
        }
        List<String> tables = new ArrayList<>();
        for (String tableName : tableNames) {
            if (db.containsTable(tableName)) {
                tables.add(tableName);
            } else {
                LOG.warn("The supplied table name {} to write to Excel is not in database {}", tableName, db.getLabel());
            }
        }
        if (tables.isEmpty()) {
            LOG.warn("The supplied list of table names had no corresponding tables in database {}, nothing to write.",
                    db.getLabel());
        } else {
            LOG.info("Filling workbook from database: {}.", db.getLabel());
        }
        int i = 0;
        for (String tableName : tables) {
            i++;
            String sheetName = tableName;
            if (sheetName.length() > 31) {
                sheetName = "SheetForTable_" + i;
                LOG.info("Table {} name exceeds 31 characters generating valid sheet name {}", tableName, sheetName);
            }
            Sheet sheet = workbook.createSheet(sheetName);
            // stopped auto sizing to speed up processing
            //sheet.trackAllColumnsForAutoSizing();
            LOG.info("Writing table {} to workbook sheet.", sheetName);
            writeTableAsExcelSheet(db, tableName, sheet);
        }
    }

    /**
     * Writes the sheets of the workbook into database tables. The list of names is the names of the
     * sheets in the workbook and the names of the tables that need to be written. They are in the
     * order that is required for entering data so that no integrity constraints are violated.
     * <p>
     * The first row of every sheet is skipped.
     *
     * @param pathToWorkbook the path to the workbook. Must be valid workbook with .xlsx extension
     * @param db             the database to write to
     * @param tableNames     the names of the sheets and tables in the order that needs to be written
     * @throws IOException an io exception
     */
    public static void writeWorkbookToDatabase(Path pathToWorkbook, DatabaseIfc db,
                                               List<String> tableNames) throws IOException {
        writeWorkbookToDatabase(pathToWorkbook, true, db, tableNames);
    }

    /**
     * Opens the workbook for reading only and writes the sheets of the workbook into database tables.
     * The list of names is the names of the
     * sheets in the workbook and the names of the tables that need to be written. They are in the
     * order that is required for entering data so that no integrity constraints are violated. The
     * underlying workbook is closed after the operation.
     *
     * @param pathToWorkbook the path to the workbook. Must be valid workbook with .xlsx extension
     * @param skipFirstRow   if true the first row of each sheet is skipped
     * @param db             the database to write to
     * @param tableNames     the names of the sheets and tables in the order that needs to be written
     * @throws IOException an io exception
     */
    public static void writeWorkbookToDatabase(Path pathToWorkbook, boolean skipFirstRow, DatabaseIfc db,
                                               List<String> tableNames) throws IOException {

        XSSFWorkbook workbook = openExistingXSSFWorkbookReadOnly(pathToWorkbook);
        if (workbook == null) {
            throw new IOException("There was a problem opening the workbook!");
        }
        LOG.info("Writing workbook {} to database {}", pathToWorkbook, db.getLabel());
        writeWorkbookToDatabase(workbook, skipFirstRow, db, tableNames);
        workbook.close();
        LOG.info("Closed workbook {} ", pathToWorkbook);
        LOG.info("Completed writing workbook {} to database {}", pathToWorkbook, db.getLabel());
    }

    /**
     * IO exceptions are squelched in this method.  If there is a problem, then null is returned.
     * Opens an Apache POI XSSFWorkbook instance. The user is responsible for closing the workbook
     * when done. Do not try to write to the returned workbook.
     *
     * @param pathToWorkbook the path to a valid Excel xlsx workbook
     * @return an Apache POI XSSFWorkbook or null if there was a problem opening the workbook.
     */
    public static XSSFWorkbook openExistingXSSFWorkbookReadOnly(Path pathToWorkbook) {
        Objects.requireNonNull(pathToWorkbook, "The path to the workbook must not be null");
        File file = pathToWorkbook.toFile();
        if (!file.exists()){
            LOG.warn("The file at {} does not exist", pathToWorkbook);
            return null;
        }

        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.open(file, PackageAccess.READ);
        } catch (InvalidFormatException e) {
            LOG.error("The workbook has an invalid format. See Apache POI InvalidFormatException");
            return null;
        }
        //TODO consider using SXSSFWorkbook
        XSSFWorkbook wb = null;
        try {
            wb = new XSSFWorkbook(pkg);
            LOG.info("Opened workbook for reading only at: {}", pathToWorkbook);
        } catch (IOException e) {
            LOG.error("There was an IO error when trying to open the workbook at: {}", pathToWorkbook);
        }
        return wb;
    }

    /**
     * Writes the sheets of the workbook into database tables. The list of names is the names of the
     * sheets in the workbook and the names of the tables that need to be written. They are in the
     * order that is required for entering data so that no integrity constraints are violated.
     * <p>
     * The first row of every sheet is skipped.
     *
     * @param wb         the workbook to copy from
     * @param db         the database to write to
     * @param tableNames the names of the sheets and tables in the order that needs to be written
     * @throws IOException an io exception
     */
    public static void writeWorkbookToDatabase(Workbook wb, DatabaseIfc db,
                                               List<String> tableNames) throws IOException {
        writeWorkbookToDatabase(wb, true, db, tableNames);
    }

    /**
     * Writes the sheets of the workbook into database tables. The list of names is the names of the
     * sheets in the workbook and the names of the tables that need to be written. They are in the
     * order that is required for entering data so that no integrity constraints are violated.
     *
     * @param wb           the workbook to copy from
     * @param skipFirstRow if true the first row of each sheet is skipped
     * @param db           the database to write to
     * @param tableNames   the names of the sheets and tables in the order that needs to be written
     * @throws IOException an io exception
     */
    public static void writeWorkbookToDatabase(Workbook wb, boolean skipFirstRow, DatabaseIfc db,
                                               List<String> tableNames) throws IOException {
        Objects.requireNonNull(wb, "The workbook was null!");
        Objects.requireNonNull(db, "The database was null!");
        Objects.requireNonNull(tableNames, "The list of table names was null!");
        for (String tableName : tableNames) {
            Sheet sheet = wb.getSheet(tableName);
            if (sheet == null) {
                LOG.info("Skipping table {} no corresponding sheet in workbook", tableName);
                continue;
            }
            writeSheetToTable(sheet, skipFirstRow, tableName, db);
        }
    }

    /**
     * Writes the sheet to the named table.  Automatically skips the first row of the sheet. Uses
     * the name of the sheet as the name of the table. The table must exist in the database with that name.
     *
     * @param sheet the sheet to get the data from
     * @param db    the database containing the table
     * @throws IOException an io exception
     */
    public static void writeSheetToTable(Sheet sheet, DatabaseIfc db) throws IOException {
        writeSheetToTable(sheet, true, null, db);
    }

    /**
     * Writes the sheet to the named table.  Automatically skips the first row of the sheet
     *
     * @param sheet     the sheet to get the data from
     * @param tableName the name of the table to write to
     * @param db        the database containing the table
     * @throws IOException an io exception
     */
    public static void writeSheetToTable(Sheet sheet, String tableName, DatabaseIfc db) throws IOException {
        writeSheetToTable(sheet, true, tableName, db);
    }

    /**
     * This method assumes that the tableName exists in the database or that a table with the same
     * name as the sheet exists within the database and that the sheet has the appropriate structure
     * to be placed within the table in the database. If the table does not exist in the database
     * the method returns and logs a warning.
     *
     * @param sheet        the sheet to get the data from, must null be null
     * @param skipFirstRow true means skip the first row of the Excel sheet
     * @param tableName    the name of the table to write to, can be null, if so the sheet name is used
     * @param db           the database containing the table, must not be null
     * @throws IOException an io exception
     */
    public static void writeSheetToTable(Sheet sheet, boolean skipFirstRow, String tableName, DatabaseIfc db)
            throws IOException {
        if (sheet == null) {
            throw new IllegalArgumentException("The Sheet was null");
        }
        if (db == null) {
            throw new IllegalArgumentException("The database was null");
        }
        if (tableName == null) {
            tableName = sheet.getSheetName();
        }
        if (!db.containsTable(tableName)) {
            LOG.warn("Attempting to write sheet {} to database {}, the table {} does not exist",
                    sheet.getSheetName(), db.getLabel(), tableName);
            return;
        }
        final Table<? extends Record> table = db.getTable(tableName);
        final Field<?>[] fields = table.fields();
        LOG.info("Reading sheet {} for table {} in database {}", sheet.getSheetName(), tableName, db.getLabel());
        //TODO could this be a performance bottleneck? all in memory??
        final List<Object[]> lists = readSheetAsListOfObjects(sheet, fields, skipFirstRow);
        db.getDSLContext().loadInto(table).batchAll().loadArrays(lists.iterator()).fields(fields).execute();
        LOG.info("Wrote sheet {} for table {} into database {}", sheet.getSheetName(), tableName, db.getLabel());
    }

    /**
     * Assumes that the first row is a header for a CSV like file and
     * returns the number of columns (1 for each header)
     *
     * @param sheet the sheet to write, must not be null
     * @return the number of header columns
     */
    public static int getNumberColumnsForCSVHeader(Sheet sheet) {
        Objects.requireNonNull(sheet, "The supplied sheet was null");
        Row row = sheet.getRow(0);
        if (row != null) {
            return row.getLastCellNum();
        } else {
            return 0;
        }
    }

    /**
     * Treats the columns as fields in a csv file, writes each row as a separate csv row
     * in the resulting csv file. Uses default maximum cell size. Does not skip the first row.
     * Writes to a CSV file with the same name as the sheet in the current working directory.
     * The number of columns is determined by assuming that the first row contains
     * the CSV header. If the sheet has no columns, then an exception is thrown.
     *
     * @param sheet the sheet to write, must not be null
     * @throws IOException an IO exception
     */
    public static void writeSheetToCSV(Sheet sheet) throws IOException {
        int numCols = getNumberColumnsForCSVHeader(sheet);
        if (numCols <= 0) {
            throw new IllegalStateException("There were no columns in the sheet to write out.");
        }
        writeSheetToCSV(sheet, numCols);
    }

    /**
     * Treats the columns as fields in a csv file, writes each row as a separate csv row
     * in the resulting csv file. Uses default maximum cell size. Does not skip the first row.
     * Writes to a CSV file with the same name as the sheet in the current working directory.
     * The number of columns is determined by assuming that the first row contains
     * the CSV header. If the sheet has no columns, then an exception is thrown.
     *
     * @param sheet         the sheet to write, must not be null
     * @param pathToCSVFile a Path to the file to write as csv, must not be null
     * @throws IOException an IO exception
     */
    public static void writeSheetToCSV(Sheet sheet, Path pathToCSVFile) throws IOException {
        int numCols = getNumberColumnsForCSVHeader(sheet);
        if (numCols <= 0) {
            throw new IllegalStateException("There were no columns in the sheet to write out.");
        }
        writeSheetToCSV(sheet, pathToCSVFile, numCols);
    }

    /**
     * Treats the columns as fields in a csv file, writes each row as a separate csv row
     * in the resulting csv file. Uses default maximum cell size. Does not skip the first row.
     * Writes to a CSV file with the same name as the sheet in the current working directory.
     *
     * @param sheet  the sheet to write, must not be null
     * @param numCol the number of columns to write from each row, must be at least 1
     * @throws IOException an IO exception
     */
    public static void writeSheetToCSV(Sheet sheet, int numCol) throws IOException {
        Objects.requireNonNull(sheet, "The supplied sheet was null");
        String sheetName = sheet.getSheetName();
        Path path = Paths.get(".").resolve(sheetName + ".csv");
        writeSheetToCSV(sheet, false, path, numCol, DEFAULT_MAX_CHAR_IN_CELL);
    }

    /**
     * Treats the columns as fields in a csv file, writes each row as a separate csv row
     * in the resulting csv file. Uses default maximum cell size. Does not skip the first row
     *
     * @param sheet     the sheet to write, must not be null
     * @param pathToCSV a Path to the file to write as csv, must not be null
     * @param numCol    the number of columns to write from each row, must be at least 1
     * @throws IOException an IO exception
     */
    public static void writeSheetToCSV(Sheet sheet, Path pathToCSV, int numCol) throws IOException {
        writeSheetToCSV(sheet, false, pathToCSV, numCol, DEFAULT_MAX_CHAR_IN_CELL);
    }

    /**
     * Treats the columns as fields in a csv file, writes each row as a separate csv row
     * in the resulting csv file. Uses default maximum cell size.
     *
     * @param sheet        the sheet to write, must not be null
     * @param skipFirstRow if true, the first row is skipped in the sheet
     * @param pathToCSV    a Path to the file to write as csv, must not be null
     * @param numCol       the number of columns to write from each row, must be at least 1
     * @throws IOException an IO exception
     */
    public static void writeSheetToCSV(Sheet sheet, boolean skipFirstRow, Path pathToCSV,
                                       int numCol) throws IOException {
        writeSheetToCSV(sheet, skipFirstRow, pathToCSV, numCol, DEFAULT_MAX_CHAR_IN_CELL);
    }

    /**
     * Treats the columns as fields in a csv file, writes each row as a separate csv row
     * in the resulting csv file
     *
     * @param sheet        the sheet to write, must not be null
     * @param skipFirstRow if true, the first row is skipped in the sheet
     * @param pathToCSV    a Path to the file to write as csv, must not be null
     * @param numCol       the number of columns to write from each row, must be at least 1
     * @param maxChar      the maximum number of characters that can be in any cell, must be at least 1
     * @throws IOException an IO exception
     */
    public static void writeSheetToCSV(Sheet sheet, boolean skipFirstRow, Path pathToCSV, int numCol,
                                       int maxChar) throws IOException {
        Objects.requireNonNull(sheet, "The supplied sheet was null");
        Objects.requireNonNull(pathToCSV, "The supplied path was null");
        if (numCol <= 0) {
            throw new IllegalArgumentException("The number of columns must be >= 1");
        }
        if (maxChar <= 0) {
            throw new IllegalArgumentException("The maximum number of characters must be >= 1");
        }
        Iterator<Row> rowIterator = sheet.rowIterator();
        if (skipFirstRow) {
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
        }
        FileWriter fileWriter = new FileWriter(pathToCSV.toFile());
        ICSVWriter writer = new CSVWriterBuilder(fileWriter).build();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String[] strings = readRowAsStringArray(row, numCol, maxChar);
            writer.writeNext(strings);
        }
        writer.close();
    }

    /**
     * @param sheet  the sheet to process
     * @param fields the fields associated with each row
     * @param skipFirstRow true means first row is skipped
     * @return a list of lists of the java objects representing each cell of each row of the sheet
     */
    public static List<List<Object>> readSheetAsObjects(Sheet sheet, Field<?>[] fields, boolean skipFirstRow) {
        if (sheet == null) {
            throw new IllegalArgumentException("The Sheet was null");
        }
        Iterator<Row> rowIterator = sheet.rowIterator();
        if (skipFirstRow) {
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
        }
        List<List<Object>> list = new ArrayList<>();
        while (rowIterator.hasNext()) {
            list.add(readRowAsObjectList(rowIterator.next(), fields));
        }
        return list;
    }

    /**
     * @param sheet  the sheet to process
     * @param fields the fields associated with each row
     * @param skipFirstRow true means first row is skipped
     * @return a list of the arrays of the java objects representing each cell of each row of the sheet
     */
    public static List<Object[]> readSheetAsListOfObjects(Sheet sheet, Field<?>[] fields, boolean skipFirstRow) {
        if (sheet == null) {
            throw new IllegalArgumentException("The Sheet was null");
        }
        Iterator<Row> rowIterator = sheet.rowIterator();
        if (skipFirstRow) {
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
        }
        List<Object[]> list = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            list.add(readRowAsObjectArray(row, fields));
        }
        return list;
    }

    /**
     * Read a row assuming a fixed number of columns.  Cells that
     * are missing/null in the row are read as null objects.
     *
     * @param row    the Excel row
     * @param fields the fields associated with each row
     * @return a list of java objects representing the contents of the cells
     */
    public static List<Object> readRowAsObjectList(Row row, Field<?>[] fields) {
        if (row == null) {
            throw new IllegalArgumentException("The Row was null");
        }
        if (fields == null) {
            throw new IllegalArgumentException("The Fields array was null");
        }
        int numCol = fields.length;
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < numCol; i++) {
            Cell cell = row.getCell(i);
            Object obj = null;
            if (cell != null) {
                obj = readCellAsObject(cell);
                if (obj instanceof String) {
                    int fieldLength = fields[i].getDataType().length();
                    String s = (String) obj;
                    if (s.length() > fieldLength) {
                        s = s.substring(0, fieldLength - 1);
                        obj = s;
                        LOG.warn("The cell {} was truncated to {} characters for field {}", cell.getStringCellValue(), fieldLength, fields[i].getName());
                    }
                }
            }
            list.add(obj);
        }
        return list;
    }

    /**
     * @param row    the Excel row
     * @param fields the fields associated with each row
     * @return an array of java objects representing the contents of the cells within the row
     */
    public static Object[] readRowAsObjectArray(Row row, Field<?>[] fields) {
        List<Object> objects = readRowAsObjectList(row, fields);
        return objects.toArray();
    }

    /**
     * Read a row assuming a fixed number of columns.  Cells that
     * are missing/null in the row are read as null objects.
     *
     * @param row    the Excel row
     * @param numCol the number of columns in the row
     * @return a list of java objects representing the contents of the cells
     */
    public static List<Object> readRowAsObjectList(Row row, int numCol) {
        if (row == null) {
            throw new IllegalArgumentException("The Row was null");
        }
        if (numCol <= 0) {
            throw new IllegalArgumentException("The number of columns must be >= 1");
        }
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < numCol; i++) {
            Cell cell = row.getCell(i);
            Object obj = null;
            if (cell != null) {
                obj = readCellAsObject(cell);
            }
            list.add(obj);
        }
        return list;
    }

    /**
     * @param row    the Excel row
     * @param numCol the number of columns in the row
     * @return an array of java objects representing the contents of the cells within the row
     */
    public static Object[] readRowAsObjectArray(Row row, int numCol) {
        List<Object> objects = readRowAsObjectList(row, numCol);
        return objects.toArray();
    }

    /**
     * Read a row assuming a fixed number of columns.  Cells that
     * are missing/null in the row are read as null Strings.
     *
     * @param row    the Excel row
     * @param numCol the number of columns in the row
     * @return a list of java objects representing the contents of the cells
     */
    public static List<String> readRowAsStringList(Row row, int numCol) {
        return readRowAsStringList(row, numCol, DEFAULT_MAX_CHAR_IN_CELL);
    }

    /**
     * Read a row assuming a fixed number of columns.  Cells that
     * are missing/null in the row are read as null Strings.
     *
     * @param row     the Excel row
     * @param numCol  the number of columns in the row
     * @param maxChar the maximum number of characters permitted for any string
     * @return a list of java Strings representing the contents of the cells
     */
    public static List<String> readRowAsStringList(Row row, int numCol, int maxChar) {
        if (row == null) {
            throw new IllegalArgumentException("The Row was null");
        }
        if (numCol <= 0) {
            throw new IllegalArgumentException("The number of columns must be >= 1");
        }
        if (maxChar <= 0) {
            throw new IllegalArgumentException("The maximum number of characters must be >= 1");
        }
        List<String> list = new ArrayList<>();
        for (int i = 0; i < numCol; i++) {
            Cell cell = row.getCell(i);
            String s = null;
            if (cell != null) {
                s = readCellAsString(cell);
                if (s.length() > maxChar) {
                    s = s.substring(0, maxChar - 1);
                    LOG.warn("The cell {} was truncated to {} characters", cell.getStringCellValue(), maxChar);
                }
            }
            list.add(s);
        }
        return list;
    }

    /**
     * Read a row assuming a fixed number of columns.  Cells that
     * are missing/null in the row are read as null Strings.
     *
     * @param row    the Excel row
     * @param numCol the number of columns in the row
     * @return an array of java Strings representing the contents of the cells
     */
    public static String[] readRowAsStringArray(Row row, int numCol) {
        return readRowAsStringArray(row, numCol, DEFAULT_MAX_CHAR_IN_CELL);
    }

    /**
     * Read a row assuming a fixed number of columns.  Cells that
     * are missing/null in the row are read as null Strings.
     *
     * @param row     the Excel row
     * @param numCol  the number of columns in the row
     * @param maxChar the maximum number of characters permitted for any string
     * @return an array of java Strings representing the contents of the cells
     */
    public static String[] readRowAsStringArray(Row row, int numCol, int maxChar) {
        List<String> list = readRowAsStringList(row, numCol, maxChar);
        String[] strings = new String[list.size()];
        return list.toArray(strings);
    }

    /**
     * Reads the Excel cell and translates it into a String
     *
     * @param cell the Excel cell to read data from
     * @return the data in the form of a Java String
     */
    public static String readCellAsString(Cell cell) {
        Objects.requireNonNull(cell, "The Cell must not be null");
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    date.toInstant().toString();//TODO
                    DATE_TIME_FORMATTER.format(date.toInstant());
                    return DATE_TIME_FORMATTER.format(date.toInstant());
                } else {
                    double value = cell.getNumericCellValue();
                    return Double.toString(value);
                }
            case BOOLEAN:
                boolean value = cell.getBooleanCellValue();
                Boolean.toString(value);//TODO
                return Boolean.toString(value);
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;//TODO should this be an empty string instead?
        }
    }

    /**
     * Reads the Excel cell and translates it into a Java object
     *
     * @param cell the Excel cell to read data from
     * @return the data in the form of a Java object
     */
    public static Object readCellAsObject(Cell cell) {
        Objects.requireNonNull(cell, "The Cell must not be null");
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * Writes a table from the database to the Excel sheet. Includes the field names as the first row of
     * the sheet.
     *
     * @param db        the database containing the table, must not be null
     * @param tableName the table to read from, must not be null
     * @param sheet     the Excel sheet to write to, must not be null
     */
    public static void writeTableAsExcelSheet(DatabaseIfc db, String tableName, Sheet sheet) {
        Objects.requireNonNull(db, "The database must not be null");
        Objects.requireNonNull(sheet, "The workbook sheet must not be null");
        Objects.requireNonNull(tableName, "The table name must not be null");

        if (!db.containsTable(tableName)) {
            LOG.warn("The supplied table name {} is not in database {}", tableName, db.getLabel());
            return;
        }
        Result<Record> records = db.selectAll(tableName);
        if (records == null) {
            LOG.warn("The supplied table name {} resulted in a null Result<Record> nothing was written", tableName);
            return;
        }
        writeResultRecordsAsExcelSheet(records, sheet);
    }

    /** If the workbook exists the sheet containing the results is added to the workbook. If the sheet
     *  exists with the same name then a new sheet is made. See createSheet() method. If the workbook
     *  does not exist, then it is created and the sheet of results added.
     *
     * @param pathToWb the path to the workbook, must not be null. If the workbook exists it is used, if
     *                 it does not exist at the path then a new workbook created at the location
     * @param sheetName the name of the sheet to write to, must not be null, if it already exists
     *                  then a new sheet with name sheetName_n is created where n is one more than the number of sheets
     * @param records the jooq Result to write to the sheet, must not be null
     */
    public static void writeResultRecordsToExcelWorkbook(Path pathToWb, String sheetName, Result<Record> records) {
        Objects.requireNonNull(pathToWb, "The path to the workbook must not be null");
        Objects.requireNonNull(records, "The Result records must not be null");
        Objects.requireNonNull(sheetName, "The workbook sheet name must not be null");

        if (Files.exists(pathToWb)){
            //already exists write to existing file
            writeRecordsToExistingWorkbook(pathToWb, sheetName, records);
        } else {
            // doesn't exist need to create the workbook then write
            writeRecordsToNewWorkbook(pathToWb, sheetName, records);
        }
    }

    private static void writeRecordsToNewWorkbook(Path pathToWb, String sheetName, Result<Record> records){
        Workbook wb = new XSSFWorkbook();
        LOG.info("Created workbook {} for writing records", pathToWb);
        Sheet sheet = createSheet(wb, sheetName);
        writeResultRecordsAsExcelSheet(records, sheet);
        LOG.info("Wrote {} records to sheet {} in workbook {}", records.size(), sheetName, pathToWb);
        try (OutputStream fileOut = new FileOutputStream(pathToWb.toString())) {
            wb.write(fileOut);
            LOG.info("Wrote workbook {} to file.", pathToWb);
            wb.close();
        }catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException {} ", pathToWb, e);
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("IOException {} ", pathToWb, e);
            e.printStackTrace();
        }
    }

    private static void writeRecordsToExistingWorkbook(Path pathToWb, String sheetName, Result<Record> records){
        try (InputStream inp = new FileInputStream(pathToWb.toString())) {
            Workbook wb = WorkbookFactory.create(inp);
            LOG.info("Opened workbook {} for writing records", pathToWb);
            Sheet sheet = createSheet(wb, sheetName);
            LOG.info("Created new sheet {} in workbook {} ", sheetName, pathToWb);
            writeResultRecordsAsExcelSheet(records, sheet);
            LOG.info("Wrote {} records to sheet {} in workbook {}", records.size(), sheetName, pathToWb);
            // Write workbook to a file
            try (OutputStream fileOut = new FileOutputStream(pathToWb.toString())) {
                wb.write(fileOut);
                LOG.info("Wrote workbook {} to file.", pathToWb);
            }
            wb.close();
        } catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException {} ", pathToWb, e);
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("IOException {} ", pathToWb, e);
            e.printStackTrace();
        }
    }

    /** Creates a sheet within the workbook with the name.  If a sheet already exists with the
     * same name then a new sheet with name sheetName_n, where n is the current number of sheets
     * in the workbook is created. Sheet names must follow Excel naming conventions.
     *
     * @param workbook the workbook, must not be null
     * @param sheetName the name of the sheet
     * @return the created sheet
     */
    public static Sheet createSheet(Workbook workbook, String sheetName){
        Objects.requireNonNull(workbook, "The workbook was null");
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
        } else {
            // sheet already exists
            int n = workbook.getNumberOfSheets();
            String name = sheetName + "_" + n;
            sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(name));
        }
        LOG.info("Created new sheet {} in workbook", sheetName);
        return sheet;
    }

    /**
     * Writes the results from a query to the Excel sheet. Includes the field names as the first row of
     * the sheet.
     *
     * @param records the records from a select query, must not be null
     * @param sheet   the Excel sheet to write to, must not be null
     */
    public static void writeResultRecordsAsExcelSheet(Result<Record> records, Sheet sheet) {
        Objects.requireNonNull(records, "The Result records must not be null");
        Objects.requireNonNull(sheet, "The workbook sheet must not be null");
        Field[] fields = records.fields();
        Row header = sheet.createRow(0);
        int i = 0;
        for (Field field : fields) {
            Cell cell = header.createCell(i);
            cell.setCellValue(field.getName());
            sheet.setColumnWidth(i, (field.getName().length() + 2) * 256);
            i++;
        }
        int rowCnt = 1;
        for (Record record : records) {
            Row row = sheet.createRow(rowCnt);
            writeRecordToSheet(record, row);
            rowCnt++;
        }
    }

    /**
     * Starts as the last row number of the sheet and looks up in the column to find the first non-null cell
     *
     * @param sheet       the sheet holding the column, must not be null
     * @param columnIndex the column index, must be 0 or greater, since POI is 0 based columns
     * @return the number of rows that have data in the particular column as defined by not having
     * a null cell.
     */
    public static int getColumnSize(Sheet sheet, int columnIndex) {
        Objects.requireNonNull(sheet, "The workbook sheet must not be null");

        int lastRow = sheet.getLastRowNum();
        while (lastRow >= 0 && isCellEmpty(sheet.getRow(lastRow).getCell(columnIndex))) {
            lastRow--;
        }
        int columnSize = lastRow + 1;
        return columnSize;
    }

    /**
     * @param cell the cell to check
     * @return true if it null or blank or string and empty
     */
    public static boolean isCellEmpty(final Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return true;
        }

        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * Writes a single row from the ResultSet to a row in an Excel Sheet
     *
     * @param record the Record to get the data, must not be null
     * @param row    the Excel row, must not be null
     */
    public static void writeRecordToSheet(Record record, Row row) {
        Objects.requireNonNull(record, "The supplied Record must not be null");
        Objects.requireNonNull(row, "The supplied Row must not be null");
        Field<?>[] fields = record.fields();
        int c = 0;
        for (Field<?> field : fields) {
            Cell cell = row.createCell(c);
            writeCell(cell, record.get(field));
            c++;
        }
    }

    /**
     * Writes the Java Object to the Excel cell
     *
     * @param cell   the cell to write
     * @param object a Java object
     */
    public static void writeCell(Cell cell, Object object) {
        if (object == null) {
            // nothing to write
        } else if (object instanceof String) {
            cell.setCellValue((String) ((String) object).trim());
        } else if (object instanceof Boolean) {
            cell.setCellValue((Boolean) object);
        } else if (object instanceof Integer) {
            cell.setCellValue((Integer) object);
        } else if (object instanceof Double) {
            cell.setCellValue((Double) object);
        } else if (object instanceof Float) {
            cell.setCellValue((Float) object);
        } else if (object instanceof BigDecimal) {
            BigDecimal x = (BigDecimal) object;
            cell.setCellValue(x.doubleValue());
        } else if (object instanceof Long) {
            Long x = (Long) object;
            cell.setCellValue(x.doubleValue());
        } else if (object instanceof Short) {
            Short x = (Short) object;
            cell.setCellValue(x.doubleValue());
        } else if (object instanceof java.sql.Date) {
            java.sql.Date x = (java.sql.Date) object;
            cell.setCellValue(x);
            Workbook wb = cell.getSheet().getWorkbook();
            CellStyle cellStyle = wb.createCellStyle();
            CreationHelper createHelper = wb.getCreationHelper();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("m/d/yy"));
            cell.setCellStyle(cellStyle);
        } else if (object instanceof java.sql.Time) {
            java.sql.Time x = (java.sql.Time) object;
            cell.setCellValue(x);
            Workbook wb = cell.getSheet().getWorkbook();
            CellStyle cellStyle = wb.createCellStyle();
            CreationHelper createHelper = wb.getCreationHelper();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("h:mm:ss AM/PM"));
            cell.setCellStyle(cellStyle);
        } else if (object instanceof java.sql.Timestamp) {
            java.sql.Timestamp x = (java.sql.Timestamp) object;
            java.util.Date dateFromTimeStamp = Date.from(x.toInstant());
            double excelDate = DateUtil.getExcelDate(dateFromTimeStamp);
            cell.setCellValue(excelDate);
            Workbook wb = cell.getSheet().getWorkbook();
            CellStyle cellStyle = wb.createCellStyle();
            CreationHelper createHelper = wb.getCreationHelper();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            cell.setCellStyle(cellStyle);
        } else {
            LOG.error("Could not cast type {} to Excel type.", object.getClass().getName());
            throw new ClassCastException("Could not cast database type to Excel type: " + object.getClass().getName());
        }
    }

    /**
     * Parses and shows the content of one sheet
     * using the specified styles and shared-strings tables.
     *
     * @param styles           The table of styles that may be referenced by cells in the
     *                         sheet
     * @param strings          The table of strings that may be referenced by cells in
     *                         the sheet
     * @param sheetHandler     a sheet handler that knows how to process the sheet
     * @param sheetInputStream The stream to read the sheet-data from.
     * @throws IOException An IO exception from the parser,
     *                     possibly from a byte stream or character stream
     *                     supplied by the application.
     */
    static void processXSSFSheet(StylesTable styles, ReadOnlySharedStringsTable strings,
                                 XSSFSheetXMLHandler.SheetContentsHandler sheetHandler,
                                 InputStream sheetInputStream) throws IOException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = XMLHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(
                    styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            LOG.error("SAX parser appears to be broken - {}", e.getMessage());
            throw new IOException("SAX parser appears to be broken - " + e.getMessage());
        } catch (SAXException e) {
            LOG.error("XML reader appears to be broken - {}", e.getMessage());
            throw new IOException("XML reader appears to be broken - " + e.getMessage());
        }
    }

    /**
     * Initiates the processing of the XLSX workbook using the supplied sheet handler
     *
     * @param xlsxPackage  the xlsx package context for the workbook
     * @param sheetHandler the handler to process each sheet
     * @throws IOException If reading the data from the package fails.
     */
    static void processAllXSSFSheets(OPCPackage xlsxPackage, XSSFSheetXMLHandler.SheetContentsHandler
            sheetHandler) throws IOException {
        ReadOnlySharedStringsTable strings = null;
        try {
            strings = new ReadOnlySharedStringsTable(xlsxPackage);
        } catch (SAXException e) {
            LOG.error("SAX parser appears to be broken - {}", e.getMessage());
            throw new IOException("SAX parser appears to be broken - " + e.getMessage());
        }
        XSSFReader xssfReader = null;
        try {
            xssfReader = new XSSFReader(xlsxPackage);
        } catch (OpenXML4JException e) {
            LOG.error("XML reader appears to be broken - {}", e.getMessage());
            throw new IOException("The XML reader appears to be broken - " + e.getMessage());
        }
        StylesTable styles = null;
        try {
            styles = xssfReader.getStylesTable();
        } catch (InvalidFormatException e) {
            LOG.error("The workbook seems to have a format problem - {}", e.getMessage());
            throw new IOException("The workbook seems to have a format problem - " + e.getMessage());
        }
        XSSFReader.SheetIterator iter = null;
        try {
            iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        } catch (InvalidFormatException e) {
            LOG.error("The sheet seems to have a format problem - {}", e.getMessage());
            throw new IOException("The sheet seems to have a format problem - " + e.getMessage());
        }
        int index = 0;
        while (iter.hasNext()) {
            InputStream stream = iter.next();
            String sheetName = iter.getSheetName();
            processXSSFSheet(styles, strings, sheetHandler, stream);
            stream.close();
            ++index;
        }
    }

    /** Writes the table to a workbook. A new workbook is created and a sheet with the same
     *  name as the table is created. The data is written to the sheet.
     *
     * @param table the Tablesaw Table to write out, must not be null
     * @param wbName the name to the workbook, must not be null. If there already is a workbook file at the path
     *                 location, then it is over written
     */
    public static void writeTableToExcelWorkbook(tech.tablesaw.api.Table table, String wbName){
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
    public static void writeTableToExcelWorkbook(tech.tablesaw.api.Table table, Path pathToWb){
        Objects.requireNonNull(table, "The Tablesaw table was null");
        Objects.requireNonNull(pathToWb, "The path to the new workbook was null");
        if (Files.exists(pathToWb)) {
            try {
                boolean b = Files.deleteIfExists(pathToWb);
                LOG.info("The file {} was written over to with table {}", pathToWb, table.name());
            } catch (IOException e) {
                LOG.error("There was an error deleting file {} when trying to write table {}", pathToWb, table.name());
            }
        }
        Workbook wb = new XSSFWorkbook();
        LOG.info("Created workbook {} for writing table", pathToWb);
        String sheetName = table.name();
        if (sheetName == null){
            sheetName = "TablesawData" + getNextEnumConstant();
        }
        Sheet sheet = createSheet(wb, sheetName);
        writeTableAsExcelSheet(table, sheet);
        LOG.info("Wrote {} Tablesaw rows to sheet {} in workbook {}", table.rowCount(), sheetName, pathToWb);
        try (OutputStream fileOut = new FileOutputStream(pathToWb.toString())) {
            wb.write(fileOut);
            LOG.info("Wrote workbook {} to file.", pathToWb);
            wb.close();
        }catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException {} ", pathToWb, e);
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("IOException {} ", pathToWb, e);
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
    public static void writeTableAsExcelSheet(tech.tablesaw.api.Table table, Sheet sheet) {
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
        int nRows = TablesawUtil.getMaxColumnSize(table);
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
     * Should be used by subclasses to get the next constant
     * so that unique constants can be used
     *
     * @return the constant
     */
    public static int getNextEnumConstant() {
        return (++myEnumCounter_);
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
    public static tech.tablesaw.api.Table makeTable(Path pathToWorkbook, String sheetName){
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
    public static tech.tablesaw.api.Table makeTable(Path pathToWorkbook, String sheetName, String tableName){
        Objects.requireNonNull(sheetName, "The Excel sheet name was null");
        try {
            // convert sheet to temporary csv file
            File csv = JSL.getInstance().makeFile(tableName+".csv");
            ExcelWorkbookAsCSV ewb = new ExcelWorkbookAsCSV(pathToWorkbook);
            ewb.writeXSSFSheetToCSV(sheetName, csv.toPath());
            // create the table from csv
            tech.tablesaw.api.Table table = tech.tablesaw.api.Table.read().csv(csv);
            if (tableName == null){
                tableName = sheetName;
            }
            table.setName(tableName);
            // delete the temporary csv file
            csv.delete();
            return table;
        } catch (IOException e) {
            TablesawUtil.LOGGER.warn("Attempted to make Tablesaw table {} from Excel workbook {} from sheet {} failed, returned an empty Table",
                    tableName, pathToWorkbook.toString(), sheetName);
        }
        // if we get here there was a problem, make an empty table
        return tech.tablesaw.api.Table.create(tableName);
    }
}
