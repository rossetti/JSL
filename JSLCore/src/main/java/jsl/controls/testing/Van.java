package jsl.controls.testing;

import jsl.controls.ControlType;
import jsl.controls.JSLControl;
import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;

/**
 * "Van" class with various vehicle attributes to Test annotationControl
 * - extraction from code (via relfection),
 *  - put and get of control values
 */
public class Van extends ModelElement {
    // fields
    private Boolean stickShift = false;
    private int numSeats = 3;
    private short wheels = 4;
    private double price = 1.2345;

    // constructor
    public Van(ModelElement parent) {
        super(parent);
    }

    @JSLControl(
            type = ControlType.BOOLEAN
    )
    public void setStickShift(Boolean stickShift) {
        this.stickShift = stickShift;
    }

    // numeric control setter with bounds, an alias and a comment
    @JSLControl(
            type = ControlType.INTEGER,
            name = "numberOfSeats", lowerBound = 1, upperBound = 18, comment = "0 seats == autonomous driving ?"
    )
    public void setNumSeats(Integer n) {
        numSeats = (n==null) ? numSeats:n;
    }

    public int getNumSeats() {
        return numSeats;
    }

    // numeric control setter with bounds
    @JSLControl(
            type = ControlType.SHORT,
            lowerBound = 3,
            upperBound = 8
    )
    public void setNumWheels(short wheels) {
        this.wheels = wheels;
    }

    public short getNumWheels() {
        return wheels;
    }

    // numeric control setter with all defaults
    @JSLControl(type = ControlType.DOUBLE)
    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }


    // testUtilities
    public static void main(String args[]) throws Exception {

        // define a model to attach ModelELements to
        Model mod = new Simulation().getModel();
        // and the ExperimentRunner.ExperimentRunner for this model
//        ExperimentRunner myExperimentRunner = new ExperimentRunner(mod);

        // make a few Vans (and add them to Model)
        int k = 6;
        Van[] vans = new Van[k];
        for (int i = 0; i < k; i++) {
            vans[i] = new Van(mod);
        }

        System.out.println("\nModelElements");
        System.out.println("-------------------------");
        mod.getModelElements().forEach((me) -> {
            System.out.println(me.getName());
        });

//        System.out.println("\nControls we could set values for");
//        System.out.println("-------------------------");
//        myExperimentRunner.extractControls();
//        System.out.println(myExperimentRunner.getControlDetails().toString());
//
//        // get some Random Variables to set seats, wheels, price etc.
//        // ***  and whether we provide an over-ride at all ***
//        RandomIfc seats = new UniformRV(1, 12);
//        RandomIfc wheels = new UniformRV(3, 8);
//        RandomIfc price = new NormalRV(100, 30);
//        RandomIfc p = new BernoulliRV(.5);
//
//        //Creating some NumericControlType Values...
//        Experiment.Controls pm = new Experiment.Controls();
//        for(Van v:vans){
//                String nm = v.getName();
//                // don't update everything
//                if (p.getValue() == 1)
//                    pm.put(nm + ".numberOfSeats", seats.getValue());
//                if (p.getValue() == 1)
//                    pm.put(nm + ".numWheels", wheels.getValue());
//                if (p.getValue() == 1)
//                    pm.put(nm + ".price", pow(price.getValue(), 2));
//                if (p.getValue() == 1) pm.put(nm + ".stickShift", p.getValue());
//        }
//
//        System.out.println("\nNumericControlType Values we want to over-ride");
//        System.out.println("-------------------------");
//        System.out.println(pm.toString());
//
//        // set NumericControlType Values
//        System.out.println("\nSetting NumericControlType Values");
//        System.out.println("-------------------------");
//        myExperimentRunner.cacheControlValues(pm);
//
//        System.out.println("\nNumericControlType values (updated where non-null)");
//        System.out.println("-------------------------");
//        System.out.println(myExperimentRunner.getControlValues());
    }

}
