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

import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.Variable;

public class VariableAnimationObserver extends AnimationObserverAbstract {

	public VariableAnimationObserver(AnimationMessageHandlerIfc handler) {
		this(null, handler);
	}

	public VariableAnimationObserver(String name, AnimationMessageHandlerIfc handler) {
		super(name, handler);
	}

	protected void update(ModelElement m, Object arg) {
 
		buildStandardModelElementMessage(m);
		
		Variable v = (Variable)m;

        myAnimationMessageHandler.append("UPDATE");
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(v.getTimeOfChange());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(v.getValue());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(v.getPreviousTimeOfChange());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(v.getPreviousValue());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(v.getWeight());

	}

}
