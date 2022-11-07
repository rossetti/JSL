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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import jsl.utilities.random.SampleIfc;
import jsl.utilities.distributions.Exponential;
import jsl.utilities.distributions.Uniform;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import jsl.utilities.statistic.MultiBootstrap;

import java.util.*;

/**
 * Use the static create method to get a builder to builder the experiment with its addFactor
 * data.
 * <p>
 * Input uncertainty analyzer based on :
 * <p>
 * Song, E., and Nelson, B. L. (n.d.). Quickly assessing contributions to input uncertainty.
 * IIE Transactions. http://doi.org/10.1080/0740817X.2014.980869
 */
public class SNDiagnosticExperiment {

    private final MultiBootstrap myBootstrapper;
    private final SNReplicationRunnerIfc mySim;
    Table<Integer, String, RVariableIfc> mySimulationInputTable;
    private double[] mySimOutput;
    private double[][] myRegressors;
    private OLSMultipleLinearRegression myOLSModel;
    private final Map<String, FactorInputData> myFactorInputData;
    private final Map<String, FactorOutputData> myFactorOutputData;
    private int myNumBootstrapSamples;
    private int myNumReplications;

    /**
     * @param sim            a reference to something that can run the simulation
     * @param factorDataList a list holding the specification of the number of observations associated with each
     *                       input model, the name associated with the input model, the number of
     *                       samples, the sampler, and the central moments
     */
    private SNDiagnosticExperiment(SNReplicationRunnerIfc sim, List<FactorInputData> factorDataList) {
        if (factorDataList == null) {
            throw new IllegalArgumentException("The input model addFactor data list was null");
        }
        if (factorDataList.isEmpty()) {
            throw new IllegalArgumentException("The input model addFactor data list was empty");
        }
        if (sim == null) {
            throw new IllegalArgumentException("The simulation runner was null");
        }
        mySim = sim;
        myFactorInputData = new LinkedHashMap<>();
        for (FactorInputData data : factorDataList) {
            myFactorInputData.put(data.name, data);
        }
        // create the bootstrap controlling information
        Map<String, Map.Entry<Integer, SampleIfc>> samplerMap = new LinkedHashMap<>();
        for (FactorInputData data : factorDataList) {
            Map.Entry<Integer, SampleIfc> entry =
                    new AbstractMap.SimpleEntry<Integer, SampleIfc>(data.numSamples, data.sampler);
            samplerMap.put(data.name, entry);
        }
        // this line creates the bootstrap objects for each of the named inputs
        // and samples each one for m(l) observations based on the integer entry in the map pair
        myBootstrapper = MultiBootstrap.create(samplerMap);
        mySimulationInputTable = HashBasedTable.create();
        myFactorOutputData = new LinkedHashMap<>();
    }

    /**
     * @param sim a reference to something that can run the simulation
     * @return
     */
    public static final AddFactorDataStepIfc create(SNReplicationRunnerIfc sim) {
        return new Builder(sim);
    }

    /**
     * A builder for creating SNDiagnosticExperiment
     */
    public static class Builder implements BuildStepIfc {
        private List<FactorInputData> list;
        private SNReplicationRunnerIfc sim;

        public Builder(SNReplicationRunnerIfc sim) {
            if (sim == null) {
                throw new IllegalArgumentException("The simulation runner was null");
            }
            this.sim = sim;
            list = new ArrayList<>();
        }

        /**
         * @param name       the name of the addFactor, must not be null
         * @param numSamples the number of samples for the addFactor, m(l), must be GT 1
         * @param sampler    the sampler, must not be null
         * @param m2         the 2nd central moment, must be GT or equal to zero
         * @param m3         the 3rd central moment
         * @param m4         the 2nd central moment, must be GT or equal to zero
         * @return
         */
        @Override
        public BuildStepIfc addFactor(String name, int numSamples, SampleIfc sampler,
                                      double m2, double m3, double m4) {
            list.add(new FactorInputData(name, numSamples, sampler, m2, m3, m4));
            return this;
        }

        /**
         * @return the built SNDiagnosticExperiment
         */
        @Override
        public SNDiagnosticExperiment build() {
            return new SNDiagnosticExperiment(sim, list);
        }
    }

    /**
     * Adding addFactor data step
     */
    public interface AddFactorDataStepIfc {
        BuildStepIfc addFactor(String name, int numSamples, SampleIfc sampler,
                               double m2, double m3, double m4);
    }

    /**
     * The ability to builder or add addFactor data
     */
    public interface BuildStepIfc extends AddFactorDataStepIfc {
        SNDiagnosticExperiment build();
    }

    /** Uses common random numbers across simulation runs
     *
     * @param numBootstrapSamples the number of bootstrap samples to make for each input distribution
     * @param numReplications     the number of replications in the diagnostic experiment
     */
    public void runDiagnosticExperiment(int numBootstrapSamples, int numReplications) {
        runDiagnosticExperiment(numBootstrapSamples, numReplications, true);
    }

    /**
     *  @param useCRN, if true the stream for every random variable is the same across the
     *                     bootstraps to facilitate common random number generation (CRN). If false
     *                   different streams are used for each created random variable. Each
     *                 factor gets its own unique stream regardless.
     * @param numBootstrapSamples the number of bootstrap samples to make for each input distribution
     * @param numReplications     the number of replications in the diagnostic experiment
     */
    public void runDiagnosticExperiment(int numBootstrapSamples, int numReplications, boolean useCRN) {
        if (numBootstrapSamples <= 1) {
            throw new IllegalArgumentException("The number of boot strap samples must be greater than 1");
        }
        if (numReplications <= 1) {
            throw new IllegalArgumentException("The number of replications must be greater than 1");
        }

        this.myNumBootstrapSamples = numBootstrapSamples;
        this.myNumReplications = numReplications;
        int numFactors = myBootstrapper.getNumberFactors();
        if (numBootstrapSamples < 2 * numFactors + 2) {
            throw new IllegalArgumentException("The number of boot strap samples was less than 2*numFactors+2");
        }
        // after this call, each distribution will have b=myNumBootstrapSamples saved
        myBootstrapper.generateSamples(numBootstrapSamples, true);
        // fill the data for running the simulation replications
        fillSimulationInputTable(numBootstrapSamples, useCRN);
        // defined to hold simulation results
        mySimOutput = new double[numBootstrapSamples];
        for (int i = 0; i < numBootstrapSamples; i++) {
            Map<String, RVariableIfc> row = mySimulationInputTable.row(i);
            // returns the generated mean for the replications
            mySimOutput[i] = mySim.runReplications(numReplications, row);
        }

        // builder data set for regression
        buildDataForRegression(numBootstrapSamples);
        // fit the regression model, builder output data
        fitRegressionModel();
    }

    /**
     * @return the number of bootstrap samples specified in the experiment
     */
    public final int getNumBootstrapSamples() {
        return myNumBootstrapSamples;
    }

    /**
     * @return the number of replications specified in the experiment
     */
    public final int getNumReplications() {
        return myNumReplications;
    }

    /**
     * @return the total input uncertainty across factors
     */
    public final double getTotalInputUncertainty() {
        double sum = 0.0;
        for (FactorOutputData data : myFactorOutputData.values()) {
            sum = sum + data.getUnCertaintyContribution();
        }
        return sum;
    }

    /**
     * The relative significance of input uncertainty to
     * simulation estimator variability.  This is gamma of step 4, page 899 of Song and Nelson
     *
     * @param stdError the standard error of the simulation estimator
     * @return the relative significance estimate
     */
    public final double getRelativeSignificanceRatio(double stdError) {
        if (stdError <= 0) {
            throw new IllegalArgumentException("The supplied variance was < 0");
        }
        double s = Math.sqrt(getTotalInputUncertainty());
        return s / stdError;
    }

    /**
     * @param factor the name of the addFactor
     * @return the input uncertainty for the named addFactor
     */
    public final double getInputUncertaintyContribution(String factor) {
        return myFactorOutputData.get(factor).getUnCertaintyContribution();
    }

    /**
     * @param factor the name of the addFactor
     * @return the relative input uncertainty for the named addFactor
     */
    public final double getRelativeInputUncertaintyContribution(String factor) {
        double total = getInputUncertaintyContribution(factor);
        return myFactorOutputData.get(factor).getUnCertaintyContribution() / total;
    }

    /**
     * @return an unmodifiable map of the addFactor input data
     */
    public Map<String, FactorInputData> getFactorInputData() {
        return Collections.unmodifiableMap(myFactorInputData);
    }

    /**
     * There will not be any addFactor output data until the diagnostic experiment is run
     *
     * @return an unmodifiable map of the addFactor output data
     */
    public Map<String, FactorOutputData> getFactorOutputData() {
        return Collections.unmodifiableMap(myFactorOutputData);
    }

    /**
     * @return the fitted regression, it will be null if not fitted yet
     */
    public Optional<OLSMultipleLinearRegression> getFittedRegression() {
        return Optional.ofNullable(myOLSModel);
    }

    private void fillSimulationInputTable(int numBootstrapSamples, boolean useCRN) {
        Map<String, List<RVariableIfc>> rvInputs = myBootstrapper.getBootstrapRandomVariables(useCRN);
        for (int i = 0; i < numBootstrapSamples; i++) {
            for (String name : rvInputs.keySet()) {
                RVariableIfc rv = rvInputs.get(name).get(i);
                mySimulationInputTable.put(i, name, rv);
            }
        }
    }

    private void buildDataForRegression(int numBootstrapSamples) {

        Map<String, double[]> sampleAverages = myBootstrapper.getBootstrapSampleAverages();
        Map<String, double[]> sampleVariances = myBootstrapper.getBootstrapSampleVariances();
        int numFactors = myBootstrapper.getNumberFactors();
        myRegressors = new double[numBootstrapSamples][2 * numFactors];
        int j = 0;
        for (String name : sampleAverages.keySet()) {
            double[] avg = sampleAverages.get(name);
            for (int i = 0; i < avg.length; i++) {
                myRegressors[i][j] = avg[i];
            }
            j++;
        }
        for (String name : sampleVariances.keySet()) {
            double[] var = sampleVariances.get(name);
            for (int i = 0; i < var.length; i++) {
                myRegressors[i][j] = var[i];
            }
            j++;
        }

    }

    private void fitRegressionModel() {
        myOLSModel = new OLSMultipleLinearRegression();
        myOLSModel.newSampleData(mySimOutput, myRegressors);
        double[] beta = myOLSModel.estimateRegressionParameters();
        int j = 1;
        for (String name : myFactorInputData.keySet()) {
            FactorOutputData fd = new FactorOutputData();
            fd.name = name;
            fd.numSamples = myFactorInputData.get(name).numSamples;
            fd.m2 = myFactorInputData.get(name).secondCentralMoment;
            fd.m3 = myFactorInputData.get(name).thirdCentralMoment;
            fd.m4 = myFactorInputData.get(name).fourthCentralMoment;
            fd.meanSlope = beta[j];
            fd.varSlope = beta[2 * j];
            j++;
            myFactorOutputData.put(name, fd);
        }
    }


    /**
     * A Factor Input Data class to hold the data needed by the SNDiagnosticExperiment
     */
    public static class FactorInputData {

        /**
         * The name of the addFactor
         */
        private final String name;

        /**
         * The number of samples to sample from the sampler.
         * The number of samples is m(l) in Song and Nelson's notation.
         */
        private final int numSamples;

        /**
         * The sampler to use
         */
        private final SampleIfc sampler;

        /**
         * The 2nd central moment
         */
        private final double secondCentralMoment;
        /**
         * The 3rd central moment
         */
        private final double thirdCentralMoment;

        /**
         * The 4th central moment
         */
        private final double fourthCentralMoment;

        public FactorInputData(String name, int numSamples, SampleIfc sampler,
                               double secondCentralMoment, double thirdCentralMoment,
                               double fourthCentralMoment) {
            if (name == null) {
                throw new IllegalArgumentException("The input model addFactor name was null");
            }
            if (numSamples <= 1) {
                throw new IllegalArgumentException("The number of samples must be greater than 1");
            }
            if (sampler == null) {
                throw new IllegalArgumentException("The input model addFactor name was null");
            }
            if (secondCentralMoment < 0) {
                throw new IllegalArgumentException("The 2nd central moment must be >= 0");
            }
            if (fourthCentralMoment < 0) {
                throw new IllegalArgumentException("The 4th central moment must be >= 0");
            }
            this.name = name;
            this.numSamples = numSamples;
            this.sampler = sampler;
            this.secondCentralMoment = secondCentralMoment;
            this.thirdCentralMoment = thirdCentralMoment;
            this.fourthCentralMoment = fourthCentralMoment;
        }

        public final String getName() {
            return name;
        }

        public final int getNumSamples() {
            return numSamples;
        }

        public final SampleIfc getSampler() {
            return sampler;
        }

        public final double getSecondCentralMoment() {
            return secondCentralMoment;
        }

        public final double getThirdCentralMoment() {
            return thirdCentralMoment;
        }

        public final double getFourthCentralMoment() {
            return fourthCentralMoment;
        }

        @Override
        public String toString() {
            return "FactorData{" +
                    "name='" + name + '\'' +
                    ", numSamples=" + numSamples +
                    ", sampler=" + sampler +
                    ", m2=" + secondCentralMoment +
                    ", m3=" + thirdCentralMoment +
                    ", m4=" + fourthCentralMoment +
                    '}';
        }
    }


    /**
     * A addFactor output data class to hold data produced for each addFactor by the SNDiagnosticExperiment
     */
    public static class FactorOutputData {
        private String name;
        private int numSamples;
        private double m2;
        private double m3;
        private double m4;
        private double meanSlope;
        private double varSlope;

        private FactorOutputData() {

        }

        public final String getName() {
            return name;
        }

        /**
         * @return the number of samples m(l) taken in the original input model
         */
        public final int getNumSamples() {
            return numSamples;
        }

        /**
         * @return the 2nd central moment of the input model
         */
        public final double getSecondCentralMoment() {
            return m2;
        }

        /**
         * @return the 3rd central moment of the input model
         */
        public final double getThirdCentralMoment() {
            return m3;
        }

        /**
         * @return the fourth central moment of the input model
         */
        public final double getFourthCentralMoment() {
            return m4;
        }

        /**
         * @return the regression estimate of the slope for the mean
         */
        public final double getMeanSlope() {
            return meanSlope;
        }

        /**
         * @return the regression estimate of the slope for the variance
         */
        public final double getVarSlope() {
            return varSlope;
        }

        /**
         * The estimated variance of the mean of the addFactor distribution
         * 2nd column page 898 Song and Nelson
         *
         * @return The variance of the mean of the addFactor distribution
         */
        public final double getVarMeanFactor() {
            // 2nd column page 898 Song and Nelson
            return m2 * m2 / numSamples;
        }

        /**
         * The estimated variance of the variance of the addFactor distribution
         * 2nd column page 898 Song and Nelson, without approximation
         *
         * @return The variance of the variance of the addFactor distribution
         */
        public final double getVarVarianceFactor() {
            // 2nd column page 898 Song and Nelson, without approximation
            double n = numSamples;
            double w1 = (n - 1) * (n - 1) / (n * n * n);
            double w2 = (n - 3) * (n - 1) / (n * n * n);
            return w1 * m4 - w2 * m2 * m2;
        }

        /**
         * The estimated covariance of the mean, variance of the addFactor distribution
         * 2nd column page 898 Song and Nelson, without approximation
         *
         * @return The covariance of the mean, variance of the addFactor distribution
         */
        public final double getCovMeanVarFactor() {
            // 2nd column page 898 Song and Nelson, without approximation
            double n = numSamples;
            double w1 = (n - 1) * (n - 1) / (n * n * n);
            return w1 * m3;
        }

        /**
         * The estimated uncertainty contribution of the addFactor distribution
         * 2nd column, page 896 Song and Nelson, equation 7
         *
         * @return The estimated uncertainty contribution of the addFactor distribution
         */
        public final double getUnCertaintyContribution() {
            // 2nd column, page 896 Song and Nelson, equation 7
            return (meanSlope * meanSlope) * getVarMeanFactor() + (varSlope * varSlope) * getVarVarianceFactor() +
                    2.0 * meanSlope * varSlope * getCovMeanVarFactor();
        }

        /**
         * The estimate of the sample size uncertainty of the addFactor distribution
         * Equation 13 page 899 of Song and Nelson
         *
         * @return The estimate of the sample size uncertainty of the addFactor distribution
         */
        public final double getSampleSizeSensitivity() {
            return -1.0 * getUnCertaintyContribution() / numSamples;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("FactorOutputData{");
            sb.append("name='").append(name).append('\'');
            sb.append(", m(l) = ").append(numSamples);
            sb.append(", V(m(l)) = ").append(getUnCertaintyContribution());
            sb.append(", sample size sensitivity = ").append(getSampleSizeSensitivity());
            sb.append(", mean slope = ").append(meanSlope);
            sb.append(", var slope = ").append(varSlope);
            sb.append(", var(mean) = ").append(getVarMeanFactor());
            sb.append(", var(var) = ").append(getVarVarianceFactor());
            sb.append(", cov(mean, var) = ").append(getCovMeanVarFactor());
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        return asString();
    }

    public String asString() {
        final StringBuilder sb = new StringBuilder("SNDiagnosticExperiment{");
        sb.append("number of Bootstrap Samples = ").append(myNumBootstrapSamples);
        sb.append(", number of Replications = ").append(myNumReplications);
        sb.append('}');
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Input Factor Specification");
        sb.append(System.lineSeparator());
        sb.append("---------------------------------------------------------------------");
        sb.append(System.lineSeparator());
        for (FactorInputData data : myFactorInputData.values()) {
            sb.append(data);
            sb.append(System.lineSeparator());
        }
        sb.append("---------------------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Output Factor Results");
        sb.append(System.lineSeparator());
        sb.append("---------------------------------------------------------------------");
        sb.append(System.lineSeparator());
        for (FactorOutputData data : myFactorOutputData.values()) {
            sb.append(data);
            sb.append(System.lineSeparator());
        }
        sb.append("---------------------------------------------------------------------");
        sb.append(System.lineSeparator());
        double total = getTotalInputUncertainty();
        sb.append("Total Input Uncertainty = ").append(total);
        sb.append(System.lineSeparator());
        sb.append("---------------------------------------------------------------------");
        sb.append(System.lineSeparator());
        for (FactorOutputData data : myFactorOutputData.values()) {
            sb.append("Factor = ").append(data.name);
            sb.append("\t V(m(l) = ").append(data.getUnCertaintyContribution());
            sb.append("\t Percent V(m(l) = ").append(data.getUnCertaintyContribution()*100.0/total);
            sb.append(System.lineSeparator());
        }
        sb.append("---------------------------------------------------------------------");
        sb.append(System.lineSeparator());
        return sb.toString();
    }

    public static void main(String[] args) {

       test1();
 //       test2();
//        test3();
//        test4();
    }

    public static void test1() {
        Uniform u1 = new Uniform(1, 10);

        RVariableIfc r1 = u1.getRandomVariable();

        SNDiagnosticExperiment sn = SNDiagnosticExperiment.create(new SimRunner1())
                .addFactor("A", 100, r1, u1.getVariance(), u1.getMoment3(), u1.getMoment4())
                .build();

        sn.runDiagnosticExperiment(500, 100);

        System.out.println(sn);
    }

    public static void test2() {
        Uniform u1 = new Uniform(1, 10);
        Uniform u2 = new Uniform(5, 10);

        RVariableIfc r1 = u1.getRandomVariable();
        RVariableIfc r2 = u2.getRandomVariable();

        SNDiagnosticExperiment sn = SNDiagnosticExperiment.create(new SimRunner2())
                .addFactor("A", 100, r1, u1.getVariance(), u1.getMoment3(), u1.getMoment4())
                .addFactor("B", 100, r2, u2.getVariance(), u2.getMoment3(), u2.getMoment4())
                .build();

        sn.runDiagnosticExperiment(500, 100);

        System.out.println(sn);
    }

    public static void test3() {
        Uniform u1 = new Uniform(1, 10);
        Uniform u2 = new Uniform(5, 10);
        Uniform u3 = new Uniform(5, 10);

        RVariableIfc r1 = u1.getRandomVariable();
        RVariableIfc r2 = u2.getRandomVariable();
        RVariableIfc r3 = u3.getRandomVariable();

        SNDiagnosticExperiment sn = SNDiagnosticExperiment.create(new SimRunner3())
                .addFactor("A", 100, r1, u1.getVariance(), u1.getMoment3(), u1.getMoment4())
                .addFactor("B", 100, r2, u2.getVariance(), u2.getMoment3(), u2.getMoment4())
                .addFactor("C", 100, r3, u3.getVariance(), u3.getMoment3(), u3.getMoment4())
                .build();

        sn.runDiagnosticExperiment(500, 100);

        System.out.println(sn);
    }

    public static void test4(){
        Exponential u1 = new Exponential(1);
        Exponential u2 = new Exponential(1);
        Exponential u3 = new Exponential(1);
        Exponential u4 = new Exponential(1);
        Exponential u5 = new Exponential(1);

        RVariableIfc r1 = u1.getRandomVariable();
        RVariableIfc r2 = u2.getRandomVariable();
        RVariableIfc r3 = u3.getRandomVariable();
        RVariableIfc r4 = u4.getRandomVariable();
        RVariableIfc r5 = u5.getRandomVariable();

        SNDiagnosticExperiment sn = SNDiagnosticExperiment.create(new StochasticActivityNetwork())
                .addFactor("X1", 100, r1, u1.getVariance(), u1.getMoment3(), u1.getMoment4())
                .addFactor("X2", 100, r2, u2.getVariance(), u2.getMoment3(), u2.getMoment4())
                .addFactor("X3", 100, r3, u3.getVariance(), u3.getMoment3(), u3.getMoment4())
                .addFactor("X4", 100, r4, u4.getVariance(), u4.getMoment3(), u4.getMoment4())
                .addFactor("X5", 100, r5, u5.getVariance(), u5.getMoment3(), u5.getMoment4())
                .build();

        sn.runDiagnosticExperiment(50, 200);

        System.out.println(sn);
    }

    public static class SimRunner1 implements SNReplicationRunnerIfc {

        Statistic ybar = new Statistic();
        RVariableIfc x1;

        public void setInputs(Map<String, RVariableIfc> inputs) {
            x1 = inputs.get("A");
            x1.resetStartStream();
        }

        @Override
        public double runReplications(int numReplications, Map<String, RVariableIfc> inputs) {
            setInputs(inputs);
            ybar.reset();
            double y = 0;
            for (int i = 1; i <= numReplications; i++) {
                y = 1.0 * x1.getValue();
                ybar.collect(y);
            }
            return ybar.getAverage();
        }
    }

    public static class SimRunner2 implements SNReplicationRunnerIfc {
        Statistic ybar = new Statistic();
        RVariableIfc x1;
        RVariableIfc x2;

        public void setInputs(Map<String, RVariableIfc> inputs) {
            x1 = inputs.get("A");
            x2 = inputs.get("B");
            x1.resetStartStream();
            x2.resetStartStream();
        }

        @Override
        public double runReplications(int numReplications, Map<String, RVariableIfc> inputs) {
            setInputs(inputs);
            ybar.reset();
            double y = 0;
            for (int i = 1; i <= numReplications; i++) {
                y = 1.0 * x1.getValue() + 1.0 * x2.getValue();
                ybar.collect(y);
            }
            return ybar.getAverage();
        }
    }

    public static class SimRunner3 implements SNReplicationRunnerIfc {
        Statistic ybar = new Statistic();
        RVariableIfc x1;
        RVariableIfc x2;
        RVariableIfc x3;

        public void setInputs(Map<String, RVariableIfc> inputs) {
            x1 = inputs.get("A");
            x2 = inputs.get("B");
            x3 = inputs.get("C");
            x1.resetStartStream();
            x2.resetStartStream();
            x3.resetStartStream();
        }

        @Override
        public double runReplications(int numReplications, Map<String, RVariableIfc> inputs) {
            setInputs(inputs);
            ybar.reset();
            double y = 0;
            for (int i = 1; i <= numReplications; i++) {
                y = 1.0 * x1.getValue() + 1.0 * x2.getValue() + 1.0*x3.getValue();
                ybar.collect(y);
            }
            return ybar.getAverage();
        }
    }

    public static class StochasticActivityNetwork implements  SNReplicationRunnerIfc{
        Statistic ybar = new Statistic();
        RVariableIfc rv1;
        RVariableIfc rv2;
        RVariableIfc rv3;
        RVariableIfc rv4;
        RVariableIfc rv5;

        public void setInputs(Map<String, RVariableIfc> inputs) {
            rv1 = inputs.get("X1");
            rv2 = inputs.get("X2");
            rv3 = inputs.get("X3");
            rv4 = inputs.get("X4");
            rv5 = inputs.get("X5");
            rv1.resetStartStream();
            rv2.resetStartStream();
            rv3.resetStartStream();
            rv4.resetStartStream();
            rv5.resetStartStream();

        }

        @Override
        public double runReplications(int numReplications, Map<String, RVariableIfc> inputs) {
            setInputs(inputs);
            ybar.reset();
            for (int i = 1; i <= numReplications; i++) {
                double x1 = rv1.getValue();
                double x2 = rv2.getValue();
                double x3 = rv3.getValue();
                double x4 = rv4.getValue();
                double x5 = rv5.getValue();
                double p1 = x1 + x4;
                double p2 = x1 + x3 + x5;
                double p3 = x2 + x5;
                double y = Math.max(p1, Math.max(p2, p3));
                ybar.collect(y);
            }
            return ybar.getAverage();
        }
    }
}
