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

import jsl.modeling.queue.QObject;

import java.util.Objects;

/**
 * A FailureNotice represents a notification that the ResourceUnit should go
 * down due to a failure. The failure notice may be required to be immediate or
 * not. If not immediate, then the failure may be delayed until the resource
 * unit finishes its current activity.
 *
 * A FailureNotice has a well-defined state pattern.  When created, the notice
 * is placed in the Created state. From the created state, the notice can
 * become active, delayed, or ignored. If the notice is active, the failure is in
 * progress. After being active, the notice can only become completed.
 * If the notice is delayed, the notice is waiting to become active.
 * After being delayed, the notice can become active or be ignored.
 * If the resource unit ignored the notice, then the notice is placed in the
 * ignored state. The ignored state is terminal.  Once the notice finishes
 * being active, it is placed in the completed state. The completed state
 * is terminal.  The associated FailureProcess is notified when the
 * notice enters these states. This allows FailureProcesses to react accordingly.
 *
 *
 * @author rossetti
 */
public class FailureNotice extends QObject {

    private final CreatedState myCreatedState = new CreatedState();
    private final ActiveState myActiveState = new ActiveState();
    private final DelayedState myDelayedState = new DelayedState();
    private final IgnoredState myIgnoredState = new IgnoredState();
    private final CompletedState myCompletedState = new CompletedState();

    private final FailureProcess myFailureProcess;
    private final double myDuration;
    private final boolean myDelayableFlag;
    private FailureNoticeState myState;
    private ResourceUnit myResourceUnit;

    /**
     *
     * @param fe the associated FailureElement
     * @param duration the time that the failure should last, must be greater than or equal to 0.0
     * @param delayOption true means it does not need to be immediate
     */
    FailureNotice(FailureProcess fe, double duration, boolean delayOption) {
        super(fe.getTime(), "FailureNotice");
        if (duration < 0){
            throw new IllegalArgumentException("The failure duration must be >= 0");
        }
        myFailureProcess = fe;
        myDuration = duration;
        myDelayableFlag = delayOption;
        myState = myCreatedState;
    }

    void setResourceUnit(ResourceUnit resourceUnit){
        Objects.requireNonNull(resourceUnit, "The resource unit was null");
        myResourceUnit = resourceUnit;
    }

    /**
     *
     * @return the ResourceUnit that the FailureNotice was sent to, or null if not sent
     */
    public final ResourceUnit getResourceUnit(){
        return myResourceUnit;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", FailureProcess = ").append(myFailureProcess.getName());
        sb.append(", ResourceUnit = ").append(myResourceUnit.getName());
        sb.append(", duration = ").append(getDuration());
        sb.append(", delayable = ").append(isDelayable());
        sb.append(", state = ").append(myState.myName);
        return sb.toString();
    }

    /**
     *
     * @return the time that the failure should last
     */
    public final double getDuration() {
        return myDuration;
    }

    /**
     *
     * @return the associated FailureProcess
     */
    public final FailureProcess getFailureProcess() {
        return myFailureProcess;
    }

    /**
     * If the notice can be delayed while resource finishes busy state
     *
     * @return true if the notice can be delayed
     */
    public final boolean isDelayable() {
        return myDelayableFlag;
    }

    /**
     *
     * @return true if in created state
     */
    public final boolean isCreated() {
        return myState == myCreatedState;
    }

    /**
     *
     * @return true if in delayed state
     */
    public final boolean isDelayed() {
        return myState == myDelayedState;
    }

    /**
     *
     * @return true if in ignored state
     */
    public final boolean isIgnored() {
        return myState == myIgnoredState;
    }

    /**
     *
     * @return true if in completed state
     */
    public final boolean isCompleted() {
        return myState == myCompletedState;
    }

    /**
     *
     * @return true if in active state
     */
    public final boolean isActive() {
        return myState == myActiveState;
    }

    final void activate() {
        myState.activate();
    }

    final void delay() {
        if (!isDelayable()) {
            throw new IllegalStateException("Tried to delay a FailureNotice that is not delayable.");
        }
        myState.delay();
    }

    final void ignore() {
        myState.ignore();
    }

    final void complete() {
        myState.complete();
    }

    protected final void setState(FailureNoticeState nextState){
        myState = nextState;
        myFailureProcess.failureNoticeStateChange(this);
    }

    protected class FailureNoticeState {

        protected final String myName;

        protected FailureNoticeState(String name) {
            myName = name;
        }

        protected void activate() {
            throw new IllegalStateException("Tried to activate from an illegal state: " + myName);
        }

        protected void delay() {
            throw new IllegalStateException("Tried to delay from an illegal state: " + myName);
        }

        protected void ignore() {
            throw new IllegalStateException("Tried to ignore from an illegal state: " + myName);
        }

        protected void complete() {
            throw new IllegalStateException("Tried to complete from an illegal state: " + myName);
        }
    }

    protected class CreatedState extends FailureNoticeState {

        public CreatedState() {
            super("Created");
        }

        @Override
        protected void activate() {
            setState(myActiveState);
        }

        @Override
        protected void delay() {
            setState(myDelayedState);
        }

        @Override
        protected void ignore() {
            setState(myIgnoredState);
        }
    }

    protected class ActiveState extends FailureNoticeState {

        public ActiveState() {
            super("Active");
        }

        @Override
        protected void complete() {
            setState(myCompletedState);
        }
    }

    protected class DelayedState extends FailureNoticeState {

        public DelayedState() {
            super("Delayed");
        }

        @Override
        protected void activate() {
            setState(myActiveState);
        }

        @Override
        protected void ignore() {
            setState(myIgnoredState);
        }
    }

    protected class CompletedState extends FailureNoticeState {

        public CompletedState() {
            super("Completed");
        }

    }

    protected class IgnoredState extends FailureNoticeState {

        public IgnoredState() {
            super("Ignored");
        }

    }
}
