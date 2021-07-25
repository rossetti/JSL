package jslx.tabularfiles;


/*
    Reading requires:
     - iterating forward through each row until end of rows
     - need a row or record abstraction
     - convenience methods for getting arrays of rows when columns all have same type
     - convenience methods for getting arrays of column values
     - convenience methods for getting matrix of row/column values when data is all same type
     - probably need a Cell abstraction, intersection of row and column
 */

import jsl.utilities.JSLArrayUtil;
import jslx.dbutilities.JSLDatabase;
import jslx.dbutilities.dbutil.DatabaseFactory;
import jslx.dbutilities.dbutil.DatabaseIfc;
import org.jooq.*;
import org.jooq.impl.SQLDataType;

import java.nio.file.Path;
import java.sql.Types;

import java.util.*;
import java.util.function.Consumer;

public class TabularInputFile extends TabularFile implements Iterable<RowGetterIfc> {

    private final DatabaseIfc myDb;
    private final Table<?> myTable;
    private int myDefaultRowBufferSize = 100;// maximum number of records held inside iterators
    private final long myTotalNumberRows;

    public TabularInputFile(Path pathToFile) {
        this(getColumnTypes(pathToFile), pathToFile);
    }

    protected TabularInputFile(LinkedHashMap<String, DataType> columnTypes, Path pathToFile) {
        super(columnTypes, pathToFile);
        // determine the name of the data table
        String fileName = pathToFile.getFileName().toString();
        String fixedFileName = fileName.replaceAll("[^a-zA-Z]", "");
        String dataTableName = fixedFileName.concat("_Data");
        // open up the database file
        myDb = DatabaseFactory.getSQLiteDatabase(pathToFile, true);
        myTable = myDb.getTable(dataTableName);
        myTotalNumberRows = myDb.getDSLContext().fetchCount(myTable);
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

    private class SQLiteRowId implements RowId {

        private final Long myValue;

        public SQLiteRowId(long value) {
            myValue = value;
        }

        @Override
        public Object value() {
            return myValue;
        }
    }

    public final class RowIterator implements Iterator<RowGetterIfc> {
        private final long myStartingRowNum;
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
            myStartingRowNum = startingRowNum;
            myCurrentRowNum = startingRowNum - 1;
            myRemainingNumRows = myTotalNumberRows - myCurrentRowNum;
            // fill the initial buffer
            long n = Math.min(myDefaultRowBufferSize, myRemainingNumRows);
            myBufferedRows = fetchRows(myStartingRowNum, myStartingRowNum + n);
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
                    long n = Math.min(myDefaultRowBufferSize, myRemainingNumRows);
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
    public List<RowGetterIfc> fetchRows(long minRowNum, long maxRowNum) {
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
    public RowGetterIfc fetchOneRow(long rowNum) {
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
    public Optional<RowGetterIfc> fetchRow(long rowNum) {
        if (rowNum <= 0) {
            throw new IllegalArgumentException("The row number must be > 0");
        }
        if (rowNum > getTotalNumberRows()) {
            return Optional.empty();
        }
        DSLContext dsl = myDb.getDSLContext();
        Field<RowId> rowID = myTable.rowid();
        Condition condition = rowID.eq(new SQLiteRowId(rowNum));
        Field<?>[] fields = myTable.fields();
        Record record = dsl.select(fields).from(myTable).where(condition).fetchOne();
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
        DSLContext dsl = myDb.getDSLContext();
        Field<RowId> rowID = myTable.rowid();
        Condition condition = rowID.between(new SQLiteRowId(minRowNum), new SQLiteRowId(maxRowNum));
        Field<?>[] fields = myTable.fields();
        Result<Record> records = dsl.select(fields).from(myTable).where(condition).fetch();
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
    public LinkedHashMap<String, Double[]> getNumericColumns(int maxRows) {
        return getNumericColumns(maxRows, false);
    }

    /**
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return a map of all of the data keyed by column name
     */
    public LinkedHashMap<String, Double[]> getNumericColumns(int maxRows, boolean removeMissing) {
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
    public LinkedHashMap<String, String[]> getTextColumns(int maxRows) {
        return getTextColumns(maxRows, false);
    }

    /**
     * @param maxRows       the total number of rows to extract starting at row 1
     * @param removeMissing if true, then missing (NaN values) are removed
     * @return a map of all of the data keyed by column name
     */
    public LinkedHashMap<String, String[]> getTextColumns(int maxRows, boolean removeMissing) {
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
    public Double[] getNumericColumn(int colNum, int maxRows) {
        return getNumericColumn(colNum, maxRows, false);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param columnName the column name to retrieve, must be between [0,getNumberColumns())
     * @param maxRows    the total number of rows to extract starting at row 1
     * @return the array of values, including any missing values marked as null
     */
    public Double[] getNumericColumn(String columnName, int maxRows) {
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
    public Double[] getNumericColumn(String columnName, int maxRows, boolean removeMissing) {
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
    public Double[] getNumericColumn(int colNum, int maxRows, boolean removeMissing) {
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
        Field<RowId> rowID = myTable.rowid();
        Field<?> theField = myTable.field(colNum);
        Condition condition = rowID.le(new SQLiteRowId(maxRows));
        if (removeMissing) {
            condition = condition.and(theField.isNotNull());
        }
        Double[] doubles = dsl.select().from(myTable).where(condition).fetchArray(colNum, Double.class);
        return doubles;
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param colNum  the column number to retrieve, must be between [0,getNumberColumns())
     * @param maxRows the total number of rows to extract starting at row 1
     * @return the array of values, including any missing values marked as null
     */
    public String[] getTextColumn(int colNum, int maxRows) {
        return getTextColumn(colNum, maxRows, false);
    }

    /**
     * Obviously, there are memory issues if their are a lot of rows.
     *
     * @param columnName the column name to retrieve, must be between [0,getNumberColumns())
     * @param maxRows    the total number of rows to extract starting at row 1
     * @return the array of values, including any missing values marked as null
     */
    public String[] getTextColumn(String columnName, int maxRows) {
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
    public String[] getTextColumn(String columnName, int maxRows, boolean removeMissing) {
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
    public String[] getTextColumn(int colNum, int maxRows, boolean removeMissing) {
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
        Field<RowId> rowID = myTable.rowid();
        Field<?> theField = myTable.field(colNum);
        Condition condition = rowID.le(new SQLiteRowId(maxRows));
        if (removeMissing) {
            condition = condition.and(theField.isNotNull());
        }
        String[] texts = dsl.select().from(myTable).where(condition).fetchArray(colNum, String.class);
        return texts;
    }

}
