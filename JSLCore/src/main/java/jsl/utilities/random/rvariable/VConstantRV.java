package jsl.utilities.random.rvariable;

public class VConstantRV extends ConstantRV {

    private double myPreviousValue;

    public VConstantRV(double value) {
        super(value);
    }

    /**
     *
     * @param value the value to use for the degenerate distribution
     */
    public void setValue(double value) {
        myPreviousValue = myValue;
        myValue = value;}

}
