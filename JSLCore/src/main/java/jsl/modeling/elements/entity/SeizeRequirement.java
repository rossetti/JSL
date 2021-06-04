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
 * @author rossetti
 */
abstract public class SeizeRequirement implements Comparable<SeizeRequirement> {

    protected static int myCounter_ = 0;

    protected int myId;

    protected int myAmtNeeded;

    protected boolean myPartialFillFlag;

    protected int myPriority;

    public SeizeRequirement(int amt, int priority, boolean partialFillFlag) {
        if (amt <= 0) {
            throw new IllegalArgumentException("The amount required must be > 0");
        }
        myCounter_ = myCounter_ + 1;
        myId = myCounter_;
        myAmtNeeded = amt;
        myPriority = priority;
        myPartialFillFlag = partialFillFlag;
    }

    abstract public Request createRequest(Entity entity, AllocationListenerIfc listener);

    abstract public SeizeIfc getResource();

    public final int getAmountRequired() {
        return myAmtNeeded;
    }

    public final int getPriority() {
        return myPriority;
    }

    public final boolean isPartiallyFillable() {
        return myPartialFillFlag;
    }

    public final int getId() {
        return myId;
    }

    /** Returns a negative integer, zero, or a positive integer
     * if this object is less than, equal to, or greater than the
     * specified object.
     *
     * Natural ordering:  priority, then order of creation
     *
     * Lower priority, lower order of creation goes first
     *
     * Throws ClassCastException if the specified object's type
     * prevents it from begin compared to this object.
     *
     * Throws RuntimeException if the id's of the objects are the same,
     * but the references are not when compared with equals.
     *
     * Note:  This class may have a natural ordering that is inconsistent
     * with equals.
     * @param req The requirement to compare this listener to
     * @return Returns a negative integer, zero, or a positive integer
     * if this object is less than, equal to, or greater than the
     * specified object.
     */
    @Override
    public int compareTo(SeizeRequirement req) {

        // check priorities

        if (myPriority < req.getPriority()) {
            return (-1);
        }

        if (myPriority > req.getPriority()) {
            return (1);
        }

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
            throw new RuntimeException("Id's were equal, but references were not, in JSLEvent compareTo");
        }

    }
}
