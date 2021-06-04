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

import java.util.List;

/** This station will receive a QObject and immediately
 *  send it out to a randomly selected receiver
 *
 * @author rossetti
 */
public class NWayByChanceStationSender extends Station implements RandomElementIfc {

    protected NWayByChanceQObjectSender myNWaySender;
    
    public NWayByChanceStationSender(ModelElement parent, List<ReceiveQObjectIfc> elements, double[] cdf) {
        this(parent, elements, cdf, null);
    }

    public NWayByChanceStationSender(ModelElement parent, List<ReceiveQObjectIfc> elements, double[] cdf, String name) {
        super(parent, name);
        myNWaySender = new NWayByChanceQObjectSender(this, elements, cdf);
        setSender(myNWaySender);
    }

    @Override
    public void receive(QObject qObj) {
        send(qObj);
    }

    public final int size() {
        return myNWaySender.size();
    }

    public final boolean isEmpty() {
        return myNWaySender.isEmpty();
    }

    public final int indexOf(Object arg0) {
        return myNWaySender.indexOf(arg0);
    }

    public final boolean contains(Object arg0) {
        return myNWaySender.contains(arg0);
    }

    @Override
    public void resetStartStream() {
        myNWaySender.resetStartStream();
    }

    @Override
    public void resetStartSubstream() {
        myNWaySender.resetStartSubstream();
    }

    @Override
    public void advanceToNextSubstream() {
        myNWaySender.advanceToNextSubstream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myNWaySender.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myNWaySender.getAntitheticOption();
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myNWaySender.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myNWaySender.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myNWaySender.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myNWaySender.setResetStartStreamOption(b);
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        myNWaySender.setRandomNumberStream(stream);
    }

    @Override
    public void setRandomNumberStream(int streamNumber) {
        myNWaySender.setRandomNumberStream(streamNumber);
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myNWaySender.getRandomNumberStream();
    }

    @Override
    public int getStreamNumber() {
        return myNWaySender.getStreamNumber();
    }
}
