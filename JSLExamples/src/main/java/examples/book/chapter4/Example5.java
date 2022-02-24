package examples.book.chapter4;

import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.BatchStatistic;

/**
 * This example illustrates how to create an instance of a
 * BatchStatistic.  A BatchStatistic will collect statistics on observations
 * and while doing so form batches on which batch average are computed.
 * The batching algorithm form batches that are based on the supplied
 * minimum number of batches, maximum number of batches, and the number of
 * batches multiple.
 */
public class Example5 {
    public static void main(String[] args) {
        ExponentialRV d = new ExponentialRV(2);

        // number of observations
        int n = 1000;

        // minimum number of batches permitted
        // there will not be less than this number of batches
        int minNumBatches = 40;

        // minimum batch size permitted
        // the batch size can be no smaller than this amount
        int minBatchSize = 25;

        // maximum number of batch multiple
        //  The multiple of the minimum number of batches
        //  that determines the maximum number of batches
        //  e.g. if the min. number of batches is 20
        //  and the max number batches multiple is 2,
        //  then we can have at most 40 batches
        int maxNBMultiple = 2;

        // In this example, since 40*25 = 1000, the batch multiple does not matter

        BatchStatistic bm = new BatchStatistic(minNumBatches, minBatchSize, maxNBMultiple);

        for (int i = 1; i <= n; ++i) {
            bm.collect(d.getValue());
        }
        System.out.println(bm);

        double[] bma = bm.getBatchMeanArrayCopy();
        int i = 0;
        for (double x : bma) {
            System.out.println("bm(" + i + ") = " + x);
            i++;
        }

        // this rebatches the 40 down to 10
        //Statistic s = bm.rebatchToNumberOfBatches(10);
        //System.out.println(s);
    }
}
