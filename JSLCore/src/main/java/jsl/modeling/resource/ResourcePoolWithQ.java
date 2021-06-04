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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import jsl.simulation.ModelElement;
import jsl.modeling.queue.Queue;
import jsl.modeling.queue.QueueListenerIfc;
import jsl.modeling.queue.QueueResponse;

/**
 *
 * @author rossetti
 */
public class ResourcePoolWithQ extends ResourcePool implements SeizeableIfc {

    private final Queue<Request> myRequestQ;

    public ResourcePoolWithQ(ModelElement parent, List<ResourceUnit> units) {
        this(parent, units, true, false, null);
    }

    public ResourcePoolWithQ(ModelElement parent, List<ResourceUnit> units,
            String name) {
        this(parent, units, true, false, name);
    }

    public ResourcePoolWithQ(ModelElement parent, List<ResourceUnit> units,
            boolean poolStatOption, boolean requestQStatsOption) {
        this(parent, units, poolStatOption, requestQStatsOption, null);
    }

    public ResourcePoolWithQ(ModelElement parent, List<ResourceUnit> units,
            boolean poolStatOption, boolean requestQStatsOption, String name) {
        super(parent, units, poolStatOption, name);
        myRequestQ = new Queue<>(this, getName() + ":RequestQ",
                Queue.Discipline.FIFO, requestQStatsOption);
    }

    public final boolean isQueue(Queue queue){
        return myRequestQ == queue;
    }
    
    public final Optional<QueueResponse<Request>> getQueueResponses() {
        return myRequestQ.getQueueResponses();
    }

    public final boolean getQueueStatsOption() {
        return myRequestQ.getQueueStatsOption();
    }

    public final List<Request> getUnmodifiableListOfRequestQ() {
        return myRequestQ.getUnmodifiableList();
    }

    public final boolean addQueueListener(QueueListenerIfc<Request> listener) {
        return myRequestQ.addQueueListener(listener);
    }

    public boolean removeQueueListener(QueueListenerIfc<Request> listener) {
        return myRequestQ.removeQueueListener(listener);
    }

    public final Queue.Status getRequestQStatus() {
        return myRequestQ.getStatus();
    }

    public final void changeQDiscipline(Queue.Discipline discipline) {
        myRequestQ.changeDiscipline(discipline);
    }

    public final Queue.Discipline getCurrentQDiscipline() {
        return myRequestQ.getCurrentDiscipline();
    }

    public final void changePriority(Request qObject, int priority) {
        myRequestQ.changePriority(qObject, priority);
    }

    public final Queue.Discipline getInitialQDiscipline() {
        return myRequestQ.getInitialDiscipline();
    }

    public final void setInitialQDiscipline(Queue.Discipline discipline) {
        myRequestQ.setInitialDiscipline(discipline);
    }

    public final int requestQSize() {
        return myRequestQ.size();
    }

    public final boolean isRequestQEmpty() {
        return myRequestQ.isEmpty();
    }

    @Override
    public Request seize(Request request) {
        Objects.requireNonNull(request, "The Request was null");
        if (request.getPreemptionRule() == Request.PreemptionRule.NONE) {
            if (getFailureDelayOption() == false) {
                if (hasFailureProcesses()) {
                    // only if it has failure processes attached can it be rejected in this case
                    request.reject(getTime());
                    return request;
                }
            }
            if (getInactivePeriodDelayOption() == false) {
                if (hasSchedules()){
                    // only if it has schedules attached can it be rejected in this case
                    request.reject(getTime());
                    return request;
                }
            }
        }
        // not rejected, proceed with normal processing
        request.makeReady(getTime());
        myRequestQ.enqueue(request);
        request.enterWaitingState(myRequestQ, getTime());
        if (hasIdleUnits()) {
            myRequestQ.remove(request);
            request.exitWaitingState(myRequestQ, getTime());
            selectResourceUnit().seize(request);
        }
        return request;
    }

    @Override
    protected void unitBecameIdle(ResourceUnit ru) {
        if (myRequestQ.isNotEmpty()) {
            Request request = myRequestQ.removeNext();
            request.exitWaitingState(myRequestQ, getTime());
            ru.seize(request);
        }
    }

}
