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

package jsl.utilities.random.sp;

import jsl.utilities.IdentityIfc;
import jsl.utilities.NewInstanceIfc;
import jsl.utilities.random.ParametersIfc;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.BernoulliRV;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.statistic.Statistic;

import java.util.Objects;

/**
 * Represents a two state Markov chain
 * States = {0,1}
 * User supplies
 * P{X(i) = 1| X(i-1) = 1} Probability of success after success
 * P{X(i) = 1| X(i-1) = 0} Probability of success after failure
 *
 * @author rossetti
 */
public class TwoStateMarkovChain implements TwoStateMarkovChainIfc, IdentityIfc,
        ParametersIfc, NewInstanceIfc<TwoStateMarkovChain>, RandomIfc {

    /**
     * A counter to count the number of created to assign "unique" ids
     */
    private static int myIdCounter_;

    /**
     * The id of this object
     */
    protected int myId;

    /**
     * Holds the name of the name of the object for the IdentityIfc
     */
    protected String myName;

    private double myP1;

    private double myP0;

    private BernoulliRV myB11;

    private BernoulliRV myB01;

    private RNStreamIfc myStream;

    private int myState;

    private int myInitialState;

    public TwoStateMarkovChain() {
        this(1, 0.5, 0.5);
    }

    public TwoStateMarkovChain(double p11, double p01) {
        this(1, p11, p01);
    }

    public TwoStateMarkovChain(double[] parameters) {
        this((int) parameters[2], parameters[0], parameters[1]);
    }

    public TwoStateMarkovChain(int initialState, double p11, double p01) {
        this(initialState, p11, p01, JSLRandom.nextRNStream());
    }

    public TwoStateMarkovChain(int initialState, double p11, double p01, RNStreamIfc rng) {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        setInitialState(initialState);
        setProbabilities(p11, p01, rng);
        reset();
    }

    @Override
    public final int getId() {
        return (myId);
    }

    @Override
    public final String getName() {
        return myName;
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            myName = this.getClass().getSimpleName();
        } else {
            myName = str;
        }
    }

    public void setInitialState(int initialState) {
        if ((initialState < 0) || (initialState > 1)) {
            throw new IllegalArgumentException("The initial state must be 0 or 1");
        }
        myInitialState = initialState;
    }

    public int getInitialState() {
        return myInitialState;
    }

    public void setState(int state) {
        if ((state < 0) || (state > 1)) {
            throw new IllegalArgumentException("The state must be 0 or 1");
        }
        myState = state;
    }

    public int getState() {
        return myState;
    }

    public void setProbabilities(double p11, double p01) {
        setProbabilities(p11, p01, JSLRandom.nextRNStream());
    }

    public void setProbabilities(double p11, double p01, RNStreamIfc stream) {
        if ((p11 < 0.0) || (p11 > 1.0)) {
            throw new IllegalArgumentException("P11 must be [0,1]");
        }
        if ((p01 < 0.0) || (p01 > 1.0)) {
            throw new IllegalArgumentException("P11 must be [0,1]");
        }
        setRandomNumberStream(stream);
        myB11 = new BernoulliRV(p11, stream);
        myB01 = new BernoulliRV(p01, stream);
        myP0 = 1 - (p01 / (1 - p11 + p01));
        myP1 = 1 - myP0;
    }

    /**
     * Sets the state back to the initial state
     */
    public void reset() {
        myState = myInitialState;
    }

    public double getValue() {
        if (myState == 1) {
            myState = (int) myB11.getValue();
        } else {
            myState = (int) myB01.getValue();
        }
        return myState;
    }

    @Override
    public final double sample() {
        return getValue();
    }

    public double getP0() {
        return myP0;
    }

    public double getP1() {
        return myP1;
    }

    public double getP01() {
        return myB01.getProbabilityOfSuccess();
    }

    public double getP11() {
        return myB11.getProbabilityOfSuccess();
    }

    /**
     * The array consists of:
     * p[0] = p11
     * p[1] = p01
     * p[2] = initial state
     *
     * @return the array of parameters
     */
    public double[] getParameters() {
        double[] p = {getP11(), getP01(), getInitialState()};
        return p;
    }

    /**
     * Supply an array with:
     * p[0] = p11
     * p[1] = p01
     * p[2] = initial state
     *
     * @param parameters the parameters of the Markov chain
     */
    @Override
    public void setParameters(double[] parameters) {
        setProbabilities(parameters[0], parameters[1]);
        setInitialState((int) parameters[2]);
    }

    @Override
    public final RNStreamIfc getRandomNumberStream() {
        return myStream;
    }

    @Override
    public final void setRandomNumberStream(RNStreamIfc stream) {
        Objects.requireNonNull(stream, "The supplied stream was null");
        myStream = stream;
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myStream.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myStream.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myStream.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myStream.setResetStartStreamOption(b);
    }

    @Override
    public void advanceToNextSubStream() {
        myStream.advanceToNextSubStream();
    }

    @Override
    public void resetStartStream() {
        myStream.resetStartStream();
    }

    @Override
    public void resetStartSubStream() {
        myStream.resetStartSubStream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myStream.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myStream.getAntitheticOption();
    }

    /**
     * The instance is initialized at getInitialState()
     *
     * @return the instance is initialized at getInitialState()
     */
    @Override
    public TwoStateMarkovChain newInstance() {
        return (new TwoStateMarkovChain(getInitialState(), getP11(), getP01()));
    }

    /**
     * The instance is initialized at getInitialState()
     *
     * @return the instance is initialized at getInitialState()
     */
//    @Override
    public TwoStateMarkovChain newInstance(RNStreamIfc rng) {
        return (new TwoStateMarkovChain(getInitialState(), getP11(), getP01(), rng));
    }

    public static void main(String[] args) {
        Statistic s = new Statistic();
        TwoStateMarkovChain d = new TwoStateMarkovChain();
        for (int i = 1; i <= 20000; i++) {
            double x = d.getValue();
            s.collect(x);
            //System.out.println(x);
        }
        System.out.println(s);
    }

}
