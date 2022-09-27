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
package jsl.modeling.resource;

import jsl.simulation.ModelElement;
import jsl.modeling.elements.RandomElementIfc;
import jsl.modeling.elements.Schedule;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.observers.ModelElementObserver;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.*;

/**
 * A ResourcePool represents a list of ResourceUnits from which
 * a single unit can be selected.
 * <p>
 * ResourceUnits are selected according to a ResourceSelectionRule.
 * The assumption is that any of the resource
 * units within the pool may be used to fill the request.
 * <p>
 * If no selection rule is supplied the pool selects the first idle resource
 * by default.
 * <p>
 * Statistics on the number of units idle and busy are automatically collected.
 * Statistics on number inactive and number failed in
 * the pool can be collected by specifying the statistics option, which
 * is false by default. Inactive and failure statistics will be collected
 * regardless of whether or not the units in the pool can be failed or inactive.
 * <p>
 * Sub-classes may override the methods unitbecameIdle(), unitbecameBusy(),
 * unitFailed(), unitBecameInactive() in order to react to state
 * changes on individual resource units.
 *
 * The utilization of the pool is defined as the average number busy divided by
 * the average number active. The average number active is the number of units
 * minus the average number of inactive units and average number of failed units.
 * Thus, we assume that resources cannot be busy if they are failed or inactive.
 *
 * @author rossetti
 */
public class ResourcePool extends ModelElement implements RandomElementIfc {

    protected final List<ResourceUnit> myResources;
    private ResourceUnitSelectionRuleIfc mySelectionRule;
    protected final ResourceUnitObserver myRUObserver;
    protected final TimeWeighted myNumBusy;
    protected final TimeWeighted myNumIdle;
    protected TimeWeighted myNumFailed;
    protected TimeWeighted myNumInactive;
    private final boolean myPoolStatOption;
    private RNStreamIfc myRNG;
    protected boolean myResetStartStreamOption;
    protected boolean myResetNextSubStreamOption;
    protected final ResponseVariable myUtilization;

    /**
     * Statistics option is false by default
     *
     * @param parent the parent model element
     * @param units  a list of ResourceUnits. Must not contain nulls and
     *               must not contain duplicates
     */
    public ResourcePool(ModelElement parent, List<ResourceUnit> units) {
        this(parent, units, false, null);
    }

    /**
     * Statistics option is false by default
     *
     * @param parent the parent model element
     * @param units  a list of ResourceUnits. Must not contain nulls and
     *               must not contain duplicates
     * @param name   the name of the pool
     */
    public ResourcePool(ModelElement parent, List<ResourceUnit> units, String name) {
        this(parent, units, false, name);
    }

    /**
     * @param parent     the parent model element
     * @param units      a list of ResourceUnits. Must not contain nulls and
     *                   must not contain duplicates
     * @param statOption true means collect statistics on inactive and failure states
     */
    public ResourcePool(ModelElement parent, List<ResourceUnit> units,
                        boolean statOption) {
        this(parent, units, statOption, null);
    }

    /**
     * @param parent     the parent model element
     * @param units      a list of ResourceUnits. Must not contain nulls and
     *                   must not contain duplicates
     * @param statOption true means collect statistics on inactive and failure states
     * @param name       the name of the pool
     */
    public ResourcePool(ModelElement parent, List<ResourceUnit> units,
                        boolean statOption, String name) {
        super(parent, name);
        myRUObserver = new ResourceUnitObserver();
        myResources = new LinkedList<>();
        addAll(units);
        myNumBusy = new TimeWeighted(this, getName() + ":NumBusy");
        myNumIdle = new TimeWeighted(this, getName() + ":NumIdle");
        myPoolStatOption = statOption;
        if (myPoolStatOption == true) {
            myNumFailed = new TimeWeighted(this, getName() + ":NumFailed");
            myNumInactive = new TimeWeighted(this, getName() + ":NumInactive");
        }
        myUtilization = new ResponseVariable(this, getName() +":Util");
    }


    /**
     * @return number of units in the pool
     */
    public final int getNumUnits() {
        return myResources.size();
    }

    /**
     * @return true if at least one unit is idle
     */
    public final boolean hasIdleUnits() {
        return getNumIdle() > 0;
    }

    /**
     * @return true if at least one unit is busy
     */
    public final boolean hasBusyUnits() {
        return getNumBusy() > 0;
    }

    /**
     * @return true if at least one unit is failed
     */
    public final boolean hasFailedUnits() {
        return getNumFailed() > 0;
    }

    /**
     * @return true if all units are idle
     */
    public final boolean hasAllUnitsIdle() {
        return getNumUnits() == getNumIdle();
    }

    /**
     * @return true if all units are busy
     */
    public final boolean hasAllUnitsBusy() {
        return getNumUnits() == getNumBusy();
    }

    /**
     * @return true if all units are failed
     */
    public final boolean hasAllUnitsFailed() {
        return getNumUnits() == getNumFailed();
    }

    /**
     * @return true if all units are inactive
     */
    public final boolean hasAllUnitsInactive() {
        return getNumUnits() == getNumInactive();
    }

    /**
     * @return true if at least one unit is inactive
     */
    public final boolean hasInactiveUnits() {
        return getNumInactive() > 0;
    }

    /**
     * @return the number of currently idle units
     */
    public final int getNumIdle() {
        int n = 0;
        for (ResourceUnit ru : myResources) {
            if (ru.isIdle()) {
                n++;
            }
        }
        return n;
    }

    /**
     * @return the number of currently busy units
     */
    public final int getNumBusy() {
        int n = 0;
        for (ResourceUnit ru : myResources) {
            if (ru.isBusy()) {
                n++;
            }
        }
        return n;
    }

    /**
     * @return the number of currently failed units
     */
    public final int getNumFailed() {
        int n = 0;
        for (ResourceUnit ru : myResources) {
            if (ru.isFailed()) {
                n++;
            }
        }
        return n;
    }

    /**
     * @return the number of currently inactive units
     */
    public final int getNumInactive() {
        int n = 0;
        for (ResourceUnit ru : myResources) {
            if (ru.isInactive()) {
                n++;
            }
        }
        return n;
    }

    /**
     * @return true if ALL units in the pool have true for their failure delay
     * option. That is, all units allow failures to be delayed.
     */
    public final boolean getFailureDelayOption() {
        for (ResourceUnit ru : myResources) {
            if (ru.getFailureDelayOption() == false) {
                return false;
            }
        }
        return true;
    }

    /**  Returns false if ALL units do not have failure processes
     *
     * @return returns true if at least one of the units in the pool has a failure process
     */
    public final boolean hasFailureProcesses(){
        for (ResourceUnit ru : myResources) {
            if (ru.hasFailureProcesses()) {
                return true;
            }
        }
        return false;
    }

    /**  Returns false if ALL units do not have any schedules attached
     *
     * @return returns true if at least one of the units in the pool has a schedule attached
     */
    public final boolean hasSchedules(){
        for (ResourceUnit ru : myResources) {
            if (ru.hasSchedules()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if ALL units in the pool have true for their inactive period
     * option. That is, all units allows their inactive periods to be delayed
     */
    public final boolean getInactivePeriodDelayOption() {
        for (ResourceUnit ru : myResources) {
            if (ru.getInactivePeriodDelayOption() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the preemption rule of the request is compatible
     * with the failure delay option. If the request doesn't allow
     * preemption and failures cannot be delayed, this means that the
     * request will be rejected.
     *
     * @param request the request to check
     * @return true if compatible
     */
    public boolean isPreemptionRuleCompatible(Request request) {
        if (request.getPreemptionRule() == Request.PreemptionRule.NONE) {
            if (getFailureDelayOption() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if pooled statistics will be collected
     */
    public final boolean isPooledStatsOptionOn() {
        return myPoolStatOption;
    }

    /**
     * @param unit the unit to test
     * @return true if in the pool
     */
    public final boolean contains(ResourceUnit unit) {
        return myResources.contains(unit);
    }

    /**
     * @return an unmodifiable list of the resource units
     */
    public final List<ResourceUnit> getUnits() {
        return Collections.unmodifiableList(myResources);
    }

    /**
     * Tells all ResourceUnits in the pool that are not already using a Schedule
     * to use the supplied schedule
     *
     * @param schedule the schedule to use
     */
    public final void useSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("The supplied Schedule was null");
        }
        for (ResourceUnit unit : getUnits()) {
            if (!unit.isUsingSchedule(schedule)) {
                unit.useSchedule(schedule);
            }
        }
    }

    /**
     * @param units the list to add. Must not contain any nulls and must not
     *              have any units that are already in the pool.
     */
    protected final void addAll(List<ResourceUnit> units) {
        if (units == null) {
            throw new IllegalArgumentException("The resource unit list was null!");
        }
        for (ResourceUnit ru : units) {
            add(ru);
        }
    }

    /**
     * @param unit the unit to add. Must not be null. Must not already have been
     *             added
     * @return true if added
     */
    protected final boolean add(ResourceUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("The resource unit was null!");
        }
        if (myResources.contains(unit)) {
            throw new IllegalArgumentException("The resource unit was already added!");
        }
        unit.addObserver(myRUObserver);
        return myResources.add(unit);
    }

    /**
     * @return an Optional with the rule
     */
    public Optional<ResourceUnitSelectionRuleIfc> getSelectionRule() {
        return Optional.ofNullable(mySelectionRule);
    }

    /**
     * @param rule the supplied rule, may be null
     */
    public void setSelectionRule(ResourceUnitSelectionRuleIfc rule) {
        mySelectionRule = rule;
    }

    /**
     * Selects a resource unit according to the selection rule. If no
     * selection rule is present, selects the first idle resource
     * in the list of resources.
     *
     * @return the selected resource unit or null
     */
    public ResourceUnit selectResourceUnit() {
        if (mySelectionRule != null) {
            return mySelectionRule.selectAvailableResource(myResources);
        }
        for (ResourceUnit ru : myResources) {
            if (ru.isIdle()) {
                return ru;
            }
        }
        return null;
    }

    /**
     * Randomly selects an idle resource unit if available
     *
     * @return the selected resource unit or null
     */
    public ResourceUnit randomlySelectResourceUnit() {
        List<ResourceUnit> list = findIdleResourceUnits();
        return JSLRandom.randomlySelect(list, getRandomNumberStream());
    }

//    /**
//     * @return the underlying source of randomness for this model element
//     */
//    public RNStreamIfc getRandomness() {
//        if (myRNG == null) {
//            myRNG = JSLRandom.nextRNStream();
//        }
//        return myRNG;
//    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        if (myRNG == null) {
            myRNG = JSLRandom.nextRNStream();
        }
        return myRNG;
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        Objects.requireNonNull(stream, "The supplied stream was null");
        myRNG = stream;
    }

    /**
     * @return returns a list of idle resource units. It may be empty
     */
    public List<ResourceUnit> findIdleResourceUnits() {
        List<ResourceUnit> list = new ArrayList<>();
        for (ResourceUnit ru : myResources) {
            if (ru.isIdle()) {
                list.add(ru);
            }
        }
        return list;
    }

    /**
     * @return the first idle resource found or null
     */
    public ResourceUnit findFirstIdle() {
        for (ResourceUnit ru : myResources) {
            if (ru.isIdle()) {
                return ru;
            }
        }
        return null;
    }

    @Override
    public void resetStartStream() {
        if (myRNG != null) {
            myRNG.resetStartStream();
        }
    }

    @Override
    public void resetStartSubStream() {
        if (myRNG != null) {
            myRNG.resetStartSubStream();
        }
    }

    @Override
    public void advanceToNextSubStream() {
        if (myRNG != null) {
            myRNG.advanceToNextSubStream();
        }
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        if (myRNG != null) {
            myRNG.setAntitheticOption(flag);
        }
    }

    @Override
    public boolean getAntitheticOption() {
        if (myRNG != null) {
            return myRNG.getAntitheticOption();
        }
        return false;
    }

    /**
     * Gets the current Reset Start Stream Option
     *
     * @return
     */
    @Override
    public final boolean getResetStartStreamOption() {
        return myResetStartStreamOption;
    }

    /**
     * Sets the reset start stream option, true
     * means that it will be reset to the starting stream
     *
     * @param b
     */
    @Override
    public final void setResetStartStreamOption(boolean b) {
        myResetStartStreamOption = b;
    }

    /**
     * Gets the current reset next substream option
     * true means, that it is set to jump to the next substream after
     * each replication
     *
     * @return
     */
    @Override
    public final boolean getResetNextSubStreamOption() {
        return myResetNextSubStreamOption;
    }

    /**
     * Sets the current reset next substream option
     * true means, that it is set to jump to the next substream after
     * each replication
     *
     * @param b
     */
    @Override
    public final void setResetNextSubStreamOption(boolean b) {
        myResetNextSubStreamOption = b;
    }

    /**
     * before any replications reset the underlying random number generator to
     * the
     * starting stream
     * <p>
     */
    @Override
    protected void beforeExperiment() {
        super.beforeExperiment();
        if (getResetStartStreamOption()) {
            resetStartStream();
        }

    }

    /**
     * This method should be overridden by subclasses that need actions
     * performed when the replication ends and prior to the calling of
     * afterReplication() . It is called when each replication ends and can be
     * used to collect data from the the model element, etc.
     */
    @Override
    protected void replicationEnded() {
        super.replicationEnded();
        double avgNumBusy = myNumBusy.getWithinReplicationStatistic().getAverage();

        double avgNumInactive = 0.0;
        if (myNumInactive != null){
            avgNumInactive = myNumInactive.getWithinReplicationStatistic().getAverage();
        }
        double avgNumFailed = 0.0;
        if (myNumInactive != null){
            avgNumFailed = myNumFailed.getWithinReplicationStatistic().getAverage();
        }

        double util = avgNumBusy/(getNumUnits() - avgNumInactive - avgNumFailed);
        myUtilization.setValue(util);
    }

    /**
     * after each replication reset the underlying random number generator to
     * the next
     * substream
     */
    @Override
    protected void afterReplication() {
        super.afterReplication();
        if (getResetNextSubStreamOption()) {
            advanceToNextSubStream();
        }

    }

    protected class ResourceUnitObserver extends ModelElementObserver {

        @Override
        protected void update(ModelElement m, Object arg) {
            super.update(m, arg);
            ResourceUnit ru = (ResourceUnit) m;
            collectStateStatistics(ru);
            resourceUnitChanged(ru);
        }

    }

    /**
     * Partials out unit changes to unitBecameIdle(), unitBecameBusy(),
     * unitFailed(), unitBecameInactive()
     *
     * @param ru the unit that changed
     */
    protected void resourceUnitChanged(ResourceUnit ru) {
        if (ru.isIdle()) {
            unitBecameIdle(ru);
        } else if (ru.isBusy()) {
            unitBecameBusy(ru);
        } else if (ru.isFailed()) {
            unitFailed(ru);
        } else if (ru.isInactive()) {
            unitBecameInactive(ru);
        } else {
            // nothing
        }
    }

    /**
     * Collects pool statistics based on change of state of contained
     * resource units
     *
     * @param ru the resource unit that changed state
     */
    protected void collectStateStatistics(ResourceUnit ru) {
        if (myNumBusy != null) {
            myNumBusy.setValue(getNumBusy());
        }
        if (myNumIdle != null) {
            myNumIdle.setValue(getNumIdle());
        }
        if (myNumFailed != null) {
            myNumFailed.setValue(getNumFailed());
        }
        if (myNumInactive != null) {
            myNumInactive.setValue(getNumInactive());
        }
    }

    /**
     * Called when one of the units becomes idle
     *
     * @param ru the unit that became idle
     */
    protected void unitBecameIdle(ResourceUnit ru) {

    }

    /**
     * Called when one of the units becomes busy
     *
     * @param ru the unit that became busy
     */
    protected void unitBecameBusy(ResourceUnit ru) {

    }

    /**
     * Called when one of the units becomes failed
     *
     * @param ru the unit that became failed
     */
    protected void unitFailed(ResourceUnit ru) {

    }

    /**
     * Called when one of the units becomes inactive
     *
     * @param ru the unit that became inactive
     */
    protected void unitBecameInactive(ResourceUnit ru) {

    }

    public static Comparator<ResourcePool> getDescendingByNumIdleComparator() {
        return new DescendingByNumIdleComparator();
    }

    public static class DescendingByNumIdleComparator implements Comparator<ResourcePool> {

        @Override
        public int compare(ResourcePool o1, ResourcePool o2) {
            return o2.getNumIdle() - o1.getNumIdle();
        }

    }
}
