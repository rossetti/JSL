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

import jsl.utilities.IdentityIfc;

/**
 *
 */
public class AttributeType implements IdentityIfc {

    /** incremented to give a running total of the
     *  number of attribute types created
     */
    private static int myCounter_;

    /** The id of the attribute type, currently if
     *  the attribute type is the ith attribute type created
     *  then the id is equal to i
     */
    private int myId;

    /** The name of the attribute type
     */
    private String myName;

    protected AttributeType() {
        this(null);
    }

    /**
     * @param name
     */
    protected AttributeType(String name) {
        myCounter_ = myCounter_ + 1;
        myId = myCounter_;
        setName(name);
    }

    /** Sets the name of this attribute type
     * @param str The name as a string.
     */
    protected void setName(String str) {

        if (str == null) { // no name is being passed, construct a default name
            String s = this.getClass().getName();
            int k = s.lastIndexOf(".");
            if (k != -1) {
                s = s.substring(k + 1);
            }
            str = s + "-" + getId();
        }
        myName = str;
    }

    /** Gets this attribute type's name.
     * @return The name of the attribute type.
     */
    public final String getName() {
        return myName;
    }

    /** Gets a uniquely assigned integer identifier for this attribute type.
     * This identifier is assigned when the attribute type is
     * created.  It may vary if the order of creation changes.
     * @return The identifier for the attribute type.
     */
    public final int getId() {
        return (myId);
    }
}
