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
package jsl.modeling.elements.component;

import java.util.LinkedList;
import java.util.List;

import jsl.simulation.ModelElement;

public class ComponentAssembly extends ComponentStateChangeListener {

    protected List<Component> myComponents;

    public ComponentAssembly(ModelElement parent) {
        this(parent, null);
    }

    public ComponentAssembly(ModelElement parent, String name) {
        super(parent, name);
        myComponents = new LinkedList<Component>();
    }

    /**
     * Adds the component to the assembly
     *
     * @param component, must not be null and must not be already part of
     * another assembly
     */
    public void addComponent(Component component) {

        if (component == null) {
            throw new IllegalArgumentException("The component was null!");
        }

        if (component.getAssembly() != null) {
            throw new IllegalArgumentException("The component is already part of an assembly.");
        }

        // add the component
        myComponents.add(component);
        component.setAssembly(this);
    }

    /**
     * Checks if the assembly contains the supplied component
     *
     * @param component
     * @return
     */
    public boolean contains(Component component) {
        return myComponents.contains(component);
    }

    /**
     * Removes the component from the assembly. It must be part of the assembly.
     *
     * @param component, must not be null
     */
    public void removeComponent(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("The component was null!");
        }

        if (component.getAssembly() != this) {
            throw new IllegalArgumentException("The component is not part of this assembly.");
        }

        myComponents.remove(component);
        component.setAssembly(null);
    }

}
