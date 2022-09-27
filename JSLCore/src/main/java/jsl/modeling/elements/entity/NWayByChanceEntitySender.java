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

import jsl.simulation.ModelElement;
import jsl.modeling.elements.RandomElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ConstantRV;

import java.util.List;

/**
 *
 * @author rossetti
 */
public class NWayByChanceEntitySender extends EntityReceiver {

    private RandomElement<GetEntityReceiverIfc> mySelector;

     private RandomVariable myTime;

    public NWayByChanceEntitySender(ModelElement parent, List<GetEntityReceiverIfc> elements, double[] cdf) {
        this(parent, elements, cdf, null, null);
    }

    public NWayByChanceEntitySender(ModelElement parent, List<GetEntityReceiverIfc> elements, double[] cdf,
                                    RandomIfc time, String name) {
        super(parent, name);
        mySelector = new RandomElement<GetEntityReceiverIfc>(this, elements, cdf);
        setTransferTime(time);
        setEntitySender(new Sender());
    }

    /** Sets the transfer time
     * 
     * @param time 
     */
    public final void setTransferTime(double time) {
        setTransferTime(new ConstantRV(time));
    }

    /** If the supplied value is null, then zero is used for the time
     * 
     * @param time 
     */
    public final void setTransferTime(RandomIfc time) {
        if (time == null){
            time = ConstantRV.ZERO;
        }
        if (myTime == null) {
            myTime = new RandomVariable(this, time);
        } else {
            myTime.setInitialRandomSource(time);
        }
    }

    public final int size() {
        return mySelector.size();
    }

    public final void setResetStartStreamOption(boolean b) {
        mySelector.setResetStartStreamOption(b);
    }

    public final void setResetNextSubStreamOption(boolean b) {
        mySelector.setResetNextSubStreamOption(b);
    }

    public final void setAntitheticOption(boolean flag) {
        mySelector.setAntitheticOption(flag);
    }

    public final void resetStartSubstream() {
        mySelector.resetStartSubStream();
    }

    public final void resetStartStream() {
        mySelector.resetStartStream();
    }

    public final boolean isEmpty() {
        return mySelector.isEmpty();
    }

    public final boolean getResetStartStreamOption() {
        return mySelector.getResetStartStreamOption();
    }

    public final boolean getResetNextSubStreamOption() {
        return mySelector.getResetNextSubStreamOption();
    }

    public final boolean getAntitheticOption() {
        return mySelector.getAntitheticOption();
    }

    public final void advanceToNextSubStream() {
        mySelector.advanceToNextSubStream();
    }

    @Override
    protected final void receive(Entity entity) {
        sendEntity(entity);
    }

    private class Sender implements EntitySenderIfc {
        @Override
        public void sendEntity(Entity e) {
            e.sendViaReceiver(mySelector.getRandomElement().getEntityReceiver(), myTime);
        }
    }
}
