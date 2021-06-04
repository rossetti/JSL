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

package jsl.utilities.random.arta;

import java.util.TimerTask;




/**
 * @author rossetti
 *
 */
public class ARTAFinderOutputTask extends TimerTask {

	protected ARTACorrelationFinder myCF;

	public ARTAFinderOutputTask(ARTACorrelationFinder b) {
		myCF = b;
	}

	@Override
	public void run() {
		if (myCF == null){
			System.out.println("The ARTACorrelationFinder was null");
			return;
		}
		double rho = myCF.getCorrelation();
		double rhoD = myCF.getDesiredCorrelation();
		System.out.println("Desired correlation: " + rhoD + " Current correlaton: " + rho);
		System.out.println("Stopping criteria: " + myCF.getStoppingCriteria() + " Desired precision: " + myCF.getDesiredPrecision());
//		System.out.println(myCF);
	}

}
