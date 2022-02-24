package examples.book.chapter4;

import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.Histogram;

/**
 * This example illustrates how to make an instance of a Histogram
 * and use it to collect statistics on a randomly generated sample.
 */
public class Example2 {
    public static void main(String[] args) {
        ExponentialRV d = new ExponentialRV(2);
        Histogram h = new Histogram(0.0, 20, 0.1);
        for (int i = 1; i <= 100; ++i) {
            h.collect(d.getValue());
        }
        System.out.println(h);
    }
}
