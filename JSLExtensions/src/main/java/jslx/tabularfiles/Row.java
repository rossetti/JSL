package jslx.tabularfiles;

import java.util.*;

/**
 * An abstraction for a row within a tabular file.
 * The access to the columns is 0-based.  Why? Because most if not all of java's
 * data containers (arrays, lists, etc.) are 0-based.  The the first column has index 0,
 * 2nd column has index 1, etc.
 */
public class Row implements RowGetterIfc, RowSetterIfc, RowIfc {

    private final TabularFile myTabularFile;
    private final String[] textData;
    private final double[] numericData;
    private long myRowNum;

    public Row(TabularFile tabularFile) {
        Objects.requireNonNull(tabularFile, "The tabular file was null");
        myTabularFile = tabularFile;
        textData = new String[tabularFile.getNumTextColumns()];
        numericData = new double[tabularFile.getNumNumericColumns()];
        Arrays.fill(numericData, Double.NaN);
    }

    /**
     * @return the number of this row if it was returned from a tabular file
     */
    public final long getRowNum() {
        return myRowNum;
    }

    final void setRowNum(long rowNum) {
        myRowNum = rowNum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        int n = getNumberColumns();
        formatter.format("|%-20d|", myRowNum);
        for (int i = 0; i < n; i++) {
            if (getDataType(i) == DataType.NUMERIC) {
                formatter.format("%-20f|", getNumeric(i));
            } else {
                // must be string
                formatter.format("%-20s|", getText(i));
            }
        }
        return sb.toString();
    }

    @Override
    public int getBytes() {
        int n = numericData.length * 8;
        for (String s : textData) {
            if (s != null) {
                n = n + s.getBytes().length;
            }
        }
        return n;
    }

    @Override
    public final int getNumNumericColumns() {
        return myTabularFile.getNumNumericColumns();
    }

    @Override
    public final int getNumTextColumns() {
        return myTabularFile.getNumTextColumns();
    }

    @Override
    public final LinkedHashMap<String, DataType> getColumnTypes() {
        return myTabularFile.getColumnTypes();
    }

    @Override
    public final List<String> getColumnNames() {
        return myTabularFile.getColumnNames();
    }

    @Override
    public final List<DataType> getDataTypes() {
        return myTabularFile.getDataTypes();
    }

    @Override
    public final DataType getDataType(int colNum) {
        return myTabularFile.getDataType(colNum);
    }

    @Override
    public final int getNumberColumns() {
        return myTabularFile.getNumberColumns();
    }

    @Override
    public final boolean isAllNumeric() {
        return myTabularFile.isAllNumeric();
    }

    @Override
    public final boolean isAllText() {
        return myTabularFile.isAllText();
    }

    @Override
    public final DataType getType(int col) {
        return myTabularFile.getDataType(col);
    }

    @Override
    public final String getColumnName(int col) {
        return myTabularFile.getColumnName(col);
    }

    @Override
    public final int getColumn(String name) {
        return myTabularFile.getColumn(name);
    }

    @Override
    public final boolean isNumeric(int i) {
        return myTabularFile.isNumeric(i);
    }

    @Override
    public final boolean isText(int i) {
        return myTabularFile.isText(i);
    }

    @Override
    public final void setNumeric(int colNum, double value) {
        if (isText(colNum)) {
            throw new IllegalStateException("The row does not contain a double value at this index");
        }
        // colNum is the actual index across all columns
        // must store the double at it's appropriate index in the storage array
        numericData[myTabularFile.getNumericStorageIndex(colNum)] = value;
    }

    @Override
    public final void setText(int colNum, String value) {
        if (isNumeric(colNum)) {
            throw new IllegalStateException("The row does not contain a text value at this index");
        }
        // colNum is the actual index across all columns
        // must store the string at it's appropriate index in the storage array
        textData[myTabularFile.getTextStorageIndex(colNum)] = value;
    }

    @Override
    public final double getNumeric(int colNum) {
        if (isText(colNum)) {
            throw new IllegalStateException("The row does not contain a double value at this index");
        }
        return numericData[myTabularFile.getNumericStorageIndex(colNum)];
    }

    @Override
    public final double[] getNumeric() {
        return Arrays.copyOf(numericData, numericData.length);
    }

    @Override
    public final String getText(int colNum) {
        if (isNumeric(colNum)) {
            throw new IllegalStateException("The row does not contain a text value at this index");
        }
        return textData[myTabularFile.getTextStorageIndex(colNum)];
    }

    @Override
    public final String[] getText() {
        return Arrays.copyOf(textData, textData.length);
    }

    @Override
    public final int setNumeric(double[] data) {
        Objects.requireNonNull(data, "The data array was null");
        int n = Math.min(data.length, numericData.length);
        System.arraycopy(data, 0, numericData, 0, n);
        return n;
    }

    @Override
    public final int setText(String[] data) {
        Objects.requireNonNull(data, "The data array was null");
        int n = Math.min(data.length, textData.length);
        System.arraycopy(data, 0, textData, 0, n);
        return n;
    }

    @Override
    public final int setText(List<String> data) {
        Objects.requireNonNull(data, "The data list was null");
        String[] stringArray = data.toArray(new String[data.size()]);
        return setText(stringArray);
    }

    @Override
    public final double getNumeric(String columnName) {
        return getNumeric(getColumn(columnName));
    }

    @Override
    public final String getText(String columnName) {
        return getText(getColumn(columnName));
    }

    @Override
    public final void setNumeric(String columnName, double value) {
        setNumeric(getColumn(columnName), value);
    }

    @Override
    public final void setText(String columnName, String value) {
        setText(getColumn(columnName), value);
    }

    @Override
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

    @Override
    public final Object getElement(int colNum) {
        if (getDataType(colNum) == DataType.NUMERIC) {
            return getNumeric(colNum);
        } else {
            return getText(colNum);
        }
    }

    @Override
    public final void setElements(Object[] elements) {
        Objects.requireNonNull(elements, "The array of elements was null!");
        if (elements.length != getNumberColumns()) {
            throw new IllegalArgumentException("The number of elements does not equal the number of columns");
        }
        if (myTabularFile.checkTypes(elements) == false) {
            throw new IllegalArgumentException("The elements do not match the types for each column");
        }
        // the type of the elements are unknown and must be tested
        // must convert numeric elements to doubles, non-numeric to strings
        for (int i = 0; i < elements.length; i++) {
            setElement(i, elements[i]);
        }
    }

    @Override
    public final void setElement(int colNum, Object element) {
        if (TabularFile.isNumeric(element)) {
            setNumeric(colNum, TabularFile.asDouble(element));
        } else {
            // not NUMERIC
            if (element == null) {
                setText(colNum, null);
            } else {
                setText(colNum, element.toString());
            }
        }
    }

}
