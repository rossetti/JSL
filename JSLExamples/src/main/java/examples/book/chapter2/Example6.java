package examples.book.chapter2;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

/**
 * This example illustrates another approach to producing
 * antithetic pseudo-random numbers using the same stream.
 * This approach resets the stream to its starting point and
 * then sets the anthitheic option to true.
 * Before the reset, the stream produces u1, u2, u3,...
 * After the reset and turning the antithetic option on,
 * the stream produces 1-u1, 1-u2, 1-u3, ...
 */
public class Example6 {
    public static void main(String[] args) {
        RNStreamIfc s = JSLRandom.getDefaultRNStream();
        s.resetStartStream();
        // generate regular
        System.out.printf("%3s %15s %n", "n", "U");
        for (int i = 0; i < 5; i++) {
            double u = s.randU01();
            System.out.printf("%3d %15f %n", i + 1, u);
        }
        // generate antithetic
        s.resetStartStream();
        s.setAntitheticOption(true);
        System.out.println();
        System.out.printf("%3s %15s %n", "n", "1-U");
        for (int i = 0; i < 5; i++) {
            double u = s.randU01();
            System.out.printf("%3d %15f %n", i + 1, u);
        }
    }
}
