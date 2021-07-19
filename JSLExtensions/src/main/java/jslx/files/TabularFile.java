package jslx.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TabularFile {
    //TODO  column names for a given file must be unique
    //TODO use a builder pattern to define and add the columns

    public static class Column {

        public enum DataType {
            LONG, INTEGER, DOUBLE, TEXT, BOOLEAN
        }

        private final String name;
        private final DataType dataType;

        public Column(String name, DataType dataType) {
            Objects.requireNonNull(name, "The name must not be null");
            Objects.requireNonNull(dataType, "The data type must not be null");
            this.name = name;
            this.dataType = dataType;
        }

        public final String getName() {
            return name;
        }

        public final DataType getDataType() {
            return dataType;
        }
    }

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
        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < numColumns; i++) {
            columns.add(column("C" + (i + 1), dataType));
        }
        return columns;
    }
}
