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
package jsl.modeling.elements.spatial;

import java.io.PrintWriter;
import java.util.Objects;

import jsl.simulation.ModelElement;
import jsl.observers.Mover2DObserver;

/**
 *
 * @author rossetti
 */
public class TripWriter extends Mover2DObserver {

    protected PrintWriter out;

    public TripWriter(PrintWriter writer){
        this(null, writer);
    }

    public TripWriter(String name, PrintWriter writer) {
        super(name);
        Objects.requireNonNull(writer, "The supplied writer was null");
        out = writer;
    }

    protected void writePosition(AbstractMover mover) {
        int r = mover.getCurrentReplicationNumber();
        CoordinateIfc position = mover.getPosition();
        String s = mover.getName();
        double x = position.getX1();
        double y = position.getX2();
        double t = mover.getTime();
        out.printf("%s, %d,%f, %f,%f %n", s, r, t, x, y);
    }

    @Override
    protected void initialize(ModelElement m, Object arg) {
        writePosition((AbstractMover)m);
    }

    @Override
    protected void tripEnded(ModelElement m, Object arg) {
        writePosition((AbstractMover)m);
    }

    @Override
    protected void moveEnded(ModelElement m, Object arg) {
 //       writePosition((AbstractMover)m);
    }

    @Override
    protected void replicationEnded(ModelElement m, Object arg) {
        writePosition((AbstractMover)m);
    }
    
}
