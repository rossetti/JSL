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

/** Each statistic value separated by a comma with a corresponding header
 *
 * @author rossetti
 */
public interface GetCSVStatisticIfc {

    /**
     *
     * @return the CSV string for the values of the statistics
     */
    String getCSVStatistic();

    /** The header string for the CVS representation
     *
     * @return the CVS header string
     */
    String getCSVStatisticHeader();

}
