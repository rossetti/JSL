package jslx.tabularfiles;

public class NumericCell extends Cell {

    private double value;

    /**
     *  A numeric cell with value Double.NaN
     */
    public NumericCell(){
        this(Double.NaN);
    }

    public NumericCell(Double value) {
        super(DataType.NUMERIC);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setValue(boolean value){
        if (value){
            this.value = 1.0;
        } else {
            this.value = 0.0;
        }
    }

}
