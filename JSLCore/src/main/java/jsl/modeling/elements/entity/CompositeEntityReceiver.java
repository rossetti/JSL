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

/**
 *
 * @author rossetti
 */
public class CompositeEntityReceiver extends EntityReceiver {

    protected EntityReceiver myFirstReceiver;

    protected EntityReceiver myPrevReceiver;

    protected Exit myExit;

    public CompositeEntityReceiver(ModelElement parent) {
        this(parent, null);
    }

    public CompositeEntityReceiver(ModelElement parent, String name) {
        super(parent, name);
        myExit = new Exit(this);
        myFirstReceiver = myExit;
    }

    public void addInternalReceiver(EntityReceiver receiver){
        receiver.setDirectEntityReceiver(myExit);
        receiver.setComposite(this);
        
        if (myPrevReceiver == null){
            myPrevReceiver = receiver;
            myFirstReceiver = receiver;
        } else {
            myPrevReceiver.setDirectEntityReceiver(receiver);
            myPrevReceiver = receiver;
        }
    }

    @Override
    protected void receive(Entity entity) {
        myFirstReceiver.receive(entity);
    }

    protected class Exit extends EntityReceiver {

        public Exit(ModelElement parent) {
            super(parent);
        }

        @Override
        protected void receive(Entity entity) {
            entity.setCurrentReceiver(CompositeEntityReceiver.this);
            CompositeEntityReceiver.this.sendEntity(entity);
        }

    }
}
