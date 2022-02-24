package examples.book.chapter3;

import jsl.utilities.distributions.Binomial;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 * This example illustrates how to make instances of Distributions.
 * Specifically, a binomial distribution is created, and it is used
 * to compute some properties and to make a random variable. Notice
 * that a distribution is not the same thing as a random variable.
 * Random variables generate values. Distributions describe how the
 * values are distributed. Random variables are immutable. Distributions
 * can have their parameters changed.
 */
public class Example8 {
    public static void main(String[] args) {
        // make and use a Binomial(p, n) distribution
        int n = 10;
        double p = 0.8;
        System.out.println("n = " + n);
        System.out.println("p = " + p);
        Binomial bnDF = new Binomial(p, n);
        System.out.println("mean = " + bnDF.getMean());
        System.out.println("variance = " + bnDF.getVariance());
        // compute some values
        System.out.printf("%3s %15s %15s %n", "k", "p(k)", "cdf(k)");
        for (int i = 0; i <= 10; i++) {
            System.out.printf("%3d %15.10f %15.10f %n", i, bnDF.pmf(i), bnDF.cdf(i));
        }
        System.out.println();
        // change the probability and number of trials
        bnDF.setParameters(0.5, 20);
        System.out.println("mean = " + bnDF.getMean());
        System.out.println("variance = " + bnDF.getVariance());
        // make random variables based on the distributions
        RVariableIfc brv = bnDF.getRandomVariable();
        System.out.printf("%3s %15s %n", "n", "Values");
        // generate some values
        for (int i = 0; i < 5; i++) {
            // getValue() method returns generated values
            int x = (int) brv.getValue();
            System.out.printf("%3d %15d %n", i + 1, x);
        }
    }
}
