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

import jsl.utilities.Interval;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.ExponentialRV;

import java.util.List;
import java.util.Map;

/**
 * A CachedHistogram allow collection and forming of a histogram without
 * pre-specifying the break points. It works by using an initial cache of the
 * data to determine a reasonable number of bins and bin width based on the
 * observed minimum and maximum of the data within the cache. Once the cache is
 * observed, this class works essentially like a Histogram, but you do not
 * have to supply break points.
 */
public class CachedHistogramB extends AbstractStatistic implements HistogramBIfc {

    public static final int DEFAULT_CACHE_SIZE = 100;

    /**
     * Flag indicating the histogram is caching values to compute adequate
     * range.
     */
    protected boolean myCachingFlag = true;

    /**
     * Counts number of observations in the cache
     */
    protected int myCountCache = 0;

    /**
     * Cache for accumulated values.
     */
    protected double[] myDataCache;

    /**
     * Collects histogram statistics after caching is done
     */
    protected HistogramBIfc myHistogram;

    /**
     * Creates a CachedHistogram using the DEFAULT_CACHE_SIZE by determining a
     * reasonable number of bins, each having an equal width
     */
    public CachedHistogramB() {
        this(DEFAULT_CACHE_SIZE, null);
    }

    /**
     * Creates a CachedHistogram using the DEFAULT_CACHE_SIZE by determining a
     * reasonable number of bins, each having an equal width
     *
     * @param name the name of the histogram
     */
    public CachedHistogramB(String name) {
        this(DEFAULT_CACHE_SIZE, name);
    }

    /**
     * Creates a CachedHistogram using the DEFAULT_CACHE_SIZE by determining a
     * reasonable number of bins, each having an equal width
     *
     * @param cacheSize The size of the cache for initializing the histogram, must be &gt;=0
     */
    public CachedHistogramB(int cacheSize) {
        this(cacheSize, null);
    }

    /**
     * Creates a CachedHistogram
     *
     * @param cacheSize The size of the cache for initializing the histogram, must be &gt;=0
     * @param name      the name of the histogram
     */
    public CachedHistogramB(int cacheSize, String name) {
        super(name);
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("The size of the cache must be > 0");
        }
        myDataCache = new double[cacheSize];
        double[] bp = {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY};
        myHistogram = new HistogramB(bp); // start with one bin for all observations
    }

    @Override
    public final void collect(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            myNumMissing++;
            return;
        }
        if (getSaveOption()) {
            save(x);
        }
        // always collect for the histogram
        myHistogram.collect(x);
        if (myCachingFlag) {
            myDataCache[myCountCache] = x;
            myCountCache++;
            if (myCountCache == myDataCache.length) {
                myHistogram.reset();
                // the cache is full, now form permanent histogram with recommended break points
                double[] breakPoints = HistogramBIfc.recommendBreakPoints(myDataCache);
                myHistogram = new HistogramB(breakPoints, myHistogram.getName());
                myHistogram.collect(myDataCache);
                // turn off the caching
                myCachingFlag = false;
                //myDataCache = null;  //TODO should I delete it?
            }
        }
    }




    @Override
    public final void reset() {
        myNumMissing = 0.0;
        myHistogram.reset();
        if (myCachingFlag) {
            // we will assume that the cache is cleared if it is on
            for (int i = 0; i < myCountCache; i++) {
                myDataCache[i] = 0.0;
            }
            myCountCache = 0;
        }
        clearSavedData();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (myCachingFlag) {
            sb.append("Histogram: ");
            sb.append(getName());
            sb.append(System.lineSeparator());
            sb.append("-------------------------------------");
            sb.append(System.lineSeparator());
            sb.append("Caching is still on. Results are based only on observed cached data:");
            sb.append(System.lineSeparator());
            sb.append("Cache size = ");
            sb.append(myDataCache.length);
            sb.append(System.lineSeparator());
            sb.append("Number of observations cached: ");
            sb.append(myCountCache);
            sb.append(System.lineSeparator());
            sb.append("Final break points have not yet been determined.");
            sb.append(System.lineSeparator());
// indicate current break points
            sb.append("-------------------------------------");
            sb.append(System.lineSeparator());
        }
        sb.append(myHistogram);

        return (sb.toString());
    }

    @Override
    public double getCount() {
        return myHistogram.getCount();
    }

    @Override
    public double getSum() {
        return myHistogram.getSum();
    }

    @Override
    public double getAverage() {
        return myHistogram.getAverage();
    }

    @Override
    public double getDeviationSumOfSquares() {
        return myHistogram.getDeviationSumOfSquares();
    }

    @Override
    public double getVariance() {
        return myHistogram.getVariance();
    }

    @Override
    public double getStandardDeviation() {
        return myHistogram.getStandardDeviation();
    }

    @Override
    public double getMin() {
        return myHistogram.getMin();
    }

    @Override
    public double getMax() {
        return myHistogram.getMax();
    }

    @Override
    public double getLastValue() {
        return myHistogram.getLastValue();
    }

    @Override
    public double getKurtosis() {
        return myHistogram.getKurtosis();
    }

    @Override
    public double getSkewness() {
        return myHistogram.getSkewness();
    }

    @Override
    public double getStandardError() {
        return myHistogram.getStandardError();
    }

    @Override
    public double getHalfWidth() {
        return myHistogram.getHalfWidth();
    }

    @Override
    public double getHalfWidth(double level) {
        return myHistogram.getHalfWidth(level);
    }

    @Override
    public double getConfidenceLevel() {
        return myHistogram.getConfidenceLevel();
    }

    @Override
    public Interval getConfidenceInterval() {
        return myHistogram.getConfidenceInterval();
    }

    @Override
    public Interval getConfidenceInterval(double level) {
        return myHistogram.getConfidenceInterval(level);
    }

    @Override
    public double getRelativeError() {
        return myHistogram.getRelativeError();
    }

    @Override
    public double getRelativeWidth() {
        return myHistogram.getRelativeWidth();
    }

    @Override
    public double getRelativeWidth(double level) {
        return myHistogram.getRelativeWidth(level);
    }

    @Override
    public double getLag1Covariance() {
        return myHistogram.getLag1Covariance();
    }

    @Override
    public double getLag1Correlation() {
        return myHistogram.getLag1Correlation();
    }

    @Override
    public double getVonNeumannLag1TestStatistic() {
        return myHistogram.getVonNeumannLag1TestStatistic();
    }

    @Override
    public final double getVonNeumannLag1TestStatisticPValue() {
        return myHistogram.getVonNeumannLag1TestStatisticPValue();
    }

    @Override
    public double getNumberMissing() {
        return myHistogram.getNumberMissing();
    }

    @Override
    public int getLeadingDigitRule(double a) {
        return myHistogram.getLeadingDigitRule(a);
    }

    @Override
    public double[] getStatistics() {
        return myHistogram.getStatistics();
    }

    @Override
    public String getCSVStatisticHeader() {
        return myHistogram.getCSVStatisticHeader();
    }

    @Override
    public String getCSVStatistic() {
        return myHistogram.getCSVStatistic();
    }

    @Override
    public List<String> getCSVValues() {
        return myHistogram.getCSVValues();
    }

    @Override
    public List<String> getCSVHeader() {
        return myHistogram.getCSVHeader();
    }

    @Override
    public Map<String, Double> getStatisticsAsMap() {
        return myHistogram.getStatisticsAsMap();
    }

    @Override
    public HistogramBin findBin(double x) {
        return myHistogram.findBin(x);
    }

    @Override
    public int getBinNumber(double x) {
        return myHistogram.getBinNumber(x);
    }

    @Override
    public double getUnderFlowCount() {
        return myHistogram.getUnderFlowCount();
    }

    @Override
    public double getOverFlowCount() {
        return myHistogram.getOverFlowCount();
    }

    @Override
    public int getNumberBins() {
        return myHistogram.getNumberBins();
    }

    @Override
    public HistogramBin getBin(double x) {
        return myHistogram.getBin(x);
    }

    @Override
    public HistogramBin getBin(int binNum) {
        return myHistogram.getBin(binNum);
    }

    @Override
    public List<HistogramBin> getBins() {
        return myHistogram.getBins();
    }

    @Override
    public HistogramBin[] getBinArray() {
        return myHistogram.getBinArray();
    }

    @Override
    public double[] getBreakPoints() {
        return myHistogram.getBreakPoints();
    }

    @Override
    public double[] getBinCounts() {
        return myHistogram.getBinCounts();
    }

    @Override
    public double getBinCount(double x) {
        return myHistogram.getBinCount(x);
    }

    @Override
    public double getBinCount(int binNum) {
        return myHistogram.getBinCount(binNum);
    }

    @Override
    public double getBinFraction(int binNum) {
        return myHistogram.getBinFraction(binNum);
    }

    @Override
    public double getBinFraction(double x) {
        return myHistogram.getBinFraction(x);
    }

    @Override
    public double getCumulativeBinCount(double x) {
        return myHistogram.getCumulativeBinCount(x);
    }

    @Override
    public double getCumulativeBinCount(int binNum) {
        return myHistogram.getCumulativeBinCount(binNum);
    }

    @Override
    public double getCumulativeBinFraction(int binNum) {
        return myHistogram.getCumulativeBinFraction(binNum);
    }

    @Override
    public double getCumulativeBinFraction(double x) {
        return myHistogram.getCumulativeBinFraction(x);
    }

    @Override
    public double getCumulativeCount(int binNum) {
        return myHistogram.getCumulativeCount(binNum);
    }

    @Override
    public double getCumulativeCount(double x) {
        return myHistogram.getCumulativeCount(x);
    }

    @Override
    public double getCumulativeFraction(int binNum) {
        return myHistogram.getCumulativeFraction(binNum);
    }

    @Override
    public double getCumulativeFraction(double x) {
        return myHistogram.getCumulativeFraction(x);
    }

    @Override
    public double getTotalCount() {
        return myHistogram.getTotalCount();
    }

    @Override
    public double getFirstBinLowerLimit() {
        return myHistogram.getFirstBinLowerLimit();
    }

    @Override
    public double getLastBinUpperLimit() {
        return myHistogram.getLastBinUpperLimit();
    }

    public static void main(String[] args) {
        ExponentialRV d = new ExponentialRV(2);
        CachedHistogramB h = new CachedHistogramB(100);
        for (int i = 1; i <= 1000; ++i) {
            h.collect(d.getValue());
        }
        System.out.println(h);
        d.resetStartStream();
        // should not get out of caching
        h = new CachedHistogramB(150);
        for (int i = 1; i <= 100; ++i) {
            h.collect(d.getValue());
        }
        System.out.println(h);
    }
}
