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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.utilities.reporting;

import java.text.DecimalFormat;
import java.util.*;

import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticAccessorIfc;

/**
 * A class to help with making useful statistical reports. Creates summary
 * reports as StringBuilders based on the supplied list of statistics
 *
 * @author rossetti
 */
public class StatisticReporter {

    public static final DecimalFormat D2FORMAT = new DecimalFormat(".##");

    public final String DEFAULT_ROW_FORMAT = "%-40s \t %12d \t %12.4f \t %12.4f %n";

    public final String DEFAULT_HEADER_FORMAT = "%-40s \t %12s \t %12s \t %12s %n";

    private List<StatisticAccessorIfc> myStats;

    public final StringBuilder myRowFormat;

    private final StringBuilder myHeaderFormat;

    private int myNumCols = 100;

    private int myNumDecPlaces = 6;

    private final StringBuilder myHline;

    private boolean myTimeDateFlag = false;

    private boolean myReportLableFlag = true;

    private String myReportTitle = null;

    /**
     *  Creates a Statistic Reporter with no statistics
     */
    public StatisticReporter(){
        this(new ArrayList<>());
    }

    /**
     *
     * @param listOfStats a list containing the StatisticAccessorIfc instances
     */
    public StatisticReporter(List<StatisticAccessorIfc> listOfStats) {
        Objects.requireNonNull(listOfStats, "The list of stats was null.");
        myStats = listOfStats;
        myHline = new StringBuilder();
        for (int i = 1; i <= myNumCols; i++) {
            myHline.append("-");
        }
        myRowFormat = new StringBuilder(DEFAULT_ROW_FORMAT);
        myHeaderFormat = new StringBuilder(DEFAULT_HEADER_FORMAT);

    }

    /** Creates and adds a statistic with no name to the reporter
     *
     * @return the created statistic
     */
    public final Statistic addStatistic(){
        String name = null;
        return addStatistic(name);
    }

    /**
     *
     * @param name the name of the statistic to add to the reporter
     * @return the created statistic
     */
    public final Statistic addStatistic(String name){
        Statistic s = new Statistic(name);
        addStatistic(s);
        return s;
    }

    /**
     *
     * @param statistic the statistic to add, must not be null
     */
    public final void addStatistic(StatisticAccessorIfc statistic){
        Objects.requireNonNull(statistic, "The supplied statistic was null.");
        if (myStats.contains(statistic)){
            return;
        }
        myStats.add(statistic);
    }

    /**
     * Gets the report title
     *
     * @return the title as a string
     */
    public final String getReportTitle() {
        return myReportTitle;
    }

    /**
     * Sets the report title. The title appears as the first line of any report.
     *
     * @param title the title
     */
    public final void setReportTitle(String title) {
        myReportTitle = title;
    }

    /**
     * Finds the number of characters of the statistic with the longest name
     *
     * @return number of characters
     */
    public int findSizeOfLongestName() {
        int max = Integer.MIN_VALUE;
        StatisticAccessorIfc stat = null;
        for (StatisticAccessorIfc s : myStats) {
            if (s.getName().length() > max) {
                max = s.getName().length();
                stat = s;
            }
        }
        if (stat == null) {
            return 0;
        }
        return stat.getName().length();
    }

    /**
     * Changes the number of spaces for printing the names
     *
     *
     * @param n must be between 10 and 99
     */
    public final void setNameFieldSize(int n) {
        if (n < 10) {
            throw new IllegalArgumentException("The number of decimal places must be >=10");
        }
        if (n > 99) {
            throw new IllegalArgumentException("The number of decimal places must be <=99");
        }
        myRowFormat.replace(2, 4, Integer.toString(n));
        myHeaderFormat.replace(2, 4, Integer.toString(n));
    }

    /**
     * Changes the number of decimal places in the average reporting.
     *
     *
     * @param n must be between 0 and 9
     */
    public final void setDecimalPlaces(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The number of decimal places must be >=0");
        }
        if (n > 9) {
            throw new IllegalArgumentException("The number of decimal places must be <=9");
        }
        myRowFormat.replace(19, 20, Integer.toString(n));
        myRowFormat.replace(28, 29, Integer.toString(n));
    }

    /**
     *
     * @param flag true means labeling is on report
     */
    public final void setReportLabelFlag(boolean flag) {
        myReportLableFlag = flag;
    }

    /**
     * Whether or not the report labeling occurs
     *
     * @return true means labeling is on report
     */
    public final boolean getReportLabelFlag() {
        return myReportLableFlag;
    }

    /**
     * Indicate whether to have time/date on the report
     *
     * @param flag true means yes
     */
    public final void setTimeDateFlag(boolean flag) {
        myTimeDateFlag = flag;
    }

    /**
     * Whether or not the time and date are on the report
     *
     * @return true means it is
     */
    public final boolean getTimeDateFlag() {
        return myTimeDateFlag;
    }

    /**
     * The summary statistics are presented with half-widths 95% confidence
     * level with no title
     *
     * @return a StringBuilder holding the report
     */
    public StringBuilder getHalfWidthSummaryReport() {
        return getHalfWidthSummaryReport(null, 0.95);
    }

    /**
     * The summary statistics are presented with half-widths with default 95%
     * confidence level
     *
     * @param title optional title of the report
     * @return a StringBuilder holding the report
     */
    public StringBuilder getHalfWidthSummaryReport(String title) {
        return getHalfWidthSummaryReport(title, 0.95);
    }

    /**
     * The summary statistics are presented with half-widths
     *
     * @param confLevel confidence level for the half-widths
     * @return a StringBuilder holding the report
     */
    public StringBuilder getHalfWidthSummaryReport(double confLevel) {
        return getHalfWidthSummaryReport(null, confLevel);
    }

    /**
     * The summary statistics are presented with half-widths
     *
     * @param title optional title of the report
     * @param confLevel confidence level for the half-widths
     * @return a StringBuilder holding the report
     */
    public StringBuilder getHalfWidthSummaryReport(String title, double confLevel) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        if (getReportLabelFlag()) {
            if (myReportTitle != null) {
                formatter.format("%s %n", myReportTitle);
            }
            formatter.format("Half-Width Statistical Summary Report");
            formatter.format(" - Confidence Level (%5.3f)%% %n%n", confLevel * 100.0);
        }
        if (getTimeDateFlag()) {
            formatter.format("%tc%n%n", Calendar.getInstance().getTimeInMillis());
        }
        //formatter.format("Half-Width Confidence Level %4f %n%n", confLevel);
        if (title != null) {
            formatter.format("%s %n", title);
        }
        String hf = myHeaderFormat.toString();
        String rf = myRowFormat.toString();
        formatter.format(hf, "Name", "Count", "Average", "Half-Width");
        formatter.format("%s %n", myHline);
        for (StatisticAccessorIfc stat : myStats) {
            int n = (int) stat.getCount();
            double avg = stat.getAverage();
            double hw = stat.getHalfWidth(confLevel);
            String name = stat.getName();
            formatter.format(rf, name, n, avg, hw);
        }
        formatter.format("%s %n", myHline);
        return sb;
    }

    /**
     * Gets the Summary Report as a StringBuilder with no title
     *
     * @return the StringBuilder
     */
    public StringBuilder getSummaryReport() {
        return getSummaryReport(null);
    }

    /**
     * Gets the Summary Report as a StringBuilder
     *
     * @param title an optional title for the report
     * @return the StringBuilder
     */
    public StringBuilder getSummaryReport(String title) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        if (getReportLabelFlag()) {
            if (myReportTitle != null) {
                formatter.format("%s %n", myReportTitle);
            }
            formatter.format("Statistical Summary Report%n%n");
        }
        if (getTimeDateFlag()) {
            formatter.format("%tc%n%n", Calendar.getInstance().getTimeInMillis());
        }
        if (title != null) {
            formatter.format("%s %n", title);
        }
        String hf = myHeaderFormat.toString();
        String rf = myRowFormat.toString();
        formatter.format(hf, "Name", "Count", "Average", "Std. Dev.");
        formatter.format("%s %n", myHline);
        for (StatisticAccessorIfc stat : myStats) {
            int n = (int) stat.getCount();
            double avg = stat.getAverage();
            double std = stat.getStandardDeviation();
            String name = stat.getName();
            formatter.format(rf, name, n, avg, std);
        }
        formatter.format("%s %n", myHline);
        return sb;
    }

    /**
     * Gets statistics as LaTeX tabular. Each StringBuilder in the list
     * represents a tabular with a maximum number of rows = 60
     * <p>
     * confidence limit is 0.95
     * <p>
     * Response Name Average Std. Dev.
     * <p>
     * Note: If the name has any special LaTeX characters the resulting tabular
     * may not compile.
     *
     * @return a List of StringBuilders
     */
    public List<StringBuilder> getSummaryReportAsLaTeXTabular() {
        return getSummaryReportAsLaTeXTabular(60);
    }

    /**
     * Gets statistics as LaTeX tabular. Each StringBuilder in the list
     * represents a tabular with a maximum number of rows
     * <p>
     * confidence limit is 0.95
     * <p>
     * Response Name Average Std. Dev.
     * <p>
     * Note: If the name has any special LaTeX characters the resulting tabular
     * may not compile.
     *
     * @param maxRows maximum number of rows in each tabular
     * @return a List of StringBuilders
     */
    public List<StringBuilder> getSummaryReportAsLaTeXTabular(int maxRows) {
        List<StringBuilder> builders = new ArrayList<>();
        if (!myStats.isEmpty()) {
            String hline = "\\hline";
            String f1 = "%s %n";
            String f2 = "%s & %6d & %12f & %12f \\\\ %n";
            String header = "Response Name & $n$ & $ \\bar{x} $ & $ s $ \\\\";
            String beginTabular = "\\begin{tabular}{lcc}";
            String endTabular = "\\end{tabular}";
            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            f.format(f1, beginTabular);
            f.format(f1, hline);
            f.format(f1, header);
            f.format(f1, hline);
            int i = 0;
            int mheaders = (myStats.size() / maxRows);
            if ((myStats.size() % maxRows) > 0) {
                mheaders = mheaders + 1;
            }
            int nheaders = 1;
            boolean inprogress = false;
            for (StatisticAccessorIfc stat : myStats) {
                int n = (int) stat.getCount();
                double avg = stat.getAverage();
                double std = stat.getStandardDeviation();
                String name = stat.getName();
                f.format(f2, name, n, avg, std);
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
     * Gets statistics as LaTeX tabular. Each StringBuilder in the list
     * represents a tabular with a maximum number of rows = 60 and 0.95
     * confidence limit
     * <p>
     * Response Name Average Half-width
     * <p>
     * Note: If the name has any special LaTeX characters the resulting tabular
     * may not compile.
     *
     * @return a List of StringBuilders
     */
    public List<StringBuilder> getHalfWidthSummaryReportAsLaTeXTabular() {
        return getHalfWidthSummaryReportAsLaTeXTabular(60, 0.95);
    }

    /**
     * Gets statistics as LaTeX tabular. Each StringBuilder in the list
     * represents a tabular with a maximum number of rows = 60
     * <p>
     * Response Name Average Half-width
     * <p>
     * Note: If the name has any special LaTeX characters the resulting tabular
     * may not compile.
     *
     * @param confLevel the confidence level for the half-width calculation
     * @return a List of StringBuilders
     */
    public List<StringBuilder> getHalfWidthSummaryReportAsLaTeXTabular(double confLevel) {
        return getHalfWidthSummaryReportAsLaTeXTabular(60, confLevel);
    }

    /**
     * Gets statistics as LaTeX tabular. Each StringBuilder in the list
     * represents a tabular with a maximum number of rows
     * <p>
     * Response Name Average Half-width
     * <p>
     * Note: If the name has any special LaTeX characters the resulting tabular
     * may not compile.
     *
     * @param maxRows maximum number of rows in each tabular
     * @param confLevel the confidence level for the half-width calculation
     * @return a List of StringBuilders
     */
    public List<StringBuilder> getHalfWidthSummaryReportAsLaTeXTabular(int maxRows, 
            double confLevel) {
        List<StringBuilder> builders = new ArrayList<>();
        if (!myStats.isEmpty()) {
            String hline = "\\hline";
            String f1 = "%s %n";
            String f2 = "%s & %6d & %12f & %12f \\\\ %n";
            String header = "Response Name & $n$ & $ \\bar{x} $ & $ hw $ \\\\";
            String beginTabular = "\\begin{tabular}{lcc}";
            String endTabular = "\\end{tabular}";
            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            f.format(f1, beginTabular);
            f.format(f1, hline);
            f.format(f1, header);
            f.format(f1, hline);
            int i = 0;
            int mheaders = (myStats.size() / maxRows);
            if ((myStats.size() % maxRows) > 0) {
                mheaders = mheaders + 1;
            }
            int nheaders = 1;
            boolean inprogress = false;
            for (StatisticAccessorIfc stat : myStats) {
                int n = (int) stat.getCount();
                double avg = stat.getAverage();
                double hw = stat.getHalfWidth(confLevel);
                String name = stat.getName();
                f.format(f2, name, n, avg, hw);
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
     * List of StringBuilder representing LaTeX tables max 60 rows
     *
     * @return the tables as StringBuilders
     */
    public final List<StringBuilder> getHalfWidthSummaryReportAsLaTeXTables() {
        return getHalfWidthSummaryReportAsLaTeXTables(60, 0.95);
    }

    /**
     * List of StringBuilder representing LaTeX tables max 60 rows
     *
     * @param confLevel the confidence level for the half-width
     * @return the tables as StringBuilders
     */
    public final List<StringBuilder> getHalfWidthSummaryReportAsLaTeXTables(double confLevel) {
        return getHalfWidthSummaryReportAsLaTeXTables(60, confLevel);
    }

    /**
     * List of StringBuilder representing LaTeX tables
     *
     * @param maxRows the max number of rows
     * @param confLevel the confidence level
     * @return the tables as StringBuilders
     */
    public final List<StringBuilder> getHalfWidthSummaryReportAsLaTeXTables(int maxRows, 
            double confLevel) {
        List<StringBuilder> list = getHalfWidthSummaryReportAsLaTeXTabular(maxRows, confLevel);
        String hline = "\\hline";
        String caption = "\\caption{Half-Width Summary Report} \n";
        String captionc = "\\caption{Half-Width Summary Report (continued)} \n";
        String beginTable = "\\begin{table}[ht] \n";
        String endTable = "\\end{table} \n";
        String centering = "\\centering \n";
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
            sb.append(endTable);
        }

        return list;
    }

    /**
     * List of StringBuilder representing LaTeX tables max 60 rows
     *
     * @return the tables as StringBuilders
     */
    public final List<StringBuilder> getSummaryReportAsLaTeXTables() {
        return getSummaryReportAsLaTeXTables(60);
    }

    /**
     * List of StringBuilder representing LaTeX tables
     *
     * @param maxRows the maximum number of rows in a table
     * @return the tables as StringBuilders
     */
    public final List<StringBuilder> getSummaryReportAsLaTeXTables(int maxRows) {
        List<StringBuilder> list = getSummaryReportAsLaTeXTabular(maxRows);
        String hline = "\\hline";
        String caption = "\\caption{Statistical Summary Report} \n";
        String captionc = "\\caption{Statistical Summary Report (continued)} \n";
        String beginTable = "\\begin{table}[ht] \n";
        String endTable = "\\end{table} \n";
        String centering = "\\centering \n";
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
            sb.append(endTable);
        }

        return list;
    }

    /**
     * Gets all the statistics in comma separated value format (includes header)
     *
     * @return the csv as a StringBuilder
     */
    public final StringBuilder getCSVStatistics() {
        return getCSVStatistics(true);
    }

    /**
     * Gets all the statistics in comma separated value format
     *
     * @param header true means 1st line is header
     * @return the csv as a StringBuilder
     */
    public final StringBuilder getCSVStatistics(boolean header) {
        StringBuilder sb = new StringBuilder();
        if (!myStats.isEmpty()) {
            if (header) {
                sb.append(myStats.get(0).getCSVStatisticHeader());
                sb.append("\n");
            }
        }
        for (StatisticAccessorIfc stat : myStats) {
            sb.append(stat.getCSVStatistic());
            sb.append("\n");
        }
        return sb;
    }
}
