package jslx.tabularfiles;

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
public interface TabularFileIfc {

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
    static boolean isNumeric(Object element) {
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
    static double asDouble(Object element) {
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

    /**
     * @return the list of columns associated with the tabular data file
     */
    LinkedHashMap<String, DataType> getColumnTypes();

    /**
     * @return an ordered list of the column names
     */
    List<String> getColumnNames();

    /**
     * @return an ordered list of the column data types
     */
    List<DataType> getDataTypes();

    /**
     *
     * @param colNum 0 based indexing
     * @return the name of the column at the index
     */
    default String getColumnName(int colNum){
        return getColumnNames().get(colNum);
    }

    /**
     *
     * @param colNum 0 based indexing
     * @return the data type of the column at the index
     */
    default DataType getDataType(int colNum){
        return getDataTypes().get(colNum);
    }

    /**
     * @return the number of columns of tabular data
     */
    default int getNumberColumns() {
        return getColumnTypes().size();
    }

    /**
     * @return the path to the underlying data file
     */
    Path getPath();
}
