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

import jsl.utilities.GetValueIfc;

/**  Continually gets the value of the supplied GetValueIfc in the run() until
 * the supplied sampling half-width requirement is met or the default maximum 
 * number of iterations is reached, whichever comes first.
 *
 * @author rossetti
 */
public class HalfWidthSequentialSampler {

    protected double myDefaultDesiredHW = 0.001;

    protected long myDefaultMaxIterations = 10000;

    protected Statistic myStatistic;

    public HalfWidthSequentialSampler() {
        this(null);
    }

    public HalfWidthSequentialSampler(String name) {
        myStatistic = new Statistic(name);
    }

    public final double getDefaultDesiredHalfWidth() {
        return myDefaultDesiredHW;
    }

    public final long getDefaultMaxIterations() {
        return myDefaultMaxIterations;
    }

    public void setConfidenceLevel(double alpha) {
        myStatistic.setConfidenceLevel(alpha);
    }

    public final void setDefaultDesiredHalfWidth(double hw) {
        if (hw <= 0.0) {
            throw new IllegalArgumentException("The desired half-width must be > 0");
        }
        myDefaultDesiredHW = hw;
    }

    public final void setDefaultMaxIterations(long maxIter) {
        if (maxIter <= 1) {
            throw new IllegalArgumentException("The maximum number of iterations must be > 1");
        }
        myDefaultMaxIterations = maxIter;
    }

    public StatisticAccessorIfc getStatistic() {
        return myStatistic;
    }

    public final boolean run(GetValueIfc v) {
        return run(v, getDefaultDesiredHalfWidth(), getDefaultMaxIterations());
    }

    public final boolean run(GetValueIfc v, double hw) {
        return run(v, hw, getDefaultMaxIterations());
    }

    public boolean run(GetValueIfc v, double dhw, long maxIter) {
        myStatistic.reset();
        double hw = Double.POSITIVE_INFINITY;
        boolean converged = false;
        do {
            myStatistic.collect(v);
            if (myStatistic.getCount() > 1) {
                hw = myStatistic.getHalfWidth();
                if ((hw > 0.0) && (hw <= dhw)) {
                    converged = true;
                    break;
                }
            }
        } while (myStatistic.getCount() < maxIter);
        return converged;
    }
}
