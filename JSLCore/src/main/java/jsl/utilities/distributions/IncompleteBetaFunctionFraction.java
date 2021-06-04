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

import jsl.utilities.math.ContinuedFraction;

/**
 *
 */
public class IncompleteBetaFunctionFraction extends ContinuedFraction {

	protected double alpha1;
	protected double alpha2;
	
	public double evaluateFraction(double x, double a1, double a2){
		alpha1 = a1;
		alpha2 = a2;
		setArgument(x);
		evaluate();
		return getResult();
	}
	
     /**
     * Compute the pair numerator/denominator for iteration n.
     * @param n int
     */
    protected void computeFactorsAt(int n){
    	int m = n / 2;
    	int m2 = 2 * m;
    	factors[0] = m2 == n
    					? x * m * ( alpha2 - m)
    								/ ( (alpha1 + m2) * (alpha1 + m2 - 1))
    					: -x * ( alpha1 + m) * (alpha1 + alpha2 + m)
    								/ ( (alpha1 + m2) * (alpha1 + m2 + 1));
    	return;
    }
    
    protected double initialValue(){
    	factors[1] = 1;
    	return 1;
    }

	protected void finalizeIterations() {
		
	}


}
