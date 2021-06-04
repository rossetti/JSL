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
package jsl.simulation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import javax.swing.JOptionPane;
import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.observers.textfile.CSVExperimentReport;
import jsl.observers.textfile.CSVReplicationReport;
import jsl.utilities.JSLFileUtil;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.StatisticAccessorIfc;

/**
 * This class facilitates simulation output reporting. There are two main
 * reporting functions: within replication statistics and across replication
 * statistics.
 *
 * To collect within replication statistics you must use
 * turnOnReplicationCSVStatisticalReporting() before running the simulation.
 * This needs to be done before the simulation is run because the statistics are
 * collected after each replication is completed. This method attaches a
 * CSVReplicationReport to the model for collection purposes. If the simulation
 * is run multiple times, then statistical data continues to be observed by the
 * CSVReplicationReport. Thus, data across many experiments can be captured in
 * this manner. This produces a comma separated value file containing all end of
 * replication statistical summaries for every counter and response variable in
 * the model.
 *
 * There are a number of options available if you want to capture across
 * replication statistics.
 *
 * 1) turnOnAcrossReplicationCSVStatisticReporting() - This should be done
 * before running the simulation. It uses a CSVExperimentReport to observe a
 * model. This produces a comma separated value file containing all across
 * replication statistical summaries for every counter and response variable in
 * the model.
 *
 * 2) Use any of the writeAcrossReplicationX() methods. These methods
 * will write across replication summary statistics to files, standard output,
 * LaTeX, CSV, etc.
 *
 * @author rossetti
 */
public class SimulationReporter {

    private final Simulation mySim;

    private final Model myModel;

    private final ExperimentGetIfc myExp;

    private CSVReplicationReport myCSVRepReport;

    private CSVExperimentReport myCSVExpReport;

    public SimulationReporter(Simulation sim) {
        Objects.requireNonNull(sim, "The supplied simulation was null!");
        mySim = sim;
        myModel = sim.getModel();
        myExp = sim.getExperiment();
    }

    protected final Simulation getSimulation() {
        return mySim;
    }

    protected final Model getModel() {
        return myModel;
    }

    protected final ExperimentGetIfc getExperiment() {
        return myExp;
    }

    /**
     * A convenience method for sub-classes. Gets the response variables from
     * the model
     *
     * @return the list
     */
    protected final List<ResponseVariable> getResponseVariables() {
        return getModel().getResponseVariables();
    }

    /**
     * A convenience method for sub-classes. Gets the counters from the model
     *
     * @return the list 
     */
    protected final List<Counter> getCounters() {
        return getModel().getCounters();
    }

    /**
     * Uses a StringBuilder to hold the across replication statistics formatted
     * as a comma separated values with an appropriate header
     *
     * @return the string builder
     */
    public StringBuilder getAcrossReplicationCSVStatistics() {
        StringBuilder sb = new StringBuilder();

        boolean header = true;

        List<ResponseVariable> rvs = getResponseVariables();

        if (!rvs.isEmpty()) {
            for (ResponseVariable r : rvs) {
                StatisticAccessorIfc stat = r.getAcrossReplicationStatistic();
                if (header) {
                    header = false;
                    sb.append("SimName, ModelName, ExpName, ResponseType, ResponseID, ResponseName,");
                    sb.append(stat.getCSVStatisticHeader());
                    sb.append("\n");
                }
                if (r.getDefaultReportingOption()) {
                    sb.append(getSimulation().getName());
                    sb.append(",");
                    sb.append(getModel().getName());
                    sb.append(",");
                    sb.append(getExperiment().getExperimentName());
                    sb.append(",");
                    sb.append(r.getClass().getSimpleName());
                    sb.append(",");
                    sb.append(r.getId());
                    sb.append(",");
                    sb.append(r.getName());
                    sb.append(",");
                    sb.append(stat.getCSVStatistic());
                    sb.append("\n");
                }
            }
        }

        List<Counter> cs = getCounters();

        if (!cs.isEmpty()) {
            for (Counter c : cs) {
                StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
                if (header) {
                    header = false;
                    sb.append("SimName, ModelName, ExpName, ResponseType, ResponseID, ResponseName,");
                    sb.append(stat.getCSVStatisticHeader());
                    sb.append("\n");
                }
                if (c.getDefaultReportingOption()) {
                    sb.append(getSimulation().getName());
                    sb.append(",");
                    sb.append(getModel().getName());
                    sb.append(",");
                    sb.append(getExperiment().getExperimentName());
                    sb.append(",");
                    sb.append(c.getClass().getSimpleName());
                    sb.append(",");
                    sb.append(c.getId());
                    sb.append(",");
                    sb.append(c.getName());
                    sb.append(",");
                    sb.append(stat.getCSVStatistic());
                    sb.append("\n");
                }
            }
        }

        return sb;
    }

    /**
     * Writes the across replication statistics to the supplied PrintWriter as
     * comma separated value output
     *
     * @param out the PrintWriter
     */
    public final void writeAcrossReplicationCSVStatistics(PrintWriter out) {
        Objects.requireNonNull(out,"The PrintWriter was null!");
        boolean header = true;
        List<ResponseVariable> rvs = getResponseVariables();
        if (!rvs.isEmpty()) {
            for (ResponseVariable r : rvs) {
                StatisticAccessorIfc stat = r.getAcrossReplicationStatistic();
                if (header) {
                    header = false;
                    out.print("SimName, ModelName, ExpName, ResponseType, ResponseID, ResponseName,");
                    out.println(stat.getCSVStatisticHeader());
                }
                if (r.getDefaultReportingOption()) {
                    out.print(getSimulation().getName() + ",");
                    out.print(getModel().getName() + ",");
                    out.print(getExperiment().getExperimentName() + ",");
                    out.print(r.getClass().getSimpleName() + ",");
                    out.print(r.getId() + ",");
                    out.print(r.getName() + ",");
                    out.println(stat.getCSVStatistic());
                }
            }
        }

        List<Counter> cs = getCounters();

        if (!cs.isEmpty()) {
            for (Counter c : cs) {
                StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
                if (header) {
                    header = false;
                    out.print("SimName, ModelName, ExpName, ResponseType, ResponseID, ResponseName,");
                    out.println(stat.getCSVStatisticHeader());
                }
                if (c.getDefaultReportingOption()) {
                    out.print(getSimulation().getName() + ",");
                    out.print(getModel().getName() + ",");
                    out.print(getExperiment().getExperimentName() + ",");
                    out.print(c.getClass().getSimpleName() + ",");
                    out.print(c.getId() + ",");
                    out.print(c.getName() + ",");
                    out.println(stat.getCSVStatistic());
                }
            }
        }
    }

    /**
     * Writes the full across replication statistics to the supplied PrintWriter as
     * text output.  Full means all statistical quantities are printed for every statistic
     *
     * @param out the PrintWriter
     */
    public final void writeFullAcrossReplicationStatistics(PrintWriter out) {
        Objects.requireNonNull(out,"The PrintWriter was null!");

        out.println("-------------------------------------------------------");
        out.println();
        out.println(new Date());
        out.print("Simulation Results for Model: ");
        out.println(getModel().getName());
        out.println();

        out.println("-------------------------------------------------------");
        List<ResponseVariable> rvs = getResponseVariables();
        for (ResponseVariable r : rvs) {
            StatisticAccessorIfc stat = r.getAcrossReplicationStatistic();
            if (r.getDefaultReportingOption()) {
                out.println(stat);
            }
        }

        List<Counter> cs = getCounters();
        for (Counter c : cs) {
            StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
            if (c.getDefaultReportingOption()) {
                out.println(stat);
            }
        }
        out.println("-------------------------------------------------------");
    }

    /**
     * Writes shortened across replication statistics to the supplied
     * PrintWriter as text output
     *
     * Response Name Average Std. Dev.
     *
     * @param out the PrintWriter
     */
    public final void writeAcrossReplicationSummaryStatistics(PrintWriter out) {
        Objects.requireNonNull(out,"The PrintWriter was null!");
        String hline = "-------------------------------------------------------------------------------";
        out.println(hline);
        out.println();
        out.println("Across Replication Statistical Summary Report");
        out.println(new Date());
        out.print("Simulation Results for Model: ");
        out.println(getModel().getName());
        out.println();
        out.println();
        out.print("Number of Replications: ");
        out.println(getExperiment().getCurrentReplicationNumber());
        out.print("Length of Warm up period: ");
        out.println(getExperiment().getLengthOfWarmUp());
        out.print("Length of Replications: ");
        out.println(getExperiment().getLengthOfReplication());

        List<ResponseVariable> rvs = getResponseVariables();
        String format = "%-30s \t %12f \t %12f \t %12f %n";

        if (!rvs.isEmpty()) {
            out.println(hline);
            out.println("Response Variables");
            out.println(hline);
            out.printf("%-30s \t %12s \t %12s \t %5s %n", "Name", "Average", "Std. Dev.", "Count");
            out.println(hline);

            for (ResponseVariable r : rvs) {
                StatisticAccessorIfc stat = r.getAcrossReplicationStatistic();
                if (r.getDefaultReportingOption()) {
                    double avg = stat.getAverage();
                    double std = stat.getStandardDeviation();
                    double n = stat.getCount();
                    String name = r.getName();
                    out.printf(format, name, avg, std, n);
                }
            }
            out.println(hline);
        }

        List<Counter> cs = getCounters();
        if (!cs.isEmpty()) {
            out.println();
            out.println(hline);
            out.println("Counters");
            out.println(hline);
            out.printf("%-30s \t %12s \t %12s \t %5s %n", "Name", "Average", "Std. Dev.", "Count");
            out.println(hline);

            for (Counter c : cs) {
                StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
                if (c.getDefaultReportingOption()) {
                    double avg = stat.getAverage();
                    double std = stat.getStandardDeviation();
                    double n = stat.getCount();
                    String name = c.getName();
                    out.printf(format, name, avg, std, n);
                }
            }
            out.println(hline);
        }

    }

    /**
     * Returns a StringBuilder with across replication statistics
     *
     * @return the StringBuilder
     */
    public final StringBuilder getAcrossReplicationStatistics() {
        StringBuilder sb = new StringBuilder();
        getAcrossReplicationStatistics(sb);
        return sb;
    }

    /**
     * Gets the across replication statistics as a list
     *
     * @return a list filled with the across replication statistics
     */
    public final List<StatisticAccessorIfc> getAcrossReplicationStatisticsList() {
        List<StatisticAccessorIfc> list = new ArrayList<>();
        fillAcrossReplicationStatistics(list);
        return list;
    }

    /** Fills the supplied list with the across replication statistics
     * 
     * @param list the list to fill
     */
    public final void fillAcrossReplicationStatistics(List<StatisticAccessorIfc> list) {
        fillResponseVariableReplicationStatistics(list);
        fillCounterAcrossReplicationStatistics(list);
    }

    /** Fills the list with across replication statistics from the response 
     *  variables (ResponseVariable and TimeWeighted). 
     * 
     * @param list the list to fill
     */
    public final void fillResponseVariableReplicationStatistics(List<StatisticAccessorIfc> list) {
        List<ResponseVariable> rvs = getResponseVariables();
        for (ResponseVariable r : rvs) {
            StatisticAccessorIfc stat = r.getAcrossReplicationStatistic();
            if (r.getDefaultReportingOption()) {
                list.add(stat);
            }
        }
    }

    /** Fills the list with across replication statistics from the Counters
     * 
     * @param list the list to fill
     */
    public final void fillCounterAcrossReplicationStatistics(List<StatisticAccessorIfc> list) {
        List<Counter> cs = getCounters();
        for (Counter c : cs) {
            StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
            if (c.getDefaultReportingOption()) {
                list.add(stat);
            }
        }
    }

    /**
     * Fills the StringBuilder with across replication statistics
     *
     *
     * @param sb the StringBuilder to fill
     *
     */
    public final void getAcrossReplicationStatistics(StringBuilder sb) {
        if (sb == null) {
            throw new IllegalArgumentException("The StringBuilder was null");
        }
        sb.append(new Date());
        sb.append(System.lineSeparator());
        sb.append("Simulation Results for Model:");
        sb.append(System.lineSeparator());
        sb.append(getModel().getName());
        sb.append(System.lineSeparator());
        sb.append(this);
        sb.append(System.lineSeparator());
        List<ResponseVariable> rvs = getResponseVariables();
        for (ResponseVariable r : rvs) {
            StatisticAccessorIfc stat = r.getAcrossReplicationStatistic();
            if (r.getDefaultReportingOption()) {
                sb.append(stat);
                sb.append(System.lineSeparator());
            }
        }
        List<Counter> cs = getCounters();
        for (Counter c : cs) {
            StatisticAccessorIfc stat = c.getAcrossReplicationStatistic();
            if (c.getDefaultReportingOption()) {
                sb.append(stat);
                sb.append(System.lineSeparator());
            }
        }
    }

    /**
     * Writes the across replication statistics as comma separated values to
     * System.out
     *
     */
    public final void printAcrossReplicationCSVStatistics() {
        writeAcrossReplicationCSVStatistics(new PrintWriter(System.out, true));
    }

    /**
     * Creates a PrintWriter with the supplied name in directory jslOutput and
     * writes out the across replication statistics
     *
     * @param pathToFile the Path to the file
     * @return the PrintWriter
     */
    public final PrintWriter writeAcrossReplicationCSVStatistics(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The path to the file was null");
        PrintWriter out = JSLFileUtil.makePrintWriter(pathToFile);
        writeAcrossReplicationCSVStatistics(out);
        return out;
    }

    /**
     * Creates a PrintWriter with the supplied name in directory jslOutput and
     * writes out the across replication statistics
     *
     * @param fName the file name
     * @return the PrintWriter
     */
    public final PrintWriter writeAcrossReplicationCSVStatistics(String fName) {
        if (fName == null){
            // construct reasonable name
            fName = mySim.getName() + "_AcrossRepCSVStatistics.csv";
        }
        Path path = mySim.getOutputDirectory().getOutDir().resolve(fName);
        return writeAcrossReplicationCSVStatistics(path);
    }

    /**
     * Creates a PrintWriter with the supplied path and writes the full statistics
     * to the file. Full means all detailed statistical quantities for every statistic.
     *
     * @param pathToFile the path to the file
     * @return the PrintWriter
     */
    public final PrintWriter writeFullAcrossReplicationStatistics(Path pathToFile) {
        PrintWriter out = JSLFileUtil.makePrintWriter(pathToFile);
        writeFullAcrossReplicationStatistics(out);
        return out;
    }

    /**
     * Creates a PrintWriter with the supplied name in directory jslOutput and
     * writes out the across replication statistics.
     *
     * Full means all statistical quantities are printed for every statistic
     *
     * @param fName the file name
     * @return the PrintWriter
     */
    public final PrintWriter writeFullAcrossReplicationStatistics(String fName) {
        if (fName == null){
            // construct reasonable name
            fName = mySim.getName() + "_FullAcrossRepStatistics.txt";
        }
        Path path = mySim.getOutputDirectory().getOutDir().resolve(fName);
        return writeFullAcrossReplicationStatistics(path);
    }

    /**
     * Writes the across replication statistics as text values to System.out
     *
     */
    public final void printFullAcrossReplicationStatistics() {
        writeFullAcrossReplicationStatistics(new PrintWriter(System.out, true));
    }

    /**
     * Writes the across replication statistics as text values to System.out
     *
     */
    public final void printAcrossReplicationSummaryStatistics() {
        writeAcrossReplicationSummaryStatistics(new PrintWriter(System.out, true));
    }

    /**
     * Creates a PrintWriter with the supplied name in directory within
     * jslOutput and writes out the across replication statistics
     *
     * @param pathToFile the path to the file
     * @return the PrintWriter
     */
    public final PrintWriter writeAcrossReplicationSummaryStatistics(Path pathToFile) {
        PrintWriter out = JSLFileUtil.makePrintWriter(pathToFile);
        writeAcrossReplicationSummaryStatistics(out);
        return out;
    }

    /**
     * Creates a PrintWriter with the supplied name in directory jslOutput and
     * writes out the across replication statistics
     *
     * @param fName the file name
     * @return the PrintWriter
     */
    public final PrintWriter writeAcrossReplicationSummaryStatistics(String fName) {
        if (fName == null){
            // construct reasonable name
            fName = mySim.getName() + "_SummaryStatistics.txt";
        }
        Path path = mySim.getOutputDirectory().getOutDir().resolve(fName);
        return writeAcrossReplicationSummaryStatistics(path);
    }

    /**
     * Attaches a CSVReplicationReport to the model to record within replication
     * statistics to a file
     *
     */
    public final void turnOnReplicationCSVStatisticReporting() {
        turnOnReplicationCSVStatisticReporting(null);
    }

    /**
     * Attaches a CSVReplicationReport to the model to record within replication
     * statistics to a file
     *
     * @param name the report file name
     */
    public final void turnOnReplicationCSVStatisticReporting(String name) {
        if (myCSVRepReport != null) {
            myModel.deleteObserver(myCSVRepReport);
        }
        if (name == null){
            name = getSimulation().getName() + "_ReplicationReport.csv";
        }
        // path inside jslOutput with simulation's name on it
        Path dirPath = mySim.getOutputDirectory().getOutDir();
        // now need path to csv replication report results
        Path filePath = dirPath.resolve(name);
        myCSVRepReport = new CSVReplicationReport(filePath);
        getModel().addObserver(myCSVRepReport);
    }

    /**
     * Detaches a CSVReplicationReport from the model
     *
     */
    public final void turnOffReplicationCSVStatisticReporting() {
        if (myCSVRepReport != null) {
            getModel().deleteObserver(myCSVRepReport);
        }
    }

    /**
     * Writes shortened across replication statistics to the supplied
     * PrintWriter as text output in LaTeX document form
     *
     * Response Name Average Std. Dev.
     *
     * @param out the PrintWriter to write to 
     */
    public final void writeAcrossReplicationSummaryStatisticsAsLaTeX(PrintWriter out) {
        Objects.requireNonNull(out, "The supplied PrintWriter was null");
        out.print(getAcrossReplicationStatisticsAsLaTeXDocument());
        out.flush();
    }

    /**
     * Creates a PrintWriter using the supplied path and writes out the across replication
     * statistics as a LaTeX file
     *
     * @param pathToFile the path to the file
     * @return the PrintWriter
     */
    public final PrintWriter writeAcrossReplicationSummaryStatisticsAsLaTeX(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The supplied path was null");
        if (!JSLFileUtil.isTeXFile(pathToFile)){
            // add tex
            pathToFile = Path.of(pathToFile.toString() + ".tex");
        }
        PrintWriter out = JSLFileUtil.makePrintWriter(pathToFile);
        writeAcrossReplicationSummaryStatisticsAsLaTeX(out);
        return out;
    }

    /**
     * Creates a file with the supplied name
     * in the simulation's output directory with the results as a LaTeX table.
     *
     * @param fName the file name
     * @return the PrintWriter
     */
    public final PrintWriter writeAcrossReplicationSummaryStatisticsAsLaTeX(String fName) {
        Objects.requireNonNull(fName, "The name of the file was null!");
        // get the output directory for the simulation
        Path pathToFile = getSimulation().getOutputDirectory().getOutDir().resolve(fName);
        return writeAcrossReplicationSummaryStatisticsAsLaTeX(pathToFile);
    }

    /**
     * Creates a file with name getSimulation().getName() + "_LaTeX_Across_Replication_Summary.tex"
     * in the simulation's output directory with the results as a LaTeX table.
     *
     * @return the PrintWriter
     */
    public final PrintWriter writeAcrossReplicationSummaryStatisticsAsLaTeX() {
        String fName = getSimulation().getName() + "_LaTeX_Across_Replication_Summary.tex";
        return writeAcrossReplicationSummaryStatisticsAsLaTeX(fName);
    }

    /**
     * List of StringBuilder representing LaTeX tables max 60 rows
     *
     * @return the tables as StringBuilders
     */
    public final List<StringBuilder> getAcrossReplicationStatisticsAsLaTeXTables() {
        return getAcrossReplicationStatisticsAsLaTeXTables(60);
    }

    /**
     * List of StringBuilder representing LaTeX tables
     *
     * @param maxRows the maximum number of rows
     * @return the tables as StringBuilders
     */
    public final List<StringBuilder> getAcrossReplicationStatisticsAsLaTeXTables(int maxRows) {
        List<StringBuilder> list = getAcrossReplicationStatisticsAsLaTeXTabular(maxRows);
        String hline = "\\hline";
        String caption = "\\caption{Across Replication Statistics for " + mySim.getName() + "} \n";
        String captionc = "\\caption{Across Replication Statistics for " + mySim.getName() + " (Continued)} \n";
        String beginTable = "\\begin{table}[ht] \n";
        String endTable = "\\end{table} \n";
        String centering = "\\centering \n";
        String nReps = "\n Number of Replications " + myExp.getCurrentReplicationNumber() + " \n";
        int i = 1;
        for (StringBuilder sb : list) {
            sb.insert(0, centering);
            if (i == 1) {
                sb.insert(0, caption);
            } else {
                sb.insert(0, captionc);
            }
            i++;
            sb.insert(0, beginTable);
            //sb.append(" \n \\\\");
            sb.append(nReps);
            sb.append(endTable);
        }

        return list;
    }

    /**
     * Returns a StringBuilder representation of the across replication
     * statistics as a LaTeX document with max number of rows = 60
     *
     * @return the tables as StringBuilders
     */
    public final StringBuilder getAcrossReplicationStatisticsAsLaTeXDocument() {
        return getAcrossReplicationStatisticsAsLaTeXDocument(60);
    }

    /**
     * Returns a StringBuilder representation of the across replication
     * statistics as a LaTeX document
     *
     * @param maxRows maximum number of rows in each table
     * @return the StringBuilder
     */
    public final StringBuilder getAcrossReplicationStatisticsAsLaTeXDocument(int maxRows) {
        String docClass = "\\documentclass[11pt]{article} \n";
        String beginDoc = "\\begin{document} \n";
        String endDoc = "\\end{document} \n";

        List<StringBuilder> list = getAcrossReplicationStatisticsAsLaTeXTables(maxRows);
        StringBuilder sb = new StringBuilder();
        sb.append(docClass);
        sb.append(beginDoc);
        for (StringBuilder s : list) {
            sb.append(s);
        }
        sb.append(endDoc);
        return sb;
    }

    /**
     * Gets shortened across replication statistics for response variables as a
     * LaTeX tabular. Each StringBuilder in the list represents a tabular with a
     * maximum number of 60 rows
     *
     * Response Name Average Std. Dev.
     *
     * @return a List of StringBuilders
     */
    public final List<StringBuilder> getAcrossReplicationStatisticsAsLaTeXTabular() {
        return getAcrossReplicationStatisticsAsLaTeXTabular(60);
    }

    /**
     * Gets shortened across replication statistics for response variables as a
     * LaTeX tabular. Each StringBuilder in the list represents a tabular with a
     * maximum number of rows
     *
     * Response Name Average Std. Dev.
     *
     * @param maxRows maximum number of rows in each tabular
     * @return a List of StringBuilders
     */
    public final List<StringBuilder> getAcrossReplicationStatisticsAsLaTeXTabular(int maxRows) {

        List<StringBuilder> builders = new ArrayList<StringBuilder>();
        List<StatisticAccessorIfc> stats = getModel().getListOfAcrossReplicationStatistics();

        if (!stats.isEmpty()) {
            String hline = "\\hline";
            String f1 = "%s %n";
            String f2 = "%s & %12f & %12f \\\\ %n";
            String header = "Response Name & $ \\bar{x} $ & $ s $ \\\\";
            String beginTabular = "\\begin{tabular}{lcc}";
            String endTabular = "\\end{tabular}";
            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            f.format(f1, beginTabular);
            f.format(f1, hline);
            f.format(f1, header);
            f.format(f1, hline);
            int i = 0;
            int mheaders = (stats.size() / maxRows);
            if ((stats.size() % maxRows) > 0) {
                mheaders = mheaders + 1;
            }
            int nheaders = 1;
            boolean inprogress = false;
            for (StatisticAccessorIfc stat : stats) {
                double avg = stat.getAverage();
                double std = stat.getStandardDeviation();
                String name = stat.getName();
                f.format(f2, name, avg, std);
                i++;
                inprogress = true;
                if ((i % maxRows) == 0) {
                    // close off current tabular
                    f.format(f1, hline);
                    f.format(f1, endTabular);
                    builders.add(sb);
                    inprogress = false;
                    // if necessary, start a new one
                    if (nheaders <= mheaders) {
                        nheaders++;
                        sb = new StringBuilder();
                        f = new Formatter(sb);
                        f.format(f1, beginTabular);
                        f.format(f1, hline);
                        f.format(f1, header);
                        f.format(f1, hline);
                    }
                }
            }
            // close off one in progress
            if (inprogress) {
                f.format(f1, hline);
                f.format(f1, endTabular);
                builders.add(sb);
            }
        }
        return builders;
    }

    /**
     * Attaches a CSVExperimentReport to the model to record across replication
     * statistics to a file
     *
     */
    public final void turnOnAcrossReplicationCSVStatisticReporting() {
        turnOnAcrossReplicationCSVStatisticReporting(null);
    }

    /**
     * Attaches a CSVExperimentReport to the model to record across replication
     * statistics to a file
     *
     * @param name the file name of the report
     */
    public final void turnOnAcrossReplicationCSVStatisticReporting(String name) {
        if (myCSVExpReport != null) {
            myModel.deleteObserver(myCSVExpReport);
        }
        if (name == null){
            name = getSimulation().getName() + "_CSVExperimentReport.csv";
        }
        // path inside jslOutput with simulation's name on it
        Path dirPath = mySim.getOutputDirectory().getOutDir();
        // now need path to csv replication report results
        Path filePath = dirPath.resolve(name);
        myCSVExpReport = new CSVExperimentReport(filePath);
        getModel().addObserver(myCSVExpReport);
    }

    /**
     * Detaches a CSVExperimentReport from the model
     *
     */
    public final void turnOffAcrossReplicationStatisticReporting() {
        if (myCSVExpReport != null) {
            getModel().deleteObserver(myCSVExpReport);
        }
    }

    /**
     *  Prints a half-width summary report for the across replication statistics to
     *  System.out
     */
    public final void printAcrossReplicationHalfWidthSummaryReport(){
        PrintWriter printWriter = new PrintWriter(System.out);
        writeAcrossReplicationHalfWidthSummaryReport(printWriter);
    }

    /** Writes a half-width summary report for the across replication statistics
     *
     * @param out the writer to write to
     */
    public final void writeAcrossReplicationHalfWidthSummaryReport(PrintWriter out){
        List<StatisticAccessorIfc> list = getAcrossReplicationStatisticsList();
        StatisticReporter statisticReporter = new StatisticReporter(list);
        StringBuilder report = statisticReporter.getHalfWidthSummaryReport();
        out.println(report.toString());
        out.flush();
    }

    /**
     *
     * @return a StatisticReporter holding the across replication statistics for reporting
     */
    public final StatisticReporter getAcrossReplicationStatisticReporter(){
        List<StatisticAccessorIfc> list = getAcrossReplicationStatisticsList();
        StatisticReporter statisticReporter = new StatisticReporter(list);
        return statisticReporter;
    }
}
