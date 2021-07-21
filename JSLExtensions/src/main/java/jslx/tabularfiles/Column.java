package jslx.tabularfiles;

import java.util.Objects;

/**
 * Represents a column within the tabular file.
 */
public class Column {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        return getName().equals(column.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
