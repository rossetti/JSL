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
package examples.modelelement;

import jsl.simulation.ModelElement;
import jsl.observers.ModelElementObserver;

/**
 *
 * @author rossetti
 */
public class ModelElementObserverExample extends ModelElementObserver {

    @Override
    protected void afterExperiment(ModelElement m, Object arg) {
        super.afterExperiment(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("after experiment ");
        System.out.println("time = " + m.getTime());
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        super.afterReplication(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("after replication ");
        System.out.println("time = " + m.getTime());
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        super.beforeExperiment(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("before experiment ");
        System.out.println("time = " + m.getTime());
    }

    @Override
    protected void beforeReplication(ModelElement m, Object arg) {
        super.beforeReplication(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("before replication ");
        System.out.println("time = " + m.getTime());
    }

    @Override
    protected void initialize(ModelElement m, Object arg) {
        super.initialize(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("initialize ");
        System.out.println("time = " + m.getTime());
    }

    @Override
    protected void replicationEnded(ModelElement m, Object arg) {
        super.replicationEnded(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("replication ended ");
        System.out.println("time = " + m.getTime());
    }

    @Override
    protected void warmUp(ModelElement m, Object arg) {
        super.warmUp(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("warm up ");
        System.out.println("time = " + m.getTime());
    }

    @Override
    protected void timedUpdate(ModelElement m, Object arg) {
        super.timedUpdate(m, arg);
        System.out.println("*****In observer:");
        System.out.println("ModelElement: " + m.getName());
        System.out.println("timed update ");
        System.out.println("time = " + m.getTime());
    }
}
