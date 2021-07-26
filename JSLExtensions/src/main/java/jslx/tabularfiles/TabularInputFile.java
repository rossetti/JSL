package jslx.tabularfiles;

import jslx.dbutilities.dbutil.DatabaseFactory;
import jslx.dbutilities.dbutil.DatabaseIfc;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import java.util.*;
import java.util.function.Consumer;

/**
 *  An abstraction for reading rows of tabular data. Columns of the tabular
 *  data can be of numeric or text.  Using this sub-class of TabularFile
 *  users can read rows of data.  The user is responsible for iterating rows with
 *  data of the appropriate type for the column and reading the row into their program.
 *
 *  Use the static methods of TabularFile to create and define the columns of the file.
 *  Use the methods of this class to read rows.
 *
 * @see jslx.tabularfiles.TabularFile
 * @see jslx.tabularfiles.TestTabularWork  For example code
 */
public class TabularInputFile extends TabularFile implements Iterable<RowGetterIfc> {

    public final static int DEFAULT_ROW_BUFFER_SIZE = 100;
    private final DatabaseIfc myDb;
    private int myRowBufferSize = DEFAULT_ROW_BUFFER_SIZE;// maximum number of records held inside iterators
    private final long myTotalNumberRows;
    private final String myDataTableName;
    private final List<Field> myFields; // DSL fields for JOOQ queries
    private final Field<Long> myRowId;  // DSL field for getting rowid
    private final Table<Record> myDataTable; // DSL element for the data table

    /**
     *
     * @param pathToFile the path to a valid file that was written using TabularOutputFile
     */
    public TabularInputFile(Path pathToFile) {
        this(getColumnTypes(pathToFile), pathToFile);
    }

    protected TabularInputFile(LinkedHashMap<String, DataType> columnTypes, Path pathToFile) {
        super(columnTypes, pathToFile);
        // determine the name of the data table
        String fileName = pathToFile.getFileName().toString();
        String fixedFileName = fileName.replaceAll("[^a-zA-Z]", "");
        myDataTableName = fixedFileName.concat("_Data");
        myDataTable = DSL.table(myDataTableName);
        // open up the database file
        myDb = DatabaseFactory.getSQLiteDatabase(pathToFile, true);
        myTotalNumberRows = myDb.getDSLContext().fetchCount(myDataTable);
        myFields = setupFields();
        myRowId = DSL.field("rowid", Long.class);
    }

    private List<Field> setupFields(){
        List<Field> fields = new ArrayList<>();
        for(Map.Entry<String, DataType> ct: myColumnTypes.entrySet()){
            if (ct.getValue() == DataType.NUMERIC){
                Field<Double> field = DSL.field(ct.getKey(), Double.class);
                fields.add(field);
            } else {
                Field<String> field = DSL.field(ct.getKey(), String.class);
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * Gets the meta data for an existing TabularInputFile.  The path must lead
     * to a file that has the correct internal representation for tabular data files.
     * Such a file can be created via TabularOutputFile.
     *
     * @param pathToFile the path to the input file, must not be null
     * @return the meta data for the file column names and data type
     */
    public static LinkedHashMap<String, DataType> getColumnTypes(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file was null");
        if (!DatabaseFactory.isSQLiteDatabase(pathToFile)) {
            throw new IllegalStateException("The path does represent a valid TabularInputFile " + pathToFile);
        }
        // determine the name of the data table
        String fileName = pathToFile.getFileName().toString();
        String fixedFileName = fileName.replaceAll("[^a-zA-Z]", "");
        String dataTableName = fixedFileName.concat("_Data");
        // open up the database file
        DatabaseIfc database = DatabaseFactory.getSQLiteDatabase(pathToFile, true);
        // get the table meta data from the database
        Table<?> dataTable = database.getTable(dataTableName);
        if (dataTable == null) {
            throw new IllegalStateException("The path does represent a valid TabularInputFile " + pathToFile);
        }
        // get the fields of the table
        Field<?>[] fields = dataTable.fields();
        if (fields.length == 0) {
            throw new IllegalStateException("The path does represent a valid TabularInputFile " + pathToFile);
        }
        // process the fields to determine the column names and data types
        LinkedHashMap<String, DataType> columnTypes = new LinkedHashMap<>();
        for (Field<?> field : fields) {
            String fieldName = field.getName();
            org.jooq.DataType<?> jooqType = field.getDataType();
            // get the corresponding data type
            if (jooqType.getTypeName().equals("varchar")) {
                columnTypes.put(fieldName, DataType.TEXT);
            } else if (jooqType.getTypeName().equals("double")) {
                columnTypes.put(fieldName, DataType.NUMERIC);
            } else {
                throw new IllegalStateException("The path does represent a valid TabularInputFile " + pathToFile);
            }
        }
        return columnTypes;
    }

    /**
     *
     * @return the current row buffer size
     */
    public final int getRowBufferSize() {
        return myRowBufferSize;
    }

    /**
     *
     * @param rowBufferSize must be at least 1, bigger implies more memory.
     */
    public void setRowBufferSize(int rowBufferSize) {
        if (rowBufferSize <= 0){
            myRowBufferSize = DEFAULT_ROW_BUFFER_SIZE;
        } else {
            myRowBufferSize = rowBufferSize;
        }
    }

    @Override
    public RowIterator iterator() {
        return new RowIterator();
    }

    /**
     * @param startingRow the starting row for the iteration
     * @return an iterator for moving through the rows
     */
    public RowIterator iterator(long startingRow) {
        return new RowIterator(startingRow);
    }

    public final class RowIterator implements Iterator<RowGetterIfc> {
        private long myCurrentRowNum;
        private long myRemainingNumRows;
        private List<RowGetterIfc> myBufferedRows;
        private Iterator<RowGetterIfc> myRowIterator;

        public RowIterator() {
            this(1);
        }

        public RowIterator(long startingRowNum) {
            if (startingRowNum <= 0) {
                throw new IllegalArgumentException("The row number must be > 0");
            }
            myCurrentRowNum = startingRowNum - 1;
            myRemainingNumRows = myTotalNumberRows - myCurrentRowNum;
            // fill the initial buffer
            long n = Math.min(myRowBufferSize, myRemainingNumRows);
            myBufferedRows = fetchRows(startingRowNum, startingRowNum + n);
            myRowIterator = myBufferedRows.listIterator();
        }

        public final long getCurrentRowNum() {
            return myCurrentRowNum;
        }

        public final long getRemainingNumRows() {
            return myRemainingNumRows;
        }

        @Override
        public final boolean hasNext() {
            return myRemainingNumRows > 0;
        }

        @Override
        public final RowGetterIfc next() {
            if (myRowIterator.hasNext()) {
                // some rows left in the buffer
                // decrement number of rows remaining and return the next row
                // after next the cursor is ready to return next row
                // so current is the one just returned
                myCurrentRowNum = myCurrentRowNum + 1;
                myRemainingNumRows = myRemainingNumRows - 1;
                return myRowIterator.next();
            } else {
                // buffer has no more rows, need to check if more rows remain
                if (hasNext()) {
                    // refill the buffer
                    long n = Math.min(myRowBufferSize, myRemainingNumRows);
                    long startingRow = myCurrentRowNum + 1;
                    myBufferedRows = fetchRows(startingRow, startingRow + n);
                    myRowIterator = myBufferedRows.listIterator();
                    // buffer must have rows
                    // move to first row in new buffer and return it
                    myCurrentRowNum = myCurrentRowNum + 1;
                    myRemainingNumRows = myRemainingNumRows - 1;
                    return myRowIterator.next();
                } else {
                    return null;
                }
            }
        }

        @Override
        public final void forEachRemaining(Consumer<? super RowGetterIfc> action) {
            Iterator.super.forEachRemaining(action);
        }
    }

    /**
     * @return the total number of rows in the tabular file
     */
    public final long getTotalNumberRows() {
        return myTotalNumberRows;
    }

    /**
     * Returns the rows between minRowNum and maxRowNum, inclusive. Since there may be
     * memory implications when using this method, please use it wisely. In fact,
     * use the provided iterator instead.
     *
     * @param minRowNum the minimum row number, must be less than maxRowNum, and 1 or bigger
     * @param maxRowNum the maximum row number, must be greater than minRowNum, and 2 or bigger
     * @return the list of rows, the list may be empty, if there are no rows in the row number range
     */
    public final List<RowGetterIfc> fetchRows(long minRowNum, long maxRowNum) {
        return convertRecordsToRows(selectRows(minRowNum, maxRowNum), minRowNum);
    }

    /**
     * Returns the row.
     * if the provided row number is larger than the number of rows in the file
     * then an exception is thrown. Use fetchRow() if you do not check the number of rows.
     *
     * @param rowNum the row number, must be 1 or more and less than getTotalNumberRows()
     * @return the row
     */
    public final RowGetterIfc fetchOneRow(long rowNum) {
        if (rowNum <= 0) {
            throw new IllegalArgumentException("The row number must be > 0");
        }
        if (rowNum > getTotalNumberRows()) {
            throw new IllegalArgumentException("The row number must be <= " + getTotalNumberRows());
        }
        Optional<RowGetterIfc> rowGetterIfc = fetchRow(rowNum);
        return rowGetterIfc.get();
    }

    /**
     * Returns an optional wrapping the row. The optional will only be empty
     * if the provided row number is larger than the number of rows in the file
     *
     * @param rowNum the row number, must be 1 or more
     * @return the row wrapped in an Optional
     */
    public final Optional<RowGetterIfc> fetchRow(long rowNum) {
        if (rowNum <= 0) {
            throw new IllegalArgumentException("The row number must be > 0");
        }
        if (rowNum > getTotalNumberRows()) {
            return Optional.empty();
        }
        DSLContext dsl = myDb.getDSLContext();
        Record record = dsl.select(myFields).from(myDataTable).where(myRowId.eq(rowNum)).fetchOne();
        if (record == null) {
            return Optional.empty();
        }
        return Optional.of(convertRecordToRow(record, rowNum));
    }

    private Result<Record> selectRows(long minRowNum, long maxRowNum) {
        if (minRowNum <= 0) {
            throw new IllegalArgumentException("The minimum row number must be > 0");
        }
        if (maxRowNum <= 0) {
            throw new IllegalArgumentException("The maximum row number must be > 0");
        }
        if (minRowNum > maxRowNum) {
            throw new IllegalArgumentException("The minimum row number must be < the maximum row number.");
        }
        DSLContext create = myDb.getDSLContext();
        Condition condition = myRowId.between(minRowNum, maxRowNum);
        Result<Record> records = create.select(myFields).from(myDataTable).where(condition).fetch();
        return records;
    }

    private List<RowGetterIfc> convertRecordsToRows(Result<Record> records, long startingRowNum) {
        Objects.requireNonNull(records, "The Result of records was null");
        List<RowGetterIfc> rows = new ArrayList<>();
        for (Record record : records) {
            rows.add(convertRecordToRow(record, startingRowNum));
            startingRowNum = startingRowNum + 1;
        }
        return rows;
    }

    private RowGetterIfc convertRecordToRow(Record record, long rowNum) {
        Objects.requireNonNull(record, "The record was null");
        Row row = new Row(this);
        row.setRowNum(rowNum);
        int n = record.size();
        for (int i = 0; i < n; i++) {
            Object obj = record.get(i);
            if (row.isNumeric(i)) {
                // numeric column
                if (obj == null) {
                    row.setNumeric(i, Double.NaN);
                } else {
                    // obj must be a Double
                    Double x = (Double) obj;
                    row.setNumeric(i, x.doubleValue());
                }
            } else {
                // text column
                row.setText(i, (String) obj);
            }
        }
//        row.setElements(record.intoArray());
        return row;
    }

    /**
     * @param maxRows the total number of rows to extract starting at row 1
     * @return a map of all of the data keyed by column name
     */
    public final LinkedHashMap<String, Double[]> getNumericColumns(int maxRows) {
        return getNumericColumns(maxRows, false);
    }

    /**
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return a map of all of the data keyed by column name
     */
    public final LinkedHashMap<String, Double[]> getNumericColumns(int maxRows, boolean removeMissing) {
        LinkedHashMap<String, Double[]> map = new LinkedHashMap<>();
        List<String> names = getNumericColumnNames();
        for (String name : names) {
            Double[] values = getNumericColumn(name, maxRows, removeMissing);
            map.put(name, values);
        }
        return map;
    }

    /**
     * @param maxRows the total number of rows to extract starting at row 1
     * @return a map of all of the data keyed by column name
     */
    public final LinkedHashMap<String, String[]> getTextColumns(int maxRows) {
        return getTextColumns(maxRows, false);
    }

    /**
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return a map of all of the data keyed by column name
     */
    public final LinkedHashMap<String, String[]> getTextColumns(int maxRows, boolean removeMissing) {
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        List<String> names = getNumericColumnNames();
        for (String name : names) {
            String[] values = getTextColumn(name, maxRows, removeMissing);
            map.put(name, values);
        }
        return map;
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param colNum  the column number to retrieve, must be between [0,getNumberColumns())
     * @param maxRows the total number of rows to extract starting at row 1
     * @return the array of values, including any missing values marked as null
     */
    public final Double[] getNumericColumn(int colNum, int maxRows) {
        return getNumericColumn(colNum, maxRows, false);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param columnName the column name to retrieve, must be between [0,getNumberColumns())
     * @param maxRows    the total number of rows to extract starting at row 1
     * @return the array of values, including any missing values marked as null
     */
    public final Double[] getNumericColumn(String columnName, int maxRows) {
        Objects.requireNonNull(columnName, "The name of the column cannot be null");
        return getNumericColumn(getColumn(columnName), maxRows, false);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param columnName    the column name to retrieve, must be between [0,getNumberColumns())
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return the array of values
     */
    public final Double[] getNumericColumn(String columnName, int maxRows, boolean removeMissing) {
        Objects.requireNonNull(columnName, "The name of the column cannot be null");
        return getNumericColumn(getColumn(columnName), maxRows, removeMissing);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param colNum        the column number to retrieve, must be between [0,getNumberColumns())
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return the array of values
     */
    public final Double[] getNumericColumn(int colNum, int maxRows, boolean removeMissing) {
        if (colNum < 0) {
            throw new IllegalArgumentException("The column number must be >= 0");
        }
        if (colNum >= getNumberColumns()) {
            throw new IllegalArgumentException("The column number must be < " + getNumberColumns());
        }
        if (!isNumeric(colNum)) {
            throw new IllegalArgumentException("The column is not numeric.");
        }
        if (maxRows < 0) {
            throw new IllegalArgumentException("The max number of rows must be >= 0");
        }
        DSLContext dsl = myDb.getDSLContext();
        Condition c = myRowId.le((long) maxRows);
        Field<Double> theField = myFields.get(colNum);
        if (removeMissing){
            c = c.and(theField.isNotNull());
        }
        Double[] doubles = dsl.select(theField).from(myDataTable).where(c).fetchArray(theField, Double.class);
        return doubles;
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param colNum  the column number to retrieve, must be between [0,getNumberColumns())
     * @param maxRows the total number of rows to extract starting at row 1
     * @return the array of values, including any missing values marked as null
     */
    public final String[] getTextColumn(int colNum, int maxRows) {
        return getTextColumn(colNum, maxRows, false);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param columnName the column name to retrieve, must be between [0,getNumberColumns())
     * @param maxRows    the total number of rows to extract starting at row 1
     * @return the array of values, including any missing values marked as null
     */
    public final String[] getTextColumn(String columnName, int maxRows) {
        Objects.requireNonNull(columnName, "The name of the column cannot be null");
        return getTextColumn(getColumn(columnName), maxRows, false);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param columnName    the column name to retrieve, must be between [0,getNumberColumns())
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return the array of values
     */
    public final String[] getTextColumn(String columnName, int maxRows, boolean removeMissing) {
        Objects.requireNonNull(columnName, "The name of the column cannot be null");
        return getTextColumn(getColumn(columnName), maxRows, removeMissing);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param colNum        the column number to retrieve, must be between [0,getNumberColumns())
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return the array of values
     */
    public final String[] getTextColumn(int colNum, int maxRows, boolean removeMissing) {
        if (colNum < 0) {
            throw new IllegalArgumentException("The column number must be >= 0");
        }
        if (colNum >= getNumberColumns()) {
            throw new IllegalArgumentException("The column number must be < " + getNumberColumns());
        }
        if (!isText(colNum)) {
            throw new IllegalArgumentException("The column is not text.");
        }
        if (maxRows < 0) {
            throw new IllegalArgumentException("The max number of rows must be >= 0");
        }
        DSLContext dsl = myDb.getDSLContext();
        Condition c = myRowId.le((long) maxRows);
        Field<String> theField = myFields.get(colNum);
        if (removeMissing){
            c = c.and(theField.isNotNull());
        }
        String[] texts = dsl.select(theField).from(myDataTable).where(c).fetchArray(theField, String.class);
        return texts;
    }

    /**
     *
     * @param out the file to write to
     */
    public final void writeAsCSV(PrintWriter out) {
        myDb.writeTableAsCSV(myDataTableName, out);
    }

    /**
     *  Print to standard out as CSV
     */
    public final void printAsCSV() {
        myDb.printTableAsCSV(myDataTableName);
    }

    /**
     *
     * @param out the file to write to as text
     */
    public final void writeAsText(PrintWriter out) {
        myDb.writeTableAsText(myDataTableName, out);
    }

    /**
     *  Print to standard out as text
     */
    public final void printAsText() {
        myDb.printTableAsText(myDataTableName);
    }

    /**
     *
     * @param wbName the name of the workbook, must not be null
     * @param wbDirectory the path to the directory to contain the workbook, must not be null
     * @throws IOException if something goes wrong with the writing
     */
    public final void writeToExcelWorkbook(String wbName, Path wbDirectory) throws IOException {
        List<String> names = new ArrayList<>();
        names.add(myDataTableName);
        myDb.writeDbToExcelWorkbook(names, wbName, wbDirectory);
    }
}
