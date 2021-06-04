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
package jsl.utilities.random.arta;

import jsl.utilities.distributions.Beta;
import jsl.utilities.distributions.Distribution;
import jsl.utilities.distributions.Lognormal;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticAccessorIfc;
import jsl.utilities.statistic.StatisticXY;

/**
 * @author rossetti
 *
 */
public class BivariateNORTACorrelationEvaluator {

	/** Standard normal
	 * 
	 */
	protected NormalRV snd;
	
	/** The distribution from which we want the
	 *  correlated random variates
	 * 
	 */
	protected Distribution myD1;
	
	/** The distribution from which we want the
	 *  correlated random variates
	 * 
	 */
	protected Distribution myD2;

	/** The generate size used to estimate the actual
	 *  correlation within one replication.  The 
	 *  default is 1000
	 * 
	 */
	protected int mySampleSize = 1000;
	
	/** The number of replications of the provided generate size
	 *  that are used to estimate the actual correlation
	 *  The default is 10.
	 */
	protected int myNumReps = 10;
	
	/** Whether or not to use antithetic variates in
	 *  the replications uses to estimate the correlation
	 *  The default is false
	 * 
	 */
	protected boolean myAntitheticFlag = false;

	/** Used to estimate the statistics of the indicated generate size
	 * 
	 */
	protected StatisticXY statXY;
	
	/** Used to estimate the statistics across replications for the correlation
	 * 
	 */
	protected Statistic myCorrStat;
	
	/** The input correlation
	 * 
	 */protected double myCorr;
	 
	/** 
	 *  
	 * @param d1
	 * @param d2
	 * @param correlation
	 * @param sampleSize
	 * @param numReps
	 * @param antitheticFlag
	 */
	public BivariateNORTACorrelationEvaluator(Distribution d1, Distribution d2, double correlation,
			int sampleSize, int numReps, boolean antitheticFlag) {
		setFirstDistribution(d1);
		setSecondDistribution(d2);
		statXY = new StatisticXY();
		myCorrStat = new Statistic("Lag 1 across rep statistcs");
		setCorrelation(correlation);
		setSampleSize(sampleSize);
		setSampleSize(sampleSize);
		setNumberOfReplications(numReps);
		setAntitheticFlag(antitheticFlag);	
		snd = new NormalRV();
	}
	
	/**
	 * @return the distribution
	 */
	public final Distribution getFirstDistribution() {
		return myD1;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public final void setFirstDistribution(Distribution distribution) {
		if (distribution == null)
			throw new IllegalArgumentException("The supplied distribution was null");
		
		myD1 = distribution;
	}

	/**
	 * @return the distribution
	 */
	public final Distribution getSecondDistribution() {
		return myD2;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public final void setSecondDistribution(Distribution distribution) {
		if (distribution == null)
			throw new IllegalArgumentException("The supplied distribution was null");
		
		myD2 = distribution;
	}
	
	/** This is the correlation set within the NORTA process
	 * @return the correlation
	 */
	public final double getCorrelation() {
		return myCorr;
	}

	/** This is the correlation that will be used within the NORTA process
	 *  This is not the desired correlation or the actual resulting correlation
	 *  
	 * @param corr the correlation to set
	 */
	public final void setCorrelation(double corr) {
		if ( (corr <= -1) || (corr >= 1))
			throw new IllegalArgumentException("Phi must be (-1,1)");
		myCorr = corr;
		statXY.reset();
		myCorrStat.reset();

	}

	/** This is the generate size used within each replication to estimate
	 *  the actual correlation
	 *  
	 * @return the generate size
	 */
	public final int getSampleSize() {
		return mySampleSize;
	}

	/**
	 * @param sampleSize the generate size to set, must be &gt; 2
	 */
	public final void setSampleSize(int sampleSize) {
		if (sampleSize < 3)
			throw new IllegalArgumentException("The generate size must be > 2");
		
		mySampleSize = sampleSize;
	}
	
	/** The number of replications of the provided generate size
	 *  used to estimate the actual correlation
	 *  
	 * @return the number of replications
	 */
	public final int getNumberOfReplications() {
		return myNumReps;
	}

	/** The number of replications of the provided generate size
	 *  used to estimate the actual correlation
	 * @param numReps the number of replications
	 */
	public final void setNumberOfReplications(int numReps) {
		if (numReps < 1)
			throw new IllegalArgumentException("The number of replications must be >=1");
		myNumReps = numReps;
	}

	/** The antithetic flag can be use to turn on antithethic sampling
	 *  when estimating the correlation with multiple replications
	 *  
	 * @return the antitheticFlag
	 */
	public final boolean getAntitheticFlag() {
		return myAntitheticFlag;
	}

	/** The antithetic flag can be use to turn on antithethic sampling
	 *  when estimating the correlation with multiple replications. True
	 *  means that antithetic sampling will be used.  When this is set
	 *  the number of replications represents the number of antithetic
	 *  pairs to be sampled.
	 *  
	/**
	 * @param antitheticFlag the flag to set
	 */
	public final void setAntitheticFlag(boolean antitheticFlag) {
		myAntitheticFlag = antitheticFlag;
	}

	/** Estimates the correlation based on the provided number
	 *  of replications of the given generate size
	 * @return
	 */
	public final double estimateCorrelation(){
		return (estimateCorrelation(myNumReps, mySampleSize));
	}
	
	/** Estimates the correlation based on the provided number
	 *  of replications of the given generate size
	 * 
	 * @param numReps must be &gt;=1
	 * @return
	 */
	public final double estimateCorrelation(int numReps){
		return (estimateCorrelation(numReps, mySampleSize));
	}
	
	/** Estimates the correlation based on the provided number
	 *  of replications of the given generate size
	 * 
	 * @param numReps must be &gt;=1
	 * @param sampleSize must be &gt; 3
	 * @return
	 */
	public final double estimateCorrelation(int numReps, int sampleSize){
		if (numReps < 1)
			throw new IllegalArgumentException("The number of replications must be >=1");
		if (sampleSize < 3)
			throw new IllegalArgumentException("The generate size must be > 2");

		myCorrStat.reset();
		
		if (myAntitheticFlag){
			double xodd = 0.0;
			int n = numReps*2;
			for(int i=1;i<=n;i++){
				double erho = sampleCorrelation(sampleSize);				
				if ( (i%2) == 0){
					double x = (xodd + erho)/2.0;
					myCorrStat.collect(x);
					snd.setAntitheticOption(false);
				} else {
					xodd = erho;
					snd.setAntitheticOption(true);
				}
			}
			snd.setAntitheticOption(false);
		} else {
			for(int i=1;i<=numReps;i++){
				myCorrStat.collect(sampleCorrelation(sampleSize));
				//System.out.println("hw = " + myLag1Stat.getHalfWidth() + " i = " + i);
			}
		}
		
		return(myCorrStat.getAverage());
	}

	/** Estimates the correlation to the precision of the half-width bound
	 *  The maximum number of replications is set at 20*getNumberOfReplications()
	 *  The size of each generate for an individual replication is getSampleSize()
	 *  
	 * @param hwBound
	 * @return
	 */
	public final double estimateCorrelation(double hwBound){
		return estimateCorrelation(hwBound, mySampleSize, 100*getNumberOfReplications());
	}
	
	/** Estimates the correlation to the precision of the half-width bound
	 *  The maximum number of replications is set at 20*getNumberOfReplications()
	 * 
	 * @param hwBound
	 * @param sampleSize The size of each generate for an individual replication
	 * @return
	 */
	public final double estimateCorrelation(double hwBound, int sampleSize){
		return estimateCorrelation(hwBound, sampleSize, 100*getNumberOfReplications());
	}
	
	/** Estimates the correlation to the precision of the half-width bound or
	 *  until the specified number of replications has been met
	 * 
	 * 
	 * @param hwBound
	 * @param sampleSize The size of each generate for an individual replication
	 * @param numReps The maximum number of replications
	 * @return
	 */
	public final double estimateCorrelation(double hwBound, int sampleSize, int numReps){
       
        if (hwBound <=0)
            throw new IllegalArgumentException("Half-width bound must be > 0.");
 
		if (sampleSize < 3)
			throw new IllegalArgumentException("The generate size must be > 2");

		if (numReps < 1)
			throw new IllegalArgumentException("The number of replications must be >=1");

		myCorrStat.reset();

		double hw = Double.MAX_VALUE;
		
		if (myAntitheticFlag){			
			double xodd = 0.0;
			int k = numReps*2;
			for(int i=1;i<=k;i++){
				double erho = sampleCorrelation(sampleSize);				
				if ( (i%2) == 0){
					double x = (xodd + erho)/2.0;
					myCorrStat.collect(x);
					snd.setAntitheticOption(false);
					hw = myCorrStat.getHalfWidth();
					if (hw <= hwBound)
						break;
				} else {
					xodd = erho;
					snd.setAntitheticOption(true);
				}
			}
			snd.setAntitheticOption(false);
		} else {
			for(int i=1;i<=numReps;i++){
				myCorrStat.collect(sampleCorrelation(sampleSize));
				hw = myCorrStat.getHalfWidth();
				//System.out.println("hw = " + hw + " i = " + i);
				if (hw <= hwBound)
					break;
			}		
		}
		
		return(myCorrStat.getAverage());

	}

	/** After the correlation has been estimated, this method can
	 *  be used to get the statistics across the replications on
	 *  the correlation
	 * 
	 * @return
	 */
	public final StatisticAccessorIfc getCorrelationStatistics(){
		return myCorrStat;
	}
	
	/** Returns an estimate of the correlation based on a
	 *  generate of the provided size
	 * 
	 * @param sampleSize must be &gt; 2
	 * @return
	 */
	public final double sampleCorrelation(int sampleSize){
		if (sampleSize < 3)
			throw new IllegalArgumentException("The generate size must be > 2");
		statXY.reset();
		for(int i=1;i<=sampleSize;i++){
			double z1 = snd.getValue();
			double z2 = myCorr*z1 + Math.sqrt(1.0-myCorr*myCorr)*snd.getValue();
			double u1 = Normal.stdNormalCDF(z1);
			double u2 = Normal.stdNormalCDF(z2);
			double x = myD1.invCDF(u1);
			double y= myD2.invCDF(u2);
			statXY.collectXY(x, y);			
		}
		return statXY.getCorrelationXY();
	}
	
	/** After an individual generate for a replication has been generated
	 *  this method can provide the statistics on the generate
	 * 
	 * @return
	 */
	public final StatisticXY getSampleStatistics(){
		return statXY;
	}

	/** Returns a String representation
	 * @return A String with basic results
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nNumber of replications = " + myCorrStat.getCount() + "\n");
		sb.append("Sample size = " + statXY.getCount() + "\n");
		sb.append("Antithetic flag option = " + myAntitheticFlag + "\n");
		sb.append("Specified lag 1 correlation = " + getCorrelation() + "\n");
		sb.append("Correlation statistics based on last call to estimateCorrelation() \n \n");
		sb.append(myCorrStat);
		return(sb.toString());
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Distribution xd = new Beta(1.5162, 2.1784);
		Distribution yd = new Lognormal(0.289, 0.336);

		double lag1 = 0.8;
		int n = 10000;
		int r = 10;
		boolean flag = false;
		
		BivariateNORTACorrelationEvaluator y = new BivariateNORTACorrelationEvaluator(xd, yd, lag1, n, r, flag);
		
		y.estimateCorrelation();
		
		System.out.println(y);
		
//		y.estimateCorrelation(0.001);
		
//		System.out.println(y);
	}

}
