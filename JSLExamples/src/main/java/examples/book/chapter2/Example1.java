package examples.book.chapter2;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rng.RNStreamProvider;

/**
 * This example illustrates how to make a stream provider, get streams
 * from the provider, and use the streams to generate pseudo-random
 * numbers.
 */
public class Example1 {
    public static void main(String[] args) {
        // make a provider for creating streams
        RNStreamProvider p1 = new RNStreamProvider();
        // get the first stream from the provider
        RNStreamIfc p1s1 = p1.nextRNStream();
        // make another provider, the providers are identical
        RNStreamProvider p2 = new RNStreamProvider();
        // thus the first streams returned are identical
        RNStreamIfc p2s1 = p2.nextRNStream();
        System.out.printf("%3s %15s %15s %n", "n", "p1s1", "p2s2");
        for (int i = 0; i < 10; i++) {
            System.out.printf("%3d %15f %15f %n", i + 1, p1s1.randU01(), p2s1.randU01());
        }
    }
}
