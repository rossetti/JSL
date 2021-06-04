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

import jsl.utilities.random.SampleIfc;
import jsl.utilities.random.rng.RNStreamControlIfc;
import jsl.utilities.random.rvariable.RVariableIfc;
//import tech.tablesaw.api.DoubleColumn;
//import tech.tablesaw.api.Table;

import java.util.*;

/**
 * A collection of Bootstrap instances to permit multi-dimensional bootstrapping.
 * Construction depends on a named mapping of double[] arrays that represent the
 * original samples.  A static create method also allows creation based on a mapping
 * to implementations of the SampleIfc.
 *
 * The name provided for each dataset (or sampler) should be unique and will be used
 * to identify the associated bootstrap results. We call this name a addFactor.
 */
public class MultiBootstrap implements RNStreamControlIfc {

    /**
     * A counter to count the number of created to assign "unique" ids
     */
    private static long myIdCounter_;

    /**
     * The id of this object
     */
    protected final long myId;
    protected final String myName;
    protected Map<String, Bootstrap> myBootstraps;

    /**
     * @param dataMap a map holding the name for each data set
     */
    public MultiBootstrap(Map<String, double[]> dataMap) {
        this(null, dataMap);
    }

    /**
     * @param name    the name of the instance
     * @param dataMap a map holding the name for each data set, names cannot be null and the arrays cannot be null
     */
    public MultiBootstrap(String name, Map<String, double[]> dataMap) {
        if (dataMap == null) {
            throw new IllegalArgumentException("The supplied data map was null");
        }
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        if (name == null) {
            myName = "MultiBootstrap:" + getId();
        } else {
            myName = name;
        }
        myBootstraps = new LinkedHashMap<>();
        for (Map.Entry<String, double[]> entry : dataMap.entrySet()) {
            String dname = entry.getKey();
            if (dname == null) {
                throw new IllegalArgumentException("The name associated with a data set was null");
            }
            double[] value = entry.getValue();
            Bootstrap bs = new Bootstrap(dname, value);
            myBootstraps.put(dname, bs);
        }
    }

    /**
     * @return the identity is unique to this execution/construction
     */
    public final long getId() {
        return (myId);
    }

    /**
     * @return the name of the bootstrap
     */
    public final String getName() {
        return myName;
    }

    /**
     *
     * @return the number of factors in the multibootstrap
     */
    public final int getNumberFactors(){
        return myBootstraps.size();
    }

    /**
     *
     * @return the names of the factors as a list
     */
    public final List<String> getFactorNames(){
        return new ArrayList<>(myBootstraps.keySet());
    }

    /**
     * @param sampleSize the size of the original generate
     * @param samplerMap something to generate the original generate of the provided size
     * @return an instance of MultiBootstrap based on data generated from each generate
     */
    public final static MultiBootstrap create(int sampleSize, Map<String, SampleIfc> samplerMap) {
        return create(null, sampleSize, samplerMap);
    }

    /**
     * @param name       the name of the instance
     * @param sampleSize the generate size, all samplers have the same amount sampled
     * @param samplerMap something to generate the original generate of the provided size
     * @return an instance of MultiBootstrap based on data generated from each generate
     */
    public final static MultiBootstrap create(String name, int sampleSize, Map<String, SampleIfc> samplerMap) {
        if (samplerMap == null) {
            throw new IllegalArgumentException("The sampler map was null");
        }
        if (sampleSize <= 1) {
            throw new IllegalArgumentException("The generate size must be greater than 1");
        }

        Map<String, double[]> dataMap = new LinkedHashMap<>();
        for (Map.Entry<String, SampleIfc> entry : samplerMap.entrySet()) {
            String dname = entry.getKey();
            double[] data = entry.getValue().sample(sampleSize);
            dataMap.put(dname, data);
        }

        return new MultiBootstrap(name, dataMap);
    }

    /**
     * @param samplerMap the String of the map is the named identifier for the bootstrap, the entry of the map
     *                   is a pair (Integer, SamplerIfc) which represents the number to generate and the sampler
     * @return an instance of MultiBootstrap based on data generated from each generate
     */
    public final static MultiBootstrap create(Map<String, Map.Entry<Integer, SampleIfc>> samplerMap) {
        return create(null, samplerMap);
    }

    /**
     * @param name       the name of the instance
     * @param samplerMap the String of the map is the named identifier for the bootstrap, the entry of the map
     *                   is a pair (Integer, SamplerIfc) which represents the number to generate and the sampler
     * @return an instance of MultiBootstrap based on data generated from each generate
     */
    public final static MultiBootstrap create(String name, Map<String, Map.Entry<Integer, SampleIfc>> samplerMap) {
        if (samplerMap == null) {
            throw new IllegalArgumentException("The sampler map was null");
        }
        Map<String, double[]> dataMap = new LinkedHashMap<>();
        for (Map.Entry<String, Map.Entry<Integer, SampleIfc>> entry : samplerMap.entrySet()) {
            String dname = entry.getKey();
            if (dname == null) {
                throw new IllegalArgumentException("The name in the map must not be null");
            }
            Map.Entry<Integer, SampleIfc> value = entry.getValue();
            if (value == null) {
                throw new IllegalArgumentException("The entry associated with name = " + name + " was null");
            }
            Integer n = value.getKey();
            if (n == null) {
                throw new IllegalArgumentException("The generate size associated with name = " + name + " was null");
            }
            if (n <= 1) {
                throw new IllegalArgumentException("The generate size must be greater than 1");
            }
            SampleIfc s = value.getValue();
            if (s == null) {
                throw new IllegalArgumentException("The sampler associated with name = " + name + " was null");
            }
            double[] data = s.sample(n);
            dataMap.put(dname, data);
        }

        return new MultiBootstrap(name, dataMap);
    }

    /**
     * The individual bootstrapped samples are not saved. The estimator is EstimatorIfc.Average()
     *
     * @param numBootstrapSamples the number of bootstrap samples to generate
     */
    public final void generateSamples(int numBootstrapSamples) {
        generateSamples(numBootstrapSamples, new EstimatorIfc.Average(), false);
    }

    /**
     * The estimator is EstimatorIfc.Average()
     *
     * @param numBootstrapSamples  the number of bootstrap samples to generate
     * @param saveBootstrapSamples indicates that the statistics and data of each bootstrap generate should be saved
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

    /**
     * This method changes the underlying state of the Bootstrap instance by performing
     * the bootstrap sampling.
     *
     * @param numBootstrapSamples  the number of bootstrap samples to generate, assumes all are the same.
     * @param estimator            a function of the data
     * @param saveBootstrapSamples indicates that the statistics and data of each bootstrap generate should be saved
     */
    public void generateSamples(int numBootstrapSamples, EstimatorIfc estimator,
                                boolean saveBootstrapSamples) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String name : myBootstraps.keySet()) {
            map.put(name, Integer.valueOf(numBootstrapSamples));
        }
        generateSamples(map, estimator, saveBootstrapSamples);
    }

    /**
     * This method changes the underlying state of the Bootstrap instance by performing
     * the bootstrap sampling.
     *
     * @param numBootstrapSamples  the number of bootstrap samples to generate for each of the bootstraps, the
     *                             keys must match the keys in the original data map.  If the names do not match
     *                             then the bootstraps are not generated.
     * @param estimator            a function of the data
     * @param saveBootstrapSamples indicates that the statistics and data of each bootstrap generate should be saved
     */
    public void generateSamples(Map<String, Integer> numBootstrapSamples, EstimatorIfc estimator,
                                boolean saveBootstrapSamples) {
        if (numBootstrapSamples == null) {
            throw new IllegalArgumentException("The specification of the bootstrap generate sizes was null.");
        }

        for (Map.Entry<String, Integer> entry : numBootstrapSamples.entrySet()) {
            String name = entry.getKey();
            int n = entry.getValue();
            if (n > 1) {
                Bootstrap bootstrap = myBootstraps.get(name);
                bootstrap.generateSamples(n, estimator, saveBootstrapSamples);
            }
        }
    }

    /** Gets a map with key = name, where name is the associated bootstrap name
     *  and the value is the an array holding the generate averages for each
     *  bootstrap samples within the bootstrap
     *
     * @return a map of the generate averages
     */
    public Map<String, double[]> getBootstrapSampleAverages() {
        Map<String, double[]> map = new LinkedHashMap<>();
        for (String name : myBootstraps.keySet()) {
            Bootstrap bootstrap = myBootstraps.get(name);
            double[] bootstrapSampleAverages = bootstrap.getBootstrapSampleAverages();
            map.put(name, bootstrapSampleAverages);
        }
        return map;
    }

    /** Gets a map with key = name, where name is the associated bootstrap name
     *  and the value is the an array holding the generate variances for each
     *  bootstrap samples within the bootstrap
     *
     * @return a map of the generate averages
     */
    public Map<String, double[]> getBootstrapSampleVariances() {
        Map<String, double[]> map = new LinkedHashMap<>();
        for (String name : myBootstraps.keySet()) {
            Bootstrap bootstrap = myBootstraps.get(name);
            double[] bootstrapSampleVariances = bootstrap.getBootstrapSampleVariances();
            map.put(name, bootstrapSampleVariances);
        }
        return map;
    }

    /** Gets a map with key = name, where name is the associated bootstrap name
     *  and the value is List holding the generate data for each
     *  bootstrap generate within the bootstrap.  The size of the list is the number
     *  of bootstrap samples generated. Each element of the list is the data associated
     *  with each generate.
     *
     * @return a map of the list of bootstrap data
     */
    public Map<String, List<double[]>> getBootstrapSampleData(){
        Map<String, List<double[]>> map = new LinkedHashMap<>();
        for (String name : myBootstraps.keySet()) {
            Bootstrap bootstrap = myBootstraps.get(name);
            List<double[]> list = bootstrap.getDataForEachBootstrapSample();
            map.put(name, list);
        }
        return map;
    }

    /** Gets a map with key = name, where name is the associated bootstrap name
     *  and the value is List holding a RVariableIfc representation for each
     *  bootstrap generate within the bootstrap.  The size of the list is the number
     *  of bootstrap samples generated. Each element of the list is a RVariableIfc
     *  representation of the data with the bootstrap generate.
     *
     *  The stream for every random variable is the same across the
     *  bootstraps (but different across factors) to facilitate common random number generation (CRN).
     * @return a map of the list of bootstrap random variable representations
     */
    public Map<String, List<RVariableIfc>> getBootstrapRandomVariables(){
        return getBootstrapRandomVariables(true);
    }

    /** Gets a map with key = name, where name is the associated bootstrap name
     *  and the value is List holding a RVariableIfc representation for each
     *  bootstrap generate within the bootstrap.  The size of the list is the number
     *  of bootstrap samples generated. Each element of the list is a RVariableIfc
     *  representation of the data with the bootstrap generate.
     *
     *  @param useCRN, if true the stream for every random variable is the same across the
     *                     bootstraps to facilitate common random number generation (CRN). If false
     *                   different streams are used for each created random variable
     * @return a map of the list of bootstrap random variable representations
     */
    public Map<String, List<RVariableIfc>> getBootstrapRandomVariables(boolean useCRN){
        Map<String, List<RVariableIfc>> map = new LinkedHashMap<>();
        for (String name : myBootstraps.keySet()) {
            Bootstrap bootstrap = myBootstraps.get(name);
            List<RVariableIfc> list = bootstrap.getEmpiricalRVForEachBootstrapSample(useCRN);
            map.put(name, list);
        }
        return map;
    }

    /**
     *
     * @param name the name of the bootstrap
     * @param b the bootstrap generate number, b = 1, 2, ... to getNumBootstrapSamples()
     * @return the generate generated for the bth bootstrap, if no samples are saved then
     *     the array returned is of zero length
     */
    public double[] getBootstrapSampleData(String name, int b){
        Bootstrap bs = myBootstraps.get(name);
        if (bs == null){
            return new double[0];
        }
        return bs.getDataForBootstrapSample(b);
    }

    /** Gets a map with key = name, where name is the associated bootstrap name
     *  and the value is the generate data for the bth
     *  bootstrap generate.  The size of the array is the size of the generated
     *  bootstrap generate, the array may be of zero length if the samples were not saved
     *
     * @param b the bootstrap generate number, b = 1, 2, ... to getNumBootstrapSamples()
     * @return a map holding the bth bootstrap data for each bootstrap
     */
    public Map<String, double[]> getBootstrapSampleData(int b){
        Map<String, double[]> map = new LinkedHashMap<>();
        for (String name : myBootstraps.keySet()) {
            Bootstrap bootstrap = myBootstraps.get(name);
            double[] data = bootstrap.getDataForBootstrapSample(b);
            map.put(name, data);
        }
        return map;
    }

//    /** The columns are name:avg, name:var where name is the name of each bootstrap.
//     *  Each row is the observed generate average, generate variance for each of the
//     *  bootstrap samples associated with the named bootstrap.
//     *
//     * @return a Tablesaw Table holding the bootstrap generate averages and variances
//     */
//    public Table getTablesawTable(){
//        Table table = Table.create(getName());
//        Map<String, double[]> averages = getBootstrapSampleAverages();
//        for(String name: averages.keySet()){
//            DoubleColumn dc = DoubleColumn.create(name + ":avg", averages.get(name));
//           // DoubleColumn dc = new DoubleColumn(name + ":avg", averages.get(name));
//            table.addColumns(dc);
//        }
//        Map<String, double[]> variances = getBootstrapSampleVariances();
//        for(String name: variances.keySet()){
//            DoubleColumn dc = DoubleColumn.create(name + ":var", variances.get(name));
//            table.addColumns(dc);
//        }
//        return table;
//    }

    /**
     * @param name the name of the Bootstrap to get
     * @return the Bootstrap associated with the name
     */
    public final Bootstrap getBootstrap(String name) {
        return myBootstraps.get(name);
    }

    /**
     *
     * @return a list of all the bootstraps
     */
    public List<Bootstrap> getBootstrapList(){
        return new ArrayList<>(myBootstraps.values());
    }

    @Override
    public String toString(){
        return asString();
    }

    /**
     *
     * @return the bootstrap results as a string
     */
    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append("MultiBootstrap Results for : ");
        sb.append(getName());
        sb.append(System.lineSeparator());
        for(Bootstrap bs: myBootstraps.values()){
            sb.append(bs.asString());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * The resetStartStream method will position the RNG at the beginning of its
     * stream. This is the same location in the stream as assigned when the RNG
     * was created and initialized for all bootstraps
     */
    @Override
    public void resetStartStream() {
        for (Bootstrap bs : myBootstraps.values()) {
            bs.resetStartStream();
        }
    }

    /**
     * Resets the position of the RNG at the start of the current substream
     * for all bootstraps
     */
    @Override
    public void resetStartSubstream() {
        for (Bootstrap bs : myBootstraps.values()) {
            bs.resetStartSubstream();
        }
    }

    /**
     * Positions the RNG at the beginning of its next substream for all bootstraps
     */
    @Override
    public void advanceToNextSubstream() {
        for (Bootstrap bs : myBootstraps.values()) {
            bs.advanceToNextSubstream();
        }
    }

    /**
     * Tells all the streams to change their antithetic option
     *
     * @param flag true means that it produces antithetic variates.
     */
    @Override
    public void setAntitheticOption(boolean flag) {
        for (Bootstrap bs : myBootstraps.values()) {
            bs.setAntitheticOption(flag);
        }
    }

    /**
     * False means at least one is false.
     *
     * @return true means on all bootstraps have antithetic option on
     */
    @Override
    public boolean getAntitheticOption() {
        boolean b = true;
        for (Bootstrap bs : myBootstraps.values()) {
            b = bs.getAntitheticOption();
            if (b == false) {
                return false;
            }
        }
        return b;
    }

}
