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

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;

/**
 *
 * @author rossetti
 */
public class ExampleModelElement extends ModelElement {

    public ExampleModelElement(ModelElement parent) {
        this(parent, null);
    }

    public ExampleModelElement(ModelElement parent, String name) {
        super(parent, name);
    }

    @Override
    protected void afterExperiment() {
        super.afterExperiment();
        System.out.println("In afterExperiment()");
        System.out.println("time = " + getTime());
    }

    @Override
    protected void afterReplication() {
        super.afterReplication();
        System.out.println("In afterReplication()");
        System.out.println("time = " + getTime());
    }

    @Override
    protected void beforeExperiment() {
        super.beforeExperiment();
        System.out.println("In beforeExperiment()");
        System.out.println("time = " + getTime());
    }

    @Override
    protected void beforeReplication() {
        super.beforeReplication();
        System.out.println("In beforeReplication()");
        System.out.println("time = " + getTime());
    }

    @Override
    protected void initialize() {
        super.initialize();
        System.out.println("In initialize()");
        System.out.println("time = " + getTime());
    }

    @Override
    protected void replicationEnded() {
        super.replicationEnded();
        System.out.println("In replicationEnded()");
        System.out.println("time = " + getTime());
    }

    @Override
    protected void warmUp() {
        super.warmUp();
        System.out.println("In warmUp()");
        System.out.println("time = " + getTime());
    }

    @Override
    protected void timedUpdate() {
        super.timedUpdate();
        System.out.println("In timedUpdate()");
        System.out.println("time = " + getTime());
    }

    public static void main(String[] args) {

        Simulation s = new Simulation("Example ModelElement");

        // create the containing model
        Model m = s.getModel();

        // create the model element and attach it to the model
        ExampleModelElement me = new ExampleModelElement(m);
        me.setTimedUpdateInterval(25.0);
        ModelElementObserverExample o = new ModelElementObserverExample();
        me.addObserver(o);

        // set the parameters of the experiment
        s.setNumberOfReplications(2);
        s.setLengthOfWarmUp(40.0);
        s.setLengthOfReplication(100.0);

        // tell the experiment to run
        s.run();

    }

}
