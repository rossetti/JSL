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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.utilities.statistic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jsl.utilities.GetNameIfc;
import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.distributions.Gamma;
import jsl.utilities.distributions.Normal;
import jsl.utilities.reporting.StatisticReporter;

/**
 * Holds data to perform multiple comparisons Performs pairwise comparisons and
 * computes pairwise differences and variances.
 * <p>
 * The user must supply the data samples over which the comparison will be made.
 * This is supplied in a Map with key representing a name (identifier) for the
 * data and an array representing the observations. This class computes all the
 * pairwise differences and the variances of the differences in the form of
 * tabulated statistics.
 *
 * @author rossetti
 */
public class MultipleComparisonAnalyzer implements GetNameIfc {

    private LinkedHashMap<String, double[]> myDataMap;

    private int myDataSize;

    //TODO use guava table
    private LinkedHashMap<String, LinkedHashMap<String, double[]>> myPairDiffs;

    private LinkedHashMap<String, LinkedHashMap<String, Statistic>> myPairDiffStats;

    private String myName;

    private double myDefaultIndifferenceZone;

    public MultipleComparisonAnalyzer(Map<String, double[]> dataMap) {
        setDataMap(dataMap);
        myDefaultIndifferenceZone = 0.0;
    }

    /**
     * @return the default indifference zone parameter
     */
    public final double getDefaultIndifferenceZone() {
        return myDefaultIndifferenceZone;
    }

    /**
     * Sets the default indifference zone parameter
     *
     * @param defaultIndifferenceZone must be greater than or equal to zero
     */
    public final void setDefaultIndifferenceZone(double defaultIndifferenceZone) {
        if (defaultIndifferenceZone < 0.0) {
            throw new IllegalArgumentException("The indifference zone parameter must be >= 0.0");
        }
        myDefaultIndifferenceZone = defaultIndifferenceZone;
    }

    @Override
    public final String getName() {
        return myName;
    }

    /**
     * @param name the name of the comparison
     */
    public final void setName(String name) {
        myName = name;
    }

    /**
     * The names of items being compared as an array of strings
     *
     * @return names of items being compared as an array of strings
     */
    public String[] getDataNames() {
        String[] names = new String[myDataMap.keySet().size()];
        int i = 0;
        for (String s : myDataMap.keySet()) {
            names[i] = s;
            i++;
        }
        return (names);
    }

    /**
     * The number of data sets stored in the analyzer. There is a data set
     * stored for each name.
     *
     * @return number of data sets stored in the analyzer.
     */
    public int getNumberDatasets() {
        return myDataMap.keySet().size();
    }

    /**
     * Returns true if the analyzer has data for the name
     *
     * @param dataName the name to check
     * @return true if the analyzer has data for the name
     */
    public boolean contains(String dataName) {
        return myDataMap.containsKey(dataName);
    }

    /**
     * Sets the underlying data map. Any data already in the analyzer will be
     * replaced. The supplied dataMap must not be null. There needs to be at
     * least 2 data arrays The length of each data array must be the same.
     *
     * @param dataMap the map to read from
     */
    public final void setDataMap(Map<String, double[]> dataMap) {
        if (dataMap == null) {
            throw new IllegalArgumentException("The supplied data map was null");
        }

        if (dataMap.keySet().size() <= 1) {
            throw new IllegalArgumentException("There must be 2 or more data arrays");
        }

        if (checkLengths(dataMap) == false) {
            throw new IllegalArgumentException("The data arrays do not have all the same lengths");
        }

        myDataMap = new LinkedHashMap<String, double[]>();
        for (String s : dataMap.keySet()) {
            double[] x = dataMap.get(s);
            myDataSize = x.length;
            double[] d = new double[x.length];
            System.arraycopy(x, 0, d, 0, x.length);
            myDataMap.put(s, d);
        }

        myPairDiffs = computePairedDifferences();
        myPairDiffStats = computePairedDifferenceStatistics();
    }

    /**
     * The key to each LinkedHashMap is the name of the data The Statistic is
     * based on the paired differences
     *
     * @return a map of the paired difference statistics
     */
    public LinkedHashMap<String, LinkedHashMap<String, Statistic>> computePairedDifferenceStatistics() {
        LinkedHashMap<String, LinkedHashMap<String, Statistic>> pd = new LinkedHashMap<>();
        //TODO use guava table
        int i = 1;
        for (String fn : myDataMap.keySet()) {
            int j = 1;
            if (i < myDataMap.keySet().size()) {
                LinkedHashMap<String, Statistic> m = new LinkedHashMap<>();
                pd.put(fn, m);
                for (String sn : myDataMap.keySet()) {
                    if (i < j) {
                        double[] fd = myDataMap.get(fn);
                        double[] sd = myDataMap.get(sn);
                        double[] d = computeDifference(fd, sd);
                        m.put(sn, new Statistic(fn + " - " + sn, d));
                    }
                    j++;
                }
            }
            i++;
        }
        return pd;
    }

    /**
     * The key to each LinkedHashMap is the name of the data The array contains
     * the paired differences
     *
     * @return a map holding the paired differences as an array for each data name
     */
    public LinkedHashMap<String, LinkedHashMap<String, double[]>> computePairedDifferences() {
        //TODO use guava's Table
        LinkedHashMap<String, LinkedHashMap<String, double[]>> pd = new LinkedHashMap<>();
        int i = 1;
        for (String fn : myDataMap.keySet()) {
            int j = 1;
            if (i < myDataMap.keySet().size()) {
                LinkedHashMap<String, double[]> m = new LinkedHashMap<>();
                pd.put(fn, m);
                for (String sn : myDataMap.keySet()) {
                    if (i < j) {
                        double[] fd = myDataMap.get(fn);
                        double[] sd = myDataMap.get(sn);
                        double[] d = computeDifference(fd, sd);
                        m.put(sn, d);
                    }
                    j++;
                }
            }
            i++;
        }
        return pd;
    }

    /**
     * The paired differences as a array for the pair of data names given by the
     * strings. If the data names don't exist a null pointer exception will
     * occur
     *
     * @param s1 the name of data set number 1
     * @param s2 the name of data set number 2
     * @return an array of paired differences
     */
    public double[] getPairedDifference(String s1, String s2) {
        LinkedHashMap<String, double[]> g = myPairDiffs.get(s1);
        double[] x = g.get(s2);
        double[] d = new double[x.length];
        System.arraycopy(x, 0, d, 0, x.length);
        return d;
    }

    /**
     * A list holding the statistics for all of the pairwise differences is
     * returned
     *
     * @return A list holding the statistics for all of the pairwise differences
     */
    public List<StatisticAccessorIfc> getPairedDifferenceStatistics() {
        List<StatisticAccessorIfc> list = new ArrayList<>();
        for (String f : myPairDiffStats.keySet()) {
            LinkedHashMap<String, Statistic> g = myPairDiffStats.get(f);
            for (String s : g.keySet()) {
                list.add(getPairedDifferenceStatistic(f, s));
            }
        }
        return list;
    }

    /**
     * The statistics for the pair of data names given by the strings. If the
     * data names don't exist a null pointer exception will occur
     *
     * @param s1 the name of data set number 1
     * @param s2 the name of data set number 2
     * @return a Statistic collected over the paired differences
     */
    public Statistic getPairedDifferenceStatistic(String s1, String s2) {
        LinkedHashMap<String, Statistic> g = myPairDiffStats.get(s1);
        Statistic stat = g.get(s2);
        return stat.newInstance();
    }

    /**
     * Each paired difference is labeled with data name i - data name j for all
     * i, j The returns the names as an array of strings
     *
     * @return the names as an array of strings
     */
    public String[] getNamesOfPairedDifferences() {
        List<StatisticAccessorIfc> list = getPairedDifferenceStatistics();
        String[] names = new String[list.size()];
        int i = 0;
        for (StatisticAccessorIfc s : list) {
            names[i] = s.getName();
            i++;
        }
        return names;
    }

    /**
     * The name of the maximum average difference
     *
     * @return name of the maximum average difference
     */
    public String getNameOfMaximumAverageOfDifferences() {
        return getNamesOfPairedDifferences()[getIndexOfMaximumOfAveragesOfDifferences()];
    }

    /**
     * The name of the minimum average difference
     *
     * @return name of the minimum average difference
     */
    public String getNameOfMinumumAverageOfDifferences() {
        return getNamesOfPairedDifferences()[getIndexOfMinimumOfAveragesOfDifferences()];
    }

    /**
     * The actual maximum average of the differences
     *
     * @return actual maximum average of the differences
     */
    public double getMaximumOfAveragesOfDifferences() {
        return Statistic.getMax(getAveragesOfDifferences());
    }

    /**
     * Suppose there are n data names. Then there are n(n-1)/2 pairwise
     * differences. This method returns the index of the maximum of the array
     * given by getAveragesOfDifferences()
     *
     * @return the index of the maximum of the array given by getAveragesOfDifferences()
     */
    public int getIndexOfMaximumOfAveragesOfDifferences() {
        return Statistic.getIndexOfMax(getAveragesOfDifferences());
    }

    /**
     * Returns the minimum value of the average of the differences
     *
     * @return the minimum value of the average of the differences
     */
    public double getMinimumOfAveragesOfDifferences() {
        return Statistic.getMin(getAveragesOfDifferences());
    }

    /**
     * The actual minimum average of the differences
     *
     * @return actual minimum average of the differences
     */
    public int getIndexOfMinimumOfAveragesOfDifferences() {
        return Statistic.getIndexOfMin(getAveragesOfDifferences());
    }

    /**
     * Suppose there are n data names. Then there are n(n-1)/2 pairwise
     * differences. This method returns averages of the differences in an array.
     * The elements of the array have correspondence to the array of strings
     * returned by getNamesOfPairedDifferences()
     *
     * @return averages of the differences in an array.
     */
    public double[] getAveragesOfDifferences() {
        List<Double> list = new ArrayList<>();
        for (String f : myPairDiffStats.keySet()) {
            LinkedHashMap<String, Statistic> g = myPairDiffStats.get(f);
            for (String s : g.keySet()) {
                list.add(getAverageDifference(f, s));
            }
        }
        double[] x = new double[list.size()];
        int i = 0;
        for (Double d : list) {
            x[i] = d.doubleValue();
            i++;
        }
        return x;
    }

    /**
     * Suppose there are n data names. Then there are n(n-1)/2 pairwise
     * differences. This method returns variances of the differences in an
     * array. The elements of the array have correspondence to the array of
     * strings returned by getNamesOfPairedDifferences()
     *
     * @return variances of the differences in an array
     */
    public double[] getVariancesOfDifferences() {
        List<Double> list = new ArrayList<>();
        for (String f : myPairDiffStats.keySet()) {
            LinkedHashMap<String, Statistic> g = myPairDiffStats.get(f);
            for (String s : g.keySet()) {
                list.add(getVarianceOfDifference(f, s));
            }
        }
        double[] x = new double[list.size()];
        int i = 0;
        for (Double d : list) {
            x[i] = d.doubleValue();
            i++;
        }
        return x;
    }

    /**
     * Suppose there are n data names. Then there are n(n-1)/2 pairwise
     * differences. This method returns a list of confidence intervals of the
     * differences in an array. The elements of the array have correspondence to
     * the array of strings returned by getNamesOfPairedDifferences()
     *
     * @param level the confidence level
     * @return list of confidence intervals of the differences in an array
     */
    public List<Interval> getConfidenceIntervalsOfDifferenceData(double level) {
        List<StatisticAccessorIfc> list = getPairedDifferenceStatistics();
        List<Interval> ilist = new ArrayList<>();
        for (StatisticAccessorIfc s : list) {
            ilist.add(s.getConfidenceInterval(level));
        }
        return ilist;
    }

    /**
     * The maximum variance of the differences
     *
     * @return maximum variance of the differences
     */
    public double getMaxVarianceOfDifferences() {
        double[] v = getVariancesOfDifferences();
        int indexOfMax = Statistic.getIndexOfMax(v);
        return v[indexOfMax];

    }

    /**
     * The average for the pair of data names given by the strings. If the data
     * names don't exist a null pointer exception will occur
     *
     * @param s1 the name of data set number 1
     * @param s2 the name of data set number 2
     * @return average for the pair of data names given by the strings
     */
    public double getAverageDifference(String s1, String s2) {
        LinkedHashMap<String, Statistic> g = myPairDiffStats.get(s1);
        Statistic stat = g.get(s2);
        return stat.getAverage();
    }

    /**
     * The variance for the pair of data names given by the strings. If the data
     * names don't exist a null pointer exception will occur
     *
     * @param s1 the name of data set number 1
     * @param s2 the name of data set number 2
     * @return variance for the pair of data names given by the strings
     */
    public double getVarianceOfDifference(String s1, String s2) {
        LinkedHashMap<String, Statistic> g = myPairDiffStats.get(s1);
        Statistic stat = g.get(s2);
        return stat.getVariance();
    }

    /**
     * A helper method to compute the difference between the two arrays
     *
     * @param f first array
     * @param s second array
     * @return the difference
     */
    public static double[] computeDifference(double[] f, double[] s) {
        if (f.length != s.length) {
            throw new IllegalArgumentException("The array lengths were not equal");
        }
        double[] r = new double[f.length];
        for (int i = 0; i < f.length; i++) {
            r[i] = f[i] - s[i];
        }
        return r;
    }

    /**
     * Checks if each double[] in the map has the same length
     *
     * @param dataMap the data map to check
     * @return true if same length
     */
    public final boolean checkLengths(Map<String, double[]> dataMap) {
        if (dataMap.keySet().size() <= 1) {
            throw new IllegalArgumentException("There must be 2 or more data arrays");
        }

        int[] lengths = new int[dataMap.keySet().size()];

        int i = 0;
        for (String s : dataMap.keySet()) {
            lengths[i] = dataMap.get(s).length;
            if (i > 0) {
                if (lengths[i - 1] != lengths[i]) {
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    /**
     * Get statistics on the data associated with the name. If the name is not
     * in the analyzer, null is returned
     *
     * @param name the name of the data set
     * @return the statistic over the data set
     */
    public Statistic getStatistic(String name) {
        double[] data = myDataMap.get(name);
        if (data == null) {
            return null;
        }
        return new Statistic(name, data);
    }

    /**
     * Returns the index associated with the data set name The supplied name
     * must be contained in the analyzer. Use contains() to check.
     *
     * @param name must be associated with a data set
     * @return the index associated with the data set name
     */
    public final int getIndexOfName(String name) {
        if (!contains(name)) {
            throw new IllegalArgumentException("The name is not associated with any data");
        }
        String[] names = getDataNames();
        int i = 0;
        for (String s : names) {
            if (s.equals(name)) {
                break;
            }
            i++;
        }
        return i;
    }

    /**
     * A list of statistics for all the data
     *
     * @return list of statistics for all the data
     */
    public List<StatisticAccessorIfc> getStatistics() {
        List<StatisticAccessorIfc> list = new ArrayList<StatisticAccessorIfc>();
        for (String s : myDataMap.keySet()) {
            list.add(getStatistic(s));
        }
        return list;
    }

    /**
     * The average for the named data or Double.NaN if the name is not in the
     * collector
     *
     * @param name the name of the data set
     * @return average for the named data or Double.NaN
     */
    public double getAverage(String name) {
        Statistic s = getStatistic(name);
        if (s == null) {
            return Double.NaN;
        }
        return s.getAverage();
    }

    /**
     * The variance for the named data or Double.NaN if the name is not in the
     * collector
     *
     * @param name the name of the data set
     * @return variance for the named data or Double.NaN
     */
    public double getVariance(String name) {
        Statistic s = getStatistic(name);
        if (s == null) {
            return Double.NaN;
        }
        return s.getVariance();
    }

    /**
     * The maximum of the average of all the data
     *
     * @return maximum of the average of all the data
     */
    public double getMaximumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getMax(avgs);
    }

    /**
     * The index of the maximum average
     *
     * @return index of the maximum average
     */
    public int getIndexOfMaximumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getIndexOfMax(avgs);
    }

    /**
     * The name of the maximum average
     *
     * @return name of the maximum average
     */
    public String getNameOfMaximumAverageOfData() {
        String[] names = getDataNames();
        return names[getIndexOfMaximumAverageOfData()];
    }

    /**
     * The minimum of the average of all the data
     *
     * @return minimum of the average of all the data
     */
    public double getMinimumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getMin(avgs);
    }

    /**
     * The index of the minimum of the average of all the data
     *
     * @return index of the minimum of the average of all the data
     */
    public int getIndexOfMinimumAverageOfData() {
        double[] avgs = getAveragesOfData();
        return Statistic.getIndexOfMin(avgs);
    }

    /**
     * The name of the minimum of the average of all the data
     *
     * @return name of the minimum of the average of all the data
     */
    public String getNameOfMinimumAverageOfData() {
        String[] names = getDataNames();
        return names[getIndexOfMinimumAverageOfData()];
    }

    /**
     * An array of all the averages of the data Each element is the average for
     * each of the n data names
     *
     * @return An array of all the averages of the data
     */
    public double[] getAveragesOfData() {
        List<StatisticAccessorIfc> list = getStatistics();
        double[] avg = new double[list.size()];
        int i = 0;
        for (StatisticAccessorIfc s : list) {
            avg[i] = s.getAverage();
            i++;
        }
        return avg;
    }

    /**
     * An array of all the variances of the data
     *
     * @return An array of all the variances of the data
     */
    public double[] getVariancesOfData() {
        List<StatisticAccessorIfc> list = getStatistics();
        double[] var = new double[list.size()];
        int i = 0;
        for (StatisticAccessorIfc s : list) {
            var[i] = s.getVariance();
            i++;
        }
        return var;
    }

    /**
     * Gets the difference between the system average associated with the name
     * and maximum of the rest of the averages
     *
     * @param name the name of the data set
     * @return the difference between the system average associated with the name
     * and maximum of the rest of the averages
     */
    public double getDiffBtwItemAndMaxOfRest(String name) {
        return getDiffBtwItemAndMaxOfRest(getIndexOfName(name));
    }

    /**
     * Gets the difference between the system average associated with the index
     * and maximum of the rest of the averages
     *
     * @param index the index
     * @return difference between the system average associated with the index
     * and maximum of the rest of the averages
     */
    public double getDiffBtwItemAndMaxOfRest(int index) {
        double[] avgs = getAveragesOfData();
        double[] avgsWO = JSLArrayUtil.copyWithout(index, avgs);
        double max = JSLArrayUtil.getMax(avgsWO);
        return avgs[index] - max;
    }

    /**
     * Computes the difference between each dataset average and the maximum of
     * the rest of the averages for each dataset. If d[] represents the
     * differences then d[0] is the difference between the first dataset average
     * and the maximum over the averages of the other datasets and so on.
     *
     * @return the difference between each dataset average and the maximum of
     * the rest of the averages for each dataset.
     */
    public double[] getAllDiffBtwItemsAndMaxOfRest() {
        double[] avgs = getAveragesOfData();
        double[] diffs = new double[avgs.length];

        for (int i = 0; i < avgs.length; i++) {
            diffs[i] = getDiffBtwItemAndMaxOfRest(i);
        }
        return diffs;
    }

    /**
     * Gets the difference between the system average associated with the name
     * and minimum of the rest of the averages
     *
     * @param name the name of the data set
     * @return difference between the system average associated with the name
     * and minimum of the rest of the averages
     */
    public double getDiffBtwItemAndMinOfRest(String name) {
        return getDiffBtwItemAndMinOfRest(getIndexOfName(name));
    }

    /**
     * Gets the difference between the system average associated with the index
     * and minimum of the rest of the averages
     *
     * @param index the index
     * @return the difference between the system average associated with the index
     * and minimum of the rest of the averages
     */
    public double getDiffBtwItemAndMinOfRest(int index) {
        double[] avgs = getAveragesOfData();
        double[] avgsWO = JSLArrayUtil.copyWithout(index, avgs);
        double min = JSLArrayUtil.getMin(avgsWO);
        return avgs[index] - min;
    }

    /**
     * Computes the difference between each dataset average and the minimum of
     * the rest of the averages for each dataset. If d[] represents the
     * differences then d[0] is the difference between the first dataset average
     * and the minimum over the averages of the other datasets and so on
     *
     * @return the difference between each dataset average and the minimum of
     * the rest of the averages for each dataset.
     */
    public double[] getAllDiffBtwItemsAndMinOfRest() {
        double[] avgs = getAveragesOfData();
        double[] diffs = new double[avgs.length];

        for (int i = 0; i < avgs.length; i++) {
            diffs[i] = getDiffBtwItemAndMinOfRest(i);
        }
        return diffs;
    }

    /**
     * Form the maximum comparison with the best (MCB) interval for the dataset
     * at the supplied index using an the default indifference zone
     *
     * @param index the index
     * @return the interval for maximum case
     */
    public Interval getMCBMaxInterval(int index) {
        return getMCBMaxInterval(index, getDefaultIndifferenceZone());
    }

    /**
     * Form the maximum comparison with the best (MCB) interval for the dataset
     * at the supplied index using the supplied indifference delta.
     *
     * @param index the index
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return the interval for the maximum comparison
     */
    public Interval getMCBMaxInterval(int index, double delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("The indifference delta must be >= 0");
        }
        double diff = getDiffBtwItemAndMaxOfRest(index);
        double ll = Math.min(0, diff - delta);
        double ul = Math.max(0, diff + delta);
        return new Interval(ll, ul);
    }

    /**
     * Forms all MCB intervals for the maximum given the default indifference zone
     *
     * @return all the intervals
     */
    public List<Interval> getMCBMaxIntervals() {
        return getMCBMaxIntervals(getDefaultIndifferenceZone());
    }

    /**
     * Forms all MCB intervals for the maximum given the supplied delta
     *
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return all the intervals
     */
    public List<Interval> getMCBMaxIntervals(double delta) {
        int n = getNumberDatasets();
        List<Interval> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(getMCBMaxInterval(i, delta));
        }
        return list;
    }

    /**
     * The MCB maximum intervals in the form of a map. The key names are the
     * names of the data sets with indifference delta based on the default
     *
     * @return the map holding the intervals
     */
    public Map<String, Interval> getMCBMaxIntervalsAsMap() {
        return getMCBMaxIntervalsAsMap(getDefaultIndifferenceZone());
    }

    /**
     * The MCB maximum intervals in the form of a map. The key names are the
     * names of the data sets
     *
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return the map holding the intervals
     */
    public Map<String, Interval> getMCBMaxIntervalsAsMap(double delta) {
        Map<String, Interval> map = new LinkedHashMap<>();
        int n = getNumberDatasets();
        String[] names = getDataNames();
        List<Interval> list = getMCBMaxIntervals(delta);
        for (int i = 0; i < n; i++) {
            map.put(names[i], list.get(i));
        }
        return map;
    }

    /**
     * Returns a StringBuilder representation of the MCB maximum intervals based on default
     * indifference zone setting
     *
     * @return a StringBuilder representation of the intervals
     */
    public StringBuilder getMCBMaxIntervalsAsSB() {
        return getMCBMaxIntervalsAsSB(getDefaultIndifferenceZone());
    }

    /**
     * Returns a StringBuilder representation of the MCB maximum intervals
     *
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return a StringBuilder representation of the MCB maximum intervals
     */
    public StringBuilder getMCBMaxIntervalsAsSB(double delta) {
        StringBuilder sb = new StringBuilder();
        int n = getNumberDatasets();
        String[] names = getDataNames();
        List<Interval> list = getMCBMaxIntervals(delta);
        sb.append("MCB Maximum Intervals");
        sb.append(System.lineSeparator());
        sb.append("Indifference delta: ");
        sb.append(delta);
        sb.append(System.lineSeparator());
        sb.append("Name");
        sb.append("\t \t");
        sb.append("Interval");
        sb.append(System.lineSeparator());
        for (int i = 0; i < n; i++) {
            sb.append(names[i]);
            sb.append("\t \t");
            sb.append(list.get(i).toString());
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        return sb;
    }

    /**
     * Form the maximum comparison with the best (MCB) interval for the dataset
     * at the supplied index using the default indifference zone
     *
     * @param index the index of the interval
     * @return the interval
     */
    public Interval getMCBMinInterval(int index) {
        return getMCBMinInterval(index, getDefaultIndifferenceZone());
    }

    /**
     * Form the maximum comparison with the best (MCB) interval for the dataset
     * at the supplied index using the supplied indifference delta.
     *
     * @param index the index of the interval
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return the interval
     */
    public Interval getMCBMinInterval(int index, double delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("The indifference delta must be >= 0");
        }
        double diff = getDiffBtwItemAndMinOfRest(index);
        double ll = Math.min(0, diff - delta);
        double ul = Math.max(0, diff + delta);
        return new Interval(ll, ul);
    }

    /**
     * Forms all MCB intervals for the minimum given the default indifference zone
     *
     * @return MCB intervals for the minimum given the default indifference zone
     */
    public List<Interval> getMCBMinIntervals() {
        return getMCBMinIntervals(getDefaultIndifferenceZone());
    }

    /**
     * Forms all MCB intervals for the minimum given the default indifference zone
     *
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return MCB intervals for the minimum given the default indifference zone
     */
    public List<Interval> getMCBMinIntervals(double delta) {
        int n = getNumberDatasets();
        List<Interval> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(getMCBMinInterval(i, delta));
        }
        return list;
    }

    /**
     * The MCB minimum intervals in the form of a map. The key names are the
     * names of the data sets with the default indifference zone
     *
     * @return MCB minimum intervals in the form of a map
     */
    public Map<String, Interval> getMCBMinIntervalsAsMap() {
        return getMCBMinIntervalsAsMap(getDefaultIndifferenceZone());
    }

    /**
     * The MCB minimum intervals in the form of a map. The key names are the
     * names of the data sets
     *
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return MCB minimum intervals in the form of a map
     */
    public Map<String, Interval> getMCBMinIntervalsAsMap(double delta) {
        Map<String, Interval> map = new LinkedHashMap<>();
        int n = getNumberDatasets();
        String[] names = getDataNames();
        List<Interval> list = getMCBMinIntervals(delta);
        for (int i = 0; i < n; i++) {
            map.put(names[i], list.get(i));
        }
        return map;
    }

    /**
     * Returns a StringBuilder representation of the MCB minimum intervals
     *
     * @return a StringBuilder representation of the MCB minimum intervals
     */
    public StringBuilder getMCBMinIntervalsAsSB() {
        return getMCBMinIntervalsAsSB(getDefaultIndifferenceZone());
    }

    /**
     * Returns a StringBuilder representation of the MCB minimum intervals
     *
     * @param delta the indifference zone parameter, must be greater than or equal to zero
     * @return a StringBuilder representation of the MCB minimum intervals
     */
    public StringBuilder getMCBMinIntervalsAsSB(double delta) {
        StringBuilder sb = new StringBuilder();
        int n = getNumberDatasets();
        String[] names = getDataNames();
        List<Interval> list = getMCBMinIntervals(delta);
        sb.append("MCB Minimum Intervals");
        sb.append(System.lineSeparator());
        sb.append("Indifference delta: ");
        sb.append(delta);
        sb.append(System.lineSeparator());
        sb.append("Name");
        sb.append("\t \t");
        sb.append("Interval");
        sb.append(System.lineSeparator());
        for (int i = 0; i < n; i++) {
            sb.append(names[i]);
            sb.append("\t \t");
            sb.append(list.get(i).toString());
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        return sb;
    }

    /**
     * A list of confidence intervals for the data based on the supplied
     * confidence level
     *
     * @param level the supplied confidence level
     * @return A list of confidence intervals
     */
    public List<Interval> getConfidenceIntervalsOfData(double level) {
        List<StatisticAccessorIfc> list = getStatistics();
        List<Interval> ilist = new ArrayList<>();
        for (StatisticAccessorIfc s : list) {
            ilist.add(s.getConfidenceInterval(level));
        }
        return ilist;
    }

    /**
     * A 2-Dim array of the data each row represents the across replication
     * average for each configuration (column)
     *
     * @return 2-Dim array of the data each row represents the across replication
     * average for each configuration (column)
     */
    public final double[][] getAllDataAsArray() {
        int c = myDataMap.keySet().size();
        int r = myDataSize;
        double[][] x = new double[r][c];
        int j = 0;
        for (String s : myDataMap.keySet()) {
            // get the column data
            double[] d = getData(s);
            // copy column data into the array
            for (int i = 0; i < r; i++) {
                x[i][j] = d[i];
            }
            // index to next column
            j++;
        }
        return x;
    }

    /**
     * The data associated with the name. If the name is not in the map, the
     * array will be null
     *
     * @param name the name identifying the data
     * @return the data associated with the name
     */
    public double[] getData(String name) {
        double[] x = myDataMap.get(name);
        if (x == null) {
            return null;
        }
        double[] d = new double[x.length];
        System.arraycopy(x, 0, d, 0, x.length);
        return d;
    }

    /**
     * Returns a StringBuilder representation of the statistics associated for
     * each data set
     *
     * @return a StringBuilder representation of the statistics associated for
     * each data set
     */
    public StringBuilder getSummaryStatistics() {
        return getSummaryStatistics(null);
    }

    /**
     * Returns a StringBuilder representation of the statistics associated for
     * each data set
     *
     * @param title a title for the report
     * @return a StringBuilder representation of the statistics associated for
     * each data set
     */
    public StringBuilder getSummaryStatistics(String title) {
        StatisticReporter r = new StatisticReporter(getStatistics());
        return r.getSummaryReport(title);
    }

    /**
     * A half-width summary report on the statistics for each data set
     *
     * @return A half-width summary report on the statistics for each data set
     */
    public StringBuilder getHalfWidthSummaryStatistics() {
        return getHalfWidthSummaryStatistics(null, 0.95);
    }

    /**
     * A half-width summary report on the statistics for each data set
     *
     * @param title a title for the report
     * @return A half-width summary report on the statistics for each data set
     */
    public StringBuilder getHalfWidthSummaryStatistics(String title) {
        return getHalfWidthSummaryStatistics(title, 0.95);
    }

    /**
     * A half-width summary report on the statistics for each data set
     *
     * @param level the confidence level
     * @return A half-width summary report on the statistics for each data set
     */
    public StringBuilder getHalfWidthSummaryStatistics(double level) {
        return getHalfWidthSummaryStatistics(null, level);
    }

    /**
     * A half-width summary report on the statistics for each data set
     *
     * @param title the title of the report
     * @param level the confidence level
     * @return A half-width summary report on the statistics for each data set
     */
    public StringBuilder getHalfWidthSummaryStatistics(String title, double level) {
        StatisticReporter r = new StatisticReporter(getStatistics());
        return r.getHalfWidthSummaryReport(title, level);
    }

    /**
     * A StringBuilder representation for a summary report on the pairwise
     * differences
     *
     * @return StringBuilder representation for a summary report on the pairwise differences
     */
    public StringBuilder getDifferenceSummaryStatistics() {
        return getDifferenceSummaryStatistics(null);
    }

    /**
     * A StringBuilder representation for a summary report on the pairwise
     * differences
     *
     * @param title the title of the report
     * @return StringBuilder representation for a summary report on the pairwise differences
     */
    public StringBuilder getDifferenceSummaryStatistics(String title) {
        StatisticReporter r = new StatisticReporter(getPairedDifferenceStatistics());
        return r.getSummaryReport(title);
    }

    /**
     * A StringBuilder representation for a half-width report on the pairwise
     * differences
     *
     * @param title the title of the report
     * @return StringBuilder representation for a half-width report on the pairwise differences
     */
    public StringBuilder getHalfWidthDifferenceSummaryStatistics(String title) {
        return getHalfWidthDifferenceSummaryStatistics(title, 0.95);
    }

    /**
     * A StringBuilder representation for a half-width report on the pairwise
     * differences
     *
     * @return StringBuilder representation for a half-width report on the pairwise differences
     */
    public StringBuilder getHalfWidthDifferenceSummaryStatistics() {
        return getHalfWidthDifferenceSummaryStatistics(null, 0.95);
    }

    /**
     * A StringBuilder representation for a half-width report on the pairwise
     * differences
     *
     * @param level the confidence level
     * @return StringBuilder representation for a half-width report on the pairwise differences
     */
    public StringBuilder getHalfWidthDifferenceSummaryStatistics(double level) {
        return getHalfWidthDifferenceSummaryStatistics(null, level);
    }

    /**
     * A StringBuilder representation for a half-width report on the pairwise
     * differences
     *
     * @param title the title of the report
     * @param level the confidence level
     * @return StringBuilder representation for a half-width report on the pairwise differences
     */
    public StringBuilder getHalfWidthDifferenceSummaryStatistics(String title, double level) {
        StatisticReporter r = new StatisticReporter(getPairedDifferenceStatistics());
        return r.getHalfWidthSummaryReport(title, level);
    }

    /**
     * A StringBuilder representation for the confidence intervals on the
     * datasets at the provided 0.95 level
     *
     * @return tringBuilder representation for the confidence intervals on the
     * datasets at the provided 0.95 level
     */
    public StringBuilder getConfidenceIntervalsOnData() {
        return getConfidenceIntervalsOnData(0.95);
    }

    /**
     * A StringBuilder representation for the confidence intervals on the
     * datasets at the provided level
     *
     * @param level the level
     * @return tringBuilder representation for the confidence intervals
     */
    public StringBuilder getConfidenceIntervalsOnData(double level) {
        StringBuilder sb = new StringBuilder();
        List<Interval> intervals = getConfidenceIntervalsOfData(level);
        String[] names = getDataNames();
        sb.append(level * 100);
        sb.append("% Confidence Intervals on Data\n");
        int k = 0;
        for (Interval i : intervals) {
            sb.append(names[k]);
            sb.append("\t");
            sb.append(i).append(System.lineSeparator());
            k++;
        }
        return sb;
    }

    /**
     * A StringBuilder representation for the confidence intervals on the
     * differences for the datasets at the 0.95 level
     *
     * @return StringBuilder representation for the confidence intervals
     */
    public StringBuilder getConfidenceIntervalsOnDifferenceData() {
        return getConfidenceIntervalsOnDifferenceData(0.95);
    }

    /**
     * A StringBuilder representation for the confidence intervals on the
     * differences for the datasets at the provided level
     *
     * @param level the level
     * @return StringBuilder representation for the confidence intervals
     */
    public StringBuilder getConfidenceIntervalsOnDifferenceData(double level) {
        StringBuilder sb = new StringBuilder();
        List<Interval> intervals = getConfidenceIntervalsOfDifferenceData(level);
        String[] names = getNamesOfPairedDifferences();
        sb.append(level * 100);
        sb.append("% Confidence Intervals on Difference Data\n");
        int k = 0;
        for (Interval i : intervals) {
            sb.append(names[k]);
            sb.append("\t");
            sb.append(i).append(System.lineSeparator());
            k++;
        }
        return sb;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Multiple Comparison Report for: ").append(getName());
        sb.append(System.lineSeparator());
        sb.append(this.getSummaryStatistics("Raw Data"));
        sb.append(System.lineSeparator());
        sb.append(getConfidenceIntervalsOnData());
        sb.append(System.lineSeparator());
        sb.append(getDifferenceSummaryStatistics("Difference Data"));
        sb.append(System.lineSeparator());
        sb.append(getConfidenceIntervalsOnDifferenceData());
        sb.append(System.lineSeparator());
        sb.append("Max variance = ").append(getMaxVarianceOfDifferences());
        sb.append(System.lineSeparator());
        sb.append("Min performer = ").append(getNameOfMinimumAverageOfData());
        sb.append(System.lineSeparator());
        sb.append("Min performance = ").append(getMinimumAverageOfData());
        sb.append(System.lineSeparator());
        sb.append("Max performer = ").append(getNameOfMaximumAverageOfData());
        sb.append(System.lineSeparator());
        sb.append("Max performance = ").append(getMaximumAverageOfData());
        sb.append(System.lineSeparator());
        sb.append("Min difference = ").append(getNameOfMinumumAverageOfDifferences());
        sb.append(System.lineSeparator());
        sb.append("Min difference value = ").append(getMinimumOfAveragesOfDifferences());
        sb.append(System.lineSeparator());
        sb.append("Max difference = ").append(getNameOfMaximumAverageOfDifferences());
        sb.append(System.lineSeparator());
        sb.append("Max difference value = ").append(getMaximumOfAveragesOfDifferences());
        sb.append(System.lineSeparator());
        sb.append(getMCBMaxIntervalsAsSB().toString());
        sb.append(getMCBMinIntervalsAsSB().toString());
        return sb.toString();
    }

    /**
     * Write a statistical summary of the data in the analyzer
     *
     * @param out the PrintWriter, must not be null
     */
    public final void writeSummaryStatistics(PrintWriter out) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        out.print(getSummaryStatistics());
    }

    /**
     * Write a statistical summary of the difference data in the analyzer
     *
     * @param out the PrintWriter, must not be null
     */
    public final void writeSummaryDifferenceStatistics(PrintWriter out) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        out.print(getDifferenceSummaryStatistics());
    }

    /**
     * Write the data as a csv file
     *
     * @param out the PrintWriter, must not be null
     */
    public void writeDataAsCSVFile(PrintWriter out) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        int c = myDataMap.keySet().size();
        int r = 1;
        for (String s : myDataMap.keySet()) {
            out.print(s);
            if (r < c) {
                out.print(",");
            }
            r++;
        }
        out.println();
        double[][] data = getAllDataAsArray();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                out.print(data[i][j]);
                if (j < data[i].length - 1) {
                    out.print(",");
                }
            }
            out.println();
        }
    }

    /**
     * Functions used to calculate Rinott constants
     *
     * Derived from Fortran code in
     *
     * 		Design and Analysis of Experiments for Statistical Selection,
     * 		Screening, and Multiple Comparisons
     *
     * 		Robert E. Bechhofer, Thomas J. Santner, David M. Goldsman
     *
     * 		ISBN: 978-0-471-57427-9
     * 		Wiley, 1995
     *
     * Original Fortran code available on the authors' website
     * http://www.stat.osu.edu/~tjs/REB-TJS-DMG/describe.html
     *
     * Converted to Java by Eric Ni, cn254@cornell.edu
     *
     * Revised, May 5, 2022, M. D. Rossetti, rossetti@uark.edu
     */

    /**
     * Standard Normal CDF
     *
     * @param x The point of evaluation
     * @return The cumulative normal distribution function (CNDF)
     * for a standard normal: N(0,1) evaluated at x.
     */
    private static double cumZCDF(double x) {
        int neg = (x < 0d) ? 1 : 0;
        if (neg == 1)
            x *= -1d;

        double k = (1d / (1d + 0.2316419 * x));
        double y = ((((1.330274429 * k - 1.821255978) * k + 1.781477937) *
                k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.3989422803 * Math.exp(-0.5 * x * x) * y;

        return (1 - neg) * y + neg * (1d - y);
    }

    /**
     * Chi-distribution PDF
     *
     * @param dof     Degree of freedom
     * @param x     The point of evaluation
     * @param lngam LNGAM(N) is LN(GAMMA(N/2))
     * @return The PDF of the Chi^2 distribution with N degrees of freedom for N &lt;= 50, evaluated at C
     */
    private static double chiSquaredPDF(int dof, double x, double[] lngam) {
        double dof2 = ((double) dof) / 2.0;
        double lng = 0.0;
        if (dof > LNGAM.length){
            lng = Gamma.logGammaFunction(dof2);
        } else {
            lng = lngam[dof-1];
        }
        double tmp = -dof2 * Math.log(2d) - lngam[dof - 1] + (dof2 - 1d) * Math.log(x) - x / 2.;
        return Math.exp(tmp);
    }

    static private final double[] X = {.44489365833267018419E-1,
            .23452610951961853745,
            .57688462930188642649,
            .10724487538178176330E1,
            .17224087764446454411E1,
            .25283367064257948811E1,
            .34922132730219944896E1,
            .46164567697497673878E1,
            .59039585041742439466E1,
            .73581267331862411132E1,
            .89829409242125961034E1,
            .10783018632539972068E2,
            .12763697986742725115E2,
            .14931139755522557320E2,
            .17292454336715314789E2,
            .19855860940336054740E2,
            .22630889013196774489E2,
            .25628636022459247767E2,
            .28862101816323474744E2,
            .32346629153964737003E2,
            .36100494805751973804E2,
            .40145719771539441536E2,
            .44509207995754937976E2,
            .49224394987308639177E2,
            .54333721333396907333E2,
            .59892509162134018196E2,
            .65975377287935052797E2,
            .72687628090662708639E2,
            .80187446977913523067E2,
            .88735340417892398689E2,
            .98829542868283972559E2,
            .11175139809793769521E3};

    static private final double[] LNGAM = new double[50];
    static private final double[] WEX = new double[32];

    static {
        final double[] W = {.10921834195238497114,
                .21044310793881323294,
                .23521322966984800539,
                .19590333597288104341,
                .12998378628607176061,
                .70578623865717441560E-1,
                .31760912509175070306E-1,
                .11918214834838557057E-1,
                .37388162946115247897E-2,
                .98080330661495513223E-3,
                .21486491880136418802E-3,
                .39203419679879472043E-4,
                .59345416128686328784E-5,
                .74164045786675522191E-6,
                .76045678791207814811E-7,
                .63506022266258067424E-8,
                .42813829710409288788E-9,
                .23058994918913360793E-10,
                .97993792887270940633E-12,
                .32378016577292664623E-13,
                .81718234434207194332E-15,
                .15421338333938233722E-16,
                .21197922901636186120E-18,
                .20544296737880454267E-20,
                .13469825866373951558E-22,
                .56612941303973593711E-25,
                .14185605454630369059E-27,
                .19133754944542243094E-30,
                .11922487600982223565E-33,
                .26715112192401369860E-37,
                .13386169421062562827E-41,
                .45105361938989742322E-47
        };
        for (int i = 1; i <= 32; ++i) {
            WEX[i - 1] = W[i - 1] * Math.exp(X[i - 1]);
//            System.out.printf("wex[%d] = %f %n", (i-1), WEX[i-1]);
        }
//        System.out.println();
        LNGAM[0] = 0.5723649429;
        LNGAM[1] = 0.0;
        for (int i = 2; i <= 25; ++i) {
            LNGAM[2 * i - 2] = Math.log(i - 1.5) + LNGAM[2 * i - 4];
            LNGAM[2 * i - 1] = Math.log(i - 1.0) + LNGAM[2 * i - 3];
        }
        for(int i=0;i< LNGAM.length; i++){
            System.out.printf("LNGAM[%d] = %f %n", i, LNGAM[i]);
        }

        System.out.println();

        for(int i=0;i< LNGAM.length; i++){
            LNGAM[i] = Gamma.logGammaFunction((i+1.0)/2.0);
            System.out.printf("LNGAM[%d] = %f %n", i, LNGAM[i]);
        }
    }

    /**
     * Computes the Rinott Constant
     *
     * @param T     Number of systems
     * @param PSTAR 1 - alpha, i.e. Power of the test. Usually set to 0.95
     * @param NU    Stage-0 sample size - 1
     * @return The Rinott Constant
     */
    public static double rinott(long T, double PSTAR, int NU) {
//        double LNGAM[] = new double[50];
        NU = Math.min(NU, 50);
//        double WEX[] = new double [32];

//j
        double DUMMY = 1.0;//TODO does not appear to do anything

//        if (true) return Double.NaN;
        //TODO this looks like bisection search
        double H = 4.0;
        double LOWERH = 0.0;
        double UPPERH = 20.00;
        double ANS = 0.0, TMP = 0.0;
        for (int LOOPH = 1; LOOPH <= 50; ++LOOPH) {
            ANS = 0.0;
            for (int j = 1; j <= 32; ++j) {
                TMP = 0.0;
                for (int i = 1; i <= 32; ++i) {
                    double z = H / Math.sqrt(NU * (1d / X[i - 1] + 1d / X[j - 1]) / DUMMY);
                    double zcdf = Normal.stdNormalCDF(z);
                    TMP = TMP + WEX[i - 1]
                            * zcdf
                            * chiSquaredPDF(NU, DUMMY * X[i - 1], LNGAM) * DUMMY;
//                    TMP = TMP + WEX[i - 1]
//                            * cumZCDF(z)
//                            * chiSquaredPDF(NU, DUMMY * X[i - 1], LNGAM) * DUMMY;
                }
                TMP = Math.pow(TMP, T - 1);
                ANS = ANS + WEX[j - 1] * TMP * chiSquaredPDF(NU, DUMMY * X[j - 1], LNGAM) * DUMMY;
            }
            if (Math.abs(ANS - PSTAR) <= 0.000001) {
                return H;
            } else if (ANS > PSTAR) {
                UPPERH = H;
                H = (LOWERH + UPPERH) / 2.0d;
            } else {
                LOWERH = H;
                H = (LOWERH + UPPERH) / 2.0d;
            }
        }
        return H;
    }

    public static void main(String args[]) {
//        LinkedHashMap<String, double[]> data = new LinkedHashMap<>();
//        double[] d1 = {63.72, 32.24, 40.28, 36.94, 36.29, 56.94, 34.10, 63.36, 49.29, 87.20};
//        double[] d2 = {63.06, 31.78, 40.32, 37.71, 36.79, 57.93, 33.39, 62.92, 47.67, 80.79};
//        double[] d3 = {57.74, 29.65, 36.52, 35.71, 33.81, 51.54, 31.39, 57.24, 42.63, 67.27};
//        double[] d4 = {62.63, 31.56, 39.87, 37.35, 36.65, 57.15, 33.30, 62.21, 47.46, 79.60};
//        data.put("One", d1);
//        data.put("Two", d2);
//        data.put("Three", d3);
//        data.put("Four", d4);
//
//        MultipleComparisonAnalyzer mca = new MultipleComparisonAnalyzer(data);
//
//        PrintWriter out = new PrintWriter(System.out, true);
//        mca.writeDataAsCSVFile(out);
//        out.println();
//
//        System.out.println(mca);
//
//        System.out.println("num data sets: " + mca.getNumberDatasets());
//        System.out.println(Arrays.toString(mca.getDataNames()));

        testRinott();
    }

    static public void testRinott() {

        System.out.println("Running rinott");
        double[] ans = {4.045, 6.057, 6.893, 5.488, 6.878, 8.276, 8.352};
        double[] p = {.975, .975, .975, .9, .95, 0.975, 0.975};
        long[] n = {10, 1000, 10000, 1000, 20000, 800000, 1000000};
        double x = rinott(10, 0.975, 50);
        System.out.println("rinott = " + x);
        int m = n.length;
        for (int i = 0; i < m; ++i) {
            double result = rinott(n[i], p[i], 50);
            System.out.printf("ans[%d] = %f, result = %f %n", i, ans[i], result);
            assert (result > ans[i] - 0.01 && result < ans[i] + 0.3);
        }
    }
}
