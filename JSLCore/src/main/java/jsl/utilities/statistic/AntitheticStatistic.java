package jsl.utilities.statistic;

/**
 * In progress...
 */
public class AntitheticStatistic extends AbstractStatistic {

    private final Statistic myStatistic;
    private double myOddValue;

    /**
     *
     */
    public AntitheticStatistic() {
        this(null);
    }

    /**
     * @param name the name of the statistic
     */
    public AntitheticStatistic(String name) {
        super(name);
        myStatistic = new Statistic(name);

    }

    @Override
    public void collect(double x) {
        if((myStatistic.getCount() % 2) == 0){// even
            double avg = (x + myOddValue)/2.0;
            collect(avg);
        } else {
            myOddValue = x; // save the odd value
        }
    }

    @Override
    public void reset() {
        myStatistic.reset();
    }

    @Override
    public double getCount() {
        return myStatistic.getCount();
    }

    @Override
    public double getSum() {
        return myStatistic.getSum();
    }

    @Override
    public double getAverage() {
        return myStatistic.getAverage();
    }

    @Override
    public double getDeviationSumOfSquares() {
        return myStatistic.getDeviationSumOfSquares();
    }

    @Override
    public double getVariance() {
        return myStatistic.getVariance();
    }

    @Override
    public double getStandardDeviation() {
        return myStatistic.getStandardDeviation();
    }

    @Override
    public double getMin() {
        return myStatistic.getMin();
    }

    @Override
    public double getMax() {
        return myStatistic.getMax();
    }

    @Override
    public double getLastValue() {
        return myStatistic.getLastValue();
    }

    @Override
    public double getKurtosis() {
        return myStatistic.getKurtosis();
    }

    @Override
    public double getSkewness() {
        return myStatistic.getSkewness();
    }

    @Override
    public double getStandardError() {
        return myStatistic.getStandardError();
    }

    @Override
    public double getLag1Covariance() {
        return myStatistic.getLag1Covariance();
    }

    @Override
    public double getLag1Correlation() {
        return myStatistic.getLag1Correlation();
    }

    @Override
    public double getVonNeumannLag1TestStatistic() {
        return myStatistic.getVonNeumannLag1TestStatistic();
    }

    @Override
    public double getVonNeumannLag1TestStatisticPValue() {
        return myStatistic.getVonNeumannLag1TestStatisticPValue();
    }

    @Override
    public int getLeadingDigitRule(double a) {
        return myStatistic.getLeadingDigitRule(a);
    }

    @Override
    public double getHalfWidth() {
        return myStatistic.getHalfWidth();
    }

    @Override
    public double getHalfWidth(double level) {
        return myStatistic.getHalfWidth(level);
    }

    public boolean checkMean(double mean) {
        return myStatistic.checkMean(mean);
    }

    public double getObsWeightedSum() {
        return myStatistic.getObsWeightedSum();
    }

    public String asString() {
        return myStatistic.asString();
    }

    public String getSummaryStatistics() {
        return myStatistic.getSummaryStatistics();
    }

    public String getSummaryStatisticsHeader() {
        return myStatistic.getSummaryStatisticsHeader();
    }

    public long estimateSampleSize(double desiredHW) {
        return myStatistic.estimateSampleSize(desiredHW);
    }

}
