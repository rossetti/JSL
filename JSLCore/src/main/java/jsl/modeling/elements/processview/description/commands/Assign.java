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
import jsl.modeling.elements.variable.*;
import jsl.utilities.GetValueIfc;

import java.util.*;

/**
 */
public class Assign extends ProcessCommand {

	/** The list of items in the queue.
	 */
	private List<Assignment> myAssignments;
	
	/**
	 * @param parent
	 */
	public Assign(ModelElement parent) {
		this(parent, null);
	}

	/**
	 * @param parent
	 * @param name
	 */
	public Assign(ModelElement parent, String name) {
		super(parent, name);
		myAssignments = new ArrayList<Assignment>();
	}

	/* (non-Javadoc)
	 * @see jsl.modeling.elements.processview.description.ProcessCommand#execute()
	 */
	public void execute() {
		for(Assignment a: myAssignments)
			a.makeAssignment();
	}

	/** Adds an assignment to the assign command
	 * 
	 * @param leftSide, the thing being assigned to
	 * @param rightSide the thing being used to determine the assigned value
	 */
	public void addAssignment(SetValueIfc leftSide, GetValueIfc rightSide){
		Assignment a = new Assignment(leftSide, rightSide);
		myAssignments.add(a);
	}

	/** Adds an attribute assignment to the assign command
	 *  Note: The attribute name should have been added to the entity
	 *  
	 * @param attributeName the name of the attribute to be assigned
	 * @param rightSide the thing being used to determine the assigned value
	 */
	public void addAssignment(String attributeName, GetValueIfc rightSide){
		Assignment a = new Assignment(attributeName, rightSide);
		myAssignments.add(a);
	}
	
	private class Assignment {
		
		protected SetValueIfc myLeftSide;
		
		protected GetValueIfc myRightSide;
		
		protected String myAttributeName;
		
		public Assignment(String attributeName, GetValueIfc rightSide){
			if(attributeName == null)
				throw new IllegalArgumentException("The attribute name for the assignment was null!");
				
			if(rightSide == null)
				throw new IllegalArgumentException("The right side of the assignment was null!");
			
			myAttributeName = attributeName;
			
			myRightSide = rightSide;			
		}
		
		public Assignment(SetValueIfc leftSide, GetValueIfc rightSide) {
			if(leftSide == null)
				throw new IllegalArgumentException("The left side variable for the assignment was null!");
				
			if(rightSide == null)
				throw new IllegalArgumentException("The right side of the assignment was null!");
			
			myLeftSide = leftSide;
			
			myRightSide = rightSide;
			
		}
		
		protected final void makeAssignment(){
			
			if (myAttributeName == null) // not an attribute assignment
				myLeftSide.setValue(myRightSide.getValue());
			else { // an attribute assignment
				// get the current entity
				Entity entity = getProcessExecutor().getCurrentEntity();
				entity.setAttributeValue(myAttributeName, myRightSide.getValue());
			}
		}
	}
}
