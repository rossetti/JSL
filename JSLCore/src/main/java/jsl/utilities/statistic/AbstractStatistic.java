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
package jsl.utilities.statistic;

import jsl.utilities.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves as an abstract base class for statistical collection.
 *
 *
 */
abstract public class AbstractStatistic extends AbstractCollector
        implements StatisticAccessorIfc, GetCSVStatisticIfc, Comparable<AbstractStatistic> {

    /**
     * the default confidence level
     */
    public final static double DEFAULT_CONFIDENCE_LEVEL = 0.95;

    /**
     * Holds the confidence coefficient for the statistic
     */
    protected double myConfidenceLevel;

    /**
     * Used to count the number of missing data points presented When a data
     * point having the value of (Double.NaN, Double.POSITIVE_INFINITY,
     * Double.NEGATIVE_INFINITY) are presented it is excluded from the summary
     * statistics and the number of missing points is noted. Implementers of
     * subclasses are responsible for properly collecting this value and
     * resetting this value.
     *
     */
    protected double myNumMissing = 0.0;

    /**
     *
     */
    public AbstractStatistic() {
        this(null);
    }

    /**
     *
     * @param name the name of the statistic, can be null
     */
    public AbstractStatistic(String name) {
        super(name);
        myConfidenceLevel = DEFAULT_CONFIDENCE_LEVEL;
    }

    /**
     * Sets the confidence level for the statistic
     *
     * @param level must be in (0, 1)
     */
    public void setConfidenceLevel(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        myConfidenceLevel = level;
    }

    @Override
    public double getConfidenceLevel() {
        return (myConfidenceLevel);
    }

    @Override
    public double getNumberMissing() {
        return (myNumMissing);
    }

    /**
     * Returns a negative integer, zero, or a positive integer if this object is
     * less than, equal to, or greater than the specified object.
     *
     * The natural ordering is based on getAverage() with equals.
     *
     * @param stat The statistic to compare this statistic to
     * @return Returns a negative integer, zero, or a positive integer if this
     * object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(AbstractStatistic stat) {
        return Double.compare(getAverage(), stat.getAverage());
    }

}
