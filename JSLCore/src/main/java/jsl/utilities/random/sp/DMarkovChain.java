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
 *
 * @author rossetti
 */
public class DMarkovChain {

    private int myState;

    private int myInitialState;

    private int[] myStates;

    private double[][] myCDFs;

    /**
     * myRNStream provides a reference to the underlying stream of random numbers
     */
    protected RNStreamIfc myRNG;

    /**
     *
     * @param initialState
     * @param prob
     */
    public DMarkovChain(int initialState, double[][] prob) {
        this(initialState, prob, JSLRandom.nextRNStream());
    }

    /**
     *
     * @param initialState
     * @param prob
     * @param rng
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
     *
     */
    public final void reset() {
        myState = myInitialState;
    }

    public final void setInitialState(int initialState) {
        if ((initialState < 1) || (initialState > myCDFs.length)) {
            throw new IllegalArgumentException("The initial state must be >= 1 and <= " + myCDFs.length);
        }
        myInitialState = initialState;
    }

    public final int getInitialState() {
        return myInitialState;
    }

    public final void setState(int state) {
        if ((state < 1) || (state > myCDFs.length)) {
            throw new IllegalArgumentException("The initial state must be >= 1 and <= " + myCDFs.length);
        }
        myState = state;
    }

    public final int getState() {
        return myState;
    }

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
