package examples.book.chapter4;

import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.Statistic;

import java.util.List;

/**
 * This example illustrates how to create instances of the Statistic
 * class and collect statistics on observations.  In addition, the basic
 * use of the StatisticReporter class is illustrated to show how to pretty
 * print the statistical results.
 */
public class Example1 {
    public static void main(String[] args) {
        // create a normal mean = 20.0, variance = 4.0 random variable
        NormalRV n = new NormalRV(20.0, 4.0);
        // create a Statistic to observe the values
        Statistic stat = new Statistic("Normal Stats");
        Statistic pGT20 = new Statistic("P(X>=20");
        // generate 100 values
        for (int i = 1; i <= 100; i++) {
            // getValue() method returns generated values
            double x = n.getValue();
            stat.collect(x);
            pGT20.collect(x >= 20.0);
        }
        System.out.println(stat);
        System.out.println(pGT20);

        StatisticReporter reporter = new StatisticReporter(List.of(stat, pGT20));
        System.out.println(reporter.getHalfWidthSummaryReport());
    }
}
