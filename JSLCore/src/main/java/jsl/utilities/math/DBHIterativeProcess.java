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
 * An iterative process is a general structure managing iterations.
 *
 * * This is based on the IterativeProcess class of Didier Besset in 
 * "Object-Oriented Implementation of Numerical Methods", Morgan-Kaufmann
 */
public abstract class DBHIterativeProcess {
    
    /**
     * Number of iterations performed.
     */
    private int iterations;
    
    /**
     * Maximum allowed number of iterations.
     */
    private int maximumIterations = 100;
    
    /**
     * Desired precision.
     */
    private double desiredPrecision = JSLMath.getDefaultNumericalPrecision();
    
    /**
     * Achieved precision.
     */
    private double precision;
        
    /**
     * Generic constructor.
     */
    public DBHIterativeProcess() {
    }
    
    /**
     * Performs the iterative process.
     * Note: this method does not return anything 
     * Subclass must implement a method to get the result
     */
    public void evaluate() {
        iterations = 0;
        initializeIterations();
        while ( iterations++ < maximumIterations ) {
            precision = evaluateIteration();
            if ( hasConverged() )
                break;
        }
        finalizeIterations();
    }
    
    /**
     * Evaluate the result of the current iteration.
     * @return the estimated precision of the result.
     */
    abstract protected double evaluateIteration();
    
    /**
     * Perform eventual clean-up operations
     * (must be implement by subclass when needed).
     */
    abstract protected void finalizeIterations();

    /**
     * Initializes internal parameters to start the iterative process.
     */
    abstract protected void initializeIterations();
    
    /**
     * Returns the desired precision.
     */
    public double getDesiredPrecision( ) {
        return desiredPrecision;
    }
    
    /**
     * Returns the number of iterations performed.
     */
    public int getIterations() {
        return iterations;
    }
    
    /**
     * Returns the maximum allowed number of iterations.
     */
    public int getMaximumIterations( ) {
        return maximumIterations;
    }
    
    /**
     * Returns the attained precision.
     */
    public double getPrecision() {
        return precision;
    }
    
    /**
     * Check to see if the result has been attained.
     * @return boolean
     */
    public boolean hasConverged() {
        return precision < desiredPrecision;
    }
        
    /**
     * @return double
     * @param epsilon double
     * @param x double
     */
    public double relativePrecision( double epsilon, double x) {
        return x > JSLMath.getDefaultNumericalPrecision()
        ? epsilon / x: epsilon;
    }
    
    /**
     * Defines the desired precision.
     */
    public void setDesiredPrecision( double prec ){
        if ( prec <= 0 )
            throw new IllegalArgumentException("Non-positive precision: "+prec);
        desiredPrecision = prec;
    }

    /**
     * Defines the maximum allowed number of iterations.
     */
    public void setMaximumIterations( int maxIter){
        if ( maxIter < 1 )
            throw new IllegalArgumentException("Non-positive maximum iteration: "+maxIter);
        maximumIterations = maxIter;
    }
}
