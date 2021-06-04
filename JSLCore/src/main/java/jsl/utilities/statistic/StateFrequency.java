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

import jsl.simulation.State;
import jsl.utilities.Identity;
import jsl.utilities.IdentityIfc;
import jsl.utilities.distributions.DEmpiricalCDF;

import java.util.*;

public class StateFrequency implements IdentityIfc {

    private final IntegerFrequency myFreq;

    private final Identity myIdentity;

    private final int[][] myTransCnts;

    private final Set<State> myStates;

    private int myLastValue;

    private State myLastState;

    /**
     *
     * @param numStates the number of states to observe
     */
    public StateFrequency(int numStates) {
        if (numStates <= 1){
            throw new IllegalArgumentException("The number of states must be > 1");
        }
        myIdentity = new Identity();
        myStates = new LinkedHashSet<>();
        for (int i=0; i< numStates; i++){
            myStates.add(new State(i));
        }
        myFreq = new IntegerFrequency(0, numStates - 1, getName());
        myTransCnts = new int[numStates][numStates];
    }

    /**
     *
     * @return a copy of the list of states
     */
    public final List<State> getStates(){
        return new ArrayList<>(myStates);
    }

    /**
     *
     * @return the last state number observed
     */
    public final int getLastValue(){
        return myLastValue;
    }

    /**
     *
     * @return the last state observed
     */
    public final State getLastState(){
        return myLastState;
    }

    /**
     *  Resets the statistical collection
     */
    public void reset() {
        myFreq.reset();
        for (int i = 0; i < myTransCnts.length; i++) {
            Arrays.fill(myTransCnts[i], 0);
        }
        myLastValue = Integer.MIN_VALUE;
    }

    /**
     *
     * @param states an array of states to collect on, must not be null
     */
    public final void collect(State[] states){
        Objects.requireNonNull(states, "The list was null");
        collect(Arrays.asList(states));
    }

    /**
     *
     * @param states a list of states to collect on, must not be null
     */
    public final void collect(List<State> states){
        Objects.requireNonNull(states, "The list was null");
        for(State state: states){
            collect(state);
        }
    }

    /** Tabulate statistics on the state occurrences
     *
     * @param state if state is not one of the states created by this StateFrequency then
     *              it is not tabulated (i.e. it is ignored)
     */
    public void collect(State state){
        if (myStates.contains(state)){
            int newValue = state.getNumber();
            if (myFreq.getTotalCount() > 0){
                // there was a previous value collected, update the transition counts
                myTransCnts[myLastValue][newValue]++;
            }
            myFreq.collect(newValue);
            myLastValue = newValue;
            myLastState = state;
        }
    }

    /**
     *
     * @return an array of the count of state transitions from state i to state j
     */
    public final int[][] getTransitionCounts(){
        int[][] cnt = new int[myStates.size()][myStates.size()];
        for(int i=0;i<myStates.size();i++){
            for(int j=0;j<myStates.size();j++){
                cnt[i][j] = myTransCnts[i][j];
            }
        }
        return cnt;
    }

    /**
     *
     * @return an array of the proportion of state transitions from state i to state j
     */
    public final double[][] getTransitionProportions(){
        double[][] p = new double[myStates.size()][myStates.size()];
        double total = myFreq.getTotalCount();
        if (total >=1) {
            for (int i = 0; i < myStates.size(); i++) {
                double sum = 0.0;
                for (int j = 0; j < myStates.size(); j++) {
                    sum = sum + myTransCnts[i][j];
                }
                if (sum >=1){
                    for (int j = 0; j < myStates.size(); j++) {
                        p[i][j] = myTransCnts[i][j]/sum;
                    }
                }
            }
        }
        return p;
    }

    /** Returns an array of size getNumberOfCells() containing
     *  the values increasing by value
     *
     * @return the array of values observed or an empty array
     */
    public final int[] getValues() {
        return myFreq.getValues();
    }

    /** Returns an array of size getNumberOfCells() containing
     *  the frequencies by value
     *
     * @return the array of frequencies observed or an empty array
     */
    public final int[] getFrequencies() {
        return myFreq.getFrequencies();
    }

    /** Returns an array of size getNumberOfCells() containing
     *  the proportion by value
     *
     * @return the array of proportions observed or an empty array
     */
    public final double[] getProportions() {
        return myFreq.getProportions();
    }

    /** Returns the cumulative frequency up to an including i
     *
     * @param i the integer for the desired frequency
     * @return the cumulative frequency
     */
    public final int getCumulativeFrequency(int i) {
        return myFreq.getCumulativeFrequency(i);
    }

    /** Returns the cumulative proportion up to an including i
     *
     * @param i the integer for the desired proportion
     * @return the cumulative proportion
     */
    public final double getCumulativeProportion(int i) {
        return myFreq.getCumulativeProportion(i);
    }

    /** Returns a n by 2 array of value, frequency
     *  pairs where n = getNummberOfCells()
     *
     * @return the array or an empty array
     */
    public final int[][] getValueFrequencies() {
        return myFreq.getValueFrequencies();
    }

    /** Returns a n by 2 array of value, proportion pairs
     *  where n = getNumberOfCells()
     *
     * @return the array or an empty array
     */
    public final double[][] getValueProportions() {
        return myFreq.getValueProportions();
    }

    /** Returns a n by 2 array of value, cumulative proportion pairs
     *  where n = getNumberOfCells()
     *
     * @return the array or an empty array
     */
    public final double[][] getValueCumulativeProportions() {
        return myFreq.getValueCumulativeProportions();
    }

    /** Returns the number of cells tabulated
     *
     * @return the number of cells tabulated
     */
    public final int getNumberOfCells() {
        return myFreq.getNumberOfCells();
    }

    /** The total count associated with the values
     *
     * @return total count associated with the values
     */
    public final int getTotalCount() {
        return myFreq.getTotalCount();
    }

    /** Returns the current frequency for the provided integer
     *
     * @param x the provided integer
     * @return the frequency
     */
    public final int getFrequency(int x) {
        return myFreq.getFrequency(x);
    }

    /** Gets the proportion of the observations that
     *  are equal to the supplied integer
     *
     * @param x the integer
     * @return the proportion
     */
    public final double getProportion(int x) {
        return myFreq.getProportion(x);
    }

    /** Interprets the elements of x[] as values
     *  and returns an array representing the frequency
     *  for each value
     *
     * @param x the values for the frequencies
     * @return the returned frequencies
     */
    public final int[] getFrequencies(int[] x) {
        return myFreq.getFrequencies(x);
    }

    /** Returns a copy of the cells in a list
     *  ordered by the value of each cell, 0th element
     *  is cell with smallest value, etc
     *
     * @return the list
     */
    public final List<IntegerFrequency.Cell> getCellList() {
        return myFreq.getCellList();
    }

    /**
     *
     * @return a DEmpirical based on the frequencies
     */
    public final DEmpiricalCDF createDEmpiricalCDF() {
        return myFreq.createDEmpiricalCDF();
    }

    /** Returns a sorted set containing the cells
     *
     * @return the sorted set of cells
     */
    public final SortedSet<IntegerFrequency.Cell> getCells() {
        return myFreq.getCells();
    }

    @Override
    public final String getName() {
        return myIdentity.getName();
    }

    @Override
    public final int getId() {
        return myIdentity.getId();
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        myIdentity.setName(str);
    }

    /**
     *
     * @return a Statistic over the observed integers mapped to the states
     */
    public final Statistic getStatistics(){
        return myFreq.myStatistic.newInstance();
    }

    /**
     *
     * @return a string representation
     */
    public String toString(){
        return asString();
    }

    /**
     *
     * @return a string representation
     */
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append("State Frequency Tabulation for: ").append(getName());
        sb.append(System.lineSeparator());
        sb.append("State Labels");
        sb.append(System.lineSeparator());
        for(State state: myStates){
            sb.append(state);
            sb.append(System.lineSeparator());
        }
        sb.append("State transition counts");
        sb.append(System.lineSeparator());
        for(int[] row: myTransCnts){
            sb.append(Arrays.toString(row));
            sb.append(System.lineSeparator());
        }
        sb.append("State transition proportions");
        sb.append(System.lineSeparator());
        double[][] transitionProportions = this.getTransitionProportions();
        for(double[] row: transitionProportions){
            sb.append(Arrays.toString(row));
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append(myFreq.toString());
        sb.append(System.lineSeparator());

        return sb.toString();
    }
}
