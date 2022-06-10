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

import jsl.utilities.Interval;

import java.util.*;

/**
 * The StatisticAccessIfc class presents a read-only view of a Statistic
 */
public interface StatisticAccessorIfc extends SummaryStatisticsIfc, GetCSVStatisticIfc {

    /**
     * Gets the name of the Statistic
     *
     * @return The name as a String
     */
    String getName();

    /**
     * Gets the sum of the observations.
     *
     * @return A double representing the unweighted sum
     */
    double getSum();

    /**
     * Gets the sum of squares of the deviations from the average This is the
     * numerator in the classic sample variance formula
     *
     * @return A double representing the sum of squares of the deviations from
     * the average
     */
    double getDeviationSumOfSquares();

    /**
     * Gets the minimum of the observations.
     *
     * @return A double representing the minimum
     */
    double getMin();

    /**
     * Gets the maximum of the observations.
     *
     * @return A double representing the maximum
     */
    double getMax();

    /**
     * Gets the last observed data point
     *
     * @return A double representing the last observations
     */
    double getLastValue();

    /**
     * Gets the kurtosis of the data
     *
     * @return A double representing the kurtosis
     */
    double getKurtosis();

    /**
     * Gets the skewness of the data
     *
     * @return A double representing the skewness
     */
    double getSkewness();

    /**
     * Gets the standard error of the observations. Simply the generate standard
     * deviation divided by the square root of the number of observations
     *
     * @return A double representing the standard error or Double.NaN if &lt; 1
     * observation
     */
    double getStandardError();

    /**
     * Gets the confidence interval half-width. Simply the generate standard error
     * times the confidence coefficient
     *
     * @return A double representing the half-width or Double.NaN if &lt; 1
     * observation
     */
    default double getHalfWidth() {
        return getHalfWidth(getConfidenceLevel());
    }

    /**
     * Gets the confidence interval half-width. Simply the generate standard error
     * times the confidence coefficient as determined by an appropriate sampling
     * distribution
     *
     * @param level the confidence level
     * @return A double representing the half-width or Double.NaN if &lt; 1
     * observation
     */
    double getHalfWidth(double level);

    /**
     * Gets the confidence level. The default is given by
     * Statistic.DEFAULT_CONFIDENCE_LEVEL = 0.95, which is a 95% confidence
     * level
     *
     * @return A double representing the confidence level
     */
    double getConfidenceLevel();

    /**
     * A confidence interval for the mean based on the confidence level
     *
     * @return the interval
     */
    default Interval getConfidenceInterval() {
        return getConfidenceInterval(getConfidenceLevel());
    }

    /**
     * A confidence interval for the mean based on the confidence level
     *
     * @param level the confidence level
     * @return the interval
     */
    default Interval getConfidenceInterval(double level) {
        if (getCount() < 1.0) {
            return new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        double hw = getHalfWidth(level);
        double avg = getAverage();
        double ll = avg - hw;
        double ul = avg + hw;
        return new Interval(ll, ul);
    }

    /**
     * Returns the relative error: getStandardError() / getAverage()
     *
     * @return the relative error
     */
    default double getRelativeError() {
        if (getAverage() == 0.0) {
            return Double.POSITIVE_INFINITY;
        } else {
            return getStandardError() / getAverage();
        }
    }

    /**
     * Returns the relative width of the default confidence interval: 2.0 *
     * getHalfWidth() / getAverage()
     *
     * @return the relative width
     */
    default double getRelativeWidth() {
        return getRelativeWidth(getConfidenceLevel());
    }

    /**
     * Returns the relative width of the level of the confidence interval: 2.0 *
     * getHalfWidth(level) / getAverage()
     *
     * @param level the confidence level
     * @return the relative width for the level
     */
    default double getRelativeWidth(double level) {
        if (getAverage() == 0.0) {
            return Double.POSITIVE_INFINITY;
        } else {
            return 2.0 * getHalfWidth(level) / getAverage();
        }
    }

    /**
     * Gets the lag-1 generate covariance of the unweighted observations. Note:
     * See Box, Jenkins, Reinsel, Time Series Analysis, 3rd edition,
     * Prentice-Hall, pg 31
     *
     * @return A double representing the generate covariance or Double.NaN if
     * &lt;=2 observations
     */
    double getLag1Covariance();

    /**
     * Gets the lag-1 generate correlation of the unweighted observations. Note:
     * See Box, Jenkins, Reinsel, Time Series Analysis, 3rd edition,
     * Prentice-Hall, pg 31
     *
     * @return A double representing the generate correlation or Double.NaN if
     * &lt;=2 observations
     */
    double getLag1Correlation();

    /**
     * Gets the Von Neumann Lag 1 test statistic for checking the hypothesis
     * that the data are uncorrelated Note: See Handbook of Simulation, Jerry
     * Banks editor, McGraw-Hill, pg 253.
     *
     * @return A double representing the Von Neumann test statistic
     */
    double getVonNeumannLag1TestStatistic();

    /**
     * Returns the asymptotic p-value for the Von Nueumann Lag-1 Test Statistic:
     * <p>
     * Normal.stdNormalComplementaryCDF(getVonNeumannLag1TestStatistic());
     *
     * @return the p-value
     */
    double getVonNeumannLag1TestStatisticPValue();

    /**
     * When a data point having the value of (Double.NaN,
     * Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY) are presented it is
     * excluded from the summary statistics and the number of missing points is
     * noted. This method reports the number of missing points that occurred
     * during the collection
     *
     * @return the number missing
     */
    double getNumberMissing();

    /**
     * Computes the right most meaningful digit according to
     * (int)Math.floor(Math.log10(a*getStandardError())) See doi
     * 10.1287.opre.1080.0529 by Song and Schmeiser
     *
     * @param a the std error multiplier
     * @return the meaningful digit
     */
    int getLeadingDigitRule(double a);

    /**
     * Returns a String representation of the Statistic
     *
     * @return A String with basic summary statistics
     */
    @Override
    String toString();

    /**
     * Fills up an array with the statistics defined by this interface
     * statistics[0] = getCount()
     * statistics[1] = getAverage()
     * statistics[2] = getStandardDeviation()
     * statistics[3] = getStandardError()
     * statistics[4] = getHalfWidth()
     * statistics[5] = getConfidenceLevel()
     * statistics[6] = getMin()
     * statistics[7] = getMax()
     * statistics[8] = getSum()
     * statistics[9] = getVariance()
     * statistics[10] = getDeviationSumOfSquares()
     * statistics[11] = getLastValue()
     * statistics[12] = getKurtosis()
     * statistics[13] = getSkewness()
     * statistics[14] = getLag1Covariance()
     * statistics[15] = getLag1Correlation()
     * statistics[16] = getVonNeumannLag1TestStatistic()
     * statistics[17] = getNumberMissing()
     *
     * @return an array of values
     */
    default double[] getStatistics() {
        double[] statistics = new double[18];
        statistics[0] = getCount();
        statistics[1] = getAverage();
        statistics[2] = getStandardDeviation();
        statistics[3] = getStandardError();
        statistics[4] = getHalfWidth();
        statistics[5] = getConfidenceLevel();
        statistics[6] = getMin();
        statistics[7] = getMax();
        statistics[8] = getSum();
        statistics[9] = getVariance();
        statistics[10] = getDeviationSumOfSquares();
        statistics[11] = getLastValue();
        statistics[12] = getKurtosis();
        statistics[13] = getSkewness();
        statistics[14] = getLag1Covariance();
        statistics[15] = getLag1Correlation();
        statistics[16] = getVonNeumannLag1TestStatistic();
        statistics[17] = getNumberMissing();
        return statistics;
    }

    @Override
    default String getCSVStatisticHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistic Name,");
        sb.append("Count,");
        sb.append("Average,");
        sb.append("Standard Deviation,");
        sb.append("Standard Error,");
        sb.append("Half-width,");
        sb.append("Confidence Level,");
        sb.append("Minimum,");
        sb.append("Maximum,");
        sb.append("Sum,");
        sb.append("Variance,");
        sb.append("Deviation Sum of Squares,");
        sb.append("Last value collected,");
        sb.append("Kurtosis,");
        sb.append("Skewness,");
        sb.append("Lag 1 Covariance,");
        sb.append("Lag 1 Correlation,");
        sb.append("Von Neumann Lag 1 Test Statistic,");
        sb.append("Number of missing observations");
        return sb.toString();
    }

    @Override
    default String getCSVStatistic() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(",");
        double[] stats = getStatistics();
        for (int i = 0; i < stats.length; i++) {
            if (Double.isNaN(stats[i]) || Double.isInfinite(stats[i])) {
                sb.append("");
            } else {
                sb.append(stats[i]);
            }
            if (i < stats.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * Returns the values of all the statistics as a list of strings
     * The name is the first string
     *
     * @return the values of all the statistics as a list of strings
     */
    default List<String> getCSVValues() {
        List<String> sb = new ArrayList<String>();
        sb.add(getName());
        double[] stats = getStatistics();
        for (int i = 0; i < stats.length; i++) {
            if (Double.isNaN(stats[i]) || Double.isInfinite(stats[i])) {
                sb.add("");
            } else {
                sb.add(Double.toString(stats[i]));
            }
        }
        return sb;
    }

    /**
     * Gets the CSV header values as a list of strings
     *
     * @return the CSV header values as a list of strings
     */
    default List<String> getCSVHeader() {
        List<String> sb = new ArrayList<String>();
        sb.add("Statistic Name");
        sb.add("Count");
        sb.add("Average");
        sb.add("Standard Deviation");
        sb.add("Standard Error");
        sb.add("Half-width");
        sb.add("Confidence Level");
        sb.add("Minimum");
        sb.add("Maximum");
        sb.add("Sum");
        sb.add("Variance");
        sb.add("Deviation Sum of Squares");
        sb.add("Last value collected");
        sb.add("Kurtosis");
        sb.add("Skewness");
        sb.add("Lag 1 Covariance");
        sb.add("Lag 1 Correlation");
        sb.add("Von Neumann Lag 1 Test Statistic");
        sb.add("Number of missing observations");
        return sb;
    }

    /**
     * Fills the map with the values of the statistics. Key is statistic label
     * and value is the value of the statistic. The keys are:
     *  "Count"
     *  "Average"
     *  "Standard Deviation"
     *  "Standard Error"
     *  "Half-width"
     *  "Confidence Level"
     *  "Minimum"
     *  "Maximum"
     *  "Sum"
     *  "Variance"
     *  "Deviation Sum of Squares"
     *  "Last value collected"
     *  "Kurtosis"
     *  "Skewness"
     *  "Lag 1 Covariance"
     *  "Lag 1 Correlation"
     *  "Von Neumann Lag 1 Test Statistic"
     *  "Number of missing observations"
     */
    default Map<String, Double> getStatisticsAsMap() {
        Map<String, Double> stats = new LinkedHashMap<>();
        stats.put("Count", getCount());
        stats.put("Average", getAverage());
        stats.put("Standard Deviation", getStandardDeviation());
        stats.put("Standard Error", getStandardError());
        stats.put("Half-width", getHalfWidth());
        stats.put("Confidence Level", getConfidenceLevel());
        stats.put("Minimum", getMin());
        stats.put("Maximum", getMax());
        stats.put("Sum", getSum());
        stats.put("Variance", getVariance());
        stats.put("Deviation Sum of Squares", getDeviationSumOfSquares());
        stats.put("Last value collected", getLastValue());
        stats.put("Kurtosis", getKurtosis());
        stats.put("Skewness", getSkewness());
        stats.put("Lag 1 Covariance", getLag1Covariance());
        stats.put("Lag 1 Correlation", getLag1Correlation());
        stats.put("Von Neumann Lag 1 Test Statistic", getVonNeumannLag1TestStatistic());
        stats.put("Number of missing observations", getNumberMissing());
        return stats;
    }
}
