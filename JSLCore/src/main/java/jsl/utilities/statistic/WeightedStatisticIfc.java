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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.utilities.statistic;

import jsl.utilities.IdentityIfc;

/**
 * If the observation or the weight is
 *  * infinite or NaN, then the observation should not be recorded and the number of missing observations
 *  * is incremented. If the observed weight is negative or 0.0, then the observation should not be recorded and
 *  * the number of missing observations is incremented.
 *
 * @author rossetti
 */
public interface WeightedStatisticIfc extends IdentityIfc, GetCSVStatisticIfc {

    /**
     * Gets the count of the number of the observations.
     *
     * @return A double representing the count
     */
    double getCount();

    /**
     * Gets the maximum of the observations.
     *
     * @return A double representing the maximum
     */
    double getMax();

    /**
     * Gets the minimum of the observations.
     *
     * @return A double representing the minimum
     */
    double getMin();

    /**
     * Gets the sum of the observed weights.
     *
     * @return A double representing the sum of the weights
     */
    double getSumOfWeights();

    /**
     * Gets the weighted average of the collected observations.
     *
     * @return A double representing the weighted average or Double.NaN if no
     * observations.
     */
    double getAverage();

    /**
     * Gets the weighted sum of observations observed.
     *
     * @return A double representing the weighted sum
     */
    double getWeightedSum();

    /**
     * Gets the weighted sum of squares for observations observed.
     *
     * @return A double representing the weighted sum of squares
     */
    double getWeightedSumOfSquares();

    /**
     * Clears all the statistical accumulators
     */
    void reset();

    /**
     * Gets the last observed data point
     *
     * @return A double representing the last observed observation
     */
    double getLastValue();

    /**
     * Gets the last observed weight
     *
     * @return A double representing the last observed weight
     */
    double getLastWeight();

    /**
     * When a data point having the value of (Double.NaN,
     * Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY) are presented it is
     * excluded from the summary statistics and the number of missing points is
     * noted. If the weight is infinite or less than or equal to zero, then the data point should not be recorded.
     * This method reports the number of missing points that occurred
     * during the collection.
     *
     * @return the number missing
     */
    double getNumberMissing();

    /**
     *
     * @return the unweighted sum of all of the observed data
     */
    double getUnWeightedSum();

    /**
     *
     * @return the unweighted average of all the observed data
     */
    default double getUnWeightedAverage(){
        if (getCount() == 0){
            return Double.NaN;
        }
        return getUnWeightedSum()/getCount();
    }
}
