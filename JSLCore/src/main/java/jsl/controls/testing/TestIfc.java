package jsl.controls.testing;


import jsl.controls.BooleanControl;
import jsl.controls.NumericControl;

public interface TestIfc {
    // define a numeric setter at the interface level

    @NumericControl(comment = "Control defined at TestIfc level has NO EFFECT")
    public void setMyIfcDouble(double v);

    @BooleanControl(comment = "Control defined at TestIfc level has NO EFFECT")
    public void setMyIfcBoolean(boolean b);
}
