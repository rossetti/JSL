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
package jsl.modeling.resource;

/**
 *
 * @author rossetti
 */
public interface SeizeableIfc {

    /**
     * Causes the request to enter the resource. If the resource is idle, the
     * request will be using the resource. If the resource is not idle the
     * request will wait. A Request will be rejected if its preemption rule is
     * NONE and the
     * ResourceUnit's failure delay option is false. This implies that the
     * Request cannot be processed by the ResourceUnit because the request
     * cannot be preempted and the resource unit does not permit its failures to
     * delay (i.e. they must preempt).
     *
     * @param request a request made by this unit
     * @return the request is returned to emphasize that the user may want to
     * check its state
     */
    Request seize(Request request);
    
}
