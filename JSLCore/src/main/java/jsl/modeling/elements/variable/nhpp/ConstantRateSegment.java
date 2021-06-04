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

/**
 * @author rossetti
 *
 */
public class ConstantRateSegment implements RateSegmentIfc {

    /**the rate for the interval
     * 
     */
    protected double rate = 0;

    /** the width of the interval on the cumulative rate scale (crWidth = crUL - crLL)
     * 
     */
    protected double crWidth = 0;

    /** the lower limit of the interval on cumulative rate scale
     * 
     */
    protected double crLL = 0;

    /** the upper limit of the interval on the cumulative rate scale
     * 
     */
    protected double crUL = 0;

    /** the width of the interval on the time scale (tWidth = tUL - tLL)
     * 
     */
    protected double tWidth = 0;

    /** the lower limit of the interval on the time scale
     * 
     */
    protected double tLL = 0;

    /** the upper limit of the interval on the time scale
     * 
     */
    protected double tUL = 0;

    /**
     * 
     * @param crLower represents the cumulative rate at the beginning of the segment, must be &gt;=0
     * @param tLower represents the time at the beginning of the segment, must be &gt;=0
     * @param duration represents the time duration of the segment, must be &gt;=0
     * @param rate represents the rate for the time segment, must be 0 &lt; rate &lt;= infinity
     */
    public ConstantRateSegment(double crLower, double tLower, double duration, double rate) {
        setInterval(tLower, duration);
        setRate(crLower, rate);
    }

    @Override
    public ConstantRateSegment newInstance(){
        return new ConstantRateSegment(crLL, tLL, tWidth, rate);
    }
    
    @Override
    public boolean contains(double time) {
        return (tLL <= time) && (time < tUL);
    }

    /**
     * 
     * @param tLower the lower time limit of the interval, must be &gt; = 0
     * @param duration the duration of the interval, must be &gt; 0
     */
    public final void setInterval(double tLower, double duration) {
        if (tLower < 0.0) {
            throw new IllegalArgumentException("The lower time limit must be >= 0");
        }
        if (duration <= 0.0) {
            throw new IllegalArgumentException("The duration must be > 0");
        }
        tLL = tLower;
        tUL = tLL + duration;
        tWidth = duration;
    }

    /**
     * @param crLower represents the cumulative rate at the beginning of the segment, must be &gt;=0
     * @param rate represents the rate for the time segment, must be 0 &lt; rate &lt; = infinity
     */
    public final void setRate(double crLower, double rate) {
        if (crLower < 0.0) {
            throw new IllegalArgumentException("The lower rate limit must be >= 0");
        }

        if (rate < 0.0) {
            throw new IllegalArgumentException("The rate must be > 0");
        }

        if (rate >= Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("The rate must be < infinity");
        }

        crLL = crLower;
        this.rate = rate;
        crUL = crLL + rate * tWidth;
        crWidth = crUL - crLL;
    }

    /** Returns the rate for the interval
     * 
     * @return the rate
     */
    public double getRate() {
        return (rate);
    }

    /** Gets the rate for the time within the interval
     * 
     */
    @Override
    public double getRate(double time) {
        return rate;
    }

    /* (non-Javadoc)
     * @see jsl.modeling.elements.variable.nhpp.RateSegmentIfc#getRateAtLowerTimeLimit()
     */
    @Override
    public double getRateAtLowerTimeLimit() {
        return rate;
    }

    /** The rate at the upper time limit is undefined Double.NaN.  The rate for the
     *  segment is constant throughout the interval but undefined at the end of the interval
     * 
     */
    @Override
    public double getRateAtUpperTimeLimit() {
        return Double.NaN;
    }

    /** The lower time limit
     * 
     * @return The lower time limit
     */
    @Override
    public double getLowerTimeLimit() {
        return (tLL);
    }

    /** The upper time limit
     * 
     * @return The upper time limit
     */
    @Override
    public double getUpperTimeLimit() {
        return (tUL);
    }

    /** The width of the interval
     * 
     * @return The width of the interval
     */
    @Override
    public double getTimeWidth() {
        return (tWidth);
    }

    /** The lower limit on the cumulative rate axis
     * 
     * @return The lower limit on the cumulative rate axis
     */
    @Override
    public double getCumulativeRateLowerLimit() {
        return (crLL);
    }

    /** The upper limit on the cumulative rate axis
     * 
     * @return The upper limit on the cumulative rate axis
     */
    @Override
    public double getCumulativeRateUpperLimit() {
        return (crUL);
    }

    /** The cumulative rate interval width
     * 
     * @return The cumulative rate interval width
     */
    @Override
    public double getCumulativeRateIntervalWidth() {
        return (crWidth);
    }

    /** Returns the value of the cumulative rate function for the interval
     * given a value of time within that interval 
     * 
     * @param time the time to be evaluated
     * @return cumulative rate at time t
     */
    @Override
    public double getCumulativeRate(double time) {
        double t = time - tLL;
        double mt = crLL + rate * t;
        return (mt);
    }

    /** Returns the inverse of the cumulative rate function given the interval
     *  and a cumulative rate value within that interval.  Returns a time
     * 
     * @param cumRate the rate to be evaluated
     * @return the inverse at the rate
     */
    @Override
    public double getInverseCumulativeRate(double cumRate) {
        if (rate == 0.0) {
            return (Double.NaN);
        }

        double t = cumRate - crLL;
        double inverse = tLL + t / rate;
        return (inverse);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("rate = ");
        sb.append(rate);
        sb.append(" [");
        sb.append(tLL);
        sb.append(",");
        sb.append(tUL);
        sb.append(") width = ");
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
