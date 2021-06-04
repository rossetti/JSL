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
import jsl.utilities.GetValueIfc;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.statistic.StatisticAccessorIfc;
import jsl.utilities.statistic.WeightedStatisticIfc;

import java.util.Optional;

public class DelayStation extends Station {

    private boolean myUseQObjectSTFlag;

    private GetValueIfc myDelayTime;

    protected TimeWeighted myNS;

    private EndDelayAction myEndDelayAction;

    public DelayStation(ModelElement parent) {
        this(parent, ConstantRV.ZERO, null, null);
    }

    public DelayStation(ModelElement parent, GetValueIfc sd) {
        this(parent, sd, null, null);
    }

    public DelayStation(ModelElement parent, String name) {
        this(parent, ConstantRV.ZERO, null, name);
    }

    public DelayStation(ModelElement parent, GetValueIfc sd, String name) {
        this(parent, sd, null, name);
    }

    public DelayStation(ModelElement parent,
            GetValueIfc sd, SendQObjectIfc sender, String name) {
        super(parent, sender, name);
        setDelayTime(sd);
        myNS = new TimeWeighted(this, getName() + ":NumInStation");
        myEndDelayAction = new EndDelayAction();
    }

    public final void setDelayTime(GetValueIfc st) {
        myDelayTime = st;
    }

    public GetValueIfc getDelayTime() {
        return myDelayTime;
    }

    public final double getNumInStation() {
        return myNS.getValue();
    }

    /**
     * Tells the station to use the QObject to determine the service time
     *
     * @param option
     */
    public final void setUseQObjectDelayTimeOption(boolean option) {
        myUseQObjectSTFlag = option;
    }

    /**
     * Whether or not the station uses the QObject to determine the service time
     *
     * @return
     */
    public final boolean getUseQObjectDelayTimeOption() {
        return myUseQObjectSTFlag;
    }

    protected double getDelayTime(QObject customer) {
        double t;
        if (getUseQObjectDelayTimeOption()) {
            Optional<GetValueIfc> valueObject = customer.getValueObject();
            if (valueObject.isPresent()){
                t = valueObject.get().getValue();
            } else {
                throw new IllegalStateException("Attempted to use QObject.getValueObject() when no object was set");
            }
        } else {
            t = getDelayTime().getValue();
        }
        return t;
    }

    public final StatisticAccessorIfc getNSAcrossReplicationStatistic() {
        return myNS.getAcrossReplicationStatistic();
    }

    public final WeightedStatisticIfc getNSWithinReplicationStatistic() {
        return myNS.getWithinReplicationStatistic();
    }

    @Override
    public void receive(QObject customer) {
        myNS.increment(); // new customer arrived
        scheduleEvent(myEndDelayAction, getDelayTime(customer), customer);
    }

    class EndDelayAction implements EventActionIfc<QObject> {

        @Override
        public void action(JSLEvent<QObject> event) {
            QObject leavingCustomer = event.getMessage();
            myNS.decrement(); // customer departed
            send(leavingCustomer);
        }
    }
}
