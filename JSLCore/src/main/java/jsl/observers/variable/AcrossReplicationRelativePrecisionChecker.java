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
package jsl.observers.variable;

import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.utilities.statistic.*;
import jsl.modeling.elements.variable.*;
import jsl.observers.ModelElementObserver;

/**
 *
 */
public class AcrossReplicationRelativePrecisionChecker extends ModelElementObserver {

    /** The confidence level for within replication
     *  half-width checking. The default is Statistic.DEFAULT_CONFIDENCE_LEVEL
     *
     */
    protected double myConfidenceLevel = Statistic.DEFAULT_CONFIDENCE_LEVEL;

    /** The desired relative precision for stopping
     */
    protected double myDesiredRelativePrecision = 1.0;

    /** Creates a new instance of StatisticalObserver
     * @param desiredPrecision desired precision
     */
    public AcrossReplicationRelativePrecisionChecker(double desiredPrecision) {
        this(desiredPrecision, null);
    }

    /** Creates a new instance of StatisticalObserver
     * @param desiredPrecision the desired precision
     * @param name the name
     */
    public AcrossReplicationRelativePrecisionChecker(double desiredPrecision, String name) {
        super(name);
        setDesiredRelativePrecision(desiredPrecision);
    }

        /** Sets the confidence level for the statistic
     * @param alpha must be &gt; 0.0
     */
    public void setConfidenceLevel(double alpha) {
        if ((alpha <= 0.0) || (alpha >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }

        myConfidenceLevel = alpha;
    }

    /**
     * 
     * @return  the confidence level
     */
    public double getConfidenceLevel() {
        return (myConfidenceLevel);
    }

    /** Sets the desired relative precision
     *
     * @param desiredPrecision the desired relative precision
     */
    public final void setDesiredRelativePrecision(double desiredPrecision) {
        if (desiredPrecision <= 0) {
            throw new IllegalArgumentException("Desired relative precision must be > 0.");
        }

        myDesiredRelativePrecision = desiredPrecision;
    }

    /** Gets the current desired half-width
     *
     * @return the current desired half-width
     */
    public final double getDesiredRelativePrecision() {
        return (myDesiredRelativePrecision);
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        ResponseVariable x = (ResponseVariable) m;
        Simulation s = x.getSimulation();
        
        if (s == null) {
            return;
        }
        if (s.getCurrentReplicationNumber() <= 2.0) {
            return;
        }

        StatisticAccessorIfc stat = x.getAcrossReplicationStatistic();
        double hw = stat.getHalfWidth(getConfidenceLevel());
        double xbar = stat.getAverage();
        if (hw <= myDesiredRelativePrecision * xbar) {
            s.end("Relative precision condition met for " + x.getName());
        }
    }
}
