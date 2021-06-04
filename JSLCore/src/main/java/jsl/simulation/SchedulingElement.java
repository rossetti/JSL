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
package jsl.simulation;

import jsl.utilities.GetValueIfc;

/** A SchedulingElement is a ModelElement that facilitates the scheduling of
 *  events. Every SchedulingElement has a built in event action which will
 *  call the handleEvent() method.  A subclass can override the handleEvent()
 *  method to provide event routine logic.
 *  
 *  Alternatively (and more flexibly) events can be scheduled with their own 
 *  action provided by implementing the EventActionIfc.  The class
 *  that implements the EventActionIfc can be provided when scheduling the
 *  event to ensure that the proper event routine is called when the event is 
 *  executed by the event scheduler.
 *
 */
public class SchedulingElement extends ModelElement {

    /** A reference to an instance of an inner class that implements
     *  the EventActionIfc for handling the default calling of handleEvent()
     *  if used by subclasses.
     */
//    private EventAction myHandleEventAction;

    /**
     * @param parent the parent
     */
    public SchedulingElement(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent the parent
     * @param name the name
     */
    public SchedulingElement(ModelElement parent, String name) {
        super(parent, name);

    }

    /** Tells the scheduler to cancel the provided event.
     * @param e A reference to the event to be canceled.
     */
    protected final void cancelEvent(JSLEvent e) {
        getExecutive().cancel(e);
    }

    /** This method allows a previously *executed* event to be reused
     * The event must have already been removed from the calendar through the
     * natural execute event mechanism and have been executed.
     * The user can reset the action, priority, and message as required
     * directly on the event prior to rescheduling.
     *
     * @param <T> the type associated with the events  message
     * @param event The event that needs rescheduling
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     */
    protected final <T> void rescheduleEvent(JSLEvent<T> event, double time) {
        //TODO should T be removed?
        event.setModelElement(this);
        getExecutive().reschedule(event, time);
    }

    /** This method allows a previously *executed* event to be reused
     * The event must have already been removed from the calendar through the
     * natural execute event mechanism and have been executed.
     * The user can reset the action, priority, and message as required
     * directly on the event prior to rescheduling.
     *
     * @param <T> the type associated with the events  message
     * @param event The event that needs rescheduling
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param message an Object to attach to the event
     */
    protected final <T> void rescheduleEvent(JSLEvent<T> event, double time, T message) {
        event.setMessage(message);
        event.setModelElement(this);
        getExecutive().reschedule(event, time);
    }

    /** This method allows a previously *executed* event to be reused
     * The event must have already been removed from the calendar through the
     * natural execute event mechanism and have been executed.
     * The user can reset the action, priority, and message as required
     * directly on the event prior to rescheduling.
     *
     * @param <T> the type associated with the events  message
     * @param event The event that needs rescheduling
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     */
    protected final <T> void rescheduleEvent(JSLEvent<T> event, GetValueIfc time) {
        event.setModelElement(this);
        getExecutive().reschedule(event, time.getValue());
    }

    /** This method allows a previously *executed* event to be reused
     * The event must have already been removed from the calendar through the
     * natural execute event mechanism and have been executed.
     * The user can reset the action, priority, and message as required
     * directly on the event prior to rescheduling.
     *
     * @param <T> the type of the message
     * @param event The event that needs rescheduling
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param message an Object to attach to the event
     */
    protected final <T> void rescheduleEvent(JSLEvent<T> event, GetValueIfc time, T message) {
        event.setMessage(message);
        event.setModelElement(this);
        getExecutive().reschedule(event, time.getValue());
    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action  which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(double time) {
//        return (scheduleEvent(time, JSLEvent.DEFAULT_TYPE));
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param <T> the type associated with the message
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param message An object to be sent with the event
//     * @return a valid JSLEvent
//     */
//    protected final <T> JSLEvent<T> scheduleEvent(double time, T message) {
//        JSLEvent e = scheduleEvent(time, JSLEvent.DEFAULT_TYPE);
//        e.setMessage(message);
//        return (e);
//    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(double time, int type) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time, getName(), JSLEvent.DEFAULT_PRIORITY, null);
//        e.setType(type);
//        return (e);
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @param message an object to attach to the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(double time, int type, Object message) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time, getName(), JSLEvent.DEFAULT_PRIORITY, message);
//        e.setType(type);
//        return (e);
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @param priority the priority of the event
//     * @param message an object to attach to the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(double time, int type, int priority, Object message) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time, getName(), priority, message);
//        e.setType(type);
//        return (e);
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @param priority the priority of the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(double time, int type, int priority) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time, getName(), priority, null);
//        e.setType(type);
//        return (e);
//    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(GetValueIfc time) {
//        return (scheduleEvent(time.getValue(), JSLEvent.DEFAULT_TYPE));
//    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param message An object to be sent with the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(GetValueIfc time, Object message) {
//        JSLEvent e = scheduleEvent(time.getValue(), JSLEvent.DEFAULT_TYPE, message);
//        return (e);
//    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(GetValueIfc time, int type) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time.getValue(), getName(), JSLEvent.DEFAULT_PRIORITY, null);
//        e.setType(type);
//        return (e);
//    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @param message an object to attach to the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(GetValueIfc time, int type, Object message) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time.getValue(), getName(), JSLEvent.DEFAULT_PRIORITY, message);
//        e.setType(type);
//        return (e);
//    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @param priority the priority of the event
//     * @param message an object to attach to the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(GetValueIfc time, int type, int priority, Object message) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time.getValue(), getName(), priority, message);
//        e.setType(type);
//        return (e);
//    }

//    /** Creates an event and schedules it onto the event calendar
//     * Uses this scheduling element's default event action which calls the
//     * method handleEvent(JSLEvent e)
//     *
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param type represents the type of event, user defined
//     * @param priority the priority of the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(GetValueIfc time, int type, int priority) {
//        JSLEvent e = scheduleEvent(getHandleEventAction(), time.getValue(), getName(), priority, null);
//        e.setType(type);
//        return (e);
//    }

    /** Creates an event and schedules it onto the event calendar.  This is the main scheduling method that
     * all other scheduling methods call.  The other methods are just convenience methods for this method.
     *
     * @param <T> the type associated with the attached message
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param priority is used to influence the ordering of events
     * @param message is a generic Object that may represent data to be transmitted with the event
     * @param name the name of the event
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, double time, int priority, T message, String name) {
        JSLEvent<T> event = getExecutive().scheduleEvent(action, time, priority, message, name, this);
        return (event);
    }

    /** Creates an event and schedules it onto the event calendar.  This is the main scheduling method that
     * all other scheduling methods call.  The other methods are just convenience methods for this method.
     *
     * @param <T> the type associated with the attached message
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param message is a generic Object that may represent data to be transmitted with the event
     * @param name the name of the event
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, double time, T message, String name) {
        JSLEvent<T> event = getExecutive().scheduleEvent(action, time, JSLEvent.DEFAULT_PRIORITY, message, name, this);
        return (event);
    }

    /** Creates an event and schedules it onto the event calendar
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, double time) {
        return (scheduleEvent(action, time, JSLEvent.DEFAULT_PRIORITY, null, getName()));
    }

    /** Creates an event and schedules it onto the event calendar
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, GetValueIfc time) {
        return (scheduleEvent(action, time.getValue(), JSLEvent.DEFAULT_PRIORITY, null, getName()));
    }

    /** Creates an event and schedules it onto the event calendar
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param priority is used to influence the ordering of events
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, double time, int priority) {
        return (scheduleEvent(action, time, priority, null, getName()));
    }

    /** Creates an event and schedules it onto the event calendar
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param priority is used to influence the ordering of events
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, GetValueIfc time, int priority) {
        return (scheduleEvent(action, time.getValue(), priority, null, getName()));
    }

    /** Creates an event and schedules it onto the event calendar
     * @param <T> the type associated with the attached message
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param message is a generic Object that may represent data to be transmitted with the event
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, double time, T message) {
        return (scheduleEvent(action, time, JSLEvent.DEFAULT_PRIORITY, message, getName()));
    }

    /** Creates an event and schedules it onto the event calendar
     * @param <T> the type associated with the attached message
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param message is a generic Object that may represent data to be transmitted with the event
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, GetValueIfc time, T message) {
        return (scheduleEvent(action, time.getValue(), JSLEvent.DEFAULT_PRIORITY, message, getName()));
    }

    /** Creates an event and schedules it onto the event calendar
     * @param <T> the type associated with the attached message
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param priority is used to influence the ordering of events
     * @param message is a generic Object that may represent data to be transmitted with the event
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, double time, int priority, T message) {
        return (scheduleEvent(action, time, priority, message, getName()));
    }

    /** Creates an event and schedules it onto the event calendar
     * @param <T> the type associated with the attached message
     * @param action represents an ActionListener that will handle the change of state logic
     * @param time represents the inter-event time, i.e. the interval from the current time to when the
     *        event will need to occur
     * @param priority is used to influence the ordering of events
     * @param message is a generic Object that may represent data to be transmitted with the event
     * @return a valid JSLEvent
     */
    protected final <T> JSLEvent<T> scheduleEvent(EventActionIfc<T> action, GetValueIfc time, int priority, T message) {
        return (scheduleEvent(action, time.getValue(), priority, message, getName()));
    }

//    /** Creates an event and schedules it onto the event calendar
//     * @param action represents an ActionListener that will handle the change of state logic
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param name A string to name the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(EventActionIfc action, double time, String name) {
//        return (scheduleEvent(action, time, name, JSLEvent.DEFAULT_PRIORITY, null));
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * @param action represents an ActionListener that will handle the change of state logic
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param name A string to name the event
//     * @param priority is used to influence the ordering of events
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(EventActionIfc action, double time, String name, int priority) {
//        return (scheduleEvent(action, time, name, priority, null));
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * @param action represents an ActionListener that will handle the change of state logic
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param name A string to name the event
//     * @param message is a generic Object that may represent data to be transmitted with the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(EventActionIfc action, double time, String name, Object message) {
//        return (scheduleEvent(action, time, name, JSLEvent.DEFAULT_PRIORITY, message));
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * @param action represents an ActionListener that will handle the change of state logic
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param name A string to name the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(EventActionIfc action, GetValueIfc time, String name) {
//        return (scheduleEvent(action, time.getValue(), name, JSLEvent.DEFAULT_PRIORITY, null));
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * @param action represents an ActionListener that will handle the change of state logic
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param name A string to name the event
//     * @param priority is used to influence the ordering of events
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(EventActionIfc action, GetValueIfc time, String name, int priority) {
//        return (scheduleEvent(action, time.getValue(), name, priority, null));
//    }
//
//
//
//    /** Creates an event and schedules it onto the event calendar
//     * @param action represents an ActionListener that will handle the change of state logic
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param name A string to name the event
//     * @param message is a generic Object that may represent data to be transmitted with the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(EventActionIfc action, GetValueIfc time, String name, Object message) {
//        return (scheduleEvent(action, time.getValue(), name, JSLEvent.DEFAULT_PRIORITY, message));
//    }
//
//    /** Creates an event and schedules it onto the event calendar
//     * @param action represents an ActionListener that will handle the change of state logic
//     * @param time represents the inter-event time, i.e. the interval from the current time to when the
//     *        event will need to occur
//     * @param name A string to name the event
//     * @param priority is used to influence the ordering of events
//     * @param message is a generic Object that may represent data to be transmitted with the event
//     * @return a valid JSLEvent
//     */
//    protected final JSLEvent scheduleEvent(EventActionIfc action, GetValueIfc time, String name, int priority, Object message) {
//        return (scheduleEvent(action, time.getValue(), name, priority, message));
//    }

//    /** Can be used as a general event handler by setting the event type and
//     *  conditioning on it within this method.  This alleviates the need for
//     *  actions for each event type but is not as flexible as separate actions.
//     *
//     * @param event the event to handle
//     */
//    protected void handleEvent(JSLEvent event) {
//    }
//
//    /** Returns a reference to the action that calls handleEvent()
//     *
//     * @return the action that calls handleEvent()
//     */
//    protected final EventActionIfc getHandleEventAction() {
//        if (myHandleEventAction == null) {
//            myHandleEventAction = new EventAction();
//        }
//        return (myHandleEventAction);
//    }
//
//    private class EventAction implements EventActionIfc {
//
//        @Override
//        public void action(JSLEvent evt) {
//            handleEvent(evt);
//        }
//    }
}
