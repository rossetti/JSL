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
 * Iterative process based on a one-variable function, having a single numerical
 * result.
 *
 */
public abstract class FunctionalIterator extends DBHIterativeProcess {

    /**
     * Best approximation of the zero.
     */
    protected double result = Double.NaN;

    /**
     * Function for which the zero will be found.
     */
    protected FunctionIfc f;

    /**
     * Generic constructor.
     *
     * @param func OneVariableFunction
     */
    public FunctionalIterator(FunctionIfc func) {
        setFunction(func);
    }

    /**
     * Returns the result (assuming convergence has been attained).
     *
     * @return the result (assuming convergence has been attained).
     */
    public double getResult() {
        return result;
    }

    /**
     * @return double
     * @param epsilon double
     */
    public double relativePrecision(double epsilon) {
        return relativePrecision(epsilon, Math.abs(result));
    }

    /**
     * @param func OneVariableFunction
     */
    protected void setFunction(FunctionIfc func) {
        if (func == null) {
            throw new IllegalArgumentException("The function must not be null");
        }
        f = func;
    }
}