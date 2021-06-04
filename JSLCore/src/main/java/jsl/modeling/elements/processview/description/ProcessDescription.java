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

import jsl.modeling.elements.entity.EntityType;
import jsl.modeling.elements.entity.Entity;
import jsl.simulation.ModelElement;

import java.util.*;

/**
 *
 */
public class ProcessDescription extends ModelElement {

    /**
     * Indicates whether or not the ProcessDescription
     * will automatically create a process executor and start
     * it at the beginning of the replication. By default
     * the auto start flag is false.
     */
    private boolean myAutoStartFlag;

    /**
     * A reference to an automatically started executor
     */
    private ProcessExecutor myAutoStartExecutor;

    /**
     * A reference to the list of commands
     */
    private List<ProcessCommand> myCommands;

    /**
     * A reference to the set of EntityTypes
     */
    private List<EntityType> myEntityTypes;

    /**
     * A hash set to keep track of what names have been
     * applied to entity types
     */
    private HashSet<String> myEntityTypeNames;

    /**
     * This can be used to automatically add this listener
     * to any ProcessExecutors that are created by this ProcessDescription
     * This listener is notified when the ProcessExecutor is started.
     * <p>
     */
    private ProcessExecutorListenerIfc myProcessExecutorStartListener;

    /**
     * This can be used to automatically add this listener
     * to any ProcessExecutors that are created by this ProcessDescription
     * This listener is notified when the ProcessExecutor is completed.
     * <p>
     */
    private ProcessExecutorListenerIfc myProcessExecutorCompletedListener;

    /**
     * A ProcessDescription is a list or sequence of commands
     * that represent the life of a process
     * @param parent the parent model element
     */
    public ProcessDescription(ModelElement parent) {
        this(parent, null);
    }

    /**
     * A ProcessDescription is a list or sequence of commands
     * that represent the life of a process
     *
     * @param parent the parent model element
     * @param name The name of process description
     */
    public ProcessDescription(ModelElement parent, String name) {
        super(parent, name);
        myAutoStartFlag = false;
        myCommands = new ArrayList<ProcessCommand>();
        myEntityTypes = new ArrayList<EntityType>();
        myEntityTypeNames = new HashSet<String>();
    }

    /**
     * Tells the process description that it can auto start
     * executing. That is it will create a default process
     * executor and start processing at the beginning of a
     * replication. The default is no auto start.
     */
    public final void turnOnAutoStart() {
        myAutoStartFlag = true;
    }

    /**
     * Tells the process description that it should not auto start
     * executing.
     */
    public final void turnOffAutoStart() {
        myAutoStartFlag = false;
    }

    /**
     * Adds a command to this process description. The order of adding commands
     * determines the sequence of commands to be executed. Notice that commands
     * can
     * only be added at this time. No facilities for removing or rearranging
     * commands
     * have been provided. Once you make the sequence of command, they are set.
     *
     * @param command the ProcessCommand to be added.
     */
    public final void addProcessCommand(ProcessCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command must be non-null!");
        }

        myCommands.add(command);
        //Should I worry about when model elements are removed?
    }

    /**
     * This can be used to automatically add this listener
     * to any ProcessExecutors that are created by this ProcessDescription
     * This listener is notified when the ProcessExecutor is completed.
     * The listener may be null. In which case nothing will be added.
     *
     * @param listener The processExecutorCompletedListener to set.
     */
    public final void setProcessExecutorCompletedListener(ProcessExecutorListenerIfc listener) {
        myProcessExecutorCompletedListener = listener;
    }

    /**
     * This can be used to automatically add this listener
     * to any ProcessExecutors that are created by this ProcessDescription
     * This listener is notified when the ProcessExecutor is started.
     * The listener may be null. In which case, nothing will be added.
     *
     * @param listener The processExecutorStartListener to set.
     */
    public final void setProcessExecutorStartListener(ProcessExecutorListenerIfc listener) {
        myProcessExecutorStartListener = listener;
    }

    /**
     * Defines an entity type with the given name and
     * adds it to the available types for this process description
     *
     * @param name, the name of the entity type, must be non-null
     * and unique to this process description
     * @return the entity type
     */
    public final EntityType defineEntityType(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name of entity type must be non-null!");
        }

        if (!myEntityTypeNames.add(name)) {
            throw new IllegalArgumentException("Name of entity type must be unique for this process description!");
        }

        EntityType et = new EntityType(this, name);

        myEntityTypes.add(et);
        return (et);
    }

    /**
     * Asks the process description to make an instance of
     * a ProcessExecutor in order to execute the commands. This
     * method creates a default entity to be associated with
     * the process.
     *
     * @return An instance of a ProcessExecutor
     */
    public final ProcessExecutor createProcessExecutor() {
        return (createProcessExecutor(createEntity()));
    }

    /**
     * Asks the process description to make an instance of
     * a ProcessExecutor in order to execute the commands.
     *
     * @param entity, An entity to be used within the execution
     * @return An instance of a ProcessExecutor
     */
    public ProcessExecutor createProcessExecutor(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "Entity must be non-null!");
        }
        ProcessExecutor pe = new ProcessExecutor(this, entity);
        if (myProcessExecutorStartListener != null) {
            pe.addBeforeExecutionListener(myProcessExecutorStartListener);
        }

        if (myProcessExecutorCompletedListener != null) {
            pe.addAfterExecutionListener(myProcessExecutorCompletedListener);
        }

        return (pe);
    }

    /**
     * Returns the list of commands
     *
     * @return, the list of commands as a List
     */
    protected List<ProcessCommand> getProcessCommands() {
        return (myCommands);
    }

    protected void initialize() {
        if (myAutoStartFlag == true) {
            if (myAutoStartExecutor == null) {
                myAutoStartExecutor = createProcessExecutor();
            }
            myAutoStartExecutor.initialize();
            myAutoStartExecutor.start();
        }
    }

    protected void afterReplication() {
        if (myAutoStartFlag == true) {
            if (myAutoStartExecutor != null) {
                if (!myAutoStartExecutor.isTerminated()) {
                    myAutoStartExecutor.terminate();
                }
            }

        }
    }

    /**
     * This method is automatically called whenever a process executor
     * associated
     * with this process description has been terminated
     *
     * @param processExecutor
     */
    protected void processExecutorTerminated(ProcessExecutor processExecutor) {

    }

    /*	protected class EntityTypeNameComparator implements Comparator {
		
		public int compare(Object a, Object b){
			EntityType eta = (EntityType)a;
			EntityType etb = (EntityType)b;
			return(eta.getName().compareTo(etb.getName()));
		}
	}
     */
    protected class EntityTypeNameComparator implements Comparator<EntityType> {

        public int compare(EntityType a, EntityType b) {
            return (a.getName().compareTo(b.getName()));
        }
    }
}
