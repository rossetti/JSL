package examples.book.chapter3;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.rvariable.NormalRV;

/**
 * This example illustrates how to use the classes within the rvariable package.
 * Specifically, a Normal(mean=20, variance=4.0) random variable is
 * created and values are obtained via the getValue() method.
 * <p>
 * In this case, stream 3 is used to generate from the random variable.
 */
public class Example3 {
    public static void main(String[] args) {
        // get stream 3
        RNStreamIfc stream = JSLRandom.rnStream(3);
        // create a normal mean = 20.0, variance = 4.0, with the stream
        NormalRV n = new NormalRV(20.0, 4.0, stream);
        System.out.printf("%3s %15s %n", "n", "Values");
        for (int i = 0; i < 5; i++) {
            // getValue() method returns generated values
            double x = n.getValue();
            System.out.printf("%3d %15f %n", i + 1, x);
        }
    }
}
