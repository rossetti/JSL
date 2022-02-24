package examples.book.chapter3;

import jsl.utilities.random.rvariable.DEmpiricalRV;

/**
 * This example illustrates how to use the classes within the rvariable package.
 * Specifically, a discrete empirical random variable is
 * created and values are obtained via the getValue() method. A discrete
 * empirical random variable requires a set of values and a CDF over the
 * values.
 */
public class Example4 {
    public static void main(String[] args) {
        // values is the set of possible values
        double[] values = {1.0, 2.0, 3.0, 4.0};
        // cdf is the cumulative distribution function over the values
        double[] cdf = {1.0 / 6.0, 3.0 / 6.0, 5.0 / 6.0, 1.0};
        //create a discrete empirical random variable
        DEmpiricalRV n1 = new DEmpiricalRV(values, cdf);
        System.out.println(n1);
        System.out.printf("%3s %15s %n", "n", "Values");
        for (int i = 1; i <= 5; i++) {
            System.out.printf("%3d %15f %n", i + 1, n1.getValue());
        }
    }
}
