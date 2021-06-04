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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.modeling.elements.entity;

/**
 *
 * @author rossetti
 */
abstract public class ReleaseRequirement implements Comparable<ReleaseRequirement> {

    public enum ReleaseOption {

        LAST_MEMBER_SEIZED, FIRST_MEMBER_SEIZED, SPECIFIC_MEMBER

    }
    
    protected static int myCounter_ = 0;

    protected int myId;

    protected int myReleaseAmount;

    public ReleaseRequirement(int amt) {
        if (amt <= 0) {
            throw new IllegalArgumentException("The release amount must be > 0");
        }
        myCounter_ = myCounter_ + 1;
        myId = myCounter_;
        myReleaseAmount = amt;
    }

    public int getReleaseAmount() {
        return myReleaseAmount;
    }

    public final int getId() {
        return myId;
    }

    abstract public void release(Entity e);

    /** Returns a negative integer, zero, or a positive integer
     * if this object is less than, equal to, or greater than the
     * specified object.
     *
     * Natural ordering:  order of creation
     *
     * Lower order of creation goes first
     *
     * Throws ClassCastException if the specified object's type
     * prevents it from begin compared to this object.
     *
     * Throws RuntimeException if the id's of the objects are the same,
     * but the references are not when compared with equals.
     *
     * Note:  This class may have a natural ordering that is inconsistent
     * with equals.
     * @param req 
     * @return Returns a negative integer, zero, or a positive integer
     * if this object is less than, equal to, or greater than the
     * specified object.
     */
    public int compareTo(ReleaseRequirement req) {

        // priorities are equal, compare ids

        if (myId < req.getId()) // lower id, implies created earlier
        {
            return (-1);
        }

        if (myId > req.getId()) {
            return (1);
        }

        // if the id's are equal then the object references must be equal
        // if this is not the case there is a problem

        if (this.equals(req)) {
            return (0);
        } else {
            throw new RuntimeException("Id's were equal, but references were not, in compareTo");
        }

    }
}
