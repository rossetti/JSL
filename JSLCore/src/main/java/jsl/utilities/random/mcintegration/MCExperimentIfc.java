package jsl.utilities.random.mcintegration;

import jsl.utilities.statistic.Statistic;

/**
 *  Provides an interface for algorithms that perform Monte-Carlo experiments
 *  See the abstract base class MCExperiment for further information.
 */
public interface MCExperimentIfc extends MCExperimentSetUpIfc {

    /**
     * @return true if the relative error meets the desired with the appropriate level of confidence
     */
    boolean checkStoppingCriteria();

    /**
     * @return the number of samples needed to meet the half-width error criteria with the specified level of confidence
     */
    double estimateSampleSize();

    /**
     * See page 513 of Law and Kelton
     *
     * @param relativeError a relative error bound
     * @return the recommended sample size
     */
    double estimateSampleSizeForRelativeError(double relativeError);

    /**
     * @return the estimated result of the simulation
     */
    double runSimulation();

    /**
     * @return the sampling statistics
     */
    Statistic getStatistic();

    /**
     * The purpose of the initial sample is to estimate the variability
     * and determine an approximate number of additional samples needed
     * to meet the desired absolute error. This method must ensure
     * or assumes that no previous sampling has occurred. All
     * statistical accumulators should be reset when this is executed.
     *
     * @return the number of additional samples needed
     */
    double runInitialSample();

}
