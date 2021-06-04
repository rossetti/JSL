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

package jsl.utilities.random.mcmc;

import jsl.observers.ObservableComponent;
import jsl.observers.ObservableIfc;
import jsl.observers.ObserverIfc;
import jsl.utilities.random.rng.GetRandomNumberStreamIfc;
import jsl.utilities.random.rng.RNStreamControlIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rng.SetRandomNumberStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.statistic.Statistic;

import java.util.*;

/**
 *  An implementation for a multi-Dimensional Metropolis Hasting process. The
 *  process is observable at each step
 */
public class MetropolisHastingsMV implements RNStreamControlIfc, SetRandomNumberStreamIfc, GetRandomNumberStreamIfc, ObservableIfc {

    protected double[] myCurrentX;

    protected double[] myProposedY;

    protected double[] myPrevX;

    protected double myLastAcceptanceProbability;

    protected double myFofProposedY;

    protected double myFofCurrentX;

    protected double[] myInitialX;

    protected final FunctionMVIfc myTargetFun;

    protected final ProposalFunctionMVIfc myProposalFun;

    protected final Statistic myAcceptanceStat;

    protected final List<Statistic> myObservedStatList;

    protected final ObservableComponent myObservableComponent;

    protected boolean myInitializedFlag;

    protected boolean myBurnInFlag;

    /**
     * myRNStream provides a reference to the underlying stream of random numbers
     */
    protected RNStreamIfc myStream;

    /**
     *
     * @param initialX the initial value to start generation process
     * @param targetFun the target function
     * @param proposalFun the proposal function
     */
    public MetropolisHastingsMV(double[] initialX, FunctionMVIfc targetFun, ProposalFunctionMVIfc proposalFun) {
        if (initialX == null){
            throw new IllegalArgumentException("The initial state was null!");
        }
        if (targetFun == null){
            throw new IllegalArgumentException("The target function was null!");
        }
        if (proposalFun == null){
            throw new IllegalArgumentException("The proposal function was null!");
        }
        myInitialX = Arrays.copyOf(initialX, initialX.length);
        myInitializedFlag = false;
        myBurnInFlag = false;
        myTargetFun = targetFun;
        myProposalFun = proposalFun;
        myAcceptanceStat = new Statistic("Acceptance Statistics");
        myObservedStatList = new ArrayList<>();
        for(int i=0;i<myInitialX.length;i++){
            myObservedStatList.add(new Statistic("X:" + (i+1)));
        }
        myStream = JSLRandom.nextRNStream();
        myObservableComponent = new ObservableComponent();
    }

    /**
     *
     * @param initialX the initial value to start the burn in period
     * @param burnInAmount the number of samples in the burn in period
     * @param targetFun the target function
     * @param proposalFun the proposal function
     */
    public static MetropolisHastingsMV create(double[] initialX, int burnInAmount, FunctionMVIfc targetFun,
                                              ProposalFunctionMVIfc proposalFun){
        MetropolisHastingsMV m = new MetropolisHastingsMV(initialX, targetFun, proposalFun);
        m.runBurnInPeriod(burnInAmount);
        return m;
    }

    /** Runs a burn in period and assigns the initial value of the process to the last
     * value from the burn in process.
     *
     * @param burnInAmount the amount to burn in
     */
    public void runBurnInPeriod(int burnInAmount){
        double[] x = runAll(burnInAmount);
        myBurnInFlag = true;
        myInitializedFlag = false;
        myInitialX = x;
        resetStatistics();
    }

    /**  Resets statistics and sets the initial state the the initial value or to the value
     *  found via the burn in period (if the burn in period was run).
     *
     */
    public void initialize(){
        myInitializedFlag = true;
        myCurrentX = myInitialX;
        resetStatistics();
    }

    /**
     *  Resets the automatically collected statistics
     */
    public void resetStatistics(){
        for(Statistic s: myObservedStatList){
            s.reset();
        }
        myAcceptanceStat.reset();
    }

    /**
     *
     * @return true if the process has been initialized
     */
    public final boolean isInitialized(){
        return myInitializedFlag;
    }

    /**
     *
     * @return true if the process has been warmed up
     */
    public final boolean isWarmedUp(){
        return myBurnInFlag;
    }

    /**
     *
     * @param n  runs the process for n steps
     * @return the value of the process after n steps
     */
    public final double[] runAll(int n){
        if (n <= 0){
            throw new IllegalArgumentException("The number of iterations to run was less than or equal to zero.");
        }
        initialize();
        double[] value = null;
        for(int i=1;i<=n;i++){
            value = next();
        }
        return Arrays.copyOf(value, value.length);
    }

    /**
     *
     * @return the current state (x) of the process
     */
    public final double[] getCurrentX() {
        return Arrays.copyOf(myCurrentX, myCurrentX.length);
    }

    /**
     *
     * @return the last proposed state (y)
     */
    public final double[] getProposedY() {
        return Arrays.copyOf(myProposedY, myProposedY.length);
    }

    /**
     *
     * @return the previous state (x) of the process
     */
    public final double[] getPrevX() {
        return Arrays.copyOf(myPrevX, myPrevX.length);
    }

    /**
     *
     * @return the last value of the computed probability of acceptance
     */
    public final double getLastAcceptanceProbability() {
        return myLastAcceptanceProbability;
    }

    /**
     *
     * @return the last value of the target function evaluated at the proposed state (y)
     */
    public final double getFofProposedY() {
        return myFofProposedY;
    }

    /**
     *
     * @return the last value of the target function evaluated at the current state (x)
     */
    public final double getFofCurrentX() {
        return myFofCurrentX;
    }

    /**
     *
     * @return statistics for the proportion of the proposed state (y) that are accepted
     */
    public Statistic getAcceptanceStat() {
        return myAcceptanceStat.newInstance();
    }

    /**
     *
     * @return statistics on the observed (generated) values of the process
     */
    public List<Statistic> getObservedStat() {
        return Collections.unmodifiableList(myObservedStatList);
    }

    /** Moves the process one step
     *
     * @return the next value of the process after proposing the next state (y)
     */
    public double[] next(){
        if (!isInitialized()){
            initialize();
        }
        myPrevX = myCurrentX;
        myProposedY = myProposalFun.generateProposedGivenCurrent(myCurrentX);
        myLastAcceptanceProbability = acceptanceFunction(myCurrentX, myProposedY);
        if (myStream.randU01() <= myLastAcceptanceProbability) {
            myCurrentX = myProposedY;
            myAcceptanceStat.collect(1.0);
        } else {
            myAcceptanceStat.collect(0.0);
        }
        for(int i=0;i<myCurrentX.length;i++){
            myObservedStatList.get(0).collect(myCurrentX[i]);
        }
        myObservableComponent.notifyObservers(this, this);
        return myCurrentX;
    }

    /** Computes the acceptance function for each step
     *
     * @param currentX the current state
     * @param proposedY the proposed state
     * @return the evaluated acceptance function
     */
    protected double acceptanceFunction(double[] currentX, double[] proposedY){
        double fRatio = getFunctionRatio(currentX, proposedY);
        double pRatio = myProposalFun.getProposalRatio(currentX, proposedY);
        double ratio = fRatio*pRatio;
        return Math.min(ratio, 1.0);
    }

    /**
     *
     * @param currentX the current state
     * @param proposedY the proposed state
     * @return the ratio of f(y)/f(x) for the generation step
     */
    protected double getFunctionRatio(double[] currentX, double[] proposedY){
        double fx = myTargetFun.fx(currentX);
        double fy = myTargetFun.fx(proposedY);
        if (fx < 0.0){
            throw new IllegalStateException("The target function was < 0 at current state");
        }
        if (fy < 0.0){
            throw new IllegalStateException("The proposal function was < 0 at proposed state");
        }
        double ratio;
        if (fx != 0.0){
            ratio = fy/fx;
            myFofCurrentX = fx;
            myFofProposedY = fy;
        } else {
            ratio = Double.POSITIVE_INFINITY;
            myFofCurrentX = fx;
            myFofProposedY = fy;
        }
        return ratio;
    }

    /**
     *
     * @return the specified initial state or the state after the burn in period (if run)
     */
    public final double[] getInitialX() {
        return Arrays.copyOf(myInitialX, myInitialX.length);
    }

    /**
     *
     * @param initialX the value to use for the initial state
     */
    public final void setInitialX(double[] initialX) {
        if (initialX == null){
            throw new IllegalArgumentException("The initial state was null!");
        }

        myInitialX = Arrays.copyOf(initialX, initialX.length);
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
    public void resetStartStream() {
        myStream.resetStartStream();
    }

    @Override
    public void resetStartSubstream() {
        myStream.resetStartSubstream();
    }

    @Override
    public void advanceToNextSubstream() {
        myStream.advanceToNextSubstream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myStream.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myStream.getAntitheticOption();
    }

    @Override
    public void addObserver(ObserverIfc observer) {
        myObservableComponent.addObserver(observer);
    }

    @Override
    public void deleteObserver(ObserverIfc observer) {
        myObservableComponent.deleteObserver(observer);
    }

    @Override
    public void deleteObservers() {
        myObservableComponent.deleteObservers();
    }

    @Override
    public boolean contains(ObserverIfc observer) {
        return myObservableComponent.contains(observer);
    }

    @Override
    public int countObservers() {
        return myObservableComponent.countObservers();
    }

    @Override
    public String toString(){
        return asString();
    }

    public String asString() {
        final StringBuilder sb = new StringBuilder("MetropolisHastings1D");
        sb.append(System.lineSeparator());
        sb.append("Initialized Flag = ").append(myInitializedFlag);
        sb.append(System.lineSeparator());
        sb.append("Burn In Flag = ").append(myBurnInFlag);
        sb.append(System.lineSeparator());
        sb.append("Initial X =").append(Arrays.toString(myInitialX));
        sb.append(System.lineSeparator());
        sb.append("Current X = ").append(Arrays.toString(myCurrentX));
        sb.append(System.lineSeparator());
        sb.append("Previous X = ").append(Arrays.toString(myPrevX));
        sb.append(System.lineSeparator());
        sb.append("Last Proposed Y= ").append(Arrays.toString(myProposedY));
        sb.append(System.lineSeparator());
        sb.append("Last Prob. of Acceptance = ").append(myLastAcceptanceProbability);
        sb.append(System.lineSeparator());
        sb.append("Last f(Y) = ").append(myFofProposedY);
        sb.append(System.lineSeparator());
        sb.append("Last f(X) = ").append(myFofCurrentX);
        sb.append(System.lineSeparator());
        sb.append("Acceptance Statistics");
        sb.append(System.lineSeparator());
        sb.append(myAcceptanceStat.asString());
        sb.append(System.lineSeparator());
        for(Statistic s: myObservedStatList){
            sb.append(s.asString());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
