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

package jslx.dbutilities.dbutil;

import jslx.excel.ExcelUtil;
import org.jooq.exception.DataAccessException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static jslx.dbutilities.dbutil.DatabaseIfc.LOGGER;

/**
 * A DbCreateTask represents a set of instructions that can be used to create, possibly fill,
 * and alter a database. It can be used only once. The enum Type indicates what kind of
 * tasks will be executed and the state of the task.
 */
public class DbCreateTask {

    public enum Type {
        NONE, FULL_SCRIPT, TABLES, TABLES_INSERT, TABLES_ALTER,
        TABLES_EXCEL, TABLES_INSERT_ALTER, TABLES_EXCEL_ALTER
    }

    public enum State {
        UN_EXECUTED, EXECUTED, EXECUTION_ERROR, NO_TABLES_ERROR
    }

    private Path myExcelInsertPath;
    private Path pathToCreationScript;
    private Path pathToTablesScript;
    private Path pathToInsertScript;
    private Path pathToAlterScript;

    private List<String> myInsertTableOrder = new ArrayList<>();
    private List<String> myCreationScriptCommands = new ArrayList<>();
    private List<String> myTableCommands = new ArrayList<>();
    private List<String> myInsertCommands = new ArrayList<>();
    private List<String> myAlterCommands = new ArrayList<>();
    private Type type;
    private State state = State.UN_EXECUTED;
    private DatabaseIfc myDatabase;

    /**
     *
     * @return the path to the Excel workbook to be used for inserting data, may be null
     */
    public Path getExcelWorkbookPathForDataInsert() {
        return myExcelInsertPath;
    }

    /**
     *
     * @return a list of table names in the order in which they need to be inserted. May be empty
     */
    public List<String> getInsertTableOrder() {
        return Collections.unmodifiableList(myInsertTableOrder);
    }

    /**
     *
     * @return a list of all the commands that were in the creation script, may be empty
     */
    public List<String> getCreationScriptCommands() {
        return Collections.unmodifiableList(myCreationScriptCommands);
    }

    /**
     *
     * @return a list of the create table commands, may be empty
     */
    public List<String> getTableCommands() {
        return Collections.unmodifiableList(myTableCommands);
    }

    /**
     *
     * @return a list of the insert commands, may be empty
     */
    public List<String> getInsertCommands() {
        return Collections.unmodifiableList(myInsertCommands);
    }

    /**
     *
     * @return a list of the alter commands, may be empty
     */
    public List<String> getAlterCommands() {
        return Collections.unmodifiableList(myAlterCommands);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("DbCreateTask{");
        sb.append(System.lineSeparator());
        sb.append("type=").append(type);
        sb.append(System.lineSeparator());
        sb.append("state=").append(state);
        sb.append(System.lineSeparator());
        sb.append("Creation script=").append(pathToCreationScript);
        sb.append(System.lineSeparator());
        sb.append("Full Creation Script Commands=").append(!myCreationScriptCommands.isEmpty());
        sb.append(System.lineSeparator());
        sb.append("Tables script=").append(pathToTablesScript);
        sb.append(System.lineSeparator());
        sb.append("Table Commands=").append(!myTableCommands.isEmpty());
        sb.append(System.lineSeparator());
        sb.append("Insert script=").append(pathToInsertScript);
        sb.append(System.lineSeparator());
        sb.append("Insert Commands=").append(!myInsertCommands.isEmpty());
        sb.append(System.lineSeparator());
        sb.append("Alter script=").append(pathToAlterScript);
        sb.append(System.lineSeparator());
        sb.append("Alter Commands=").append(!myAlterCommands.isEmpty());
        sb.append(System.lineSeparator());
        sb.append("Excel Workbook Path=").append(myExcelInsertPath);
        sb.append(System.lineSeparator());
        sb.append("Excel Insert Table Order= ");
        if (myInsertTableOrder.isEmpty()) {
            sb.append("None provided");
        } else {
            sb.append(System.lineSeparator());
        }
        for (String s : myInsertTableOrder) {
            sb.append("\t").append(s).append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append('}');
        return sb.toString();
    }

    private DbCreateTask(DbCreateTaskBuilder builder) {
        myDatabase = builder.database;
        type = Type.NONE;
        if (builder.pathToCreationScript != null) {
            // full creation script provided
            type = Type.FULL_SCRIPT;
            pathToCreationScript = builder.pathToCreationScript;
            myCreationScriptCommands = fillCommandsFromScript(builder.pathToCreationScript);
            if (myCreationScriptCommands.isEmpty()) {
                state = State.NO_TABLES_ERROR;
                return;
            }
        } else {
            if (builder.pathToTablesScript != null) {
                // use script to create database structure
                type = Type.TABLES;
                pathToTablesScript = builder.pathToTablesScript;
                myTableCommands = fillCommandsFromScript(builder.pathToTablesScript);
                if (myTableCommands.isEmpty()) {
                    state = State.NO_TABLES_ERROR;
                    return;
                }
                // now check for insert
                if (builder.pathToInsertScript != null) {
                    // prefer insert via SQL script if it exists
                    type = Type.TABLES_INSERT;
                    pathToInsertScript = builder.pathToInsertScript;
                    myInsertCommands = fillCommandsFromScript(builder.pathToInsertScript);
                } else {
                    // could be Excel insert
                    if (builder.pathToExcelWorkbook != null) {
                        type = Type.TABLES_EXCEL;
                        myExcelInsertPath = builder.pathToExcelWorkbook;
                        myInsertTableOrder = new ArrayList<>(builder.tableNamesInInsertOrder);
                    }
                }
                // now check for alter
                if (builder.pathToAlterScript != null) {
                    pathToAlterScript = builder.pathToAlterScript;
                    myAlterCommands = fillCommandsFromScript(builder.pathToAlterScript);
                    if (type == Type.TABLES_INSERT) {
                        type = Type.TABLES_INSERT_ALTER;
                    } else if (type == Type.TABLES_EXCEL) {
                        type = Type.TABLES_EXCEL_ALTER;
                    } else if (type == DbCreateTask.Type.TABLES) {
                        type = Type.TABLES_ALTER;
                    }
                }
            }
        }
        executeCreateTask();
    }

    /**
     * Attempts to execute a configured set of tasks that will create, possibly fill, and
     * alter the database.
     *
     * @return true if the task was executed correctly, false otherwise
     */
    private final boolean executeCreateTask() {
        switch (getState()) {
            case UN_EXECUTED:
                // execute the task
                return dbCreateTaskExecution();
            case EXECUTED:
                LOGGER.error("Tried to execute an already executed create task.\n {}", this);
                return false;
            case EXECUTION_ERROR:
                LOGGER.error("Tried to execute a previously executed task that had errors.\n {}", this);
                return false;
            case NO_TABLES_ERROR:
                LOGGER.error("Tried to execute a create task with no tables created.\n {}", this);
                return false;
        }
        return false;
    }

    private boolean dbCreateTaskExecution() {
        boolean execFlag = false; // assume it does not execute
        switch (getType()) {
            case NONE:
                LOGGER.warn("Attempted to execute a create task with no commands.\n {}", this);
                execFlag = true;
                setState(DbCreateTask.State.EXECUTED);
                break;
            case FULL_SCRIPT:
                LOGGER.info("Attempting to execute full script create task...\n {}", this);
                execFlag = myDatabase.executeCommands(getCreationScriptCommands());
                break;
            case TABLES:
                LOGGER.info("Attempting to execute tables only create task. \n{}", this);
                execFlag = myDatabase.executeCommands(getTableCommands());
                break;
            case TABLES_INSERT:
                LOGGER.info("Attempting to execute tables plus insert create task.\n{}", this);
                execFlag = myDatabase.executeCommands(getTableCommands());
                if (execFlag){
                    execFlag = myDatabase.executeCommands(getInsertCommands());
                }
                break;
            case TABLES_ALTER:
                LOGGER.info("Attempting to execute tables plus alter create task.\n{}", this);
                execFlag = myDatabase.executeCommands(getTableCommands());
                if (execFlag){
                    execFlag = myDatabase.executeCommands(getAlterCommands());
                }
                break;
            case TABLES_INSERT_ALTER:
                LOGGER.info("Attempting to execute create/insert/alter tables create task.\n {}", this);
                execFlag = myDatabase.executeCommands(getTableCommands());
                if (execFlag){
                    execFlag = myDatabase.executeCommands(getInsertCommands());
                }
                if (execFlag){
                    execFlag = myDatabase.executeCommands(getAlterCommands());
                }
                break;
            case TABLES_EXCEL:
                LOGGER.info("Attempting to execute tables create plus Excel import task.\n {}", this);
                execFlag = myDatabase.executeCommands(getTableCommands());
                if (execFlag){
                    try {
                        ExcelUtil.writeWorkbookToDatabase(getExcelWorkbookPathForDataInsert(),
                                true, myDatabase,
                                getInsertTableOrder());
                    } catch (IOException e) {
                        execFlag = false;
                    }
                }
                break;
            case TABLES_EXCEL_ALTER:
                LOGGER.info("Attempting to execute tables create plus Excel plus alter import task.\n {}", this);
                execFlag = myDatabase.executeCommands(getTableCommands());
                if (execFlag){
                    try {
                        ExcelUtil.writeWorkbookToDatabase(getExcelWorkbookPathForDataInsert(),
                                true, myDatabase, getInsertTableOrder());
                        execFlag = myDatabase.executeCommands(getAlterCommands());
                    } catch (IOException e) {
                        execFlag = false;
                    }
                }
                break;
        }
        if (execFlag) {
            setState(DbCreateTask.State.EXECUTED);
            LOGGER.info("The task was successfully executed.");
        } else {
            setState(DbCreateTask.State.EXECUTION_ERROR);
            LOGGER.info("The task had execution errors.");
            //TODO decide whether to throw this error or not
            throw new DataAccessException("There was an execution error for task \n" + this +
                    "\n see jslDbLog.log for details ");
        }
        return execFlag; // note can only get here if execFlag is true because of the execution exception
    }

    /**
     * @return the type of the command sequence specified during the builder process
     */
    public final Type getType() {
        return type;
    }

    public final State getState(){
        return state;
    }

    private void setState(State state){
        this.state = state;
    }

    /**
     * @param pathToScript the script to parse
     * @return the list of commands from the script
     */
    private final List<String> fillCommandsFromScript(Path pathToScript) {
        if (pathToScript == null) {
            throw new IllegalArgumentException("The creation script path must not be null");
        }
        if (Files.notExists(pathToScript)) {
            throw new IllegalArgumentException("The creation script file does not exist");
        }
        List<String> commands = new ArrayList<>();
        try {
            commands.addAll(DatabaseIfc.parseQueriesInSQLScript(pathToScript));
        } catch (IOException e) {
            LOGGER.warn("The script {} t failed to parse.", pathToScript);
        }
        if (commands.isEmpty()) {
            LOGGER.warn("The script {} produced no commands to execute.", pathToScript);
        }
        return commands;
    }

    /**
     * A builder that can be used to configure a database creation task through as set of configuration
     * steps.
     */
    public static final class DbCreateTaskBuilder implements DbCreateTaskExecuteStepIfc, WithCreateScriptStepIfc,
            WithTablesScriptStepIfc, DbCreateTaskFirstStepIfc, AfterTablesOnlyStepIfc,
            DbInsertStepIfc, DBAfterInsertStepIfc, DBAddConstraintsStepIfc {

        private Path pathToCreationScript;
        private Path pathToTablesScript;
        private Path pathToInsertScript;
        private Path pathToExcelWorkbook;
        private Path pathToAlterScript;
        private List<String> tableNamesInInsertOrder;
        private DatabaseIfc database;

        DbCreateTaskBuilder(DatabaseIfc database){
            this.database = database;
        }

        @Override
        public DbCreateTaskExecuteStepIfc withCreationScript(Path pathToScript) {
            if (pathToScript == null) {
                throw new IllegalArgumentException("The provided creation script path was null");
            }
            if (Files.notExists(pathToScript)) {
                throw new IllegalArgumentException("The creation script file does not exist");
            }
            pathToCreationScript = pathToScript;
            return this;
        }

        @Override
        public AfterTablesOnlyStepIfc withTables(Path pathToScript) {
            if (pathToScript == null) {
                throw new IllegalArgumentException("The provided table script path was null");
            }
            if (Files.notExists(pathToScript)) {
                throw new IllegalArgumentException("The create table script file does not exist");
            }
            pathToTablesScript = pathToScript;
            return this;
        }

        @Override
        public DBAfterInsertStepIfc withExcelData(Path toExcelWorkbook, List<String> tableNamesInInsertOrder) {
            if (toExcelWorkbook == null) {
                throw new IllegalArgumentException("The provided workbook script path was null");
            }
            if (Files.notExists(toExcelWorkbook)) {
                throw new IllegalArgumentException("The Excel workbook file does not exist");
            }
            if (tableNamesInInsertOrder == null) {
                throw new IllegalArgumentException("The provided list of table names was null");
            }
            pathToExcelWorkbook = toExcelWorkbook;
            this.tableNamesInInsertOrder = new ArrayList<>(tableNamesInInsertOrder);
            return this;
        }

        @Override
        public DBAfterInsertStepIfc withInserts(Path toInsertScript) {
            if (toInsertScript == null) {
                throw new IllegalArgumentException("The provided inset script path was null");
            }
            if (Files.notExists(toInsertScript)) {
                throw new IllegalArgumentException("The insert script file does not exist");
            }
            pathToInsertScript = toInsertScript;
            return this;
        }

        @Override
        public DbCreateTaskExecuteStepIfc withConstraints(Path toAlterScript) {
            if (toAlterScript == null) {
                throw new IllegalArgumentException("The provided alter script path was null");
            }
            if (Files.notExists(toAlterScript)) {
                throw new IllegalArgumentException("The alter table script file does not exist");
            }
            pathToAlterScript = toAlterScript;
            return this;
        }

        @Override
        public DbCreateTask execute() {
            return new DbCreateTask(this);
        }
    }


    /**
     * Used to limit the options on the first step
     */
    public interface DbCreateTaskFirstStepIfc extends WithCreateScriptStepIfc,
            WithTablesScriptStepIfc {

    }

    /**
     * Allows the user to specify a full creation script that puts the database into
     * the state desired by the user.
     */
    public interface WithCreateScriptStepIfc {
        /**
         * @param pathToCreationScript a path to a full creation script that specifies the database, must not be null
         * @return A builder step to permit connecting
         */
        DbCreateTaskExecuteStepIfc withCreationScript(Path pathToCreationScript);
    }

    /**
     * Allows the user to specify a script that creates the tables of the database
     */
    public interface WithTablesScriptStepIfc {
        /**
         * @param pathToScript a path to a script that specifies the database tables, must not be null
         * @return A builder step to permit connecting
         */
        AfterTablesOnlyStepIfc withTables(Path pathToScript);
    }

    public interface AfterTablesOnlyStepIfc extends DbCreateTaskExecuteStepIfc, DbInsertStepIfc {

    }

    public interface DbCreateStepIfc extends DbCreateTaskExecuteStepIfc {
        /**
         * @param toCreateScript the path to a script that will create the database, must not be null
         * @return a reference to the insert step in the builder process
         */
        DbInsertStepIfc using(Path toCreateScript);

    }

    public interface DbInsertStepIfc extends DbCreateTaskExecuteStepIfc {

        /**
         * @param toExcelWorkbook a path to an Excel workbook that can be read to insert
         *                        data into the database, must not be null
         * @param tableNames      a list of table names that need to be filled. Sheets in
         *                        the workbook must correspond exactly to these names
         * @return a reference to the alter step in the builder process
         */
        DBAfterInsertStepIfc withExcelData(Path toExcelWorkbook, List<String> tableNames);

        /**
         * @param toInsertScript a path to an SQL script that can be read to insert
         *                       data into the database, must not be null
         * @return a reference to the alter step in the builder process
         */
        DBAfterInsertStepIfc withInserts(Path toInsertScript);

    }

    public interface DBAddConstraintsStepIfc extends DbCreateTaskExecuteStepIfc {
        /**
         * @param toConstraintScript a path to an SQL script that can be read to alter the
         *                           table structure of the database and add constraints, must not be null
         * @return a reference to the alter step in the builder process
         */
        DbCreateTaskExecuteStepIfc withConstraints(Path toConstraintScript);
    }

    public interface DBAfterInsertStepIfc extends DBAddConstraintsStepIfc, DbCreateTaskExecuteStepIfc {

    }

    public interface DbCreateTaskExecuteStepIfc {
        /**
         * Finishes the builder process of building the creation commands
         *
         * @return an instance of DbCreateCommandList
         */
        DbCreateTask execute();
    }

}
