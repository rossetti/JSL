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
package jsl.modeling.elements.entity;

/**
 *
 */
public class Attribute implements AttributeIfc {

	private double myValue;
	
	private String myName;
	
	/**
	 * 
	 * @param name
	 */
	public Attribute(String name) {
		this(0.0, name);
	}
	
	/**
	 * @param initialValue
	 * @param name
	 */
	public Attribute(double initialValue, String name) {
		myValue = initialValue;
		myName = name;
	}

	/** Gets this attribute's name.
	 * @return The name of the attribute type.
	 */
	public final String getName() {
		return myName;
	}
	
	/* (non-Javadoc)
	 * @see jsl.modeling.elements.variable.GetValueIfc#getValue()
	 */
	public double getValue() {
		return (myValue);
	}

	/* (non-Javadoc)
	 * @see jsl.modeling.elements.variable.SetValueIfc#setValue(double)
	 */
	public void setValue(double value) {
		myValue = value;
	}
}
