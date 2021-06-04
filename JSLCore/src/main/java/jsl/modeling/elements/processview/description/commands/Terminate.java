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

import jsl.simulation.ModelElement;
import jsl.modeling.elements.processview.description.ProcessCommand;
import jsl.modeling.elements.entity.Entity;
import jsl.modeling.elements.variable.ResponseVariable;

/**
 */
public class Terminate extends ProcessCommand {

	protected ResponseVariable myProcessTime;
	
	/**
	 * @param parent
	 */
	public Terminate(ModelElement parent) {
		this(parent, false, null);
	}
	
	/**
	 * @param parent
	 * @param name
	 */
	public Terminate(ModelElement parent, String name) {
		this(parent, false, name);
	}

	/**
	 * @param parent
	 * @param name
	 */
	public Terminate(ModelElement parent, boolean collectEntityTimeFlag, String name) {
		super(parent, name);
		
		if (collectEntityTimeFlag == true)
			myProcessTime = new ResponseVariable(this, getName() + " Entity Flow Time");
	}
	
	/* (non-Javadoc)
	 * @see jsl.modeling.elements.processview.description.ProcessCommand#execute(jsl.modeling.elements.processview.description.ProcessExecutor)
	 */
	public final void execute() {
		if (myProcessTime != null){
			// get the entity that is terminating
			Entity entity = getProcessExecutor().getCurrentEntity();
			myProcessTime.setValue(getTime() - entity.getCreateTime());
		}
		getProcessExecutor().terminate();
	}

}
