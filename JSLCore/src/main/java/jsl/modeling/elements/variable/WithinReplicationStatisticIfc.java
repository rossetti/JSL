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

import jsl.utilities.statistic.WeightedStatisticIfc;


/** An interface for accessing within replication statistics
 *  Within replication statistics can be observation or time weighted.
 *  No variance information is provided due to the fact that the standard
 *  generate variance estimator is likely to be biased because of within
 *  replication correlation.
 *
 * @author rossetti
 */
public interface WithinReplicationStatisticIfc {

    /** Returns a reference to the underlying WeightedStatistic
     *
     * @return
     */
    WeightedStatisticIfc getWithinReplicationStatistic();

}
