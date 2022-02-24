package examples.book.chapter4;

import jsl.utilities.random.rvariable.BinomialRV;
import jsl.utilities.statistic.IntegerFrequency;

/**
 * This example illustrates how to create an instance of an IntegerFrequency
 * class in order to tabulate the frequency of occurrence of integers within
 * a sample.
 */
public class Example3 {
    public static void main(String[] args) {
        IntegerFrequency f = new IntegerFrequency("Frequency Demo");
        BinomialRV bn = new BinomialRV(0.5, 100);
        double[] sample = bn.sample(10000);
        f.collect(sample);
        System.out.println(f);
    }
}
