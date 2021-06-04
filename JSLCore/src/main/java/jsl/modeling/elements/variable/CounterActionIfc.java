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

public interface CounterActionIfc {

    /**
     * @return Returns the counter's limit.
     */
    public abstract double getCounterActionLimit();

    /** Returns true if the counter's last observer state is equal
     *  to COUNTER_LIMIT_REACHED
     *
     * @return
     */
    public abstract boolean checkForCounterLimitReachedState();

    /** Tells the Counter to add an CounterActionIfc that will
     *  automatically stop the replication when the counter limit is
     *  reached.
     *
     */
    public abstract void addStoppingAction();

    /** Adds a counter action listener.  It will be called if the counter's limit is
     *  set and it is reached.
     *
     * @param action
     * @return
     */
    public abstract boolean addCounterActionListener(
            CounterActionListenerIfc action);

    /** Removes the counter action listener
     * @param action
     * @return
     */
    public abstract boolean removeCounterActionListener(
            CounterActionListenerIfc action);
}
