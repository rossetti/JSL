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

import java.util.*;

import jsl.utilities.distributions.DEmpiricalCDF;

/**
 * This class tabulates the frequency associated with
 * the integers presented to it via the collect() method
 * Every value presented is interpreted as an integer
 * For every value presented a count is maintained.
 * There could be space/time performance issues if
 * the number of different values presented is large.
 * <p>
 * This class can be useful for tabulating a
 * discrete histogram over the values (integers) presented.
 *
 * @author rossetti
 */
public class IntegerFrequency {

    /**
     * A Cell represents a value, count pairing
     */
    private Map<Cell, Cell> myCells;

    /**
     * Collects statistical information
     */
    protected Statistic myStatistic;

    /**
     * Used as a temporary cell during tabulation
     */
    private Cell myTemp;

    /**
     * The smallest value allowed.  Any
     * values &lt; to this value will be counted
     * in the underflow count
     */
    private int myLowerLimit;

    /**
     * The largest value allowed.  Any
     * values &gt; to this value will be counted
     * in the overflow count
     */
    private int myUpperLimit;

    /**
     * Counts of values located below first bin.
     */
    private int myUnderFlowCount;

    /**
     * Counts of values located above last bin.
     */
    private int myOverFlowCount;

    protected String myName;

    /**
     * Can tabulate any integer value
     */
    public IntegerFrequency() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }

    /** Can tabulate any integer value
     *
     * @param name a name for the instance
     */
    public IntegerFrequency(String name) {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE, name);
    }

    /**
     *
     * @param lowerLimit the defined lower limit of the integers, values less than this are not tabulated
     * @param upperLimit the defined upper limit of the integers, values less than this are not tabulated
     */
    public IntegerFrequency(int lowerLimit, int upperLimit) {
        this(lowerLimit, upperLimit, null);
    }

    /**
     *
     * @param lowerLimit the defined lower limit of the integers, values less than this are not tabulated
     * @param upperLimit the defined upper limit of the integers, values less than this are not tabulated
     * @param name a name for the instance
     */
    public IntegerFrequency(int lowerLimit, int upperLimit, String name) {
        if (lowerLimit >= upperLimit) {
            throw new IllegalArgumentException("The lower limit must be < the upper limit");
        }
        myName = name;
        myLowerLimit = lowerLimit;
        myUpperLimit = upperLimit;
        myStatistic = new Statistic(name);
        myTemp = new Cell();
        myCells = new HashMap<Cell, Cell>();

    }

    /**
     * @return the assigned name
     */
    public final String getName() {
        return myName;
    }

    /**
     * @param name the name to assign
     */
    public final void setName(String name) {
        myName = name;
    }

    /**
     * @param intArray collects on the values in the array
     */
    public final void collect(int[] intArray) {
        Objects.requireNonNull(intArray, "The array was null");
        for (int i : intArray) {
            collect(i);
        }
    }

    /**
     * Tabulates the count of the number of i's
     * presented.
     *
     * @param i the presented integer
     */
    public void collect(int i) {
        myStatistic.collect(i);
        if (i < myLowerLimit) {
            myUnderFlowCount = myUnderFlowCount + 1;
        }
        if (i > myUpperLimit) {
            myOverFlowCount = myOverFlowCount + 1;
        }
        // myLowerLimit <= x <= myUpperLimit
        myTemp.myValue = i;
        Cell c = myCells.get(myTemp);
        if (c == null) {
            c = new Cell(i);
            myCells.put(c, c);
        } else {
            c.myCount = c.myCount + 1;
        }
    }

    /**
     *
     * @param i casts the double down to an int
     */
    public void collect(double i){
        collect((int)i);
    }

    /**
     *
     * @param array casts the doubles to ints
     */
    public void collect(double[] array){
        Objects.requireNonNull(array, "The array was null");
        for (double i : array) {
            collect(i);
        }
    }

    /**
     * Resets the statistical collection
     */
    public void reset() {
        myOverFlowCount = 0;
        myUnderFlowCount = 0;
        myStatistic.reset();
        myCells.clear();
    }

    /**
     * The number of observations that fell below the first bin's lower limit
     *
     * @return number of observations that fell below the first bin's lower limit
     */
    public final int getUnderFlowCount() {
        return (myUnderFlowCount);
    }

    /**
     * The number of observations that fell past the last bin's upper limit
     *
     * @return number of observations that fell past the last bin's upper limit
     */
    public final int getOverFlowCount() {
        return (myOverFlowCount);
    }

    /**
     * Returns an array of size getNumberOfCells() containing
     * the values increasing by value
     *
     * @return the array of values observed or an empty array
     */
    public final int[] getValues() {
        if (myCells.isEmpty()) {
            return new int[0];
        }
        SortedSet<Cell> cellSet = getCells();
        int[] v = new int[myCells.size()];
        int i = 0;
        for (Cell c : cellSet) {
            v[i] = c.myValue;
            i++;
        }
        return v;
    }

    /**
     * Returns an array of size getNumberOfCells() containing
     * the frequencies by value
     *
     * @return the array of frequencies observed or an empty array
     */
    public final int[] getFrequencies() {
        if (myCells.isEmpty()) {
            return new int[0];
        }
        SortedSet<Cell> cellSet = getCells();
        int[] v = new int[myCells.size()];
        int i = 0;
        for (Cell c : cellSet) {
            v[i] = c.myCount;
            i++;
        }
        return v;
    }

    /**
     * Returns an array of size getNumberOfCells() containing
     * the proportion by value
     *
     * @return the array of proportions observed or an empty array
     */
    public final double[] getProportions() {
        if (myCells.isEmpty()) {
            return new double[0];
        }
        SortedSet<Cell> cellSet = getCells();
        double[] v = new double[myCells.size()];
        int i = 0;
        for (Cell c : cellSet) {
            v[i] = c.myProportion;
            i++;
        }
        return v;
    }

    /**
     * Returns the cumulative frequency up to an including i
     *
     * @param i the integer for the desired frequency
     * @return the cumulative frequency
     */
    public final int getCumulativeFrequency(int i) {
        if (myCells.isEmpty()) {
            return 0;
        }
        SortedSet<Cell> cellSet = getCells();
        int sum = 0;
        for (Cell c : cellSet) {
            if (c.myValue <= i) {
                sum = sum + c.myCount;
            } else {
                break;
            }
        }
        return sum;
    }

    /**
     * Returns the cumulative proportion up to an including i
     *
     * @param i the integer for the desired proportion
     * @return the cumulative proportion
     */
    public final double getCumulativeProportion(int i) {
        if (myCells.isEmpty()) {
            return 0;
        }
        double n = getTotalCount();
        return (getCumulativeFrequency(i) / n);
    }

    /**
     * Returns a n by 2 array of value, frequency
     * pairs where n = getNummberOfCells()
     *
     * @return the array or an empty array
     */
    public final int[][] getValueFrequencies() {
        if (myCells.isEmpty()) {
            return new int[0][0];
        }
        SortedSet<Cell> cellSet = getCells();
        int[][] v = new int[myCells.size()][2];
        int i = 0;
        for (Cell c : cellSet) {
            v[i][0] = c.myValue;
            v[i][1] = c.myCount;
            i++;
        }
        return v;
    }

    /**
     * Returns a 2 by n array of value, proportion pairs
     * where n = getNumberOfCells()
     * row 0 is the values
     * row 1 is the proportions
     *
     * @return the array or an empty array
     */
    public final double[][] getValueProportions() {
        if (myCells.isEmpty()) {
            return new double[0][0];
        }
        SortedSet<Cell> cellSet = getCells();
        double[][] v = new double[myCells.size()][2];
        int i = 0;
        for (Cell c : cellSet) {
            v[0][i] = c.myValue;
            v[1][i] = c.myProportion;
            i++;
        }
        return v;
    }

    /**
     * Returns a 2 by n array of value, cumulative proportion pairs
     * where n = getNumberOfCells()
     * row 0 is the values
     * row 1 is the cumulative proportions
     *
     * @return the array or an empty array
     */
    public final double[][] getValueCumulativeProportions() {
        if (myCells.isEmpty()) {
            return new double[0][0];
        }
        SortedSet<Cell> cellSet = getCells();
        double[][] v = new double[myCells.size()][2];
        int i = 0;
        double sum = 0.0;
        for (Cell c : cellSet) {
            v[0][i] = c.myValue;
            sum = sum + c.myProportion;
            v[1][i] = sum;
            i++;
        }
        return v;
    }

    /**
     * Returns the number of cells tabulated
     *
     * @return the number of cells tabulated
     */
    public final int getNumberOfCells() {
        return myCells.size();
    }

    /**
     * The total count associated with the values
     *
     * @return total count associated with the values
     */
    public final int getTotalCount() {
        return ((int) myStatistic.getCount());//TODO need to check
        //return ((int) myStatistic.getSumOfWeights());
    }

    /**
     * Returns the current frequency for the provided integer
     *
     * @param x the provided integer
     * @return the frequency
     */
    public final int getFrequency(int x) {
        myTemp.myValue = x;
        Cell c = myCells.get(myTemp);
        if (c == null) {
            return 0;
        } else {
            return c.myCount;
        }
    }

    /**
     * Gets the proportion of the observations that
     * are equal to the supplied integer
     *
     * @param x the integer
     * @return the proportion
     */
    public final double getProportion(int x) {
        myTemp.myValue = x;
        Cell c = myCells.get(myTemp);
        if (c == null) {
            return 0;
        } else {
            double n = getTotalCount();
            return c.myCount / n;
        }
    }

    /**
     * Interprets the elements of x[] as values
     * and returns an array representing the frequency
     * for each value
     *
     * @param x the values for the frequencies
     * @return the returned frequencies
     */
    public final int[] getFrequencies(int[] x) {
        int[] f = new int[x.length];
        for (int j = 0; j < x.length; j++) {
            f[j] = getFrequency(x[j]);
        }
        return f;
    }

    /**
     * Returns a copy of the cells in a list
     * ordered by the value of each cell, 0th element
     * is cell with smallest value, etc
     *
     * @return the list
     */
    public final List<Cell> getCellList() {
        SortedSet<Cell> cellSet = getCells();
        List<Cell> list = new ArrayList<Cell>();
        for (Cell c : cellSet) {
            list.add(c.newInstance());
        }
        return list;
    }

    /**
     * @return a DEmpirical based on the frequencies
     */
    public DEmpiricalCDF createDEmpiricalCDF() {
        // form the array of parameters
        double[][] x = getValueCumulativeProportions();
        return (new DEmpiricalCDF(x[0], x[1]));
    }

    /**
     * Returns a sorted set containing the cells
     *
     * @return the sorted set of cells
     */
    protected final SortedSet<Cell> getCells() {
        SortedSet<Cell> cellSet = new TreeSet<Cell>();
        for (Cell c : myCells.keySet()) {
            double n = getTotalCount();
            c.myProportion = c.myCount / n;
            cellSet.add(c);
        }
        return (cellSet);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Frequency Tabulation ").append(getName()).append(System.lineSeparator());
        sb.append("----------------------------------------").append(System.lineSeparator());
        sb.append("Number of cells = ").append(getNumberOfCells()).append(System.lineSeparator());
        sb.append("Lower limit = ").append(myLowerLimit).append(System.lineSeparator());
        sb.append("Upper limit = ").append(myUpperLimit).append(System.lineSeparator());
        sb.append("Under flow count = ").append(myUnderFlowCount).append(System.lineSeparator());
        sb.append("Over flow count = ").append(myOverFlowCount).append(System.lineSeparator());
        sb.append("Total count = ").append(getTotalCount()).append(System.lineSeparator());
        sb.append("----------------------------------------").append(System.lineSeparator());
        sb.append("Value \t Count \t Proportion\n");
        for (Cell c : getCells()) {
            sb.append(c);
        }
        sb.append("----------------------------------------").append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(myStatistic.toString());
        return (sb.toString());
    }

    /**
     *
     * @return a Statistic over the observed integers
     */
    public final Statistic getStatistic() {
        return myStatistic.newInstance();
    }

    /**
     * Holds the values and their counts
     */
    public class Cell implements Comparable<Cell> {

        private int myValue;

        private int myCount;

        private double myProportion = 0.0;

        public Cell() {
            this(0);
        }

        public Cell(int i) {
            myValue = i;
            myCount = 1;
        }

        public final int getValue() {
            return myValue;
        }

        public final int getCount() {
            return myCount;
        }

        public final double getProportion() {
            return myProportion;
        }

        @Override
        public final int compareTo(Cell cell) {
            if (myValue < cell.myValue) {
                return (-1);
            }
            if (myValue > cell.myValue) {
                return (1);
            }
            return 0;
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cell other = (Cell) obj;
            if (this.myValue != other.myValue) {
                return false;
            }
            return true;
        }

        @Override
        public final int hashCode() {
            return myValue;
        }

        @Override
        public final String toString() {
            return (myValue + " \t " + myCount + " \t " + myProportion + "\n");
        }

        public Cell newInstance() {
            Cell c = new Cell();
            c.myValue = this.myValue;
            c.myCount = this.myCount;
            c.myProportion = this.myProportion;
            return c;
        }
    }

}
