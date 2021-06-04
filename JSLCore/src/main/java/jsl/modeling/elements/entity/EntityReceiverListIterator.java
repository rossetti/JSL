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

import java.util.ListIterator;
import java.util.List;

/**
 *
 */
public class EntityReceiverListIterator implements EntityReceiverIteratorIfc {

    protected ListIterator<EntityReceiverAbstract> myListIterator;

    public EntityReceiverListIterator(List<EntityReceiverAbstract> list) {
        if (list == null) {
            throw new IllegalArgumentException("List list must be non-null!");
        }
        myListIterator = list.listIterator();
    }

    @Override
    public EntityReceiverAbstract nextEntityReceiver() {
        if (myListIterator.hasNext()) {
            return ((EntityReceiverAbstract) myListIterator.next());
        } else {
            return (null);
        }
    }

    @Override
    public EntityReceiverAbstract previousEntityReceiver() {
        if (myListIterator.hasPrevious()) {
            return ((EntityReceiverAbstract) myListIterator.previous());
        } else {
            return (null);
        }
    }

    @Override
    public boolean hasNextEntityReceiver() {
        return myListIterator.hasNext();
    }

    @Override
    public boolean hasPreviousEntityReceiver() {
        return myListIterator.hasPrevious();
    }

    @Override
    public int nextEntityReceiverIndex() {
        return myListIterator.nextIndex();
    }

    @Override
    public int previousEntityReceiverIndex() {
        return myListIterator.previousIndex();
    }
}
