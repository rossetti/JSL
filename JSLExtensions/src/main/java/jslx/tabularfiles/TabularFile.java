package jslx.tabularfiles;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
    private final BiMap<Integer, Integer> myNumericIndices;
    private final BiMap<Integer, Integer> myTextIndices;
    private final List<String> myColumnNames;
    private final List<DataType> myDataTypes;

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
                if (!TabularFileIfc.isNumeric(elements[i])){
                    return false;
                }
            } else {
                // must be text
                if (TabularFileIfc.isNumeric(elements[i])){
                    return false;
                }
            }
            i++;
        }
        return true;
    }

}
