package examples.book.chapter2;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

/**
 * This example illustrates how to clone an instance of a stream.
 * This will produce a new stream that has the same underlying state
 * as the current stream and thus will produce exactly the same
 * sequence of pseudo-random numbers. This is one approach
 * for implementing common random numbers.
 */
public class Example3 {
    public static void main(String[] args) {
        // get the default stream
        RNStreamIfc s = JSLRandom.getDefaultRNStream();
        // make a clone of the stream
        RNStreamIfc clone = s.newInstance();
        System.out.printf("%3s %15s %15s %n", "n", "U", "U again");
        for (int i = 0; i < 3; i++) {
            System.out.printf("%3d %15f %15f %n", i + 1, s.randU01(), clone.randU01());
        }
    }
}
