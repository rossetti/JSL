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

import jsl.utilities.random.rng.AR1CorrelatedRNStream;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.StatisticAccessorIfc;

/**
 * @author rossetti
 *
 */
public class ARTACorrelationEvaluator {

	/** The correlated random number generator used in
	 *  the NORTA process
	 * 
	 */
	protected AR1CorrelatedRNStream myCorrelatedRng;
	
	/** The distribution from which we want the
	 *  correlated random variates
	 * 
	 */
	protected RVariableIfc myDistribution;
		
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
	protected Statistic mySampleStat;
	
	/** Used to estimate the statistics across replications for the correlation
	 * 
	 */
	protected Statistic myLag1Stat;
	
	/** Note that the distribution will be set to use a correlated
	 *  random number generator, with the supplied lag 1 correlation
	 *   lag 1 correlation = 0.0
	 *   sampleSize = 1000
	 * 	 numReps = 1
	 *   antitheticFlag = false
	 *
	 * @param randomVariable the distribution
	 */
	public ARTACorrelationEvaluator(RVariableIfc randomVariable) {
		this(randomVariable, 0.0, 1000, 1, false);
	}
	
	/** Note that the distribution will be set to use a correlated
	 *  random number generator, with the supplied lag 1 correlation
	 *   sampleSize = 100
	 * 	 numReps = 1
	 *   antitheticFlag = false
	 *
	 * @param randomVariable the distribution
	 * @param lag1 the desired lag
	 */
	public ARTACorrelationEvaluator(RVariableIfc randomVariable, double lag1) {
		this(randomVariable, lag1, 1000, 1, false);
	}
	
	/** Note that the distribution will be set to use a correlated
	 *  random number generator, with the supplied lag 1 correlation
	 *   numReps = 1
	 *   antitheticFlag = false
	 *
	 * @param randomVariable the distribution
	 * @param lag1 the desired lag
	 * @param sampleSize the sample size
	 */
	public ARTACorrelationEvaluator(RVariableIfc randomVariable, double lag1,
			int sampleSize) {
		this(randomVariable, lag1, sampleSize, 1, false);
	}
	
	/** Note that the distribution will be set to use a correlated
	 *  random number generator, with the supplied lag 1 correlation
	 *   antitheticFlag = false
	 *
	 * @param randomVariable the distribution
	 * @param lag1 the desired lag
	 * @param sampleSize the sample size
	 * @param numReps the number of replications
	 */
	public ARTACorrelationEvaluator(RVariableIfc randomVariable, double lag1,
			int sampleSize, int numReps) {
		this(randomVariable, lag1, sampleSize, numReps, false);
	}
	
	/** Note that the randomVariable will be set to use a correlated
	 *  random number generator, with the supplied lag 1 correlation
	 *
	 * @param randomVariable the distribution
	 * @param lag1 the desired lag
	 * @param sampleSize the sample size
	 * @param numReps the number of replications
	 * @param antitheticFlag use antithetics flag
	 */
	public ARTACorrelationEvaluator(RVariableIfc randomVariable, double lag1,
			int sampleSize, int numReps, boolean antitheticFlag) {
		myCorrelatedRng = new AR1CorrelatedRNStream(lag1);
		setRandomVariable(randomVariable);
		randomVariable.setRandomNumberStream(myCorrelatedRng);
		setSampleSize(sampleSize);
		setNumberOfReplications(numReps);
		setAntitheticFlag(antitheticFlag);	
		mySampleStat = new Statistic("Sample Statistics");
		myLag1Stat = new Statistic("Lag 1 across rep statistcs");
	}
	
	/**
	 * @return the distribution
	 */
	public final RVariableIfc getRandomVariable() {
		return myDistribution;
	}

	/**
	 * @param randomVariable the randomVariable to set
	 */
	public final void setRandomVariable(RVariableIfc randomVariable) {
		if (randomVariable == null)
			throw new IllegalArgumentException("The supplied randomVariable was null");
		
		myDistribution = randomVariable;
	}

	/** This is the correlation set within the NORTA process
	 * @return the lag 1 correlation
	 */
	public final double getCorrelation() {
		return myCorrelatedRng.getLag1Correlation();
	}

	/** This is the correlation that will be used within the NORTA process
	 *  This is not the desired correlation or the actual resulting correlation
	 *  
	 * @param lag1 the lag 1 correlation to set
	 */
	public final void setCorrelation(double lag1) {
		myCorrelatedRng.setLag1Correlation(lag1);
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
	 * @return the estimated correlation
	 */
	public final double estimateCorrelation(){
		return (estimateCorrelation(myNumReps, mySampleSize));
	}
	
	/** Estimates the correlation based on the provided number
	 *  of replications of the given generate size
	 * 
	 * @param numReps must be &gt;=1
	 * @return the estimated correlation
	 */
	public final double estimateCorrelation(int numReps){
		return (estimateCorrelation(numReps, mySampleSize));
	}
	
	/** Estimates the correlation based on the provided number
	 *  of replications of the given generate size
	 * 
	 * @param numReps must be &gt;=1
	 * @param sampleSize must be &gt; 3
	 * @return the estimated correlation
	 */
	public final double estimateCorrelation(int numReps, int sampleSize){
		if (numReps < 1)
			throw new IllegalArgumentException("The number of replications must be >=1");
		if (sampleSize < 3)
			throw new IllegalArgumentException("The generate size must be > 2");

		myLag1Stat.reset();
		
		if (myAntitheticFlag){
			double xodd = 0.0;
			int n = numReps*2;
			for(int i=1;i<=n;i++){
				double erho = sampleCorrelation(sampleSize);				
				if ( (i%2) == 0){
					double x = (xodd + erho)/2.0;
					myLag1Stat.collect(x);
					myCorrelatedRng.setAntitheticOption(false);
				} else {
					xodd = erho;
					myCorrelatedRng.setAntitheticOption(true);
				}
			}
			myCorrelatedRng.setAntitheticOption(false);
		} else {
			for(int i=1;i<=numReps;i++){
				myLag1Stat.collect(sampleCorrelation(sampleSize));
				//System.out.println("hw = " + myLag1Stat.getHalfWidth() + " i = " + i);
			}
		}
		
		return(myLag1Stat.getAverage());
	}

	/** Estimates the correlation to the precision of the half-width bound
	 *  The maximum number of replications is set at 20*getNumberOfReplications()
	 *  The size of each generate for an individual replication is getSampleSize()
	 *  
	 * @param hwBound the half-width bound
	 * @return the estimated correlation
	 */
	public final double estimateCorrelation(double hwBound){
		return estimateCorrelation(hwBound, mySampleSize, 100*getNumberOfReplications());
	}
	
	/** Estimates the correlation to the precision of the half-width bound
	 *  The maximum number of replications is set at 20*getNumberOfReplications()
	 * 
	 * @param hwBound  the half-width bound
	 * @param sampleSize The size of each generate for an individual replication
	 * @return the estimated correlation
	 */
	public final double estimateCorrelation(double hwBound, int sampleSize){
		return estimateCorrelation(hwBound, sampleSize, 100*getNumberOfReplications());
	}
	
	/** Estimates the correlation to the precision of the half-width bound or
	 *  until the specified number of replications has been met
	 * 
	 * 
	 * @param hwBound the half-width bound
	 * @param sampleSize The size of each generate for an individual replication
	 * @param numReps The maximum number of replications
	 * @return the estimated correlation
	 */
	public final double estimateCorrelation(double hwBound, int sampleSize, int numReps){
       
        if (hwBound <=0)
            throw new IllegalArgumentException("Half-width bound must be > 0.");
 
		if (sampleSize < 3)
			throw new IllegalArgumentException("The generate size must be > 2");

		if (numReps < 1)
			throw new IllegalArgumentException("The number of replications must be >=1");

		myLag1Stat.reset();

		double hw = Double.MAX_VALUE;
		
		if (myAntitheticFlag){			
			double xodd = 0.0;
			int k = numReps*2;
			for(int i=1;i<=k;i++){
				double erho = sampleCorrelation(sampleSize);				
				if ( (i%2) == 0){
					double x = (xodd + erho)/2.0;
					myLag1Stat.collect(x);
					myCorrelatedRng.setAntitheticOption(false);
					hw = myLag1Stat.getHalfWidth();
					if (hw <= hwBound)
						break;
				} else {
					xodd = erho;
					myCorrelatedRng.setAntitheticOption(true);
				}
			}
			myCorrelatedRng.setAntitheticOption(false);
		} else {
			for(int i=1;i<=numReps;i++){
				myLag1Stat.collect(sampleCorrelation(sampleSize));
				hw = myLag1Stat.getHalfWidth();
				//System.out.println("hw = " + hw + " i = " + i);
				if (hw <= hwBound)
					break;
			}		
		}
		
		return(myLag1Stat.getAverage());

	}
	
	/** After the correlation has been estimated, this method can
	 *  be used to get the statistics across the replications on
	 *  the lag 1 correlation
	 * 
	 * @return the lag 1 statistics
	 */
	public final StatisticAccessorIfc getLag1Statistics(){
		return myLag1Stat;
	}
	
	/** Returns an estimate of the correlation based on a
	 *  generate of the provided size
	 * 
	 * @param sampleSize must be &gt; 2
	 * @return the sample correlation
	 */
	public final double sampleCorrelation(int sampleSize){
		if (sampleSize < 3)
			throw new IllegalArgumentException("The generate size must be > 2");
		mySampleStat.reset();
		for(int i=1;i<=sampleSize;i++)
			mySampleStat.collect(myDistribution);
		return mySampleStat.getLag1Correlation();
	}
	
	/** After an individual generate for a replication has been generated
	 *  this method can provide the statistics on the generate
	 * 
	 * @return the sample statistics
	 */
	public final StatisticAccessorIfc getSampleStatistics(){
		return mySampleStat;
	}
	
	/** Returns a String representation
	 * @return A String with basic results
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nNumber of replications = " + myLag1Stat.getCount() + "\n");
		sb.append("Sample size = " + mySampleStat.getCount() + "\n");
		sb.append("Antithetic flag option = " + myAntitheticFlag + "\n");
		sb.append("Specified lag 1 correlation = " + getCorrelation() + "\n");
		sb.append("Correlation statistics based on last call to estimateCorrelation() \n \n");
		sb.append(myLag1Stat);
		return(sb.toString());
	}

	public static void main(String[] args) {

		ExponentialRV d = new ExponentialRV(1.0);
		double lag1 = 0.8278;
		//double lag1 = 0.8;
		int n = 10000;
		int r = 10;
		boolean flag = false;
		
		ARTACorrelationEvaluator y = new ARTACorrelationEvaluator(d, lag1, n, r, flag);
		
		y.estimateCorrelation();
		
		System.out.println(y);
		
		y.estimateCorrelation(0.001);
		
		System.out.println(y);
	}

}
