package jsl.utilities.statistic;

import jsl.utilities.GetTimeIfc;

import java.util.Objects;

public class TimeWeightedStatistic extends AbstractCollector implements WeightedStatisticIfc {

    private final GetTimeIfc myTimeGetter;
    private double myLastValue;
    private double myLastTime;
    private final WeightedStatistic myWeightedStatistic;

    public TimeWeightedStatistic(GetTimeIfc timeGetter){
        this(timeGetter, 0.0, 0.0);
    }

    public TimeWeightedStatistic(GetTimeIfc timeGetter, double initialValue){
        this(timeGetter, initialValue, 0.0);
    }

    public TimeWeightedStatistic(GetTimeIfc timeGetter, double initialValue, double initialTime){
        Objects.requireNonNull(timeGetter, "The GetTimeIfc instance was null");
        myTimeGetter = timeGetter;
        myWeightedStatistic = new WeightedStatistic();
        myLastTime = initialTime;
        myLastValue = initialValue;
    }

    @Override
    public void collect(double value){
        double time = myTimeGetter.getTime();
        double weight = time - myLastTime;
        myLastTime = time;
        myWeightedStatistic.collect(myLastValue, weight);
        myLastValue = value;
    }

    @Override
    public void reset() {
        myWeightedStatistic.reset();
        myLastTime = myTimeGetter.getTime();
    }

    @Override
    public double getLastValue() {
        return myWeightedStatistic.getLastValue();
    }

    @Override
    public double getLastWeight() {
        return myWeightedStatistic.getLastWeight();
    }

    @Override
    public double getAverage() {
        return myWeightedStatistic.getAverage();
    }

    @Override
    public double getCount() {
        return myWeightedStatistic.getCount();
    }

    @Override
    public double getWeightedSum() {
        return myWeightedStatistic.getWeightedSum();
    }

    @Override
    public double getSumOfWeights() {
        return myWeightedStatistic.getSumOfWeights();
    }

    @Override
    public double getWeightedSumOfSquares() {
        return myWeightedStatistic.getWeightedSumOfSquares();
    }

    @Override
    public double getMin() {
        return myWeightedStatistic.getMin();
    }

    @Override
    public double getMax() {
        return myWeightedStatistic.getMax();
    }

    @Override
    public double getNumberMissing() {
        return myWeightedStatistic.getNumberMissing();
    }

    @Override
    public double getUnWeightedSum() {
        return myWeightedStatistic.getUnWeightedSum();
    }

    public void getStatistics(double[] statistics) {
        myWeightedStatistic.getStatistics(statistics);
    }

    public double[] getStatistics() {
        return myWeightedStatistic.getStatistics();
    }
    
    public String[] getStatisticsHeader() {
        return myWeightedStatistic.getStatisticsHeader();
    }

    @Override
    public String getCSVStatistic() {
        return myWeightedStatistic.getCSVStatistic();
    }

    @Override
    public String getCSVStatisticHeader() {
        return myWeightedStatistic.getCSVStatisticHeader();
    }
}
