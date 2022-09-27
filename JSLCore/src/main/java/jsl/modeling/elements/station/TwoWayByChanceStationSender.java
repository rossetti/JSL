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
package jsl.modeling.elements.station;

import jsl.modeling.elements.RandomElementIfc;
import jsl.simulation.ModelElement;
import jsl.modeling.queue.QObject;
import jsl.utilities.random.rng.RNStreamIfc;

/** This station will receive a QObject and immediately
 *  send it out to one of two randomly selected receivers
 * 
 * @author rossetti
 */
public class TwoWayByChanceStationSender extends Station implements RandomElementIfc {

    protected TwoWayByChanceQObjectSender myTwoWaySender;
    
    public TwoWayByChanceStationSender(ModelElement parent, double p,
            ReceiveQObjectIfc r1, ReceiveQObjectIfc r2) {
        this(parent, null, p, r1, r2);
    }

    public TwoWayByChanceStationSender(ModelElement parent, String name, double p,
            ReceiveQObjectIfc r1, ReceiveQObjectIfc r2) {
        super(parent, name);
        myTwoWaySender = new TwoWayByChanceQObjectSender(this, p, r1, r2);
        setSender(myTwoWaySender);
    }

    public final void setSecondReceiver(ReceiveQObjectIfc r2) {
        myTwoWaySender.setSecondReceiver(r2);
    }

    public final void setFirstReceiver(ReceiveQObjectIfc r1) {
        myTwoWaySender.setFirstReceiver(r1);
    }

    @Override
    public void receive(QObject qObj) {
        send(qObj);
    }

    @Override
    public void resetStartStream() {
        myTwoWaySender.resetStartStream();
    }

    @Override
    public void resetStartSubStream() {
        myTwoWaySender.resetStartSubStream();
    }

    @Override
    public void advanceToNextSubStream() {
        myTwoWaySender.advanceToNextSubStream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myTwoWaySender.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myTwoWaySender.getAntitheticOption();
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        myTwoWaySender.setRandomNumberStream(stream);
    }

    @Override
    public void setRandomNumberStream(int streamNumber) {
        myTwoWaySender.setRandomNumberStream(streamNumber);
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myTwoWaySender.getRandomNumberStream();
    }

    @Override
    public int getStreamNumber() {
        return myTwoWaySender.getStreamNumber();
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myTwoWaySender.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myTwoWaySender.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myTwoWaySender.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myTwoWaySender.setResetStartStreamOption(b);
    }
}
