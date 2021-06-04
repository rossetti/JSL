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

import jsl.utilities.Identity;

/**
 * Collects a basic weighted statistical summary.  If the observation or the weight is
 * infinite or NaN, then the observation is not recorded and the number of missing observations
 * is incremented. If the observed weight is negative or 0.0, then the observation is not recorded and
 * the number of missing observations is incremented.
 *
 * @author rossetti
 */
public class WeightedStatistic implements CollectorIfc, WeightedStatisticIfc {

    /**
     * Used to count the number of missing data points presented When a data
     * point having the value of (Double.NaN, Double.POSITIVE_INFINITY,
     * Double.NEGATIVE_INFINITY) are presented it is excluded from the summary
     * statistics and the number of missing points is noted. Implementers of
     * subclasses are responsible for properly collecting this value and
     * resetting this value.
     * <p>
     */
    private double myNumMissing = 0.0;

    /**
     * Holds the minimum of the observed data.
     */
    private double min = Double.POSITIVE_INFINITY;

    /**
     * Holds the maximum of the observed data
     */
    private double max = Double.NEGATIVE_INFINITY;

    /**
     * Holds the number of observations observed
     */
    private double num = 0.0;

    /**
     * Holds the weighted sum of the data.
     */
    private double wsum = 0.0;

    /**
     * Holds the unweighted sum of the data
     */
    private double uwsum = 0.0;

    /**
     * Holds the weighted sum of squares of the data.
     */
    private double wsumsq = 0.0;

    /**
     * Holds the sum of the weights observed.
     */
    private double sumw = 0.0;

    /**
     * Holds the last value observed
     */
    private double myValue;

    /**
     * Holds the last weight observed
     */
    private double myWeight;

    private final Identity myIdentity;

    /**
     *
     */
    public WeightedStatistic() {
        this(null);
    }

    /**
     * @param name the name of the statistic
     */
    public WeightedStatistic(String name) {
        myIdentity = new Identity(name);
        reset();
    }

    /**
     * @param x      the value to collect
     * @param weight the weight associated with the value
     */
    public final void collect(double x, double weight) {
        if (Double.isNaN(x) || Double.isInfinite(x) || Double.isNaN(weight) || Double.isInfinite(weight) || (weight <= 0.0)) {
            myNumMissing++;
            return;
        }

        // update moments
        num = num + 1.0;
        sumw = sumw + weight;
        uwsum = uwsum + x;
        wsum = wsum + x * weight;
        wsumsq = wsumsq + x * x * weight;

        // update min, max, current value, current weight
        if (x > max) {
            max = x;
        }
        if (x < min) {
            min = x;
        }
        myValue = x;
        myWeight = weight;
    }

    /**
     * Collects the passed in arrays. The lengths of
     * the arrays must be the same.
     *
     * @param x the values
     * @param w the weights
     */
    public void collect(double[] x, double[] w) {
        if (x.length != w.length) {
            throw new IllegalArgumentException("The supplied arrays are not of equal length");
        }
        for (int i = 0; i < x.length; i++) {
            collect(x[i], w[i]);
        }
    }

    /**
     * Returns a statistic that summarizes the passed in arrays. The lengths of
     * the arrays must be the same.
     *
     * @param x the values
     * @param w the weights
     * @return a weighted statistic based on the arrays
     */
    public static WeightedStatistic collectStatistics(double[] x, double[] w) {
        if (x.length != w.length) {
            throw new IllegalArgumentException("The supplied arrays are not of equal length");
        }
        WeightedStatistic s = new WeightedStatistic();
        s.collect(x, w);
        return (s);
    }

    /**
     * Creates a instance of Statistic that is a copy of the supplied Statistic
     * All internal state is the same except for the id of the returned
     * Statistic
     *
     * @param stat the instance that needs to be copied
     * @return the copy
     */
    public static WeightedStatistic newInstance(WeightedStatistic stat) {
        WeightedStatistic s = new WeightedStatistic();
        s.max = stat.max;
        s.min = stat.min;
        s.setName(stat.getName());
        s.num = stat.num;
        s.sumw = stat.sumw;
        s.wsum = stat.wsum;
        s.wsumsq = stat.wsumsq;
        s.myValue = stat.myValue;
        s.myWeight = stat.myWeight;
        s.uwsum = stat.uwsum;
        return (s);
    }

    /**
     * Creates a instance of Statistic that is a copy of this Statistic All
     * internal state is the same except for the id of the returned Statistic
     *
     * @return a new instance based on the current state of this instance
     */
    public final WeightedStatistic newInstance() {
        WeightedStatistic s = new WeightedStatistic();
        s.max = max;
        s.min = min;
        s.setName(getName());
        s.num = num;
        s.sumw = sumw;
        s.wsum = wsum;
        s.wsumsq = wsumsq;
        s.myValue = myValue;
        s.myWeight = myWeight;
        s.uwsum = uwsum;
        return (s);
    }

    @Override
    public void collect(double value) {
        collect(value, 1.0);
    }

    @Override
    public final void reset() {
        myValue = Double.NaN;
        myWeight = Double.NaN;
        num = 0.0;
        wsum = 0.0;
        sumw = 0.0;
        wsumsq = 0.0;
        uwsum = 0.0;
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        myNumMissing = 0.0;
    }

    @Override
    public final double getLastValue() {
        return (myValue);
    }

    @Override
    public final double getLastWeight() {
        return (myWeight);
    }

    @Override
    public final double getAverage() {
        if (sumw <= 0.0) {
            return Double.NaN;
        }
        return (wsum / sumw);
    }

    @Override
    public final double getCount() {
        return (num);
    }

    @Override
    public final double getWeightedSum() {
        return (wsum);
    }

    @Override
    public final double getSumOfWeights() {
        return (sumw);
    }

    @Override
    public final double getWeightedSumOfSquares() {
        return wsumsq;
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
    public double getNumberMissing() {
        return (myNumMissing);
    }

    @Override
    public final double getUnWeightedSum() {
        return uwsum;
    }

    /**
     * Returns a String representation of the WeightedStatistic
     *
     * @return A String with basic summary statistics
     */
    @Override
    public final String toString() {
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

        sb.append("Minimum ");
        sb.append(getMin());
        sb.append(System.lineSeparator());

        sb.append("Maximum ");
        sb.append(getMax());
        sb.append(System.lineSeparator());

        sb.append("Weighted Average ");
        sb.append(getAverage());
        sb.append(System.lineSeparator());

        sb.append("Weighted Sum ");
        sb.append(getWeightedSum());
        sb.append(System.lineSeparator());

        sb.append("Weighted Sum of Squares ");
        sb.append(getWeightedSumOfSquares());
        sb.append(System.lineSeparator());

        sb.append("Sum of Weights ");
        sb.append(getSumOfWeights());
        sb.append(System.lineSeparator());

        sb.append("Unweighted Sum ");
        sb.append(getUnWeightedSum());
        sb.append(System.lineSeparator());

        sb.append("Unweighted Average ");
        sb.append(getUnWeightedAverage());
        sb.append(System.lineSeparator());

        sb.append("Number Missing ");
        sb.append(getNumberMissing());
        sb.append(System.lineSeparator());

        sb.append("Last Value ");
        sb.append(getLastValue());
        sb.append(System.lineSeparator());

        sb.append("Last Weight ");
        sb.append(getLastWeight());
        sb.append(System.lineSeparator());

        return (sb.toString());
    }

    /**
     * Fills up the supplied array with the statistics defined by index =
     * statistic
     * <p>
     * statistics[0] = getCount();
     * <p>
     * statistics[1] = getAverage();
     * <p>
     * statistics[2] = getMin();
     * <p>
     * statistics[3] = getMax();
     * <p>
     * statistics[4] = getWeightedSum();
     * <p>
     * statistics[5] = getSumOfWeights();
     * <p>
     * statistics[6] = getWeightedSumOfSquares();
     * <p>
     * statistics[7] = getLastValue();
     * <p>
     * statistics[8] = getLastWeight();
     * <p>
     * statistics[9] = getUnWeightedSum();
     * <p>
     * statistics[10] = getUnWeightedAverage();
     * <p>
     * <p>
     * The array must be of size 9 or an exception will be thrown
     *
     * @param statistics the array to fill
     */
    public final void getStatistics(double[] statistics) {
        if (statistics.length != 11) {
            throw new IllegalArgumentException("The supplied array was not of size 7");
        }

        statistics[0] = getCount();
        statistics[1] = getAverage();
        statistics[2] = getMin();
        statistics[3] = getMax();
        statistics[4] = getWeightedSum();
        statistics[5] = getSumOfWeights();
        statistics[6] = getWeightedSumOfSquares();
        statistics[7] = getLastValue();
        statistics[8] = getLastWeight();
        statistics[9] = getUnWeightedSum();
        statistics[10] = getUnWeightedAverage();
    }

    /**
     * Returns an array with the statistics defined by index = statistic
     * <p>
     * statistics[0] = getCount();
     * <p>
     * statistics[1] = getAverage();
     * <p>
     * statistics[2] = getMin();
     * <p>
     * statistics[3] = getMax();
     * <p>
     * statistics[4] = getSum();
     * <p>
     * statistics[5] = getSumOfWeights();
     * <p>
     * statistics[6] = getWeightedSumOfSquares();
     * <p>
     * statistics[7] = getLastValue();
     * <p>
     * statistics[8] = getLastWeight();
     * <p>
     * statistics[9] = getUnWeightedSum();
     * <p>
     * statistics[10] = getUnWeightedAverage();
     * <p>
     *
     * @return the array of statistics
     */
    public final double[] getStatistics() {
        double[] x = new double[11];
        getStatistics(x);
        return (x);
    }

    /**
     * s[0] = "Count"; s[1] = "Average"; s[2] = "Minimum"; s[3] = "Maximum";
     * s[4] = "Weighted Sum"; s[5] = "Sum of Weights"; s[6] = "Weighted sum of
     * squares"; s[7] = "Last Value"; s[8] = "Last Weight"; s[9] = "Unweighted Sum"; s[10] = "Unweighted Average";
     *
     * @return the headers
     */
    public String[] getStatisticsHeader() {
        String[] s = new String[11];
        s[0] = "Count";
        s[1] = "Average";
        s[2] = "Minimum";
        s[3] = "Maximum";
        s[4] = "Weighted Sum";
        s[5] = "Sum of Weights";
        s[6] = "Weighted sum of squares";
        s[7] = "Last Value";
        s[8] = "Last Weight";
        s[9] = "Unweighted Sum";
        s[10] = "Unweighted Average";
        return s;
    }

    @Override
    public String getCSVStatistic() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(",");
        double[] stats = getStatistics();
        for (int i = 0; i < stats.length; i++) {
            if (Double.isNaN(stats[i]) || Double.isInfinite(stats[i])) {
                sb.append("");
            } else {
                sb.append(stats[i]);
            }
            if (i < stats.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Override
    public String getCSVStatisticHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistic Name,");
        sb.append("Count,");
        sb.append("Average,");
        sb.append("Minimum,");
        sb.append("Maximum,");
        sb.append("Weighted Sum,");
        sb.append("Sum of Weights,");
        sb.append("Weighted sum of squares,");
        sb.append("Last Value,");
        sb.append("Last Weight,");
        sb.append("Unweighted Sum,");
        sb.append("Unweighted Average");
        return sb.toString();
    }

    @Override
    public final String getName() {
        return myIdentity.getName();
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        myIdentity.setName(str);
    }

    @Override
    public final int getId() {
        return (myIdentity.getId());
    }

}
