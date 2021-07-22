package jslx.tabularfiles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class Row {

    private final TabularFile myTabularFile;
    private final List<Cell> myCells;
    //TODO consider getting rid of cells and just using arrays double[] and String[]
    // for the data in a row because the number is known in advance

    public Row(TabularFile tabularFile) {
        Objects.requireNonNull(tabularFile, "The tabular file was null");
        myTabularFile = tabularFile;
        List<DataType> dataTypes = myTabularFile.getDataTypes();
        myCells = new ArrayList<>();
        for(DataType type: dataTypes){
            if (type == DataType.NUMERIC){
                myCells.add(new NumericCell());
            } else {
                myCells.add(new TextCell());
            }
        }
    }

    /**
     *
     * @return the total number of numeric columns
     */
    public final int getNumNumericColumns() {
        return myTabularFile.getNumNumericColumns();
    }

    /**
     *
     * @return the total number of text columns
     */
    public final int getNumTextColumns() {
        return myTabularFile.getNumTextColumns();
    }

    /**
     *
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
     *
     * @param colNum 0 based indexing
     * @return the data type of the column at the index
     */
    public final DataType getDataType(int colNum) {
        return myTabularFile.getDataType(colNum);
    }

    /**
     * @return the number of columns of tabular data
     */
    public final int numberColumns() {
        return myTabularFile.numberColumns();
    }

    /**
     *
     * @return true if all cells are NUMERIC
     */
    public final boolean isAllNumeric(){
        return myTabularFile.isAllNumeric();
    }

    /**
     *
     * @return true if all cells are TEXT
     */
    public final boolean isAllText(){
        return myTabularFile.isAllText();
    }

    /**
     *
     * @param col the index of the column, 0 based
     * @return the data type of the column associated with this cell
     */
    public final DataType getType(int col){
        return myTabularFile.getDataType(col);
    }

    /**
     *
     * @param col the index of the column, 0 based
     * @return the name of the column associated with this cell
     */
    public final String getColumnName(int col){
        return myTabularFile.getColumnName(col);
    }

    /**
     *
     * @param name the name to look up
     * @return the index or -1 if not found
     */
    public final int getColumn(String name){
        return myTabularFile.getColumn(name);
    }

    /**
     *
     * @param i the index into the row (0 based)
     * @return true if the cell at location i is NUMERIC
     */
    public final boolean isNumeric(int i){
        return myTabularFile.isNumeric(i);
    }

    /**
     *
     * @param i i the index into the row (0 based)
     * @return true if the cell at location i is TEXT
     */
    public final boolean isText(int i){
        return myTabularFile.isText(i);
    }

    /**
     *
     * @param i i the index into the row (0 based)
     * @param value the value to set, will throw an exception of the cell is not NUMERIC
     */
    public final void setValue(int i, double value){
        if (isText(i)){
            throw new IllegalStateException("The cell does not contain a double value");
        }
        NumericCell c = (NumericCell) myCells.get(i);;
        c.setValue(value);
    }

    /**
     *
     * @param i i the index into the row (0 based)
     * @param value the value to set, will throw an exception of the cell is not TEXT
     */
    public final void setValue(int i, String value){
        if (isNumeric(i)){
            throw new IllegalStateException("The cell does not contain a text value");
        }
        TextCell c = (TextCell) myCells.get(i);
        c.setValue(value);
    }

    /**
     *
     * @param i i the index into the row (0 based)
     * @return the value as a double, will throw an exception if the cell is not NUMERIC
     */
    public final double getDouble(int i){
        if (isText(i)){
            throw new IllegalStateException("The cell does not contain a double value");
        }
        NumericCell c = (NumericCell) myCells.get(i);
        return c.getValue();
    }

    /**
     *
     * @param i i the index into the row (0 based)
     * @return the value as a double, will throw an exception if the cell is not TEXT
     */
    public final String getText(int i){
        if (isNumeric(i)){
            throw new IllegalStateException("The cell does not contain a text value");
        }
        TextCell c = (TextCell) myCells.get(i);
        return c.getValue();
    }

    /** Sets the numeric cells according to the data in the array.
     *  The number of numeric cells must match the size of the array.
     *  The assignment occurs in column order until all cells are assigned.
     *
     * @param data an array of data for the numeric rows
     */
    public final void setNumericCells(double[] data){
        Objects.requireNonNull(data, "The data array was null");
        if (data.length != getNumNumericColumns()){
            throw new IllegalArgumentException("The array did not have the correct number of numeric elements");
        }
        int i = 0;
        for(Cell cell: myCells){
            if (cell.getDataType() == DataType.NUMERIC){
                NumericCell nc = (NumericCell) cell;
                nc.setValue(data[i]);
                i++;
            }
        }
    }

    /** Sets the text cells according to the data in the array.
     *  The number of text cells must match the size of the array.
     *  The assignment occurs in column order until all cells are assigned
     *
     * @param data an array of data for the numeric rows
     */
    public final void setTextCells(String[] data){
        Objects.requireNonNull(data, "The data array was null");
        if (data.length != getNumTextColumns()){
            throw new IllegalArgumentException("The array did not have the correct number of text elements");
        }
        int i = 0;
        for(Cell cell: myCells){
            if (cell.getDataType() == DataType.TEXT){
                TextCell nc = (TextCell) cell;
                nc.setValue(data[i]);
                i++;
            }
        }
    }

    //TODO add setters and getters based on column name
    //TODO set elements with array of Objects across columns
    //TODO set and get based on Objects
}
