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

import jsl.utilities.math.JSLMath;

/**
 * @author rossetti
 *
 */
public class LinearRateSegment implements RateSegmentIfc {

    /**
     * the slope for the interval
     */
    protected double slope = 0; 

    /** the rate at the lower limit of the interval
     * 
     */
    protected double rLL = 0;

    /**
     * the rate at the upper limit of the interval
     */
    protected double rUL = 0; 

    /**
     * the width of the interval on the cumulative rate scale (crWidth = crUL - crLL)
     */
    protected double crWidth = 0; 

    /**
     * the lower limit of the interval on cumulative rate scale
     */
    protected double crLL = 0; 

    /** the upper limit of the interval on the cumulative rate scale
     * 
     */
    protected double crUL = 0; 

    /**
     * the width of the interval on the time scale (tWidth = tUL - tLL)
     */
    protected double tWidth = 0; 

    /**
     * the lower limit of the interval on the time scale
     */
    protected double tLL = 0; 

    /**
     * the upper limit of the interval on the time scale
     */
    protected double tUL = 0; 

    protected LinearRateSegment(double cumRateLL, double timeLL, double rateLL, double timeUL, 
            double rateUL) {
        tLL = timeLL;
        rLL = rateLL;
        tUL = timeUL;
        rUL = rateUL;
        tWidth = tUL - tLL;
        slope = (rUL - rLL) / tWidth;
        crLL = cumRateLL;
        crUL = crLL + (0.5) * (rUL + rLL) * (tUL - tLL);
        crWidth = crUL - crLL;
    }

    @Override
    public LinearRateSegment newInstance(){
        return new LinearRateSegment(crLL, tLL, rLL, tUL, rUL);
    }
    
    @Override
    public boolean contains(double time) {
        return (tLL <= time) && (time <= tUL);
    }

    public double getSlope() {
        return slope;
    }

    @Override
    public double getRate(double time) {
        if (JSLMath.equal(slope, 0.0)) {
            return rLL;
        } else {
            return (rLL + slope * (time - tLL));
        }
    }

    @Override
    public double getRateAtLowerTimeLimit() {
        return rLL;
    }

    @Override
    public double getRateAtUpperTimeLimit() {
        return rUL;
    }

    @Override
    public double getLowerTimeLimit() {
        return (tLL);
    }

    @Override
    public double getUpperTimeLimit() {
        return (tUL);
    }

    @Override
    public double getTimeWidth() {
        return (tWidth);
    }

    @Override
    public double getCumulativeRateLowerLimit() {
        return (crLL);
    }

    @Override
    public double getCumulativeRateUpperLimit() {
        return (crUL);
    }

    @Override
    public double getCumulativeRateIntervalWidth() {
        return (crWidth);
    }

    @Override
    public double getCumulativeRate(double time) {
        if (JSLMath.equal(slope, 0.0)) {
            return (crLL + rLL * (time - tLL));
        } else {
            return (crLL + rLL * (time - tLL) + (0.5) * slope * (time - tLL) * (time - tLL));
        }
    }

    @Override
    public double getInverseCumulativeRate(double cumRate) {

        if (JSLMath.equal(slope, 0.0)) {
            if (JSLMath.equal(rLL, 0.0)) {
                return Double.NaN;
            } else {
                return (tLL + (cumRate - crLL) / rLL);
            }
        } else {
            double n = 2.0 * (cumRate - crLL);
            double d = rLL + Math.sqrt(rLL * rLL + 2.0 * slope * (cumRate - crLL));
            return (tLL + n / d);
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" [");
        sb.append(rLL);
        sb.append(",");
        sb.append(rUL);
        sb.append("] slope = ");
        sb.append(slope);
        sb.append(" [");
        sb.append(tLL);
        sb.append(",");
        sb.append(tUL);
        sb.append("] width = ");
        sb.append(tWidth);
        sb.append(" [");
        sb.append(crLL);
        sb.append(",");
        sb.append(crUL);
        sb.append("] cr width = ");
        sb.append(crWidth);
        sb.append("\n");
        return (sb.toString());
    }
}
