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

import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.utilities.GetValueIfc;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.RVariableIfc;

import java.util.*;

/** EntityType represents a generic classification of entities. Every
 *  entity must have an entity type.  The entity type holds information that
 *  is "global" to all entities of the type. To create entities, the client
 *  must use an instance of EntityType
 *
 */
public class EntityType extends SchedulingElement {

    /**
     *  SendOption {DIRECT, SEQ, BY_TYPE}
     *  DIRECT, client must use setDirectEntityReceiver() to set the receiver
     *  SEQ, entity uses predefined sequence in its EntityType
     *  BY_TYPE, entity uses its EntityType to determine next receiver
     *  NONE, there is no option specified, will cause exception when sending
     *  entities unless behavior is overridden
     */
    public enum SendOption {

        NONE, DIRECT, SEQ, BY_TYPE, BY_SENDER

    };
    /** A reference to the set of AttributeTypes
     */
    private List<AttributeType> myAttributeTypes;

    /** A hash set to keep track of what names have been
     *  applied to AttributeTypes
     */
    private HashSet<String> myAttributeTypeNames;

    /** For tracking system time for entities by EntityType
     *
     */
    protected ResponseVariable myTimeInSystem;

    /** For tracking number of entities by EntityType
     *
     */
    protected TimeWeighted myNumInSystem;

    /** Holds the entity's list of receivers to visit
     *
     */
    protected List<EntityReceiverAbstract> myReceiverSequence;

    /** Holds the origin and destination pair for
     *  to send entity from origin to destination
     *
     */
    protected Map<EntityReceiverAbstract, DestinationIfc> myODNetwork;

    /** Used to hold listeners called prior to an entity
     *  being received
     *
     */
    protected List<EntityReceivedListener> myEntityReceivedListeners;

    /** Used to hold listeners called after an entity is sent
     *
     */
    protected List<EntitySentListener> myEntitySentListeners;

    /** Holds the time it takes for each activity
     *  experienced by the entity
     *
     */
    protected Map<Delay, RandomVariable> myActivityTimes;

    private final EventHandler myEventHandler;

    /** Creates an EntityType with a default name
     * 
     * @param parent 
     */
    public EntityType(ModelElement parent) {
        this(parent, null);
    }

    /** Creates an EntityType with the given name
     * 
     * @param parent
     * @param name 
     */
    public EntityType(ModelElement parent, String name) {
        super(parent, name);
        myEventHandler = new EventHandler();
        myAttributeTypes = new ArrayList<AttributeType>();
        myAttributeTypeNames = new HashSet<String>();
    }

    /** Creates an entity
     * 
     * @return 
     */
    @Override
    public final Entity createEntity() {
        return createEntity(getName());
    }

    /** Creates an entity with the given name
     * 
     * @param name
     * @return 
     */
    @Override
    public Entity createEntity(String name) {
        Entity e = new Entity(this, name);

        if (myNumInSystem != null) {
            myNumInSystem.increment();
        }

        return e;
    }

    /** Causes time in system statistics to be collected
     *  for the EntityType
     *
     */
    public void turnOnTimeInSystemCollection() {
        if (myTimeInSystem == null) {
            myTimeInSystem = new ResponseVariable(this, getName() + "_TimeInSystem");
        }
    }

    /** Causes number in system statistics to be collected
     *  for the EntityType
     *
     */
    public void turnOnNumberInSystemCollection() {
        if (myNumInSystem == null) {
            myNumInSystem = new TimeWeighted(this, getName() + "_NumberInSystem");
        }
    }

    /** Defines an attribute type with the given name and
     *  adds it to the available types for this entity type
     *
     * @param name the name of the attribute type, must be non-null
     *  and unique to this entity type
     * @return  
     */
    public final AttributeType defineAttributeType(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name of attribute type must be non-null!");
        }

        if (!myAttributeTypeNames.add(name)) {
            throw new IllegalArgumentException("Name of attribute type must be unique for this process description!");
        }

        AttributeType at = new AttributeType(name);

        myAttributeTypes.add(at);
        return (at);
    }

    /** Returns an unmodifiable list of the attribute types
     *
     * @return
     */
    public final List<AttributeType> getAttributeTypes() {
        return Collections.unmodifiableList(myAttributeTypes);
    }

    /** Convenience method
     *  See addEntityReceiver(EntityReceiverAbstract receiver)
     *
     * @param g
     */
    public final void addToSequence(GetEntityReceiverIfc g) {
        addToSequence(g.getEntityReceiver());
    }

    /** Adds a receiver to the receiver sequence
     *
     * @param receiver
     */
    public final void addToSequence(EntityReceiverAbstract receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("ReceiveEntityIfc receiver must be non-null!");
        }
        if (myReceiverSequence == null) {
            myReceiverSequence = new ArrayList<EntityReceiverAbstract>();
        }

        myReceiverSequence.add(receiver);
    }

    /** Gets an EntityReceiverIteratorIfc to the sequence of receivers
     *  positioned at the beginning of the sequence
     *
     * @return the iterator or null if no receiver list
     */
    public final EntityReceiverIteratorIfc getSequenceIterator() {
        if (myReceiverSequence == null) {
            return null;
        } else {
            return (new EntityReceiverListIterator(myReceiverSequence));
        }
    }

    /** Returns whether or not the entity receiver sequence is empty
     * 
     * @return
     */
    public final boolean isSequenceEmpty() {
        if (myReceiverSequence == null) {
            return true;
        } else {
            return myReceiverSequence.isEmpty();
        }
    }

    /** Returns the size of the Entity's receiver sequence
     *  zero if no sequence or if empty
     *
     * @return
     */
    public final int getSequenceSize() {
        if (myReceiverSequence == null) {
            return 0;
        } else {
            return myReceiverSequence.size();
        }
    }

    /** Returns the EntityReceiver at the supplied index
     *  If a sequence of receivers is not defined or the supplied
     *  index is out of bounds then null is returned. Indexing is 0 based
     *
     * @param index
     * @return
     */
    public final EntityReceiverAbstract getEntityReceiver(int index) {
        if (myReceiverSequence == null) {
            return null;
        }
        if ((index < 0) || (index >= myReceiverSequence.size())) {
            return null;
        }
        return myReceiverSequence.get(index);
    }

    protected final void setSequence(List<EntityReceiverAbstract> receiverSequence) {
        if (receiverSequence == null) {
            throw new IllegalArgumentException("List receiverSequence must be non-null!");
        }
        myReceiverSequence = receiverSequence;
    }

    /** By default the origin/destination mapping (if defined) is used
     *  to determine the next receiver; however, this
     *  method can be overridden in sub-classes to provide
     *  a general method of determining the destination
     *
     * @param e
     * @return
     */
    protected DestinationIfc getDestination(Entity e) {
        EntityReceiverAbstract origin = e.getCurrentReceiver();
        DestinationIfc destination = myODNetwork.get(origin);
        return destination;
    }

    /** Uses the DestinationIfc returned by getDestination(Entity e)
     *  to send the entity to its destination (receiver)
     * 
     * @param e
     */
    protected final void sendToDestination(Entity e) {
        DestinationIfc destination = getDestination(e);
        EntityReceiverAbstract receiver = destination.getEntityReceiver();
        sendToReceiver(e, receiver, destination.getValue());
    }

    /** Causes the entity to be received by the receiver, with no time delay
     *
     * @param e
     * @param receiver
     */
    protected void sendToReceiver(Entity e, EntityReceiverAbstract receiver) {
        sendToReceiver(e, receiver, 0.0);
    }

    /** Causes the entity to be received by the receiver, perhaps
     *  after the designated time delay.
     * 
     * @param e
     * @param receiver
     * @param time
     */
    protected void sendToReceiver(Entity e,
            EntityReceiverAbstract receiver, double time) {
        if (receiver == null) {
            throw new NoEntityReceiverException("No EntityReceiver was  " +
                    "specified to receive the entity.");
        }
        notifyEntitySentListeners(e);
        if (time > 0.0) {
            e.setMessage(receiver);
            scheduleEvent(myEventHandler, time, e);
        } else {
            // send the entity to the receiver
            e.setPlannedReceiver(receiver);
            notifyEntityReceivedListeners(e);
            receiver.receive(e);
        }
    }

    private class EventHandler implements EventActionIfc<Entity> {

        @Override
        public void action(JSLEvent<Entity> event) {
            Entity e = event.getMessage();
            EntityReceiverAbstract receiver = (EntityReceiverAbstract) e.getMessage();
            // send the entity to the receiver
            e.setPlannedReceiver(receiver);
            notifyEntityReceivedListeners(e);
            receiver.receive(e);
        }
    }

    /** Disposes of the entity, collects statistics if turned on
     *
     * @param e
     */
    protected void dispose(Entity e) {

        if (myNumInSystem != null) {
            myNumInSystem.decrement();
        }

        if (myTimeInSystem != null) {
            myTimeInSystem.setValue(getTime() - e.getCreateTime());
        }

    }

    /** Attaches a listener that will be called immediately before
     *  the entity is received, i.e. prior to receive(Entity entity)
     *  being called
     *
     * @param listener
     */
    public void attachEntityReceivedListener(EntityReceivedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The supplied listener was null");
        }
        if (myEntityReceivedListeners == null) {
            myEntityReceivedListeners = new ArrayList<EntityReceivedListener>();
        }

        if (myEntityReceivedListeners.contains(listener)) {
            throw new IllegalArgumentException("The supplied listener is already attached");
        }

        myEntityReceivedListeners.add(listener);
    }

    /** Detaches a previously attached listener
     *
     * @param listener
     */
    public void detachEntityReceivedListener(EntityReceivedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The supplied listener was null");
        }

        if (myEntityReceivedListeners == null) {
            throw new IllegalArgumentException("No listeners are attached.");
        }

        if (!myEntityReceivedListeners.contains(listener)) {
            throw new IllegalArgumentException("The supplied listener was not attached.");
        }

        myEntityReceivedListeners.remove(listener);
    }

    protected void notifyEntityReceivedListeners(Entity entity) {
        if (myEntityReceivedListeners == null) {
            return;
        }

        for (EntityReceivedListener listener : myEntityReceivedListeners) {
            listener.entityReceived(entity);
        }
    }

    protected void notifyEntitySentListeners(Entity entity) {
        if (myEntitySentListeners == null) {
            return;
        }
        for (EntitySentListener listener : myEntitySentListeners) {
            listener.entitySent(entity);
        }
    }

    /** Attaches a listener that will be called immediately before
     *  the entity is sent, i.e. prior to sendEntity(Entity e)
     *  being called
     *
     * @param listener
     */
    public void attachEntitySentListener(EntitySentListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The supplied listener was null");
        }
        if (myEntitySentListeners == null) {
            myEntitySentListeners = new ArrayList<EntitySentListener>();
        }

        if (myEntitySentListeners.contains(listener)) {
            throw new IllegalArgumentException("The supplied listener is already attached");
        }

        myEntitySentListeners.add(listener);
    }

    /** Detaches a listener
     *
     * @param listener
     */
    public void detachEntitySentListener(EntitySentListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The supplied listener was null");
        }

        if (myEntitySentListeners == null) {
            throw new IllegalArgumentException("No listeners are attached.");
        }

        if (!myEntitySentListeners.contains(listener)) {
            throw new IllegalArgumentException("The supplied listener was not attached.");
        }

        myEntitySentListeners.remove(listener);
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (EntityReceiverAbstract).  The supplied RandomIfc indicates
     *  the delay to arrive at the destination
     *
     * @param origin
     * @param destination
     * @param random
     */
    public void addDestination(EntityReceiverAbstract origin,
            EntityReceiverAbstract destination, RVariableIfc random) {
        RandomVariable rv = new RandomVariable(this, random);
        addDestination(origin, new Destination(destination, rv));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (EntityReceiverAbstract).  The supplied GetValueIfc indicates
     *  the delay to arrive at the destination
     *
     * @param origin
     * @param destination
     * @param value
     */
    public void addDestination(EntityReceiverAbstract origin,
            EntityReceiverAbstract destination, GetValueIfc value) {
        addDestination(origin, new Destination(destination, value));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (EntityReceiverAbstract).  The supplied time indicates
     *  the delay to arrive at the destination
     *
     * @param origin
     * @param destination
     * @param time
     */
    public void addDestination(EntityReceiverAbstract origin,
            EntityReceiverAbstract destination, double time) {
        addDestination(origin, new CDestination(destination, time));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (EntityReceiverAbstract).  The supplied time indicates
     *  the delay to arrive at the destination
     *
     * @param origin
     * @param destination
     */
    public void addDestination(EntityReceiverAbstract origin,
            EntityReceiverAbstract destination) {
        addDestination(origin, new CDestination(destination, 0.0));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (GetEntityReceiverIfc).  The supplied time indicates
     *  the delay to arrive at the destination (GetEntityReceiverIfc)
     *
     * @param origin
     * @param destination
     * @param time
     */
    public void addDestination(GetEntityReceiverIfc origin,
            GetEntityReceiverIfc destination, double time) {
        addDestination(origin.getEntityReceiver(),
                new CDestination(destination.getEntityReceiver(), time));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (GetEntityReceiverIfc).  The supplied time indicates
     *  the delay to arrive at the destination (GetEntityReceiverIfc)
     *
     * @param origin
     * @param destination
     */
    public void addDestination(GetEntityReceiverIfc origin,
            GetEntityReceiverIfc destination) {
        addDestination(origin.getEntityReceiver(),
                new CDestination(destination.getEntityReceiver(), 0.0));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (GetEntityReceiverIfc).  The supplied time indicates
     *  the delay to arrive at the destination
     *
     * @param origin
     * @param destination
     * @param time
     */
    public void addDestination(GetEntityReceiverIfc origin,
            EntityReceiverAbstract destination, double time) {
        addDestination(origin.getEntityReceiver(), new CDestination(destination, time));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (GetEntityReceiverIfc).  The supplied time indicates
     *  the delay to arrive at the destination
     *
     * @param origin
     * @param destination
     */
    public void addDestination(GetEntityReceiverIfc origin,
            EntityReceiverAbstract destination) {
        addDestination(origin.getEntityReceiver(), new CDestination(destination, 0.0));
    }

    /** Associates a destination (DestinationIfc) with the supplied
     *  origin (EntityReceiverAbstract)
     * 
     * @param origin
     * @param d
     */
    public final void addDestination(EntityReceiverAbstract origin, DestinationIfc d) {

        if (origin == null) {
            throw new IllegalArgumentException("The supplied origin was null");
        }

        if (d == null) {
            throw new IllegalArgumentException("The supplied destination was null");
        }

        if (myODNetwork == null) {
            myODNetwork = new HashMap<EntityReceiverAbstract, DestinationIfc>();
        }

        myODNetwork.put(origin, d);
    }

    /** Number of origins in the origin/destination mapping
     *
     * @return
     */
    public final int numberOfOrigins() {
        if (myODNetwork == null) {
            return 0;
        }
        return myODNetwork.size();
    }

    /** Whether or not the origin/destination mapping is empty
     *
     * @return
     */
    public final boolean isOriginDestinationNetworkEmpty() {
        if (myODNetwork == null) {
            return false;
        }
        return myODNetwork.isEmpty();
    }

    /** True if the origin has already been added to the O/D mapping
     *
     * @param origin
     * @return
     */
    public final boolean containsOrigin(EntityReceiverAbstract origin) {
        if (myODNetwork == null) {
            return false;
        }
        return myODNetwork.containsKey(origin);
    }

    protected class Destination implements DestinationIfc {

        private GetValueIfc v;

        private EntityReceiverAbstract d;

        public Destination(EntityReceiverAbstract r, GetValueIfc v) {
            d = r;
            this.v = v;
        }

        @Override
        public EntityReceiverAbstract getEntityReceiver() {
            return d;
        }

        @Override
        public double getValue() {
            return v.getValue();
        }
    }

    protected class CDestination implements DestinationIfc {

        private double v;

        private EntityReceiverAbstract d;

        public CDestination(EntityReceiverAbstract r, double v) {
            d = r;
            this.v = v;
        }

        @Override
        public EntityReceiverAbstract getEntityReceiver() {
            return d;
        }

        @Override
        public double getValue() {
            return v;
        }
    }

    protected double getActivityTime(Delay a) {

        if (myActivityTimes == null) {
            throw new IllegalArgumentException("No activities are registered");
        }

        if (!containsActivity(a)) {
            throw new IllegalArgumentException("The activity is not registered");
        }

        return myActivityTimes.get(a).getValue();
    }

    /** True if the Activity has already been added
     *
     * @param a
     * @return
     */
    public final boolean containsActivity(Delay a) {
        if (myActivityTimes == null) {
            return false;
        }
        return myActivityTimes.containsKey(a);
    }

    /** Associates a time with an Activity for the EntityType
     *
     * @param a
     * @param time
     */
    public final void addActivityTime(Delay a, double time) {
        addActivityTime(a, new ConstantRV(time));
    }

    /** Associates a activity (Activity) with the supplied
     *  random time
     *
     * @param a  The activity
     * @param r  The time
     */
    public final void addActivityTime(Delay a, RVariableIfc r) {

        if (a == null) {
            throw new IllegalArgumentException("The supplied activity was null");
        }

        if (r == null) {
            throw new IllegalArgumentException("The supplied time was null");
        }

        if (myActivityTimes == null) {
            myActivityTimes = new HashMap<Delay, RandomVariable>();
        }

        a.setDelayOption(Delay.DelayOption.BY_TYPE);
        myActivityTimes.put(a, new RandomVariable(this, r, a.getName() + "_Activity Time"));
    }

    /**
     * 
     * @param aThis
     * @return
     */
    protected List<SeizeRequirement> getSeizeRequirements(SeizeResources aThis) {
        throw new UnsupportedOperationException("Not yet implemented");//TODO
    }

    SortedSet<ReleaseRequirement> getReleaseRequirements(ReleaseResources aThis) {
        throw new UnsupportedOperationException("Not yet implemented");//TODO
    }
}
