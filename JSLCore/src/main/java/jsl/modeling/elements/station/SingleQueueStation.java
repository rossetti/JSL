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
package jsl.modeling.elements.station;

import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.queue.Queue.Discipline;
import jsl.modeling.queue.QueueListenerIfc;
import jsl.modeling.queue.QueueResponse;
import jsl.utilities.GetValueIfc;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.statistic.StatisticAccessorIfc;
import jsl.utilities.statistic.WeightedStatisticIfc;

import java.util.Optional;

/**
 * Models a service station with a resource that has a single queue to hold
 * waiting customers. Customers can only use 1 unit of the resource while in
 * service.
 *
 * @author rossetti
 */
public class SingleQueueStation extends Station {

    protected final Queue<QObject> myWaitingQ;

    private GetValueIfc myServiceTime;

    protected final TimeWeighted myNS;

    protected final SResource myResource;

    private final EndServiceAction myEndServiceAction;

    private boolean myUseQObjectSTFlag;

    /**
     * Uses a resource with capacity 1 and service time Constant.ZERO
     *
     * @param parent
     */
    public SingleQueueStation(ModelElement parent) {
        this(parent, null, ConstantRV.ZERO, null, null);
    }

    /**
     * Uses a resource with capacity 1
     *
     * @param parent
     * @param sd
     */
    public SingleQueueStation(ModelElement parent, GetValueIfc sd) {
        this(parent, null, sd, null, null);
    }

    /**
     * Uses a resource with capacity 1 and service time Constant.ZERO
     *
     * @param parent
     * @param name
     */
    public SingleQueueStation(ModelElement parent, String name) {
        this(parent, null, ConstantRV.ZERO, null, name);
    }

    /**
     * Uses a resource with capacity 1
     *
     * @param parent
     * @param sd
     * @param name
     */
    public SingleQueueStation(ModelElement parent, GetValueIfc sd, String name) {
        this(parent, null, sd, null, name);
    }

    /**
     * No sender is provided.
     *
     * @param parent
     * @param resource
     */
    public SingleQueueStation(ModelElement parent, SResource resource) {
        this(parent, resource, null, null, null);
    }

    /**
     * No sender is provided.
     *
     * @param parent
     * @param resource
     * @param sd
     */
    public SingleQueueStation(ModelElement parent, SResource resource,
            GetValueIfc sd) {
        this(parent, resource, sd, null, null);
    }

    /**
     * No sender is provided.
     *
     * @param parent
     * @param resource
     * @param sd
     * @param name
     */
    public SingleQueueStation(ModelElement parent, SResource resource,
            GetValueIfc sd, String name) {
        this(parent, resource, sd, null, name);
    }

    /**
     * Default resource of capacity 1 is used
     *
     * @param parent
     * @param sd
     * @param sender
     * @param name
     */
    public SingleQueueStation(ModelElement parent, GetValueIfc sd,
            SendQObjectIfc sender, String name) {
        this(parent, null, sd, sender, name);
    }

    /**
     *
     * @param parent
     * @param resource
     * @param sd Represents the time using the resource
     * @param sender handles sending to next
     * @param name
     */
    public SingleQueueStation(ModelElement parent, SResource resource,
            GetValueIfc sd, SendQObjectIfc sender, String name) {
        super(parent, name);
        setSender(sender);
        if (resource == null) {
            myResource = new SResource(this, 1, getName() + ":R");
        } else {
            myResource = resource;
        }
        setServiceTime(sd);
        myWaitingQ = new Queue<>(this, getName() + ":Q");
        myNS = new TimeWeighted(this, 0.0, getName() + ":NS");
        myUseQObjectSTFlag = false;
        myEndServiceAction = new EndServiceAction();
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    protected double getServiceTime(QObject customer) {
        double t;
        if (getUseQObjectServiceTimeOption()) {
            Optional<GetValueIfc> valueObject = customer.getValueObject();
            if (valueObject.isPresent()){
                t = valueObject.get().getValue();
            } else {
                throw new IllegalStateException("Attempted to use QObject.getValueObject() when no object was set");
            }
        } else {
            t = getServiceTime().getValue();
        }
        return t;
    }

    /**
     * Called to determine which waiting QObject will be served next Determines
     * the next customer, seizes the resource, and schedules the end of the
     * service.
     */
    protected void serveNext() {
        QObject customer = myWaitingQ.removeNext(); //remove the next customer
        myResource.seize();
        // schedule end of service
        scheduleEvent(myEndServiceAction, getServiceTime(customer), customer);
    }

    @Override
    public void receive(QObject customer) {
        myNS.increment(); // new customer arrived
        myWaitingQ.enqueue(customer); // enqueue the newly arriving customer
        if (isResourceAvailable()) { // server available
            serveNext();
        }
    }

    public Optional<QueueResponse<QObject>> getQueueResponses() {
        return myWaitingQ.getQueueResponses();
    }

    class EndServiceAction implements EventActionIfc<QObject> {

        @Override
        public void action(JSLEvent<QObject> event) {
            QObject leavingCustomer = event.getMessage();
            myNS.decrement(); // customer departed
            myResource.release();
            if (isQueueNotEmpty()) { // queue is not empty
                serveNext();
            }
            send(leavingCustomer);
        }
    }

    /**
     * Tells the station to use the QObject to determine the service time
     *
     * @param option true means the station uses the QObject's getValueObject() to determine the service time
     */
    public final void setUseQObjectServiceTimeOption(boolean option) {
        myUseQObjectSTFlag = option;
    }

    /**
     * Whether or not the station uses the QObject to determine the service time
     *
     * @return true means the station uses the QObject's getValueObject() to determine the service time
     */
    public final boolean getUseQObjectServiceTimeOption() {
        return myUseQObjectSTFlag;
    }

    /**
     * The current number in the queue
     *
     * @return The current number in the queue
     */
    public final int getNumberInQueue() {
        return myWaitingQ.size();
    }

    /**
     * The current number in the station (in queue + in service)
     *
     * @return current number in the station (in queue + in service)
     */
    public final int getNumberInStation() {
        return (int) myNS.getValue();
    }

    /**
     * The initial capacity of the resource at the station
     *
     * @return initial capacity of the resource at the station
     */
    public final int getInitialResourceCapacity() {
        return (myResource.getInitialCapacity());
    }

    /**
     * Sets the initial capacity of the station's resource
     *
     * @param capacity the initial capacity of the station's resource
     */
    public final void setInitialCapacity(int capacity) {
        myResource.setInitialCapacity(capacity);
    }

    /**
     * If the service time is null, it is assumed to be zero
     *
     * @param st the GetValueIfc implementor that provides the service time
     */
    public final void setServiceTime(GetValueIfc st) {
        if (st == null) {
            st = ConstantRV.ZERO;
        }
        myServiceTime = st;
    }

    /**
     * The object used to determine the service time when not using the QObject
     * option
     *
     * @return the object used to determine the service time when not using the QObject
     */
    public final GetValueIfc getServiceTime() {
        return myServiceTime;
    }

    /**
     * Across replication statistics on the number busy servers
     *
     * @return Across replication statistics on the number busy servers
     */
    public final StatisticAccessorIfc getNBAcrossReplicationStatistic() {
        return myResource.getNBAcrossReplicationStatistic();
    }

    /**
     * Across replication statistics on the number in system
     *
     * @return Across replication statistics on the number in system
     */
    public final StatisticAccessorIfc getNSAcrossReplicationStatistic() {
        return myNS.getAcrossReplicationStatistic();
    }

    /**
     * Within replication statistics on the number in system
     *
     * @return Within replication statistics on the number in system
     */
    public final WeightedStatisticIfc getNSWithinReplicationStatistic() {
        return myNS.getWithinReplicationStatistic();
    }

    /**
     *
     * @return true if a resource has available units
     */
    public final boolean isResourceAvailable() {
        return myResource.hasAvailableUnits();
    }

    /**
     * The capacity of the resource. Maximum number of units that can be busy.
     *
     * @return The capacity of the resource. Maximum number of units that can be busy.
     */
    public final int getCapacity() {
        return myResource.getCapacity();
    }

    /**
     * Current number of busy servers
     *
     * @return Current number of busy servers
     */
    public final int getNumBusyServers() {
        return myResource.getNumBusy();
    }

    /**
     * Fraction of the capacity that is busy.
     *
     * @return  Fraction of the capacity that is busy.
     */
    public final double getFractionBusy() {
        double capacity = getCapacity();
        return getNumBusyServers() / capacity;
    }

    /**
     * Whether the queue is empty
     *
     * @return Whether the queue is empty
     */
    public final boolean isQueueEmpty() {
        return myWaitingQ.isEmpty();
    }

    /**
     * Whether the queue is not empty
     *
     * @return Whether the queue is not empty
     */
    public final boolean isQueueNotEmpty() {
        return myWaitingQ.isNotEmpty();
    }

    /**
     * Adds a QueueListenerIfc to the underlying queue
     *
     * @param listener the listener to queue state changes
     * @return true if added
     */
    public final boolean addQueueListener(QueueListenerIfc<QObject> listener) {
        return myWaitingQ.addQueueListener(listener);
    }

    /**
     * Removes a QueueListenerIfc from the underlying queue
     *
     * @param listener the listener to queue state changes
     * @return true if removed
     */
    public boolean removeQueueListener(QueueListenerIfc<QObject> listener) {
        return myWaitingQ.removeQueueListener(listener);
    }

    /**
     *
     * @param discipline the new discipline
     */
    public final void changeDiscipline(Discipline discipline) {
        myWaitingQ.changeDiscipline(discipline);
    }

    /**
     *
     * @return the initial queue discipline
     */
    public final Discipline getInitialDiscipline() {
        return myWaitingQ.getInitialDiscipline();
    }

    /**
     *
     * @param discipline the initial queue discipline
     */
    public final void setInitialDiscipline(Discipline discipline) {
        myWaitingQ.setInitialDiscipline(discipline);
    }

}
