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

import jsl.utilities.IdentityIfc;
import jsl.utilities.Interval;
import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.SampleIfc;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rng.GetRandomNumberStreamIfc;
import jsl.utilities.random.rng.RNStreamControlIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rng.SetRandomNumberStreamIfc;
import jsl.utilities.random.robj.DPopulation;
import jsl.utilities.random.rvariable.EmpiricalRV;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.*;

/**
 * A class to do statistical bootstrapping.  The calculations occur via the method generateSamples().
 * Until generateSamples() is called the results are meaningless.
 *
 * It is possible to save the individual
 * bootstrap samples from which the bootstrap samples can be retrieved. Recognize that
 * this could be a lot of data.  The class implements three classic bootstrap confidence
 * intervals normal, basic, and percentile.  To estimate the quantiles it uses algorithm 8 from
 * Hyndman, R. J. and Fan, Y. (1996) Sample quantiles in statistical packages,
 * American Statistician 50, 361–365 as the default.  This can be changed by the user.
 */
public class Bootstrap implements IdentityIfc, RNStreamControlIfc, SetRandomNumberStreamIfc, GetRandomNumberStreamIfc {

    /**
     * A counter to count the number of created to assign "unique" ids
     */
    private static int myIdCounter_;

    /**
     * The id of this object
     */
    protected final int myId;
    protected final String myName;
    protected final DPopulation myOriginalPop;
    protected final Statistic myAcrossBSStat;
    protected final List<Statistic> myBSStatList;
    protected final Statistic myOriginalPopStat;
    protected final Percentile myPercentileCalc;
    protected final double[] myOrginalData;
    protected int myNumBSSamples;
    protected double myOrgEstimate;
    protected Percentile.EstimationType myQuantileType;
    protected double myDefaultLevel = 0.95;

    /**
     * Creates a Bootstrap instance with name Bootstrap:getId()
     *
     * @param originalData the original data
     */
    public Bootstrap(double[] originalData) {
        this(null, originalData);
    }

    /**
     * @param name         the name of bootstrap instance
     * @param originalData the original data
     */
    public Bootstrap(String name, double[] originalData) {
        if (originalData == null) {
            throw new IllegalArgumentException("The supplied bootstrap generate was null");
        }
        if (originalData.length <= 1) {
            throw new IllegalArgumentException("The supplied bootstrap generate had only 1 data point");
        }
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        if (name == null) {
            myName = "Bootstrap:" + getId();
        } else {
            myName = name;
        }
        myOrginalData = Arrays.copyOf(originalData, originalData.length);
        myOriginalPop = new DPopulation(myOrginalData);
        myOriginalPopStat = new Statistic("Original Pop Statistics", myOrginalData);

        myAcrossBSStat = new Statistic("Across Bootstrap Statistics");
        myAcrossBSStat.setSaveOption(true);
        myBSStatList = new ArrayList<>();
        myPercentileCalc = new Percentile();
        myQuantileType = Percentile.EstimationType.R_8;
    }

    /**
     * @param sampleSize the size of the original sample
     * @param sampler something to generate the original sample of the provided size
     * @return an instance of Bootstrap based on the sample
     */
    public static Bootstrap create(int sampleSize, SampleIfc sampler) {
        return create(null, sampleSize, sampler);
    }
    /**
     * @param name         the name of bootstrap instance
     * @param sampleSize the size of the original sample, must be greater than 1
     * @param sampler something to generate the original sample of the provided size
     * @return an instance of Bootstrap based on the sample
     */
    public static Bootstrap create(String name, int sampleSize, SampleIfc sampler) {
        Objects.requireNonNull(sampler, "The sampler was null");
        if (sampleSize <= 1) {
            throw new IllegalArgumentException("The generate size must be greater than 1");
        }
        return new Bootstrap(name, sampler.sample(sampleSize));
    }

    /**
     * The individual bootstrapped samples are not saved. The estimator is EstimatorIfc.Average()
     *
     * @param numBootstrapSamples the number of bootstrap samples to generate, must be greater than 1
     */
    public final void generateSamples(int numBootstrapSamples) {
        generateSamples(numBootstrapSamples, new EstimatorIfc.Average(), false);
    }

    /**
     * The estimator is EstimatorIfc.Average()
     *
     * @param numBootstrapSamples the number of bootstrap samples to generate
     * @param saveBootstrapSamples   indicates that the statistics and data of each bootstrap generate should be saved
     */
    public final void generateSamples(int numBootstrapSamples, boolean saveBootstrapSamples) {
        generateSamples(numBootstrapSamples, new EstimatorIfc.Average(), saveBootstrapSamples);
    }

    /**
     * The individual bootstrapped samples are not saved.
     *
     * @param numBootstrapSamples the number of bootstrap samples to generate
     * @param estimator           a function of the data
     */
    public final void generateSamples(int numBootstrapSamples, EstimatorIfc estimator) {
        generateSamples(numBootstrapSamples, estimator, false);
    }

    /** This method changes the underlying state of the Bootstrap instance by performing
     *  the bootstrap sampling.
     *
     * @param numBootstrapSamples the number of bootstrap samples to generate
     * @param estimator           a function of the data
     * @param saveBootstrapSamples   indicates that the statistics and data of each bootstrap generate should be saved
     */
    public void generateSamples(int numBootstrapSamples, EstimatorIfc estimator,
                                   boolean saveBootstrapSamples) {
        Objects.requireNonNull(estimator, "The estimator function was null");
        if (numBootstrapSamples <= 1) {
            throw new IllegalArgumentException("The number of boot strap samples must be greater than 1");
        }
        myNumBSSamples = numBootstrapSamples;
        myAcrossBSStat.reset();
        for(Statistic s: myBSStatList){
            s.reset();
        }
        myBSStatList.clear();
        myOrgEstimate = estimator.getEstimate(myOrginalData);
        for (int i = 0; i < numBootstrapSamples; i++) {
            double[] sample = myOriginalPop.sample(myOriginalPop.size());
            myAcrossBSStat.collect(estimator.getEstimate(sample));
            if (saveBootstrapSamples) {
                Statistic bs = new Statistic(getName() + ":bs:" + (i + 1));
                bs.setSaveOption(true);
                bs.collect(sample);
                myBSStatList.add(bs);
            }
        }
        myPercentileCalc.setData(myAcrossBSStat.getSavedData());
    }

    /**
     * @return the identity is unique to this execution/construction
     */
    public final int getId() {
        return (myId);
    }

    /**
     * @return the name of the bootstrap
     */
    public final String getName() {
        return myName;
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myOriginalPop.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myOriginalPop.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myOriginalPop.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myOriginalPop.setResetStartStreamOption(b);
    }

    /**
     * The resetStartStream method will position the RNG at the beginning of its
     * stream. This is the same location in the stream as assigned when the RNG
     * was created and initialized.
     */
    @Override
    public void resetStartStream() {
        myOriginalPop.resetStartStream();
    }

    /**
     * Resets the position of the RNG at the start of the current substream
     */
    @Override
    public void resetStartSubStream() {
        myOriginalPop.resetStartSubStream();
    }

    /**
     * Positions the RNG at the beginning of its next substream
     */
    @Override
    public void advanceToNextSubStream() {
        myOriginalPop.advanceToNextSubStream();
    }

    /**
     * Tells the stream to start producing antithetic variates
     *
     * @param flag true means that it produces antithetic variates.
     */
    @Override
    public void setAntitheticOption(boolean flag) {
        myOriginalPop.setAntitheticOption(flag);
    }

    /**
     * @return true means on
     */
    @Override
    public boolean getAntitheticOption() {
        return myOriginalPop.getAntitheticOption();
    }


    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        myOriginalPop.setRandomNumberStream(stream);
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myOriginalPop.getRandomNumberStream();
    }

    /**
     * @return the default confidence interval level
     */
    public final double getDefaultCILevel() {
        return myDefaultLevel;
    }

    /**
     * @param level the level to set must be (0,1)
     */
    public final void setDefaultCILevel(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        this.myDefaultLevel = level;
    }

    /**
     * @param type the type to set, must not be null.
     */
    public final void setDefaultQuantileEstimationType(Percentile.EstimationType type) {
        if (type == null) {
            throw new IllegalArgumentException("The supplied type was null");
        }
        myQuantileType = type;
    }

    /**
     * @return the number of requested bootstrap samples
     */
    public final int getNumBootstrapSamples() {
        return myNumBSSamples;
    }

    /**
     *
     * @return a copy of the original data
     */
    public final double[] getOriginalData() {
        return Arrays.copyOf(myOrginalData, myOrginalData.length);
    }

    /**
     * Each returned statistic has the data saved. The list itself is unmodifiable. The
     * underlying statistic objects can be modified, but have no effect on the bootstrap generate statistics.
     * The statistical values will be changed the next time generateSamples() is executed. Users are
     * advised to copy the statistics in the list (via Statistic newInstance())
     * before executing generateSamples if persistence is required.
     *
     * If the save bootstrap data option was not turned on during the sampling then the list returned is empty.
     *
     * @return a list of size getNumBootstrapSamples() holding statistics and data from
     * every bootstrap generate
     */
    public final List<Statistic> getStatisticForEachBootstrapSample() {
        return Collections.unmodifiableList(myBSStatList);
    }

    /**
     *
     * If the save bootstrap data option was not turned on during the sampling then the list returned is empty.
     *
     * @return a list of size getNumBootstrapSamples() holding a copy of the data from
     * every bootstrap generate
     */
    public final List<double[]> getDataForEachBootstrapSample(){
        List<double[]> list = new ArrayList<>();
        for(Statistic s: myBSStatList){
            list.add(s.getSavedData());
        }
        return list;
    }

    /** Creates a random variable to represent the data in each bootstrap generate for which
     *  the data was saved. By default, the stream for every random variable is the same across the
     *   bootstraps to facilitate common random number generation (CRN)
     *
     * @return a list of the random variables
     */
    public final List<RVariableIfc> getEmpiricalRVForEachBootstrapSample(){
        return getEmpiricalRVForEachBootstrapSample(true);
    }

    /** Creates a random variable to represent the data in each bootstrap generate for which
     *  the data was saved.
     *
     * @param useCRN, if true the stream for every random variable is the same across the
     *                bootstraps to facilitate common random number generation (CRN). If false
     *                different streams are used for each created random variable
     * @return a list of the random variables
     */
    public final List<RVariableIfc> getEmpiricalRVForEachBootstrapSample(boolean useCRN){
        List<RVariableIfc> list = new ArrayList<>();
        RNStreamIfc rnStream = null;
        if (useCRN){
            rnStream = JSLRandom.nextRNStream();
        }
        for(Statistic s: myBSStatList){
            double[] data = s.getSavedData();
            if ((data != null)){
                if (data.length != 0){
                    if (useCRN){
                        list.add(new EmpiricalRV(data, rnStream));
                    } else {
                        list.add(new EmpiricalRV(data));
                    }
                }
            }
        }
        return list;
    }

    /**
     *
     * @param b the bootstrap generate number, b = 1, 2, ... to getNumBootstrapSamples()
     * @return the generated values for the bth bootstrap, if no samples are saved then
     * the array returned is of zero length
     */
    public final double[] getDataForBootstrapSample(int b){
        if (myBSStatList.isEmpty()){
            return new double[0];
        }
        if ((b < 0) || (b >= myBSStatList.size())){
            throw new IllegalArgumentException("The supplied index was out of range");
        }
        return myBSStatList.get(b).getSavedData();
    }

    /** If the bootstrap samples were saved, this returns the
     * generated averages for each of the samples
     *
     * @return an array of the bootstrap generate averages, will be zero length if
     * no bootstrap samples were saved
     */
    public final double[] getBootstrapSampleAverages(){
        double[] avg = new double[myBSStatList.size()];
        int i = 0;
        for(Statistic stat: myBSStatList){
            avg[i] = stat.getAverage();
            i++;
        }
        return avg;
    }

    /** If the bootstrap samples were saved, this returns the
     * generated variance for each of the samples
     *
     * @return an array of the bootstrap generated variances, will be zero length if
     * no bootstrap samples were saved
     */
    public final double[] getBootstrapSampleVariances(){
        double[] var = new double[myBSStatList.size()];
        int i = 0;
        for(Statistic stat: myBSStatList){
            var[i] = stat.getVariance();
            i++;
        }
        return var;
    }

    /**
     * @return a Statistic observed over estimates from the bootstrap samples
     */
    public final Statistic getAcrossBootstrapStatistics() {
        return Statistic.newInstance(myAcrossBSStat);
    }

    /**
     * @return the generated average of the estimates from the bootstrap samples
     */
    public final double getAcrossBootstrapAverage() {
        return myAcrossBSStat.getAverage();
    }

    /**
     * @return the observations of the estimator for each bootstrap generate, may be zero length if
     * no samples have been generated
     */
    public final double[] getBootstrapEstimates() {
        double[] savedData = myAcrossBSStat.getSavedData();
        if (savedData == null){
            return new double[0];
        } else {
            return savedData;
        }
    }

    /**
     * Each element is the bootstrap estimate for sample i minus getOriginalDataEstimate()
     *
     * @return the array of bootstrap differences
     */
    public final double[] getBootstrapDifferences() {
        return JSLArrayUtil.subtractConstant(getBootstrapEstimates(), getOriginalDataEstimate());
    }

    /**
     * Each element is the bootstrap estimate for sample i minus getOriginalDataEstimate()
     * divided by getBootstrapStdErrEstimate()
     *
     * @return the array of bootstrap differences
     */
    public final double[] getStandardizedBootstrapDifferences() {
        return JSLArrayUtil.divideConstant(getBootstrapDifferences(), getBootstrapStdErrEstimate());
    }

    /**
     * @return the generate average for the original data
     */
    public final double getOriginalDataAverage() {
        return myOriginalPopStat.getAverage();
    }

    /**
     * @return the estimate from the supplied EstimatorIfc based on the original data
     */
    public final double getOriginalDataEstimate() {
        return myOrgEstimate;
    }

    /**
     * This is getAcrossBootstrapAverage() - getOriginalDataEstimate()
     *
     * @return an estimate the bias based on bootstrapping
     */
    public final double getBootstrapBiasEstimate() {
        return getAcrossBootstrapAverage() - getOriginalDataEstimate();
    }

    /**
     * This is the standard deviation of the across bootstrap observations of the estimator
     * for each bootstrap generate
     *
     * @return the standard error of the estimate based on bootstrapping
     */
    public final double getBootstrapStdErrEstimate() {
        return myAcrossBSStat.getStandardDeviation();
    }

    /**
     * @return summary statistics for the original data
     */
    public final Statistic getOriginalDataStatistics() {
        return Statistic.newInstance(myOriginalPopStat);
    }

    /**
     * Gets the standard normal based bootstrap confidence interval. Not recommended.
     *
     * @return the confidence interval
     */
    public final Interval getStdNormalBootstrapCI() {
        return getStdNormalBootstrapCI(getDefaultCILevel());
    }

    /**
     * Gets the standard normal based bootstrap confidence interval. Not recommended.
     *
     * @param level the confidence level, must be between 0 and 1
     * @return the confidence interval
     */
    public final Interval getStdNormalBootstrapCI(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        double alpha = 1.0 - level;
        double z = Normal.stdNormalInvCDF(1.0 - alpha / 2.0);
        double estimate = getOriginalDataEstimate();
        double se = getBootstrapStdErrEstimate();
        double ll = estimate - z * se;
        double ul = estimate + z * se;
        Interval ci = new Interval(ll, ul);
        return ci;
    }

    /**
     * The "basic" method, but with no bias correction. This
     * is the so called centered percentile method (2θ − Bu , 2θ − Bl )
     * where θ is the bootstrap estimator and Bu is the 1 - alpha/2 percentile
     * and Bl is the lower (alpha/2) percentile, where level = 1-alpha of
     * the bootstrap replicates.
     *
     * @return the confidence interval
     */
    public final Interval getBasicBootstrapCI() {
        return getBasicBootstrapCI(getDefaultCILevel());
    }

    /**
     * The "basic" method, but with no bias correction. This
     * is the so called centered percentile method (2θ − Bu , 2θ − Bl )
     * where θ is the bootstrap estimator and Bu is the 1 - alpha/2 percentile
     * and Bl is the lower (alpha/2) percentile, where level = 1-alpha of
     * the bootstrap replicates.
     *
     * @param level the confidence level, must be between 0 and 1
     * @return the confidence interval
     */
    public final Interval getBasicBootstrapCI(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        double a = 1.0 - level;
        double ad2 = a / 2.0;
        double llq = myPercentileCalc.evaluate(100.0 * ad2);
        double ulq = myPercentileCalc.evaluate(100.0 * (1.0 - ad2));
        double estimate = getOriginalDataEstimate();
        double ll = 2.0 * estimate - ulq;
        double ul = 2.0 * estimate - llq;
        Interval ci = new Interval(ll, ul);
        return ci;
    }

    /**
     * The "percentile" method, but with no bias correction. This
     * is the percentile method (θ − Bl , θ + Bu )
     * where θ is the bootstrap estimator and Bu is the 1 - alpha/2 percentile
     * and Bl is the lower (alpha/2) percentile, where level = 1-alpha of the
     * bootstrap replicates
     *
     * @return the confidence interval
     */
    public final Interval getPercentileBootstrapCI() {
        return getPercentileBootstrapCI(getDefaultCILevel());
    }

    /**
     * The "percentile" method, but with no bias correction. This
     * is the percentile method (Bl , Bu )
     * where Bu is the 1 - alpha/2 percentile
     * and Bl is the lower (alpha/2) percentile, where level = 1-alpha of the
     * bootstrap replicates
     *
     * @param level the confidence level, must be between 0 and 1
     * @return the confidence interval
     */
    public final Interval getPercentileBootstrapCI(double level) {
        if ((level <= 0.0) || (level >= 1.0)) {
            throw new IllegalArgumentException("Confidence Level must be (0,1)");
        }
        double a = 1.0 - level;
        double ad2 = a / 2.0;
        double llq = myPercentileCalc.evaluate(100.0 * ad2);
        double ulq = myPercentileCalc.evaluate(100.0 * (1.0 - ad2));
        Interval ci = new Interval(llq, ulq);
        return ci;
    }

    @Override
    public String toString() {
        return asString();
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("Bootstrap statistical results:");
        sb.append(System.lineSeparator());
        sb.append("id = ").append(getId());
        sb.append(System.lineSeparator());
        sb.append("name = ").append(getName());
        sb.append(System.lineSeparator());
        sb.append("------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("number of bootstrap samples = ").append(getNumBootstrapSamples());
        sb.append(System.lineSeparator());
        sb.append("size of original generate = ").append(myOriginalPopStat.getCount());
        sb.append(System.lineSeparator());
        sb.append("original estimate = ").append(getOriginalDataEstimate());
        sb.append(System.lineSeparator());
        sb.append("bias estimate = ").append(getBootstrapBiasEstimate());
        sb.append(System.lineSeparator());
        sb.append("across bootstrap average = ").append(getAcrossBootstrapAverage());
        sb.append(System.lineSeparator());
        sb.append("std. err. estimate = ").append(getBootstrapStdErrEstimate());
        sb.append(System.lineSeparator());
        sb.append("default c.i. level = ").append(getDefaultCILevel());
        sb.append(System.lineSeparator());
        sb.append("norm c.i. = ").append(getStdNormalBootstrapCI());
        sb.append(System.lineSeparator());
        sb.append("basic c.i. = ").append(getBasicBootstrapCI());
        sb.append(System.lineSeparator());
        sb.append("percentile c.i. = ").append(getPercentileBootstrapCI());
        sb.append(System.lineSeparator());
        sb.append("------------------------------------------------------");
        return sb.toString();
    }

}
