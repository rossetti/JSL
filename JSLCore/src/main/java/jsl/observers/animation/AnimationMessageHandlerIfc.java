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
package jsl.observers.animation;

public interface AnimationMessageHandlerIfc {

	/** Checks if a message has been started
	 * 
	 * @return
	 */
	public boolean isStarted();
	
	/** Tells the animation generator to begin a new message
	 * 
	 */
	public void beginMessage();
	
	/** Tells the animation generator to committ
	 *  the current message
	 *
	 */
	public void commitMessage();
	
	/** Appends the supplied value to the animation message
	 * 
	 * @param value
	 */
	public void append(double value);

	/** Appends the supplied value to the animation message
	 * 
	 * @param value
	 */
	public void append(long value);

	/** Appends the supplied value to the animation message
	 * 
	 * @param value
	 */
	public void append(String value);

}
