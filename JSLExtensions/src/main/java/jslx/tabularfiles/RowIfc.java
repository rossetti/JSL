package jslx.tabularfiles;

import java.util.LinkedHashMap;
import java.util.List;

public interface RowIfc {
    /**
     * @return the number of bytes stored in the row
     */
    int getBytes();

    /**
     * @return the total number of numeric columns
     */
    int getNumNumericColumns();

    /**
     * @return the total number of text columns
     */
    int getNumTextColumns();

    /**
     * @return the map of columns associated with this row
     */
    LinkedHashMap<String, DataType> getColumnTypes();

    /**
     * @return an ordered list of the column names for the row
     */
    List<String> getColumnNames();

    /**
     * @return an ordered list of the column data types
     */
    List<DataType> getDataTypes();

    /**
     * @param colNum 0 based indexing
     * @return the data type of the column at the index
     */
    DataType getDataType(int colNum);

    /**
     * @return the number of columns of tabular data
     */
    int getNumberColumns();

    /**
     * @return true if all cells are NUMERIC
     */
    boolean isAllNumeric();

    /**
     * @return true if all cells are TEXT
     */
    boolean isAllText();

    /**
     * @param col the index of the column, 0 based
     * @return the data type of the column associated with this cell
     */
    DataType getType(int col);

    /**
     * @param col the index of the column, 0 based
     * @return the name of the column associated with this cell
     */
    String getColumnName(int col);

    /**
     * @param name the name to look up
     * @return the index or -1 if not found
     */
    int getColumn(String name);

    /**
     * @param i the index into the row (0 based)
     * @return true if the cell at location i is NUMERIC
     */
    boolean isNumeric(int i);

    /**
     * @param i i the index into the row (0 based)
     * @return true if the cell at location i is TEXT
     */
    boolean isText(int i);
}
