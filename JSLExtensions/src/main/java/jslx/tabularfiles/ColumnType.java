package jslx.tabularfiles;

import java.util.Objects;

/**
 * Describes a type of column within the tabular file.  There are only two types: numeric and text
 * The numeric type should be used for numeric data (float, double, long, int, etc.). In addition,
 * use the numeric type for boolean values, which are stored 1.0 = true, 0.0 = false).  The text type
 * should be used for strings and date/time data.  Date/time data is saved
 * as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").  If you need more type complexity, you should use
 * a database.
 */
public class ColumnType {

    private final String name;
    private final DataType dataType;

    public ColumnType(String name, DataType dataType) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnType columnType = (ColumnType) o;

        return getName().equals(columnType.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
