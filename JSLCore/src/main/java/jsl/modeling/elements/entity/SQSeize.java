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

import jsl.simulation.ModelElement;
import jsl.modeling.queue.Queue;

/**
 *
 * @author rossetti
 */
public class SQSeize extends SeizeResources {

    protected Queue<Entity> myQueue;

    public SQSeize(ModelElement parent) {
        this(parent, null);
    }

    public SQSeize(ModelElement parent, String name) {
        super(parent, name);

        myQueue = new Queue<>(this, getName() + "_Q");
    }

    @Override
    protected void queueEntity(Entity entity) {
        myQueue.enqueue(entity);
    }

    @Override
    protected Entity selectNextEntity() {
        return (Entity) myQueue.peekNext();
    }

    @Override
    protected void removeEntity(Entity entity) {
        myQueue.remove(entity);
    }
}
