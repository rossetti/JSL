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
package jsl.modeling.elements.entity;

import java.util.List;

/** This interface governs the selection of resources from
 *  a set
 *
 * @author rossetti
 */
public interface ResourceSelectionRuleIfc {

    /** Finds a resource within the supplied list that has
     *  getNumberAvailable() &gt; = amtNeeded or returns null
     *
     *
     * @param list
     * @param amtNeeded
     * @return
     */
    public Resource selectAvailableResource(List<Resource> list, int amtNeeded);

    /** Places the supplied resource (if resource.hasAvailableUnits()) in the list
     *
     * @param list
     * @param resource
     */
    public void addAvailableResource(List<Resource> list, Resource resource);

    /** Selects an available resource from the list or returns null if none
     *  are found
     *
     * @param list
     * @return
     */
    public Resource selectAvailableResource(List<Resource> list);

}
