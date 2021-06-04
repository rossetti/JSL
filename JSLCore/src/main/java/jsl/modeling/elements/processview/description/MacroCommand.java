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

import jsl.simulation.ModelElement;

public class MacroCommand extends ProcessCommand {

    /**
     * A reference to the subcommands within this macro
     */
    private ProcessDescription mySubCommands;

    /**
     * A macro command uses its own process executor
     * to execute its subcommands
     */
    private ProcessExecutor mySubCommandExecutor;

    /**
     *
     * @param parent the parent of the model element
     */
    public MacroCommand(ModelElement parent) {
        this(parent, null);
    }

    /**
     *
     * @param parent the parent of the model element
     * @param name the name of the model element
     */
    public MacroCommand(ModelElement parent, String name) {
        super(parent, name);
        mySubCommands = new ProcessDescription(this, name);
        mySubCommandExecutor = mySubCommands.createProcessExecutor();
        mySubCommandExecutor.initialize();

    }

    @Override
    public void execute() {
        // create a sub command executor for the sub process
        mySubCommandExecutor = getProcessExecutor().createSubProcessExecutor(mySubCommands);

        mySubCommandExecutor.start();
    }

    public void addSubCommand(ProcessCommand command) {
        mySubCommands.addProcessCommand(command);
    }
}
