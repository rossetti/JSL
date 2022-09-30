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
package jsl.modeling.elements.variable.nhpp;

import jsl.simulation.ModelElement;
import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.EventGeneratorIfc;
import jsl.utilities.random.RandomIfc;
import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.utilities.random.rng.GetRandomNumberStreamIfc;
import jsl.utilities.random.rng.RNStreamControlIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rng.SetRandomNumberStreamIfc;

/**
 * @author rossetti
 */
public class NHPPEventGenerator extends ModelElement implements EventGeneratorIfc, RNStreamControlIfc,
        SetRandomNumberStreamIfc, GetRandomNumberStreamIfc {

    protected EventGenerator myEventGenerator;

    protected NHPPTimeBtwEventRV myTBARV;

    /**
     * @param parent       the parent
     * @param rateFunction the rate function
     * @param listener     the listener for generation
     */
    public NHPPEventGenerator(ModelElement parent, InvertibleCumulativeRateFunctionIfc rateFunction,
                              EventGeneratorActionIfc listener) {
        this(parent, rateFunction, listener, null);
    }

    /**
     * @param parent       the parent
     * @param rateFunction the rate function
     * @param listener     the listener for generation
     * @param name         the name to assign
     */
    public NHPPEventGenerator(ModelElement parent, InvertibleCumulativeRateFunctionIfc rateFunction,
                              EventGeneratorActionIfc listener, String name) {
        super(parent, name);
        myTBARV = new NHPPTimeBtwEventRV(this, rateFunction);
        myEventGenerator = new EventGenerator(this, listener, myTBARV, myTBARV);
    }

    /**
     * @param parent       the parent
     * @param rateFunction the rate function
     * @param listener     the listener for generation
     * @param lastrate     the last rate
     * @param name         the name to assign
     */
    public NHPPEventGenerator(ModelElement parent, InvertibleCumulativeRateFunctionIfc rateFunction,
                              EventGeneratorActionIfc listener, double lastrate, String name) {
        super(parent, name);
        myTBARV = new NHPPTimeBtwEventRV(this, rateFunction, lastrate);
        myEventGenerator = new EventGenerator(this, listener, myTBARV, myTBARV);
    }

    @Override
    public final boolean isEventPending() {
        return myEventGenerator.isEventPending();
    }

    @Override
    public final boolean isGeneratorStarted() {
        return myEventGenerator.isGeneratorStarted();
    }

    @Override
    public double getEndingTime() {
        return myEventGenerator.getEndingTime();
    }


    @Override
    public double getInitialEndingTime() {
        return myEventGenerator.getInitialEndingTime();
    }

    @Override
    public long getMaximumNumberOfEvents() {
        return myEventGenerator.getMaximumNumberOfEvents();
    }

    @Override
    public long getInitialMaximumNumberOfEvents() {
        return myEventGenerator.getInitialMaximumNumberOfEvents();
    }

    @Override
    public long getNumberOfEventsGenerated() {
        return myEventGenerator.getNumberOfEventsGenerated();
    }

    @Override
    public RandomIfc getInitialTimeBetweenEvents() {
        return myEventGenerator.getInitialTimeBetweenEvents();
    }

    @Override
    public RandomIfc getInitialTimeUntilFirstEvent() {
        return myEventGenerator.getInitialTimeUntilFirstEvent();
    }

    @Override
    public boolean isGeneratorDone() {
        return myEventGenerator.isGeneratorDone();
    }

    @Override
    public boolean isSuspended() {
        return myEventGenerator.isSuspended();
    }

    @Override
    public void resume() {
        myEventGenerator.resume();
    }

    @Override
    public void setEndingTime(double endingTime) {
        myEventGenerator.setEndingTime(endingTime);
    }

    @Override
    public void setInitialEndingTime(double endingTime) {
        myEventGenerator.setInitialEndingTime(endingTime);
    }

    @Override
    public void setMaximumNumberOfEvents(long maxNum) {
        myEventGenerator.setMaximumNumberOfEvents(maxNum);
    }

    @Override
    public void setInitialMaximumNumberOfEvents(long maxNumEvents) {
        myEventGenerator.setInitialMaximumNumberOfEvents(maxNumEvents);
    }

    @Override
    public void setTimeBetweenEvents(RandomIfc timeBtwEvents, long maxNumEvents) {
        myEventGenerator.setTimeBetweenEvents(timeBtwEvents, maxNumEvents);
    }

    @Override
    public void setTimeBetweenEvents(RandomIfc timeUntilNext) {
        myEventGenerator.setTimeBetweenEvents(timeUntilNext);
    }

    @Override
    public final RandomIfc getTimeBetweenEvents() {
        return myEventGenerator.getTimeBetweenEvents();
    }

    @Override
    public void setInitialTimeBetweenEventsAndMaxNumEvents(RandomIfc timeBtwEvents, long maxNumEvents) {
        myEventGenerator.setInitialTimeBetweenEventsAndMaxNumEvents(timeBtwEvents, maxNumEvents);
    }

    @Override
    public void setInitialTimeBetweenEvents(RandomIfc timeBtwEvents) {
        myEventGenerator.setInitialTimeBetweenEvents(timeBtwEvents);
    }

    @Override
    public void setInitialTimeUntilFirstEvent(RandomIfc timeUntilFirst) {
        myEventGenerator.setInitialTimeUntilFirstEvent(timeUntilFirst);
    }

    @Override
    public void suspend() {
        myEventGenerator.suspend();
    }

    @Override
    public final boolean getStartOnInitializeFlag() {
        return myEventGenerator.getStartOnInitializeFlag();
    }

    @Override
    public final void setStartOnInitializeFlag(boolean flag) {
        myEventGenerator.setStartOnInitializeFlag(flag);
    }

    @Override
    public final void turnOnGenerator() {
        myEventGenerator.turnOnGenerator();
    }

    @Override
    public final void turnOnGenerator(double t) {
        myEventGenerator.turnOnGenerator(t);
    }

    @Override
    public void turnOnGenerator(RandomIfc r) {
        myEventGenerator.turnOnGenerator(r);
    }

    @Override
    public void turnOffGenerator() {
        myEventGenerator.turnOffGenerator();
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        myTBARV.setRandomNumberStream(stream);
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myTBARV.getRandomNumberStream();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myTBARV.getResetStartStreamOption();
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myTBARV.setResetStartStreamOption(b);
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myTBARV.getResetNextSubStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myTBARV.setResetNextSubStreamOption(b);
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myTBARV.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myTBARV.getAntitheticOption();
    }

    @Override
    public void advanceToNextSubStream() {
        myTBARV.advanceToNextSubStream();
    }

    @Override
    public void resetStartStream() {
        myTBARV.resetStartStream();
    }

    @Override
    public void resetStartSubStream() {
        myTBARV.resetStartSubStream();
    }

    public InvertibleCumulativeRateFunctionIfc getRateFunction() {
        return myTBARV.getRateFunction();
    }

    public void setRateFunction(InvertibleCumulativeRateFunctionIfc rateFunction) {
        myTBARV.setRateFunction(rateFunction);
    }
}
