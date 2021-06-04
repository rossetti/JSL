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

/** Provides an interface for functions related to
 *  a cumulative distribution function CDF
 *
 * @author rossetti
 */
public interface CDFIfc {

    /** Returns the F(x) = Pr{X &lt;= x} where F represents the
     * cumulative distribution function
     *
     * @param x a double representing the upper limit
     * @return a double representing the probability
     */
    double cdf(double x);

    /** Returns the Pr{x1&lt;=X&lt;=x2} for the distribution
     *
     * @param x1 a double representing the lower limit
     * @param x2 a double representing the upper limit
     * @return cdf(x2)-cdf(x1)
     * @throws IllegalArgumentException if x1 &gt; x2
     */
    default double cdf(double x1, double x2) {
        if (x1 > x2) {
            String msg = "x1 = " + x1 + " > x2 = " + x2 + " in cdf(x1,x2)";
            throw new IllegalArgumentException(msg);
        }
        return (cdf(x2) - cdf(x1));
    }

    /** Computes the complementary cumulative probability
     * distribution function for given value of x
     * @param x The value to be evaluated
     * @return The probability, 1-P{X&lt;=x}
     */
    default double complementaryCDF(double x) {
        return (1.0 - cdf(x));
    }

}
