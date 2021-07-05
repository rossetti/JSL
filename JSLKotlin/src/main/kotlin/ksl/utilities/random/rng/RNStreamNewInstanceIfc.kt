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
package ksl.utilities.random.rng

/**
 * The new instance has the same state as the underlying stream.  This is a new stream
 * but it has the same state (starting values, etc.)
 * @author rossetti
 */
interface RNStreamNewInstanceIfc {
    /** Returns a copy of the stream with
     * exactly the same state
     *
     * @return Returns a copy of the stream with
     * exactly the same state
     */
    fun newInstance(): RNStreamIfc

    /** Returns a copy of the stream that
     * has exactly the same state
     *
     * @param name  the name of the new instance
     * @return Returns a copy of the stream with
     * exactly the same state
     */
    fun newInstance(name: String?): RNStreamIfc
}