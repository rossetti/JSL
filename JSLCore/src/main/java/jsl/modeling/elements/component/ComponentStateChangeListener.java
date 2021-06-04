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

import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;

/**
 * A component state change listener can be attached to a component and will
 * have it's stateChange() method called at each state change of the component.
 *
 *
 */
abstract public class ComponentStateChangeListener extends SchedulingElement implements ComponentStateChangeListenerIfc {

    /**
     * Creates a component state change listener
     *
     * @param parent
     */
    public ComponentStateChangeListener(ModelElement parent) {
        this(parent, null);
    }

    /**
     * Creates a component state change listener
     *
     * @param parent
     * @param name
     */
    public ComponentStateChangeListener(ModelElement parent, String name) {
        super(parent, name);
    }

    @Override
    public void stateChange(Component c) {

        if (c.isAvailable()) {
            componentAvailable(c);
            if (c.isPreviousState(c.getCreatedState())) {
                // available after being created
                componentAvailableAfterCreation(c);
            } else if (c.isPreviousState(c.getDeactivatedState())) {
                // available after being deactivated
                componentAvailableAfterUnavailable(c);
            } else if (c.isPreviousState(c.getRepairingState())) {
                // available after being repaired
                componentAvailableAfterRepair(c);
            } else if (c.isPreviousState(c.getOperatingState())) {
                componentAvailableAfterOperating(c);
            }
        } else if (c.isOperating()) {
            componentStartedOperating(c);
        } else if (c.isFailed()) {
            componentFailed(c);
        } else if (c.isInRepair()) {
            componentStartedRepair(c);
        } else if (c.isUnavailable()) {
            componentUnavailable(c);
        }

    }

    /**
     * Called when the component transitions into the available state from any
     * other legal state
     *
     * @param c
     */
    protected void componentAvailable(Component c) {

    }

    /**
     * Called after componentAvailable() but only when the component enters from
     * the created state
     *
     * @param c
     */
    protected void componentAvailableAfterCreation(Component c) {

    }

    /**
     * Called after componentAvailable() but only when the component enters from
     * the repairing state
     *
     * @param c
     */
    protected void componentAvailableAfterRepair(Component c) {

    }

    /**
     * Called after componentAvailable() but only when the component enters from
     * the unavailable state
     *
     * @param c
     */
    protected void componentAvailableAfterUnavailable(Component c) {

    }

    /**
     * Called after componentAvailable() but only when the component enters from
     * the operating state
     *
     * @param c
     */
    protected void componentAvailableAfterOperating(Component c) {

    }

    /**
     * Called when the component transitions into the operating state
     *
     * @param c
     */
    protected void componentStartedOperating(Component c) {

    }

    /**
     * Called when the component transitions into the unavailable state
     *
     * @param c
     */
    protected void componentUnavailable(Component c) {

    }

    /**
     * Called when the component transitions into the failed state
     *
     * @param c
     */
    protected void componentFailed(Component c) {

    }

    /**
     * Called when the component transitions into the repairing state
     *
     * @param c
     */
    protected void componentStartedRepair(Component c) {

    }

}
