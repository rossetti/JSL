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
package jsl.utilities.random.sp;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.statistic.IntegerFrequency;

import java.util.Objects;

/**
 *  Randomly generates the states of a discrete Markov Chain. Assumes that
 *  the states are labeled 1, 2, 3, etc.
 *  The transition probabilities are supplied as an array of arrays.
 *  cdf[0] holds the array of transition probabilities for transition to each state {p11, p12, p13, .., p1n} for state 1
 *  cdf[1] holds the array of transition probabilities for transition to each state {p21, p22, p23, .., p2n} for state 2
 *  etc.
 * @author rossetti
 */
public class DMarkovChain {

    private int myState;

    private int myInitialState;

    private final int[] myStates;

    private final double[][] myCDFs;

    /**
     * myRNStream provides a reference to the underlying stream of random numbers
     */
    protected RNStreamIfc myRNG;

    /**
     * @param initialState the initial starting state as an integer
     * @param prob         the transition probability array, holds the probabilities across the states
     */
    public DMarkovChain(int initialState, double[][] prob) {
        this(initialState, prob, JSLRandom.nextRNStream());
    }

    /**
     * @param initialState the initial starting state as an integer
     * @param prob         the transition probability array, holds the probabilities across the states
     * @param rng          the random number stream
     */
    public DMarkovChain(int initialState, double[][] prob, RNStreamIfc rng) {
        Objects.requireNonNull(prob, "The array was null");
        Objects.requireNonNull(rng, "The RNStreamIfc was null");
        myRNG = rng;
        myCDFs = new double[prob.length][];
        myStates = new int[prob.length];
        for (int r = 0; r < prob.length; r++) {
            if (prob[r].length != prob.length) {
                throw new IllegalArgumentException("The #rows != #cols for probability array");
            }
            myCDFs[r] = JSLRandom.makeCDF(prob[r]);
            myStates[r] = r + 1;
        }
        setInitialState(initialState);
        reset();
    }

    /**
     * Sets the state back to the initial state
     */
    public final void reset() {
        myState = myInitialState;
    }

    /**
     *
     * @param initialState the initial state, must be 1, 2, etc. to number of states
     */
    public final void setInitialState(int initialState) {
        if ((initialState < 1) || (initialState > myCDFs.length)) {
            throw new IllegalArgumentException("The initial state must be >= 1 and <= " + myCDFs.length);
        }
        myInitialState = initialState;
    }

    /**
     *
     * @return the initial state for the chain
     */
    public final int getInitialState() {
        return myInitialState;
    }

    /** Causes the chain to be in the supplied state, without any transition
     *
     * @param state the state to be in
     */
    public final void setState(int state) {
        if ((state < 1) || (state > myCDFs.length)) {
            throw new IllegalArgumentException("The initial state must be >= 1 and <= " + myCDFs.length);
        }
        myState = state;
    }

    /**
     *
     * @return the current state, without the transition
     */
    public final int getState() {
        return myState;
    }

    /** Causes a transition to the next state and returns it
     *
     * @return the next state
     */
    public final int next() {
        myState = JSLRandom.randomlySelect(myStates, myCDFs[myState - 1], myRNG);
        return myState;
    }

    public final RNStreamIfc getRandomNumberGenerator() {
        return (myRNG);
    }

    /**
     * Sets the underlying random number generator for the distribution Throws a
     * NullPointerException if rng is null
     *
     * @param rng the reference to the random number generator
     */
    public final void setRandomNumberGenerator(RNStreamIfc rng) {
        if (rng == null) {
            throw new NullPointerException("RngIfc rng must be non-null");
        }
        myRNG = rng;
    }

    public void advanceToNextSubstream() {
        myRNG.advanceToNextSubstream();
    }

    public void resetStartStream() {
        myRNG.resetStartStream();
    }

    public void resetStartSubstream() {
        myRNG.resetStartSubstream();
    }

    public void setAntitheticOption(boolean flag) {
        myRNG.setAntitheticOption(flag);
    }

    public boolean getAntitheticOption() {
        return myRNG.getAntitheticOption();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        double[][] p = {
                {0.3, 0.1, 0.6},
                {0.4, 0.4, 0.2},
                {0.1, 0.7, 0.2}};

        DMarkovChain mc = new DMarkovChain(1, p);
        IntegerFrequency f = new IntegerFrequency();

        for (int i = 1; i <= 100000; i++) {
            int k = mc.next();
            f.collect(k);
            //System.out.println("state = " + k);
        }
        System.out.println("True Steady State Distribution");
        System.out.println("P{X=1} = " + (238.0 / 854.0));
        System.out.println("P{X=2} = " + (350.0 / 854.0));
        System.out.println("P{X=3} = " + (266.0 / 854.0));
        System.out.println();
        System.out.println("Observed Steady State Distribution");
        System.out.println(f);
    }
}
