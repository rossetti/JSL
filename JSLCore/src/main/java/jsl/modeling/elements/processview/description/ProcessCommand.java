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
package jsl.modeling.elements.processview.description;

import java.util.LinkedList;
import java.util.List;

import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;

/**
 *
 */
public abstract class ProcessCommand extends SchedulingElement {

    /**
     * A reference to the process description using this command
     * <p>
     */
    private ProcessDescription myProcessDescription;

    /**
     * A reference to the process executor that is
     * executing this command
     */
    private ProcessExecutor myProcessExecutor;

    /**
     * A reference to the command's ResumeListener
     */
    private ResumeListener myResumeListener = new ResumeListener();

    /**
     * Holds the listeners that are notified prior to the execution of a command
     * <p>
     */
    private List<ProcessCommandListenerIfc> myBeforeExecutionListeners;

    /**
     * Holds the listeners that are notified after the execution of a command
     * <p>
     */
    private List<ProcessCommandListenerIfc> myAfterExecutionListeners;

    /**
     * @param parent the parent of the model element
     */
    public ProcessCommand(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent the parent of the model element
     * @param name the name for the model element
     */
    public ProcessCommand(ModelElement parent, String name) {
        super(parent, name);
        myBeforeExecutionListeners = new LinkedList<ProcessCommandListenerIfc>();
        myAfterExecutionListeners = new LinkedList<ProcessCommandListenerIfc>();
    }

    /**
     * The execute method is responsible for executing the command
     */
    abstract public void execute();

    /**
     * Returns a reference to the process description that
     * this command is within
     *
     * @return A reference to the process description for this command
     */
    public ProcessDescription getProcessDescription() {
        return (myProcessDescription);
    }

    /**
     * Adds a listener to be called prior to the execution of the command
     *
     * @param listener the listener to add
     */
    public final void addBeforeExecutionListener(ProcessCommandListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to add a null listener");
        }
        myBeforeExecutionListeners.add(listener);
    }

    /**
     * Removes the listener that is called prior to the execution of the command
     *
     * @param listener the listener to remove
     */
    public final void removeBeforeExecutionListener(ProcessCommandListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to remove a null listener");
        }
        myBeforeExecutionListeners.remove(listener);
    }

    /**
     * Adds a listener to be called after the execution of the command
     *
     * @param listener the listener to add
     */
    public final void addAfterExecutionListener(ProcessCommandListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to add a null listener");
        }
        myAfterExecutionListeners.add(listener);
    }

    /**
     * Removes the listener that is called after the execution of the command
     *
     * @param listener the listener to remove
     */
    public final void removeAfterExecutionListener(ProcessCommandListenerIfc listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Attempted to remove a null listener");
        }
        myAfterExecutionListeners.remove(listener);
    }

    /**
     * Sets the process description that this command currently is in
     *
     * @param processDescription The ProcessDescription
     */
    protected void setProcessDescription(ProcessDescription processDescription) {
        if (processDescription == null) {
            throw new IllegalArgumentException("ProcessDescription must be non-null!");
        }
        myProcessDescription = processDescription;
    }

    /**
     * The execute method is responsible for executing the command
     *
     * @param processExecutor, a reference to the process executor that is
     * currently executing the command
     */
    protected final void execute(ProcessExecutor processExecutor) {
        setProcessExecutor(processExecutor);
        notifyBeforeExecutionListeners();
        execute();
        notifyAfterExecutionListeners();
    }

    /**
     * Sets the process executor that is currently executing the command
     *
     * @param processExecutor The ProcessExecutor
     */
    protected final void setProcessExecutor(ProcessExecutor processExecutor) {
        if (processExecutor == null) {
            throw new IllegalArgumentException("ProcessExecutor was null!");
        }
        myProcessExecutor = processExecutor;
    }

    /**
     * Gets a reference to the process executor that is
     * currently executing this command
     *
     * @return A reference to the process executor.
     */
    protected final ProcessExecutor getProcessExecutor() {
        return (myProcessExecutor);
    }

    /**
     * Gets a reference to the ActionListener
     * to resume this command
     *
     * @return A reference to the ActionListener
     */
    protected final EventActionIfc getResumeListener() {
        return (myResumeListener);
    }

    /**
     * This method uses the event scheduling mechanism to
     * schedule the resumption of the process executor
     *
     * @param processExecutor the executor
     * @param time the time until
     * @param priority the priority
     * @param eventName the name of the event
     */
    protected final void scheduleResume(ProcessExecutor processExecutor,
            double time, int priority, String eventName) {
        if (processExecutor == null) {
            throw new IllegalArgumentException("ProcessExecutor was null!");
        }
        scheduleEvent(myResumeListener, time, priority, processExecutor, eventName);
    }

    /**
     * Notifies the before execution listeners
     */
    protected final void notifyBeforeExecutionListeners() {
        for (ProcessCommandListenerIfc a : myBeforeExecutionListeners) {
            a.update(this);
        }
    }

    /**
     * Notifies the before execution listeners
     */
    protected final void notifyAfterExecutionListeners() {
        for (ProcessCommandListenerIfc a : myAfterExecutionListeners) {
            a.update(this);
        }
    }

    /**
     * This class listens for the resumption event
     * and then resumes the process executor
     */
    protected class ResumeListener implements EventActionIfc<ProcessExecutor> {

        public void action(JSLEvent<ProcessExecutor> event) {
            ProcessExecutor e = event.getMessage();
            e.resume();
        }
    }
}
