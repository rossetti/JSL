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
package jsl.observers.animation;

import jsl.simulation.ModelElement;
import jsl.observers.Mover2DObserver;

public abstract class AnimationObserverAbstract extends Mover2DObserver {

    protected AnimationMessageHandlerIfc myAnimationMessageHandler;

    protected double myTimeOfPreviousUpdate;

    public AnimationObserverAbstract(AnimationMessageHandlerIfc handler) {
        this(null, handler);
    }

    public AnimationObserverAbstract(String name, AnimationMessageHandlerIfc handler) {
        super(name);
        setAnimationMessageHandler(handler);
    }

    public void update(Object observable, Object arg) {

        ModelElement m = (ModelElement) observable;

        // tell the message handler that a new message is beginning
        myAnimationMessageHandler.beginMessage();

        // builder standard model element message
//		buildStandardModelElementMessage(m);

        // handle the model element updates
        super.update(observable, arg);

        // post the message and record the time of this update
        if (myAnimationMessageHandler.isStarted()) {
            myAnimationMessageHandler.commitMessage();
            myTimeOfPreviousUpdate = m.getTime();
        }

    }

    /**
     * @return Returns the animation message handler.
     */
    protected final AnimationMessageHandlerIfc getAnimationMessageHandler() {
        return myAnimationMessageHandler;
    }

    /**
     * @param handler The AnimationMessageHandlerIfc to set.
     */
    protected final void setAnimationMessageHandler(AnimationMessageHandlerIfc handler) {
        if (handler == null) {
            throw new IllegalArgumentException("The AnimationMessageHandlerIfc was null");
        }
        myAnimationMessageHandler = handler;
    }

    protected void buildStandardModelElementMessage(ModelElement element) {

        myAnimationMessageHandler.append(getClass().getName());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(element.getClass().getName());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(element.getTime());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(element.getId());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(element.getName());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(myTimeOfPreviousUpdate);
        myAnimationMessageHandler.append("\t");

    }
}
