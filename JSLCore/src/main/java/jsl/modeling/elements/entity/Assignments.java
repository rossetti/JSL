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

import java.util.ArrayList;
import java.util.List;
import jsl.modeling.elements.variable.SetValueIfc;
import jsl.utilities.GetValueIfc;

/**
 *
 * @author rossetti
 */
public class Assignments {

        /** The list of Assignments to make
     */
    private List<Assignment> myAssignments;

    public Assignments() {
        myAssignments = new ArrayList<Assignment>();
    }

        /** Adds an assignment to the assign command
     *
     * @param leftSide, the thing being assigned to
     * @param rightSide the thing being used to determine the assigned value
     */
    public void addAssignment(SetValueIfc leftSide, GetValueIfc rightSide) {
        Assignment a = new Assignment(leftSide, rightSide);
        myAssignments.add(a);
    }

    /** Adds an attribute assignment to the assign command
     *  Note: The attribute name should have been added to the entity
     *
     * @param attributeName the name of the attribute to be assigned
     * @param rightSide the thing being used to determine the assigned value
     */
    public void addAssignment(String attributeName, GetValueIfc rightSide) {
        Assignment a = new Assignment(attributeName, rightSide);
        myAssignments.add(a);
    }
}
