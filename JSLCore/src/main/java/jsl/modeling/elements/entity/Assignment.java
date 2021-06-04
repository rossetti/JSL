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

import jsl.modeling.elements.variable.SetValueIfc;
import jsl.utilities.GetValueIfc;

/**
 *
 * @author rossetti
 */
public class Assignment {

    protected SetValueIfc myLeftSide;

    protected GetValueIfc myRightSide;

    protected String myAttributeName;

    public Assignment(String attributeName, GetValueIfc rightSide) {
        if (attributeName == null) {
            throw new IllegalArgumentException("The attribute name for the assignment was null!");
        }

        if (rightSide == null) {
            throw new IllegalArgumentException("The right side of the assignment was null!");
        }

        myAttributeName = attributeName;

        myRightSide = rightSide;
    }

    public Assignment(SetValueIfc leftSide, GetValueIfc rightSide) {
        if (leftSide == null) {
            throw new IllegalArgumentException("The left side variable for the assignment was null!");
        }

        if (rightSide == null) {
            throw new IllegalArgumentException("The right side of the assignment was null!");
        }

        myLeftSide = leftSide;

        myRightSide = rightSide;

    }

    protected final void makeAssignment(Entity entity) {

        if (myAttributeName == null) // not an attribute assignment
        {
            myLeftSide.setValue(myRightSide.getValue());
        } else { // an attribute assignment
            entity.setAttributeValue(myAttributeName, myRightSide.getValue());
        }
    }
}
