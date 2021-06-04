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
package jsl.modeling.elements.variable.nhpp;

/** Models a rate function for the non-stationary Poisson Process
 * @author rossetti
 *
 */
public interface RateFunctionIfc {

    /** Returns the rate the the supplied time
     * 
     * @param time the time to evaluate
     * @return Returns the rate the the supplied time
     */
    double getRate(double time);

    /** Gets the maximum value of the rate function over its time horizon
     *  
     * @return Gets the maximum value of the rate function over its time horizon
     */
    double getMaximum();

    /** Gets the minimum value of the rate function over its time horizon
     * 
     * @return Gets the minimum value of the rate function over its time horizon
     */
    double getMinimum();

    /** The function's lower limit on the time range
     * 
     * @return The function's lower limit on the time range
     */
    double getTimeRangeLowerLimit();

    /** The function's upper limit on the time range
     * 
     * @return The function's upper limit on the time range
     */
    double getTimeRangeUpperLimit();

    /** Returns true if the supplied time is within the time range
     *  of the rate function
     * 
     * @param time the time to evaluate
     * @return true if the supplied time is within the time range
     */
    boolean contains(double time);
}
