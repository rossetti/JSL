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
public class Allocation {

    /**
     * The entity holding the allocation
     */
    private Entity myEntity;

    /**
     * The current resource that the units of the allocation are
     * associated with
     */
    private Resource myAllocatedResource;

    /**
     * The amount of resource allocated
     */
    private int myAmountAllocated = 0;

    protected Allocation(Entity entity, Resource resource) {
        setEntity(entity);
        setAllocatedResource(resource);
        myEntity.addAllocation(this);
    }

    public final void nullify() {
        myAllocatedResource = null;
        myEntity = null;
    }

    protected final void setEntity(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The supplied entity was null");
        }
        myEntity = entity;
    }

    protected final void setAllocatedResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("The supplied resource was null");
        }
        myAllocatedResource = resource;
    }

    protected final void increaseAllocation(int amountAllocated) {
        if (amountAllocated <= 0) {
            throw new IllegalArgumentException("Amount of increase in allocation was less or equal to zero!");
        }

        myAmountAllocated = myAmountAllocated + amountAllocated;
    }

    protected final void decreaseAllocation(int amountOfDecrease) {
        if (amountOfDecrease <= 0) {
            throw new IllegalArgumentException("Amount of decrease in allocation was less or equal to zero!");
        }

        myAmountAllocated = myAmountAllocated - amountOfDecrease;
    }

//    public final void releaseResource(){
//        myAllocatedResource.release(this);
//    }

    /** The current amount allocated
     * 
     * @return
     */
    public final int getAmountAllocated() {
        return myAmountAllocated;
    }

    /** Returns true if there are units allocated
     *
     * @return
     */
    public final boolean isAllocated() {
        return (myAmountAllocated > 0);
    }

    /** Returns true if there are no units allocated
     *
     * @return
     */
    public final boolean isDeallocated() {
        return (myAmountAllocated == 0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entity ");
        sb.append(myEntity.getId());
        sb.append(" holds ");
        sb.append(getAmountAllocated());
        sb.append(" units of resource ");
        sb.append(myAllocatedResource.getName());
        return sb.toString();
    }

    /** The Entity associated with the allocation
     *
     * @return
     */
    public final Entity getEntity() {
        return myEntity;
    }

    /** Gets the resource that is associated with the allocation
     *
     * @return The resource
     */
    public final Resource getAllocatedResource() {
        return (myAllocatedResource);
    }
}
