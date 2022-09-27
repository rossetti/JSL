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
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.statistic.Statistic;

import java.util.Objects;

/**
 *  An implementation for a 1-Dimensional Metropolis Hasting process. The
 *  process is observable at each step
 */
public class MetropolisHastings1D implements RandomIfc, ObservableIfc {

    protected double myCurrentX;

    protected double myProposedY;

    protected double myPrevX;

    protected double myLastAcceptanceProbability;

    protected double myFofProposedY;

    protected double myFofCurrentX;

    protected double myInitialX;

    protected final FunctionIfc myTargetFun;

    protected final ProposalFunction1DIfc myProposalFun;

    protected final Statistic myAcceptanceStat;

    protected final Statistic myObservedStat;

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
    public MetropolisHastings1D(double initialX, FunctionIfc targetFun, ProposalFunction1DIfc proposalFun) {
        if (targetFun == null){
            throw new IllegalArgumentException("The target function was null!");
        }
        if (proposalFun == null){
            throw new IllegalArgumentException("The proposal function was null!");
        }
        myInitialX = initialX;
        myInitializedFlag = false;
        myBurnInFlag = false;
        myTargetFun = targetFun;
        myProposalFun = proposalFun;
        myAcceptanceStat = new Statistic("Acceptance Statistics");
        myObservedStat = new Statistic("Observed Value Statistics");
        myStream = JSLRandom.nextRNStream();
        myObservableComponent = new ObservableComponent();
    }

    /**
     *
     * @param initialX the initial value to start the burn in period
     * @param burnInAmount the number of samples in the burn in period
     * @param targetFun the target function
     * @param proposalFun the proposal function
     * @return the created instance
     */
    public static MetropolisHastings1D create(double initialX, int burnInAmount, FunctionIfc targetFun,
                                              ProposalFunction1DIfc proposalFun){
        MetropolisHastings1D m = new MetropolisHastings1D(initialX, targetFun, proposalFun);
        m.runBurnInPeriod(burnInAmount);
        return m;
    }

    /** Runs a burn in period and assigns the initial value of the process to the last
     * value from the burn in process.
     *
     * @param burnInAmount the amount of sampling for the burn-in (warm up) period
     */
    public void runBurnInPeriod(int burnInAmount){
        double x = runAll(burnInAmount);
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
        myObservedStat.reset();
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
    public final double runAll(int n){
        if (n <= 0){
            throw new IllegalArgumentException("The number of iterations to run was less than or equal to zero.");
        }
        initialize();
        double value = 0;
        for(int i=1;i<=n;i++){
            value = next();
        }
        return value;
    }

    /**
     *
     * @return the current state (x) of the process
     */
    public final double getCurrentX() {
        return myCurrentX;
    }

    /**
     *
     * @return the last proposed state (y)
     */
    public final double getProposedY() {
        return myProposedY;
    }

    /**
     *
     * @return the previous state (x) of the process
     */
    public final double getPrevX() {
        return myPrevX;
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
    public Statistic getAcceptanceStatistics() {
        return myAcceptanceStat.newInstance();
    }

    /**
     *
     * @return statistics on the observed (generated) values of the process
     */
    public Statistic getObservedStatistics() {
        return myObservedStat.newInstance();
    }

    /** Moves the process one step
     *
     * @return the next value of the process after proposing the next state (y)
     */
    public double next(){
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
        myObservedStat.collect(myCurrentX);
        myObservableComponent.notifyObservers(this, this);
        return myCurrentX;
    }

    @Override
    public final double getValue(){
        return next();
    }

    @Override
    public final double sample(){
        return getValue();
    }

    /** Computes the acceptance function for each step
     *
     * @param currentX the current state
     * @param proposedY the proposed state
     * @return the evaluated acceptance function
     */
    protected double acceptanceFunction(double currentX, double proposedY){
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
    protected double getFunctionRatio(double currentX, double proposedY){
        double fx = myTargetFun.fx(currentX);
        double fy = myTargetFun.fx(proposedY);
        if (fx < 0.0){
            throw new IllegalStateException("The target function was < 0 at current = " + currentX);
        }
        if (fy < 0.0){
            throw new IllegalStateException("The proposal function was < 0 at proposed = " + proposedY);
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
    public final double getInitialX() {
        return myInitialX;
    }

    /**
     *
     * @param initialX the value to use for the initial state
     */
    public final void setInitialX(double initialX) {
        this.myInitialX = initialX;
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
    public void resetStartSubStream() {
        myStream.resetStartSubStream();
    }

    @Override
    public void advanceToNextSubStream() {
        myStream.advanceToNextSubStream();
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
        sb.append("Initial X =").append(myInitialX);
        sb.append(System.lineSeparator());
        sb.append("Current X = ").append(myCurrentX);
        sb.append(System.lineSeparator());
        sb.append("Previous X = ").append(myPrevX);
        sb.append(System.lineSeparator());
        sb.append("Last Proposed Y= ").append(myProposedY);
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
        sb.append(myObservedStat.asString());
        return sb.toString();
    }
}
