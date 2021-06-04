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
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.utilities.GetValueIfc;

/**
 * @author rossetti
 *
 */
public class Record extends ProcessCommand {

	protected GetValueIfc myExpression;
	protected ResponseVariable myResponse;
	
	/**
	 * @param parent
	 */
	public Record(ModelElement parent, GetValueIfc expression) {
		this(parent, expression,  null);
	}

	/**
	 * @param parent
	 * @param name
	 */
	public Record(ModelElement parent, GetValueIfc expression, String name) {
		super(parent, name);
		setExpression(expression);
		myResponse = new ResponseVariable(this,getName()+"Record");
	}

	/**
	 * @return Returns the recorded expression
	 */
	public GetValueIfc getExpression() {
		return myExpression;
	}
	/**
	 * @param expression The expression to record
	 */
	public void setExpression(GetValueIfc expression) {
		if(expression == null)
			throw new IllegalArgumentException("The expression must be non-null!");
		myExpression = expression;
	}
	
	/* (non-Javadoc)
	 * @see jsl.modeling.elements.processview.description.ProcessCommand#execute()
	 */
	@Override
	public void execute() {
		myResponse.setValue(myExpression.getValue());
	}

}
