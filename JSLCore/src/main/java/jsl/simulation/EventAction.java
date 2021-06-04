package jsl.simulation;


/**
 *  An abstract class that implements EventActionIfc with a JSLEvent
 *  message type of Object.
 */
abstract public class EventAction implements EventActionIfc<Object>{
    abstract public void action(JSLEvent<Object> event);
}
