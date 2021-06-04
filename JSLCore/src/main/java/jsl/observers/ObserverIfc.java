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
package jsl.observers;

/** This interface works with observers as a call back function
 *  for when the observable needs observing
 *
 * @author rossetti
 */
public interface ObserverIfc {

    /** This method is called when the observable needs observing
     *
     * @param theObserved the thing observed
     * @param arg an object of info
     */
    void update(Object theObserved, Object arg );
}
