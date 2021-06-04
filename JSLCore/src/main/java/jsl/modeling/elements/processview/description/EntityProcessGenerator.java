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
package jsl.modeling.elements.processview.description;

import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.entity.EntityGenerator;
import jsl.modeling.elements.entity.Entity;
import jsl.modeling.elements.entity.EntityType;
import jsl.modeling.elements.entity.NoEntityTypeSpecifiedException;
import jsl.utilities.random.RandomIfc;

/**
 *
 */
public class EntityProcessGenerator extends EntityGenerator {

    /** A reference to the process description for this generator
     *
     */
    protected ProcessDescription myProcessDescription;

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription) {
        this(parent, processDescription, null, null, Long.MAX_VALUE, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param name the name of the model element
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription, String name) {
        this(parent, processDescription, null, null, Long.MAX_VALUE, Double.POSITIVE_INFINITY, name);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param timeUntilFirst the time until the first generation
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst) {
        this(parent, processDescription, timeUntilFirst, null, Long.MAX_VALUE, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param name the name of the model element
     * @param timeUntilFirst the time until the first generation
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst, String name) {
        this(parent, processDescription, timeUntilFirst, null, Long.MAX_VALUE, Double.POSITIVE_INFINITY, name);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param name the name of the model element
     * @param timeUntilFirst the time until the first generation
     * @param timeUntilNext the time between generations
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst, RandomIfc timeUntilNext, String name) {
        this(parent, processDescription, timeUntilFirst, timeUntilNext, Long.MAX_VALUE, Double.POSITIVE_INFINITY, name);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param timeUntilFirst the time until the first generation
     * @param timeUntilNext the time between generations
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst, RandomIfc timeUntilNext) {
        this(parent, processDescription, timeUntilFirst, timeUntilNext, Long.MAX_VALUE, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param name the name of the model element
     * @param timeUntilFirst the time until the first generation
     * @param timeUntilNext the time between generations
     * @param maxNum the maximum number of generations
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst, RandomIfc timeUntilNext, long maxNum, String name) {
        this(parent, processDescription, timeUntilFirst, timeUntilNext, maxNum, Double.POSITIVE_INFINITY, name);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param timeUntilFirst the time until the first generation
     * @param timeUntilNext the time between generations
     * @param maxNum the maximum number of generations
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst, RandomIfc timeUntilNext, long maxNum) {
        this(parent, processDescription, timeUntilFirst, timeUntilNext, maxNum, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param timeUntilFirst the time until the first generation
     * @param timeUntilNext the time between generations
     * @param maxNum the maximum number of generations
     * @param timeUntilLast the time until the last event
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst, RandomIfc timeUntilNext, long maxNum, double timeUntilLast) {
        this(parent, processDescription, timeUntilFirst, timeUntilNext, maxNum, timeUntilLast, null);
    }

    /**
     * @param parent the parent model element
     * @param processDescription the ProcessDescription
     * @param timeUntilFirst the time until the first generation
     * @param timeUntilNext the time between generations
     * @param maxNum the maximum number of generations
     * @param timeUntilLast the time until the last event
     * @param name the name of the generator
     */
    public EntityProcessGenerator(ModelElement parent, ProcessDescription processDescription,
            RandomIfc timeUntilFirst, RandomIfc timeUntilNext,
            long maxNum, double timeUntilLast, String name) {
        super(parent, timeUntilFirst, timeUntilNext, maxNum, timeUntilLast, name);
        setProcessDescription(processDescription);
    }

    /** Returns a reference to the process description for this generator
     * @return A reference to the process description for this generator
     */
    protected final ProcessDescription getProcessDescription() {
        return (myProcessDescription);
    }

    /** Sets the process description for this generator
     *
     * @param processDescription The ProcessDescription
     */
    protected void setProcessDescription(ProcessDescription processDescription) {
        if (processDescription == null) {
            throw new IllegalArgumentException("ProcessDescription must be non-null!");
        }
        myProcessDescription = processDescription;
    }

    @Override
    protected void generate(JSLEvent event) {
//TODO this will not work
        EntityType et = getEntityType();

        if (et == null) {
            throw new NoEntityTypeSpecifiedException("No entity type was " +
                    "provided for the generator");
        }

        // create the entity
        Entity e = et.createEntity();
        ProcessDescription pd = getProcessDescription();
        ProcessExecutor pe = pd.createProcessExecutor(e);
        pe.initialize();
        pe.start();

    }
}
