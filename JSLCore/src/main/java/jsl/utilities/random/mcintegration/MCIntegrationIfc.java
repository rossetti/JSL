package jsl.utilities.random.mcintegration;

import jsl.utilities.statistic.Statistic;

/**
 *  Provides an interface for algorithms that perform Monte-Carlo Integration
 *  See the abstract base class MCIntegration for further information.
 */
public interface MCIntegrationIfc {
    /**
     * @param level the desired confidence level
     */
    void setConfidenceLevel(double level);

    /**
     * @return true if the relative error meets the desired with the appropriate level of confidence
     */
    boolean checkStoppingCriteria();

    /**
     * @return the number of samples needed to meet the half-width error criteria with the specified level of confidence
     */
    double estimateSampleSize();

    /**
     * See page 513 of Law & Kelton
     *
     * @param relativeError a relative error bound
     * @return the recommended sample size
     */
    double estimateSampleSizeForRelativeError(double relativeError);

    /**
     * @return the estimated integral
     */
    double evaluate();

    /**
     * @return the sampling statistics
     */
    Statistic getStatistic();

    /**
     * The purpose of the initial sample is to estimate the variability
     * and determine an approximate number of additional samples needed
     * to meet the desired absolute error.
     *
     * @return the number of additional samples needed
     */
    double runInitialSample();

    int getInitialSampleSize();

    void setInitialSampleSize(int initialSampleSize);

    long getMaxSampleSize();

    void setMaxSampleSize(int maxSampleSize);

    double getDesiredAbsError();

    void setDesiredAbsError(double desiredAbsError);

    boolean isResetStreamOptionOn();

    void setResetStreamOption(boolean resetStreamOptionOn);

}
