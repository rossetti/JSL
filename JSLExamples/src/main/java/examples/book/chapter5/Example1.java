package examples.book.chapter5;

import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;

/**
 *  This example illustrates how to perform simple Monte-Carlo
 *  integration on the sqrt(x) over the range from 1 to 4.
 */
public class Example1 {
    public static void main(String[] args) {
        double a = 1.0;
        double b = 4.0;
        UniformRV ucdf = new UniformRV(a, b);
        Statistic stat = new Statistic("Area Estimator");
        int n = 100; // sample size
        for (int i = 1; i <= n; i++) {
            double x = ucdf.getValue();
            double gx = Math.sqrt(x);
            double y = (b - a) * gx;
            stat.collect(y);
        }
        System.out.printf("True Area = %10.3f %n", 14.0 / 3.0);
        System.out.printf("Area estimate = %10.3f %n", stat.getAverage());
        System.out.println("Confidence Interval");
        System.out.println(stat.getConfidenceInterval());
    }
}
