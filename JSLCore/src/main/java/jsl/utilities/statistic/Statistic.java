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

import java.util.Arrays;
import java.util.Objects;

import jsl.utilities.distributions.Normal;
import jsl.utilities.distributions.StudentT;

/**
 * The Statistic class allows the collection of summary statistics on data via
 * the collect() methods.  The primary statistical summary is for the statistical moments.
 */
public class Statistic extends AbstractStatistic {

    /**
     * Holds the minimum of the observed data.
     */
    protected double min;

    /**
     * Holds the maximum of the observed data
     */
    protected double max;

    /**
     * Holds the number of observations observed
     */
    protected double num;

    /**
     * Holds the last value observed
     */
    protected double myValue;

    /**
     * Holds the sum the lag-1 data, i.e. from the second data point on variable
     * for collecting lag1 covariance
     */
    protected double sumxx = 0.0;

    /**
     * Holds the first observed data point, needed for von-neuman statistic
     */
    protected double firstx = 0.0;

    /**
     * Holds the first 4 statistical central moments
     */
    protected double[] moments;

    /**
     * Holds sum = sum + j*x
     */
    protected double myJsum;

    /**
     * Creates a Statistic with name "null"
     */
    public Statistic() {
        this(null, null);
    }

    /**
     * Creates a Statistic with the given name
     *
     * @param name A String representing the name of the statistic
     */
    public Statistic(String name) {
        this(name, null);
    }

    /**
     * Creates a Statistic \based on the provided array
     *
     * @param values an array of values to collect statistics on
     */
    public Statistic(double[] values) {
        this(null, values);
    }

    /**
     * Creates a Statistic with the given name based on the provided array
     *
     * @param name   A String representing the name of the statistic
     * @param values an array of values to collect statistics on
     */
    public Statistic(String name, double[] values) {
        super(name);
        moments = new double[5];
        reset();
        if (values != null) {
            collect(values);
        }
    }

    /**
     * Returns a statistic that summarizes the passed in array of values
     *
     * @param x the values to compute statistics for
     * @return a Statistic summarizing the data
     */
    public static Statistic collectStatistics(double[] x) {
        Statistic s = new Statistic();
        s.collect(x);
        return (s);
    }

    /**
     * Creates a instance of Statistic that is a copy of the supplied Statistic
     * All internal state is the same (including whether the collection is
     * on or off) and the collection rule. The only exception is for the id of the returned Statistic
     * If this statistic has saved data, the new instance will also have that data.
     *
     * @param stat the stat to copy
     * @return a copy of the supplied Statistic
     */
    public static Statistic newInstance(Statistic stat) {
        Statistic s = new Statistic();
        s.myNumMissing = stat.myNumMissing;
        s.firstx = stat.firstx;
        s.max = stat.max;
        s.min = stat.min;
        s.myConfidenceLevel = stat.myConfidenceLevel;
        s.myJsum = stat.myJsum;
        s.myValue = stat.myValue;
        s.setName(stat.getName());
        s.num = stat.num;
        s.sumxx = stat.sumxx;
        s.moments = Arrays.copyOf(stat.moments, stat.moments.length);

        if (stat.getSaveOption()) {
            s.save(stat.getSavedData());
            s.setSaveOption(true);
        }
        return (s);
    }

    /**
     * Creates a instance of Statistic that is a copy of the supplied Statistic
     * All internal state is the same (including whether the collection is
     * on or off) and the collection rule. The only exception is for the id of the returned Statistic.
     * If this statistic has saved data, the new instance will also have that data.
     *
     * @return a copy of the supplied Statistic
     */
    public final Statistic newInstance() {
        Statistic s = new Statistic();
        s.myNumMissing = myNumMissing;
        s.firstx = firstx;
        s.max = max;
        s.min = min;
        s.myConfidenceLevel = myConfidenceLevel;
        s.myJsum = myJsum;
        s.myValue = myValue;
        s.setName(getName());
        s.num = num;
        s.sumxx = sumxx;
        s.moments = Arrays.copyOf(moments, moments.length);
        if (getSaveOption()) {
            s.save(getSavedData());
            s.setSaveOption(true);
        }
        return (s);
    }

    /**
     * Returns the index associated with the minimum element in the array For
     * ties, this returns the first found
     *
     * @param x the array of data
     * @return the index associated with the minimum element
     */
    public static int getIndexOfMin(double[] x) {
        Objects.requireNonNull(x, "The supplied array was null");
        int index = 0;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] < min) {
                min = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array of data
     * @return the minimum value in the array
     */
    public static double getMin(double[] x) {
        Objects.requireNonNull(x, "The supplied array was null");
        return x[getIndexOfMin(x)];
    }

    /**
     * Returns the index associated with the maximum element in the array For
     * ties, this returns the first found
     *
     * @param x the array of data
     * @return the index associated with the maximum element
     */
    public static int getIndexOfMax(double[] x) {
        Objects.requireNonNull(x, "The supplied array was null");
        int index = 0;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
                index = i;
            }
        }
        return (index);
    }

    /**
     * @param x the array of data
     * @return the maximum value in the array
     */
    public static double getMax(double[] x) {
        Objects.requireNonNull(x, "The supplied array was null");
        return x[getIndexOfMax(x)];
    }

    /**
     * Returns the median of the data. The array is sorted
     *
     * @param data the array of data, must not be null
     * @return the median of the data
     */
    public static double getMedian(double[] data) {
        Objects.requireNonNull(data, "The supplied array was null");
        Arrays.sort(data);
        int size = data.length;
        double median = -1;
        if (size % 2 == 0) {//even
            int firstIndex = (size / 2) - 1;
            int secondIndex = firstIndex + 1;
            double firstValue = data[firstIndex];
            double secondValue = data[secondIndex];
            median = (firstValue + secondValue) / 2.0;
        } else {//odd
            int index = (int) Math.ceil(size / 2.0);
            median = data[index];
        }
        return median;
    }

    /**
     * @param data the data to count
     * @param x    the ordinate to check
     * @return the number of data points less than or equal to x
     */
    public static int countLessEqualTo(double[] data, double x) {
        Objects.requireNonNull(data, "The supplied array was null");
        int cnt = 0;
        for (double datum : data) {
            if (datum <= x) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * @param data the data to count
     * @param x    the ordinate to check
     * @return the number of data points less than x
     */
    public static int countLessThan(double[] data, double x) {
        Objects.requireNonNull(data, "The supplied array was null");
        int cnt = 0;
        for (double datum : data) {
            if (datum < x) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * @param data the data to count
     * @param x    the ordinate to check
     * @return the number of data points greater than or equal to x
     */
    public static int countGreaterEqualTo(double[] data, double x) {
        Objects.requireNonNull(data, "The supplied array was null");
        int cnt = 0;
        for (double datum : data) {
            if (datum >= x) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * @param data the data to count
     * @param x    the ordinate to check
     * @return the number of data points greater than x
     */
    public static int countGreaterThan(double[] data, double x) {
        Objects.requireNonNull(data, "The supplied array was null");
        int cnt = 0;
        for (double datum : data) {
            if (datum > x) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * @param data the data to sort
     * @return a copy of the sorted array in ascending order representing the order statistics
     */
    public static double[] getOrderStatistics(double[] data) {
        Objects.requireNonNull(data, "The supplied array was null");
        double[] doubles = Arrays.copyOf(data, data.length);
        Arrays.sort(doubles);
        return doubles;
    }

    @Override
    public final double getCount() {
        return (moments[0]);
    }

    @Override
    public final double getSum() {
        return (moments[1] * moments[0]);
    }

    @Override
    public final double getAverage() {
        if (moments[0] < 1.0) {
            return Double.NaN;
        }
        return (moments[1]);
    }

    /**
     * Returns the 2nd statistical central moment
     *
     * @return the 2nd statistical central moment
     */
    public final double get2ndCentralMoment() {
        return moments[2];
    }

    /**
     * Returns the 3rd statistical central moment
     *
     * @return the 3rd statistical central moment
     */
    public final double get3rdCentralMoment() {
        return moments[3];
    }

    /**
     * Returns the 4th statistical central moment
     *
     * @return the 4th statistical central moment
     */
    public final double get4thCentralMoment() {
        return moments[4];
    }

    /**
     * The 0th moment is the count, the 1st central moment zero,
     * the 2nd, 3rd, and 4th central moments
     *
     * @return an array holding the central moments, 0, 1, 2, 3, 4
     */
    public final double[] getCentralMoments() {
        return Arrays.copyOf(moments, moments.length);
    }

    /**
     * Returns the 2nd statistical raw moment (about zero)
     *
     * @return the 2nd statistical raw moment (about zero)
     */
    public final double get2ndRawMoment() {
        double mu = getAverage();
        return moments[2] + mu * mu;
    }

    /**
     * Returns the 3rd statistical raw moment (about zero)
     *
     * @return the 3rd statistical raw moment (about zero)
     */
    public final double get3rdRawMoment() {
        double m3 = get3rdCentralMoment();
        double mr2 = get2ndRawMoment();
        double mu = getAverage();
        return m3 + 3.0 * mu * mr2 - 2.0 * mu * mu * mu;
    }

    /**
     * Returns the 4th statistical raw moment (about zero)
     *
     * @return the 4th statistical raw moment (about zero)
     */
    public final double get4thRawMoment() {
        double m4 = get4thCentralMoment();
        double mr3 = get3rdRawMoment();
        double mr2 = get2ndRawMoment();
        double mu = getAverage();
        return m4 + 4.0 * mu * mr3 - 6.0 * mu * mu * mr2 + 3.0 * mu * mu * mu * mu;
    }

    @Override
    public final double getDeviationSumOfSquares() {
        return (moments[2] * moments[0]);
    }

    @Override
    public final double getVariance() {
        if (moments[0] < 2) {
            return Double.NaN;
        }

        return (getDeviationSumOfSquares() / (moments[0] - 1.0));
    }

    @Override
    public final double getMin() {
        return (min);
    }

    @Override
    public final double getMax() {
        return (max);
    }

    @Override
    public final double getLastValue() {
        return (myValue);
    }

    @Override
    public final double getKurtosis() {
        if (moments[0] < 4) {
            return (Double.NaN);
        }

        double n = moments[0];
        double n1 = n - 1.0;
        double v = getVariance();
        double d = (n - 1.0) * (n - 2.0) * (n - 3.0) * v * v;
        double t = n * (n + 1.0) * n * moments[4] - 3.0 * n1 * n1 * n1 * v * v;
        double k = (double) t / (double) d;
        return (k);
    }

    @Override
    public final double getSkewness() {
        if (moments[0] < 3) {
            return (Double.NaN);
        }

        double n = moments[0];
        double v = getVariance();
        double s = Math.sqrt(v);
        double d = (n - 1.0) * (n - 2.0) * v * s;
        double t = n * n * moments[3];

        double k = (double) t / (double) d;
        return (k);
    }

    /**
     * Checks if the supplied value falls within getAverage() +/- getHalfWidth()
     *
     * @param mean the value to check
     * @return true if the supplied value falls within getAverage() +/-
     * getHalfWidth()
     */
    public final boolean checkMean(double mean) {
        double a = getAverage();
        double hw = getHalfWidth();
        double ll = a - hw;
        double ul = a + hw;
        if ((ll <= mean) && (mean <= ul)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the half-width for a confidence interval on the mean with
     * confidence level  based on StudentT distribution
     *
     * @param level the confidence level
     * @return the half-width
     */
    @Override
    public double getHalfWidth(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        if (getCount() <= 1.0) {
            return (Double.NaN);
        }
        double dof = getCount() - 1.0;
        double alpha = 1.0 - level;
        double p = 1.0 - alpha / 2.0;
        double t = StudentT.getInvCDF(dof, p);
        double hw = t * getStandardError();
        return hw;
    }

    @Override
    public final double getStandardError() {
        if (moments[0] < 1.0) {
            return (Double.NaN);
        }
        return (getStandardDeviation() / Math.sqrt(moments[0]));
    }

    @Override
    public final int getLeadingDigitRule(double a) {
        return (int) Math.floor(Math.log10(a * getStandardError()));
    }

    @Override
    public final double getLag1Covariance() {
        if (num > 2.0) {
            double c1 = sumxx - (num + 1.0) * moments[1] * moments[1] + moments[1] * (firstx + myValue);
            return (c1 / num);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getLag1Correlation() {
        if (num > 2.0) {
            return (getLag1Covariance() / moments[2]);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getVonNeumannLag1TestStatistic() {
        if (num > 2.0) {
            double r1 = getLag1Correlation();
            double t = (firstx - moments[1]) * (firstx - moments[1]) + (myValue - moments[1]) * (myValue - moments[1]);
            double b = 2.0 * num * moments[2];
            double v = Math.sqrt((num * num - 1.0) / (num - 2.0)) * (r1 + (t / b));
            return (v);
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final double getVonNeumannLag1TestStatisticPValue() {
        return Normal.stdNormalComplementaryCDF(getVonNeumannLag1TestStatistic());
    }

    /**
     * Returns the observation weighted sum of the data i.e. sum = sum + j*x
     * where j is the observation number and x is jth observation
     *
     * @return the observation weighted sum of the data
     */
    public final double getObsWeightedSum() {
        return (myJsum);
    }

    @Override
    public void collect(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            myNumMissing++;
            return;
        }
        if (getSaveOption()) {
            save(x);
        }
        double n, n1, n2, delta, d2, d3, r1;

        // update moments
        num = num + 1;
        myJsum = myJsum + num * x;

        n = moments[0];
        n1 = n + 1.0;
        n2 = n * n;
        delta = (moments[1] - x) / n1;
        d2 = delta * delta;
        d3 = delta * d2;
        r1 = n / n1;
        moments[4] = (1.0 + n * n2) * d2 * d2 + 6.0 * moments[2] * d2 + 4.0 * moments[3] * delta + moments[4];
        moments[4] *= r1;

        moments[3] = (1.0 - n2) * d3 + 3.0 * moments[2] * delta + moments[3];
        moments[3] *= r1;

        moments[2] = (1.0 + n) * d2 + moments[2];
        moments[2] *= r1;

        moments[1] = moments[1] - delta;
        moments[0] = n1;

        // to collect lag 1 cov, we need x(1)
        if (num == 1.0) {
            firstx = x;
        }

        // to collect lag 1 cov, we must provide new x and previous x
        // to collect lag 1 cov, we must sum x(i) and x(i+1)
        if (num >= 2.0) {
            sumxx = sumxx + x * myValue;
        }

        // update min, max, current value, current weight
        if (x > max) {
            max = x;
        }
        if (x < min) {
            min = x;
        }
        myValue = x;
    }

    @Override
    public void reset() {
        myValue = Double.NaN;
        myNumMissing = 0.0;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        num = 0.0;
        myJsum = 0.0;
        sumxx = 0.0;

        for (int i = 0; i < moments.length; i++) {
            moments[i] = 0.0;
        }

        clearSavedData();
    }

    public String asString() {
        final StringBuilder sb = new StringBuilder("Statistic{");
        sb.append("name='").append(getName()).append('\'');
        sb.append(", n=").append(getCount());
        sb.append(", avg=").append(getAverage());
        sb.append(", sd=").append(getStandardDeviation());
        sb.append(", ci=").append(getConfidenceInterval().toString());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID ");
        sb.append(getId());
        sb.append(System.lineSeparator());
        sb.append("Name ");
        sb.append(getName());
        sb.append(System.lineSeparator());
        sb.append("Number ");
        sb.append(getCount());
        sb.append(System.lineSeparator());
        sb.append("Average ");
        sb.append(getAverage());
        sb.append(System.lineSeparator());
        sb.append("Standard Deviation ");
        sb.append(getStandardDeviation());
        sb.append(System.lineSeparator());
        sb.append("Standard Error ");
        sb.append(getStandardError());
        sb.append(System.lineSeparator());
        sb.append("Half-width ");
        sb.append(getHalfWidth());
        sb.append(System.lineSeparator());
        sb.append("Confidence Level ");
        sb.append(getConfidenceLevel());
        sb.append(System.lineSeparator());
        sb.append("Confidence Interval ");
        sb.append(getConfidenceInterval());
        sb.append(System.lineSeparator());
        sb.append("Minimum ");
        sb.append(getMin());
        sb.append(System.lineSeparator());
        sb.append("Maximum ");
        sb.append(getMax());
        sb.append(System.lineSeparator());
        sb.append("Sum ");
        sb.append(getSum());
        sb.append(System.lineSeparator());
        sb.append("Variance ");
        sb.append(getVariance());
        sb.append(System.lineSeparator());
        sb.append("Deviation Sum of Squares ");
        sb.append(getDeviationSumOfSquares());
        sb.append(System.lineSeparator());
        sb.append("Last value collected ");
        sb.append(getLastValue());
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Kurtosis ");
        sb.append(getKurtosis());
        sb.append(System.lineSeparator());
        sb.append("Skewness ");
        sb.append(getSkewness());
        sb.append(System.lineSeparator());
        sb.append("Lag 1 Covariance ");
        sb.append(getLag1Covariance());
        sb.append(System.lineSeparator());
        sb.append("Lag 1 Correlation ");
        sb.append(getLag1Correlation());
        sb.append(System.lineSeparator());
        sb.append("Von Neumann Lag 1 Test Statistic ");
        sb.append(getVonNeumannLag1TestStatistic());
        sb.append(System.lineSeparator());
        sb.append("Number of missing observations ");
        sb.append(getNumberMissing());
        sb.append(System.lineSeparator());
        sb.append("Lead-Digit Rule(1) ");
        sb.append(getLeadingDigitRule(1.0));
        sb.append(System.lineSeparator());
        return (sb.toString());
    }

    /**
     * Returns the summary statistics values Name Count Average Std. Dev.
     *
     * @return the string of summary statistics
     */
    public String getSummaryStatistics() {
        String format = "%-50s \t %12d \t %12f \t %12f %n";
        int n = (int) getCount();
        double avg = getAverage();
        double std = getStandardDeviation();
        String name = getName();
        return String.format(format, name, n, avg, std);
    }

    /**
     * Returns the header for the summary statistics Name Count Average Std.
     * Dev.
     *
     * @return the header
     */
    public String getSummaryStatisticsHeader() {
        return String.format("%-50s \t %12s \t %12s \t %12s %n", "Name", "Count", "Average", "Std. Dev.");
    }

    /**
     * Estimates the number of observations needed in order to obtain a
     * getConfidenceLevel() confidence interval with plus/minus the provided
     * half-width
     *
     * @param desiredHW the desired half-width, must be greater than zero
     * @return the estimated sample size
     */
    public final long estimateSampleSize(double desiredHW) {
        if (desiredHW <= 0.0) {
            throw new IllegalArgumentException("The desired half-width must be > 0");
        }
        double cl = this.getConfidenceLevel();
        double a = 1.0 - cl;
        double a2 = a / 2.0;
        double z = Normal.stdNormalInvCDF(1.0 - a2);
        double s = getStandardDeviation();
        double m = (z * s / desiredHW) * (z * s / desiredHW);
        return Math.round(m + .5);
    }

    /**
     * Estimate the sample size based on a normal approximation
     *
     * @param desiredHW the desired half-width (must be bigger than 0)
     * @param stdDev    the standard deviation (must be bigger than or equal to 0)
     * @param level     the confidence level (must be between 0 and 1)
     * @return the estimated sample size
     */
    public static long estimateSampleSize(double desiredHW, double stdDev, double level) {
        if (desiredHW <= 0.0) {
            throw new IllegalArgumentException("The desired half-width must be > 0");
        }
        if (stdDev < 0.0) {
            throw new IllegalArgumentException("The desired std. dev. must be >= 0");
        }
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        double a = 1.0 - level;
        double a2 = a / 2.0;
        double z = Normal.stdNormalInvCDF(1.0 - a2);
        double m = (z * stdDev / desiredHW) * (z * stdDev / desiredHW);
        return Math.round(m + .5);
    }
}
