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

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.ModelElementObserver;
import jsl.utilities.JSLFileUtil;
import jsl.utilities.statistic.MultipleComparisonAnalyzer;

/**
 * Collects multiple comparison data for a particular ResponseVariable (each
 * across replication average for each system configuration)
 *
 * This class assumes that each Experiment is given a unique name in order to
 * uniquely determine the configuration for which the across replication data is
 * collected for the observed response variable.
 * 
 * This observer must be attached prior to running the simulation.
 *
 * @author rossetti
 */
public class MultipleComparisonDataCollector extends ModelElementObserver {

    private ResponseVariable myResponse;

    private LinkedHashMap<String, double[]> myDataMap;

    private int myRepCount = 0;
    
    /**
     * the minimum number of replications across all the experiments
     * Used to make sure comparison data all has same number of observations
     */
    private int myMinNumRepsAcrossExperiments = Integer.MAX_VALUE;

    public MultipleComparisonDataCollector(ResponseVariable response) {
        this(response, null);
    }

    public MultipleComparisonDataCollector(ResponseVariable response, String name) {
        super(name);

        if (response == null) {
            throw new IllegalArgumentException("The response must not be null");
        }

        myDataMap = new LinkedHashMap<String, double[]>();

        myResponse = response;

        myResponse.addObserver(this);

    }

    /**
     * Sets all the data to zero
     *
     */
    public final void clearData() {
        myDataMap.clear();
    }

    /** Returns the names of the experiments as an array of strings
     * 
     * @return the names of the experiments as an array of strings
     */
    public String[] getExperimentNames() {
        return (String[]) myDataMap.keySet().toArray();
    }

    /** Returns a MultipleComparisonAnalyzer based on the data
     *  of the collector
     * 
     * @return a MultipleComparisonAnalyzer based on the data
     *  of the collector
     */
    public final MultipleComparisonAnalyzer getMultipleComparisonAnalyzer() {
        return new MultipleComparisonAnalyzer(getDataAsMap());
    }

    /** The key is the name of the experiment
     *  The double[] is a array of the across replication averages
     *  for the response variable
     *
     * @return a copy of the data
     */
    public final LinkedHashMap<String, double[]> getDataAsMap() {
        LinkedHashMap<String, double[]> m = new LinkedHashMap<>();
        for (String s : myDataMap.keySet()) {
            double[] d = getAcrossReplicationData(s);
            m.put(s, d);
        }
        return m;
    }
    
    /** Returns the number of replications in the experiment that had
     *  the smallest number of replications
     * 
     * @return the number of replications in the experiment that had
     *  the smallest number of replications
     */
    public final int getMinimumNumberReplicationsAcrossExperiments(){
        return myMinNumRepsAcrossExperiments;
    }

    /** A 2-Dim array of the data
     *  each row represents the across replication average for each  
     *  configuration (column). The number of rows is based on the
     *  configuration that has the smallest number of replications.
     *  Thus, this produces a rectangular array and may not include
     *  all replication data for every configuration.
     * 
     * @return  the data as an array
     */
    public final double[][] getDataAsArray() {
        int c = myDataMap.keySet().size();
        int r = getMinimumNumberReplicationsAcrossExperiments();
        //int r = myResponse.getExperiment().getNumberOfReplications();
        double[][] x = new double[r][c];
        int j = 0;
        for (String s : myDataMap.keySet()) {
            // get the column data
            double[] d = getAcrossReplicationData(s);
            // copy column data into the array
            for (int i = 0; i < r; i++) {
                x[i][j] = d[i];
            }
            // index to next column
            j++;
        }
        return x;
    }

    /** Writes the data out to the PrintWriter
     *  Each row represents a different replication
     *  the columns start with ResponseName, Rep_Num and then
     *  each configuration in a new column
     *  The header has "ResponseName", "Rep_Num", Experiment Name, ...
     * 
     * @param out the PrintWriter
     * @param header if true write the header, false don't write the header
     */
    public void writeDataAsCSVFile(PrintWriter out, boolean header) {
        int c = myDataMap.keySet().size();
        int r = 1;
        if (header == true) {
            out.print("ResponseName");
            out.print(",");
            out.print("Rep_Num");
            out.print(",");
            for (String s : myDataMap.keySet()) {
                out.print(s);
                if (r < c) {
                    out.print(",");
                }
                r++;
            }
            out.println();
        }
        double[][] data = getDataAsArray();
        for (int i = 0; i < data.length; i++) {
            out.print(myResponse.getName());
            out.print(",");
            out.print(i + 1);
            out.print(",");
            for (int j = 0; j < data[i].length; j++) {
                out.print(data[i][j]);
                if (j < data[i].length - 1) {
                    out.print(",");
                }
            }
            out.println();
        }
    }

    /** Write the data to a default file with name 
     *  Response.getName() + "_MCBData".csv within the simulation's
     *  output directory
     * 
     * @return the PrintWriter
     */
    public PrintWriter writeDataAsCSVFile() {
        Path outDir = myResponse.getSimulation().getOutputDirectory().getOutDir();
        String fName = myResponse.getName() + "_MCBData.csv";
        Path filePath = outDir.resolve(fName);
        PrintWriter out = JSLFileUtil.makePrintWriter(filePath);
        writeDataAsCSVFile(out, true);
        return out;
    }

    /** Across replication averages for the experiment with
     *  the given name
     * 
     * @param name the name
     * @return  the replication averages
     */
    public final double[] getAcrossReplicationData(String name) {
        double[] x = myDataMap.get(name);
        int r = getMinimumNumberReplicationsAcrossExperiments();
        // x may have more reps, but only return min reps across experiments
        double[] d = new double[r];
        System.arraycopy(x, 0, d, 0, r);
        return d;
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        // get the experiment's name
        String name = m.getExperiment().getExperimentName();
        // get the number of replications
        int n = m.getExperiment().getNumberOfReplications();
        // record the smallest number of replications across the experiments
        if (n < myMinNumRepsAcrossExperiments){
            myMinNumRepsAcrossExperiments = n;
        }
        // allocate the array to hold the across replication data for response
        double[] data = new double[n];
        // hold the data
        myDataMap.put(name, data);
        myRepCount = 0;
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        // capture the replication data for response
        // get the experiment's name
        String name = m.getExperiment().getExperimentName();
        // get the array that holds the across replication data for the experiment
        double data[] = myDataMap.get(name);
        // get the response
        double avg = myResponse.getWithinReplicationStatistic().getAverage();
        // record it for the replication
        data[myRepCount] = avg;
        // increase the replication counter
        myRepCount++;
    }
}
