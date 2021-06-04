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
public class AcrossReplicationHalfWidthChecker extends ModelElementObserver {

    /** The confidence level for within replication
     *  half-width checking. The default is Statistic.DEFAULT_CONFIDENCE_LEVEL
     *
     */
    protected double myConfidenceLevel = Statistic.DEFAULT_CONFIDENCE_LEVEL;

    /** The desired half-width for stopping
     */
    protected double myDesiredHalfWidth = 1.0;

    /** Creates a new instance of StatisticalObserver
     * @param desiredHW the desired half-width
     */
    public AcrossReplicationHalfWidthChecker(double desiredHW) {
        this(desiredHW, null);
    }

    /** Creates a new instance of StatisticalObserver
     * @param desiredHW the desired half-width
     * @param name  the name of the checker
     */
    public AcrossReplicationHalfWidthChecker(double desiredHW, String name) {
        super(name);
        setDesiredHalfWidth(desiredHW);
    }

    /** Sets the confidence level for the statistic
     * @param level must be &gt; 0.0 and less than 1
     */
    public void setConfidenceLevel(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }

        myConfidenceLevel = level;
    }

    public double getConfidenceLevel() {
        return (myConfidenceLevel);
    }

    /** Sets the desired half-width
     *
     * @param desiredHalfWidth the desired half-width
     */
    public final void setDesiredHalfWidth(double desiredHalfWidth) {
        if (desiredHalfWidth <= 0) {
            throw new IllegalArgumentException("Desired half-width must be > 0.");
        }

        myDesiredHalfWidth = desiredHalfWidth;
    }

    /** Gets the current desired half-width
     *
     * @return the current desired half-width
     */
    public final double getDesiredHalfWidth() {
        return (myDesiredHalfWidth);
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
        if (hw <= myDesiredHalfWidth) {
            s.end("Half-width condition met for " + x.getName());
        }
    }
}
