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
package jsl.modeling.elements.spatial;

import java.io.PrintWriter;

/**
 *
 * @author rossetti
 */
public class RG2DMoverWriter extends TripWriter {

    public RG2DMoverWriter(PrintWriter writer){
        this(null, writer);
    }

    public RG2DMoverWriter(String name, PrintWriter writer) {
        super(name, writer);
    }
 
    @Override
        protected void writePosition(AbstractMover mover) {
        int r = mover.getCurrentReplicationNumber();
        CoordinateIfc position = mover.getPosition();
        String s = mover.getName();
        double x = position.getX1();
        double y = position.getX2();
        double t = mover.getTime();
        RG2DMover rm = (RG2DMover)mover;
        RectangularCell2D cell = rm.getCurrentCell();
        String cName = cell.getRowColName();
        out.printf("%s, %d, %f, %f, %f, %s %n", s, r, t, x, y, cName);
    }
}
