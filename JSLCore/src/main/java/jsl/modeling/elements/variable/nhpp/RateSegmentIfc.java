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

public interface RateSegmentIfc {

    /** Returns a new instance of the rate segment
     * 
     * @return a new instance of the rate segment
     */
    RateSegmentIfc newInstance();
    
    /** Returns true if the supplied time is within this
     *  rate segments time interval
     * 
     * @param time the time to be evaluated
     * @return true if in the segment
     */
    boolean contains(double time);

    /** Returns the rate for the interval
     * 
     * @param time  the time to evaluate
     * @return the rate at the time
     */
    double getRate(double time);

    /** The rate at the time that the time interval begins
     * 
     * @return The rate at the time that the time interval begins
     */
    double getRateAtLowerTimeLimit();

    /** The rate at the time that the time interval ends
     * 
     * @return The rate at the time that the time interval ends
     */
    double getRateAtUpperTimeLimit();

    /** The lower time limit
     * 
     * @return The lower time limit
     */
    double getLowerTimeLimit();

    /** The upper time limit
     * 
     * @return The upper time limit
     */
    double getUpperTimeLimit();

    /** The width of the interval
     * 
     * @return The width of the interval
     */
    double getTimeWidth();

    /** The lower limit on the cumulative rate axis
     * 
     * @return The lower limit on the cumulative rate axis
     */
    double getCumulativeRateLowerLimit();

    /** The upper limit on the cumulative rate axis
     * 
     * @return The upper limit on the cumulative rate axis
     */
    double getCumulativeRateUpperLimit();

    /** The cumulative rate interval width
     * 
     * @return The cumulative rate interval width
     */
    double getCumulativeRateIntervalWidth();

    /** Returns the value of the cumulative rate function for the interval
     * given a value of time within that interval 
     * 
     * @param time the time to be evaluated
     * @return the cumulative rate at the given time
     */
    double getCumulativeRate(double time);

    /** Returns the inverse of the cumulative rate function given the interval
     *  and a cumulative rate value within that interval.  Returns a time
     * 
     * @param cumRate the cumulative rate
     * @return the inverse of the cumulative rate function
     */
    double getInverseCumulativeRate(double cumRate);
}