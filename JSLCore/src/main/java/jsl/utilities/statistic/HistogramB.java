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

import jsl.utilities.random.rvariable.ExponentialRV;

import java.util.ArrayList;
import java.util.List;

/**
 * A Histogram tabulates data into bins.  The user must specify the break points
 * of the bins, b0, b1, b2, ..., bk, where there are k+1 break points, and k bins.
 * b0 may be Double.NEGATIVE_INFINITY and bk may be Double.POSITIVE_INFINITY.
 * <p>
 * If only one break point is supplied, then the bins are automatically defined as:
 * (Double.NEGATIVE_INFINITY, b0] and (b0, Double.POSITIVE_INFINITY).
 * <p>
 * If two break points are provided, then there is one bin: [b0, b1), any values
 * less than b0 will be counted as underflow and any values [b1, +infinity) will
 * be counted as overflow.
 * <p>
 * If k+1 break points are provided then the bins are defined as:
 * [b0,b1), [b1,b2), [b2,b3), ..., [bk-1,bk) and
 * any values in (-infinity, b0) will be counted as underflow and any values [bk, +infinity) will
 * be counted as overflow. If b0 equals Double.NEGATIVE_INFINITY then there can
 * be no underflow. Similarly, if bk equals Double.POSITIVE_INFINITY there can be no
 * overflow.
 * <p>
 * The break points do not have to define equally sized bins. Static methods within HistogramBIfc
 * are provided to create equal width bins and to create histograms with common
 * characteristics.
 * <p>
 * If any presented value is Double.NaN, then the value is counted as missing
 * and the observation is not tallied towards the total number of observations. Underflow and
 * overflow counts also do not count towards the total number of observations.
 * <p>
 * Statistics are also automatically collected on the collected observations. The statistics
 * do not include missing, underflow, and overflow observations. Statistics are only computed
 * on those observations that were placed (counted) within some bin.
 */
public class HistogramB extends AbstractStatistic implements HistogramBIfc {

    /**
     * Lower limit of first histogram bin.
     */
    protected final double myFirstBinLL;

    /**
     * Upper limit of last histogram bin.
     */
    protected final double myLastBinUL;

    /**
     * Counts of values located below first bin.
     */
    protected double myUnderFlowCount;

    /**
     * Counts of values located above last bin.
     */
    protected double myOverFlowCount;

    /**
     * Collects statistical information
     */
    protected final Statistic myStatistic;

    /**
     *  holds the binned data
     */
    protected final List<HistogramBin> myBins;

    /**
     *
     * @param breakPoints the break points for the histogram, must be strictly increasing
     */
    public HistogramB(double[] breakPoints) {
        this(breakPoints, null);
    }

    /**
     *
     * @param breakPoints the break points for the histogram, must be strictly increasing
     * @param name an optional name for the histogram
     */
    public HistogramB(double[] breakPoints, String name) {
        super(name);
        myBins = HistogramBIfc.makeBins(breakPoints);
        myFirstBinLL = myBins.get(0).lowerLimit;
        myLastBinUL = myBins.get(myBins.size() - 1).upperLimit;
        myStatistic = new Statistic();
        myNumMissing = 0.0;
        myOverFlowCount = 0;
        myUnderFlowCount = 0;
    }

    @Override
    public void collect(double x) {
        if (isMissing(x)) {
            myNumMissing++;
            return;
        }
        if (getSaveOption()) {
            save(x);
        }
        if (x < myFirstBinLL) {
            myUnderFlowCount++;
        } else if (x >= myLastBinUL) {
            myOverFlowCount++;
        } else {
            HistogramBin bin = findBin(x);
            bin.increment();
            // collect statistics on only binned observations
            myStatistic.collect(x);
        }
    }

    @Override
    public final HistogramBin findBin(double x) {
        for (HistogramBin bin : myBins) {
            if (x < bin.upperLimit) {
                return bin;
            }
        }
        // bin must be found, but just in case
        String s = "The observation = " + x + " could not be binned!";
        throw new IllegalStateException(s);
    }

    @Override
    public void reset() {
        myNumMissing = 0.0;
        myStatistic.reset();
        myOverFlowCount = 0;
        myUnderFlowCount = 0;
        for (HistogramBin bin : myBins) {
            bin.reset();
        }
        clearSavedData();
    }

    @Override
    public final int getBinNumber(double x) {
        HistogramBin bin = findBin(x);
        return bin.getBinNumber();
    }

    @Override
    public final double getUnderFlowCount() {
        return (myUnderFlowCount);
    }

    @Override
    public final double getOverFlowCount() {
        return (myOverFlowCount);
    }

    @Override
    public final int getNumberBins() {
        return myBins.size();
    }

    @Override
    public final HistogramBin getBin(double x) {
        HistogramBin bin = findBin(x);
        return bin.newInstance();
    }

    @Override
    public final HistogramBin getBin(int binNum) {
        return myBins.get(binNum - 1).newInstance();
    }

    @Override
    public final List<HistogramBin> getBins() {
        List<HistogramBin> bins = new ArrayList<>();
        for (HistogramBin bin : myBins) {
            bins.add(bin.newInstance());
        }
        return bins;
    }

    @Override
    public final HistogramBin[] getBinArray(){
        HistogramBin[] bins = new HistogramBin[myBins.size()];
        getBins().toArray(bins);
        return bins;
    }

    @Override
    public final double[] getBreakPoints(){
        double[] b = new double[myBins.size() + 1];
        int i = 0;
        for (HistogramBin bin: myBins){
            b[i] = bin.getLowerLimit();
            i++;
        }
        b[myBins.size()] = getBin(getNumberBins()).getUpperLimit();
        return b;
    }

    @Override
    public final double[] getBinCounts(){
        double[] cnts = new double[getNumberBins()];
        int i = 0;
        for (HistogramBin bin: myBins){
            cnts[i] = bin.getCount();
            i++;
        }
        return cnts;
    }

    @Override
    public final double getBinCount(double x) {
        return findBin(x).getCount();
    }

    @Override
    public final double getBinCount(int binNum) {
        return myBins.get(binNum - 1).getCount();
    }

    @Override
    public final double getBinFraction(int binNum) {
        double n = myStatistic.getCount();
        if (n > 0.0) {
            return (getBinCount(binNum) / n);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getBinFraction(double x) {
        return (getBinFraction(getBinNumber(x)));
    }

    @Override
    public final double getCumulativeBinCount(double x) {
        return (getCumulativeBinCount(getBinNumber(x)));
    }

    @Override
    public final double getCumulativeBinCount(int binNum) {
        if (binNum < 0) {
            return 0.0;
        }
        if (binNum > myBins.size()) {
            return myStatistic.getCount();
        }
        double sum = 0.0;
        for (int i = 1; i <= binNum; i++) {
            sum = sum + getBinCount(i);
        }
        return sum;
    }

    @Override
    public final double getCumulativeBinFraction(int binNum) {
        double n = myStatistic.getCount();
        if (n > 0.0) {
            return (getCumulativeBinCount(binNum) / n);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getCumulativeBinFraction(double x) {
        return (getCumulativeBinFraction(getBinNumber(x)));
    }

    @Override
    public final double getCumulativeCount(int binNum) {
        if (binNum < 0) {
            return myUnderFlowCount;
        }
        if (binNum > myBins.size()) {
            return getTotalCount();
        }
        return myUnderFlowCount + getCumulativeBinCount(binNum);
    }

    @Override
    public final double getCumulativeCount(double x) {
        return (getCumulativeCount(getBinNumber(x)));
    }

    @Override
    public final double getCumulativeFraction(int binNum) {
        double n = getTotalCount();
        if (n > 0.0) {
            return (getCumulativeCount(binNum) / n);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getCumulativeFraction(double x) {
        return (getCumulativeFraction(getBinNumber(x)));
    }

    @Override
    public final double getTotalCount() {
        return (myStatistic.getCount() + myOverFlowCount + myUnderFlowCount);
    }

    @Override
    public final double getFirstBinLowerLimit() {
        return (myFirstBinLL);
    }

    @Override
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
        sb.append("Number of bins = ").append(getNumberBins());
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
        sb.append(String.format("%3s %-12s %-5s %-5s %-5s %-6s", "Bin", "Range", "Count", "CumTot", "Frac", "CumFrac"));
        sb.append(System.lineSeparator());
//        sb.append("Bin \t Range \t Count \t\t tc \t\t p \t\t cp\n");
        double ct = 0.0;
        for (HistogramBin bin : myBins) {
            double c = bin.getCount();
            ct = ct + c;
            String s = String.format("%s %5.1f %5f %6f %n", bin, ct, (c / n), (ct / n));
            sb.append(s);
        }
        sb.append("-------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("Statistics on data collected within bins:");
        sb.append(System.lineSeparator());
        sb.append("-------------------------------------");
        sb.append(System.lineSeparator());
        sb.append(myStatistic);
        sb.append("-------------------------------------");
        sb.append(System.lineSeparator());

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


    public static void main(String args[]) {
        ExponentialRV d = new ExponentialRV(2);
        double[] points = HistogramBIfc.createBreakPoints(0.0, 10, 0.25);
        HistogramBIfc h1 = new HistogramB(points);
        HistogramBIfc h2 = new HistogramB(HistogramBIfc.addPositiveInfinity(points));
        for (int i = 1; i <= 100; ++i) {
            double x = d.getValue();
            h1.collect(x);
            h2.collect(x);
        }
        System.out.println(h1);
        System.out.println(h2);

    }
}
