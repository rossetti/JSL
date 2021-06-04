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
package jsl.utilities.distributions;

/**
 *
 * @author rossetti
 */
public interface InverseCDFIfc {

    /**
     * Provides the inverse cumulative distribution function for the
     * distribution
     *
     * While closed form solutions for the inverse cdf may not exist, numerical
     * search methods can be used to solve F(X) = U.
     *
     * @param p The probability to be evaluated for the inverse, p must be [0,1]
     * or an IllegalArgumentException is thrown
     * @return The inverse cdf evaluated at the supplied probability
     */
    double invCDF(double p);
}
