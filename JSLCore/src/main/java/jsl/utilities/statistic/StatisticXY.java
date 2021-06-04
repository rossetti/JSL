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

import jsl.utilities.IdentityIfc;
import jsl.utilities.math.JSLMath;

/**
 *
 */
public class StatisticXY implements IdentityIfc {

    /** A counter to count the number of created to assign "unique" ids
     */
    private static int myIdCounter_;

    /** The id of this object
     */
    protected int myId;

    /** Holds the name of the statistic for reporting purposes.
     */
    protected String myName;

    // variables for collecting statistics
    protected double avgx = 0.0;

    protected double avgy = 0.0;

    protected double varx = 0.0;

    protected double vary = 0.0;

    protected double sumxy = 0.0;

    protected double covxy = 0.0;

    protected double nxy = 0.0;

    /**
     *
     */
    public StatisticXY() {
        this(null);
    }

    public StatisticXY(String name) {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        setName(name);
        avgx = 0.0;
        avgy = 0.0;
        covxy = 0.0;
        nxy = 0.0;
        varx = 0.0;
        vary = 0.0;
    }

    public static StatisticXY newInstance(StatisticXY stat) {
        if (stat == null) {
            throw new IllegalArgumentException("The supplied StatisticXY was null");
        }
        StatisticXY s = new StatisticXY();
        s.avgx = stat.avgx;
        s.avgy = stat.avgy;
        s.covxy = stat.covxy;
        s.nxy = stat.nxy;
        s.varx = stat.varx;
        s.vary = stat.vary;
        return (s);
    }

    public StatisticXY newInstance() {
        StatisticXY s = new StatisticXY();
        s.avgx = avgx;
        s.avgy = avgy;
        s.covxy = covxy;
        s.nxy = nxy;
        s.varx = varx;
        s.vary = vary;
        return s;
    }

    /** Gets the name.
     * @return The name of object.
     */
    public final String getName() {
        return myName;
    }

    /** Sets the name
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            myName = s;
        } else {
            myName = str;
        }
    }

    /** Returns the id for this collector
     *
     * @return
     */
    public final int getId() {
        return (myId);
    }

    public final double getAverageX() {
        return (avgx);
    }

    public final double getSumX() {
        return avgx * getCount();
    }

    public final double getAverageY() {
        return (avgy);
    }

    public final double getSumY() {
        return avgy * getCount();
    }

    public final double getVarianceX() {
        return (varx);
    }

    public final double getVarianceY() {
        return (vary);
    }

    public final double getSumXY() {
        return (sumxy);
    }

    public final double getSumXX() {
        if (JSLMath.equal(getCount(), 0.0)) {
            return Double.NaN;
        }
        return getSSXX() + (getSumX() * getAverageX());
    }

    public final double getSumYY() {
        if (JSLMath.equal(getCount(), 0.0)) {
            return Double.NaN;
        }
        return getSSYY() + (getSumY() * getAverageY());
    }

    public final double getSSXX() {
        return varx * (getCount());
    }

    public final double getSSYY() {
        return vary * (getCount());
    }

    public final double getSSXY() {
        return covxy * (getCount());
    }

    public final double getSumSquareErrorsRegression() {
        double t = getSSXY();
        return (t * t) / getSSXX();
    }

    public final double getSST() {
        return getSSYY();
    }

    public final double getCoeffOfDetermination() {
        return getSumSquareErrorsRegression() / getSSYY();
    }

    public final double getSumSquareErrorResiduals() {
        return getSST() - getSumSquareErrorsRegression();
    }

    public final double getMSEResiduals() {
        double n = getCount() - 2.0;
        if (n <= 0.0) {
            return Double.NaN;
        }
        return getSumSquareErrorResiduals() / n;
    }

    public final double getSlope() {
        if (JSLMath.equal(getSSXX(), 0.0)) {
            return Double.NaN;
        }
        return getSSXY() / getSSXX();
    }

    public final double getSlopeStdError() {
        return Math.sqrt(getMSEResiduals() / getSSXX());
    }

    public final double getIntercept() {
        return getAverageY() - getSlope() * getAverageX();
    }

    public final double getInterceptStdError() {
        double n = getCount();
        double n2 = n - 2.0;
        if (n2 <= 0.0) {
            return Double.NaN;
        }
        double t1 = (1.0 / n) + ((avgx * avgx) / getSSXX());
        double t2 = getSumSquareErrorResiduals() / n2;
        return Math.sqrt(t1 * t2);
    }

    public final double getAdjustedRSq() {
        double n = getCount();
        double k = 1.0;
        double r2 = getCoeffOfDetermination();
        double d = n - 1.0 - k;
        if (d <= 0.0) {
            return Double.NaN;
        }
        return ((n - 1.0) * r2 - k) / d;
    }

    public final double getCovarianceXY() {
        if (nxy > 1.0) {
            return (covxy);
        } else {
            return (Double.NaN);
        }
    }

    public final double getCount() {
        return (nxy);
    }

    public final double getCorrelationXY() {
        if (nxy > 1.0) {
            return ((covxy) / (Math.sqrt(varx * vary))); // matches Pearson correlation
        } else {
            return (Double.NaN);
        }
    }

    public void reset() {
        avgx = 0.0;
        avgy = 0.0;
        varx = 0.0;
        vary = 0.0;
        sumxy = 0.0;
        covxy = 0.0;
        nxy = 0.0;
    }

    public final void collectXY(double x, double y) {
        double deltax, deltay, n1, r1;
        // compute for n+1
        n1 = nxy + 1.0;
        r1 = nxy / n1;
        deltax = (avgx - x) / n1;
        deltay = (avgy - y) / n1;
        sumxy = sumxy + x * y;
        covxy = r1 * covxy + nxy * deltax * deltay;
        avgx = avgx - deltax;
        avgy = avgy - deltay;
        varx = nxy * deltax * deltax + r1 * varx;
        vary = nxy * deltay * deltay + r1 * vary;
        nxy = nxy + 1.0;
    }

    public final double getRatioXY() {
        if (avgy == 0.0) {
            if (avgx > 0.0) {
                return (Double.POSITIVE_INFINITY);
            } else if (avgx < 0.0) {
                return (Double.NEGATIVE_INFINITY);
            } else {
                return (Double.NaN);
            }
        } else {
            return (avgx / avgy);
        }
    }

    public final double getRatioXYVariance() {
        double mu = getRatioXY();
        double vx = getVarianceX();
        double vy = getVarianceY();
        double cxy = getCovarianceXY();
        double var = vx - 2.0 * mu * cxy + mu * mu * vy;
        return (var);
    }

    public final double getRatioXYStdError() {
        if (nxy >= 1.0) {
            if (avgy != 0.0) {
                double sd = Math.sqrt(getRatioXYVariance());
                double se = sd / (avgy * nxy);
                return (se);
            } else {
                return (Double.NaN);
            }
        } else {
            return (Double.NaN);
        }
    }

    @Override
    public final String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("---------------\n");
        sb.append("X Y Statistics: \n");
        sb.append("---------------\n");
        sb.append("nxy: ");
        sb.append(nxy);
        sb.append("\n");
        sb.append("avg x: ");
        sb.append(avgx);
        sb.append("\n");
        sb.append("sum x: ");
        sb.append(getSumX());
        sb.append("\n");
        sb.append("avg y: ");
        sb.append(avgy);
        sb.append("\n");
        sb.append("sum y: ");
        sb.append(getSumY());
        sb.append("\n");
        sb.append("var x: ");
        sb.append(varx);
        sb.append("\n");
        sb.append("var y: ");
        sb.append(vary);
        sb.append("\n");
        sb.append("covxy: ");
        sb.append(covxy);
        sb.append("\n");
        sb.append("corrxy: ");
        sb.append(getCorrelationXY());
        sb.append("\n");
        sb.append("sum xy: ");
        sb.append(getSumXY());
        sb.append("\n");
        sb.append("SSXY: ");
        sb.append(getSSXY());
        sb.append("\n");
        sb.append("SSXX: ");
        sb.append(getSSXX());
        sb.append("\n");
        sb.append("sum xx: ");
        sb.append(getSumXX());
        sb.append("\n");
        sb.append("sum yy: ");
        sb.append(getSumYY());
        sb.append("\n");
        sb.append("intercept: ");
        sb.append(getIntercept());
        sb.append("\n");
        sb.append("slope: ");
        sb.append(getSlope());
        sb.append("\n");
        sb.append("Sum Squared Error Regression: ");
        sb.append(getSumSquareErrorsRegression());
        sb.append("\n");
        sb.append("Sum Squared Error Residuals: ");
        sb.append(getSumSquareErrorResiduals());
        sb.append("\n");
        sb.append("Sum Squared Error Total: ");
        sb.append(getSST());
        sb.append("\n");
        sb.append("coeff of determination: ");
        sb.append(getCoeffOfDetermination());
        sb.append("\n");

        sb.append("adjusted R Squared: ");
        sb.append(getAdjustedRSq());
        sb.append("\n");

        sb.append("MSE of Residuals: ");
        sb.append(getMSEResiduals());
        sb.append("\n");
        sb.append("Slope std error: ");
        sb.append(getSlopeStdError());
        sb.append("\n");
        sb.append("Intercept std error: ");
        sb.append(getInterceptStdError());
        sb.append("\n");
        sb.append("ratioxy: ");
        sb.append(getRatioXY());
        sb.append("\n");
        sb.append("var ratioxy: ");
        sb.append(getRatioXYVariance());
        sb.append("\n");
        sb.append("se ratioxy: ");
        sb.append(getRatioXYStdError());
        sb.append("\n");

        return (sb.toString());
    }

    public static void main(String args[]) {

        test2();
    }

    public static void test1() {
        double[] x = {0.0, 30.0, 10.0, 25.0, 12.0, 14.0, 5.0, 40.0, 42.0, 32.0, 16.0};
        double[] y = {0.0, 4.0, 2.0, 3.0, 2.0, 3.0, 1.0, 6.0, 7.0, 5.0, 3.0};

        StatisticXY stat = new StatisticXY();

        for (int i = 0; i < x.length; i++) {
            stat.collectXY(x[i], y[i]);
        }

        System.out.println(stat);
    }

    public static void test2() {
        double[] x = {99.0, 101.1, 102.7, 103.0, 105.4, 107.0, 108.7, 110.8,
            112.1, 112.4, 113.6, 113.8, 115.1, 115.4, 120.0};

        double[] y = {28.8, 27.9, 27.0, 25.2, 22.8, 21.5, 20.9, 19.6, 17.1,
            18.9, 16.0, 16.7, 13.0, 13.6, 10.8};

        StatisticXY stat = new StatisticXY();

        for (int i = 0; i < x.length; i++) {
            stat.collectXY(x[i], y[i]);
        }

        System.out.println(stat);
    }
}
