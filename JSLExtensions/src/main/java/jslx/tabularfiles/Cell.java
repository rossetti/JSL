package jslx.tabularfiles;

abstract public class Cell {

    private final DataType dataType;

    public Cell(DataType dataType) {
        this.dataType = dataType;
    }

    public final DataType getDataType() {
        return dataType;
    }
}
