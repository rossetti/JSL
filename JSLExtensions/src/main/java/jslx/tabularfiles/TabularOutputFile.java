package jslx.tabularfiles;

import jslx.dbutilities.dbutil.DatabaseFactory;
import jslx.dbutilities.dbutil.DatabaseIfc;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *  An abstraction for writing rows of tabular data. Columns of the tabular
 *  data can be of numeric or text.  Using this sub-class of TabularFile
 *  users can write rows of data.  The user is responsible for filling rows with
 *  data of the appropriate type for the column and writing the row to the file.
 *
 *  Use the static methods of TabularFile to create and define the columns of the file.
 *  Use the methods of this class to write rows.  After writing the rows, it is important
 *  to call the flush() method to ensure that all buffered rows are committed to the file.
 *
 * @see jslx.tabularfiles.TabularFile
 * @see jslx.tabularfiles.TestTabularWork  For example code
 */
public class TabularOutputFile extends TabularFile {
//TODO use a builder pattern to define and add the columns
    //TODO consider permitting the appending of rows to an existing file

    private final static int DEFAULT_PAGE_SIZE = 8192;
    private final static int MIN_DEFAULT_ROWS_IN_BATCH = 32;
    private static int DEFAULT_TEXT_SIZE = 32;

    private final DatabaseIfc myDb;
    private final Table<?> myTable;
    private int myMaxRowsInBatch;
    private final LoadArrayTransaction myLoadArrayTransaction;
    private final Configuration myLoadingConfiguration;
    private Object[][] myLoadArray;
    private int myRowCount = 0;
    private final RowSetterIfc myRow;

    public TabularOutputFile(LinkedHashMap<String, DataType> columnTypes, Path path) {
        super(columnTypes, path);
        String fileName = path.getFileName().toString();
        Path dir = path.getParent();
        myDb = DatabaseFactory.createSQLiteDatabase(fileName, dir);
        String fixedFileName = fileName.replaceAll("[^a-zA-Z]", "");
        String dataTableName = fixedFileName.concat("_Data");
        createTable(dataTableName);
        myTable = myDb.getTable(dataTableName);
        myLoadArrayTransaction = new LoadArrayTransaction();
        myLoadingConfiguration = new DefaultConfiguration();
        myLoadingConfiguration.set(SQLDialect.SQLITE).set(new ConnectionProviderImp());
        int numRowBytes = getNumRowBytes(getNumNumericColumns(), getNumTextColumns(), DEFAULT_TEXT_SIZE);
        int rowBatchSize = getRecommendedRowBatchSize(numRowBytes);
        myMaxRowsInBatch = Math.max(MIN_DEFAULT_ROWS_IN_BATCH, rowBatchSize);
        myLoadArray = new Object[myMaxRowsInBatch][];
        myRow = getRow();
    }

    /**
     *
     * @return the assumed default length of the longest text column
     */
    public static int getDefaultTextSize() {
        return DEFAULT_TEXT_SIZE;
    }

    /** The assumed length of the longest text column. For performance
     *  optimization purposes only.
     *
     * @param defaultTextSize must be 0 or more
     */
    public static void setDefaultTextSize(int defaultTextSize) {
        if (defaultTextSize < 0){
            throw new IllegalArgumentException("The text size must be >= 0");
        }
        DEFAULT_TEXT_SIZE = defaultTextSize;
    }

    /**
     *
     * @param numNumericColumns the number of numeric columns
     * @param numTextColumns the number of text columns
     * @param maxTextLength the length of the longest text column
     * @return the number of bytes on such a row
     */
    public static int getNumRowBytes(int numNumericColumns, int numTextColumns, int maxTextLength){
        if (numNumericColumns < 0){
            throw new IllegalArgumentException("The number of numeric columns must be >= 0");
        }
        if (numTextColumns < 0){
            throw new IllegalArgumentException("The number of text columns must be >= 0");
        }
        if (maxTextLength < 0){
            throw new IllegalArgumentException("The maximum text length must be >= 0");
        }
        if ((numNumericColumns == 0) && (numTextColumns == 0)){
            throw new IllegalArgumentException("The number of numeric columns and the number of text cannot both be zero");
        }
        int nb = numNumericColumns*8;
        int tb = numTextColumns*maxTextLength*2;
        return (nb + tb);
    }

    /**
     *
     * @param rowByteSize the number of bytes in a row, must be greater than 0
     * @return the recommended number of rows in a batch, given the row byte size
     */
    public static final int getRecommendedRowBatchSize(int rowByteSize){
        if (rowByteSize <= 0){
            throw new IllegalArgumentException("The row byte size must be > 0");
        }
        return Math.floorDiv(DEFAULT_PAGE_SIZE, rowByteSize);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(System.lineSeparator());
        sb.append("Estimated number of bytes per row = ");
        int numRowBytes = getNumRowBytes(getNumNumericColumns(), getNumTextColumns(), DEFAULT_TEXT_SIZE);
        int rowBatchSize = getRecommendedRowBatchSize(numRowBytes);
        sb.append(numRowBytes);
        sb.append(System.lineSeparator());
        sb.append("Possible number of rows per batch = ");
        sb.append(rowBatchSize);
        sb.append(System.lineSeparator());
        sb.append("Configured number of rows per batch = ");
        sb.append(myMaxRowsInBatch);
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    /** Allows the user to configure the size of the batch writing if performance becomes an issue.
     *  This may or may not provide any benefit. The static methods related to this functionality
     *  can be used to recommend a reasonable batch size.
     *
     * @param numRows the number of rows to use when writing a batch to disk, must be greater than 0
     */
    public final void setMaxRowsInBatch(int numRows){
        if (numRows <= 0){
            throw new IllegalArgumentException("The number of rows in a batch must be > 0");
        }
        myMaxRowsInBatch = numRows;
    }

    /**
     * Provides a row that can be used to set individual columns
     * before writing the row to the file
     *
     * @return a RowSetterIfc
     */
    public final RowSetterIfc getRow() {
        return new Row(this);
    }

    /**
     * A convenience method. This writes the values in the array
     * to the numeric columns in the file in the order of their appearance.
     * Any text columns will have the value null and cannot be unwritten.
     * <p>
     * The recommended use is for files that have all numeric columns.
     * <p>
     * If you have mixed column types, then use getRow() to first
     * set the appropriate columns before writing them.
     *
     * @param data the data to write
     */
    public final void writeNumeric(double[] data) {
        myRow.setNumeric(data);
        writeRow(myRow);
    }

    /**
     * A convenience method. This writes the values in the array
     * to the text columns in the file in the order of their appearance.
     * Any numeric columns will have the value Double.NaN and cannot be unwritten.
     * <p>
     * The recommended use is for files that have all text columns.
     * <p>
     * If you have mixed column types, then use getRow() to first
     * set the appropriate columns before writing them.
     *
     * @param data the data to write
     */
    public final void writeText(String[] data) {
        myRow.setText(data);
        writeRow(myRow);
    }

    /**
     * Writes the data currently in the row to the file. Once
     * written, the write cannot be undone.
     *
     * @param rowSetter a rowSetter, provided by getRow()
     */
    public final void writeRow(RowSetterIfc rowSetter) {
        Row row = (Row) rowSetter;
        myLoadArray[myRowCount] = row.getElements();
        myRowCount++;
        if (myRowCount == (myMaxRowsInBatch)) {
            loadArray(myLoadArray);
            myRowCount = 0;
        }
    }

    /** A convenience method if the user has a list of rows to write.
     *  All rows in the list are written to the file.
     *
     * @param rows the rows to write, must not be null
     */
    public final void writeRows(List<RowSetterIfc> rows) {
        Objects.requireNonNull(rows, "The list was null");
        for (RowSetterIfc row : rows) {
            writeRow(row);
        }
    }

    /**
     * After writing all rows, you must call flushRows() to ensure that
     * all buffered row data is committed to the file.
     */
    public final void flushRows() {
        if (myRowCount > 0) {
            Object[][] array = new Object[myRowCount][];
            for (int i = 0; i < array.length; i++) {
                array[i] = myLoadArray[i];
            }
            Object[][] temp = myLoadArray;
            // this changes myLoadArray to array for loading
            loadArray(array);
            // now change it back for future loading
            myLoadArray = temp;
            myRowCount = 0;
            // now clear the array
            for (int i = 0; i < myMaxRowsInBatch; i++) {
                myLoadArray[i] = null;
            }
        }
    }

    private void createTable(String name) {
        DSLContext dsl = myDb.getDSLContext();
        // make the fields
        CreateTableColumnStep columnStep = dsl.createTable(name);
        for (Map.Entry<String, DataType> entry : myColumnTypes.entrySet()) {
            columnStep = columnStep.column(entry.getKey(), JOOQ_TYPE.get(entry.getValue()));
        }
        columnStep.execute();
    }

    /**
     * Inner class provide a connection that has been configured
     * to permit transactions.
     */
    private class ConnectionProviderImp implements ConnectionProvider {

        @Override
        public Connection acquire() throws DataAccessException {
            try {
                Connection c = myDb.getConnection();
                c.setAutoCommit(false);
                return c;
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to get a db connection");
            }
        }

        @Override
        public void release(Connection connection) throws DataAccessException {
            try {
                connection.close();
            } catch (SQLException exception) {
                throw new DataAccessException("Unable to close the db connection");
            }
        }
    }

    /**
     * An inner class to wrap a SQL transaction
     */
    private class LoadArrayTransaction implements TransactionalRunnable {
        private Loader<?> loader;

        @Override
        public void run(Configuration configuration) throws Throwable {
            loader = DSL.using(configuration).loadInto(myTable)
                    .batchAll()
                    .bulkAll()
                    .commitAll()
                    .loadArrays(myLoadArray)
                    .fields(myTable.fields())
                    .execute();
        }

    }

    /**
     * @param array the array of data to load into the file
     * @return the number of executed statements that occurred during the loading process
     */
    private int loadArray(Object[][] array) {
        myLoadArray = array;
        DSL.using(myLoadingConfiguration).transaction(myLoadArrayTransaction);
//        Loader<?> loader = dsl.loadInto(myTable)
//                .batchAll()
//                .bulkAll()
////                .commitAll() // commitAll() requires auto commit to be off and a transaction
//                .loadArrays(array)
//                .fields(myTable.fields())
//                .execute();
        List<LoaderError> errors = myLoadArrayTransaction.loader.errors();
        if (!errors.isEmpty()) {
            throw new DataAccessException("Unable to write the data to file " + myPath);
        }
        return myLoadArrayTransaction.loader.executed();
    }

}
