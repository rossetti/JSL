package examples.book.chapter5;

import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.Statistic;

import java.util.List;

public class Example2 {
    public static void main(String[] args) {
        NormalRV rv = new NormalRV(10.0, 4.0);
        Statistic estimateX = new Statistic("Estimated X");
        Statistic estOfProb = new Statistic("Pr(X>8)");
        StatisticReporter r = new StatisticReporter(List.of(estOfProb, estimateX));
        int n = 20; // sample size
        for (int i = 1; i <= n; i++) {
            double x = rv.getValue();
            estimateX.collect(x);
            estOfProb.collect(x > 8);
        }
        System.out.println(r.getHalfWidthSummaryReport());
    }
}
