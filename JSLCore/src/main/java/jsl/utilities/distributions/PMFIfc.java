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
package jsl.utilities.distributions;

/**
 * Represents the probability mass function for 1-d discrete distributions
 *
 * @author rossetti
 */
public interface PMFIfc {

    /**
     * Returns the f(x) where f represents the probability mass function for the
     * distribution.
     *
     * @param x a double representing the value to be evaluated
     * @return f(x) the P(X=x)
     */
    double pmf(double x);
}
