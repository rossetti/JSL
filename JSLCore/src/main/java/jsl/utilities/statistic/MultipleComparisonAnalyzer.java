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
import java.util.*;

import jsl.utilities.GetNameIfc;
import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.distributions.Tukey;
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

    private static Rinott rinott = null;

    /**
     * Calculates Rinott constants as per Table 2.13 of Bechhofer et al.
     *
     * Derived from Fortran code in
     *
     * 		Design and Analysis of Experiments for Statistical Selection,
     * 		Screening, and Multiple Comparisons
     *
     * 		Robert E. Bechhofer, Thomas J. Santner, David M. Goldsman
     *
     * @param numTreatments the number of treatments in the comparison, must be at least 2
     * @param pStar the lower bound on probably of correct selection
     * @param dof the number of degrees of freedom.  If the first stage samples size is n_0, then
     *            the dof = n_0 - 1, must be 4 or more
     * @return the computed Rinott constant
     */
    public static double rinottConstant(int numTreatments, double pStar, int dof){
        if (rinott == null){
            rinott = new Rinott();
        }
        return rinott.rinottConstant(numTreatments, pStar, dof);
    }

    /**
     * @param p      the probability, typically a confidence level (1-alpha), must be in (0,1)
     * @param nMeans the number of columns or treatments (means), must be greater than or equal to 2.0
     * @param dof     the degrees of freedom, must be greater than or equal to 1.0
     * @return the quantile of the Tukey distribution
     */
    public static double qtukey(double p, double nMeans, double dof){
        return Tukey.invCDF(p, nMeans, dof);
    }

    private static final double[][] QMVT90 = {
            {3.07768353717525, 4.69571963040887, 5.70445123022356, 6.4724021936715, 7.09929768515749, 7.58346779783018, 7.98576287509112, 8.31974697973131, 8.64207775110614, 8.93597833794726},
            {1.88561808316413, 2.53830491652939, 2.92479503549605, 3.19236024096424, 3.40729208632331, 3.57209067748936, 3.70844369993062, 3.83654436378227, 3.93095709455992, 4.02808791777624},
            {1.63774435369621, 2.12968383477043, 2.41295525577478, 2.60668189849101, 2.7557305041991, 2.87478449371977, 2.97517457790034, 3.06051250716423, 3.13484592966047, 3.19902976830057},
            {1.53320627405894, 1.96288394127457, 2.20524544551961, 2.36954305779548, 2.49617103255596, 2.59934698051841, 2.6805355403422, 2.75197782734875, 2.81728016240746, 2.87216362567742},
            {1.47588404882448, 1.87297946493498, 2.09299817354973, 2.24604254489296, 2.35925998783421, 2.45031739270427, 2.52738014923327, 2.59224078977435, 2.64874235451492, 2.69984531203838},
            {1.43975574726515, 1.81690381434763, 2.02565327159958, 2.16695047616115, 2.27426055041312, 2.36070647821071, 2.42949806485059, 2.49221178078156, 2.54510057130863, 2.59108133481585},
            {1.41492392765051, 1.77863210260068, 1.97854122301143, 2.11370116571856, 2.21524576537449, 2.29662335269464, 2.36468074803084, 2.42443861248256, 2.4752946407382, 2.51754322298286},
            {1.39681530974387, 1.75131090444351, 1.94624102696181, 2.07606495245088, 2.17416223979089, 2.2543657850261, 2.31830784765627, 2.37483375557822, 2.42295825423433, 2.46766292967846},
            {1.38302873839663, 1.73022064029105, 1.92056355496593, 2.04638130873966, 2.14360762788368, 2.21953889382978, 2.28448428350838, 2.33831462458232, 2.38670082332811, 2.42782023655739},
            {1.37218364111034, 1.71368753599462, 1.89805687357369, 2.02503049118681, 2.11893210863344, 2.19305507678262, 2.25552971088976, 2.31142928485806, 2.35521456822302, 2.39668205268596},
            {1.36343031802054, 1.70038415587991, 1.88219947435065, 2.00586606060673, 2.09825304676484, 2.17254751456474, 2.23295974838922, 2.28581075819409, 2.33205902326693, 2.37178323531376},
            {1.35621733402321, 1.68940652866248, 1.87093277552636, 1.99157656315086, 2.0827425067104, 2.15577879292687, 2.21613229391386, 2.26818224383615, 2.31176977971214, 2.34952279125251},
            {1.35017128878006, 1.68026449165744, 1.85770921801766, 1.97912699440071, 2.06900578277677, 2.14111966733505, 2.20108860752468, 2.25118872910711, 2.29429041940135, 2.33291166853321},
            {1.34503037445465, 1.67246198519892, 1.8481733271584, 1.96689684437876, 2.05705487007671, 2.12823557583567, 2.18696970774252, 2.23763422027665, 2.28095098816462, 2.31960188402709},
            {1.34060560785046, 1.66580479504174, 1.84007774258622, 1.95777831106349, 2.04715551186278, 2.11736616770731, 2.17628864112805, 2.22547085748623, 2.26840963839587, 2.30614629827298},
            {1.33675716732731, 1.65997431040027, 1.83461538132333, 1.95046565580647, 2.03770278161937, 2.10823989815313, 2.16610377793855, 2.21426206803647, 2.25867921443328, 2.29538799591856},
            {1.33337938972163, 1.65486193952371, 1.82661281226553, 1.94330628431718, 2.03076311809578, 2.10050810010917, 2.15829087902162, 2.20694365503386, 2.2486437974038, 2.2860130480229},
            {1.33039094356991, 1.65034276894606, 1.8208227937489, 1.9375374980674, 2.02525468124877, 2.09370612350824, 2.15089216698967, 2.19878612099699, 2.24088505641237, 2.27772330808184},
            {1.3277282090268, 1.64637416850456, 1.81804716312017, 1.93224382818188, 2.01763818520599, 2.08795716164707, 2.14336199009922, 2.19250036849794, 2.23424864071209, 2.27053404466138},
            {1.32534070698505, 1.64276598826184, 1.8120404260685, 1.92752222739425, 2.0135023383425, 2.08216938534792, 2.13838369634566, 2.18528450153074, 2.22723570447806, 2.26353473018266},
            {1.32318787386517, 1.6395147488055, 1.80914492393965, 1.92302819534847, 2.0084025789457, 2.07552200761873, 2.13265960480042, 2.18100855972218, 2.22164832501626, 2.25777053627865},
            {1.32123674161336, 1.63656959721711, 1.80519407432345, 1.91855740823075, 2.00508059678358, 2.07182735085976, 2.12797745928717, 2.17486007609438, 2.21551373798264, 2.25188222695467},
            {1.31946023981616, 1.6338894620963, 1.80215550558046, 1.91486097803089, 2.00039396059534, 2.06779234189432, 2.12242918749863, 2.17015350740322, 2.21087425961544, 2.24713348086994},
            {1.31783593367315, 1.63144023369619, 1.79973680113765, 1.91199627264265, 1.99619193777172, 2.06328047220069, 2.11914384473404, 2.16538751191929, 2.20663340011339, 2.24253528321711},
            {1.31634507267387, 1.62925766969928, 1.79640116730728, 1.90912708563952, 1.99256716677698, 2.06048021261575, 2.11475141340044, 2.16313648418541, 2.20278083480962, 2.2383367165296},
            {1.31497186427052, 1.62718620634137, 1.79410663633476, 1.90629347447289, 1.99038658956813, 2.05675132817343, 2.11219488528824, 2.15976130295569, 2.19902530843668, 2.23495544644582},
            {1.31370291282927, 1.62527274443738, 1.79113398091587, 1.90342375929878, 1.98865338562703, 2.05416014915983, 2.1082621796026, 2.15489639527729, 2.19565892180525, 2.23149785484139},
            {1.31252678159267, 1.62349987369162, 1.78845035574089, 1.90185720044221, 1.98531413521782, 2.05125181702539, 2.10606453206276, 2.1519207893833, 2.19223475649433, 2.22789970313121},
            {1.31143364730155, 1.62185265071838, 1.78722437765303, 1.89958820118018, 1.98191935598683, 2.04807554797549, 2.10287076109542, 2.14969522371719, 2.18966770517128, 2.22486596473562},
            {1.3104150253914, 1.6203181771261, 1.78610120098592, 1.89694967810055, 1.98044777564335, 2.04608759217629, 2.10061944641625, 2.14675591211539, 2.18671925666323, 2.22212799595276},
            {1.30946354949465, 1.61888502827481, 1.78428341124482, 1.89453775702271, 1.97823018898428, 2.04333804477083, 2.09813375292917, 2.14477231160415, 2.18435538060273, 2.21965607383046},
            {1.30857279312952, 1.61754391832041, 1.78278235609854, 1.89314051644869, 1.97641028556807, 2.0416975204203, 2.09590614693877, 2.14251086592286, 2.18199821672112, 2.2175235844932},
            {1.30773712445089, 1.61628606086373, 1.78002249007541, 1.89125631226387, 1.97470160322308, 2.03982600664291, 2.09453694165655, 2.13972963566108, 2.17914442232179, 2.21431773361451},
            {1.30695158712643, 1.61510393800136, 1.78026298779289, 1.89001557288919, 1.97256795587529, 2.0379094091043, 2.09184885778997, 2.1386641183409, 2.17694017488841, 2.21315000443622},
            {1.30621180201603, 1.61399089262564, 1.77767517020637, 1.88889974787418, 1.97062138430433, 2.03607340896473, 2.08995664304667, 2.13567257375019, 2.17520454554021, 2.21054201525558},
            {1.30551388553625, 1.6129410469542, 1.77695676049534, 1.88643091555047, 1.96915915000597, 2.03474615716703, 2.0873536168334, 2.13483130225664, 2.17355965036755, 2.20862842020926},
            {1.30485438149762, 1.61202953823294, 1.77511936895726, 1.88506724904847, 1.96641774491664, 2.03290031041935, 2.0862764554296, 2.13240052096713, 2.17148350930061, 2.20669051390643},
            {1.3042302038905, 1.61108905539569, 1.77508454336411, 1.88371018958469, 1.96650267983095, 2.03169680237789, 2.08453561185446, 2.13140742806448, 2.17039898154919, 2.20499856909255},
            {1.30363858862127, 1.61019779830261, 1.77259064113776, 1.88335578174743, 1.96589885789045, 2.03038234992609, 2.08390846921145, 2.12887778543658, 2.16840076860716, 2.20283445509016},
            {1.3030770526072, 1.60935200110595, 1.7720071294385, 1.88149916440833, 1.96326498963126, 2.02870439155853, 2.08214807625848, 2.12820849147527, 2.16728076788651, 2.20199916700668},
            {1.30254335895338, 1.60854827249581, 1.7705469549006, 1.88152865281921, 1.96259056581245, 2.02823190686044, 2.08169393985289, 2.12614012547765, 2.16555575858882, 2.19988617279263},
            {1.30203548718252, 1.60778355027102, 1.77086007499723, 1.87923587024555, 1.96130041189948, 2.02696108175428, 2.07899219951152, 2.12513182616072, 2.16456895475017, 2.19902683351797},
            {1.30155160768217, 1.60705506236526, 1.76951962446147, 1.8783684419354, 1.96059878933752, 2.02528419139942, 2.0782209455033, 2.12428896976363, 2.1631509306375, 2.19788895124879},
            {1.3010900596888, 1.60636029328296, 1.76910532767596, 1.87754198141247, 1.95915708411155, 2.02416529532827, 2.07767534067308, 2.1225741957302, 2.16143195482838, 2.19597592881428},
            {1.30064933225024, 1.60569695508901, 1.7680911302048, 1.87735623378527, 1.9579297764941, 2.02281660064117, 2.07607676855071, 2.12128535167258, 2.16078795911085, 2.19506806058841},
            {1.30022804770694, 1.60506296224728, 1.76729059584393, 1.87616773911687, 1.95720541836265, 2.02265855823892, 2.07416085840482, 2.12096542255893, 2.15913867981841, 2.19435255748098},
            {1.29982494731166, 1.60445640972526, 1.76585008971279, 1.87544506241707, 1.95696986977977, 2.02160476313006, 2.07371584341155, 2.11858391130883, 2.15828951665942, 2.19237720753929},
            {1.29943887867139, 1.60387555388065, 1.76510994428679, 1.87555004639674, 1.95626753078934, 2.02033375218336, 2.07329827025263, 2.11851847241175, 2.15723856580467, 2.19175829433131},
            {1.29906878474775, 1.60331879572615, 1.76545619794616, 1.87346587641862, 1.95409058135236, 2.01979087289868, 2.07293902736834, 2.11683045275556, 2.15636162864425, 2.19065357837252},
            {1.29871369419481, 1.60278466623415, 1.76503176121015, 1.87337473966281, 1.95321963547793, 2.01894967249763, 2.07105706692771, 2.11630526820335, 2.15526830498928, 2.1895611421685},
            {1.29837271284837, 1.6022718133972, 1.76420216433132, 1.87256643266624, 1.95310437497514, 2.01795514898318, 2.07081571536908, 2.11583641907813, 2.15447848108215, 2.18902933862165},
            {1.29804501620975, 1.6017789908041, 1.7634322475685, 1.87180942072668, 1.95276890823051, 2.01721551220327, 2.06963625009181, 2.1143564495164, 2.15342937418902, 2.18752841451015},
            {1.29772984279107, 1.60130504752869, 1.76277535696802, 1.87113192234533, 1.95227500989107, 2.01602447548055, 2.0688351531883, 2.11406754666953, 2.15264877904065, 2.18651029671432},
            {1.29742648820907, 1.60084891915845, 1.76233034746565, 1.87103019619448, 1.95110476604067, 2.01493473073053, 2.06806262161949, 2.11281633360153, 2.15205834105685, 2.18634275498451},
            {1.29713429993094, 1.600409619816, 1.76252614807267, 1.86975120786341, 1.95056064019153, 2.01476430878792, 2.06755892835691, 2.11178067068903, 2.15097656431104, 2.18541109693155},
            {1.2968526725898, 1.59998623504744, 1.7616716630462, 1.86890712318676, 1.94974811607433, 2.01375870571488, 2.0668739675311, 2.11159187826183, 2.1501441816021, 2.18490676491183},
            {1.29658104379901, 1.59957791546977, 1.76105917947018, 1.86856739822317, 1.94996636308376, 2.01375986425817, 2.06675132652278, 2.11114762700006, 2.14957634081063, 2.18360960309254},
            {1.29631889040442, 1.59918387108438, 1.75996426180362, 1.86768986176876, 1.94852229059385, 2.01306307071823, 2.06595961881215, 2.11007303967872, 2.14925338193088, 2.18307215829868},
            {1.29606572512205, 1.59880336617662, 1.76020859947226, 1.86817606817545, 1.94836864023868, 2.01289824642225, 2.06480774157199, 2.11047651683551, 2.14816340560884, 2.18264411236095},
            {1.29582109351573, 1.59843571473217, 1.7593518838383, 1.86745577623717, 1.94775935545728, 2.01118975148025, 2.06433522761892, 2.10879676792792, 2.14724253782039, 2.18177774271578}
    };

    private static final double[][] QMVT95 = {{6.31375151467504, 9.50997825828177, 11.5798721422664, 13.0957050943247, 14.2541572256462, 15.1842270020881, 16.0560035834168, 16.7492692648287, 17.2988641258188, 17.8922506621456},
            {2.91998558035372, 3.80452173160657, 4.33264050587089, 4.7040154789854, 5.00990906308444, 5.23720389414317, 5.4307048563446, 5.61918846663977, 5.7498011203099, 5.88064373650628},
            {2.35336343480182, 2.93829762274126, 3.27823370305098, 3.51072343208338, 3.70287764059767, 3.84134149081432, 3.970228071477, 4.07832400302387, 4.17097294323816, 4.25189924930256},
            {2.13184678632665, 2.61033872883509, 2.88642493836309, 3.08008129265441, 3.21768519510167, 3.33391411957388, 3.43609914781602, 3.52211989791077, 3.59364386307348, 3.65826286051067},
            {2.01504837333302, 2.44030755230868, 2.68205263773534, 2.8487275222124, 2.97453690195111, 3.07384442525886, 3.15807624287092, 3.23827841501145, 3.29790281534983, 3.35363444873052},
            {1.9431802805153, 2.33673549404633, 2.55523263942112, 2.71179845062481, 2.82473092138894, 2.9199779947046, 2.9976310472982, 3.06378873409337, 3.12055759621755, 3.17414087468183},
            {1.89457860509001, 2.26717355826608, 2.4709502749203, 2.61655157787842, 2.72697739991645, 2.81530871408487, 2.88490033368662, 2.94863865964298, 3.00401566272028, 3.05186948116977},
            {1.8595480375309, 2.21772798303128, 2.41470745770827, 2.55289819162729, 2.66156748515467, 2.73955769470331, 2.80845502707993, 2.86917844527773, 2.92002386284771, 2.96735166269606},
            {1.83311293265624, 2.18018200645153, 2.37080722039311, 2.50482326198161, 2.60081200729514, 2.68463546334248, 2.75039644208256, 2.8071204793549, 2.85722504239021, 2.90010045684266},
            {1.81246112281168, 2.15094459092497, 2.33761470637791, 2.46663637969706, 2.56036697098192, 2.63844317048381, 2.70376942886797, 2.75845218005153, 2.80798602734025, 2.85024812236481},
            {1.79588481870404, 2.12749965830338, 2.3077352687327, 2.43431906667373, 2.53144886039698, 2.60392753833879, 2.66755992731049, 2.72141798399717, 2.76846837946316, 2.81010744094098},
            {1.78228755564932, 2.10834447173759, 2.28554689781211, 2.40971494685164, 2.50339196362384, 2.57637012416464, 2.63812729708048, 2.69161299903684, 2.73658649192169, 2.7750266185811},
            {1.77093339598687, 2.09234122429454, 2.27095531838353, 2.38764945411501, 2.47818434507659, 2.55232279925101, 2.61296236923461, 2.66376082770799, 2.70991511453189, 2.74850297382399},
            {1.76131013577489, 2.07883802775936, 2.25488476983034, 2.3717431212451, 2.46096990555601, 2.5310370980903, 2.59289617052777, 2.64079655540681, 2.6877328953877, 2.72660123965749},
            {1.75305035569257, 2.06722371977157, 2.23943617741336, 2.35698870371721, 2.44257166534604, 2.51662247109991, 2.57353893930688, 2.62364487042839, 2.66609720250385, 2.70646636261977},
            {1.74588367627625, 2.05715735407914, 2.22666916377954, 2.34430701884865, 2.43081785027701, 2.50054748711431, 2.55904094221618, 2.60729214532603, 2.65015594553309, 2.68942177876288},
            {1.73960672607507, 2.04839469657651, 2.2138588821906, 2.33217404807401, 2.41770410972451, 2.48609227666029, 2.54627320785683, 2.59363014557785, 2.63502387002776, 2.67223625748329},
            {1.73406360661754, 2.04061947694032, 2.20567247350867, 2.32045368306871, 2.40689829626525, 2.47577594469065, 2.53265066773066, 2.58049485509054, 2.62378621778333, 2.65985219816894},
            {1.72913281152137, 2.0337083536127, 2.20066799076052, 2.31214853539229, 2.3972409597189, 2.46656321630531, 2.52091016996395, 2.57022013884709, 2.61120109152798, 2.64842612476185},
            {1.72471824292079, 2.02752494840904, 2.19226180530193, 2.30350356816924, 2.3895770071143, 2.45652731694229, 2.51350734272611, 2.55829972349127, 2.60072198586465, 2.63710981642704},
            {1.72074290281188, 2.02196013271222, 2.18558207116695, 2.29766105135715, 2.38311662209763, 2.44865167624017, 2.50288817971099, 2.54968645548164, 2.59008031947213, 2.62830066958083},
            {1.71714437438024, 2.01692554855524, 2.18039963005486, 2.28976424307525, 2.3727238145929, 2.44017363066428, 2.49563624744929, 2.54231371593123, 2.58302944280509, 2.61949654910989},
            {1.71387152774705, 2.01239869995845, 2.17608240766995, 2.28271089574161, 2.36585689038702, 2.43308303008408, 2.48867689153597, 2.53558873302735, 2.57601481325627, 2.61268147733578},
            {1.71088207990943, 2.00821786027136, 2.17006013206501, 2.28098665332488, 2.36182020741469, 2.42873367463587, 2.48252611105845, 2.52853049675884, 2.56885988061203, 2.60334989968166},
            {1.7081407612519, 2.00438566451709, 2.16495965938735, 2.27408397313885, 2.35689019177374, 2.42182793655531, 2.47681951561631, 2.52265620943887, 2.56262815370898, 2.59854674730375},
            {1.70561791975927, 2.00086026323205, 2.16203296442019, 2.26834713886584, 2.3513229421763, 2.41649105183701, 2.46989507822704, 2.51667444329761, 2.55621876174321, 2.59324018547589},
            {1.70328844572213, 1.99760624496381, 2.15520687343899, 2.26569855568232, 2.34667568719174, 2.4117417016815, 2.4658988589986, 2.51208901020856, 2.55089241881696, 2.58713425957452},
            {1.70113093426593, 1.99459344447877, 2.15433714959713, 2.26177861507465, 2.34248876282616, 2.40637122644104, 2.46114830522778, 2.50602695836527, 2.54705769520962, 2.58114011451417},
            {1.6991270265335, 1.99179600619297, 2.15285056484587, 2.25829838440037, 2.34007954917843, 2.40399988249763, 2.4561360493935, 2.50207528805473, 2.54163886278127, 2.57689168238517},
            {1.69726088659396, 1.98919164384089, 2.14577826588411, 2.25490595641113, 2.33569395047436, 2.39957429712481, 2.45104318218776, 2.49801032576009, 2.53704176339343, 2.57200229173078},
            {1.69551878254587, 1.98676103559292, 2.14247998220274, 2.25148108164641, 2.33286994505006, 2.39542493376952, 2.44943702580315, 2.49415726781962, 2.53355948532192, 2.56813610531463},
            {1.69388874838371, 1.98448736079815, 2.13947308247316, 2.24762594827299, 2.32871541452351, 2.39163621852488, 2.44566907807731, 2.49056657679579, 2.52861730437734, 2.56437384439911},
            {1.69236030903034, 1.98241688298758, 2.13919590839963, 2.24703602267003, 2.32687497735033, 2.38981290433572, 2.44339850849018, 2.48744858635413, 2.52662800190179, 2.56137724214187},
            {1.69092425518685, 1.98041285118093, 2.13764710339249, 2.24354837927013, 2.32204562338818, 2.38666542659679, 2.43890317044316, 2.48429805102092, 2.5222628087307, 2.55756653579071},
            {1.68957245778027, 1.97852680622617, 2.13467191369763, 2.24059295240916, 2.32042101280325, 2.38339532599205, 2.43713335476784, 2.48104448564317, 2.51944102406941, 2.5546250470705},
            {1.68829771411682, 1.9767486283688, 2.13288233243211, 2.239171663854, 2.31790906478657, 2.38110048996704, 2.43310600245513, 2.47769055917854, 2.51727971412581, 2.55029685586356},
            {1.68709361959626, 1.97506932278131, 2.12981082020951, 2.2357235909566, 2.31554403509696, 2.37838521056043, 2.43158653428475, 2.47732467512426, 2.51361087653574, 2.54777660862155},
            {1.68595446016674, 1.97348086750466, 2.12821875325046, 2.23311338362962, 2.31214674514618, 2.37654798274087, 2.4286770356647, 2.4722480110957, 2.51145973815718, 2.54551368484298},
            {1.68487512171122, 1.9719760854016, 2.12790745075889, 2.23328556457617, 2.31022983193404, 2.37352106752221, 2.4277378527206, 2.47104688195728, 2.50967972812648, 2.5422377430628},
            {1.68385101333565, 1.97054853581201, 2.12520977402589, 2.23063387966631, 2.30906151222246, 2.37281100924529, 2.42442862989308, 2.46888368124629, 2.50652823609602, 2.54035889312691},
            {1.68287800213271, 1.96919242246274, 2.12405555150624, 2.22837203170997, 2.30996744847491, 2.37001162272737, 2.4221869055399, 2.46631141643729, 2.50518708464773, 2.53822302862644},
            {1.68195235746753, 1.96790251485653, 2.12270340425246, 2.22908365635513, 2.30625885934162, 2.36854616287889, 2.42037961371738, 2.46354070981639, 2.50241537003588, 2.53577030225367},
            {1.68107070320252, 1.9666740808934, 2.12082849258832, 2.22476083246546, 2.30321833419578, 2.36649657781383, 2.4182864789171, 2.46131279953762, 2.49971327040057, 2.5343306166076},
            {1.68022997657212, 1.96550282889588, 2.12144582280713, 2.22340193453609, 2.30060556441748, 2.36344573199975, 2.41642317447361, 2.46059999097099, 2.49824268734652, 2.53231181631934},
            {1.67942739265235, 1.96438485754182, 2.11844871218808, 2.22202698633536, 2.30087438658061, 2.36403596661732, 2.41526779279794, 2.45881401600836, 2.49650825491612, 2.5302607166311},
            {1.67866041355687, 1.96331661247456, 2.11743233284608, 2.22110334755603, 2.29894767631747, 2.36116776759378, 2.41349235824289, 2.457020272394, 2.49482796388889, 2.52835599844489},
            {1.67792672164186, 1.96229484857417, 2.11696923302475, 2.21978460067856, 2.29725682946001, 2.35975765610009, 2.41225928923895, 2.45581066947973, 2.49254730206509, 2.52644400469617},
            {1.67722419612434, 1.96131659704656, 2.11501260909552, 2.21789128057549, 2.2956213354104, 2.35845194814658, 2.40944777996997, 2.4534164549314, 2.49147439437487, 2.52400350326142},
            {1.67655089261685, 1.96037913662759, 2.11418132822357, 2.21745607991753, 2.2933124659621, 2.35663869779618, 2.40895549464779, 2.45233671509252, 2.48968814388306, 2.52341316004475},
            {1.6759050251631, 1.95947996831443, 2.11358612684737, 2.21548227953659, 2.29247021877614, 2.35525812463708, 2.40627574397708, 2.45021476621827, 2.48799556015123, 2.52114634768901},
            {1.67528495042491, 1.95861679312993, 2.11053920685998, 2.21438838103777, 2.29171909486553, 2.35450606014971, 2.40612004217442, 2.44944954838999, 2.48772512853879, 2.52048027158246},
            {1.67468915372603, 1.957787492504, 2.11059688251218, 2.21418195223883, 2.29258153048576, 2.35358198588316, 2.40526298937546, 2.44803223866605, 2.48514156938463, 2.51814431574321},
            {1.6741162367031, 1.95699011091924, 2.10915214843384, 2.21166903618155, 2.28989996799627, 2.35187786382987, 2.40243373230426, 2.44654627206961, 2.48414251847078, 2.51763495553744},
            {1.67356490635216, 1.95622284052201, 2.10823624063273, 2.21182451059826, 2.28906964861915, 2.35048376213181, 2.40188986273256, 2.44542083840657, 2.48248813482839, 2.516123769172},
            {1.67303396528991, 1.9554840074439, 2.10759887098948, 2.21193362093006, 2.28831314326518, 2.34982306120762, 2.40131294722772, 2.44294101732026, 2.48224211773105, 2.51467588967778},
            {1.67252230307558, 1.95477205961597, 2.10703412331726, 2.21176198926924, 2.28626807252824, 2.34922617203155, 2.40081549375381, 2.44320161781966, 2.48112272879059, 2.51423610923163},
            {1.67202888846095, 1.95408555588902, 2.10631650218035, 2.20995978338523, 2.28581397752003, 2.34843194029217, 2.39772299175983, 2.44220712405304, 2.47893906567524, 2.51221238278172},
            {1.67155276245486, 1.95342315629954, 2.10549910409532, 2.20878089339405, 2.28617009351111, 2.34688860732621, 2.39681517518123, 2.44111246066491, 2.47849785280053, 2.51157783864959},
            {1.67109303210389, 1.95278361334297, 2.10488630097935, 2.20724928960668, 2.28433199199497, 2.34592915850982, 2.39764690897201, 2.43997518953176, 2.47735757613956, 2.51049562317477},
            {1.67064886490464, 1.95216576413478, 2.10395549647444, 2.20667757790958, 2.28237185840767, 2.34481455818838, 2.39614827205541, 2.43881026801995, 2.47620044724278, 2.50898894033524}};

    private static final double[][] QMVT99 = {
            {31.8205159537739, 47.7392729073932, 59.9705353187789, 64.3292966560448, 71.2909817451985, 75.396584617031, 78.4654369440401, 82.9935127873682, 87.4780132355718, 90.671978725256},
            {6.96455673428327, 8.87822059559499, 10.0800823200058, 10.7980849878332, 11.500808017877, 12.0656341338127, 12.4853429993672, 12.8632611052064, 13.174343121462, 13.415927558518},
            {4.54070285856813, 5.48218849750321, 6.03093235465903, 6.3834405332736, 6.66107658970298, 6.96148873908058, 7.21067427615982, 7.32183296127455, 7.53139416709035, 7.67433842747944},
            {3.7469473879792, 4.40774511515991, 4.77647500645739, 5.1580054774342, 5.26050877207168, 5.46360332842531, 5.60274990432737, 5.69576722708728, 5.83288994367405, 5.87044665373717},
            {3.36492999890722, 3.89961416805991, 4.17878356826485, 4.44681371054772, 4.60179067517618, 4.71325953027191, 4.84373587447122, 4.95181767914282, 5.02644222346503, 5.11831389879331},
            {3.14266840329098, 3.60715646659068, 3.87675468412104, 4.04023250911205, 4.17903328294412, 4.31283564317049, 4.42181519498825, 4.49581580772201, 4.57126153908138, 4.64568800585859},
            {2.99795156686853, 3.41812013263789, 3.65459142726757, 3.81318276019955, 3.96546267490871, 4.06424114600171, 4.15682368196553, 4.23518949454549, 4.29084899040606, 4.35077361462949},
            {2.89645944770962, 3.28658543250473, 3.51931576339042, 3.64818935612727, 3.77870546243956, 3.86555908477173, 3.95993955101926, 4.02966409165598, 4.09312990541236, 4.13481572889364},
            {2.82143792502581, 3.18941315616063, 3.40370859026679, 3.54289472817035, 3.65752176882272, 3.74166302287909, 3.82512038099491, 3.90251872750168, 3.94817486737915, 3.99297625997548},
            {2.7637694581127, 3.11499032355274, 3.29852371806192, 3.46053853426872, 3.55527423530483, 3.64215439390875, 3.71282569798614, 3.78096489447352, 3.82593853261517, 3.87433051035229},
            {2.71807918381386, 3.0561840477724, 3.24009696684251, 3.3905899357203, 3.48530977286465, 3.56308576927805, 3.63137590991424, 3.68630974953505, 3.7348183329309, 3.78734890088539},
            {2.68099799312091, 3.00852911680654, 3.18581142562678, 3.315399673648, 3.42808168349317, 3.49535660805642, 3.56105022240401, 3.62035837690871, 3.66843199806201, 3.71369766363793},
            {2.65030883791219, 2.96916079452922, 3.15916565390038, 3.27410415336902, 3.37869977942879, 3.43339730925448, 3.50279033975494, 3.56725733574502, 3.61072122660855, 3.65392041614471},
            {2.62449406759005, 2.93612538343292, 3.11305633040444, 3.2426392441795, 3.32082232434655, 3.40650625007607, 3.46022958551749, 3.5285891700575, 3.56354894629205, 3.60222711095912},
            {2.60248029501112, 2.90796308360335, 3.06613113713661, 3.18205441278004, 3.29039042838957, 3.3559874340066, 3.4203546511406, 3.47463788069299, 3.51982302392276, 3.55993977187034},
            {2.58348718527599, 2.88369329732423, 3.05299572291218, 3.16696362048174, 3.25804535392093, 3.3295646396892, 3.38392194807778, 3.43436982781932, 3.48085775726461, 3.52469711622468},
            {2.56693398372472, 2.8625627702299, 3.05252848226826, 3.14583874012616, 3.22155504245902, 3.29960927907974, 3.35961220753581, 3.40319392716715, 3.45137554345273, 3.49075508696677},
            {2.55237963018225, 2.84402887443522, 3.00188534026651, 3.120140289455, 3.20524916386501, 3.27753396660033, 3.33217055880051, 3.38639707308503, 3.42887732791494, 3.4631586722364},
            {2.53948319062396, 2.82759156276287, 2.98515776951321, 3.10037347692132, 3.18561187864776, 3.25657934768383, 3.31230436539752, 3.35582296817795, 3.40316966594538, 3.43408091853776},
            {2.52797700274157, 2.81293678910904, 2.97650779650281, 3.08268713417925, 3.1604289547967, 3.23385002556629, 3.2879071979654, 3.33180833251404, 3.37838122079902, 3.41460427991247},
            {2.51764801604474, 2.7997898952386, 2.95701279477513, 3.06129303423552, 3.14872381027327, 3.21707116385943, 3.27220941338774, 3.31935937182201, 3.35876221238478, 3.39375037712349},
            {2.50832455289908, 2.78792982779159, 2.9422743573848, 3.04977624304494, 3.14348552651609, 3.20538912997796, 3.25158407361103, 3.30064656902347, 3.33864791717523, 3.38025024978993},
            {2.49986673949467, 2.77717668231239, 2.93056511608397, 3.03352244608355, 3.11614212907501, 3.18009815200034, 3.23684627047332, 3.28819052094428, 3.32377205010499, 3.36057156999303},
            {2.49215947315776, 2.76738256319643, 2.91912972116657, 3.02435748230746, 3.10295092694579, 3.17196887605574, 3.21839218012093, 3.26929653188476, 3.30868114821459, 3.34523770942876},
            {2.48510717541076, 2.75842477424791, 2.91096076845553, 3.02323740836708, 3.09127482557764, 3.16118174748596, 3.21116616352813, 3.26120434507106, 3.29989885365238, 3.33195108547006},
            {2.47862982359124, 2.75022908781105, 2.8898694229984, 3.00088141152564, 3.09083322496697, 3.14418102440856, 3.20287035225332, 3.24925325183502, 3.2871462041325, 3.32150664945092},
            {2.47265991195601, 2.7426505446535, 2.89267724322336, 2.9879575298065, 3.07054582676109, 3.13703623638929, 3.18916284027766, 3.23345660859443, 3.27362000137833, 3.30855597949776},
            {2.46714009796747, 2.73564589666184, 2.88660410480268, 2.98545902436576, 3.06699362425439, 3.12177499334621, 3.18369346841265, 3.23058788609407, 3.26457218522626, 3.29663843540761},
            {2.46202136015041, 2.72915238096577, 2.87473720671939, 2.98269843365211, 3.05509571308851, 3.12065549100488, 3.16815144932484, 3.2158501535135, 3.2564922966556, 3.28950961091207},
            {2.45726154240059, 2.72311605869519, 2.87376771776306, 2.96660815565051, 3.03887316771619, 3.10764235052687, 3.16604219460488, 3.20131622019502, 3.24323584143059, 3.27893236292878},
            {2.45282419340265, 2.71749031782246, 2.86905651167713, 2.96191168627698, 3.04213105498606, 3.10604109707832, 3.15308532296685, 3.20252684711989, 3.23671847193345, 3.26801486528705},
            {2.44867763367205, 2.71223467066035, 2.85719378389962, 2.96325075749049, 3.04007105710076, 3.09632920917986, 3.14556692808623, 3.19331128106129, 3.22795231586459, 3.2639626464525},
            {2.44479419980781, 2.70731378054151, 2.85503490084349, 2.95734060983507, 3.03642003245691, 3.08793401389267, 3.14138673916605, 3.18574219885617, 3.2213905631436, 3.25808685661213},
            {2.44114962790648, 2.70269666831317, 2.84494646291104, 2.94808020274335, 3.02810826976263, 3.08515510018985, 3.13695425413613, 3.1785571610378, 3.21413586494326, 3.24829670993525},
            {2.43772254714374, 2.69835606106543, 2.83503005820281, 2.94545656567713, 3.01791464225035, 3.08063365606487, 3.13299614003778, 3.16950190259832, 3.20991960408766, 3.24301340602694},
            {2.43449406123114, 2.69426785422464, 2.84087082003415, 2.93795120320695, 3.00952491511354, 3.07404088830039, 3.120636582462, 3.16560675257434, 3.20222449759309, 3.23790271493265},
            {2.43144740046467, 2.69041061323816, 2.83516672073616, 2.93053380694335, 3.01597389533905, 3.0677950418863, 3.12043440227001, 3.15866772031342, 3.19588527562995, 3.23052054892098},
            {2.42856763085909, 2.68676558931895, 2.83357343038595, 2.92750796344435, 3.00495661092256, 3.05835756231798, 3.11080199651706, 3.16000260388267, 3.19208467849739, 3.22517058966928},
            {2.42584140973563, 2.68331529578786, 2.82978837527288, 2.92827601125176, 2.99559940619599, 3.05895920101269, 3.10597977098324, 3.14767225881493, 3.18734025218195, 3.21978403975538},
            {2.42325677933486, 2.68004477956526, 2.82310406730253, 2.92145070086871, 2.99560627936761, 3.05026166890121, 3.0999484655279, 3.14624640819795, 3.18384469534252, 3.21039540507956},
            {2.42080299172908, 2.67694035058459, 2.81948064007075, 2.91589830644077, 2.9900617917428, 3.0513147597081, 3.10021079463266, 3.14174452422064, 3.17689718058519, 3.20956812044383},
            {2.41847035963464, 2.67398967343349, 2.81146548874102, 2.91734642791315, 2.99183302084426, 3.04393500419455, 3.09341082760355, 3.13595915630858, 3.17311538704127, 3.2049931860042},
            {2.41625012876297, 2.67118159954985, 2.80667868617878, 2.90400649832498, 2.98483010873651, 3.03996752569499, 3.09077195328653, 3.13548577905527, 3.17034050471506, 3.20208481218194},
            {2.41413436816874, 2.66850603650255, 2.8151277163234, 2.90409366319487, 2.98987322500725, 3.03457715162534, 3.07898536167141, 3.12797354950389, 3.16373163747029, 3.1964719952296},
            {2.41211587570336, 2.66595382339379, 2.8006308587656, 2.90237562197352, 2.96990554903168, 3.03459593754101, 3.08452182862621, 3.12049208548768, 3.16313248834281, 3.19230265961184},
            {2.41018809620138, 2.66351662452049, 2.80141177389525, 2.89881420923683, 2.97664266596411, 3.02596809351409, 3.07869222523931, 3.12037078278521, 3.15705555488005, 3.18865766607817},
            {2.40834505044343, 2.66121681922685, 2.80487093525679, 2.89743137041406, 2.97225871470651, 3.02984168916032, 3.08249984017474, 3.11645954883976, 3.15531300097344, 3.18493306538319},
            {2.40658127327561, 2.65898672860016, 2.79350554198715, 2.89629361117451, 2.96116854771965, 3.02428844976574, 3.07284658706055, 3.11294605165789, 3.1526108270294, 3.18404761195155},
            {2.40489175953767, 2.65685040683097, 2.79386159799205, 2.88485357163574, 2.96231820419485, 3.02012212827202, 3.06920863140795, 3.10957384054625, 3.1455696804995, 3.1727168995588},
            {2.40327191667417, 2.65480278347574, 2.79717231958895, 2.88933384363132, 2.9629326424692, 3.01728916625973, 3.0726329709504, 3.10845299998482, 3.14503869381848, 3.1789696624176},
            {2.4017175230847, 2.6528381065117, 2.78681172582377, 2.8896029733049, 2.9599345382294, 3.01646703631342, 3.06672882262498, 3.10593461205398, 3.13942053915568, 3.17422502662088},
            {2.40022469141838, 2.65095143683576, 2.78606026001096, 2.88325434023165, 2.95382962788069, 3.0177039958552, 3.05972198489725, 3.10268748776445, 3.14069251563686, 3.17122589357531},
            {2.39878983614144, 2.64913819296906, 2.79891650088476, 2.8772720723351, 2.96113165873128, 3.00964924462543, 3.05936013718209, 3.10270366945412, 3.13506585287725, 3.16889905051578},
            {2.39740964480846, 2.64739417113623, 2.78511034111243, 2.88558968756323, 2.94924017549723, 3.00328364296112, 3.05564314411557, 3.09676110648409, 3.13214942446484, 3.16475842308432},
            {2.39608105255332, 2.64571550391541, 2.78054730809681, 2.87910872315717, 2.94608820485985, 3.00468652703675, 3.05386020869476, 3.0912003799932, 3.13187952590037, 3.16122523429747},
            {2.39480121938657, 2.64409858600747, 2.7851636848446, 2.87554810487343, 2.94822395001963, 3.00478675016558, 3.05638843969672, 3.09107103742545, 3.12993494272954, 3.15764960643447},
            {2.39356750994555, 2.64254007245714, 2.78478649796949, 2.87441594698397, 2.94399263301239, 3.00363001943324, 3.04575538721757, 3.09449816012518, 3.12490798302937, 3.15637259305469},
            {2.39237747539368, 2.64103685557427, 2.78510391387377, 2.87232758593581, 2.93757245468076, 2.99934488424451, 3.04436905769322, 3.09139789415543, 3.11957755264798, 3.15678964239476},
            {2.39122883720736, 2.63958604426689, 2.77900253445336, 2.8684474646258, 2.94029653146996, 2.99671665308416, 3.04977512658292, 3.08525963150693, 3.12165114887075, 3.15469616711441},
            {2.39011947262491, 2.63818494549719, 2.77333473640525, 2.86444016981279, 2.94301926616138, 2.9942108402074, 3.04515211802243, 3.08026951498761, 3.11924849862914, 3.14693984229218}};

    /** The quantile from a multi-variate t-distribution with common correlation of 0.5.
     *  This is table look up for values of Table B.3 from Bechhofer, Santner, and Goldsman (1995)
     *  "Design and Analysis of Experiments for Statistical Selection, Screening, and Multiple
     *  Comparisons"
     *  
     * @param level must be 0.90, 0.95, or 0.99
     * @param dof the degrees of freedom, must be [1,60]
     * @param nDim the number of dimensions of the distribution, must be [1,10]
     * @return the quantile of the multi-variate t distribution or Double.NaN
     */
    public static double mvtQuantile(double level, int dof, int nDim){
       if (level == 0.90){
            return mvtQuantile90(dof, nDim);
        } else if (level == 0.95){
            return mvtQuantile95(dof, nDim);
        } else if (level == 0.99){
            return mvtQuantile99(dof, nDim);
        } else {
           return Double.NaN;
       }
    }

    /**
     *
     * @param dof the degrees of freedom, must be [1,60]
     * @param nDim the number of dimensions of the distribution, must be [1,10]
     * @return the 90 percent quantile of the multi-variate t distribution
     */
    public static double mvtQuantile90(int dof, int nDim){
        if ((dof < 1) || (dof > 60)) {
            throw new IllegalArgumentException("The look up is limited to dof >=1 and dof <= 60");
        }
        if ((nDim < 1) || (nDim > 10)) {
            throw new IllegalArgumentException("The look up is limited to nDim >=1 and nDim <= 10");
        }
        return QMVT90[dof-1][nDim-1];
    }

    /**
     *
     * @param dof the degrees of freedom, must be [1,60]
     * @param nDim the number of dimensions of the distribution, must be [1,10]
     * @return the 95 percent quantile of the multi-variate t distribution
     */
    public static double mvtQuantile95(int dof, int nDim){
        if ((dof < 1) || (dof > 60)) {
            throw new IllegalArgumentException("The look up is limited to dof >=1 and dof <= 60");
        }
        if ((nDim < 1) || (nDim > 10)) {
            throw new IllegalArgumentException("The look up is limited to nDim >=1 and nDim <= 10");
        }
        return QMVT95[dof-1][nDim-1];
    }

    /**
     *
     * @param dof the degrees of freedom, must be [1,60]
     * @param nDim the number of dimensions of the distribution, must be [1,10]
     * @return the 99 percent quantile of the multi-variate t distribution
     */
    public static double mvtQuantile99(int dof, int nDim){
        if ((dof < 1) || (dof > 60)) {
            throw new IllegalArgumentException("The look up is limited to dof >=1 and dof <= 60");
        }
        if ((nDim < 1) || (nDim > 10)) {
            throw new IllegalArgumentException("The look up is limited to nDim >=1 and nDim <= 10");
        }
        return QMVT99[dof-1][nDim-1];
    }

    public static void main(String args[]) {
        LinkedHashMap<String, double[]> data = new LinkedHashMap<>();
        double[] d1 = {63.72, 32.24, 40.28, 36.94, 36.29, 56.94, 34.10, 63.36, 49.29, 87.20};
        double[] d2 = {63.06, 31.78, 40.32, 37.71, 36.79, 57.93, 33.39, 62.92, 47.67, 80.79};
        double[] d3 = {57.74, 29.65, 36.52, 35.71, 33.81, 51.54, 31.39, 57.24, 42.63, 67.27};
        double[] d4 = {62.63, 31.56, 39.87, 37.35, 36.65, 57.15, 33.30, 62.21, 47.46, 79.60};
        data.put("One", d1);
        data.put("Two", d2);
        data.put("Three", d3);
        data.put("Four", d4);

        MultipleComparisonAnalyzer mca = new MultipleComparisonAnalyzer(data);

        PrintWriter out = new PrintWriter(System.out, true);
        mca.writeDataAsCSVFile(out);
        out.println();

        System.out.println(mca);

        System.out.println("num data sets: " + mca.getNumberDatasets());
        System.out.println(Arrays.toString(mca.getDataNames()));

    }


}
