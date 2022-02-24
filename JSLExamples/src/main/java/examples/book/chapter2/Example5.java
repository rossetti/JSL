package examples.book.chapter2;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

/**
 * This example illustrates how to create a new stream from
 * an existing stream such that the new stream produces the
 * antithetic pseudo-random numbers of the first stream.
 * That is if stream A produces u1, u2, .., then the
 * antithetic of stream A produces 1-u1, 1-u2, ....
 */
public class Example5 {
    public static void main(String[] args) {
        // get the default stream
        RNStreamIfc s = JSLRandom.getDefaultRNStream();
        // make its antithetic version
        RNStreamIfc as = s.newAntitheticInstance();
        System.out.printf("%3s %15s %15s %15s %n", "n", "U", "1-U", "sum");
        for (int i = 0; i < 5; i++) {
            double u = s.randU01();
            double au = as.randU01();
            System.out.printf("%3d %15f %15f %15f %n", i + 1, u, au, (u + au));
        }
    }
}
