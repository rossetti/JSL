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
package jsl.utilities.math;

/**
 * Continued fraction
 * 
 */
public abstract class ContinuedFraction extends DBHIterativeProcess {
	
	/**
	 * Best approximation of the fraction.
	 */
	private double result;
	/**
	 * Fraction's argument.
	 */
	protected double x;
	/**
	 * Fraction's accumulated numerator.
	 */
	private double numerator;
	/**
	 * Fraction's accumulated denominator.
	 */
	private double denominator;
	/**
	 * Fraction's next factors.
	 */
	protected double[] factors = new double[2];
	
	/**
	 * Compute the pair numerator/denominator for iteration n.
	 * @param n int
	 */
	protected abstract void computeFactorsAt(int n);
	
	/**
	 * @return double
	 */
	public double evaluateIteration(){
		computeFactorsAt(getIterations());
		denominator = 1 / limitedSmallValue( factors[0] * denominator
				+ factors[1]);
		numerator = limitedSmallValue( factors[0] / numerator + factors[1]);
		double delta = numerator * denominator;
		result = result*delta;
		return Math.abs(delta - 1);
	}
	
	/**
	 * @return double
	 */
	public double getResult( ){
		return result;
	}
	
	public void initializeIterations(){
		numerator = limitedSmallValue(initialValue());
		denominator = 0;
		result = numerator;
		return;
	}
	
	/**
	 * @return double
	 */
	protected abstract double initialValue();
	
	/**
	 * Protection against small factors.
	 * @return double
	 * @param r double
	 */
	private double limitedSmallValue( double r){
		if (Math.abs( r) < JSLMath.getSmallNumber())
			return(JSLMath.getSmallNumber());
		else
			return(r);
	}
	
	/**
	 * @param r double	the value of the series argument.
	 */
	public void setArgument(double r){
		x = r;
		return;
	}
}