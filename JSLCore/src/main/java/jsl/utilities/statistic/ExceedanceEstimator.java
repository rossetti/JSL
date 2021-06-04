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

import java.util.Arrays;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.UniformRV;

/** Tabulates the proportion and frequency for a random variable X &gt; a(i)
 *  where a(i) are thresholds.
 *
 * @author rossetti
 */
public class ExceedanceEstimator extends AbstractCollector {

    /**
     * The thresholds for the exceedance estimates
     *
     */
    protected double[] myThresholds;

    /**
     * Counts the number of times threshold is exceeded
     *
     */
    protected double[] myCounts;

    /**
     * Holds the number of observations observed
     */
    protected double num = 0.0;

    public ExceedanceEstimator(double... thresholds) {
        this(null, thresholds);
    }

    public ExceedanceEstimator(String name, double[] thresholds) {
        super(name);
        setThresholds(thresholds);
        num = 0.0;
    }

    private void setThresholds(double[] thresholds) {
        if (thresholds == null) {
            throw new IllegalArgumentException("The threshold array was null");
        }
        myThresholds = new double[thresholds.length];
        myCounts = new double[thresholds.length];
        System.arraycopy(thresholds, 0, myThresholds, 0, thresholds.length);
        Arrays.sort(myThresholds);
    }

    @Override
    public void collect(double x) {
        if (getSaveOption()) {
            save(x);
        }
        num = num + 1.0;
        for (int i = 0; i < myThresholds.length; i++) {
            if (x > myThresholds[i]) {
                myCounts[i]++;
            }
        }
    }

    @Override
    public void reset() {
        num = 0.0;
        for (int i = 0; i < myCounts.length; i++) {
            myCounts[i] = 0.0;
        }
        clearSavedData();
    }

    public final double[] getFrequencies() {
        double[] f = new double[myCounts.length];
        System.arraycopy(myCounts, 0, f, 0, myCounts.length);
        return f;
    }

    public final double getFrequency(int i) {
        return myCounts[i];
    }

    public final double getProportion(int i) {
        if (num > 0) {
            return myCounts[i] / num;
        } else {
            return 0.0;
        }
    }

    public final double[] getProportions() {
        double[] f = getFrequencies();
        if (num == 0.0) {
            return f;
        }
        for (int i = 0; i < f.length; i++) {
            f[i] = (f[i] / num);
        }
        return f;
    }

    public final double[][] getValueFrequencies() {
        double[][] f = new double[myCounts.length][2];
        for (int i = 0; i < myCounts.length; i++) {
            f[i][0] = myThresholds[i];
            f[i][1] = myCounts[i];
        }
        return f;
    }

    public final double[][] getValueProportions() {
        double[][] f = new double[myCounts.length][2];
        for (int i = 0; i < myCounts.length; i++) {
            f[i][0] = myThresholds[i];
            if (num > 0) {
                f[i][1] = myCounts[i] / num;
            }
        }
        return f;
    }

    /**
     * Gets the count of the number of the observations.
     *
     * @return A double representing the count
     */
    public final double getNumberDataPointsSaved() {
        return (num);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Exceedance Tabulation ");
        sb.append(getName());
        sb.append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Number of thresholds = ");
        sb.append(myThresholds.length);
        sb.append("\n");
        sb.append("Count = ");
        sb.append(getNumberDataPointsSaved());
        sb.append("\n");
        sb.append("----------------------------------------\n");
        if (getNumberDataPointsSaved() > 0) {
            sb.append("Threshold \t Count \t p \t 1-p \n");
            for (int i = 0; i < myThresholds.length; i++) {
                double p = myCounts[i] / num;
                double cp = 1.0 - p;
                sb.append("{X > ");
                sb.append(myThresholds[i]);
                sb.append("}\t");
                sb.append(myCounts[i]);
                sb.append("\t");
                sb.append(p);
                sb.append("\t");
                sb.append(cp);
                sb.append("\n");
            }
            sb.append("----------------------------------------\n");
            sb.append("\n");
        }

        return (sb.toString());
    }

    public static void main(String[] args) {

        UniformRV du = new UniformRV(0, 100);

        double[] t = {0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0};
        ExceedanceEstimator f = new ExceedanceEstimator(t);

        f.collect(du.sample(10000));
        System.out.println("Testing");
        System.out.println(f);

        NormalRV n = new NormalRV();
        ExceedanceEstimator e = new ExceedanceEstimator(Normal.stdNormalInvCDF(0.95));
        for(int i=1;i<=1000;i++){

        }
        //e.collect(n.sample(10000));
        System.out.println(e);

    }
}
