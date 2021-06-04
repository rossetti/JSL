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

package jsl.utilities.random.rng;

/**
 *
 * @author rossetti
 */
public interface RandomStreamManagerIfc extends RNStreamControlIfc {

    /** Returns the number of streams being managed
     *
     * @return
     */
    int size();

    /** Checks if the manager is empty (has no streams)
     *
     * @return
     */
    boolean isEmpty();

    /** Returns the index of the stream
     *
     * @param o
     * @return
     */
    int indexOf(RNStreamIfc o);

    /** Gets the stream at the supplied index
     *
     * @param index
     * @return
     */
    RNStreamControlIfc get(int index);

    /** Checks if the manager contains the supplied stream
     *
     * @param o
     * @return
     */
    boolean contains(RNStreamIfc o);

}
