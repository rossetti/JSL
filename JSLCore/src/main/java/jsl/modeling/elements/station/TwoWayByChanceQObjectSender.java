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
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.queue.QObject;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.BernoulliRV;

/** This model element randomly selects between two receivers 
 *  (objects that implement ReceiveQObjectIfc) and sends the
 *  QObject to the chosen receiver.  The first receiver is 
 *  chosen with probability p and the second receiver is chosen
 *  with probability 1-p
 *
 * @author rossetti
 */
public class TwoWayByChanceQObjectSender extends ModelElement implements SendQObjectIfc, RandomElementIfc {

    private RandomVariable myRV;

    private ReceiveQObjectIfc myR1;

    private ReceiveQObjectIfc myR2;

    public TwoWayByChanceQObjectSender(ModelElement parent, double p,
            ReceiveQObjectIfc r1, ReceiveQObjectIfc r2) {
        this(parent, null, p, r1, r2);
    }

    public TwoWayByChanceQObjectSender(ModelElement parent, String name, double p,
            ReceiveQObjectIfc r1, ReceiveQObjectIfc r2) {
        super(parent, name);
        setFirstReceiver(r1);
        setSecondReceiver(r2);
        myRV = new RandomVariable(this, new BernoulliRV(p));
    }

    public final void setFirstReceiver(ReceiveQObjectIfc r1) {
        if (r1 == null) {
            throw new IllegalArgumentException("Receiver 1 was null");
        }
        myR1 = r1;
    }

    public final void setSecondReceiver(ReceiveQObjectIfc r2) {
        if (r2 == null) {
            throw new IllegalArgumentException("Receiver 2 was null");
        }
        myR2 = r2;
    }

    @Override
    public void send(QObject qObj) {
        if (myRV.getValue() == 1.0) {
            myR1.receive(qObj);
        } else {
            myR2.receive(qObj);
        }
    }

    @Override
    public void resetStartStream() {
        myRV.resetStartStream();
    }

    @Override
    public void resetStartSubStream() {
        myRV.resetStartSubStream();
    }

    @Override
    public void advanceToNextSubStream() {
        myRV.advanceToNextSubStream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myRV.setAntitheticOption(flag);
    }

    @Override
    public boolean getAntitheticOption() {
        return myRV.getAntitheticOption();
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        myRV.setRandomNumberStream(stream);
    }

    @Override
    public void setRandomNumberStream(int streamNumber) {
        myRV.setRandomNumberStream(streamNumber);
    }

    @Override
    public RNStreamIfc getRandomNumberStream() {
        return myRV.getRandomNumberStream();
    }

    @Override
    public int getStreamNumber() {
        return myRV.getStreamNumber();
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myRV.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myRV.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myRV.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myRV.setResetStartStreamOption(b);
    }
}
