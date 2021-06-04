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
package jsl.utilities.rootfinding;

import jsl.utilities.Interval;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.distributions.Uniform;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rvariable.JSLRandom;

/** This class implements a basic 1-D stochastic approximation algorithm
 * 
 *  Robbins, H., and S. Monroe(1951).  On a Stochastic Approximation Technique.  Annals of Mathematical Statistics 22, pp 400-407.
 *  Kesten, H.(1958).  Accelerated Stochastic Approximation.  Annals of Mathematical Statistics 29, pp 41-59.
 *  
 *  Kesten, H.(1958).  Accelerated Stochastic Approximation.  Annals of Mathematical Statistics 29, pp 41-59.
 *  
 *  Suri, R., and Y. T.Leung(1987).  Single Run Optimization of Discrete Event Simulation:  
 *  An Empirical Study Using the M/M/1 Queue.  Technical Report No. 87-3, 
 *  Department of Industrial Engineering, University of Wisconsin, at Madison.
 *  
 *  Suri, R., and Y. T.Leung(1987).  Single Run Optimization of a Siman Model for
 *  Closed Loop Flexible Assembly Systems.  In the Proceedings of the 1987 Winter Simulation Conference.
 *  
 *  Rossetti M. D. and Clark G. M.  (1998)  Evaluating an Approximation for the Two Type Stoppage Machine 
 *  Interference Model Via Simulation Optimization, Computers and Industrial Engineering, Vol. 34, No. 3, pp. 655-688.
 *  
 *  This implementation uses a stopping criteria suggested by Suri and Leung and uses the suggested 
 *  acceleration method proposed by Kesten.
 *  
 * @author rossetti
 *
 */
public class StochasticApproximationRootFinder extends IPRootFinder {

	public static final double DEFAULT_PREC = 0.0001;
	
	public static final double DEFAULT_ALPHA = 0.7;
	
	public static final double DEFAULT_SCALE_FACTOR = 100.0;
	
	public static final int DEFAULT_MAX_ITER = 10000;
	
	/**
     * Desired precision.
     */
    protected double myDesiredPrecision = DEFAULT_PREC;
    
	/** The smoothing parameter
	 * 
	 */
	protected double myAlpha = DEFAULT_ALPHA;
	
	/** Used in the exponentially weighted stopping criteria
	 * 
	 */
	protected double myRSC;

	/** Scale addFactor can be used in accelerated procedure
	 * 
	 */
	protected double myScaleFactor = DEFAULT_SCALE_FACTOR;

	/** The stepping series 
	 * 
	 */
	protected double myRMSeries;

	/** Used to remember the last root
	 * 
	 */
	protected double myPrevX = Double.NaN;
	
	/** Used to remember the last evaluation
	 * 
	 */
	protected double myPrevFofX = Double.NaN;
	
	/** Used to "bounce" the search back into the interval
	 *  when a step attempts to go out of the interval
	 * 
	 */
//	protected Uniform myBounce = new Uniform();
			    
	/** Constructs a stochastic approximation root finder for the function, using
	 *  default scale addFactor and mid-point of interval as initial point
	 *   
     * @param func
     * @param interval
     */
	public StochasticApproximationRootFinder(FunctionIfc func, Interval interval){
    	this(func, ((interval.getUpperLimit() + interval.getLowerLimit())/2.0), DEFAULT_SCALE_FACTOR, interval.getLowerLimit(), interval.getUpperLimit());
    }

	/** Constructs a stochastic approximation root finder for the function, using
	 *  default scale addFactor and mid-point of interval as initial point
	 * 
	 * @param func
	 * @param xLower - a lower limit on the search, initial point must be &gt; xLower
	 * @param xUpper - an upper limit on the search, initial point must be &lt; xUpper
	 */
	public StochasticApproximationRootFinder(FunctionIfc func, double xLower, double xUpper){
		this(func, (xLower + xUpper)/2.0, DEFAULT_SCALE_FACTOR, xLower, xUpper);
	}
	
	/** Constructs a stochastic approximation root finder for the function, using default scale addFactor
	 * 
	 * @param func
	 * @param initialPt  the initial point for the search
	 * @param xLower - a lower limit on the search, initial point must be &gt; xLower
	 * @param xUpper - an upper limit on the search, initial point must be &lt; xUpper
	 */
	public StochasticApproximationRootFinder(FunctionIfc func, double initialPt, double xLower, double xUpper){
		this(func, initialPt, DEFAULT_SCALE_FACTOR, xLower, xUpper);
	}
	
	/** Constructs a stochastic approximation root finder for the function
	 * 
	 * @param func
	 * @param initialPt  the initial point for the search
	 * @param scaleFactor - the scale addFactor used during the search
	 * @param xLower - a lower limit on the search, initial point must be &gt; xLower
	 * @param xUpper - an upper limit on the search, initial point must be &lt; xUpper
	 */
	public StochasticApproximationRootFinder(FunctionIfc func, double initialPt, double scaleFactor, double xLower, double xUpper){
		super(func, xLower, xUpper);
		setInitialPoint(initialPt);
		setScaleFactor(scaleFactor);
		setMaxIterations(DEFAULT_MAX_ITER);
	}
	
	/** Sets the scale addFactor used during the search process
	 * 
	 * @param scaleFactor, must be &gt; 0.0
	 */
	public void setScaleFactor(double scaleFactor){
		if (scaleFactor <= 0.0)
			throw new IllegalArgumentException("The scale addFactor must be > 0.0");
		myScaleFactor = scaleFactor;
	}
	
	/** Gets the current scale addFactor value
	 * 
	 * @return
	 */
	public double getScaleFactor(){
		return myScaleFactor;
	}

    /**
     * Returns the desired precision.
     */
    public double getDesiredPrecision( ) {
        return myDesiredPrecision;
    }
    
    /**
     * Defines the desired precision.
     * @param prec, must be &gt; 0.0
     */
    public void setDesiredPrecision( double prec ){
        if ( prec <= 0 )
            throw new IllegalArgumentException("Non-positive precision: "+prec);
        myDesiredPrecision = prec;
    }
 
	/** Returns the smoothing parameter
	 * 
	 * @return
	 */
	public final double getSmoothingParameter(){
		return myAlpha;
	}
	
	/** Sets the smoothing parameter. 
	 * 
	 * @param alpha, must be in [0,1]
	 */
	public final void setSmoothingParameter(double alpha){
		if ((alpha < 0) || (alpha > 1))
			throw new IllegalArgumentException("The smoothing parameter must be in [0,1]");
		myAlpha = alpha;
	}
 
	/** Uses a linear approximation to recommend a scaling addFactor that can be
	 *  used during the search
	 * 
	 * @param initialPt
	 * @param delta - defines an interval +/- delta around initialPt to make linear approximation
	 * @return
	 */
	public double recommendScalingFactor(double initialPt, double delta){
		if (!myInterval.contains(initialPt)) 
			throw new IllegalArgumentException("The intial point is not in the interval");

		double xu = initialPt + delta;
		if (xu > getUpperLimit())
			xu = getUpperLimit();
		
		double xl = initialPt - delta;
		if (xl < getLowerLimit())
			xl = getLowerLimit();
		
		double fu = f.fx(xu);
		double fl = f.fx(xl);
		double fp = f.fx(initialPt);
		
		double factor = 1.0;
		double slope;
		
		if (JSLMath.equal(xu, xl)){
			slope = Double.MAX_VALUE;
		} else {
			slope = (fu - fl)/(xu - xl);
		}
		
		double rfp = 1.0/Math.abs(fp);
		double rslope = 1.0/Math.abs(slope);
//		System.out.println("fp = " + fp);
//		System.out.println("rfp = " + rfp);
//		System.out.println("slope = " + slope);
//		System.out.println("rslope = " + rslope);
		
		factor = Math.min(rfp, rslope);
		
		if (factor < DEFAULT_SCALE_FACTOR*DEFAULT_PREC)
			return (1.0);
		
		if (factor > DEFAULT_SCALE_FACTOR)
			return (DEFAULT_SCALE_FACTOR);		
		
		return factor;
	}
	
    protected void initializeIterations(){
    	super.initializeIterations();
    	myRMSeries = 1.0;
    	setRoot(getInitialPoint());
		// remember the last evaluation
		myPrevX = getRoot();
		myPrevFofX = getFunctionAtRoot();
    	myRSC = getFunctionAtRoot();
    }
    
	/** Continues until hasConverged() is true or max iterations is reached
	 * 
	 */
    protected boolean hasNext() {
		if (hasConverged() || (myStepCounter >= myMaxIterations))
			return false;
		else
			return true;
	}

    /**
     * Check to see if the result has been attained.
     * @return boolean
     */
    public boolean hasConverged() {
        return getStoppingCriteria() < myDesiredPrecision;
    }
    
    /** Gets the stopping criteria of the search
     * 
     * @return
     */
    public double getStoppingCriteria(){
    	return Math.abs(myScaleFactor*myRMSeries*myRSC);
    }
  
	/** Returns a String representation
	 * @return A String with basic results
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("Scale addFactor: " + getScaleFactor() + "\n");
		sb.append("RM series: " + myRMSeries + "\n");
		sb.append("RSC: " + myRSC + "\n");
		sb.append("Stopping criteria: " + getStoppingCriteria() + "\n");
		sb.append("Desired precision: " + getDesiredPrecision() + "\n");
		return(sb.toString());
	}

	/** Computes the next possible root value provided process has
	 *  a valid step. 
	 * 
	 * @return RootFinderStep or null if no more steps
	 */
	protected RootFinderStep next() {
		
		if (!hasNext())
			return null;

		// determine series, series changes if sign changes
		if (Math.signum(myPrevFofX) != Math.signum(getFunctionAtRoot())){
			myRMSeries = 1.0/(1.0/myRMSeries + 1.0);
		}
		
		// find possible root value
		double x = getRoot() - myScaleFactor*myRMSeries*getFunctionAtRoot();
		
		// bounce it back in range if out of range
		if (x < getLowerLimit()) {
			//TODO what should be done if lower limit equals the previous root value
			// thus no valid bounce interval can be formed

			x = JSLRandom.rUniform(getLowerLimit(), getRoot());
/*
			double ll = getLowerLimit();
			double r = getRoot();
			if (JSLMath.equal(ll, r)){
				myBounce.setParameters(ll,getUpperLimit());
			} else {
				myBounce.setParameters(getLowerLimit(), getRoot());
				x = myBounce.getValue();				
			}
*/		
		} else if (x > getUpperLimit()) {
			//TODO what should be done if upper limit equals the previous root value
			// thus no valid bounce interval can be formed
			x = JSLRandom.rUniform(getRoot(), getUpperLimit());
		}

		// remember the last evaluation
		myPrevX = getRoot();
		myPrevFofX = getFunctionAtRoot();
		
		setRoot(x);
				
		return myCurrentStep;

	}

	/** Tells the process to run (execute) the current step. Moves to
	 *  the next step if available and updates current step of the process
	 * 
	 */
	protected void runStep() {
		myCurrentStep = next();
		
		//TODO what if myCurrentStep is null
		
//		System.out.println(myCurrentStep);
						
		if (mySaveStepOption == true){
			RootFinderStep s = new RootFinderStep();
			s.myX = getRoot();
			s.myFofX = getFunctionAtRoot();
			mySteps.add(s);
		}
		
	}
	
}
