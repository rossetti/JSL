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
package examples.jobshop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.utilities.random.RandomIfc;

/**
 * @author rossetti
 *
 */
public class Sequence extends ModelElement {

    private List<JobStep> myJobSteps;

    /**
     * @param parent
     */
    public Sequence(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public Sequence(ModelElement parent, String name) {
        super(parent, name);
        myJobSteps = new ArrayList<JobStep>();
    }

    public void addJobStep(WorkStation workStation, RandomIfc processingTime) {
        new RandomVariable(this, processingTime);
        JobStep step = new JobStep(workStation, processingTime);
        myJobSteps.add(step);
    }

    public Iterator<JobStep> getIterator() {
        return (myJobSteps.iterator());
    }
}
