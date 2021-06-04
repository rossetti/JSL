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

import java.io.PrintWriter;
import java.util.Date;
import jsl.simulation.ModelElement;
import static jsl.utilities.reporting.JSL.getInstance;

/**
 *
 */
public class ModelActionObserver extends ModelElementObserver {

    private PrintWriter out;

        /**
     *
     */
    public ModelActionObserver() {
        this(null, getInstance().out);
    }


    /**
     *
     */
    public ModelActionObserver(PrintWriter out) {
        this(null, out);
    }

    /**
     * @param name
     */
    public ModelActionObserver(String name, PrintWriter out) {
        super(name);
        this.out = out;
        
        out.println(new Date());
        out.println();
    }

    protected void beforeExperiment(ModelElement m, Object arg) {
        out.println("beforeExperiment " + m);
    }

    protected void beforeReplication(ModelElement m, Object arg) {
        out.println("beforeReplication " + m);
    }

    protected void initialize(ModelElement m, Object arg) {
        out.println("initialize " + m);
    }

    protected void montecarlo(ModelElement m, Object arg) {
        out.println("montecarlo " + m);
    }

    protected void update(ModelElement m, Object arg) {
        out.println("update " + m);
    }

    protected void warmUp(ModelElement m, Object arg) {
        out.println("warmUp " + m);
    }

    protected void timedUpdate(ModelElement m, Object arg) {
        out.println("timedUpdate " + m);
    }

    protected void batch(ModelElement m, Object arg) {
        out.println("batch " + m);
    }

    protected void replicationEnded(ModelElement m, Object arg) {
        out.println("afterExperiment " + m);
    }

    protected void afterReplication(ModelElement m, Object arg) {
        out.println("afterReplication " + m);
    }

    protected void afterExperiment(ModelElement m, Object arg) {
        out.println("afterExperiment " + m);
    }
}
