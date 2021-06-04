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
package jsl.modeling.elements.processview.description.commands;

import jsl.modeling.elements.processview.description.ProcessCommand;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.*;


/**
 * 
 */
public class Delay extends ProcessCommand {

	/**
	 *  A reference to the delay variable
	 */
	private Variable myDelay;

	public Delay(ModelElement parent, Variable delay) {
		this(parent, delay, null);
	}
	
	/**
	 * 
	 */
	public Delay(ModelElement parent, Variable delay, String name) {
		super(parent, name);
		setDelay(delay);
	}
	
	/**
	 * @return Returns the delay.
	 */
	public Variable getDelay() {
		return myDelay;
	}
	/**
	 * @param delay The delay to set.
	 */
	public void setDelay(Variable delay) {
		if(delay == null)
			throw new IllegalArgumentException("Variable delay must be non-null!");
		myDelay = delay;
	}

	/* (non-Javadoc)
	 * @see jsl.modeling.elements.processview.description.ProcessCommand#execute()
	 */
	public void execute() {
		
		double t = myDelay.getValue();
		scheduleResume(getProcessExecutor(), t, 1, "Resume Delay " + getName());

		getProcessExecutor().suspend();

	}

}
