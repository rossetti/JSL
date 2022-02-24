package examples.book.chapter3;

import static jsl.utilities.random.rvariable.JSLRandom.*;

/**
 * This example illustrates that the user can use the static methods
 * of JSLRandom to generate from any of the defined random variables
 * as simple function calls.
 */
public class Example5 {
    public static void main(String[] args) {
        // use import static jsl.utilities.random.rvariable.JSLRandom.*;
        // at the top of your java file
        double v = rUniform(10.0, 15.0); // generate a U(10, 15) value
        double x = rNormal(5.0, 2.0); // generate a Normal(mu=5.0, var= 2.0) value
        double n = rPoisson(4.0); //generate from a Poisson(mu=4.0) value
        System.out.printf("v = %f, x = %f, n = %f %n", v, x, n);
    }
}
