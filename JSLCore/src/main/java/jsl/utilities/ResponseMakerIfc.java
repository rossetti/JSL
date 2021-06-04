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
package jsl.utilities;

/** Implementors of this interface should be able to return an instance of
 *  the Responses class or null
 *
 *
 */
public interface ResponseMakerIfc extends IdentityIfc {

    /** Returns a valid instance of Responses that can be used with
     *  this ResponseMakerIfc or null
     * 
     * @return
     */
    public Responses makeResponses();
}
