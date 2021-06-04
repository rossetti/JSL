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

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 * A CachedHistogram allow collection and forming of a histogram without
 * pre-specifying the number of bins. It works by using an initial cache of the
 * data to determine a reasonable number of bins and bin width based on the
 * observed minimum and maximum of the data within the cache. Once the cache is
 * observed, this class works essentially like a Histogram, which can be
 * returned via the getHistogram() method.
 *
 *
 */
public class CachedHistogram extends AbstractStatistic {

    public static final int DEFAULT_CACHE_SIZE = 100;

    /**
     * The number of bins for the histogram
     */
    protected int myNumBins;

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
     * Collects statistical information on the data during caching
     */
    protected Statistic myCacheStatistic;

    /**
     * Collects histogram statistics after caching is done
     */
    protected Histogram myHistogram;

    /**
     * Creates a CachedHistogram using the DEFAULT_CACHE_SIZE by determining a
     * reasonable number of bins
     */
    public CachedHistogram() {
        this(DEFAULT_CACHE_SIZE, 0, null);
    }

    /**
     * Creates a CachedHistogram using the DEFAULT_CACHE_SIZE by determining a
     * reasonable number of bins
     *
     * @param name the name of the histogram
     */
    public CachedHistogram(String name) {
        this(DEFAULT_CACHE_SIZE, 0, name);
    }

    /**
     * Creates a CachedHistogram by determining a reasonable number of bins
     *
     * @param cacheSize The size of the cache for initializing the histogram
     * @param name the name of the histogram
     */
    public CachedHistogram(int cacheSize, String name) {
        this(cacheSize, 0, name);
    }

    /**
     * Creates a CachedHistogram by determining a reasonable number of bins
     *
     * @param cacheSize The size of the cache for initializing the histogram
     */
    public CachedHistogram(int cacheSize) {
        this(cacheSize, 0, null);
    }

    /**
     * Creates a CachedHistogram
     *
     * @param cacheSize The size of the cache for initializing the histogram
     * @param numBins The number of desired bins, must be &gt;=0, if zero a
     * reasonable number of bins is automatically determined
     */
    public CachedHistogram(int cacheSize, int numBins) {
        this(cacheSize, numBins, null);
    }

    /**
     * Creates a CachedHistogram
     *
     * @param cacheSize The size of the cache for initializing the histogram
     * @param numBins The number of desired bins, must be &gt;=0, if zero a
     * reasonable number of bins is automatically determined
     * @param name the name of the histogram
     */
    public CachedHistogram(int cacheSize, int numBins, String name) {
        super(name);
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("The size of the cache must be > 0");
        }
        if (numBins < 0) {
            throw new IllegalArgumentException("The number of bins must be > 0");
        }
        myDataCache = new double[cacheSize];
        myNumBins = numBins;
        myCacheStatistic = new Statistic(getName() + " Cache Statistics");
    }

    /**
     * Returns a histogram based on the data or null if the cache limit has not
     * been reached.
     *
     * @return the histogram
     */
    public final Histogram getHistogram() {
        return myHistogram;
    }

    @Override
    public final void collect(double x) {
        if (Double.isNaN(x)) {
            myNumMissing++;
        }

        if (getSaveOption()) {
            save(x);
        }

        if (myCachingFlag == true) {
            myDataCache[myCountCache] = x;
            myCountCache++;
            myCacheStatistic.collect(x);
            if (myCountCache == myDataCache.length) {
                collectOnCache();
            }
        } else {
            myHistogram.collect(x);
        }
    }

    /**
     * When the cache is full this method is called to form the histogram
     *
     *
     */
    protected final void collectOnCache() {
        // turn off caching
        myCachingFlag = false;
        // need to set up histogram
        double LL = myCacheStatistic.getMin();
        double UL = myCacheStatistic.getMax();
        if (myNumBins == 0) { // user has not specified a number of bins
            // try to approximate a reasonable number of bins from the cache
            // first determine a reasonable bin width
            double s = myCacheStatistic.getStandardDeviation();
            double n = myCacheStatistic.getCount();
            // http://www.fmrib.ox.ac.uk/analysis/techrep/tr00mj2/tr00mj2/node24.html
            double width = 3.49 * s * Math.pow(n, -1.0 / 3.0);
            // now compute a number of bins for this with
            double nb = (UL - LL) / width;
            myNumBins = (int) Math.floor(nb + 0.5);
        }
        // form the histogram
        myHistogram = Histogram.makeHistogram(LL, UL, myNumBins);
        // collect on the cache
        for (int i = 0; i < myDataCache.length; i++) {
            myHistogram.collect(myDataCache[i]);
        }
        // clear cache
        myCacheStatistic.reset();
        myCacheStatistic = null;
        myDataCache = null;

    }

    @Override
    public final double getAverage() {
        if (myCachingFlag) {
            return myCacheStatistic.getAverage();
        } else {
            return myHistogram.getAverage();
        }
    }

    @Override
    public final double getConfidenceLevel() {
        if (myCachingFlag) {
            return myCacheStatistic.getConfidenceLevel();
        } else {
            return myHistogram.getConfidenceLevel();
        }
    }

    @Override
    public final double getCount() {
        if (myCachingFlag) {
            return myCacheStatistic.getCount();
        } else {
            return myHistogram.getCount();
        }
    }

    @Override
    public final double getDeviationSumOfSquares() {
        if (myCachingFlag) {
            return myCacheStatistic.getDeviationSumOfSquares();
        } else {
            return myHistogram.getDeviationSumOfSquares();
        }
    }

    @Override
    public final double getHalfWidth(double alpha) {
        if (myCachingFlag) {
            return myCacheStatistic.getHalfWidth(alpha);
        } else {
            return myHistogram.getHalfWidth(alpha);
        }
    }

    @Override
    public final double getKurtosis() {
        if (myCachingFlag) {
            return myCacheStatistic.getKurtosis();
        } else {
            return myHistogram.getKurtosis();
        }
    }

    @Override
    public final double getLag1Correlation() {
        if (myCachingFlag) {
            return myCacheStatistic.getLag1Correlation();
        } else {
            return myHistogram.getLag1Correlation();
        }
    }

    @Override
    public final double getLag1Covariance() {
        if (myCachingFlag) {
            return myCacheStatistic.getLag1Covariance();
        } else {
            return myHistogram.getLag1Covariance();
        }
    }

    @Override
    public final double getLastValue() {
        if (myCachingFlag) {
            return myCacheStatistic.getLastValue();
        } else {
            return myHistogram.getLastValue();
        }
    }

    @Override
    public final double getMax() {
        if (myCachingFlag) {
            return myCacheStatistic.getMax();
        } else {
            return myHistogram.getMax();
        }
    }

    @Override
    public final double getMin() {
        if (myCachingFlag) {
            return myCacheStatistic.getMin();
        } else {
            return myHistogram.getMin();
        }
    }

    @Override
    public final double getSkewness() {
        if (myCachingFlag) {
            return myCacheStatistic.getSkewness();
        } else {
            return myHistogram.getSkewness();
        }
    }

    @Override
    public final double getStandardDeviation() {
        if (myCachingFlag) {
            return myCacheStatistic.getStandardDeviation();
        } else {
            return myHistogram.getStandardDeviation();
        }
    }

    @Override
    public final double getStandardError() {
        if (myCachingFlag) {
            return myCacheStatistic.getStandardError();
        } else {
            return myHistogram.getStandardError();
        }
    }

    @Override
    public final double getSum() {
        if (myCachingFlag) {
            return myCacheStatistic.getSum();
        } else {
            return myHistogram.getSum();
        }
    }

    @Override
    public final double getVariance() {
        if (myCachingFlag) {
            return myCacheStatistic.getVariance();
        } else {
            return myHistogram.getVariance();
        }
    }

    @Override
    public final double getVonNeumannLag1TestStatistic() {
        if (myCachingFlag) {
            return myCacheStatistic.getVonNeumannLag1TestStatistic();
        } else {
            return myHistogram.getVonNeumannLag1TestStatistic();
        }
    }

    @Override
    public final double getVonNeumannLag1TestStatisticPValue(){
        return Normal.stdNormalComplementaryCDF(getVonNeumannLag1TestStatistic());
    }

    @Override
    public final int getLeadingDigitRule(double a) {
        if (myCachingFlag) {
            return myCacheStatistic.getLeadingDigitRule(a);
        } else {
            return myHistogram.getLeadingDigitRule(a);
        }
    }

    @Override
    public final void reset() {
        myNumMissing = 0.0;
        if (myCachingFlag) {
            myCacheStatistic.reset();
            for (int i = 0; i < myCountCache; i++) {
                myDataCache[i] = 0.0;
            }
            myCountCache = 0;
        } else {
            myHistogram.reset();
        }
        clearSavedData();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (myCachingFlag) {
            sb.append("Histogram: ");
            sb.append(getName());
            sb.append("\n");
            sb.append("-------------------------------------\n");
            sb.append("Caching is still on. Only within cache statistics available:\n");
            sb.append("Cache size = ");
            sb.append(myDataCache.length);
            sb.append("\n");
            sb.append("Number of observations cached: ");
            sb.append(myCountCache);
            sb.append("\n");
            sb.append(myCacheStatistic);
        } else {
            sb.append(myHistogram);
        }

        return (sb.toString());
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        ExponentialRV d = new ExponentialRV(2);
        CachedHistogram h = new CachedHistogram(50);
        for (int i = 1; i <= 100; ++i) {
            h.collect(d.getValue());
        }
        System.out.println(h);
        d.resetStartStream();
        // should not get out of caching
        h = new CachedHistogram(150);
        for (int i = 1; i <= 100; ++i) {
            h.collect(d.getValue());
        }
        System.out.println(h);
        // specify 10 bins
        d.resetStartStream();
        h = new CachedHistogram(50, 10);
        for (int i = 1; i <= 100; ++i) {
            h.collect(d.getValue());
        }
        System.out.println(h);
    }
}
