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

import jsl.utilities.Interval;
import jsl.utilities.distributions.StudentT;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.statistic.Statistic;

import java.util.Arrays;

public class JackKnifeEstimator {

    protected EstimatorIfc myEstimator;
    protected Statistic myOriginalPopStat;
    protected double[] myOrginalData;
    protected double myOrgEstimate;
    protected Statistic myJNStatistics;
    protected double myJNEofSE;
    protected double myDefaultLevel = 0.95;

    public JackKnifeEstimator(double[] originalData, EstimatorIfc estimator){
        if (estimator == null){
            throw new IllegalArgumentException("The estimator function was null");
        }
        if (originalData == null){
            throw new IllegalArgumentException("The supplied data was null");
        }
        if (originalData.length <=1){
            throw new IllegalArgumentException("The supplied generate had only 1 data point");
        }

        myOriginalPopStat = new Statistic("Original Pop Statistics", originalData);
        myJNStatistics = new Statistic("Jackknife Statistic");
        myJNStatistics.setSaveOption(true);
        myEstimator = estimator;
        myOrginalData = Arrays.copyOf(originalData, originalData.length);
        myOrgEstimate = myEstimator.getEstimate(originalData);
        computeJackknife();
    }

    protected void computeJackknife(){
        double[] loos = new double[myOrginalData.length-1];
        double[] jks = new double[myOrginalData.length];
        for (int i=0; i< myOrginalData.length; i++){
            // get the leave out generate missing i
            JSLArrayUtil.copyWithout(i, myOrginalData, loos);
            // compute the estimator based on the leave out generate
            jks[i] = myEstimator.getEstimate(loos);
            // observe each estimate for jackknife stats
            myJNStatistics.collect(jks[i]);
        }
        // now compute the std err of the jackknife
        double jne = getJackKnifeEstimate();
        double n = myJNStatistics.getCount();
        Statistic s = new Statistic();
        for (int i=0; i< myOrginalData.length; i++){
            double tmp = (jks[i] - jne)*(jks[i] - jne);
            s.collect(tmp);
        }
        myJNEofSE = Math.sqrt((n - 1.0)*s.getAverage());
    }

    /**
     *
     * @return the default confidence interval level
     */
    public final double getDefaultCILevel() {
        return myDefaultLevel;
    }

    /**
     *
     * @param level the level to set must be (0,1)
     */
    public final void setDefaultCILevel(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        this.myDefaultLevel = level;
    }

    /**  nxoe - (n-1)xjne[i], where n is the number of observations, oe= original estimate
     *  and jne[i] is the ith leave one out estimate
     *
     * @return an array containing the jackknife pseudo-values
     */
    public final double[] getPseudoValues(){
        double[] a = new double[myOrginalData.length];
        double n = myOrginalData.length;
        double ntheta = n*getOriginalDataEstimate();
        double[] thetai = myJNStatistics.getSavedData();
        for(int i=0;i<a.length; i++){
            a[i] = ntheta - (n - 1.0)* thetai[i];
        }
        return a;
    }

    /**
     *
     * @return a copy of the original data
     */
    public final double[] getOriginalData() {
        return Arrays.copyOf(myOrginalData, myOrginalData.length);
    }

    /**
     *
     * @return the number of observations in the original generate
     */
    public final double getSampleSize(){
        return myOriginalPopStat.getCount();
    }

    /**
     *
     * @return the generate average for the original data
     */
    public final double getOriginalDataAverage(){
        return myOriginalPopStat.getAverage();
    }

    /**
     *
     * @return the estimate from the supplied EstimatorIfc based on the original data
     */
    public final double getOriginalDataEstimate(){
        return myOrgEstimate;
    }

    /**
     *
     * @return summary statistics for the original data
     */
    public final Statistic getOriginalDataStatistics(){
        return Statistic.newInstance(myOriginalPopStat);
    }

    /**
     *
     * @return the average of the leave one out samples
     */
    public final double getJackKnifeEstimate(){
        return myJNStatistics.getAverage();
    }

    /**
     *
     * @return the jackknife estimate of the standard error
     */
    public final double getJackKnifeEstimateOfSE(){
        return myJNEofSE;
    }

    /** The c.i. is based on the Student-t distribution and the
     *  jackknife estimate and its estimate of the standard error
     *
     * @return the interval
     */
    public final Interval getJackKnifeConfidenceInterval(){
        return getJackKnifeConfidenceInterval(getDefaultCILevel());
    }

    /** The c.i. is based on the Student-t distribution and the
     *  jackknife estimate and its estimate of the standard error
     *
     * @param level the confidence level, must be in (0,1)
     * @return the interval
     */
    public final Interval getJackKnifeConfidenceInterval(double level){
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        double dof = getSampleSize() - 1.0;
        double alpha = 1.0 - level;
        double p = 1.0 - alpha / 2.0;
        double t = StudentT.getInvCDF(dof, p);
        double jne = getJackKnifeEstimate();
        double se = getJackKnifeEstimateOfSE();
        double ll = jne - t*se;
        double ul = jne + t*se;
        Interval ci = new Interval(ll, ul);
        return ci;
    }

    /** The estimate is (n-1)x(jne - oe), where n = the number of observations, jne is the jackknife estimate
     * and oe = the original data estimate
     *
     * @return the estimate of the bias based on jackknifing
     */
    public final double getJackKnifeBiasEstimate(){
        double n = myOriginalPopStat.getCount();
        double jne = getJackKnifeEstimate();
        double oe = getOriginalDataEstimate();
        return (n - 1.0)*(jne - oe);
    }

    /**
     *
     * @return the bias corrected jackknife estimate, getOriginalDataEstimate() - getJackKnifeBiasEstimate()
     */
    public final double getBiasCorrectedJackknifeEstimate(){
        return getOriginalDataEstimate() - getJackKnifeBiasEstimate();
    }

    @Override
    public String toString(){
        return asString();
    }

    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("Jackknife statistical results:");
        sb.append(System.lineSeparator());
        sb.append("------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("size of original = ").append(myOriginalPopStat.getCount());
        sb.append(System.lineSeparator());
        sb.append("original estimate = ").append(getOriginalDataEstimate());
        sb.append(System.lineSeparator());
        sb.append("jackknife estimate = ").append(getJackKnifeEstimate());
        sb.append(System.lineSeparator());
        sb.append("jackknife bias estimate = ").append(getJackKnifeBiasEstimate());
        sb.append(System.lineSeparator());
        sb.append("bias corrected jackknife estimate = ").append(getBiasCorrectedJackknifeEstimate());
        sb.append(System.lineSeparator());
        sb.append("std. err. of jackknife estimate = ").append(getJackKnifeEstimateOfSE());
        sb.append(System.lineSeparator());
        sb.append("default c.i. level = ").append(getDefaultCILevel());
        sb.append(System.lineSeparator());
        sb.append("jackknife c.i. = ").append(getJackKnifeConfidenceInterval());
        sb.append(System.lineSeparator());
        sb.append("------------------------------------------------------");
        return sb.toString();
    }

}
