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
import jsl.utilities.random.RandomIfc;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 *  A FailureEvent models a one time failure process.  There is a time until the failure
 *  occurs and a duration for the failure.  When the process is started the
 *  failure event is scheduled. If the process is not started then no failure event
 *  scheduled.  Stopping the process causes the failure event to be cancelled. If the failure event
 *  occurs then the end of the duration of the event is scheduled.
 *
 *  Suspending the process causes the first and only failure event to be cancelled, if the process has
 *  been started.  If the process has been suspended, then this implies that it has
 *  been started but the failure event has not yet occurred.  Suspending the process can
 *  not stop the failure if it has already occurred.  Resuming the process only causes the
 *  first event to be uncancelled.  If the failure has already occurred, then resume has not effect.
 */
public class SingleFailureEvent extends FailureProcess {

    private final Set<FailureEventListenerIfc> myFailureEventListeners;

    /**
     *
     * @param resourceUnit the resource unit effected by the failure
     * @param duration the duration of the failure
     */
    public SingleFailureEvent(ResourceUnit resourceUnit, RandomIfc duration) {
        this(resourceUnit, duration, null, null);
    }

    /**
     *
     * @param resourceUnit the resource unit effected by the failure
     * @param duration the duration of the failure
     * @param name the name of the model element
     */
    public SingleFailureEvent(ResourceUnit resourceUnit, RandomIfc duration, String name) {
        this(resourceUnit, duration, null, name);
    }

    /**
     *
     * @param resourceUnit the resource unit effected by the failure
     * @param duration the duration of the failure
     * @param initialStartTimeRV the time that the failure should start
     */
    public SingleFailureEvent(ResourceUnit resourceUnit, RandomIfc duration, RandomIfc initialStartTimeRV) {
        this(resourceUnit, duration, initialStartTimeRV, null);
    }

    /**
     *
     * @param resourceUnit the resource unit effected by the failure
     * @param duration the duration of the failure
     * @param initialStartTimeRV the time that the failure should start
     * @param name the name of the model element
     */
    public SingleFailureEvent(ResourceUnit resourceUnit, RandomIfc duration,
                              RandomIfc initialStartTimeRV, String name) {
        super(resourceUnit, duration, initialStartTimeRV, name);
        setPriority(JSLEvent.DEFAULT_PRIORITY - 5);
        myFailureEventListeners = new LinkedHashSet<>();
    }

    // so that one random value is used per replication

    /** Adds a listener to react to failureStarted event
     *
     * @param listener the listener to add
     */
    public final void addFailureEventListener(FailureEventListenerIfc listener){
        if (listener == null){
            return;
        }
        myFailureEventListeners.add(listener);
    }

    /** Removes the listener from the event
     *
     * @param listener the listener to remove
     */
    public final void removeFailureEventListener(FailureEventListenerIfc listener){
        if (listener == null){
            return;
        }
        myFailureEventListeners.remove(listener);
    }

    /**
     *  Used internally to notify failure event listeners that the failure started
     */
    protected final void notifyFailureEventListenersFailureStarted(){
        for(FailureEventListenerIfc fpl: myFailureEventListeners){
            fpl.failureStarted();
        }
    }

    /**
     * Used internally to notify failure event listeners that the failure completed
     */
    protected final void notifyFailureEventListenersFailureCompleted(){
        for(FailureEventListenerIfc fpl: myFailureEventListeners){
            fpl.failureCompleted();
        }
    }

    @Override
    protected void failureNoticeActivated(FailureNotice fn) {
        // since it is a one time event notify the listeners of it
        notifyFailureEventListenersFailureStarted();
//        System.out.printf("%f > The FailureEvent %d was activated. %n", getTime(), fn.getId());
    }

    @Override
    protected void failureNoticeDelayed(FailureNotice fn) {
//        System.out.printf("%f > The FailureEvent %d was delayed. %n", getTime(), fn.getId());
        // nothing to do because it is a one time event
    }

    @Override
    protected void failureNoticeIgnored(FailureNotice fn) {
        // nothing to do because it is a one time event
    }

    @Override
    protected void failureNoticeCompleted(FailureNotice fn) {
        notifyFailureEventListenersFailureCompleted();
        // nothing to do because it is a one time event
//        System.out.printf("%f > The FailureEvent %d was completed. %n", getTime(), fn.getId());
    }

    @Override
    protected void suspendProcess() {
        // if the first event is not executed yet, then cancel it.
        cancelStartEvent();
    }

    @Override
    protected void stopProcess() {
        // if the first event is not executed yet, then cancel it.
        cancelStartEvent();
    }

    @Override
    protected void resumeProcess() {
        // can only be resumed if suspended, if suspended this implies that it has been started
        // cause the start event to be uncancelled
        unCancelStartEvent();
    }

}
