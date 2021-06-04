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

/** Models a cumulative rate function for the nonhomogeneous Poisson Process
 * @author rossetti
 *
 */
public interface CumulativeRateFunctionIfc extends RateFunctionIfc {

    /** Gets the cumulative rate from time 0 to the supplied time
     *  for the rate function
     * 
     * @param time the time to evaluate
     * @return Gets the cumulative rate from time 0 to the supplied time
     */
    double getCumulativeRate(double time);

    /** The function's lower limit on the cumulative rate range
     * 
     * @return The function's lower limit on the cumulative rate range
     */
    double getCumulativeRateRangeLowerLimit();

    /** The function's upper limit on the cumulative rate range
     * 
     * @return The function's upper limit on the cumulative rate range
     */
    double getCumulativeRateRangeUpperLimit();
}
