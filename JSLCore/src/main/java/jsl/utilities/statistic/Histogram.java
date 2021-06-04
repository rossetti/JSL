/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package jsl.utilities.statistic;

import jsl.utilities.math.*;
import jsl.utilities.random.rvariable.ExponentialRV;

/** A Histogram tabulates data into bins.  The user must specify the lower limit
 *  of the first bin, the width of the bins, and the number of bins.  Alternatively,
 *  the user can use the static methods makeHistogram() to specify the range of the data
 *  via a lower limit and upper limit and a desired number of bins. For a histogram
 *  that automatically specifies the bins, see CachedHistogram
 *
 */
public class Histogram extends AbstractStatistic {

    /** The number of bins for the histogram
     */
    protected int myNumBins;

    /**
     * Lower limit of first histogram bin.
     */
    protected double myFirstBinLL;

    /**
     * Upper limit of last histogram bin.
     */
    protected double myLastBinUL;

    /**
     * Width of a bin.
     */
    protected double myBinWidth;

    /**
     * Histogram counts.
     */
    protected double[] myCountData;

    /** Counts of values located below first bin.
     */
    protected double myUnderFlowCount;

    /** Counts of values located above last bin.
     */
    protected double myOverFlowCount;

    /** Collects statistical information
     */
    protected Statistic myStatistic;

    protected Histogram() {
    }

    /** Create a histogram with the lower limit of the first bin equal to 0.0
     *
     * @param numBins number of bins
     * @param binWidth The width of each bin
     */
    public Histogram(int numBins, double binWidth) {
        this(0.0, numBins, binWidth, null, null);
    }

    /** Create a histogram
     *
     * @param firstBinLL lower limit of first bin
     * @param numBins number of bins
     * @param binWidth The width of each bin
    
     */
    public Histogram(double firstBinLL, int numBins, double binWidth) {
        this(firstBinLL, numBins, binWidth, null, null);
    }

    /** Create a histogram with the given name
     *
     * @param firstBinLL lower limit of first bin
     * @param numBins number of bins
     * @param binWidth The width of each bin
     * @param name the name of the histogram
     */
    public Histogram(double firstBinLL, int numBins, double binWidth, String name) {
        this(firstBinLL, numBins, binWidth, name, null);
    }

    /** Create a histogram with based on the provided values
     *
     * @param firstBinLL lower limit of first bin
     * @param numBins number of bins
     * @param binWidth The width of each bin
     * @param values an array of data values to collect on when creating the histogram
     */
    public Histogram(double firstBinLL, int numBins, double binWidth, double[] values) {
        this(firstBinLL, numBins, binWidth, null, values);
    }

    /** Create a histogram with the given name based on the provided values
     *
     * @param firstBinLL lower limit of first bin
     * @param numBins number of bins
     * @param binWidth The width of each bin
     * @param name the name of the histogram
     * @param values an array of data values to collect on when creating the histogram
     */
    public Histogram(double firstBinLL, int numBins, double binWidth, String name, double[] values) {
        super(name);
        if( Double.isInfinite(firstBinLL)){
            throw new IllegalArgumentException("The lower limit of the range cannot be infinite.");
        }
        if( Double.isInfinite(binWidth)){
            throw new IllegalArgumentException("The bin width cannot be infinite.");
        }
        if (numBins <= 0) {
            throw new IllegalArgumentException("The number of bins must be > 0");
        }
        if (binWidth <= 0.0) {
            throw new IllegalArgumentException("The width of the bins must be > 0.0");
        }
        myStatistic = new Statistic();
        myNumBins = numBins;
        myFirstBinLL = firstBinLL;
        myBinWidth = binWidth;
        myLastBinUL = myFirstBinLL + myNumBins * myBinWidth;
        myCountData = new double[myNumBins];
        reset();
        if (values != null) {
            for (double x : values) {
                collect(x);
            }
        }
    }

    /** Create a histogram with lower limit set to zero
     *
     * @param upperLimit the upper limit of the last bin
     * @param numBins the number of bins to create
     * @return  the histogram
     */
    public static Histogram makeHistogram(double upperLimit, int numBins) {
        return (makeHistogram(0.0, upperLimit, numBins, null, null));
    }

    /** Create a histogram
     *
     * @param lowerLimit lower limit of first bin
     * @param upperLimit the upper limit of the last bin
     * @param numBins the number of bins to create
     * @return  the histogram
     */
    public static Histogram makeHistogram(double lowerLimit, double upperLimit, int numBins) {
        return (makeHistogram(lowerLimit, upperLimit, numBins, null, null));
    }

    /** Create a histogram with the given name
     *
     * @param lowerLimit lower limit of first bin
     * @param upperLimit the upper limit of the last bin
     * @param numBins the number of bins to create
     * @param name the name of the histogram
     * @return  the histogram
     */
    public static Histogram makeHistogram(double lowerLimit, double upperLimit, int numBins, String name) {
        return (makeHistogram(lowerLimit, upperLimit, numBins, name, null));
    }

    /** Create a histogram with the given name based on the provided values
     *
     * @param lowerLimit lower limit of first bin
     * @param upperLimit the upper limit of the last bin
     * @param numBins the number of bins to create
     * @param name the name of the histogram
     * @param values an array of data values to collect on when creating the histogram
     * @return  the histogram
     */
    public static Histogram makeHistogram(double lowerLimit, double upperLimit, int numBins, String name, double[] values) {
        if( Double.isInfinite(lowerLimit)){
            throw new IllegalArgumentException("The lower limit of the range cannot be infinite.");
        }
        if( Double.isInfinite(upperLimit)){
            throw new IllegalArgumentException("The upper limit of the range cannot be infinite.");
        }
        if (lowerLimit >= upperLimit) {
            throw new IllegalArgumentException("The lower limit must be < the upper limit of the range");
        }
        if (numBins <= 0) {
            throw new IllegalArgumentException("The number of bins must be > 0");
        }
        double binWidth = JSLMath.roundToScale((upperLimit - lowerLimit) / numBins, false);
        Histogram h = new Histogram(lowerLimit, numBins, binWidth, name, values);
        return (h);
    }

    @Override
    public void collect(double x) {
        if (Double.isNaN(x)) {
            myNumMissing++;
        }

        if (getSaveOption()) {
            save(x);
        }

        if (x < myFirstBinLL) {
            myUnderFlowCount++;
        } else if (x >= myLastBinUL) {
            myOverFlowCount++;
        } else {
            int index = binIndex(x);
            myCountData[index]++;
            myStatistic.collect(x);
        }
    }

    /** computes the zero based bin index for the bin that x falls within
     *
     * @param x the x to find
     * @return the bin index
     */
    protected final int binIndex(double x) {
        return (int) Math.floor((x - myFirstBinLL) / myBinWidth);
    }

    @Override
    public void reset() {
        myNumMissing = 0.0;
        myStatistic.reset();
        myOverFlowCount = 0;
        myUnderFlowCount = 0;
        for (int i = 0; i < myCountData.length; i++) {
            myCountData[i] = 0;
        }
        clearSavedData();
    }

    /** Bins are numbered starting at 1 through the number of bins
     *
     * @return int	the number of the bin where x is located
     * @param x double
     */
    public final int getBinNumber(double x) {
        return (int) Math.ceil((x - myFirstBinLL) / myBinWidth);
    }

    /** The number of observations that fell below the first bin's lower limit
     *
     * @return number of observations that fell below the first bin's lower limit
     */
    public final double getUnderFlowCount() {
        return (myUnderFlowCount);
    }

    /** The number of observations that fell past the last bin's upper limit
     *
     * @return number of observations that fell past the last bin's upper limit
     */
    public final double getOverFlowCount() {
        return (myOverFlowCount);
    }

    /** The bin that x falls in
     *
     * @param x the data to check
     * @return bin that x falls in
     */
    public final Bin getBin(double x) {
        return (getBin(getBinNumber(x)));
    }

    /** Returns an instance of a Bin for the supplied bin number
     *  The bin does not reflect changes to the histogram after
     *  this call
     *
     * @param binNum the bin number to get
     * @return the bin
     */
    public final Bin getBin(int binNum) {
        if (binNum < 1) {
            return null;
        }
        if (binNum > myNumBins) {
            return null;
        }

        Bin b = new Bin();
        b.count = getBinCount(binNum);
        b.lowerLimit = myFirstBinLL + (binNum - 1) * myBinWidth;
        b.upperLimit = b.lowerLimit + myBinWidth;
        return (b);
    }

    /** Returns an array of Bins based on the current state of the
     *  histogram
     *
     * @return the array of bins
     */
    public final Bin[] getBins() {
        Bin[] bins = new Bin[myNumBins];
        for (int i = 1; i <= myNumBins; i++) {
            bins[i - 1] = getBin(i);
        }
        return bins;
    }

    /** Returns the current bin count for the bin associated with x
     *
     * @param x the data to check
     * @return the bin count
     */
    public final double getBinCount(double x) {
        return (getBinCount(getBinNumber(x)));
    }

    /** Returns the bin count for the indicated bin
     *
     * @param binNum the bin number
     * @return the bin count for the indicated bin
     */
    public final double getBinCount(int binNum) {
        if (binNum < 1) {
            return 0.0;
        }
        if (binNum > myNumBins) {
            return 0.0;
        }
        // binNum is 1..myNumBins, but array is zero based
        return (myCountData[binNum - 1]);
    }

    /** Returns the fraction of the data relative to those
     *  tabulated in the bins for the supplied bin number
     *
     * @param binNum the bin number
     * @return the fraction of the data
     */
    public final double getBinFraction(int binNum) {
        double n = myStatistic.getCount();
        if (n > 0.0) {
            return (getBinCount(binNum) / n);
        } else {
            return (Double.NaN);
        }
    }

    /** Returns the fraction of the data relative to those
     *  tabulated in the bins for the bin number associated with the x
     *
     * @param x the data point
     * @return the fraction
     */
    public final double getBinFraction(double x) {
        return (getBinFraction(getBinNumber(x)));
    }

    /** Returns the cumulative count of all bins up to and
     *  including the bin containing the value x
     *
     * @param x the data point
     * @return the cumulative bin count
     */
    public final double getCumulativeBinCount(double x) {
        return (getCumulativeBinCount(getBinNumber(x)));
    }

    /** Returns the cumulative count of all the bins up to
     *  and including the indicated bin number
     *
     * @param binNum the bin number
     * @return cumulative count
     */
    public final double getCumulativeBinCount(int binNum) {
        if (binNum < 0) {
            return 0.0;
        }
        if (binNum > myNumBins) {
            return myStatistic.getCount();
        }
        double sum = 0.0;
        // binNum is 1..myNumBins, but array is zero based
        for (int i = 0; i <= binNum - 1; i++) {
            sum = sum + myCountData[i];
        }
        return sum;
    }

    /** Returns the cumulatiive fraction of the data up to and
     *  including the indicated bin number
     *
     * @param binNum the bin number
     * @return the cumulatiive fraction
     */
    public final double getCumulativeBinFraction(int binNum) {
        double n = myStatistic.getCount();
        if (n > 0.0) {
            return (getCumulativeBinCount(binNum) / n);
        } else {
            return (Double.NaN);
        }
    }

    /** Returns the cumulative fraction of the data up to and
     *  including the bin containing the value of x
     *
     * @param x the datum
     * @return the cumulative fraction
     */
    public final double getCumulativeBinFraction(double x) {
        return (getCumulativeBinFraction(getBinNumber(x)));
    }

    /** Returns the cumulative count of all the data (including under flow and over flow)
     *  up to and including the indicated bin
     *
     * @param binNum the bin number
     * @return the cumulative count
     */
    public final double getCumulativeCount(int binNum) {
        if (binNum < 0) {
            return myUnderFlowCount;
        }
        if (binNum > myNumBins) {
            return getTotalCount();
        }
        double sum = myUnderFlowCount;
        // binNum is 1..myNumBins, but array is zero based
        for (int i = 0; i <= binNum - 1; i++) {
            sum = sum + myCountData[i];
        }
        return sum;
    }

    /** Returns the cumulative count of all the data (including under flow
     *  and over flow) for all bins up to and including the bin containing x
     *
     * @param x the datum
     * @return the cumulative count
     */
    public final double getCumulativeCount(double x) {
        return (getCumulativeCount(getBinNumber(x)));
    }

    /** Returns the cumulative fraction of all the data up to and including
     *  the supplied bin (includes over and under flow)
     *
     * @param binNum the bin number
     * @return the cumulative fraction
     */
    public final double getCumulativeFraction(int binNum) {
        double n = getTotalCount();
        if (n > 0.0) {
            return (getCumulativeBinCount(binNum) / n);
        } else {
            return (Double.NaN);
        }
    }

    /** Returns the cumulative fraction of all the data up to an including
     *  the bin containing the value x, (includes over and under flow)
     *
     * @param x the datum
     * @return the cumulative fraction
     */
    public final double getCumulativeFraction(double x) {
        return (getCumulativeFraction(getBinNumber(x)));
    }

    /** Total number of observations collected including overflow and underflow
     *
     * @return Total number of observations
     */
    public final double getTotalCount() {
        return (myStatistic.getCount() + myOverFlowCount + myUnderFlowCount);
    }

    /** The first bin's lower limit
     *
     * @return first bin's lower limit
     */
    public final double getFirstBinLowerLimit() {
        return (myFirstBinLL);
    }

    /** The last bin's upper limit
     *
     * @return last bin's upper limit
     */
    public final double getLastBinUpperLimit() {
        return (myLastBinUL);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Histogram: ").append(getName());
        sb.append(System.lineSeparator());
        sb.append("-------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("Number of bins = ").append(myNumBins);
        sb.append(System.lineSeparator());
        sb.append("Bin width = ").append(myBinWidth);
        sb.append(System.lineSeparator());
        sb.append("First bin starts at = ").append(myFirstBinLL);
        sb.append(System.lineSeparator());
        sb.append("Last bin ends at = ").append(myLastBinUL);
        sb.append(System.lineSeparator());
        sb.append("Under flow count = ").append(myUnderFlowCount);
        sb.append(System.lineSeparator());
        sb.append("Over flow count = ").append(myOverFlowCount);
        sb.append(System.lineSeparator());
        double n = getCount();
        sb.append("Total bin count = ").append(n);
        sb.append(System.lineSeparator());
        sb.append("Total count = ").append(getTotalCount());
        sb.append(System.lineSeparator());
        sb.append("-------------------------------------");
        sb.append(System.lineSeparator());
        sb.append(String.format("%3s %-12s %-5s %-5s %-5s %-6s", "Bin", "Range", "Count", "Total", "Prob", "CumProb"));
        sb.append(System.lineSeparator());
//        sb.append("Bin \t Range \t Count \t\t tc \t\t p \t\t cp\n");
        double tc = 0.0;
        for (int i = 1; i <= myNumBins; i++) {
            double LL = myFirstBinLL + (i - 1) * myBinWidth;
            double UL = LL + myBinWidth;
            double c = getBinCount(i);
            tc = tc + c;
            String s = String.format("%3d [%4.2f,%4.2f) %5.1f %5.1f %5f %6f %n",i,LL, UL, c, tc, (c/n), (tc/n));
            sb.append(s);
        }

//        for (int i = 1; i <= myNumBins; i++) {
//            double LL = myFirstBinLL + (i - 1) * myBinWidth;
//            double UL = LL + myBinWidth;
//            double c = getBinCount(i);
//            tc = tc + c;
//            sb.append(i).append("\t" + "[").append(LL).append(",").append(UL).append(")\t").append(c).append("\t").append(tc).append("\t").append(c / n).append("\t").append(tc / n).append("\n");
//        }
        sb.append("-------------------------------------\n");
        sb.append("Statistics on data collected within bins:\n");
        sb.append("-------------------------------------\n");
        sb.append(myStatistic);
        sb.append("-------------------------------------\n");

        return (sb.toString());
    }

    @Override
    public final double getAverage() {
        return myStatistic.getAverage();
    }

    @Override
    public final double getConfidenceLevel() {
        return myStatistic.getConfidenceLevel();
    }

    @Override
    public final double getCount() {
        return myStatistic.getCount();
    }

    @Override
    public final double getDeviationSumOfSquares() {
        return myStatistic.getDeviationSumOfSquares();
    }

    @Override
    public double getHalfWidth(double alpha) {
        return myStatistic.getHalfWidth(alpha);
    }

    @Override
    public final double getKurtosis() {
        return myStatistic.getKurtosis();
    }

    @Override
    public final double getLag1Correlation() {
        return myStatistic.getLag1Correlation();
    }

    @Override
    public final double getLag1Covariance() {
        return myStatistic.getLag1Covariance();
    }

    @Override
    public final double getLastValue() {
        return myStatistic.getLastValue();
    }

    @Override
    public final double getMax() {
        return myStatistic.getMax();
    }

    @Override
    public final double getMin() {
        return myStatistic.getMin();
    }

    /**
     * Returns the observation weighted sum of the data i.e. sum = sum + j*x
     * where j is the observation number and x is jth observation
     *
     * @return the observation weighted sum of the data
     */
    public final double getObsWeightedSum() {
        return myStatistic.getObsWeightedSum();
    }

    @Override
    public final double getSkewness() {
        return myStatistic.getSkewness();
    }

    @Override
    public final double getStandardDeviation() {
        return myStatistic.getStandardDeviation();
    }

    @Override
    public final double getStandardError() {
        return myStatistic.getStandardError();
    }

    @Override
    public final double getSum() {
        return myStatistic.getSum();
    }

    @Override
    public final double getVariance() {
        return myStatistic.getVariance();
    }

    @Override
    public final double getVonNeumannLag1TestStatistic() {
        return myStatistic.getVonNeumannLag1TestStatistic();
    }

    @Override
    public final double getVonNeumannLag1TestStatisticPValue() {
        return myStatistic.getVonNeumannLag1TestStatisticPValue();
    }

    @Override
    public final int getLeadingDigitRule(double a) {
        return myStatistic.getLeadingDigitRule(a);
    }

    public class Bin {

        double lowerLimit;

        double upperLimit;

        double count;

        @Override
        public String toString() {
            String s = String.format("[%3.2f,%3.2f) = %d", lowerLimit, upperLimit, count);
           // String s = "[" + lowerLimit + "," + upperLimit + ") = " + count;
            return (s);
        }
    }

    public static void main(String args[]) {
        ExponentialRV d = new ExponentialRV(2);
        Histogram h = new Histogram(0.0, 20, 0.1);
        for (int i = 1; i <= 100; ++i) {
            h.collect(d.getValue());
        }
        System.out.println(h);

    }
}
