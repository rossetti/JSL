package jsl.controls.work.testing;


import jsl.controls.work.Control;
import jsl.controls.work.JSLControl;

public interface TestIfc {
    // define a numeric setter at the interface level

    @JSLControl(
            type = Control.ControlType.DOUBLE,
            comment = "Control defined at TestIfc level has NO EFFECT")
    public void setMyIfcDouble(double v);

    @JSLControl(
            type = Control.ControlType.BOOLEAN,
            comment = "Control defined at TestIfc level has NO EFFECT")
    public void setMyIfcBoolean(boolean b);
}
