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

import jsl.simulation.JSLEvent;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.utilities.GetValueIfc;
import jsl.utilities.random.RandomIfc;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A FailureProcess causes FailureNotices to be sent to a ResourceUnit. If
 * the user supplies an initial starting time random variable then, it will
 * be used to start the process at that time for each replication of the simulation. If
 * no initial starting time random variable is supplied, then the process will
 * not be started automatically and the user should use the start() methods to
 * start the process at the appropriate time.  Starting the process causes
 * the first failure to occur at the specified start time and for it to last for
 * the provided duration.  When the process is started the first
 * failure event is scheduled. If the process is not started then no first event is
 * scheduled.  Implementors of sub-classes are responsible for suspending, resuming, and stopping the
 * process by implementing the suspendProcess(), resumeProcess(), and stopProcess() methods.
 * <p>
 * Once the failure process has been started it
 * cannot be started again (until the next replication). Once the failure process has been stopped, it cannot
 * be started again (until the next replication). A FailureProcess is associated with one ResourceUnit.
 *
 * @author rossetti
 */
abstract public class FailureProcess extends SchedulingElement {

    private final CreatedState myCreatedState = new CreatedState();
    private final RunningState myRunningState = new RunningState();
    private final SuspendedState mySuspendedState = new SuspendedState();
    private final StoppedState myStoppedState = new StoppedState();

    private final RandomVariable myFailureDurationRV;
    private RandomVariable myInitialStartTimeRV;
    private boolean myAutoStartOption;
    private int myPriority;
    private FailureProcessState myProcessState;
    private final ResourceUnit myResourceUnit;
    private JSLEvent myStartEvent;
    private final Set<FailureProcessListenerIfc> myFailureProcessListeners;


    /**
     * The process will not be started automatically.
     *
     * @param resourceUnit the resourceUnit
     * @param duration     governs the duration of the FailureNotices
     */
    public FailureProcess(ResourceUnit resourceUnit, RandomIfc duration) {
        this(resourceUnit, duration, null, null);
    }

    /**
     * The process will not be started automatically.
     *
     * @param resourceUnit the resourceUnit
     * @param duration     governs the duration of the FailureNotices
     */
    public FailureProcess(ResourceUnit resourceUnit, RandomIfc duration, String name) {
        this(resourceUnit, duration, null, name);
    }

    /**
     * @param resourceUnit       the resourceUnit, may not be null
     * @param duration           governs the duration of the FailureNotices, may not be null
     * @param initialStartTimeRV used to specify the time of the start of the process, if the process is
     *                           to be started automatically at the beginning of each replication. May be null.
     */
    public FailureProcess(ResourceUnit resourceUnit, RandomIfc duration, RandomIfc initialStartTimeRV) {
        this(resourceUnit, duration, initialStartTimeRV, null);
    }

    /**
     * @param resourceUnit       the resourceUnit, may not be null
     * @param duration           governs the duration of the FailureNotices, may not be null
     * @param initialStartTimeRV used to specify the time of the start of the process, if the process is
     *                           to be started automatically at the beginning of each replication. May be null.
     * @param name               the name of the FailureProcess
     */
    public FailureProcess(ResourceUnit resourceUnit, RandomIfc duration, RandomIfc initialStartTimeRV, String name) {
        super(resourceUnit, name);
        Objects.requireNonNull(duration, "The failure duration must not be null");
        myFailureDurationRV = new RandomVariable(this, duration, getName() + ":Duration");
        if (initialStartTimeRV != null) {
            turnOnAutoStartProcessOption(initialStartTimeRV);
        } else {
            turnOffAutoStartProcessOption();
        }
        myPriority = JSLEvent.DEFAULT_PRIORITY;
        myProcessState = myCreatedState;
        myResourceUnit = resourceUnit;
        myFailureProcessListeners = new LinkedHashSet<>();
        myResourceUnit.addFailureProcess(this);
    }

    /**
     * Adds a listener to react to FailureProcess state changes
     *
     * @param listener the listener to add
     */
    public final void addFailureProcessListener(FailureProcessListenerIfc listener) {
        if (listener == null) {
            return;
        }
        myFailureProcessListeners.add(listener);
    }

    /**
     * Removes the listener from the FailureProcess
     *
     * @param listener the listener to remove
     */
    public final void removeFailureProcessListener(FailureProcessListenerIfc listener) {
        if (listener == null) {
            return;
        }
        myFailureProcessListeners.remove(listener);
    }

    /**
     * @return the priority of the generated FailureNotices
     */
    public final int getPriority() {
        return myPriority;
    }

    /**
     * @param priority the priority of the generated FailureNotices
     */
    public final void setPriority(int priority) {
        myPriority = priority;
    }

    /**
     * The default option is true
     *
     * @return returns true if the FailureNotices made by this FailureElement
     * can be delayed if the ResourceUnit it is sent to is busy
     */
    public final boolean getFailureDelayOption() {
        return myResourceUnit.getFailureDelayOption();
    }

    /**
     * Sets the failure duration distribution
     *
     * @param d the distribution
     */
    public final void setFailureDurationTimeInitialRandomSource(RandomIfc d) {
        if (d == null) {
            throw new IllegalArgumentException("Failure duration was null!");
        }
        myFailureDurationRV.setInitialRandomSource(d);
    }

    /**
     * The default is false
     *
     * @return true if failure process will start automatically upon
     * initialization
     */
    public final boolean getAutoStartProcessOption() {
        return myAutoStartOption;
    }

    /**
     *  Causes the auto starting of the process to be turned off. This indicates that the process
     *  should not use the initial start time if it was supplied.  Thus, a start time may be
     *  supplied but not used.
     */
    public final void turnOffAutoStartProcessOption(){
        myAutoStartOption = false;
    }

    /**
     * Setting an initial start time indicates to the failure process that it should
     * automatically start using the supplied time at the beginning of each replication.
     *
     * @param startTime the time that the process should start at the beginning of each simulation, must not be
     *                  null
     */
    public final void turnOnAutoStartProcessOption(RandomIfc startTime){
        Objects.requireNonNull(startTime, "The supplied start time was null");
        if (myInitialStartTimeRV == null) {
            myInitialStartTimeRV = new RandomVariable(this, startTime, getName() + ":InitialStartTime");
        } else {
            myInitialStartTimeRV.setInitialRandomSource(startTime);
        }
        myAutoStartOption = true;
    }

    /**
     * Since supplying an initial starting time is optional, it may be null.
     *
     * @return the random variable that computes the value of the initial start time (if present)
     */
    protected final Optional<RandomVariable> getInitialStartTimeRV() {
        return Optional.ofNullable(myInitialStartTimeRV);
    }

    /**
     *
     * @return the random variable representing the duration
     */
    protected final RandomVariable getFailureDurationRV(){
        return myFailureDurationRV;
    }

    /** If the initial start time random variable is set then this method should return
     *  the starting time of the event.  If the initial start time is not set, this method returns Double.NaN.
     *  If the initial start time random variable is set, then this method is used when initializing
     *  the failure process to set the start time of the process to the value returned from this method.
     *
     * @return returns the value of the initial start time or Double.NaN if the initial start time
     * was not specified.
     */
    public final double getInitialStartTimeValue() {
        if (myInitialStartTimeRV == null) {
            return Double.NaN;
        }
        return myInitialStartTimeRV.getValue();
    }

    /**
     * Used for getting the value of the duration within createFailureNotice(). All notices
     * will use the values returned from this method
     *
     * @return the value of the duration, which may be random
     */
    public final double getDurationValue() {
        return myFailureDurationRV.getValue();
    }

    @Override
    protected void initialize() {
        super.initialize();
        myProcessState = myCreatedState;
        if (getAutoStartProcessOption()) {
            double timeValue = getInitialStartTimeValue();
            if (!Double.isNaN(timeValue)){
                start(timeValue);
            }
        }
    }

    /**
     * Should be used in implementations of sendFailureNotice()
     *
     * @return creates a FailureNotice
     */
    protected final FailureNotice createFailureNotice() {
        FailureNotice fn = new FailureNotice(this, getDurationValue(), getFailureDelayOption());
        fn.setPriority(getPriority());
        return fn;
    }

    /**
     * If the process has not been started yet then the start event may be null
     *
     * @return the event that is scheduled to start the process
     */
    protected final Optional<JSLEvent> getStartEvent() {
        return Optional.ofNullable(myStartEvent);
    }

    /**
     * Stops the failure process.  Once the failure process is stopped, the
     * the process cannot be restarted. The process can be stopped from
     * the running or suspended states.
     */
    public final void stop() {
        myProcessState.stop();
    }

    /**
     * This method starts the process by scheduling the first event to occur at getTime() + time.
     * <p>
     * If the failure process has already been started then an IllegalStateException is thrown. The process must be
     * started to be able to send failure notices. The failure process can
     * only be started once per replication. The process can only be started
     * from the created state.
     */
    public final void start(double time) {
        myProcessState.start(time);
    }

    /**
     * Start the process at the current time. In other words in getTime() + 0.0 into the future.
     */
    public final void start() {
        start(0.0);
    }

    /**
     * Causes the failure process to start at getTime() + value.getValue().
     *
     * @param value the GetValueIfc object that should be used get the value of the starting time
     */
    public final void start(GetValueIfc value) {
        Objects.requireNonNull(value, "The supplied GetValueIfc was null");
        start(value.getValue());
    }

    /**
     * Tells the process to suspend the generation of FailureNotices.
     * Once suspended, resume() can be used to continue the generation
     * of FailureNotices. Suspending the generation of FailureNotices should
     * cause no new FailureNotices to be sent. The process can only
     * be suspended from the running state.
     */
    public final void suspend() {
        myProcessState.suspend();
    }

    /**
     * Tells the process to resume the generation of FailureNotices.
     * Once resumed, suspend() can be used to pause the generation of new
     * FailureNotices. Resuming the generation of FailureNotices should
     * allow the process to continue sending notices if resumption of the
     * process is permitted. The process can only be resumed from the suspended state.
     */
    public final void resume() {
        myProcessState.resume();
    }

    /**
     * @return the resource unit attached to this failure process
     */
    protected final ResourceUnit getResourceUnit() {
        return myResourceUnit;
    }

    /**
     * When a FailureNotice is made active,
     * this method is called. Called from FailureNotice.CreatedState or
     * FailureNotice.DelayedState when activate() is called.  A FailureNotice
     * is activated from ResourceUnit when scheduling the end of failure for the generated
     * FailureNotice using ResourceUnit's scheduleEndOfFailure(FailureNotice failureNotice).
     * If the FailureNotice is not delayed then it is activated immediately upon
     * the ResourceUnit receiving the failure notice.
     * If the FailureNotice is delayed, then after the delay it is activated
     * This can be used to react to the notices
     * becoming active.  A FailureNotice becoming active means that
     * the failure has started (taking down the resource unit).
     *
     * @param fn the failure notice
     */
    abstract protected void failureNoticeActivated(FailureNotice fn);

    /**
     * When a FailureNotice is delayed,
     * this method is called. This can be used to react to the notice
     * becoming delayed.
     *
     * @param fn the failure notice
     */
    abstract protected void failureNoticeDelayed(FailureNotice fn);

    /**
     * When a FailureNotice is ignored,
     * this method is called. This can be used to react to the notice
     * becoming ignored.
     *
     * @param fn the failure notice
     */
    abstract protected void failureNoticeIgnored(FailureNotice fn);

    /**
     * When a FailureNotice is completed,
     * this method is called. This can be used to react to the notice
     * becoming completed.
     *
     * @param fn the failure notice
     */
    abstract protected void failureNoticeCompleted(FailureNotice fn);

    /**
     * Performs work associated with suspending the process
     */
    abstract protected void suspendProcess();

    /**
     * Performs work associated with stopping the process
     */
    abstract protected void stopProcess();

    /**
     * Performs work to resume the process.
     */
    abstract protected void resumeProcess();

    /**
     * Called by FailureNotice when its state changes
     *
     * @param fn
     */
    final void failureNoticeStateChange(FailureNotice fn) {
        if (fn.isActive()) {
            failureNoticeActivated(fn);
            notifyFailureProcessListeners(fn);
        } else if (fn.isDelayed()) {
            failureNoticeDelayed(fn);
            notifyFailureProcessListeners(fn);
        } else if (fn.isIgnored()) {
            failureNoticeIgnored(fn);
            notifyFailureProcessListeners(fn);
        } else if (fn.isCompleted()) {
            failureNoticeCompleted(fn);
            notifyFailureProcessListeners(fn);
        } else {
            throw new IllegalStateException("Invalid FailureNotice state");
        }
    }

    /**
     * Used internally to notify failure process listeners of state changes:
     * start, stop, failure, suspend, resume
     */
    protected final void notifyFailureProcessListeners(FailureNotice fn) {
        for (FailureProcessListenerIfc fpl : myFailureProcessListeners) {
            fpl.changed(this, fn);
        }
    }

    /**
     * Use this method to cause FailureNotices to be sent. This
     * method properly checks the state of the process before sending.
     */
    protected final void fail() {
        myProcessState.fail();
    }

    /**
     * Implement this method signal ResourceUnits via FailureNotices
     * This method is called by fail() which properly
     * checks the state of the process before signalling
     */
    protected void signalFailure() {
        myResourceUnit.receiveFailureNotice(createFailureNotice());
    }

    /**
     * @return true if the failure process is in the running state
     */
    public final boolean isRunning() {
        return myProcessState == myRunningState;
    }

    /**
     * @return true if the failure process is in the created state
     */
    public final boolean isCreated() {
        return myProcessState == myCreatedState;
    }

    /**
     * @return true if the failure process is in the suspended state
     */
    public final boolean isSuspended() {
        return myProcessState == mySuspendedState;
    }

    /**
     * @return true if the failure process is in the running state
     */
    public final boolean isStopped() {
        return myProcessState == myStoppedState;
    }

    /**
     * Performs the work to start the failure process. Schedules the first event
     *
     * @param time the time that the process should be scheduled to start, must be not be negative
     */
    protected final void scheduleStartOfProcess(double time) {
        myStartEvent = schedule(this::startEvent).havingPriority(getPriority()).in(time).units();
    }

    private void startEvent(JSLEvent event) {
        fail();
    }

    /**
     * If the start event has been scheduled, then cancel it
     */
    protected final void cancelStartEvent() {
        Optional<JSLEvent> startEvent = getStartEvent();
        if (startEvent.isPresent()) {
            startEvent.get().setCanceledFlag(true);
        }
    }

    /**
     * Uncancels the start event
     */
    protected final void unCancelStartEvent() {
        Optional<JSLEvent> startEvent = getStartEvent();
        if (startEvent.isPresent()) {
            JSLEvent event = startEvent.get();
            if (event.isScheduled()) {
                event.setCanceledFlag(false);
            }
        }
    }

    private class FailureProcessState {
        protected final String myName;

        private FailureProcessState(String name) {
            myName = name;
        }

        protected void fail() {
            throw new IllegalStateException("Tried to fail from an illegal state: " + myName);
        }

        protected void start(double time) {
            throw new IllegalStateException("Tried to start from an illegal state: " + myName);
        }

        protected void suspend() {
            throw new IllegalStateException("Tried to suspend from an illegal state: " + myName);
        }

        protected void resume() {
            throw new IllegalStateException("Tried to resume from an illegal state: " + myName);
        }

        protected void stop() {
            throw new IllegalStateException("Tried to stop from an illegal state: " + myName);
        }

    }

    private final class CreatedState extends FailureProcessState {

        private CreatedState() {
            super("Created");
        }

        @Override
        protected void start(double time) {
            if (time < 0.0) {
                throw new IllegalArgumentException("The starting time was negative (must be >=0");
            }
            myProcessState = myRunningState;
            scheduleStartOfProcess(time);
        }

    }

    private final class RunningState extends FailureProcessState {

        private RunningState() {
            super("Running");
        }

        @Override
        protected void fail() {
            signalFailure();
        }

        @Override
        protected void suspend() {
            myProcessState = mySuspendedState;
            suspendProcess();
        }

        @Override
        protected void stop() {
            myProcessState = myStoppedState;
            stopProcess();
        }

    }

    private final class SuspendedState extends FailureProcessState {

        private SuspendedState() {
            super("Suspended");
        }

        @Override
        protected void resume() {
            myProcessState = myRunningState;
            resumeProcess();
        }

        @Override
        protected void stop() {
            myProcessState = myStoppedState;
            stopProcess();
        }

        @Override
        protected void suspend() {

        }
    }

    private final class StoppedState extends FailureProcessState {

        private StoppedState() {
            super("Stopped");
        }
    }


    /**
     * This method is called by ResourceUnit when a state change has
     * occurred. It calls the resourceUnitXtoY() methods to allow
     * sub-classes to specialize the behavior associated with a
     * state change on a ResourceUnit.
     */
    protected final void resourceUnitStateChange() {
        if (myResourceUnit.isPreviousStateIdle()) {
            if (myResourceUnit.isBusy()) {
                //idle to busy
                resourceUnitIdleToBusy();
            } else if (myResourceUnit.isFailed()) {
                // idle to failed
                resourceUnitIdleToFailed();
            } else if (myResourceUnit.isInactive()) {
                // idle to inactive
                resourceUnitIdleToInactive();
            } else if (myResourceUnit.isIdle()) {
                // idle to idle, not possible
                resourceUnitIdleToIdle();
            }
        } else if (myResourceUnit.isPreviousStateInactive()) {
            if (myResourceUnit.isBusy()) {
                //inactive to busy
                resourceUnitInactiveToBusy();
            } else if (myResourceUnit.isFailed()) {
                // inactive to failed
                resourceUnitInactiveToFailed();
            } else if (myResourceUnit.isInactive()) {
                // inactive to inactive
                resourceUnitInactiveToInactive();
            } else if (myResourceUnit.isIdle()) {
                // inactive to idle
                resourceUnitInactiveToIdle();
            }
        } else if (myResourceUnit.isPreviousStateBusy()) {
            if (myResourceUnit.isBusy()) {
                //busy to busy
                resourceUnitBusyToBusy();
            } else if (myResourceUnit.isFailed()) {
                // busy to failed
                resourceUnitBusyToFailed();
            } else if (myResourceUnit.isInactive()) {
                // busy to inactive
                resourceUnitBusyToInactive();
            } else if (myResourceUnit.isIdle()) {
                // busy to idle
                resourceUnitBusyToIdle();
            }
        } else if (myResourceUnit.isPreviousStateFailed()) {
            if (myResourceUnit.isBusy()) {
                //failed to busy
                resourceUnitFailedToBusy();
            } else if (myResourceUnit.isFailed()) {
                // failed to failed
                resourceUnitFailedToFailed();
            } else if (myResourceUnit.isInactive()) {
                // failed to inactive
                resourceUnitFailedToInactive();
            } else if (myResourceUnit.isIdle()) {
                // failed to idle
                resourceUnitFailedToIdle();
            }
        }
    }

    protected void resourceUnitInactiveToIdle() {
//     JSL.out.println(getTime() + " > transition from Inactive to Idle");
    }

    protected void resourceUnitInactiveToInactive() {
//     JSL.out.println(getTime() + " > transition from Inactive to Inactive");
    }

    protected void resourceUnitInactiveToFailed() {
//    JSL.out.println(getTime() + " > transition from Inactive to Failed");
    }

    protected void resourceUnitInactiveToBusy() {
//        JSL.out.println(getTime() + " > transition from Inactive to Busy");
    }

    protected void resourceUnitBusyToBusy() {
//        System.out.println(getTime() + " > transition from Busy to Busy");
    }

    protected void resourceUnitBusyToFailed() {
//      JSL.out.println(getTime() + " > transition from Busy to Failed");
    }

    protected void resourceUnitBusyToInactive() {
//        JSL.out.println(getTime() + " > transition from Busy to Inactive");
    }

    protected void resourceUnitBusyToIdle() {
//        JSL.out.println(getTime() + " > transition from Busy to Idle");
    }

    protected void resourceUnitFailedToBusy() {
//        JSL.out.println(getTime() + " > transition from Failed to Busy");
    }

    protected void resourceUnitFailedToFailed() {
//    JSL.out.println(getTime() + " > transition from Failed to Failed");
    }

    protected void resourceUnitFailedToInactive() {
//      JSL.out.println(getTime() + " > transition from Failed to Inactive");
    }

    protected void resourceUnitFailedToIdle() {
//        JSL.out.println(getTime() + " > transition from Failed to Idle");
    }

    protected void resourceUnitIdleToBusy() {
//        JSL.out.println(getTime() + " > transition from Idle to Busy");
    }

    protected void resourceUnitIdleToFailed() {
//       JSL.out.println(getTime() + " > transition from Idle to Failed");
    }

    protected void resourceUnitIdleToInactive() {
//        JSL.out.println(getTime() + " > transition from Idle to Inactive");
    }

    protected void resourceUnitIdleToIdle() {
//       JSL.out.println(getTime() + " > transition from Idle to Idle");
    }
}
