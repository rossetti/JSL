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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jslx.dbutilities.dbutil;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jslx.excel.ExcelUtil;
import org.apache.commons.io.FileUtils;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

/**
 * An abstraction for using Derby embedded databases.  The static method createDb() provides a builder
 * for constructing instances.
 * <p>
 * The assumption is that the creation script only creates the tables with no
 * constraints on keys. The insertion process can be either through Excel
 * or a script having SQL insert statements. The alter script then places the key and
 * other constraints on the database.  We assume that valid data and scripts are in place.
 * <p>
 * The Excel workbook must be a worksheet for each table of the database for which
 * you want data inserted. The worksheets should be named exactly the same as the table
 * names in the database. The first row of each sheet should contain the exact field
 * names for the table that the sheet represents. Valid data must be entered into each
 * sheet. No validation is provided.
 *
 * This class is being deprecated.  Use Database and DatabaseFactory instead.
 *
 * @author rossetti
 */
@Deprecated
public class EmbeddedDerbyDatabase implements DatabaseIfc {

    /**
     * The connection to the database
     */
    private final EmbeddedDataSource myEmbeddedDS;

    /**
     * The path to the database
     */
    private final Path myDbPath;
    private final Path myDbDirPath;

    private final String myDbName;
    private String myDefaultSchemaName;

    private final DSLContext myDSLContext;
    /**
     * The connection URL
     */
    private String myConnURL;

    private final SQLDialect mySQLDialect = SQLDialect.DERBY;

    private Path myCreationScriptPath;
    private Path myTableScriptPath;
    private Path myInsertionScriptPath;
    private Path myExcelInsertPath;
    private Path myAlterScriptPath;

    private final List<String> myTruncateTableOrder;
    private final List<String> myInsertTableOrder;
    private final List<String> myCreationScriptCommands;
    private final List<String> myTableCommands;
    private final List<String> myInsertCommands;
    private final List<String> myAlterCommands;

    private EmbeddedDerbyDatabase(DbBuilder builder) throws IOException {
        // set up the arrays to hold possble commands from scripts
        myCreationScriptCommands = new ArrayList<>();
        myTableCommands = new ArrayList<>();
        myInsertCommands = new ArrayList<>();
        myAlterCommands = new ArrayList<>();
        // set up arrays for insert order or truncate order if supplied
        myTruncateTableOrder = new ArrayList<>();
        myInsertTableOrder = new ArrayList<>();
        // copy over information from the builder
        myDbName = builder.dbName;
        myDbDirPath = builder.pathToDirectory;
        myDbPath = myDbDirPath.resolve(myDbName);
        myEmbeddedDS = new EmbeddedDataSource();
        myEmbeddedDS.setDatabaseName(myDbPath.toString());
        // copy over the possible script specifications
        myCreationScriptPath = builder.pathToCreationScript;
        myTableScriptPath = builder.pathToTablesOnlyScript;
        myInsertionScriptPath = builder.pathToInsertScript;
        myAlterScriptPath = builder.pathToAlterScript;
        myExcelInsertPath = builder.pathToExcelWorkbook;
        // start the builder process
        if (builder.createFlag == true) {
            myEmbeddedDS.setCreateDatabase("create");
            openConnection();
            myEmbeddedDS.setCreateDatabase(null);
            if (myCreationScriptPath != null) {
                // full creation script provided
                executeCreationScript(myCreationScriptPath);
            } else {
                if (myTableScriptPath != null) {
                    // use script to create database structure
                    executeCreateTablesOnlyScript(myTableScriptPath);
                    // now check for insert
                    if (myInsertionScriptPath != null) {
                        // prefer insert via SQL script if it exists
                        executeInsertionScript(myInsertionScriptPath);
                    } else {
                        // could be Excel insert
                        if (myExcelInsertPath != null) {
                            setInsertTableOrder(builder.tableNames);
                            ExcelUtil.writeWorkbookToDatabase(myExcelInsertPath, true, this, myInsertTableOrder);
                        }
                    }
                    // now check for alter
                    if (myAlterScriptPath != null) {
                        executeAlterScript(myAlterScriptPath);
                    }
                }
            }
        } else {
            openConnection();
        }
        myDSLContext = DSL.using(getDataSource(), getSQLDialect());
        setJooQDefaultExecutionLoggingOption(false);
    }

    private void openConnection() {
        try {
            Connection connection = myEmbeddedDS.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            myConnURL = metaData.getURL();
            LOGGER.trace("Connection made to {}", myEmbeddedDS.getDatabaseName());
            DatabaseIfc.logWarnings(connection);
        } catch (SQLException e) {
            LOGGER.error("Unable to make connection to {}", myEmbeddedDS.getDatabaseName());
            throw new DataAccessException("Unable to make connection to database");
        }
    }

    /**
     * Creates a Derby database with the given name in the current working directory
     *
     * @param name the name of the database
     * @return the new database after creation
     */
    public static FirstDbBuilderStepIfc createDb(String name) {
        return new DbBuilder(name, Paths.get("."), true);
    }

    /**
     * Creates a Derby database with the given name in the specified directory
     *
     * @param name        the name of the database
     * @param dbDirectory a path to the directory that holds the database
     * @return the new database after creation
     */
    public static FirstDbBuilderStepIfc createDb(String name, Path dbDirectory) {
        return new DbBuilder(name, dbDirectory, true);
    }

    /**
     * Connects to a Derby database with the given name in the current working directory
     *
     * @param name the name of the database
     * @return the new database after creation
     */
    public static DbConnector connectDb(String name) {
        return new DbConnector(name, Paths.get("."));
    }

    /**
     * Connects to a Derby database with the given name in the specified directory
     *
     * @param name        the name of the database
     * @param dbDirectory a path to the directory that holds the database
     * @return the new database after creation
     */
    public static DbConnector connectDb(String name, Path dbDirectory) {
        return new DbConnector(name, dbDirectory);
    }

    public static final class DbConnector implements DbConnectStepIfc {
        private String dbName;
        private Path pathToDirectory;

        public DbConnector(String name, Path dbDirectory) {
            if (name == null) {
                throw new IllegalArgumentException("The provided name was null");
            }
            if (dbDirectory == null) {
                throw new IllegalArgumentException("The provided directory was null");
            }
            dbName = name;
            pathToDirectory = dbDirectory;
        }

        @Override
        public EmbeddedDerbyDatabase connect() throws IOException {
            DbBuilder dbBuilder = new DbBuilder(dbName, pathToDirectory, false);
            return dbBuilder.connect();
        }
    }

    /**
     * Provides the builder process for creating instances of EmbeddedDerbyDatabase.
     * The assumption is that the creation script only creates the tables with no
     * constraints on keys. The insertion process can be either through Excel
     * or a script having SQL insert statements. The alter script then places the key and
     * other constraints on the database.  We assume that valid data and scripts are in place.
     */
    public static final class DbBuilder implements DbConnectStepIfc, WithCreateScriptStepIfc,
            WithTablesOnlyScriptStepIfc, FirstDbBuilderStepIfc, AfterTablesOnlyStepIfc,
            DbInsertStepIfc, DBAfterInsertStepIfc, DBAddConstraintsStepIfc {

        private String dbName;
        private Path pathToDirectory;
        private Path pathToCreationScript;
        private Path pathToTablesOnlyScript;
        private Path pathToInsertScript;
        private Path pathToExcelWorkbook;
        private Path pathToAlterScript;
        private boolean createFlag;
        private List<String> tableNames;

        public DbBuilder(String name, Path dbDirectory, boolean createFlag) {
            this.createFlag = createFlag;
            if (name == null) {
                throw new IllegalArgumentException("The provided name was null");
            }
            if (dbDirectory == null) {
                throw new IllegalArgumentException("The provided directory was null");
            }
            dbName = name;
            pathToDirectory = dbDirectory;
        }

        @Override
        public DbConnectStepIfc withCreationScript(Path pathToScript) {
            if (pathToScript == null) {
                throw new IllegalArgumentException("The provided creation script path was null");
            }
            pathToCreationScript = pathToScript;
            return this;
        }

        @Override
        public AfterTablesOnlyStepIfc withTables(Path pathToScript) {
            if (pathToScript == null) {
                throw new IllegalArgumentException("The provided table script path was null");
            }
            pathToTablesOnlyScript = pathToScript;
            return this;
        }

        @Override
        public DBAfterInsertStepIfc withExcelData(Path toExcelWorkbook, List<String> tableNames) {
            if (toExcelWorkbook == null) {
                throw new IllegalArgumentException("The provided workbook script path was null");
            }
            if (tableNames == null) {
                throw new IllegalArgumentException("The provided list of table names was null");
            }
            pathToExcelWorkbook = toExcelWorkbook;
            this.tableNames = new ArrayList<>(tableNames);
            return this;
        }

        @Override
        public DBAfterInsertStepIfc withInsertData(Path toInsertScript) {
            if (toInsertScript == null) {
                throw new IllegalArgumentException("The provided inset script path was null");
            }
            pathToInsertScript = toInsertScript;
            return this;
        }

        @Override
        public DbConnectStepIfc withConstraints(Path toAlterScript) {
            if (toAlterScript == null) {
                throw new IllegalArgumentException("The provided alter script path was null");
            }
            pathToAlterScript = toAlterScript;
            return this;
        }

        @Override
        public EmbeddedDerbyDatabase connect() throws IOException {
            return new EmbeddedDerbyDatabase(this);
        }

    }

    public interface FirstDbBuilderStepIfc extends DbConnectStepIfc, WithCreateScriptStepIfc,
            WithTablesOnlyScriptStepIfc {

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
        DbConnectStepIfc withCreationScript(Path pathToCreationScript);
    }

    /**
     * Allows the user to specify a script that creates the tables of the database
     */
    public interface WithTablesOnlyScriptStepIfc {
        /**
         * @param pathToScript a path to a script that specifies the database tables, must not be null
         * @return A builder step to permit connecting
         */
        AfterTablesOnlyStepIfc withTables(Path pathToScript);
    }

    public interface AfterTablesOnlyStepIfc extends DbConnectStepIfc, DbInsertStepIfc {

    }

    public interface DbCreateStepIfc extends DbConnectStepIfc {
        /**
         * @param toCreateScript the path to a script that will create the database, must not be null
         * @return a reference to the insert step in the builder process
         */
        DbInsertStepIfc using(Path toCreateScript);

    }

    public interface DbInsertStepIfc extends DbConnectStepIfc {

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
        DBAfterInsertStepIfc withInsertData(Path toInsertScript);

    }

    public interface DBAddConstraintsStepIfc extends DbConnectStepIfc {
        /**
         * @param toConstraintScript a path to an SQL script that can be read to alter the
         *                           table structure of the database and add constraints, must not be null
         * @return a reference to the alter step in the builder process
         */
        DbConnectStepIfc withConstraints(Path toConstraintScript);
    }

    public interface DBAfterInsertStepIfc extends DBAddConstraintsStepIfc, DbConnectStepIfc {

    }

    public interface DbConnectStepIfc {
        /**
         * Finalizes the builder process and connects to the database
         *
         * @return an instance of EmbeddedDerbyDatabase
         */
        EmbeddedDerbyDatabase connect() throws IOException;
    }

    @Override
    public final DataSource getDataSource() {
        return myEmbeddedDS;
    }

    @Override
    public final DSLContext getDSLContext() {
        return myDSLContext;
    }

    @Override
    public final String getLabel() {
        return myDbName;
    }

    @Override
    public final SQLDialect getSQLDialect() {
        return mySQLDialect;
    }

    @Override
    public String getDefaultSchemaName() {
        return myDefaultSchemaName;
    }

    @Override
    public void setDefaultSchemaName(String defaultSchemaName) {
        myDefaultSchemaName = defaultSchemaName;
        if (defaultSchemaName != null) {
            if (!containsSchema(defaultSchemaName)) {
                LOGGER.warn("The supplied default schema name {} was not in the database {}.",
                        defaultSchemaName, getLabel());
            }
        } else {
            LOGGER.warn("The default schema name was set to null for database {}.", getLabel());
        }
    }

    /**
     *
     * @return a Path representation of the directory to the database
     */
    public final Path getDirectory() {
        return Paths.get(myDbDirPath.toUri());
    }

    /**
     * A URL representation of the embedded database
     *
     * @return the URL
     */
    public final String getURL() {
        return myConnURL;
    }

    /**
     * @return the path representation for the database
     */
    public final Path getDBPath() {
        return Paths.get(myDbPath.toUri());
    }

    /**
     * @return an EmbeddedDataSource for working with the database
     */
    public final EmbeddedDataSource getEmbeddedDataSource() {
        return myEmbeddedDS;
    }

    /**
     * @return the path to the tables only script
     */
    public final Path getCreationScriptPath() {
        return myCreationScriptPath;
    }

    /**
     * Sets the path, but does not execute the script
     *
     * @param path the path to the script
     */
    public void setCreationScriptPath(Path path) {
        myCreationScriptPath = path;
    }

    /**
     * @return the path to the tables only script
     */
    public final Path getTablesOnlyScriptPath() {
        return myTableScriptPath;
    }

    /**
     * Sets the path, but does not execute the script
     *
     * @param path the path to the script
     */
    public void setTablesOnlyScriptPath(Path path) {
        myTableScriptPath = path;
    }

    /**
     * @return the path to the insertion script
     */
    public final Path getInsertionScriptPath() {
        return myInsertionScriptPath;
    }

    /**
     * Sets the path, but does not execute the script
     *
     * @param path the path to the script
     */
    public void setInsertionScriptPath(Path path) {
        myInsertionScriptPath = path;
    }

    /**
     * Sets the path, but does not execute the script
     *
     * @param path
     */
    public void setAlterScriptPath(Path path) {
        myAlterScriptPath = path;
    }

    /**
     * Sets the path, but does not cause any inserts
     *
     * @param path the path to the script
     */
    public void setExcelInsertPath(Path path) {
        myExcelInsertPath = path;
    }

    /**
     * @return the path to the Excel workbook that holds data for inserts
     */
    public Path getExcelInsertPath() {
        return myExcelInsertPath;
    }

    /**
     * @return the path to a script that can alter the database
     */
    public Path getAlterScriptPath() {
        return myAlterScriptPath;
    }

    /**
     * @return a list of table names in the order that they must be truncated. May be empty.
     */
    public List<String> getTruncateTableOrder() {
        return Collections.unmodifiableList(myTruncateTableOrder);
    }

    /**
     * @return a list of table names in the order that they must be inserted. May be empty
     */
    public List<String> getInsertTableOrder() {
        return Collections.unmodifiableList(myInsertTableOrder);
    }

    /**
     * @return a list of strings representing the creation commands for the database. May be empty.
     */
    public List<String> getCreateCommands() {
        return Collections.unmodifiableList(myTableCommands);
    }

    /**
     * @return a list of strings representing the insertion commands for the database. May be empty.
     */
    public List<String> getInsertCommands() {
        return Collections.unmodifiableList(myInsertCommands);
    }

    /**
     * @return a list of strings representing the insertion commands for the database. May be empty.
     */
    public List<String> getAlterCommands() {
        return Collections.unmodifiableList(myAlterCommands);
    }

    /**
     * @param tableNames the names of the tables in the order needed for truncation
     */
    public final void setTruncateTableOrder(List<String> tableNames) {
        if (tableNames == null) {
            throw new IllegalArgumentException("The truncate table name array must not be null");
        }
        myTruncateTableOrder.clear();
        myTruncateTableOrder.addAll(tableNames);
    }

    /**
     * @param tableNames the names of the tables in the order needed for insertion
     */
    public final void setInsertTableOrder(List<String> tableNames) {
        if (tableNames == null) {
            throw new IllegalArgumentException("The truncate table name array must not be null");
        }
        myInsertTableOrder.clear();
        myInsertTableOrder.addAll(tableNames);
    }

    /**
     * @param pathToScript sets and executes the commands in the script for creating only tables in the database
     * @return true if all commands executed
     */
    public final boolean executeCreateTablesOnlyScript(Path pathToScript) throws IOException {
        if (pathToScript == null) {
            throw new IllegalArgumentException("The creation script path must not be null");
        }
        if (Files.notExists(pathToScript)) {
            throw new IllegalArgumentException("The creation script file does not exist");
        }
        setTablesOnlyScriptPath(pathToScript);
        List<String> parsedCmds = DatabaseIfc.parseQueriesInSQLScript(pathToScript);
        myTableCommands.clear();
        myTableCommands.addAll(parsedCmds);
        return executeCommands(myTableCommands);
    }

    /**
     * @param pathToScript sets and executes the commands in the script for creating the database
     * @return true if all commands executed
     */
    public final boolean executeCreationScript(Path pathToScript) throws IOException {
        if (pathToScript == null) {
            throw new IllegalArgumentException("The creation script path must not be null");
        }
        if (Files.notExists(pathToScript)) {
            throw new IllegalArgumentException("The creation script file does not exist");
        }
        setCreationScriptPath(pathToScript);
        List<String> parsedCmds = DatabaseIfc.parseQueriesInSQLScript(pathToScript);
        myCreationScriptCommands.clear();
        myCreationScriptCommands.addAll(parsedCmds);
        return executeCommands(myCreationScriptCommands);
    }

    /**
     * @param pathToScript sets and executes the commands in the script for inserting data into the database
     * @return true if all commands executed
     */
    public final boolean executeInsertionScript(Path pathToScript) throws IOException {
        if (pathToScript == null) {
            throw new IllegalArgumentException("The insertion script path must not be null");
        }
        if (Files.notExists(pathToScript)) {
            throw new IllegalArgumentException("The insertion script file does not exist");
        }
        setInsertionScriptPath(pathToScript);
        List<String> parsedCmds = DatabaseIfc.parseQueriesInSQLScript(pathToScript);
        myInsertCommands.clear();
        myInsertCommands.addAll(parsedCmds);
        return executeCommands(myInsertCommands);
    }

    /**
     * @param pathToScript sets and executes the commands in the script for altering the database
     * @return true if all commands executed
     */
    public final boolean executeAlterScript(Path pathToScript) throws IOException {
        if (pathToScript == null) {
            throw new IllegalArgumentException("The alter script path must not be null");
        }
        if (Files.notExists(pathToScript)) {
            throw new IllegalArgumentException("The alter script file does not exist");
        }
        setAlterScriptPath(pathToScript);
        List<String> parsedCmds = DatabaseIfc.parseQueriesInSQLScript(pathToScript);
        myAlterCommands.clear();
        myAlterCommands.addAll(parsedCmds);
        return executeCommands(myAlterCommands);
    }

    /**
     * @return a jooq Parser for parsing queries on the database
     */
    public Parser getParser() {
        return getDSLContext().parser();
    }

    /**
     * @return the DDL queries needed to define and create the database
     */
    private Queries getJOOQDDLQueries() {
        //TODO waiting on jooq fix
        //return create.ddl(getUserSchema(), DDLFlag.TABLE, DDLFlag.PRIMARY_KEY, DDLFlag.UNIQUE, DDLFlag.FOREIGN_KEY);
        Schema schema = getDefaultSchema();
        if (schema != null){
            return getDSLContext().ddl(schema);
        } else {
            return null;
        }
    }

    /**
     * @return the DDL queries needed to define and create the database as a string
     */
    private String getJOOQDDLQueriesAsString() {
        Schema schema = getDefaultSchema();
        if (schema != null){
            return getDSLContext().ddl(schema).toString();
        } else {
            return null;
        }
    }

    /**
     * Writes the DDL queries needed to define and create the database to a file
     *
     * @param out the place to write the queries
     */
    private void writeJOOQDDLQueries(PrintWriter out) {
        //TODO waiting on jooq fix
        Queries ddlQueries = getJOOQDDLQueries();
        if (ddlQueries == null){
            return;
        }
        Query[] queries = ddlQueries.queries();
        if (queries.length == 1) {
            return;
        }
        for (int i = 1; i < queries.length; i++) {
            out.print(queries[i]);
            out.print(";");
            out.println();
            out.flush();
        }
//        for (Query q : queries) {
//            out.print(q);
//            out.print(";");
//            out.println();
//            out.flush();
//        }
    }

    /**
     * Displays the DDL queries needed to define and create the database on the console
     */
    public void printJOOQDDLQueries() {
        writeJOOQDDLQueries(new PrintWriter(System.out));
    }

    /**
     * @return gets the DDL queries as a list of strings
     */
    private List<String> getDDLQueryStrings() {
        //TODO waiting on jooq fix
        List<String> list = new ArrayList<>();
        Queries ddlQueries = getJOOQDDLQueries();
        if (ddlQueries == null){
            return list;
        }
        Query[] queries = ddlQueries.queries();
        if (queries.length == 1) {
            return list;
        }
        for (int i = 1; i < queries.length; i++) {
            list.add(queries[i].getSQL());
        }
//        for (Query q : queries) {
//            list.add(q.getSQL());
//        }
        return list;
    }

    /** Uses the active database connection and derby system commands to freeze the database,
     * uses system OS commands to copy the database, and then unfreezes the database.  The duplicate name
     * and directory path must not already exist
     *
     * @param dupName the name of the duplicate database
     * @param directory the directory to place the database in
     * @throws SQLException thrown if the derby commands fail
     * @throws IOException thrown if the system file copy commands fail
     */
    public final void copyDb(String dupName, Path directory) throws SQLException, IOException {
        if (dupName == null) {
            throw new IllegalArgumentException("The duplicate's name must not be null!");
        }
        if (directory == null) {
            throw new IllegalArgumentException("The directory must not be null!");
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("The directory path was not a directory!");
        }

        if (Files.exists(directory.resolve(dupName))) {
            throw new IllegalArgumentException("A database with the supplied name already exists in the directory! db name = " + dupName);
        }
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        // freeze the database
        s.executeUpdate("CALL SYSCS_UTIL.SYSCS_FREEZE_DATABASE()");
        //copy the database directory during this interval
        // translate paths to files
        File target = directory.resolve(dupName).toFile();
        File source = this.getDBPath().toFile();
        FileUtils.copyDirectory(source, target);
        s.executeUpdate("CALL SYSCS_UTIL.SYSCS_UNFREEZE_DATABASE()");
        s.close();
        connection.close();
    }

}
