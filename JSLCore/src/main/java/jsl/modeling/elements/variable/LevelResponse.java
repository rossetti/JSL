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

package jsl.modeling.elements.variable;

import jsl.observers.ModelElementObserver;
import jsl.simulation.*;
import jsl.utilities.Interval;
import jsl.utilities.statistic.StateFrequency;

import java.util.List;
import static jsl.utilities.reporting.StatisticReporter.D2FORMAT;

/**
 * Collects statistics on whether or not a specific level associated with a variable is
 * maintained.
 */
public class LevelResponse extends SchedulingElement {

    private final Variable myVariable;
    private final double myLevel;
    private final State myAbove;
    private final State myBelow;
    private final StateFrequency myStateFreq;
    private final ModelElementObserver myObserver = new TheObserver();
    private State myCurrentState;
    private final ResponseVariable myDistanceAbove;
    private final ResponseVariable myDistanceBelow;
    private final TimeWeighted myDevAboveLevel;
    private final TimeWeighted myDevBelowLevel;
    private final TimeWeighted myDeviationFromLevel;
    // all these are collected within replicationEnded()
    private ResponseVariable myAvgTimeAbove;
    private ResponseVariable myAvgTimeBelow;
    private ResponseVariable myMaxTimeAbove;
    private ResponseVariable myMaxTimeBelow;
    private ResponseVariable myPAA;
    private ResponseVariable myPAB;
    private ResponseVariable myPBB;
    private ResponseVariable myPBA;
    private ResponseVariable myNAA;
    private ResponseVariable myNAB;
    private ResponseVariable myNBB;
    private ResponseVariable myNBA;
    private final ResponseVariable myPctTimeAbove;
    private final ResponseVariable myPctTimeBelow;
    private final ResponseVariable myTotalTimeAbove;
    private final ResponseVariable myTotalTimeBelow;
    private final ResponseVariable myMaxDistanceAbove;
    private final ResponseVariable myMaxDistanceBelow;
    private final ResponseVariable myTotalAbsDeviationFromLevel;
    private final ResponseVariable myProportionDevFromAboveLevel;
    private final ResponseVariable myProportionDevFromBelowLevel;
    private final ResponseVariable myRelDevFromLevel;
    private final ResponseVariable myRelPosDevFromLevl;
    private final ResponseVariable myRelNegDevFromLevl;
    // end of collected in replicationEnded()
    private final boolean myStatsOption;
    protected double myInitTime;
    private double myObservationIntervalStartTime;
    private double myObservationIntervalDuration;
    private JSLEvent myObservationIntervalStartEvent;
    private JSLEvent myObservationIntervalEndEvent;
    private boolean myHasObservationIntervalFlag = false;
    private boolean myIntervalEndedFlag;
    private boolean myIntervalStartedFlag;

    /**
     * @param variable the variable to observe
     * @param level    the level to associate with the variable
     * @param name     the name of the response
     */
    public LevelResponse(Variable variable, double level, String name) {
        this(variable, level, true, name);
    }

    /**
     * @param variable the variable to observe
     * @param level    the level to associate with the variable
     */
    public LevelResponse(Variable variable, double level) {
        this(variable, level, true, null);
    }

    /**
     * @param variable the variable to observe
     * @param level    the level to associate with the variable
     * @param stats    whether or not detailed state change statistics are collected
     */
    public LevelResponse(Variable variable, boolean stats, double level) {
        this(variable, level, stats, null);
    }

    /**
     * @param variable the variable to observe
     * @param level    the level to associate with the variable
     * @param stats    whether or not detailed state change statistics are collected
     * @param name     the name of the response
     */
    public LevelResponse(Variable variable, double level, boolean stats, String name) {
        super(variable, name);
        myVariable = variable;
        if ((level < myVariable.getLowerLimit()) || (myVariable.getUpperLimit() < level)) {
            Interval i = new Interval(myVariable.getLowerLimit(), myVariable.getUpperLimit());
            throw new IllegalArgumentException("The supplied level " + level + " was outside the range of the variable " + i);
        }
        myLevel = level;
        // collected during the replication
        myStateFreq = new StateFrequency(2);
        List<State> list = myStateFreq.getStates();
        myAbove = list.get(0);
        myBelow = list.get(1);
        myAbove.setName(myVariable.getName() + ":" + getName() + ":+");
        myBelow.setName(myVariable.getName() + ":" + getName() + ":-");
        myAbove.turnOnSojournTimeCollection();
        myBelow.turnOnSojournTimeCollection();
        myVariable.addObserver(myObserver);
        myDistanceAbove = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":DistAboveLevel:" + D2FORMAT.format(level));
        myDistanceBelow = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":DistBelowLevel:" + D2FORMAT.format(level));
        myDevAboveLevel = new TimeWeighted(this,
                myVariable.getName() + ":" + getName() + ":DevAboveLevel:" + D2FORMAT.format(level));
        myDevBelowLevel = new TimeWeighted(this,
                myVariable.getName() + ":" + getName() + ":DevBelowLevel:" + D2FORMAT.format(level));
        myDeviationFromLevel = new TimeWeighted(this,
                myVariable.getName() + ":" + getName() + ":DevFromLevel:" + D2FORMAT.format(level));
        // collected after the replication ends
        myMaxDistanceAbove = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":MaxDistAboveLevel:" + D2FORMAT.format(level));
        myMaxDistanceBelow = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":MaxDistBelowLevel:" + D2FORMAT.format(level));
        myPctTimeAbove = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":PctTimeAbove:" + D2FORMAT.format(level));
        myPctTimeBelow = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":PctTimeBelow:" + D2FORMAT.format(level));
        myTotalTimeAbove = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":TotalTimeAbove:" + D2FORMAT.format(level));
        myTotalTimeBelow = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":TotalTimeBelow:" + D2FORMAT.format(level));
        myTotalAbsDeviationFromLevel = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":TotalAbsDevFromLevel:" + D2FORMAT.format(level));
        myProportionDevFromAboveLevel = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":PctDevAboveLevel:" + D2FORMAT.format(level));
        myProportionDevFromBelowLevel = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":PctDevBelowLevel:" + D2FORMAT.format(level));
        myRelDevFromLevel = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":RelDevFromLevel:" + D2FORMAT.format(level));
        myRelPosDevFromLevl = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":RelPosDevFromLevel:" + D2FORMAT.format(level));
        myRelNegDevFromLevl = new ResponseVariable(this,
                myVariable.getName() + ":" + getName() + ":RelNegDevFromLevel:" + D2FORMAT.format(level));
        myStatsOption = stats;
        if (stats) {
            myAvgTimeAbove = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":AvgTimeAboveLevel:" + D2FORMAT.format(level));
            myAvgTimeBelow = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":AvgTimeBelowLevel:" + D2FORMAT.format(level));
            myMaxTimeAbove = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":MaxTimeAboveLevel:" + D2FORMAT.format(level));
            myMaxTimeBelow = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":MaxTimeBelowLevel:" + D2FORMAT.format(level));
            myPAA = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":P(AboveToAbove)");
            myPAB = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":P(AboveToBelow)");
            myPBB = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":P(BelowToBelow)");
            myPBA = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":P(BelowToAbove)");
            myNAA = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":#(AboveToAbove)");
            myNAB = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":#(AboveToBelow)");
            myNBB = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":#(BelowToBelow)");
            myNBA = new ResponseVariable(this,
                    myVariable.getName() + ":" + getName() + ":#(BelowToAbove)");
        }
    }

    /**
     * Causes an observation interval to be specified. An observation interval is
     * an interval of time over which the response statistics will be collected.  This
     * method will cause events to be scheduled (at the start of the simulation) that
     * represent the interval.
     *
     * @param startTime the time to start the interval, must be greater than or equal to 0.0
     * @param duration  the duration of the observation interval, must be greater than 0.0
     */
    public void scheduleObservationInterval(double startTime, double duration) {
        if (startTime < 0.0) {
            throw new IllegalArgumentException("Start time must be non-negative");
        }
        if (duration <= 0.0) {
            throw new IllegalArgumentException("Duration must be greater than zero");
        }
        myObservationIntervalStartTime = startTime;
        myObservationIntervalDuration = duration;
        myHasObservationIntervalFlag = true;
    }

    /**
     * @return true if scheduleObservationInterval() has been previously called
     */
    public boolean hasObservationInterval() {
        return myHasObservationIntervalFlag;
    }

    /**
     * Causes the cancellation of the observation interval events
     */
    public void cancelObservationInterval() {
        if (myObservationIntervalStartEvent != null) {
            myObservationIntervalStartEvent.setCanceledFlag(true);
        }
        if (myObservationIntervalEndEvent != null) {
            myObservationIntervalEndEvent.setCanceledFlag(true);
        }
    }

    private class StartObservationIntervalAction extends EventAction {

        @Override
        public void action(JSLEvent evt) {
            // clear any previous statistics prior to start of the interval
            warmUp();
            myIntervalStartedFlag = true;
        }
    }

    private class EndObservationIntervalAction extends EventAction {

        @Override
        public void action(JSLEvent evt) {
            myIntervalEndedFlag = true;
        }
    }

    /**
     * @return true if detailed state change statistics are collected
     */
    public final boolean getStatisticsOption() {
        return myStatsOption;
    }

    private class TheObserver extends ModelElementObserver {
        @Override
        protected void initialize(ModelElement m, Object arg) {
           // variableInitialized();
        }

        @Override
        protected void warmUp(ModelElement m, Object arg) {
            //variableWarmedUp();
        }

        @Override
        protected void update(ModelElement m, Object arg) {
            variableUpdated();
        }

        @Override
        protected void replicationEnded(ModelElement m, Object arg) {
            variableReplicationEnded();
        }
    }

    protected void variableUpdated() {
        if (hasObservationInterval()) {
            // has observation interval, only capture during the interval
            if ((myIntervalStartedFlag == true) && (myIntervalEndedFlag == false)) {
                // interval has started but not yet ended
                stateUpdate();
            }
        } else {
            // no interval, always capture
            stateUpdate();
        }
    }

    protected void stateUpdate() {
        State nextState;
        myDeviationFromLevel.setValue(myVariable.getValue() - myLevel);
        if (myVariable.getValue() >= myLevel) {
//            myAboveIndicator.setValue(1.0);
//            myBelowIndicator.setValue(0.0);
            myDevAboveLevel.setValue(myVariable.getValue() - myLevel);
            myDevBelowLevel.setValue(0.0);
            myDistanceAbove.setValue(myVariable.getValue() - myLevel);
            nextState = myAbove;
        } else {
            // below level
//            myAboveIndicator.setValue(0.0);
//            myBelowIndicator.setValue(1.0);
            myDevAboveLevel.setValue(0.0);
            myDevBelowLevel.setValue(myLevel - myVariable.getValue());
            myDistanceBelow.setValue(myLevel - myVariable.getValue());
            nextState = myBelow;
        }
        nextState.enter(getTime());
        // now check if exit is required
        if (myCurrentState != nextState) {
            myCurrentState.exit(getTime());
        }
        myCurrentState = nextState;
        myStateFreq.collect(myCurrentState);
    }

    /**
     * This method should be overridden by subclasses that need actions
     * performed to initialize prior to a replication. It is called once before
     * each replication occurs if the model element wants initialization. It is
     * called after beforeReplication() is called
     */
    @Override
    protected void initialize() {
        variableInitialized();
    }

    /**
     * This method should be overridden by subclasses that need actions
     * performed at the warm up event during each replication. It is called once
     * during each replication if the model element reacts to warm up actions.
     */
    @Override
    protected void warmUp() {
        variableWarmedUp();
    }

    protected void variableInitialized() {
        myIntervalEndedFlag = false;
        myIntervalStartedFlag = false;
        myInitTime = getTime();
        myAbove.initialize();
        myBelow.initialize();
        myStateFreq.reset();
        if (myVariable.getInitialValue() >= myLevel) {
            myCurrentState = myAbove;
        } else {
            myCurrentState = myBelow;
        }
        myCurrentState.enter(getTime());
        if (hasObservationInterval()) {
            myObservationIntervalStartEvent = scheduleEvent(new StartObservationIntervalAction(), myObservationIntervalStartTime);
            myObservationIntervalEndEvent = scheduleEvent(new EndObservationIntervalAction(), myObservationIntervalStartTime + myObservationIntervalDuration);
        }
    }

    protected void variableWarmedUp() {
        myInitTime = getTime();
        myAbove.initialize();
        myBelow.initialize();
        myStateFreq.reset();
        if (myVariable.getValue() >= myLevel) {
            myCurrentState = myAbove;
        } else {
            myCurrentState = myBelow;
        }
        myCurrentState.enter(getTime());
    }

    protected void variableReplicationEnded() {
        myCurrentState.exit(getTime());
        // need to get statistics to the end of the simulation, act like exiting current state
        myMaxDistanceAbove.setValue(myDistanceAbove.getWithinReplicationStatistic().getMax());
        myMaxDistanceBelow.setValue(myDistanceBelow.getWithinReplicationStatistic().getMax());
        double avgDevAbove = myDevAboveLevel.getWithinReplicationStatistic().getAverage();
        double avgDevBelow = myDevBelowLevel.getWithinReplicationStatistic().getAverage();
        double avgTotalDev = avgDevAbove + avgDevBelow;
        myTotalAbsDeviationFromLevel.setValue(avgTotalDev);
        if (avgTotalDev > 0.0){
            myProportionDevFromAboveLevel.setValue(avgDevAbove/avgTotalDev);
            myProportionDevFromBelowLevel.setValue(avgDevBelow/avgTotalDev);
        }
        if (myLevel != 0.0){
            myRelDevFromLevel.setValue(myDeviationFromLevel.getWithinReplicationStatistic().getAverage()/myLevel);
            myRelPosDevFromLevl.setValue(avgDevAbove/myLevel);
            myRelNegDevFromLevl.setValue(avgDevBelow/myLevel);
        }
        if (myAbove.getSojournTimeStatistic().isPresent()) {
            double totalTimeInState = myAbove.getTotalTimeInState();
            myPctTimeAbove.setValue(totalTimeInState / (getTime() - myInitTime));
            myTotalTimeAbove.setValue(totalTimeInState);
        }
        if (myBelow.getSojournTimeStatistic().isPresent()) {
            double totalTimeInState = myBelow.getTotalTimeInState();
            myPctTimeBelow.setValue(totalTimeInState / (getTime() - myInitTime));
            myTotalTimeBelow.setValue(totalTimeInState);
        }
        // collect state statistics
        if (getStatisticsOption()) {
            if (myAbove.getSojournTimeStatistic().isPresent()) {
                myAvgTimeAbove.setValue(myAbove.getSojournTimeStatistic().get().getAverage());
                myMaxTimeAbove.setValue(myAbove.getSojournTimeStatistic().get().getMax());
            }
            if (myBelow.getSojournTimeStatistic().isPresent()) {
                myAvgTimeBelow.setValue(myBelow.getSojournTimeStatistic().get().getAverage());
                myMaxTimeBelow.setValue(myBelow.getSojournTimeStatistic().get().getMax());
            }
            double[][] p = myStateFreq.getTransitionProportions();
            if (p != null) {
                myPAA.setValue(p[0][0]);
                myPAB.setValue(p[0][1]);
                myPBB.setValue(p[1][1]);
                myPBA.setValue(p[1][0]);
            }
            int[][] n = myStateFreq.getTransitionCounts();
            if (n != null) {
                myNAA.setValue(n[0][0]);
                myNAB.setValue(n[0][1]);
                myNBB.setValue(n[1][1]);
                myNBA.setValue(n[1][0]);
            }
        }
    }


}
