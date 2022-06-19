package jsl.controls.testing;


import jsl.controls.ControlType;
import jsl.controls.JSLControl;

public interface TestIfc {
    // define a numeric setter at the interface level

    @JSLControl(
            type = ControlType.DOUBLE,
            comment = "Control defined at TestIfc level has NO EFFECT")
    public void setMyIfcDouble(double v);

    @JSLControl(
            type = ControlType.BOOLEAN,
            comment = "Control defined at TestIfc level has NO EFFECT")
    public void setMyIfcBoolean(boolean b);
}
