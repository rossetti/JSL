package jslx.tabularfiles;

/**
 *  An abstraction for getting information and data from a row within a tabular file.
 *  The access to the columns is 0-based.  Why? Because most if not all of java's
 *  data containers (arrays, lists, etc.) are 0-based.  The the first column has index 0,
 *  2nd column has index 1, etc.
 */
public interface RowGetterIfc extends RowIfc {
    /**
     * @param colNum the index into the row (0 based)
     * @return the value as a double, will throw an exception if the cell is not NUMERIC
     */
    double getNumeric(int colNum);

    /**
     * @param columnName the name of the column
     * @return the value of the column
     */
    double getNumeric(String columnName);

    /**
     *
     * @return the numeric columns as an array
     */
    double[] getNumeric();

    /**
     * @param colNum the index into the row (0 based)
     * @return the value as a double, will throw an exception if the cell is not TEXT
     */
    String getText(int colNum);

    /**
     *
     * @return the text columns in order of appearance as an array
     */
    String[] getText();

    /**
     * @param columnName the name of the column
     * @return the value of the column
     */
    String getText(String columnName);

    /**
     * @return the elements of the row as Objects
     */
    Object[] getElements();

    /**
     * @param colNum the column number
     * @return an object representation of the element at the column
     */
    Object getElement(int colNum);

    /**
     *
     * @return the row as an array of strings
     */
    String[] asStringArray();

    /**
     *
     * @return the row as comma separated values. The row does not contain a line separator.
     */
    String toCSV();
}
