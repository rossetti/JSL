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
package jsl.modeling.elements.processview.description.commands;

import jsl.modeling.elements.processview.description.ProcessCommand;
import jsl.modeling.queue.Queue;
import jsl.modeling.elements.entity.*;
import jsl.simulation.ModelElement;

/**
 *
 */
public class Release extends ProcessCommand {

    private Resource myResource;

    private Queue myQueue;

    public Release(ModelElement parent, Resource resource, Queue queue) {
        this(parent, resource, queue, null);
    }

    public Release(ModelElement parent, Resource resource, Queue queue, String name) {
        super(parent, name);
        setResource(resource);
        setQueue(queue);
    }

    protected final void setResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource was equal to null!");
        }
        myResource = resource;
    }

    protected final void setQueue(Queue queue) {
        if (queue == null) {
            throw new IllegalArgumentException("Queue was equal to null!");
        }
        myQueue = queue;
    }

    @Override
    public void execute() {

        // get the entity that is releasing the resource
        Entity entity = getProcessExecutor().getCurrentEntity();

        // release the resource
        entity.release(myResource);

    }
}
