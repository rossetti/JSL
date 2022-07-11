package jsl.utilities.statistic;

import jsl.utilities.GetTimeIfc;

import java.util.Objects;

public class TimeWeightedStatistic extends AbstractCollector implements WeightedStatisticIfc {

    private final GetTimeIfc myTimeGetter;
    private double myLastValue;
    private double myLastTime;
    private final WeightedStatistic myWeightedStatistic;
    public boolean updateTimeAtReset = true;

    public TimeWeightedStatistic(GetTimeIfc timeGetter){
        this(timeGetter, 0.0, 0.0);
    }

    public TimeWeightedStatistic(GetTimeIfc timeGetter, double initialValue){
        this(timeGetter, initialValue, 0.0);
    }

    public TimeWeightedStatistic(GetTimeIfc timeGetter, double initialValue, double initialTime){
        Objects.requireNonNull(timeGetter, "The GetTimeIfc instance was null");
        if (initialTime < 0.0){
            throw new IllegalArgumentException("The initial time must be >= 0.0");
        }
        if (initialValue < 0.0){
            throw new IllegalArgumentException("The initial value must be >= 0.0");
        }
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
//        System.out.println("time = " + time + " value = " + value + " last  = " + myLastValue + " weight = " + weight);
        myWeightedStatistic.collect(myLastValue, weight);
        myLastValue = value;
    }

    @Override
    public void reset() {
        myWeightedStatistic.reset();
        if (updateTimeAtReset){
            myLastTime = myTimeGetter.getTime();
        }
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

    @Override
    public String toString(){
        return myWeightedStatistic.toString();
    }

    public static void main(String[] args) {
        double[] t = {0, 2, 5, 11, 14, 17, 22, 26, 28, 31, 35, 36};
        double[] n = {0, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0, 0};

        TimeWeightedStatistic tws = new TimeWeightedStatistic(new TimeArray(t));
        for(double x: n){
            tws.collect(x);
        }
        System.out.println(tws);

    }

    public static class TimeArray implements GetTimeIfc {

        double[] time;
        int index = -1;

        public TimeArray(double[] time){
            this.time = time;
        }

        @Override
        public double getTime() {
            if (index < time.length - 1){
                index = index + 1;
                return time[index];
            }
            return time[index];
        }

        public void reset(){
            index = -1;
        }
    }
}
