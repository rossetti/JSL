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

import java.io.IOException;
import java.nio.file.Path;

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.spatial.SpatialElementIfc;
import jsl.modeling.elements.spatial.SpatialModel;
import jsl.utilities.reporting.TextReport;

public class SpatialModelTextFileObserver implements ObserverIfc {

    protected Model myModel;

    protected SpatialModel mySpatialModel2D;

    protected boolean myFirstUpdateFlag = true;

    protected final TextReport myTextReport;

    public SpatialModelTextFileObserver(Path pathToFile) {
        myTextReport = new TextReport(pathToFile);
    }

    public void update(Object arg0, Object arg1) {

        mySpatialModel2D = (SpatialModel) arg0;
        int state = mySpatialModel2D.getObserverState();

        if (myFirstUpdateFlag) {
            myFirstUpdateFlag = false;
            recordFirstUpdate();
        }

        if (state == SpatialModel.UPDATED_POSITION) {
            recordPositionUpdate();
        } else if (state == SpatialModel.ADDED_ELEMENT) {
            recordSpatialModelElementAdded();
        } else if (state == SpatialModel.REMOVED_ELEMENT) {
            recordSpatialModelElementRemoved();
        } else {
            throw new IllegalStateException("Not a valid state in SpatialModelTextFileObserver state = " + state);
        }

    }

    protected void recordFirstUpdate() {
        myTextReport.println("Starting observations on : " + mySpatialModel2D.getName());
    }

    protected void recordSpatialModelElementRemoved() {
        SpatialElementIfc se = mySpatialModel2D.getUpdatingSpatialElement();
        ModelElement me = se.getModelElement();
        double t = me.getTime();
        myTextReport.println("t> " + t + " The following spatial element was removed from the spatial model: " + se.getName() + " ModelElement: " + me.getName());
    }

    protected void recordSpatialModelElementAdded() {
        SpatialElementIfc se = mySpatialModel2D.getUpdatingSpatialElement();
        ModelElement me = se.getModelElement();
        double t = me.getTime();
        myTextReport.println("t> " + t + " The following spatial element was added to the spatial model: " + se.getName() + " ModelElement: " + me.getName());

    }

    protected void recordPositionUpdate() {
        SpatialElementIfc se = mySpatialModel2D.getUpdatingSpatialElement();
        ModelElement me = se.getModelElement();
        double t = me.getTime();
        String s = "t> " + t + " Position Update( " + se.getName() + ", " + me.getName() + " ) moved from (";
        s = s + se.getPreviousPosition().getX1() + ", " + se.getPreviousPosition().getX2() + ") to (" + se.getPosition().getX1() + ", " + se.getPosition().getX2() + ")";
        myTextReport.println(s);

    }

}
