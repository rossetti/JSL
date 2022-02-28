package examples.book.chapter5;

import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.statistic.Statistic;

public class Example4 {
    public static void main(String[] args) {
        double q = 30; // order qty
        double s = 0.25; //sales price
        double c = 0.15; // unit cost
        double u = 0.02; //salvage value
        double[] values = {5, 10, 40, 45, 50, 55, 60};
        double[] cdf = {0.1, 0.3, 0.6, 0.8, 0.9, 0.95, 1.0};
        DEmpiricalRV dCDF = new DEmpiricalRV(values, cdf);
        Statistic stat = new Statistic("Profit");
        double n = 100; // sample size
        for (int i = 1; i <= n; i++) {
            double d = dCDF.getValue();
            double amtSold = Math.min(d, q);
            double amtLeft = Math.max(0, q - d);
            double g = s * amtSold + u * amtLeft - c * q;
            stat.collect(g);
        }
        System.out.printf("%s \t %f %n", "Count = ", stat.getCount());
        System.out.printf("%s \t %f %n", "Average = ", stat.getAverage());
        System.out.printf("%s \t %f %n", "Std. Dev. = ", stat.getStandardDeviation());
        System.out.printf("%s \t %f %n", "Half-width = ", stat.getHalfWidth());
        System.out.println(stat.getConfidenceLevel() * 100 + "% CI = " + stat.getConfidenceInterval());
    }
}
