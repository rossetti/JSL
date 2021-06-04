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

/** This interface is used by EntityReceiver if one of the
 *  default options is not specified.  A client can supply an instance of 
 *  a class that implements this interface in order to provide 
 *  a general method to send the entity to its next receiver
 *
 * @author rossetti
 */
public interface EntitySenderIfc {
    
    /** Generic method for sending an entity to a receiver
     * 
     * @param e
     */
    void sendEntity(Entity e);
}
