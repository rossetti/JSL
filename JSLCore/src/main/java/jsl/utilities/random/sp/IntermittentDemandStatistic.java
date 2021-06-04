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
package jsl.utilities.random.sp;

import jsl.observers.ObserverIfc;
import jsl.utilities.DataObservable;

import jsl.utilities.IdentityIfc;
import jsl.utilities.Interval;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticAccessorIfc;

/**
 * Implements the statistics estimator for an intermittent demand scenario
 * @author vvarghe
 *
 */
public class IntermittentDemandStatistic implements StatisticAccessorIfc, IdentityIfc, ObserverIfc {

    protected int myId;

    protected String myName;

    private static int myIdCounter_;

    private double myDemand;

    private Statistic myNZDemandStat;

    private Statistic myDemandStat;

    private Statistic myIntervalBetweenNonZeroDemandsStat;

    private Statistic myIntervalBetweenZeroDemandsStat;

    /*
     * This can be true only after the first the demand state is observed: zero or non-zero
     *
     */
    private boolean TRANSITION_PROB_ESTIMATION_CAN_BE_INITIALIZED;

    private int myPreviousEvent;

    private int numSuccess;

    private int numFailure;

    private int numSuccessAfterSuccess;

    private int numSuccessAfterFailure;

    private int myTimeSpanSinceLastDemand;

    private int myTimeSpanSinceLastZeroDemand;

    private int myTimeIndexofLastDemand;

    private int myTimeIndexofLastZeroDemand;

    private boolean INTERVAL_BTWN_NONZERODEMANDS_INITIALISED;

    private boolean INTERVAL_BTWN_ZERODEMANDS_INITIALISED;

    private int numTransBefore_IntvlBtwnNonZeroDemandsStatInitialized;

    private int numTransBefore_IntvlBtwnZeroDemandsStatInitialized;

    private int myCurrentTimeIndex = 0;

    public IntermittentDemandStatistic() {
        this(null);
    }

    public IntermittentDemandStatistic(String name) {
        setId();
        setName(name);

        myNZDemandStat = new Statistic("Non-zero demand series");
        myDemandStat = new Statistic("Demand series");
        myIntervalBetweenNonZeroDemandsStat = new Statistic("Interval between non zero demands");
        myIntervalBetweenZeroDemandsStat = new Statistic("Interval between zero demands");
        //myNZDemandStat.setSaveDataOption(true);
        //myDemandStat.setSaveDataOption(true);
        //myIntervalBetweenNonZeroDemandsStat.setSaveDataOption(true);
        //myIntervalBetweenZeroDemandsStat.setSaveDataOption(true);

        TRANSITION_PROB_ESTIMATION_CAN_BE_INITIALIZED = false;
        numSuccess = 0;
        numFailure = 0;
        numSuccessAfterSuccess = 0;
        numSuccessAfterFailure = 0;

        INTERVAL_BTWN_NONZERODEMANDS_INITIALISED = false;
        INTERVAL_BTWN_ZERODEMANDS_INITIALISED = false;
    }

    public void reset() {
        myNZDemandStat.reset();
        myDemandStat.reset();
        myIntervalBetweenNonZeroDemandsStat.reset();
        myIntervalBetweenZeroDemandsStat.reset();

        myCurrentTimeIndex = 0;

        TRANSITION_PROB_ESTIMATION_CAN_BE_INITIALIZED = false;
        numSuccess = 0;
        numFailure = 0;
        numSuccessAfterSuccess = 0;
        numSuccessAfterFailure = 0;

        INTERVAL_BTWN_NONZERODEMANDS_INITIALISED = false;
        INTERVAL_BTWN_ZERODEMANDS_INITIALISED = false;

        myPreviousEvent = 0;

        myTimeSpanSinceLastDemand = 0;
        myTimeSpanSinceLastZeroDemand = 0;
        myTimeIndexofLastDemand = 0;
        myTimeIndexofLastZeroDemand = 0;

        numTransBefore_IntvlBtwnNonZeroDemandsStatInitialized = 0;
        numTransBefore_IntvlBtwnZeroDemandsStatInitialized = 0;

    }

    public void setSaveDataOption(boolean flag) {
        myNZDemandStat.setSaveOption(flag);
        myDemandStat.setSaveOption(flag);
        myIntervalBetweenNonZeroDemandsStat.setSaveOption(flag);
        myIntervalBetweenZeroDemandsStat.setSaveOption(flag);
    }

    protected void setId() {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
    }

    public final void setName(String str) {
        if (str == null) {
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            myName = s;
        } else {
            myName = str;
        }
    }

    @Override
    public String getName() {
        return myName;
    }

    @Override
    public int getId() {
        return myId;
    }

    /**
     * 
     * @param o
     * @param arg
     */
    @Override
    public void update(Object o, Object arg) {
        DataObservable ds = (DataObservable) o;
        double forecastValue = ds.getValue();
        if (!Double.isNaN(forecastValue)) {
            collect(forecastValue);
        }
    }

    public void collect(double[] data) {

        for (double d : data) {
            this.collect(d);
        }

    }

    public void collect(double data) {
        myDemand = data;
        if (myDemand > 0) {
            numSuccess++;
            if (TRANSITION_PROB_ESTIMATION_CAN_BE_INITIALIZED == true) {
                if (myPreviousEvent != 0) {
                    numSuccessAfterSuccess++;
                } else {
                    numSuccessAfterFailure++;
                }
            }
            myPreviousEvent = 1;
            //myNZDemandStat.collect(myDemand);
            //myDemandStat.collect(myDemand);
            myNZDemandStat.collect(myDemand);
            myDemandStat.collect(myDemand);
            TRANSITION_PROB_ESTIMATION_CAN_BE_INITIALIZED = true;
            setIntervalBetweenNonZeroDemands(myCurrentTimeIndex);
            setIntervalBetweenZeroDemands(myCurrentTimeIndex);
        } else {
            myPreviousEvent = 0;
            numFailure++;
            myDemand = 0;
            //myDemandStat.collect(myDemand);
            myDemandStat.collect(myDemand);
            TRANSITION_PROB_ESTIMATION_CAN_BE_INITIALIZED = true;
            setIntervalBetweenNonZeroDemands(myCurrentTimeIndex);
            setIntervalBetweenZeroDemands(myCurrentTimeIndex);
        }
        myCurrentTimeIndex++;
    }

    public double[] getNonZeroDemandsData() {
        return this.myNZDemandStat.getSavedData();
    }

    public double[] getDemandsData() {
        return this.myDemandStat.getSavedData();
    }

    public double[] getIntervalBetweenNonZeroDemandsData() {
        return this.myIntervalBetweenNonZeroDemandsStat.getSavedData();
    }

    public double[] getIntervalBetweenZeroDemandsData() {
        return this.myIntervalBetweenZeroDemandsStat.getSavedData();
    }

    public int getNumTransactions() {
        return (int) myNZDemandStat.getCount();
    }

    private void setIntervalBetweenNonZeroDemands(int myCurrentTimeIndex) {
        if (INTERVAL_BTWN_NONZERODEMANDS_INITIALISED == false) {
            if (myDemand != 0) {
                numTransBefore_IntvlBtwnNonZeroDemandsStatInitialized++;
                if (numTransBefore_IntvlBtwnNonZeroDemandsStatInitialized == 2) {
                    INTERVAL_BTWN_NONZERODEMANDS_INITIALISED = true;
                    myTimeSpanSinceLastDemand = myCurrentTimeIndex - myTimeIndexofLastDemand;
                    //myIntervalBetweenNonZeroDemandsStat.collect(myTimeSpanSinceLastDemand);
                    myIntervalBetweenNonZeroDemandsStat.collect(myTimeSpanSinceLastDemand);
                }
                myTimeIndexofLastDemand = myCurrentTimeIndex;
            }
        } else {
            if (myDemand != 0) {
                numTransBefore_IntvlBtwnNonZeroDemandsStatInitialized++;
                myTimeSpanSinceLastDemand = myCurrentTimeIndex - myTimeIndexofLastDemand;
                //myIntervalBetweenNonZeroDemandsStat.collect(myTimeSpanSinceLastDemand);
                myIntervalBetweenNonZeroDemandsStat.collect(myTimeSpanSinceLastDemand);
                myTimeIndexofLastDemand = myCurrentTimeIndex;
            }
        }
    }

    private void setIntervalBetweenZeroDemands(int myCurrentTimeIndex) {
        if (INTERVAL_BTWN_ZERODEMANDS_INITIALISED == false) {
            if (myDemand == 0) {
                numTransBefore_IntvlBtwnZeroDemandsStatInitialized++;
                if (numTransBefore_IntvlBtwnZeroDemandsStatInitialized == 2) {
                    INTERVAL_BTWN_ZERODEMANDS_INITIALISED = true;
                    myTimeSpanSinceLastZeroDemand = myCurrentTimeIndex - myTimeIndexofLastZeroDemand;
                    //myIntervalBetweenZeroDemandsStat.collect(myTimeSpanSinceLastZeroDemand);
                    myIntervalBetweenZeroDemandsStat.collect(myTimeSpanSinceLastZeroDemand);
                }
                myTimeIndexofLastZeroDemand = myCurrentTimeIndex;
            }
        } else {
            if (myDemand == 0) {
                numTransBefore_IntvlBtwnZeroDemandsStatInitialized++;
                myTimeSpanSinceLastZeroDemand = myCurrentTimeIndex - myTimeIndexofLastZeroDemand;
                //myIntervalBetweenZeroDemandsStat.collect(myTimeSpanSinceLastZeroDemand);
                myIntervalBetweenZeroDemandsStat.collect(myTimeSpanSinceLastZeroDemand);
                myTimeIndexofLastZeroDemand = myCurrentTimeIndex;
            }
        }
    }

    public Statistic getIntervalBetweenNonZeroDemandStat() {
        return myIntervalBetweenNonZeroDemandsStat;
    }

    public Statistic getIntervalBetweenZeroDemandsStat() {
        return myIntervalBetweenZeroDemandsStat;
    }

    public Statistic getNonZeroDemandStat() {
        return myNZDemandStat;
    }

    public Statistic getDemandStat() {
        return myDemandStat;
    }

    public double getProbSuccessAfterSuccess() {
        return (double) (numSuccessAfterSuccess) / (double) numSuccess;
    }

    public double getProbSuccessAfterFailure() {
        return (double) numSuccessAfterFailure / (double) numFailure;
    }

    public double getProbSuccess() {
        return (double) numSuccess / (double) (numSuccess + numFailure);
    }

    public double getProbFailure() {
        return (double) numFailure / (double) (numSuccess + numFailure);
    }

    @Override
    public double getAverage() {
        return myDemandStat.getAverage();
    }

    @Override
    public double getConfidenceLevel() {
        return myDemandStat.getConfidenceLevel();
    }

    @Override
    public double getCount() {
        return myDemandStat.getCount();
    }

    @Override
    public double getDeviationSumOfSquares() {
        return myDemandStat.getDeviationSumOfSquares();
    }

    @Override
    public double getHalfWidth(double alpha) {
        return myDemandStat.getHalfWidth(alpha);
    }

    @Override
    public double getHalfWidth() {
        return myDemandStat.getHalfWidth();
    }
    
    public Interval getConfidenceInterval(double alpha) {
        return myDemandStat.getConfidenceInterval(alpha);
    }

    @Override
    public Interval getConfidenceInterval() {
        return myDemandStat.getConfidenceInterval();
    }

    @Override
    public double getKurtosis() {
        return myDemandStat.getKurtosis();
    }

    @Override
    public double getLag1Correlation() {
        return myDemandStat.getLag1Correlation();
    }

    @Override
    public double getLag1Covariance() {
        return myDemandStat.getLag1Covariance();
    }

    @Override
    public double getLastValue() {
        return myDemandStat.getLastValue();
    }

    @Override
    public double getMax() {
        return myDemandStat.getMax();
    }

    @Override
    public double getRelativeError() {
        return myDemandStat.getRelativeError();
    }

    @Override
    public double getRelativeWidth() {
        return myDemandStat.getRelativeWidth();
    }

    @Override
    public double getRelativeWidth(double level) {
        return myDemandStat.getRelativeWidth(level);
    }

    @Override
    public double getMin() {
        return myDemandStat.getMin();
    }

    @Override
    public double getNumberMissing() {
        return myDemandStat.getNumberMissing();
    }

    @Override
    public double getSkewness() {
        return myDemandStat.getSkewness();
    }

    @Override
    public double getStandardDeviation() {
        return myDemandStat.getStandardDeviation();
    }

    @Override
    public double getStandardError() {
        return myDemandStat.getStandardError();
    }

    @Override
    public final int getLeadingDigitRule(double a) {
        return myDemandStat.getLeadingDigitRule(a);
    }

    @Override
    public double[] getStatistics() {
        double[] statistics = new double[67];
        statistics[0] = getCount();
        statistics[1] = getAverage();
        statistics[2] = getVariance();
        statistics[3] = getStandardDeviation();
        statistics[4] = getStandardError();
        statistics[5] = getMin();
        statistics[6] = getMax();
        statistics[7] = getSum();
        statistics[8] = getDeviationSumOfSquares();
        statistics[9] = getLastValue();
        statistics[10] = getKurtosis();
        statistics[11] = getSkewness();
        statistics[12] = getLag1Correlation();
        statistics[13] = getLag1Covariance();
        statistics[14] = getVonNeumannLag1TestStatistic();
        statistics[15] = getNumberMissing();

        statistics[16] = myNZDemandStat.getCount();
        statistics[17] = myNZDemandStat.getAverage();
        statistics[18] = myNZDemandStat.getVariance();
        statistics[19] = myNZDemandStat.getStandardDeviation();
        statistics[20] = myNZDemandStat.getStandardError();
        statistics[21] = myNZDemandStat.getMin();
        statistics[22] = myNZDemandStat.getMax();
        statistics[23] = myNZDemandStat.getSum();
        statistics[24] = myNZDemandStat.getDeviationSumOfSquares();
        statistics[25] = myNZDemandStat.getLastValue();
        statistics[26] = myNZDemandStat.getKurtosis();
        statistics[27] = myNZDemandStat.getSkewness();
        statistics[28] = myNZDemandStat.getLag1Correlation();
        statistics[29] = myNZDemandStat.getLag1Covariance();
        statistics[30] = myNZDemandStat.getVonNeumannLag1TestStatistic();
        statistics[31] = myNZDemandStat.getNumberMissing();

        statistics[32] = myIntervalBetweenNonZeroDemandsStat.getCount();
        statistics[33] = myIntervalBetweenNonZeroDemandsStat.getAverage();
        statistics[34] = myIntervalBetweenNonZeroDemandsStat.getVariance();
        statistics[35] = myIntervalBetweenNonZeroDemandsStat.getStandardDeviation();
        statistics[36] = myIntervalBetweenNonZeroDemandsStat.getStandardError();
        statistics[37] = myIntervalBetweenNonZeroDemandsStat.getMin();
        statistics[38] = myIntervalBetweenNonZeroDemandsStat.getMax();
        statistics[39] = myIntervalBetweenNonZeroDemandsStat.getSum();
        statistics[40] = myIntervalBetweenNonZeroDemandsStat.getDeviationSumOfSquares();
        statistics[41] = myIntervalBetweenNonZeroDemandsStat.getLastValue();
        statistics[42] = myIntervalBetweenNonZeroDemandsStat.getKurtosis();
        statistics[43] = myIntervalBetweenNonZeroDemandsStat.getSkewness();
        statistics[44] = myIntervalBetweenNonZeroDemandsStat.getLag1Correlation();
        statistics[45] = myIntervalBetweenNonZeroDemandsStat.getLag1Covariance();
        statistics[46] = myIntervalBetweenNonZeroDemandsStat.getVonNeumannLag1TestStatistic();
        statistics[47] = myIntervalBetweenNonZeroDemandsStat.getNumberMissing();

        statistics[48] = myIntervalBetweenZeroDemandsStat.getCount();
        statistics[49] = myIntervalBetweenZeroDemandsStat.getAverage();
        statistics[50] = myIntervalBetweenZeroDemandsStat.getVariance();
        statistics[51] = myIntervalBetweenZeroDemandsStat.getStandardDeviation();
        statistics[52] = myIntervalBetweenZeroDemandsStat.getStandardError();
        statistics[53] = myIntervalBetweenZeroDemandsStat.getMin();
        statistics[54] = myIntervalBetweenZeroDemandsStat.getMax();
        statistics[55] = myIntervalBetweenZeroDemandsStat.getSum();
        statistics[56] = myIntervalBetweenZeroDemandsStat.getDeviationSumOfSquares();
        statistics[57] = myIntervalBetweenZeroDemandsStat.getLastValue();
        statistics[58] = myIntervalBetweenZeroDemandsStat.getKurtosis();
        statistics[59] = myIntervalBetweenZeroDemandsStat.getSkewness();
        statistics[60] = myIntervalBetweenZeroDemandsStat.getLag1Correlation();
        statistics[61] = myIntervalBetweenZeroDemandsStat.getLag1Covariance();
        statistics[62] = myIntervalBetweenZeroDemandsStat.getVonNeumannLag1TestStatistic();
        statistics[63] = myIntervalBetweenZeroDemandsStat.getNumberMissing();

        statistics[64] = this.getProbSuccessAfterSuccess();
        statistics[65] = this.getProbSuccessAfterFailure();
        statistics[66] = this.getProbFailure();
        return (statistics);
    }

    @Override
    public double getSum() {
        return myDemandStat.getSum();
    }

    @Override
    public double getVariance() {
        return myDemandStat.getVariance();
    }

    @Override
    public double getVonNeumannLag1TestStatistic() {
        return myDemandStat.getVonNeumannLag1TestStatistic();
    }

    @Override
    public final double getVonNeumannLag1TestStatisticPValue() {
        return myDemandStat.getVonNeumannLag1TestStatisticPValue();
    }

    /** NOT IMPLEMENTED YET
     *
     * @return
     */
    @Override
    public String getCSVStatistic() {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** NOT IMPLEMENTED YET
     *
     * @return
     */
    @Override
    public String getCSVStatisticHeader() {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
