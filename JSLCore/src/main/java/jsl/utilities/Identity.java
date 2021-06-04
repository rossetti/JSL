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
package jsl.utilities;

/**  A class to assist with the naming and numbering of objects.  The number
 *  cannot change, but the name can be changed.
 *
 * @author rossetti
 */
public class Identity implements IdentityIfc {

    /** A counter to count the number of objects created to assign "unique" ids
     */
    private static int myIdCounter_;

    /** The name of this object
     */
    private String myName;

    /** The id of this object
     */
    private int myId;

    /**
     * Assumes the name is null.
     */
    public Identity(){
        this(null);
    }

    /**
     *
     * @param name the name to be used for the identity, can be null
     */
    public Identity(String name) {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        setName(name);
    }

    /** Gets the name.
     * @return The name of object.
     */
    @Override
    public final String getName() {
        return myName;
    }

    /** Returns the id for this object
     *
     * @return the id for this object
     */
    @Override
    public final int getId() {
        return (myId);
    }

    /** Sets the name. If null, a name is constructed based on the simple class name
     *  and the id of the object
     * @param str The name as a string.
     */
    public final void setName(String str) {
        if (str == null) {
            myName = this.getClass().getSimpleName() + "#" + getId();
        } else {
            myName = str;
        }
    }
}
