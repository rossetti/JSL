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

/** An interface used to implement the actions associated with
 * event logic within the simulation.
 * @param <T> the type associated with the JSLEvent's message property
 *
 * Implementor's of this interface should define a class that has concrete
 * specification for the type T.
 */
@FunctionalInterface
public interface EventActionIfc<T> {

    /** This must be implemented by any objects that want to supply event
     * logic.  This is essentially the "event routine".
     * @param event The event that triggered this action.
     */
    void action(JSLEvent<T> event);
}

