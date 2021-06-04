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

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author rossetti
 */
public class CyclicResourceSelectionRule implements ResourceSelectionRuleIfc {

    /** Returns the next available resource to be used for allocating
     *  to requests, null if none are found that can satisfy the request.
     *
     *  The default selection method is to cycle through the resources in the
     *  order in which they were RELEASED.
     *
     *  The idle resource set starts in the order the resources were added to the
     *  resource set and assumes that the first resource listed was the resource that
     *  was allocated the longest time in the past, and so on.  For example,
     *  if the set of idle resources has the following list:
     *
     *  Resource B
     *  Resource A
     *  Resource C
     *
     *  Then C was the most recently used, resource A the next most recently used, and
     *  resource B the oldest used.  Resource B will be next allocated, if it can
     *  satisfy the amount of the request. If not, Resource A will be checked, and so forth
     *  until a resource that can satisfy the request is found. Thus, it is entirely
     *  possible that B will not be the next recommended resource. The selected resource
     *  is added to the end of the list after it is released.
     *
     *  Note that the the order can vary with this list depending on the order in which the resources are
     *  released.  To provide alternative behavior either override
     *  this method, or provide a ResourceSelectionRuleIfc
     *
     *  The default behavior is to return the first available resource
     *  that can fully supply the amount needed
     *
     * @param amtNeeded
     * @return
     */
    public Resource selectAvailableResource(List<Resource> list, int amtNeeded) {
        Resource found = null;

        if (!list.isEmpty()) {
            Iterator<Resource> iter = list.iterator();
            Resource r = null;
            int amt = amtNeeded;
            while (iter.hasNext()) {
                r = iter.next();
                if (amt <= r.getNumberAvailable()) {
                    found = r;
                    break;
                }
            }
        }
        return (found);
    }

    public void addAvailableResource(List<Resource> list, Resource resource) {
        if (!list.contains(resource)) {
            list.add(resource);
        }
    }

    /** Selects an available resource or null if none are available
     *  The default is to find the first available resource
     *  that has the maximum available units. To change this
     *  either override this method or supply a ResourceSelectionRuleIfc
     *
     * @return
     */
    public Resource selectAvailableResource(List<Resource> list) {
        // the default is the resource in the idle resource list
        // that has the max available units
        int max = Integer.MIN_VALUE;
        Resource rmax = null;
        for (Resource r : list) {
            if (r.getNumberAvailable() > max) {
                rmax = r;
            }
        }
        return (rmax);
    }
}
