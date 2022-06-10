package jsl.utilities.statistic;

/**
 * A general interface that represents some mechanism that will produce an estimate
 * of a population mean.  The key functionality is that the estimate can be combined
 * with some other estimator of the mean of the population to produce a new estimate.
 * This interface does not assume how the new estimate is generated. Only, rather,
 * it assumes that a new estimate can be produced.
 * 
 */
public interface MeanEstimateIfc extends EstimatorIfc {

    /**
     * Combines the supplied estimate with the current estimate to produce a new estimate.
     *
     * @param estimate an estimate of the mean to combine with the current estimate
     * @return a new mean estimate based on the current and supplied
     */
    MeanEstimateIfc combine(MeanEstimatorIfc estimate);

}
