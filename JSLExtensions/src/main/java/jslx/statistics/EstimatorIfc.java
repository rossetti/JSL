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

package jslx.statistics;

import jsl.utilities.statistic.Statistic;

public interface EstimatorIfc {

    double getEstimate(double[] data);

    /**
     * A predefined EstimatorIfc that estimates the mean of the data
     */
    class Average implements EstimatorIfc {
        private Statistic s = new Statistic();
        @Override
        public double getEstimate(double[] data) {
            s.reset();
            s.collect(data);
            return s.getAverage();
        }
    }

    /**
     * A predefined EstimatorIfc that estimates the variance of the data
     */
    class Variance implements EstimatorIfc {
        private Statistic s = new Statistic();
        @Override
        public double getEstimate(double[] data) {
            s.reset();
            s.collect(data);
            return s.getVariance();
        }
    }

    /**
     * A predefined EstimatorIfc that estimates the median of the data
     */
    class Median implements EstimatorIfc {
        public double getEstimate(double[] data) {
            return Statistic.getMedian(data);
        }
    }

    /**
     * A predefined EstimatorIfc that estimates the minimum of the data
     */
    class Minimum implements EstimatorIfc {
        public double getEstimate(double[] data) {
            return Statistic.getMin(data);
        }
    }

    /**
     * A predefined EstimatorIfc that estimates the maximum of the data
     */
    class Maximum implements EstimatorIfc {
        public double getEstimate(double[] data) {
            return Statistic.getMax(data);
        }
    }
}
