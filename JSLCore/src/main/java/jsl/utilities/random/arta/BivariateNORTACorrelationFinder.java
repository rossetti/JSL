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

import java.util.TimerTask;

import jsl.utilities.Interval;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.rootfinding.StochasticApproximationRootFinder;
import jsl.utilities.distributions.Beta;
import jsl.utilities.distributions.Distribution;
import jsl.utilities.distributions.Exponential;
import jsl.utilities.distributions.Lognormal;


/**
 * @author rossetti
 *
 */
public class BivariateNORTACorrelationFinder extends
		BivariateNORTACorrelationEvaluator {

	public final static int DEFAULT_INITIAL_PTS = 10;
	
	public final static double DEFAULT_HWBOUND = 0.005;

	public final double DEFAULT_DELTA = 0.1;

	protected double myDesiredCorrelation;

	protected CorrFunction myCorrFunction = new CorrFunction();

	protected StochasticApproximationRootFinder mySARootFinder;

	protected double myMatchingCorrelation = Double.NaN;

	protected double myHWBound = DEFAULT_HWBOUND;
	
	protected double myDelta = DEFAULT_DELTA;
	
	protected int myInitialPoints = DEFAULT_INITIAL_PTS;

	protected Interval myInterval;

	protected double myLRho;
	
	protected double myURho;
	
	/**
	 * @param d1 first distribution
	 * @param d2 second distribution
	 * @param correlation the correlation
	 * @param sampleSize the sample size
	 * @param numReps the number of replications
	 * @param antitheticFlag use antithetics
	 */
	public BivariateNORTACorrelationFinder(Distribution d1, Distribution d2,
			double correlation, int sampleSize, int numReps,
			boolean antitheticFlag) {
		super(d1, d2, correlation, sampleSize, numReps, antitheticFlag);
	}

	public boolean checkMatchingCorrelation(double desiredCorrelation){
		if ( (desiredCorrelation <= -1) || (desiredCorrelation >= 1))
			throw new IllegalArgumentException("Correlation must be (-1,1)");
		
		double ll = -0.999999;
		double ul =  0.999999;
		
		if (desiredCorrelation <= 0.0)
			ul = 0.0;
		else
			ll = 0.0;
		
		// test if a match is possible
		setCorrelation(ll);
		double rl = estimateCorrelation(myHWBound);
		setCorrelation(ul);
		double ru = estimateCorrelation(myHWBound);
	
		myLRho = rl;
		myURho = ru;

		if ( (desiredCorrelation < rl) || (desiredCorrelation > ru))
			return false;

		myDesiredCorrelation = desiredCorrelation;

		if (myInterval == null)
			myInterval = new Interval(ll, ul);
		else
			myInterval.setInterval(ll, ul);

		if (mySARootFinder == null)
			mySARootFinder = new StochasticApproximationRootFinder(myCorrFunction, myInterval);
		else
			mySARootFinder.setInterval(myInterval);	

		return true;
	}
	
	/** Returns the interval of possible correlation values
	 * 
	 * @return the interval
	 */
	public Interval getCorrelationInterval(){
		return new Interval(myLRho, myURho);
	}
	
	public void setNumberOfPointsInInitialSearch(int n){
		if (n <= 1)
			throw new IllegalArgumentException("The number of points in initial search must be > 1");
		myInitialPoints = n;
	}
		
	public double findMatchingCorrelation(){

		myMatchingCorrelation = Double.NaN;
		
		if (mySARootFinder != null){
			double ip = mySARootFinder.recommendInitialPoint(myInitialPoints);
			mySARootFinder.setInitialPoint(ip);
			
			double scale = StochasticApproximationRootFinder.DEFAULT_SCALE_FACTOR;
			scale = mySARootFinder.recommendScalingFactor(ip, myDelta);
			mySARootFinder.setScaleFactor(scale);
								
			mySARootFinder.run();

			myMatchingCorrelation = mySARootFinder.getRoot();
		}
					
		return myMatchingCorrelation;
	}
	
	public double getDesiredCorrelation(){
		return myDesiredCorrelation;
	}
	
	public double getMatchingCorrelation(){
		return myMatchingCorrelation;
	}
	
	protected class CorrFunction implements FunctionIfc {

		public double fx(double x) {
			setCorrelation(x);
			return estimateCorrelation() - myDesiredCorrelation;
		}
		
	}

	/** Returns a String representation
	 * @return A String with basic results
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nCorrelation Matching");
		sb.append(super.toString());
		sb.append(mySARootFinder);
		return(sb.toString());
	}


	/**
	 * @param milliseconds the time in milliseconds
	 */
	public void turnOnTimer(long milliseconds) {
		mySARootFinder.turnOnTimer(milliseconds);
	}

	/**
	 * @return the desired precision
	 */
	public double getDesiredPrecision() {
		return mySARootFinder.getDesiredPrecision();
	}

	/**
	 * @param prec the desired precision
	 */
	public void setDesiredPrecision(double prec) {
		mySARootFinder.setDesiredPrecision(prec);
	}

	
	/**
	 * @return the stopping criteria
	 */
	public double getStoppingCriteria() {
		return mySARootFinder.getStoppingCriteria();
	}

	public static void main(String[] args) {

//		Distribution xd = new Beta(1.5162, 2.1784);
//		Distribution yd = new Lognormal(0.289, 0.336);

		Distribution xd = new Exponential(1.0);
		Distribution yd = new Beta(1.0, 0.5);

		double lag1 = 0.45;
		int n = 10000;
		int r = 20;
		boolean flag = false;
		
		BivariateNORTACorrelationFinder b = new BivariateNORTACorrelationFinder(xd, yd, lag1, n, r, flag);

		System.out.println("Working ...");
		
		boolean found = b.checkMatchingCorrelation(lag1);
		
		System.out.println("Check for match result: " + found);

//		System.out.println(b);

		if (found == true){
			System.out.println("Searching for match");
			NORTAFinderOutputTask t = new NORTAFinderOutputTask(b);
			b.turnOnTimer(10000);
		
			b.setDesiredPrecision(0.005);
			
			b.findMatchingCorrelation();
			System.out.println("Matching correlation: " + b.getMatchingCorrelation());
			
			System.out.println(b);			
		} else {
			System.out.println("No match is possible");
			Interval i = b.getCorrelationInterval();
			System.out.println("The possible correlation interval is: " + i);
		}

		System.out.println("Done!");
	}

}
