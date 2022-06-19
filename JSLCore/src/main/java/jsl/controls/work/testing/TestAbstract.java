package jsl.controls.work.testing;

import jsl.controls.work.Control;
import jsl.controls.work.JSLControl;
import jsl.simulation.ModelElement;

public abstract class TestAbstract
        extends ModelElement
        implements TestIfc {
    private int myAbstractInt = 0;
    private double myAbstractDouble = 0.0;

    // required constructor
    public TestAbstract(ModelElement parent) {
        super(parent);
    }

    @JSLControl(
            type = Control.ControlType.INTEGER,
            name = "AbstractInt",
            comment = "Control defined at TestAbstract level" +
                    ", no lower or upper bounds defined; " +
                    ", name overide as 'AbstractInt' for a setter named 'setInt'")
    public void setInt(int i){
        myAbstractInt = i;
    }

    /**
     * note that this required override method has no @NumericControl and WILL NOT inherit
     * that behavior from the interface this class implements.
     * @param v
     */
    @Override
    public void setMyIfcDouble(double v) {
        myAbstractDouble = v;
    }
}
