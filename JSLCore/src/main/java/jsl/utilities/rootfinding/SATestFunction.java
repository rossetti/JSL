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

import jsl.utilities.math.FunctionIfc;
import jsl.utilities.distributions.DistributionIfc;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;

/**
 * @author rossetti
 *
 */
public class SATestFunction implements FunctionIfc {

	protected RVariableIfc myNoise;
	
	/**
	 * 
	 */
	public SATestFunction() {
		myNoise = new NormalRV(0.0, 1.0);
	}

	/* (non-Javadoc)
	 * @see jsl.utilities.optimize.FunctionIfc#fx(double)
	 */
	public double fx(double x) {
		return x*x*x + 4.0*x*x -10.0 + myNoise.getValue();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FunctionIfc f = new SATestFunction();
        
		StochasticApproximationRootFinder b = new StochasticApproximationRootFinder(f, 1.0, 2.0);
		
		b.setMaxIterations(10000000);
		
		double x = b.recommendInitialPoint();
		System.out.println("x = " + x);

		double scale = b.recommendScalingFactor(x, 0.02);
		System.out.println("scale = " + scale);

		b.setInitialPoint(x);
		b.setScaleFactor(scale);
		b.setDesiredPrecision(0.00001);
		b.run();
		
		System.out.println(b);

        System.out.println("Evalating the function at the root");
        Statistic s = new Statistic("function at root");
        for(int i=1;i<=100;i++)
            s.collect(f.fx(b.getRoot()));
        System.out.println(s);

	}

}
