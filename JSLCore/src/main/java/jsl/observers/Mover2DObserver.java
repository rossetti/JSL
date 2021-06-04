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
package jsl.observers;


import jsl.simulation.ModelElement;
import jsl.modeling.elements.spatial.AbstractMover;



/**
 *
 */
public class Mover2DObserver extends ModelElementObserver {

    /**
     *
     */
    public Mover2DObserver() {
        this(null);
    }

    /**
     * @param name
     */
    public Mover2DObserver(String name) {
        super(name);
    }

    @Override
    public void update(Object observable, Object arg) {

        super.update(observable, arg);

        ModelElement m = (ModelElement) observable;

        int state = m.getObserverState();

        if (state == AbstractMover.TRIP_STARTED) {
            tripStarted(m, arg);
        } else if (state == AbstractMover.TRIP_ENDED) {
            tripEnded(m, arg);
        } else if (state == AbstractMover.MOVE_STARTED) {
            moveStarted(m, arg);
        } else if (state == AbstractMover.MOVE_ENDED) {
            moveEnded(m, arg);
        }
    }

    /**
     * @param element
     * @param arg
     */
    protected void moveEnded(ModelElement element, Object arg) {
    }

    /**
     * @param element
     * @param arg
     */
    protected void moveStarted(ModelElement element, Object arg) {
    }

    /**
     * @param element
     * @param arg
     */
    protected void tripEnded(ModelElement element, Object arg) {
    }

    /**
     * @param element
     * @param arg
     */
    protected void tripStarted(ModelElement element, Object arg) {
    }
}
