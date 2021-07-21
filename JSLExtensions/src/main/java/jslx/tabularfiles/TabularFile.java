package jslx.tabularfiles;

import java.util.*;

/**
 *  An abstraction for holding tabular data in a single file. That is, a list of columns
 *  with specified data types and rows containing the values of every column.
 *  This is a table of mixed data types as specified by Column.DataType (LONG, INTEGER, DOUBLE, TEXT, BOOLEAN).
 *  If you need more complexity, then use a database.
 *
 */
abstract public class TabularFile {
    //TODO use a builder pattern to define and add the columns

    private final List<Column> myColumns;

    public TabularFile(List<Column> columns){
        Objects.requireNonNull(columns, "The list of columns must not be null");
        if (columns.isEmpty()){
            throw new IllegalArgumentException("The number of columns must be > 0");
        }
        Set<Column> colSet = new HashSet<>(columns);
        if (colSet.size() != columns.size()){
            throw new IllegalArgumentException("The names of the columns in the list are not unique!");
        }
        myColumns = new ArrayList<>(columns);
    }

    /** The list is unmodifiable and the elements are immutable.
     *
     * @return the list of columns associated with this tabular file
     */
    public final List<Column> getColumns(){
        return Collections.unmodifiableList(myColumns);
    }


}
