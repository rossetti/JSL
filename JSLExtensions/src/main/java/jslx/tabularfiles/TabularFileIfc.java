package jslx.tabularfiles;

import java.nio.file.Path;
import java.util.*;

public interface TabularFileIfc {

    /** Creates a boolean column
     *
     * @param name the name of the column, must not be null
     * @return the created column
     */
    public static Column booleanColumn(String name){
        return new Column(name, Column.DataType.BOOLEAN);
    }

    /** Creates a long column
     *
     * @param name the name of the column, must not be null
     * @return the created column
     */
    public static Column longColumn(String name){
        return new Column(name, Column.DataType.LONG);
    }

    /** Creates a int column
     *
     * @param name the name of the column, must not be null
     * @return the created column
     */
    public static Column intColumn(String name){
        return new Column(name, Column.DataType.INTEGER);
    }

    /** Creates a double column
     *
     * @param name the name of the column, must not be null
     * @return the created column
     */
    public static Column doubleColumn(String name){
        return new Column(name, Column.DataType.DOUBLE);
    }

    /** Creates a text column
     *
     * @param name the name of the column, must not be null
     * @return the created column
     */
    public static Column textColumn(String name){
        return new Column(name, Column.DataType.TEXT);
    }

    /** Creates a  column with the given data type
     *
     * @param name the name of the column, must not be null
     * @param dataType the type of the column, must not be null
     * @return the created column
     */
    public static Column column(String name, Column.DataType dataType){
        return new Column(name, dataType);
    }

    /** Makes a list of strings containing, C1, C2, ..., CN, where N = number
     *
     * @param number the number of names, must be 1 or more
     * @return the list of names
     */
    public static List<String> columnNames(int number){
        return columnNames("C", number);
    }

    /** Makes a list of strings containing, prefix1, prefix2,..., prefixN, where N = number
     *
     * @param prefix the prefix for each name, must not be null
     * @param number the number of names, must be 1 or more
     * @return the list of names
     */
    public static List<String> columnNames(String prefix, int number){
        Objects.requireNonNull(prefix, "The prefix must not be null");
        if (number <= 0) {
            throw new IllegalArgumentException("The number of names must be > 0");
        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            names.add(prefix+(i+1));
        }
        return names;
    }

    /** Creates names.size() columns with the provided names and data type
     *
     * @param names the names for the columns, must not be null or empty
     * @param dataType the data type to associated with each column
     * @return the list of columns
     */
    public static List<Column> columns(List<String> names, Column.DataType dataType){
        Objects.requireNonNull(dataType, "The data type must not be null");
        Objects.requireNonNull(names, "The list of names must not be null");
        if (names.isEmpty()){
            throw new IllegalArgumentException("The number of names must be > 0");
        }
        Set<String> nameSet = new HashSet<>(names);
        if (nameSet.size() != names.size()){
            throw new IllegalArgumentException("The names in the list are not unique!");
        }
        List<Column> columns = new ArrayList<>();
        for(String name: names){
            columns.add(column(name, dataType));
        }
        return columns;
    }

    /** Creates n = numColumns of columns all with the same data type, with names C1, C2, ..., Cn
     *
     * @param numColumns the number of columns to make, must be greater than 0
     * @param dataType the type of all of the columns
     * @return the list containing the columns
     */
    public static List<Column> columns(int numColumns, Column.DataType dataType) {
        Objects.requireNonNull(dataType, "The data type must not be null");
        if (numColumns <= 0) {
            throw new IllegalArgumentException("The number of columns must be > 0");
        }
        return columns(columnNames(numColumns), dataType);
    }

    /**
     *
     * @return the list of columns associated with the tabular data file
     */
    List<Column> getColumns();

    /**
     *
     * @return the number of columns of tabular data
     */
    default int getNumberColumns(){
        return getColumns().size();
    }

    /**
     *
     * @return an ordered list of the column names
     */
    default List<String> getColumnNames(){
        List<String> names = new ArrayList<>();
        List<Column> columns = getColumns();
        for(Column c: columns){
            names.add(c.getName());
        }
        return names;
    }

    /**
     *
     * @return an ordered list of the column data types
     */
    default List<Column.DataType> getColumnDataTypes(){
        List<Column.DataType> types = new ArrayList<>();
        List<Column> columns = getColumns();
        for(Column c: columns){
            types.add(c.getDataType());
        }
        return types;
    }

    /**
     *
     * @return the path to the underlying data file
     */
    Path getPath();
}
