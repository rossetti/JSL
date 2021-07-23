package jslx.tabularfiles;

import java.util.*;

public class Row2 {

    private final TabularFile myTabularFile;
    private final String[] textData;
    private final double[] numericData;

    //TODO consider getting rid of cells and just using arrays double[] and String[]
    // for the data in a row because the number is known in advance

    public Row2(TabularFile tabularFile) {
        Objects.requireNonNull(tabularFile, "The tabular file was null");
        myTabularFile = tabularFile;
        textData = new String[tabularFile.getNumTextColumns()];
        numericData = new double[tabularFile.getNumNumericColumns()];
        Arrays.fill(numericData, Double.NaN);
    }

    /**
     * @return the total number of numeric columns
     */
    public final int getNumNumericColumns() {
        return myTabularFile.getNumNumericColumns();
    }

    /**
     * @return the total number of text columns
     */
    public final int getNumTextColumns() {
        return myTabularFile.getNumTextColumns();
    }

    /**
     * @return the map of columns associated with this row
     */
    public final LinkedHashMap<String, DataType> getColumnTypes() {
        return myTabularFile.getColumnTypes();
    }

    /**
     * @return an ordered list of the column names for the row
     */
    public final List<String> getColumnNames() {
        return myTabularFile.getColumnNames();
    }

    /**
     * @return an ordered list of the column data types
     */
    public final List<DataType> getDataTypes() {
        return myTabularFile.getDataTypes();
    }

    /**
     * @param colNum 0 based indexing
     * @return the data type of the column at the index
     */
    public final DataType getDataType(int colNum) {
        return myTabularFile.getDataType(colNum);
    }

    /**
     * @return the number of columns of tabular data
     */
    public final int getNumberColumns() {
        return myTabularFile.getNumberColumns();
    }

    /**
     * @return true if all cells are NUMERIC
     */
    public final boolean isAllNumeric() {
        return myTabularFile.isAllNumeric();
    }

    /**
     * @return true if all cells are TEXT
     */
    public final boolean isAllText() {
        return myTabularFile.isAllText();
    }

    /**
     * @param col the index of the column, 0 based
     * @return the data type of the column associated with this cell
     */
    public final DataType getType(int col) {
        return myTabularFile.getDataType(col);
    }

    /**
     * @param col the index of the column, 0 based
     * @return the name of the column associated with this cell
     */
    public final String getColumnName(int col) {
        return myTabularFile.getColumnName(col);
    }

    /**
     * @param name the name to look up
     * @return the index or -1 if not found
     */
    public final int getColumn(String name) {
        return myTabularFile.getColumn(name);
    }

    /**
     * @param i the index into the row (0 based)
     * @return true if the cell at location i is NUMERIC
     */
    public final boolean isNumeric(int i) {
        return myTabularFile.isNumeric(i);
    }

    /**
     * @param i i the index into the row (0 based)
     * @return true if the cell at location i is TEXT
     */
    public final boolean isText(int i) {
        return myTabularFile.isText(i);
    }

    /**
     * @param colNum the index into the row (0 based)
     * @param value  the value to set, will throw an exception of the cell is not NUMERIC
     */
    public final void setDouble(int colNum, double value) {
        if (isText(colNum)) {
            throw new IllegalStateException("The row does not contain a double value at this index");
        }
        // colNum is the actual index across all columns
        // must store the double at it's appropriate index in the storage array
        numericData[myTabularFile.getNumericStorageIndex(colNum)] = value;
    }

    /**
     * @param colNum the index into the row (0 based)
     * @param value  the value to set, will throw an exception of the cell is not TEXT
     */
    public final void setText(int colNum, String value) {
        if (isNumeric(colNum)) {
            throw new IllegalStateException("The row does not contain a text value at this index");
        }
        // colNum is the actual index across all columns
        // must store the string at it's appropriate index in the storage array
        textData[myTabularFile.getTextStorageIndex(colNum)] = value;
    }

    /**
     * @param colNum the index into the row (0 based)
     * @return the value as a double, will throw an exception if the cell is not NUMERIC
     */
    public final double getDouble(int colNum) {
        if (isText(colNum)) {
            throw new IllegalStateException("The row does not contain a double value at this index");
        }
        return numericData[myTabularFile.getNumericStorageIndex(colNum)];
    }

    /**
     * @param colNum the index into the row (0 based)
     * @return the value as a double, will throw an exception if the cell is not TEXT
     */
    public final String getText(int colNum) {
        if (isNumeric(colNum)) {
            throw new IllegalStateException("The row does not contain a text value at this index");
        }
        return textData[myTabularFile.getTextStorageIndex(colNum)];
    }

    /**
     * Sets the numeric columns according to the data in the array.
     * If the array has more elements than the number of columns, then the columns
     * are filled with first elements of the array up to the number of columns.
     * If the array has less elements than the number of columns, then only
     * the first data.length columns are set.
     *
     * @param data an array of data for the numeric rows. The array must not be null.
     * @return the number of columns that were set
     */
    public final int setNumericColumns(double[] data) {
        Objects.requireNonNull(data, "The data array was null");
        int n = Math.min(data.length, numericData.length);
        System.arraycopy(data, 0, numericData, 0, n);
        return n;
    }

    /**
     * Sets the text columns according to the data in the array.
     * If the array has more elements than the number of columns, then the columns
     * are filled with first elements of the array up to the number of columns.
     * If the array has less elements than the number of columns, then only
     * the first data.length columns are set.
     *
     * @param data an array of data for the text rows. The array must not be null.
     * @return the number of columns that were set
     */
    public final int setTextColumns(String[] data) {
        Objects.requireNonNull(data, "The data array was null");
        int n = Math.min(data.length, textData.length);
        System.arraycopy(data, 0, textData, 0, n);
        return n;
    }

    /**
     * Sets the text columns according to the data in the list.
     * If the list has more elements than the number of columns, then the columns
     * are filled with first elements of the list up to the number of columns.
     * If the list has less elements than the number of columns, then only
     * the first data.size() columns are set.
     *
     * @param data a list of data for the text rows. The list must not be null.
     * @return the number of columns that were set
     */
    public final int setTextColumns(List<String> data) {
        Objects.requireNonNull(data, "The data list was null");
        String[] stringArray = data.toArray(new String[data.size()]);
        return setTextColumns(stringArray);
    }

    /**
     * @param columnName the name of the column
     * @return the value of the column
     */
    public final double getDouble(String columnName) {
        return getDouble(getColumn(columnName));
    }

    /**
     * @param columnName the name of the column
     * @return the value of the column
     */
    public final String getText(String columnName) {
        return getText(getColumn(columnName));
    }

    /**
     * @param columnName the name of the column to set
     * @param value      the value to set
     */
    public final void setDouble(String columnName, double value) {
        setDouble(getColumn(columnName), value);
    }

    /**
     * @param columnName the name of the column to set
     * @param value      the value to set
     */
    public final void setText(String columnName, String value) {
        setText(getColumn(columnName), value);
    }

    /**
     * @return the elements of the row as Objects
     */
    public final Object[] getElements() {
        Object[] elements = new Object[getNumberColumns()];
        // need to copy elements from storage arrays to correct object location
        // go from storage arrays to elements because type is known
        for (int i = 0; i < numericData.length; i++) {
            // look up the index of the column
            int col = myTabularFile.getColumnIndexForNumeric(i);
            elements[col] = numericData[i];
        }
        for (int i = 0; i < textData.length; i++) {
            // look up the index of the column
            int col = myTabularFile.getColumnIndexForText(i);
            elements[col] = textData[i];
        }
        return elements;
    }

    /**  The row is filled with the elements. Numeric elements are saved in
     *  numeric columns in the order presented. Non-numeric elements are all converted
     *  to strings and stored in the order presented. Numeric elements are of types
     *  {Double, Long, Integer, Boolean, Float, Short, Byte}. Any other type is
     *  converted to text via toString().
     *
     *  The order and types of the elements must match the order and types associated
     *  with the columns.
     *
     * @param elements the elements to add to the row. The number of elements must
     *                 be equal to the number of columns
     */
    public final void setElements(Object[] elements) {
        Objects.requireNonNull(elements, "The array of elements was null!");
        if (elements.length != getNumberColumns()){
            throw new IllegalArgumentException("The number of elements does not equal the number of columns");
        }
        if (myTabularFile.checkTypes(elements) == false){
            throw new IllegalArgumentException("The elements do not match the types for each column");
        }
        // the type of the elements are unknown and must be tested
        // must convert numeric elements to doubles, non-numeric to strings
        for(int i=0; i< elements.length; i++){
            if (TabularFileIfc.isNumeric(elements[i])){
                // place in numeric array
                setDouble(i, TabularFileIfc.asDouble(elements[i]));
            } else {
                // not numeric, assume it is text, place in text array
                setText(i, elements[i].toString());
            }
        }
    }


    //TODO set elements with array of Objects across columns
    //TODO set and get based on Objects
}
