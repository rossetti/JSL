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

/**
 * @author rossetti
 *
 */
public class RootFinderStep {

	/** The current value evaluated operand (function input)
	 * 
	 */
	public double myX = Double.NaN;

	/** The current value of the function evaluated
	 *  at the last presented operand
	 * 
	 */
	public double myFofX = Double.NaN;

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("X: " + myX + "\n");
		sb.append("FofX: " + myFofX + "\n");
		return(sb.toString());
	}
}
