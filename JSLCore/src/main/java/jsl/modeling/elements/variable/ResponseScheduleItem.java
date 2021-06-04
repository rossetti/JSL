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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.modeling.elements.variable;

/**
 * Represents a ResponseInterval placed on a ResponseSchedule.
 * @author rossetti
 */
public class ResponseScheduleItem {

    private final double myStartTime;
    private final ResponseInterval myResponseInterval;
    private final ResponseSchedule mySchedule;

    /**
     *
     * @param time must be greater than zero. Represents start time relative to start of schedule
     * @param duration the ResponseInterval
     */
    protected ResponseScheduleItem(ResponseSchedule schedule, double time, double duration, String label) {
        if (schedule == null) {
            throw new IllegalArgumentException("The ResponseSchedule must not be null");
        }
        if (time < 0) {
            throw new IllegalArgumentException("The start time must be >= 0");
        }
        mySchedule = schedule;
        myStartTime = time;
        myResponseInterval = new ResponseInterval(schedule, duration, label);
        myResponseInterval.setResponseSchedule(schedule);
    }

    /**
     *
     * @return the time to start the schedule
     */
    public final double getStartTime() {
        return myStartTime;
    }

    /**
     * Adds a ResponseVariable to the item for data collection over the
     * interval
     *
     * @param theResponse the response to collect interval statistics on
     * @return a ResponseVariable for the interval
     */
    public final ResponseVariable addResponseToInterval(ResponseVariable theResponse) {
        return myResponseInterval.addResponseToInterval(theResponse);
    }

    /**
     * Adds a Counter to the interval for data collection over the interval
     *
     * @param theCounter the counter to collect interval statistics on
     * @return a ResponseVariable for the interval
     */
    public final ResponseVariable addCounterToInterval(Counter theCounter) {
        return myResponseInterval.addCounterToInterval(theCounter);
    }

    /**
     *
     * @return true if the interval has been scheduled
     */
    public final boolean isScheduled() {
        return myResponseInterval.isScheduled();
    }

    /**
     * When the interval was last started
     *
     * @return When the interval was last started
     */
    public final double getTimeLastStarted() {
        return myResponseInterval.getTimeLastStarted();
    }

    /**
     * When the interval was last ended
     *
     * @return When the interval was last ended
     */
    public final double getTimeLastEnded() {
        return myResponseInterval.getTimeLastEnded();
    }

    /**
     * The duration (length) of the interval
     *
     * @return The duration (length) of the interval
     */
    public final double getDuration() {
        return myResponseInterval.getDuration();
    }


    /**
     * Causes the response interval to be scheduled at the start time for
     * the item.
     */
    protected final void scheduleResponseInterval(){
        myResponseInterval.scheduleInterval(getStartTime());
    }

    /**
     *
     * @return the ResponseInterval
     */
    protected final ResponseInterval getResponseInterval() {
        return myResponseInterval;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule starts at time: ").append(mySchedule.getStartTime());
        sb.append("\t Item starts at time: ").append(mySchedule.getStartTime() + getStartTime());
        sb.append(System.lineSeparator());
        sb.append(myResponseInterval.toString());
        return sb.toString();
    }
}
