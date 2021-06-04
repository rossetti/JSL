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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.modeling.elements.station;

import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author rossetti
 */
public class ReceiverListIterator implements ReceiverIteratorIfc {

    protected ListIterator<ReceiveQObjectIfc> myListIterator;

    public ReceiverListIterator(List<ReceiveQObjectIfc> list) {
        if (list == null) {
            throw new IllegalArgumentException("List list must be non-null!");
        }
        myListIterator = list.listIterator();
    }

    @Override
    public ReceiveQObjectIfc nextReceiver() {
        if (myListIterator.hasNext()) {
            return ((ReceiveQObjectIfc) myListIterator.next());
        } else {
            return (null);
        }
    }

    @Override
    public ReceiveQObjectIfc previousReceiver() {
        if (myListIterator.hasPrevious()) {
            return ((ReceiveQObjectIfc) myListIterator.previous());
        } else {
            return (null);
        }
    }

    @Override
    public boolean hasNextReceiver() {
        return myListIterator.hasNext();
    }

    @Override
    public boolean hasPreviousReceiver() {
        return myListIterator.hasPrevious();
    }

    @Override
    public int nextReceiverIndex() {
        return myListIterator.nextIndex();
    }

    @Override
    public int previousReceiverIndex() {
        return myListIterator.previousIndex();
    }
}
