package examples.book.chapter3;

import jsl.utilities.random.rvariable.NormalRV;

/**
 * This example illustrates how to use the classes within the rvariable package.
 * Specifically, a Normal(mean=20, variance=4.0) random variable is
 * created and values are obtained via the getValue() method.
 */
public class Example1 {
    public static void main(String[] args) {
        // create a normal mean = 20.0, variance = 4.0 random variable
        NormalRV n = new NormalRV(20.0, 4.0);
        System.out.printf("%3s %15s %n", "n", "Values");
        // generate some values
        for (int i = 0; i < 5; i++) {
            // getValue() method returns generated values
            double x = n.getValue();
            System.out.printf("%3d %15f %n", i+1, x);
        }
    }
}
