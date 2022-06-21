package jsl.controls.testing;

import jsl.controls.ControlType;
import jsl.controls.Controls;
import jsl.controls.JSLControl;
import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;

/**
 * testUtilities annotation control inheritance.
 * - initailizes an object of class TestControlInheritanceSub
 * which is a subclass of Test (1 control)
 * which is a subclass of TestAbstract (1 control)
 * which implements TestIfc (2 controls)
 * <p>
 * However the annotated setters in testControlInherianceIfc (setMyIfcDouble) are
 * overridden as required in TestAbstract/Test but unless the necessary annotations
 * are also applied at the override, these are not be found via reflection
 */

public class Test extends TestAbstract {
    private Long mySubLong = 0L;

    // required constructor
    public Test(ModelElement parent) {
        super(parent);
    }

    @JSLControl(
            type = ControlType.LONG,
            lowerBound = 100,
            upperBound = 200,
            comment = "Control defined at Test level" +
                    ", the name is inferred from the setter name 'setMySubLong'" +
                    ", lower and upper bounds set"
    )
    public void setMySubLong(Long l) {
        mySubLong = l;
    }

    @JSLControl(
            type = ControlType.FLOAT
    )
    public void setLeadTime(float t) {
        float myLeadTime = t;
    }


    @Override
    @JSLControl(
            type = ControlType.BOOLEAN,
            comment = "Control required by the interface 'TestIfc' but " +
                    "an additional annotation applied to the override in the 'Test' class"
    )
    public void setMyIfcBoolean(boolean b) {
    }


    public static void main(String args[]) {
        Simulation sim = new Simulation();
        Model mod = sim.getModel();

        // add Test as a ModelElement
        new Test(mod);

        Controls cs = new Controls(mod);

        System.out.println(cs.toControlsAsDoublesJSON());

        // create a new controller
//        ExperimentRunner cs = new ExperimentRunner(mod);
        // all control-setter values should be NULL as values are not loaded
//        cs.extractControls();

        System.out.println("\nSearching for annotated controls in a 'Test' model element");
        System.out.println("Note that the 'Test' class extends the abstract 'TestAbstract' class " +
                "which in turn implements the interface 'TestIfc'");
        System.out.println("'TestIfc' -> 'TestAbstract' -> 'Test'");

        System.out.println("\nControls found (should be 4)");
        System.out.println(cs.getControlRecordsAsString());

        System.out.println(" Key Learning - the 2 annotated-controls in TestIfc are " +
                "overridden as required in TestAbstract/Test but only one " +
                "has an annotation with the over-ride and without this, " +
                "they cannot be found via reflection.");
        System.out.println("\n--------------------------------------------------------");
        System.out.println("Annotations are inherited through classes but ");
        System.out.println("\nADDING CONTROL-ANNOTATIONS TO INTERFACES IS POINTLESS");
    }

}
