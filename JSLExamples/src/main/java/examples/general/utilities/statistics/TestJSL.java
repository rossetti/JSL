package examples.general.utilities.statistics;

import jsl.utilities.random.rvariable.BivariateNormalRV;
import jsl.utilities.reporting.JSL;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.Statistic;

import java.io.PrintWriter;
import java.util.List;

public class TestJSL {

    public static void main(String[] args) {
        // write string to file jslOutput.txt found in directory jslOutput
        // JSL.out can be used just like System.out except text goes to a file
        JSL.getInstance().out.println("Hello World!");

        // make a file and write some data to it, file will be directory jslOutput, by default
        PrintWriter writer = JSL.getInstance().makePrintWriter("data.csv");

        // create a bivariate normal
        BivariateNormalRV rv = new BivariateNormalRV(1.0, 2.0, 3.0, 1.0, 0.8);

        Statistic xStat = new Statistic("X");
        Statistic yStat = new Statistic("Y");

        //make a header
        writer.printf("%s, %s %n", "x", "y");
        for (int i = 1; i <= 100; i++) {
            // array is size 2, 0th element is x, 1st element is y
            double[] sample = rv.sample();
            xStat.collect(sample[0]);
            yStat.collect(sample[1]);
            // write the data to the file
            writer.printf("%f, %f %n", sample[0], sample[1]);
        }
        StatisticReporter reporter = new StatisticReporter(List.of(xStat, yStat));
        System.out.println(reporter.getHalfWidthSummaryReport());

    }
}
