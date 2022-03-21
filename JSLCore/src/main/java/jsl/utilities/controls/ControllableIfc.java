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
package jsl.utilities.controls;

import jsl.utilities.IdentityIfc;

/** Implementors of this interface should be able to return an instance of
 *  the Controls class and should be able to take in an instance of Controls
 *  and use it correctly to set the internal state of the implementation.
 *
 *
 */
public interface ControllableIfc extends IdentityIfc {

    /** Returns a valid instance of Controls that can be used with
     *  this ControllableIfc or null
     * 
     * @return valid instance of Controls
     */
    Controls getControls();

    /** Takes in a valid instance of Controls for this class
     *  If controls is null or if it was not created by this
     *  class this method should throw an IllegalArgumentException
     * 
     * @param controls takes in a valid instance of Controls for this class
     */
    void setControls(Controls controls);
}
