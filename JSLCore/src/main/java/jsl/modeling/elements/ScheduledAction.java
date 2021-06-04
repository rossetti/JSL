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
package jsl.modeling.elements;

import jsl.simulation.EventAction;
import jsl.simulation.JSLEvent;

import java.util.Objects;

/** A ScheduledAction is used on a ActionSchedule.
 *  A ScheduledAction represents a duration of time and action that
 *  will occur after the duration
 * 
 *
 */
public class ScheduledAction {

//    private final ActionSchedule myActionSchedule;

    private final double myDuration;

    private final String myName;
    
    private final EventAction myEventAction;

    /** Creates a ScheduleAction and places it on the supplied ActionSchedule
     *
//     * @param schedule the schedule that holds the actions
     * @param duration the duration from the start of the schedule, must be positive
     * @param eventAction the event action that will be invoked, cannot be null
     * @param name the name of the scheduled action
     */
    ScheduledAction(EventAction eventAction, double duration, String name) {
//        Objects.requireNonNull(schedule, "The ActionSchedule was null");
        Objects.requireNonNull(eventAction, "The EventActionIfc was null");
        if (duration <= 0.0) {
            throw new IllegalArgumentException("The time duration must be > 0");
        }
        myDuration = duration;
//        myActionSchedule = schedule;
        myEventAction = eventAction;
        myName = name;
    }

    public final double getDuration() {
        return myDuration;
    }

    /** Gets the name of the event
     * @return The name of the event
     */
    public final String getName() {
        return (myName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name = ").append(myName).append("\t");
        sb.append("Duration = ").append(myDuration).append("\t");
        return sb.toString();
    }

    /**
     *
     * @param event the event associated with the action
     */
    void action(JSLEvent<Object> event){
        myEventAction.action(event);
    }
}
