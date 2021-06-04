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
import jsl.modeling.elements.spatial.CoordinateIfc;
import jsl.modeling.elements.spatial.SpatialElementIfc;

public class MoveableElementAnimationObserver extends AnimationObserverAbstract {

    public MoveableElementAnimationObserver(AnimationMessageHandlerIfc generator) {
        this(null, generator);
    }

    public MoveableElementAnimationObserver(String name, AnimationMessageHandlerIfc generator) {
        super(name, generator);

    }

    @Override
    protected void initialize(ModelElement m, Object arg) {
        buildSpatialElementMessage(m, "INITIALIZE");
    }

    /**
     * @param m
     * @param arg
     */
    @Override
    protected void moveEnded(ModelElement m, Object arg) {
        buildSpatialElementMessage(m, "MOVE_ENDED");
    }

    /**
     * @param m
     * @param arg
     */
    @Override
    protected void moveStarted(ModelElement m, Object arg) {
//		buildSpatialElementMessage(m, "MOVE_STARTED");	
    }

    @Override
    protected void tripEnded(ModelElement m, Object arg) {
//		buildSpatialElementMessage(m, "TRIP_ENDED");	
    }

    @Override
    protected void tripStarted(ModelElement m, Object arg) {
//		buildSpatialElementMessage(m, "TRIP_STARTED");			
    }

//	protected void update(ModelElement m, Object arg) {
//			buildSpatialElementMessage(m, "MOVE_ENDED");	
//	}
    protected void buildSpatialElementMessage(ModelElement element, String message) {

        buildStandardModelElementMessage(element);

        SpatialElementIfc se = (SpatialElementIfc) element;

        CoordinateIfc p = se.getPreviousPosition();
        CoordinateIfc c = se.getPosition();

        myAnimationMessageHandler.append(message);
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(p.getX1());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(p.getX2());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(c.getX1());
        myAnimationMessageHandler.append("\t");
        myAnimationMessageHandler.append(c.getX2());
    }

}
