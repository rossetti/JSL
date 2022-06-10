package jsl.utilities.statistic;

public interface SummaryStatisticsIfc extends MeanEstimatorIfc{

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
}
