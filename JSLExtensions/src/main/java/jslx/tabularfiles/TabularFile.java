package jslx.tabularfiles;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import jsl.utilities.JSLArrayUtil;

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
abstract public class TabularFile implements TabularFileIfc {
    //TODO use a builder pattern to define and add the columns

    private final LinkedHashMap<String, DataType> myColumnTypes;
    private final BiMap<String, Integer> myNameAndIndex;
    private final BiMap<Integer, Integer> myNumericIndexes;
    private final List<String> myColumnNames;
    private final List<DataType> myDataTypes;
    private final int[] myNumericIndices;
    private final int[] myTextIndices;

    public TabularFile(LinkedHashMap<String, DataType> columnTypes){
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
        List<Integer> numeric = new ArrayList<>();
        List<Integer> text = new ArrayList<>();
        for(String name: myColumnNames){
            myNameAndIndex.put(name, myDataTypes.size());
            DataType type = myColumnTypes.get(name);
            if (type == DataType.NUMERIC){
                numeric.add(myDataTypes.size());
            } else {
                text.add(myDataTypes.size());
            }
            myDataTypes.add(type);
        }
        myNumericIndices = JSLArrayUtil.toPrimitiveInteger(numeric);
        myTextIndices = JSLArrayUtil.toPrimitiveInteger(text);
        myNumericIndexes = HashBiMap.create();
        for(int i=0; i<myNumericIndices.length; i++){
            myNumericIndexes.put(myNumericIndices[i], i);
        }
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
        return myNumericIndices.length;
    }

    /**
     *
     * @return the total number of text columns
     */
    public final int getNumTextColumns() {
        return myTextIndices.length;
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
}
