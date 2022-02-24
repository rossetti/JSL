package examples.book.chapter2;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

/**
 *  This example illustrates how to use JSLRandom to get and use streams.
 *  The example illustrates how to advance a stream to it's next sub-stream,
 *  how to reset the stream back to its beginning.
 */
public class Example2 {
    public static void main(String[] args) {
        RNStreamIfc s1 = JSLRandom.getDefaultRNStream();
        System.out.println("Default stream is stream 1");
        System.out.println("Generate 3 random numbers");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s1.randU01());
        }
        s1.advanceToNextSubstream();
        System.out.println("Advance to next sub-stream and get some more random numbers");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s1.randU01());
        }
        System.out.println("Notice that they are different from the first 3.");
        s1.resetStartStream();
        System.out.println("Reset the stream to the beginning of its sequence");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s1.randU01());
        }
        System.out.println("Notice that they are the same as the first 3.");
        System.out.println("Get another random number stream");
        RNStreamIfc s2 = JSLRandom.nextRNStream();
        System.out.println("2nd stream");
        for (int i = 1; i <= 3; i++) {
            System.out.println("u = " + s2.randU01());
        }
        System.out.println("Notice that they are different from the first 3.");
    }
}
