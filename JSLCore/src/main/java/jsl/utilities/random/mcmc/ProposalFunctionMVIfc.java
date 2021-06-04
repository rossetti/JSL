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

package jsl.utilities.random.mcmc;

/**
 *  For use with MetropolisHastingsMV. Represents the proposal function
 *  for the multivariate case.
 *
 */
public interface ProposalFunctionMVIfc {

    /** The ratio of g(y,x)/g(x,y).  The ratio of the proposal function
     *  evaluated at x = current and y = proposed, where g() is some
     *  proposal function of x and y. The implementor should ensure
     *  that the returned ratio is a valid double
     *
     * @param current the x to evaluate
     * @param proposed the y to evaluate
     * @return the ratio of the proposal function
     */
    double getProposalRatio(double[] current, double[] proposed);

    /**
     *
     * @param current the current state value of the chain (i.e. x)
     * @return the generated possible state (i.e. y) which may or may not be accepted
     */
    double[] generateProposedGivenCurrent(double[] current);
}
