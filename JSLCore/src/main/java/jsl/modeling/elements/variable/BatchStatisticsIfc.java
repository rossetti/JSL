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

import jsl.utilities.statistic.StatisticAccessorIfc;

public interface BatchStatisticsIfc {

	/** Gets the batching option flag that indicates whether or not this
	 * time weighted variable will allow the model's batching event
	 * to perform time based batching.
	 * @return True means it will allow
	 */
	public abstract boolean isDefaultBatchingOn();

	/** Indicates whether or not the response variable
	 *  has batching turned on, either by default or by its own choosing.
	 *  In other words, whether it will be batched. This if false by default, and
	 *  will be set to true if:
	 *  1) the response variable participates in default batching or
	 *  2) the user sets this directly
	 * @return True means it will be batched
	 */
	public abstract boolean isBatchingOn();

	/** Indicates whether or not the response variable
	 *  has batching turned on, either by default or by its own choosing.
	 *  In other words, whether it will be batched. This is false by default, and
	 *  will be set to true if:
	 *  1) the response variable participates in default batching or
	 *  2) the user sets this directly
	 *  
	 * @param option
	 */
	public abstract void setBatchingOption(boolean option);

	/** Returns a StatisticAccessorIfc for the within replication batching statistics
	 *  that have been collected on this ResponseVariable.  
	 * 
	 * @return
	 */
	public abstract StatisticAccessorIfc getReplicationBatchStatistic();

	/** A convenience method to get the within replication batch average from
	 *  the underlying StatisticalObserver For other statistics use
	 *  getReplicationBatchStatistic()
	 * 
	 * @return
	 */
	public abstract double getReplicationAcrossBatchAverage();

	/** Sets the parameters for batch statistics for the underlying statistical observer
	 * @param minNumBatches The minimum number of batches, must be &gt;= 2
	 * @param minBatchSize The minimum number of observations per batch, must be &gt;= 2
	 * @param maxNBMultiple The maximum number of batches as a multiple of the minimum number of batches.
	 * For example, if minNB = 20 and maxNBMultiple = 2 then the maximum number of batches allowed will be 40.
	 * maxNBMultiple must be &gt;= 2.
	 */
	public abstract void setBatchingParameters(int minNumBatches,
			int minBatchSize, int maxNBMultiple);

	public abstract void setAcrossBatchStatisticName(String name);

}