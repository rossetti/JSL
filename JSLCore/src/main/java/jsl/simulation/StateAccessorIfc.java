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
package jsl.simulation;

import jsl.utilities.IdentityIfc;
import jsl.utilities.statistic.Statistic;

import java.util.Optional;

/**
 *
 */
public interface StateAccessorIfc extends IdentityIfc {

    /**
     * Gets whether or not the state has been entered
     *
     * @return True means that the state has been entered
     */
    boolean isEntered();

    /**
     * Gets the time that the state was last entered
     *
     * @return A double representing the time that the state was last entered
     */
    double getTimeStateEntered();

    /**
     * Gets the time that the state was last exited
     *
     * @return A double representing the time that the state was last exited
     */
    double getTimeStateExited();

    /**
     * Gets the number of times the state was entered
     *
     * @return A double representing the number of times entered
     */
    double getNumberOfTimesEntered();

    /**
     * Gets the number of times the state was exited
     *
     * @return A double representing the number of times exited
     */
    double getNumberOfTimesExited();

    /**
     * Gets a statistic that collected sojourn times
     *
     * @return A statistic for sojourn times or null if use statistic was false
     */
    Optional<Statistic> getSojournTimeStatistic();

    /**
     * Gets the total time spent in the state
     *
     * @return a double representing the total sojourn time
     */
    double getTotalTimeInState();

    /**
     * @return returns getTimeStateExited() - getTimeStateEntered()
     */
    default double getTimeInState() {
        return getTimeStateExited() - getTimeStateEntered();
    }

}
