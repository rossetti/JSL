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
package jsl.modeling.queue;

/** This interface should be implemented by classes that need to be notified
 *  when an object gets enqueued or when the object gets removed (via removeNext()) on
 *  a queue
 *
 * @param <T> the sub-type of QObject that is associated with the updating
 */
public interface QueueListenerIfc<T extends QObject> {

    void update(T qObject);
}
