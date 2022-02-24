package examples.book.chapter2;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

/**
 *  This example illustrates how to reset a stream back to its
 *  starting point in its sequence and thus reproduce the same
 *  sequence of pseudo-random numbers. This is an alternative
 *  method for performing common random numbers.
 */
public class Example4 {
    public static void main(String[] args) {
        RNStreamIfc s = JSLRandom.getDefaultRNStream();
        // generate regular
        System.out.printf("%3s %15s %n", "n", "U");
        for (int i = 0; i < 3; i++) {
            double u = s.randU01();
            System.out.printf("%3d %15f %n", i+1, u);
        }
        // reset the stream and generate again
        s.resetStartStream();
        System.out.println();
        System.out.printf("%3s %15s %n", "n", "U again");
        for (int i = 0; i < 3; i++) {
            double u = s.randU01();
            System.out.printf("%3d %15f %n", i+1, u);
        }
    }
}
