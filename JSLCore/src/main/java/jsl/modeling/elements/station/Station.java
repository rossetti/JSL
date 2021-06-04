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

import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.modeling.queue.QObject;
//import jsl.utilities.reporting.JSL;

/** A Station represents a location that can receive QObjects for
 *  processing. Sub-classes of Station must supply an implementation of the 
 *  ReceiveQObjectIfc interface.
 * 
 *  A Station may or may not have a helper object that implements the 
 *  SendQObjectIfc interface.  If this helper object is supplied it will
 *  be used to send the processed QObject to its next location for
 *  processing.
 * 
 *  A Station may or may not have a helper object that implements the 
 *  ReceiveQObjectIfc interface.  If this helper object is supplied and
 *  the SendQObjectIfc helper is not supplied, then the object that implements
 *  the ReceiveQObjectIfc will be the next receiver for the QObject
 * 
 *  If neither helper object is supplied then a runtime exception will
 *  occur when trying to use the send() method
 *
 * @author rossetti
 */
public abstract class Station extends SchedulingElement implements ReceiveQObjectIfc {

    /**
     * Can be supplied in order to provide logic
     *  to send the QObject to its next receiver
     */
    private SendQObjectIfc mySender;

    /** Can be used to directly tell the receiver to receive the departing
     *  QObject
     * 
     */
    private ReceiveQObjectIfc myNextReceiver;

    /**
     *
     * @param parent the parent model element
     */
    public Station(ModelElement parent) {
        this(parent, null, null);
    }

    /**
     *
     * @param parent the parent model element
     * @param name a unique name
     */
    public Station(ModelElement parent, String name) {
        this(parent, null, name);
    }

    /**
     * 
     * @param parent the parent model element
     * @param sender can be null, represents something that can send QObjects
     * @param name a unique name
     */
    public Station(ModelElement parent, SendQObjectIfc sender, String name) {
        super(parent, name);
        setSender(sender);
    }

    /**
     * A Station may or may not have a helper object that implements the 
     *  SendQObjectIfc interface.  If this helper object is supplied it will
     *  be used to send the processed QObject to its next location for
     *  processing.
     * @return the thing that will be used to send the completed QObject
     */
    public final SendQObjectIfc getSender() {
        return mySender;
    }

    /**
     * A Station may or may not have a helper object that implements the 
     *  SendQObjectIfc interface.  If this helper object is supplied it will
     *  be used to send the processed QObject to its next location for
     *  processing.
     * @param sender the thing that will be used to send the completed QObject
     */
    public final void setSender(SendQObjectIfc sender) {
//        if (sender == null){
//            JSL.LOGGER.warn("The sender for station {} was set to null!", getName());
//        }
        mySender = sender;
    }

    /**
     *  A Station may or may not have a helper object that implements the 
     *  ReceiveQObjectIfc interface.  If this helper object is supplied and
     *  the SendQObjectIfc helper is not supplied, then the object that implements
     *  the ReceiveQObjectIfc will be the next receiver for the QObject when using 
     *  default send() method.
     * @return the thing that should receive the completed QObject, may be null
     */
    public final ReceiveQObjectIfc getNextReceiver() {
        return myNextReceiver;
    }

    /**
     *  A Station may or may not have a helper object that implements the 
     *  ReceiveQObjectIfc interface.  If this helper object is supplied and
     *  the SendQObjectIfc helper is not supplied, then the object that implements
     *  the ReceiveQObjectIfc will be the next receiver for the QObject when using 
     *  default send() method.
     * @param receiver the thing that should receive the completed QObject, may be null
     */
    public final void setNextReceiver(ReceiveQObjectIfc receiver) {
//        if (receiver == null){
//            JSL.LOGGER.warn("The receiver for station {} was set to null!", getName());
//        }
        myNextReceiver = receiver;
    }

    /**
     *  A Station may or may not have a helper object that implements the 
     *  SendQObjectIfc interface.  If this helper object is supplied it will
     *  be used to send the processed QObject to its next location for
     *  processing.
     * 
     *  A Station may or may not have a helper object that implements the 
     *  ReceiveQObjectIfc interface.  If this helper object is supplied and
     *  the SendQObjectIfc helper is not supplied, then the object that implements
     *  the ReceiveQObjectIfc will be the next receiver for the QObject
     * 
     *  If neither helper object is supplied then a runtime exception will
     *  occur when trying to use the send() method     
     * @param qObj the completed QObject
     */
    protected void send(QObject qObj) {
        if (getSender() != null) {
            getSender().send(qObj);
        } else if (getNextReceiver() != null) {
            getNextReceiver().receive(qObj);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("There was no sender or receiver for station: ");
            sb.append(getName());
            sb.append(System.lineSeparator());
            sb.append(", both had null values.  Make sure to create the receiver or sender");
            sb.append(System.lineSeparator());
            sb.append(" before using setNextReceiver() or setSender() methods!");
            throw new RuntimeException(sb.toString());
        }
    }

}
