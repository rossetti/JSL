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
package jsl.modeling.elements.entity;

import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.entity.EntityType.SendOption;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;

/** Represents a base class for developing model elements that can
 *  receive entities.
 *
 * @author rossetti
 */
public abstract class EntityReceiver extends SchedulingElement
        implements GetEntityReceiverIfc {

    private Receiver myReceiver = new Receiver();

    /** The option used by the receiver for
     *  sending entities.
     * 
     */
    protected SendOption mySendOption = SendOption.NONE;

    /** Used to collect time spent at receiver
     *
     */
    protected ResponseVariable myTimeInReceiver;

    /** Used to collect number of entities at the receiver
     *
     */
    protected TimeWeighted myNumInReceiver;

    /** This can be used to directly specify the next receiver
     *
     */
    protected EntityReceiverAbstract myDirectEntityReceiver;

    /** If the EntityReceiver is part of a CompositeReceiver
     *  this attribute contains the reference to the composite
     *
     */
    protected EntityReceiver myComposite;
    
    /** Used if the sending option is
     * 
     */
    protected EntitySenderIfc mySender;

    public EntityReceiver(ModelElement parent) {
        this(parent, null);
    }

    public EntityReceiver(ModelElement parent, String name) {
        super(parent, name);
    }

    /** Causes the receiver to collect the time spent
     *  at the receiver.  This must be called
     *  prior to any replications
     *
     */
    public final void turnOnTimeInReceiverCollection() {
        if (myTimeInReceiver == null) {
            myTimeInReceiver = new ResponseVariable(this, getName() + " Receiver Time");
        }
    }

    /** Causes the receiver to collect the number of entity's
     *  at the receiver.  This must be called
     *  prior to any replications
     *
     */
    public final void turnOnNumberInReceiverCollection() {
        if (myNumInReceiver == null) {
            myNumInReceiver = new TimeWeighted(this, getName() + " Receiver WIP");
        }
    }

    protected void setComposite(EntityReceiver composite){
        myComposite = composite;
    }

    protected EntityReceiver getComposite(){
        return myComposite;
    }

    public boolean isPartOfComposite(){
        return myComposite != null;
    }

    /** Represents logic to correctly receive the entity and
     *  process it accordingly
     *
     * @param entity
     */
    abstract protected void receive(Entity entity);

    private class Receiver extends EntityReceiverAbstract {

        @Override
        protected void receive(Entity entity) {
            if (entity == null) {
                throw new IllegalArgumentException("The supplied entity was null");
            }
            if (myNumInReceiver != null) {
                myNumInReceiver.increment();
            }
            if (isPartOfComposite()){
                entity.setCurrentReceiver(myComposite);
            } else {
                entity.setCurrentReceiver(this);
                entity.setTimeEnteredReceiver(getTime());
            }
            EntityReceiver.this.receive(entity);
        }
    }

    /** Returns a reference to the underlying EntityReceiverAbstract
     *
     * @return
     */
    @Override
    public final EntityReceiverAbstract getEntityReceiver() {
        return myReceiver;
    }

    /** Can be used by sub-classes to send the entity
     *  to its next receiver according to one of the specified
     *  options. 
     *
     * @param e
     */
    protected void sendEntity(Entity e) {
        if (myNumInReceiver != null) {
            myNumInReceiver.decrement();
        }
        if (myTimeInReceiver != null){
            if (!isPartOfComposite()){
                myTimeInReceiver.setValue(getTime() - e.getTimeEnteredReceiver());
            }
        }
        
        // tell the entity to goto next receiver
        if (mySendOption == SendOption.DIRECT) {
            e.sendViaReceiver(myDirectEntityReceiver);
        } else if (mySendOption == SendOption.SEQ) {
            e.sendViaSequence();
        } else if (mySendOption == SendOption.BY_TYPE) {
            e.sendViaEntityType();
        } else if (mySendOption == SendOption.BY_SENDER) {
            mySender.sendEntity(e);
        } else if (mySendOption == SendOption.NONE){
            throw new NoEntityReceiverException("No sending option was specified");
        }
    }

    /** Supply a sender to be used to send the entity
     *  If null is supplied the option is set to SendOption.NONE
     * 
     * @param sender 
     */
    public final void setEntitySender(EntitySenderIfc sender){
        if (sender == null){
            mySendOption = SendOption.NONE;
        } else {
            mySendOption = SendOption.BY_SENDER; 
        }
        mySender = sender;
    }
    
    /** Sets the sending option
     *  SendOption {DIRECT, SEQ, BY_TYPE}
     *  DIRECT, client must use setDirectEntityReceiver() to set receiver
     *  SEQ, entity uses predefined sequence in its EntityType
     *  BY_TYPE, entity uses its EntityType to determine next receiver
     *
     * @param option
     */
    public final void setSendingOption(SendOption option) {
        if (option == SendOption.BY_SENDER){
            throw new IllegalArgumentException("Use setEntitySender() for By_SENDER optioin");
        }
        mySendOption = option;
    }

    /** An object that will directly receive the entity
     *
     * @return
     */
    public final EntityReceiverAbstract getDirectEntityReceiver() {
        return myDirectEntityReceiver;
    }

    /** Can be used to supply a direct receiver. If used
     *  the sending option is automatically changed to direct
     *
     * @param receiver
     */
    public final void setDirectEntityReceiver(EntityReceiverAbstract receiver) {
        myDirectEntityReceiver = receiver;
        setSendingOption(SendOption.DIRECT);
    }

    /** See setDirectEntityReceiver()
     *
     * @param g
     */
    public final void setDirectEntityReceiver(GetEntityReceiverIfc g) {
        setDirectEntityReceiver(g.getEntityReceiver());
    }
}
