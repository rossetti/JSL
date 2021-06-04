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

import jsl.modeling.queue.Queue;

/**
 * A convenience class that implements the RequestReactorIfc.
 * Clients can sub-class this base class and override only those methods that they are interested in.
 * It would be very common to just override the completed() method
 *
 * @author rossetti
 */
public class RequestReactorAdapter implements RequestReactorIfc {

    @Override
    public void enqueued(Request request, Queue<Request> queue) {
    }

    @Override
    public void dequeued(Request request, Queue<Request> queue) {
    }

    @Override
    public void canceled(Request request) {
    }

    @Override
    public void preempted(Request request) {
    }

    @Override
    public void allocated(Request request) {
    }

    @Override
    public void completed(Request request) {
    }

    @Override
    public void prepared(Request request) {
    }

    @Override
    public void resumed(Request request) {
    }

}
