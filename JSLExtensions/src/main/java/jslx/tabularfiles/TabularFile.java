package jslx.tabularfiles;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import jsl.utilities.reporting.JSL;
import org.jooq.impl.SQLDataType;

import java.nio.file.Path;
import java.util.*;

/**
 * An abstraction for holding tabular data in a single file. That is, a list of columns
 * with specified data types and rows containing the values of every column stored within rows within a file.
 * The order of the columns is important. (first column, second column, etc.). The order of the
 * rows is relevant (first row, second row, etc.).
 *
 * There are only two types: numeric and text.
 * The numeric type should be used for numeric data (float, double, long, int, etc.). In addition,
 * use the numeric type for boolean values, which are stored 1.0 = true, 0.0 = false).  The text type
 * should be used for strings and date/time data.  Date/time data is saved
 * as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").  If you need more type complexity, you should use
 * a database.
 *
 */
abstract public class TabularFile  {
    //TODO use a builder pattern to define and add the columns

    public static final ImmutableBiMap<DataType, org.jooq.DataType> JOOQ_TYPE =
            new ImmutableBiMap.Builder<DataType, org.jooq.DataType>()
                    .put(DataType.TEXT, SQLDataType.VARCHAR)
                    .put(DataType.NUMERIC, SQLDataType.DOUBLE)
            .build();

    protected final LinkedHashMap<String, DataType> myColumnTypes;
    protected final BiMap<String, Integer> myNameAndIndex;
    protected final BiMap<Integer, Integer> myNumericIndices;
    protected final BiMap<Integer, Integer> myTextIndices;
    protected final List<String> myColumnNames;
    protected final List<DataType> myDataTypes;
    protected final Path myPath;

    public TabularFile(LinkedHashMap<String, DataType> columnTypes, Path path){
        Objects.requireNonNull(path, "The path to the file was null!");
        myPath = path;
        Objects.requireNonNull(columnTypes, "The column information map must not be null");
        if (columnTypes.isEmpty()){
            throw new IllegalArgumentException("The number of columns must be > 0");
        }
        myColumnTypes = new LinkedHashMap<>(columnTypes);
        myColumnNames = new ArrayList<>();
        for (String name : myColumnTypes.keySet()) {
            myColumnNames.add(name);
        }
        myDataTypes = new ArrayList<>();
        myNameAndIndex = HashBiMap.create();
        int i = 0;
        int cntNumeric = 0;
        int cntText = 0;
        myNumericIndices = HashBiMap.create();
        myTextIndices = HashBiMap.create();
        for(String name: myColumnNames){
            myNameAndIndex.put(name, i);
            DataType type = myColumnTypes.get(name);
            if (type == DataType.NUMERIC){
                myNumericIndices.put(i, cntNumeric);
                cntNumeric++;
            } else {
                myTextIndices.put(i, cntText);
                cntText++;
            }
            myDataTypes.add(type);
            i++;
        }
    }

    /**
     * Creates a double column
     *
     * @param name the name of the column, must not be null
     * @return the created column
     */
    public static ColumnType numericColumn(String name) {
        return new ColumnType(name, DataType.NUMERIC);
    }

    /**
     * Creates a text column
     *
     * @param name the name of the column, must not be null
     * @return the created column
     */
    public static ColumnType textColumn(String name) {
        return new ColumnType(name, DataType.TEXT);
    }

    /**
     * Creates a  column with the given data type
     *
     * @param name     the name of the column, must not be null
     * @param dataType the type of the column, must not be null
     * @return the created column
     */
    public static ColumnType column(String name, DataType dataType) {
        return new ColumnType(name, dataType);
    }

    /**
     * Makes a list of strings containing, C1, C2, ..., CN, where N = number
     *
     * @param number the number of names, must be 1 or more
     * @return the list of names
     */
    public static List<String> columnNames(int number) {
        return columnNames("C", number);
    }

    /**
     * Makes a list of strings containing, prefix1, prefix2,..., prefixN, where N = number
     *
     * @param prefix the prefix for each name, must not be null
     * @param number the number of names, must be 1 or more
     * @return the list of names
     */
    public static List<String> columnNames(String prefix, int number) {
        Objects.requireNonNull(prefix, "The prefix must not be null");
        if (number <= 0) {
            throw new IllegalArgumentException("The number of names must be > 0");
        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            names.add(prefix + (i + 1));
        }
        return names;
    }

    /**
     * Creates names.size() columns with the provided names and data type
     *
     * @param names    the names for the columns, must not be null or empty
     * @param dataType the data type to associated with each column
     * @return a map with the column names all assigned the same data type
     */
    public static LinkedHashMap<String, DataType> columns(List<String> names, DataType dataType) {
        Objects.requireNonNull(dataType, "The data type must not be null");
        Objects.requireNonNull(names, "The list of names must not be null");
        if (names.isEmpty()) {
            throw new IllegalArgumentException("The number of names must be > 0");
        }
        Set<String> nameSet = new HashSet<>(names);
        if (nameSet.size() != names.size()) {
            throw new IllegalArgumentException("The names in the list are not unique!");
        }
        LinkedHashMap<String, DataType> map = new LinkedHashMap<>();
        for (String name : names) {
            map.put(name, dataType);
        }
        return map;
    }

    /**
     * Creates n = numColumns of columns all with the same data type, with names C1, C2, ..., Cn
     *
     * @param numColumns the number of columns to make, must be greater than 0
     * @param dataType   the type of all of the columns
     * @return a map with the column names all assigned the same data type
     */
    public static LinkedHashMap<String, DataType> columns(int numColumns, DataType dataType) {
        Objects.requireNonNull(dataType, "The data type must not be null");
        if (numColumns <= 0) {
            throw new IllegalArgumentException("The number of columns must be > 0");
        }
        return columns(columnNames(numColumns), dataType);
    }

    /**
     * Test if the object is any of {Double, Long, Integer, Boolean, Float, Short, Byte}
     *
     * @param element the element to test
     * @return true if it is numeric
     */
    public static boolean isNumeric(Object element) {
        if (element instanceof Double) {
            return true;
        } else if (element instanceof Integer) {
            return true;
        } else if (element instanceof Long) {
            return true;
        } else if (element instanceof Boolean) {
            return true;
        } else if (element instanceof Float) {
            return true;
        } else if (element instanceof Short) {
            return true;
        } else if (element instanceof Byte) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param element the element to convert
     * @return the element as a double, the element must be numeric
     */
    public static double asDouble(Object element) {
        if (!isNumeric(element)) {
            throw new IllegalArgumentException("The element was not of numeric type");
        }
        if (element instanceof Double) {
            return ((Double) element).doubleValue();
        } else if (element instanceof Integer) {
            return ((Integer) element).doubleValue();
        } else if (element instanceof Long) {
            return ((Long) element).doubleValue();
        } else if (element instanceof Boolean) {
            if (((Boolean) element).booleanValue())
                return 1.0;
            else return 0.0;
        } else if (element instanceof Float) {
            return ((Float) element).doubleValue();
        } else if (element instanceof Short) {
            return ((Short) element).doubleValue();
        } else if (element instanceof Byte) {
            return ((Byte) element).doubleValue();
        } else {
            throw new IllegalArgumentException("The element was not of numeric type");
        }
    }

    /** Creates an all numeric output file
     *
     * @param columnNames the names of the columns
     * @param pathToFile the path to the file
     * @return the output file
     */
    public static TabularOutputFile createAllNumeric(List<String> columnNames, Path pathToFile){
        LinkedHashMap<String, DataType> columns = columns(columnNames, DataType.NUMERIC);
        return new TabularOutputFile(columns, pathToFile);
    }

    /** Creates an all numeric output file with columns C1, C2, etc
     *
     * @param numColumns the number of columns
     * @param pathToFile path to file
     * @return the tabular output file
     */
    public static TabularOutputFile createAllNumeric(int numColumns, Path pathToFile){
        return createAllNumeric(columnNames(numColumns), pathToFile);
    }

    /** Creates an all numeric output file with columns C1, C2, .. in JSL.getInstance().getOutDir()
     *
     * @param numColumns number of columns in the file
     * @param fileName the name of the file, must not be null
     * @return the tabular output file
     */
    public static TabularOutputFile createAllNumeric(int numColumns, String fileName){
        Objects.requireNonNull(fileName, "The file name was null!");
        Path path = JSL.getInstance().getOutDir().resolve(fileName);
        return createAllNumeric(numColumns, path);
    }

    public final Path getPath(){
        return myPath;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Tabular File");
        sb.append(System.lineSeparator());
        sb.append("Path = ").append(myPath);
        sb.append(System.lineSeparator());
        sb.append("Column Names:");
        sb.append(System.lineSeparator());
        sb.append(myColumnNames);
        sb.append(System.lineSeparator());
        sb.append("Column Types:");
        sb.append(System.lineSeparator());
        sb.append(myDataTypes);
        sb.append(System.lineSeparator());
        sb.append("Column Indices:");
        sb.append(System.lineSeparator());
        sb.append(myNameAndIndex);
        sb.append(System.lineSeparator());
        sb.append("Numeric Indices:");
        sb.append(System.lineSeparator());
        sb.append(myNumericIndices);
        sb.append(System.lineSeparator());
        sb.append("Text Indices:");
        sb.append(System.lineSeparator());
        sb.append(myTextIndices);
        return sb.toString();
    }

    /** Returns the storage index of the numeric column at column index
     *
     * @param colNum the column index
     * @return the assigned storage-index for the numeric value
     */
    final int getNumericStorageIndex(int colNum){
        return myNumericIndices.get(colNum);
    }

    /** Returns the storage index of the text column at column index
     *
     * @param colNum the column index
     * @return the assigned storage-index for the text value
     */
    final int getTextStorageIndex(int colNum){
        return myTextIndices.get(colNum);
    }

    /** Returns the column index associated with the storage index
     *
     * @param storageIndex the storage index to find
     * @return the column index
     */
    final int getColumnIndexForNumeric(int storageIndex){
        return myNumericIndices.inverse().get(storageIndex);
    }

    /** Returns the column index associated with the storage index
     *
     * @param storageIndex the storage index to find
     * @return the column index
     */
    final int getColumnIndexForText(int storageIndex){
        return myTextIndices.inverse().get(storageIndex);
    }

    /**
     *
     * @param colNum 0 based indexing
     * @return the name of the column at the index
     */
    public final String getColumnName(int colNum){
        return myNameAndIndex.inverse().get(colNum);
    }

    /**
     *
     * @param colNum 0 based indexing
     * @return the data type of the column at the index
     */
    public final DataType getDataType(int colNum){
        return getDataTypes().get(colNum);
    }

    /**
     * @return the number of columns of tabular data
     */
    public final int getNumberColumns() {
        return getColumnTypes().size();
    }

    /**
     *
     * @param name the name of the column
     * @return the index or -1 if not found
     */
    public final int getColumn(String name){
        Integer integer = myNameAndIndex.get(name);
        if (integer == null){
            return -1;
        }
        return integer.intValue();
    }

    /**
     *
     * @param colNum the index into the row (0 based)
     * @return true if the cell at location i is NUMERIC
     */
    public final boolean isNumeric(int colNum){
        DataType type = myDataTypes.get(colNum);
        if (type == null){
            return false;
        }
        return type == DataType.NUMERIC;
    }

    /**
     *
     * @param colNum the index into the row (0 based)
     * @return true if the cell at location i is TEXT
     */
    public final boolean isText(int colNum){
        DataType type = myDataTypes.get(colNum);
        if (type == null){
            return false;
        }
        return type == DataType.TEXT;
    }

    /**
     *
     * @return the total number of numeric columns
     */
    public final int getNumNumericColumns() {
        return myNumericIndices.size();
    }

    /**
     *
     * @return the total number of text columns
     */
    public final int getNumTextColumns() {
        return myTextIndices.size();
    }

    /**
     *
     * @return the map of columns associated with this tabular file
     */
    public final LinkedHashMap<String, DataType> getColumnTypes(){
        return new LinkedHashMap<>(myColumnTypes);
    }

    /**
     * @return an ordered list of the column names
     */
    public final List<String> getColumnNames() {
        return Collections.unmodifiableList(myColumnNames);
    }

    /**
     *
     * @return  A list of all the numeric column names
     */
    public final List<String> getNumericColumnNames(){
        List<String> theNames = new ArrayList<>();
        List<String> allNames = getColumnNames();
        for (String name: allNames){
            if (isNumeric(getColumn(name))){
                theNames.add(name);
            }
        }
        return theNames;
    }

    /**
     *
     * @return  A list of all the text column names
     */
    public final List<String> getTextColumnNames(){
        List<String> theNames = new ArrayList<>();
        List<String> allNames = getColumnNames();
        for (String name: allNames){
            if (isText(getColumn(name))){
                theNames.add(name);
            }
        }
        return theNames;
    }

    /**
     * @return an ordered list of the column data types
     */
    public final List<DataType> getDataTypes() {
        return Collections.unmodifiableList(myDataTypes);
    }

    /**
     *
     * @return true if all columns have type NUMERIC
     */
    public final boolean isAllNumeric(){
        return myDataTypes.size() == getNumNumericColumns();
    }

    /**
     *
     * @return true if all columns have type TEXT
     */
    public final boolean isAllText(){
        return myDataTypes.size() == getNumTextColumns();
    }

    /**
     *
     * @param elements the elements to check
     * @return true if all elements match the correct types
     */
    public final boolean checkTypes(Object[] elements){
        if (elements == null){
            return false;
        }
        if (elements.length != getNumberColumns()){
            return false;
        }
        List<DataType> dataTypes = getDataTypes();
        int i = 0;
        for(DataType type: dataTypes){
            if (type == DataType.NUMERIC){
                if (!TabularFile.isNumeric(elements[i])){
                    return false;
                }
            } else {
                // must be text
                if (TabularFile.isNumeric(elements[i])){
                    return false;
                }
            }
            i++;
        }
        return true;
    }

}
