package jslx.tabularfiles;

import java.util.List;

/**
 *  An abstraction for getting information and setting data for a row within a tabular file.
 *  The access to the columns is 0-based.  Why? Because most if not all of java's
 *  data containers (arrays, lists, etc.) are 0-based.  The the first column has index 0,
 *  2nd column has index 1, etc.
 */
public interface RowSetterIfc extends RowIfc {
    /**
     * @param colNum the index into the row (0 based)
     * @param value  the value to set, will throw an exception of the cell is not NUMERIC
     */
    void setNumeric(int colNum, double value);

    /**
     * @param colNum the index into the row (0 based)
     * @param value  the value to set, will throw an exception of the cell is not NUMERIC
     */
    default void setNumeric(int colNum, boolean value){
        if (value){
            setNumeric(colNum, 1.0);
        } else {
            setNumeric(colNum, 0.0);
        }
    }

    /**
     * @param colNum the index into the row (0 based)
     * @param value  the value to set, will throw an exception of the cell is not TEXT
     */
    void setText(int colNum, String value);

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
    int setNumeric(double[] data);

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
    int setText(String[] data);

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
    int setText(List<String> data);

    /**
     * @param columnName the name of the column to set
     * @param value      the value to set
     */
    void setNumeric(String columnName, double value);

    /**
     * @param columnName the name of the column to set
     * @param value      the value to set
     */
    void setText(String columnName, String value);

    /**
     * The row is filled with the elements. Numeric elements are saved in
     * numeric columns in the order presented. Non-numeric elements are all converted
     * to strings and stored in the order presented. Numeric elements are of types
     * {Double, Long, Integer, Boolean, Float, Short, Byte}. Any other type is
     * converted to text via toString().
     * <p>
     * The order and types of the elements must match the order and types associated
     * with the columns.
     *
     * @param elements the elements to add to the row. The number of elements must
     *                 be equal to the number of columns
     */
    void setElements(Object[] elements);

    /**
     * @param colNum  the column number to set
     * @param element the element to set
     */
    void setElement(int colNum, Object element);
}
