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
import jsl.modeling.elements.entity.Resource;

public class ResourceAnimationObserver extends AnimationObserverAbstract {

    public ResourceAnimationObserver(AnimationMessageHandlerIfc handler) {
        this(null, handler);
    }

    public ResourceAnimationObserver(String name, AnimationMessageHandlerIfc handler) {
        super(name, handler);
    }

    protected void update(ModelElement m, Object arg) {

        buildStandardModelElementMessage(m);

        Resource r = (Resource) m;
        String s = null;
        if (r.isBusy()) {
            s = "BUSY";
        } else {
            s = "IDLE";
        }

        myAnimationMessageHandler.append(s);
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(r.getInitialCapacity());
        myAnimationMessageHandler.append("\t");
        int c = r.getCapacity();
        int a = r.getNumberAvailable();
        int b = c - a;
        myAnimationMessageHandler.append(b);

    }
}
