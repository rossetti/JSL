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
package jsl.calendar;

import java.util.PriorityQueue;

import jsl.simulation.JSLEvent;

/**
 * This class provides an event calendar by using a priority queue to hold the
 * underlying events.
 */
public class PriorityQueueEventCalendar implements CalendarIfc {

    private final PriorityQueue<JSLEvent> myEventSet;

    public PriorityQueueEventCalendar() {
        super();
        myEventSet = new PriorityQueue<>();
    }

    @Override
    public final void add(JSLEvent event) {
        myEventSet.add(event);
    }

    @Override
    public final JSLEvent nextEvent() {
        return ((JSLEvent) myEventSet.poll());
    }

    @Override
    public final JSLEvent peekNext() {
        return (myEventSet.peek());
    }

    @Override
    public final boolean isEmpty() {
        return (myEventSet.isEmpty());
    }

    @Override
    public final void clear() {
        myEventSet.clear();
    }

    @Override
    public final void cancel(JSLEvent event) {
        event.setCanceledFlag(true);
    }

    @Override
    public int size() {
        return (myEventSet.size());
    }

}
