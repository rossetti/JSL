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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jsl.modeling.elements.entity;

/**
 *
 * @author rossetti
 */
public interface SeizeIfc {

    /**
     * Seizes the resource using the request.
     * Conditions:
     * 1) request must not be null
     * 2) request.getEntity() must not be null
     * 3) The request must not have been seized with another resource
     * 4) The request must have a ResourceAllocationListener attached.
     *
     * @param request
     */
    void seize(Request request);

}
