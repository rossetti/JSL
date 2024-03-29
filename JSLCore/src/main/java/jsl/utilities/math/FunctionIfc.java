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
 * FunctionIfc is an interface for mathematical functions of
 * a single variable, that is functions of the form f(x).
 *
 */
public interface FunctionIfc {
    
    /**
     * Returns the value of the function for the specified variable value.
     * @param x the input value
     * @return the function value
     */
    double fx(double x);
}