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
import jsl.utilities.reporting.JSL;
import org.jooq.*;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Property;
import org.jooq.meta.jaxb.Target;
import org.jooq.meta.jaxb.Generator;
import org.jooq.tools.jdbc.JDBCUtils;
//import org.jooq.util.GenerationTool;
//import org.jooq.util.jaxb.Generator;
//import org.jooq.util.jaxb.Property;
//import org.jooq.util.jaxb.Target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many databases define the terms database, user, schema in a variety of ways. This abstraction
 * defines this concept as the userSchema.  It is the name of the organizational construct for
 * which the user defined database object are contained. These are not the system abstractions.
 * The database name provided to the construct is for labeling and may or may not have any relationship
 * to the actual file name or database name of the database. The supplied connection has all
 * the information that it needs to access the database.
 */
public interface DatabaseIfc {

    enum LineOption {
        COMMENT, CONTINUED, END
    }

    Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    String DEFAULT_DELIMITER = ";";
    Pattern NEW_DELIMITER_PATTERN = Pattern.compile("(?:--|\\/\\/|\\#)?!DELIMITER=(.+)");
    Pattern COMMENT_PATTERN = Pattern.compile("^(?:--|\\/\\/|\\#).+");

    /**
     * @return the DataSource backing the database
     */
    DataSource getDataSource();

    /**
     * @return an identifying string representing the database. This has no relation to
     * the name of the database on disk or in the dbms. The sole purpose is for labeling of output
     */
    String getLabel();

    /**
     * @return the jooq SQL dialect for the database
     */
    SQLDialect getSQLDialect();

    /**
     * @return the jooq DSLContext for manipulating this database
     */
    DSLContext getDSLContext();

    /**
     * @return a String that represents the name of the default schema for the database.
     * This is the schema that contains the database objects such as the tables. This may
     * be null if no default schema is specified.
     */
    String getDefaultSchemaName();

    /**
     * Sets the name of the default schema
     *
     * @param name the name for the default schema, may be null
     */
    void setDefaultSchemaName(String name);

    /**
     * @return a jooq Schema representing the default schema that holds the user defined
     * tables that are in the database, or null if no default schema is specified
     */
    default Schema getDefaultSchema() {
        return getSchema(getDefaultSchemaName());
    }

    /**
     * @param option true means the default jooq execution logging is on, false means that it is not
     */
    default void setJooQDefaultExecutionLoggingOption(boolean option) {
        getDSLContext().settings().withExecuteLogging(option);
//        getDSLContext().settings().withExecuteUpdateWithoutWhere(ExecuteWithoutWhere.LOG_WARN)
//                .withExecuteDeleteWithoutWhere(ExecuteWithoutWhere.LOG_WARN);
//        getDSLContext().settings().withExecuteLogging(option)
//                .withExecuteDeleteWithoutWhere(ExecuteWithoutWhere.LOG_DEBUG);
        //getDSLContext().settings().setExecuteDeleteWithoutWhere(ExecuteWithoutWhere.LOG_DEBUG);
    }

    /**
     * @return true if jooq default execution logging is on
     */
    default boolean isJooQDefaultExecutionLoggingOn() {
        return getDSLContext().settings().isExecuteLogging();
    }

    /**
     * It is best to use this method within a try-with-resource construct
     * This method calls the DataSource for a connection. You are responsible for closing the connection.
     *
     * @return a connection to the database
     */
    default Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * @return the meta data about the database if available, or null
     */
    default DatabaseMetaData getDatabaseMetaData() {
        DatabaseMetaData metaData = null;
        try (Connection connection = getConnection()) {
            metaData = connection.getMetaData();
        } catch (SQLException e) {
            LOGGER.warn("The meta data was not available", e);
        }
        return metaData;
    }

    /**
     * @param schemaName the name of the schema that should contain the tables
     * @return a list of table names within the schema
     */
    default List<String> getTableNames(String schemaName) {
        List<Table<?>> tables = getTables(schemaName);
        List<String> list = new ArrayList<>();
        for (Table t : tables) {
            list.add(t.getName());
        }
        return list;
    }

    /**
     * @return a list of all table names within the database
     */
    default List<String> getAllTableNames() {
        Meta meta = getDSLContext().meta();
        List<Table<?>> tables = meta.getTables();
        List<String> list = new ArrayList<>();
        for (Table t : tables) {
            list.add(t.getName());
        }
        return list;
    }

    /**
     * @param schemaName the schema name to check
     * @return true if the database contains a schema with the provided name
     */
    default boolean containsSchema(String schemaName) {
        return getSchema(schemaName) != null;
    }

    /**
     * @param schema the schema to check
     * @return true if the schema is in this database
     */
    default boolean containsSchema(Schema schema) {
        Meta meta = getDSLContext().meta();
        List<Schema> schemas = meta.getSchemas();
        return schemas.contains(schema);
    }

    /**
     * The name of the schema is first checked for an exact lexicographical match.
     * If a match occurs, the schema is returned.  If a lexicographical match fails,
     * then a check for a match ignoring the case of the string is performed.
     * This is done because SQL identifier names should be case insensitive.
     * If neither matches then null is returned.
     *
     * @param schemaName the schema name to find
     * @return the jooq schema for the name or null
     */
    default Schema getSchema(String schemaName) {
        Meta meta = getDSLContext().meta();
        List<Schema> schemas = meta.getSchemas();
        //LOG.debug("Looking for schema {}",schemaName);
        //Schema found = null;
        for (Schema s : schemas) {
            if (s.getName().equals(schemaName)) {
                return s;
            } else if (s.getName().equalsIgnoreCase(schemaName)) {
                return s;
            }
        }
        // if it gets here it was not found
        return null;
    }

    /**
     * @param table a jooq table for a potential table in the database
     * @return true if the table is in this database
     */
    default boolean containsTable(Table<?> table) {
        Meta meta = getDSLContext().meta();
        List<Table<?>> tables = meta.getTables();
        return tables.contains(table);
    }

    /**
     * @param tableName the unqualified table name to find as a string
     * @return true if the database contains the table
     */
    default boolean containsTable(String tableName) {
        return getTable(tableName) != null;
    }

    /**
     * The name of the table is first checked for an exact lexicographical match.
     * If a match occurs, the table is returned.  If a lexicographical match fails,
     * then a check for a match ignoring the case of the string is performed.
     * This is done because SQL identifier names should be case insensitive.
     * If neither matches then null is returned.
     *
     * @param tableName the unqualified table name to find as a string
     * @return the jooq Table representation or null if not found
     */
    default Table<?> getTable(String tableName) {
        //LOG.debug("Looking for table {}",tableName);
        Meta meta = getDSLContext().meta();
        List<Table<?>> tables = meta.getTables();
        for (Table<?> t : tables) {
            if (t.getName().equals(tableName)) {
                return t;
            } else if (t.getName().equalsIgnoreCase(tableName)) {
                return t;
            }
        }
        return null;
    }

    /**
     * The name of the table is first checked for an exact lexicographical match.
     * If a match occurs, the table is returned.  If a lexicographical match fails,
     * then a check for a match ignoring the case of the string is performed.
     * This is done because SQL identifier names should be case insensitive.
     * If neither matches then null is returned.
     *
     * @param schema    the schema to check, must not be null
     * @param tableName the unqualified table name to find as a string
     * @return the jooq Table representation or null if not found
     */
    default Table<?> getTable(Schema schema, String tableName) {
        Objects.requireNonNull(schema, "The schema was null");
        //LOG.debug("Looking for table {}",tableName);
        Table<?> table = schema.getTable(tableName);
        if (table == null) {
            // try all upper case
            table = schema.getTable(tableName.toUpperCase());
            if (table == null) {
                // try all lower case
                table = schema.getTable(tableName.toLowerCase());
            }
        }
        return table;
    }

    /**
     * @param schemaName the name of the schema that should contain the tables
     * @return a list of jooq Tables that are in the specified schema of the database
     */
    default List<Table<?>> getTables(String schemaName) {
        Schema schema = getSchema(schemaName);
        if (schema == null) {
            return new ArrayList<>();
        }
        return schema.getTables();//TODO jooq returns List<Table<?>>
    }

    /**
     * Checks if tables exist in the specified schema
     *
     * @param schemaName the name of the schema that should contain the tables
     * @return true if at least one table exists in the schema
     */
    default boolean hasTables(String schemaName) {
        return (!getTables(schemaName).isEmpty());
    }

    /**
     * Checks if the supplied table exists in the schema
     *
     * @param schemaName the name of the schema that should contain the table
     * @param table      a string representing the unqualified name of the table
     * @return true if it exists
     */
    default boolean containsTable(String schemaName, String table) {
        return (getTable(schemaName, table) != null);
    }

    /**
     * @param schemaName the name of the schema that should contain the table
     * @param tableName  a string representation of the unqualified table name as recognized by valid SQL table name
     * @return a jooq Table, or null if no table with that name exists
     */
    default Table<?> getTable(String schemaName, String tableName) {
        Schema schema = getSchema(schemaName);
        if (schema == null) {
            return null;
        }
        return schema.getTable(tableName);
    }

    /**
     * Writes the table as comma separated values
     *
     * @param tableName the unqualified name of the table to write
     * @param out       the PrintWriter to write to
     */
    default void writeTableAsCSV(String tableName, PrintWriter out) {
        if (!containsTable(tableName)) {
            LOGGER.trace("Table: {} does not exist in database {}", tableName, getLabel());
            return;
        }
        out.println(selectAll(tableName).formatCSV());
        out.flush();
    }

    /**
     * Prints the table as comma separated values to the console
     *
     * @param tableName the unqualified name of the table to print
     */
    default void printTableAsCSV(String tableName) {
        writeTableAsCSV(tableName, new PrintWriter(System.out));
    }

    /**
     * Writes the table as prettified text
     *
     * @param tableName the unqualified name of the table to write
     * @param out       the PrintWriter to write to
     */
    default void writeTableAsText(String tableName, PrintWriter out) {
        if (!containsTable(tableName)) {
            LOGGER.trace("Table: {} does not exist in database {}", tableName, getLabel());
            return;
        }
        out.println(tableName);
        out.println(selectAll(tableName));
        out.flush();
    }

    /**
     * Prints the table as prettified text to the console
     *
     * @param tableName the unqualified name of the table to write
     */
    default void printTableAsText(String tableName) {
        writeTableAsText(tableName, new PrintWriter(System.out));
    }

    /**
     * Writes all tables as text
     *
     * @param schemaName the name of the schema that should contain the tables
     * @param out        the PrintWriter to write to
     */
    default void writeAllTablesAsText(String schemaName, PrintWriter out) {
        List<Table<?>> tables = getTables(schemaName);
        for (Table<?> table : tables) {
            out.println(table.getName());
            out.println(selectAll(table));
            out.flush();
        }
    }

    /**
     * @param table the Table to get all records from
     * @return the records as a jooq Result or null
     */
    default Result<Record> selectAll(Table<? extends Record> table) {
        if (table == null) {
            return null;
        }
        if (!containsTable(table)) {
            return null;
        }
        Result<Record> result = getDSLContext().select().from(table).fetch();
        return result;
    }

    /**
     * Prints all tables as text to the console
     *
     * @param schemaName the name of the schema that should contain the tables
     */
    default void printAllTablesAsText(String schemaName) {
        writeAllTablesAsText(schemaName, new PrintWriter(System.out));
    }

    /**
     * Writes all tables as separate comma separated value files into the supplied
     * directory. The files are written to text files using the same name as
     * the tables in the database
     *
     * @param schemaName            the name of the schema that should contain the tables
     * @param pathToOutPutDirectory the path to the output directory to hold the csv files
     * @throws IOException a checked exception
     */
    default void writeAllTablesAsCSV(String schemaName, Path pathToOutPutDirectory) throws IOException {
        Files.createDirectories(pathToOutPutDirectory);
        List<Table<?>> tables = getTables(schemaName);
        for (Table<?> table : tables) {
            Path path = pathToOutPutDirectory.resolve(table.getName() + ".csv");
            OutputStream newOutputStream;
            newOutputStream = Files.newOutputStream(path);
            PrintWriter printWriter = new PrintWriter(newOutputStream);
            printWriter.println(selectAll(table).formatCSV());
            printWriter.flush();
            printWriter.close();
        }
    }

    /**
     * @param tableName the unqualified name of the table to get all records from
     * @return a jooq result holding all of the records from the table or null
     */
    default Result<Record> selectAll(String tableName) {
        if (!containsTable(tableName)) {
            return null;
        }
        return selectAll(getTable(tableName));
    }

    /**
     * @param table the unqualified name of the table
     * @return true if the table contains no records (rows)
     */
    default boolean isTableEmpty(String table) {
        Result<Record> selectAll = selectAll(table);
        if (selectAll == null) {
            return true;
        }
        return selectAll.isEmpty();
    }

    /**
     * @param table the table to check
     * @return true if the table has no data in the result
     */
    default boolean isTableEmpty(Table<? extends Record> table) {
        Result<Record> selectAll = selectAll(table);
        if (selectAll == null) {
            return true;
        }
        return selectAll.isEmpty();
    }

    /**
     * @param schemaName the name of the schema that should contain the tables
     * @return true if at least one user defined table in the schema has data
     */
    default boolean hasData(String schemaName) {
        return areAllTablesEmpty(schemaName) != true;
    }

    /**
     * @param schemaName the name of the schema that should contain the tables
     * @return true if all user defined tables are empty in the schema
     */
    default boolean areAllTablesEmpty(String schemaName) {
        List<Table<?>> tables = getTables(schemaName);
        boolean result = true;
        for (Table<?> t : tables) {
            result = isTableEmpty(t);
            if (result == false) {
                break;
            }
        }
        return result;
    }


    /**
     * @param tableName the unqualified name of the table
     * @return a string that represents all of the insert queries for the data that is currently in the
     * supplied table or null
     */
    default String getInsertQueries(String tableName) {
        Table<? extends Record> table = getTable(tableName);
        if (table == null) {
            return null;
        }
        return getInsertQueries(table);
    }

    /**
     * @param table the table to generate the insert statements for, must not be null
     * @return the insert statements as a string or null
     */
    default String getInsertQueries(Table<? extends Record> table) {
        if (table == null) {
            LOGGER.trace("The supplied table reference was null");
            throw new IllegalArgumentException("The supplied table was null");
        }
        if (!containsTable(table)) {
            LOGGER.trace("Table: {} does not exist in database {}", table.getName(), getLabel());
            return null;
        }
        Result<Record> results = selectAll(table);
        return results.formatInsert(table);
    }

    /**
     * Prints the insert queries associated with the supplied table to the console
     *
     * @param tableName the unqualified name of the table
     */
    default void printInsertQueries(String tableName) {
        writeInsertQueries(tableName, new PrintWriter(System.out));
    }

    /**
     * Writes the insert queries associated with the supplied table to the PrintWriter
     *
     * @param tableName the unqualified name of the table
     * @param out       the PrintWriter to write to
     */
    default void writeInsertQueries(String tableName, PrintWriter out) {
        if (!containsTable(tableName)) {
            LOGGER.trace("Table: {} does not exist in database {}", tableName, getLabel());
            return;
        }
        writeInsertQueries(getTable(tableName), out);
    }

    /**
     * Writes the insert statements for the table in the file
     *
     * @param table the the table for the insert statements
     * @param out   the file to write to
     */
    default void writeInsertQueries(Table<? extends Record> table, PrintWriter out) {
        if (table == null) {
            throw new IllegalArgumentException("The supplied table was null");
        }
        if (!containsTable(table)) {
            LOGGER.trace("Table: {} does not exist in database {}", table.getName(), getLabel());
            return;
        }
        Result<Record> results = selectAll(table);
        out.print(results.formatInsert(table));
        out.flush();
    }

    /**
     * Prints all table data as insert queries to the console
     *
     * @param schemaName the name of the schema that should contain the tables
     */
    default void printAllTablesAsInsertQueries(String schemaName) {
        writeAllTablesAsInsertQueries(schemaName, new PrintWriter(System.out));
    }

    /**
     * Writes all table data as insert queries to the PrintWriter
     *
     * @param schemaName the name of the schema that should contain the tables
     * @param out        the PrintWriter to write to
     */
    default void writeAllTablesAsInsertQueries(String schemaName, PrintWriter out) {
        List<Table<?>> tables = getTables(schemaName);
        for (Table<?> t : tables) {
            writeInsertQueries(t, out);
        }
    }

    /**
     * Writes all the tables to an Excel workbook, uses name of schema, uses the working directory
     */
    default void writeDbToExcelWorkbook(String schemaName) throws IOException {
        writeDbToExcelWorkbook(schemaName, null, null);
    }

    /**
     * Writes all the tables to an Excel workbook, uses name of database
     *
     * @param schemaName  the name of the schema that should contain the tables
     * @param wbDirectory directory of the workbook, if null uses the working directory
     */
    default void writeDbToExcelWorkbook(String schemaName, Path wbDirectory) throws IOException {
        writeDbToExcelWorkbook(schemaName, null, wbDirectory);
    }

    /**
     * Writes all the tables to an Excel workbook uses the working directory
     *
     * @param schemaName the name of the schema that should contain the tables
     * @param wbName     name of the workbook, if null uses name of database
     */
    default void writeDbToExcelWorkbook(String schemaName, String wbName) throws IOException {
        writeDbToExcelWorkbook(schemaName, wbName, null);
    }

    /**
     * Writes all the tables in the supplied schema to an Excel workbook
     *
     * @param schemaName  the name of the schema that should contain the tables, must not be null
     * @param wbName      name of the workbook, if null uses name of database
     * @param wbDirectory directory of the workbook, if null uses the working directory
     */
    default void writeDbToExcelWorkbook(String schemaName, String wbName, Path wbDirectory) throws IOException {
        Objects.requireNonNull(schemaName, "The schema name was null");
        if (!containsSchema(schemaName)) {
            LOGGER.warn("Attempting to write to Excel: The supplied schema name {} is not in database {}",
                    schemaName, getLabel());
            return;
        }
        List<String> tableNames = getTableNames(schemaName);
        if (tableNames.isEmpty()) {
            LOGGER.warn("The supplied schema name {} had no tables to write to Excel in database {}",
                    schemaName, getLabel());
        } else {
            writeDbToExcelWorkbook(tableNames, wbName, wbDirectory);
        }
    }

    /**
     * Writes the tables in the supplied list to an Excel workbook, if they exist in the database.
     *
     * @param tableNames  a list of table names that should be written to Excel, must not be null
     * @param wbName      name of the workbook, if null uses name of database
     * @param wbDirectory directory of the workbook, if null uses the working directory
     */
    default void writeDbToExcelWorkbook(List<String> tableNames, String wbName, Path wbDirectory) throws IOException {
        Objects.requireNonNull(tableNames, "The list of table names was null");
        if (wbName == null) {
            wbName = getLabel() + ".xlsx";
        } else {
            // name is not null make sure it has .xlsx
            if (!wbName.endsWith(".xlsx")) {
                wbName = wbName.concat(".xlsx");
            }
        }
        if (wbDirectory == null) {
            wbDirectory = JSL.getInstance().getExcelDir().toAbsolutePath();
        }
        Path path = wbDirectory.resolve(wbName);
        if (tableNames.isEmpty()) {
            LOGGER.warn("The supplied list of table names was empty when writing to Excel in database {}", getLabel());
        } else {
            ExcelUtil.writeDBAsExcelWorkbook(this, tableNames, path);
        }
    }

    /**
     * @return returns a DbCreateTask that can be configured to execute on the database
     */
    default DbCreateTask.DbCreateTaskFirstStepIfc create() {
        return new DbCreateTask.DbCreateTaskBuilder(this);
    }

    /**
     * Executes a single command on an database connection
     *
     * @param cmd a valid SQL command
     * @return true if the command executed without an SQLException
     */
    default boolean executeCommand(String cmd) {
        boolean flag = false;
        try (Connection con = getConnection()) {
            flag = executeCommand(con, cmd);
        } catch (SQLException ex) {
            LOGGER.error("SQLException when executing {}", cmd, ex);
        }
        return flag;
    }

    /**
     * Executes the SQL provided in the string. Squelches exceptions The string
     * must not have ";" semi-colon at the end. The caller is responsible for closing the connection
     *
     * @param con a connection for preparing the statement
     * @param cmd the command
     * @return true if the command executed without an exception
     */
    default boolean executeCommand(Connection con, String cmd) {
        boolean flag = false;
        try (Statement statement = con.createStatement()) {
            statement.execute(cmd);
            LOGGER.trace("Executed SQL: {}", cmd);
            statement.close();
            flag = true;
        } catch (SQLException ex) {
            LOGGER.error("SQLException when executing {}", cmd, ex);
        }
        return flag;
    }

    /**
     * Consecutively executes the list of SQL queries supplied as a list of
     * strings The strings must not have ";" semi-colon at the end.
     *
     * @param cmds the commands
     * @return true if all commands were executed
     */
    default boolean executeCommands(List<String> cmds) {
        boolean flag = true;
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            for (String cmd : cmds) {
                flag = executeCommand(con, cmd);
                if (flag == false) {
                    con.rollback();
                    break;
                }
            }
            if (flag == true) {
                con.commit();
            }
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            flag = false;
            LOGGER.error("SQLException: ", ex);
        }
        return flag;
    }

    /**
     * Executes the commands in the script on the database
     *
     * @param path the path
     * @return true if all commands are executed
     */
    default boolean executeScript(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("The script path must not be null");
        }
        if (Files.notExists(path)) {
            throw new IllegalArgumentException("The script file does not exist");
        }
        LOGGER.trace("Executing SQL in file: {}", path);
        return executeCommands(parseQueriesInSQLScript(path));
    }

    /** A simple wrapper to ease the use of jooq for novices. Returns a jooq query that can
     *  be executed to return results.  Errors in the SQL are the user's responsibility.
     *  With the query, the user has multiple paths to execution.
     *
     * @param sql an SQL text string that is valid
     * @return the query, ready to execute
     */
    default ResultQuery<Record> createResultQuery(String  sql){
        return getDSLContext().resultQuery(sql);
    }

    /** A simple wrapper to ease the use of jooq for novices. Returns the results of a jooq query that can
     *  be executed to return results. Errors in the SQL are the user's responsibility
     *
     * @param sql an SQL text string that is valid
     * @return the results of the query, basically uses fetch() on createResultQuery(String sql)
     */
    default Result<Record> fetchResults(String sql){
        return createResultQuery(sql).fetch();
    }

    /** A simple wrapper to ease the use of JDBC for novices. Returns the results of a query in the
     *  form of a JDBC ResultSet. Errors in the SQL are the user's responsibility
     *
     * @param sql an SQL text string that is valid
     * @return the results of the query
     */
    default ResultSet fetchJDBCResultSet(String sql){
        return createResultQuery(sql).fetchResultSet();
    }

    /**
     * Method to parse a SQL script for the database. The script honors SQL
     * comments and separates each SQL command into a list of strings, 1 string
     * for each command. The list of queries is returned.
     * <p>
     * The script should have each command end in a semi-colon, ; The best
     * comment to use is #. All characters on a line after # will be stripped.
     * Best to put # as the first character of a line with no further SQL on the
     * line
     * <p>
     * Based on the work described here:
     * <p>
     * https://blog.heckel.xyz/2014/06/22/run-sql-scripts-from-java-on-hsqldb-derby-mysql/
     *
     * @param filePath a path to the file for parsing
     * @return the list of strings of the commands
     */
    public static List<String> parseQueriesInSQLScript(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("The supplied path was null!");
        }
        List<String> queries = new ArrayList<>();
        InputStream in = Files.newInputStream(filePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder cmd = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            //boolean end = parseCommandString(line, cmd);
            LineOption option = parseLine(line, cmd);
            if (option == LineOption.END) {
                String trimmedString = cmd.toString().trim();
                //System.out.println(trimmedString);
                queries.add(trimmedString);
                cmd = new StringBuilder();
            }
        }
        return queries;
    }

    /**
     * Takes the input string and builds a string to represent the SQL command from
     * the string. Uses EmbeddedDerbyDatabase.DEFAULT_DELIMITER as the delimiter, i.e. ";"
     * Checks for "--", "//" and "#" as start of line comments
     *
     * @param line    the input to parse
     * @param command the parsed output
     * @return the LineOption COMMENT means line was a comment, CONTINUED means that
     * command continues on next line, END means that command was ended with the delimiter
     */
    public static LineOption parseLine(String line, StringBuilder command) {
        return parseLine(line, DEFAULT_DELIMITER, command);
    }

    /**
     * Takes the input string and builds a string to represent the SQL command from
     * the string.  Checks for "--", "//" and "#" as start of line comments
     *
     * @param line      the input to parse
     * @param delimiter the end of comand indicator
     * @param command   the parsed output
     * @return the LineOption COMMENT means line was a comment, CONTINUED means that
     * command continues on next line, END means that command was ended with the delimiter
     */
    public static LineOption parseLine(String line, String delimiter, StringBuilder command) {
        String trimmedLine = line.trim();

        if (trimmedLine.startsWith("--")
                || trimmedLine.startsWith("//")
                || trimmedLine.startsWith("#")) {
            return LineOption.COMMENT;
        }
        // not a comment, could be end of command or continued on next line
        // add the line to the command
        //command.append(trimmedLine);
        if (trimmedLine.endsWith(delimiter)) {
            // remove the delimiter
            trimmedLine = trimmedLine.replaceFirst(delimiter, " ");
            trimmedLine = trimmedLine.trim();
            command.append(trimmedLine);
//            command.delete(command.length() - delimiter.length() - 1, command.length());
            command.append(" ");
            return LineOption.END;
        }
        command.append(trimmedLine);
        command.append(" ");
        // already added the line, command must be continued on next line
        return LineOption.CONTINUED;
    }

    /**
     * Writes SQLWarnings to log file
     *
     * @param conn the connection
     * @throws SQLException the exception
     */
    public static void logWarnings(Connection conn) throws SQLException {
        SQLWarning warning = conn.getWarnings();
        if (warning != null) {
            while (warning != null) {
                LOGGER.warn("Message: {}", warning.getMessage());
                warning = warning.getNextWarning();
            }
        }
    }

    /**
     * Parses the supplied string and breaks it up into a list of strings The
     * string needs to honor SQL comments and separates each SQL command into a
     * list of strings, 1 string for each command. The list of queries is
     * returned.
     * <p>
     * The script should have each command end in a semi-colon, ; The best
     * comment to use is #. All characters on a line after # will be stripped.
     * Best to put # as the first character of a line with no further SQL on the
     * line
     *
     * @param str A big string that has SQL queries
     * @return a list of strings representing each SQL command
     */
    public static List<String> parseQueriesInString(String str) throws IOException {
        List<String> queries = new ArrayList<>();
        if (str != null) {
            StringReader sr = new StringReader(str); // wrap your String
            BufferedReader reader = new BufferedReader(sr); // wrap your StringReader
            StringBuilder cmd = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                //boolean end = parseCommandString(line, cmd);
                LineOption option = parseLine(line, cmd);
                if (option == LineOption.END) {
                    queries.add(cmd.toString().trim());
                    cmd = new StringBuilder();
                }
            }
        }
        return queries;
    }

    /**
     * Takes the input string and builds a string to represent the command from
     * the string.
     *
     * @param input   the input to parse
     * @param command the parsed output
     * @return true if the parse was successful
     */
    public static boolean parseCommandString(String input, StringBuilder command) {
        String delimiter = DEFAULT_DELIMITER;
        String trimmedLine = input.trim();

        Matcher delimiterMatcher = NEW_DELIMITER_PATTERN.matcher(trimmedLine);
        Matcher commentMatcher = COMMENT_PATTERN.matcher(trimmedLine);

        if (delimiterMatcher.find()) {
            // a) Delimiter change
            delimiter = delimiterMatcher.group(1);
            //LOGGER.log(Level.INFO, "SQL (new delimiter): {0}", delimiter);
        } else if (commentMatcher.find()) {
            // b) Comment
            //LOGGER.log(Level.INFO, "SQL (comment): {0}", trimmedLine);
        } else { // c) Statement
            command.append(trimmedLine);
            command.append(" ");
            // End of statement
            if (trimmedLine.endsWith(delimiter)) {
                command.delete(command.length() - delimiter.length() - 1, command.length());
                LOGGER.trace("Parsed SQL: {}", command);
                return true;
            }
        }
        return false;
    }

    /**
     * Runs jooq code generation on the database at the supplied path.  Assumes
     * that the database exists and has well defined structure.  Places generated
     * source files in named package with the main java source
     *
     * @param dataSource  a DataSource that can provide a connection to the database, must not be null
     * @param schemaName  the name of the schema for which tables need to be generated, must not be null
     * @param pkgDirName  the directory that holds the target package, must not be null
     * @param packageName name of package to be created to hold generated code, must not be null
     */
    public static void jooqCodeGenerationDerbyDatabase(DataSource dataSource, String schemaName,
                                                       String pkgDirName, String packageName) throws Exception {
        Objects.requireNonNull(dataSource, "The data source was null");
        Objects.requireNonNull(schemaName, "The schema name was null");
        Objects.requireNonNull(pkgDirName, "The package directory was null");
        Objects.requireNonNull(packageName, "The package name was null");

        Connection connection = dataSource.getConnection();
        org.jooq.meta.jaxb.Configuration configuration = new org.jooq.meta.jaxb.Configuration()
                .withGenerator(new Generator()
                        .withDatabase(new org.jooq.meta.jaxb.Database()
                                .withName("org.jooq.meta.derby.DerbyDatabase")
                                .withInputSchema(schemaName))
                        .withTarget(new Target()
                                .withPackageName(packageName)
                                .withDirectory(pkgDirName)));
        GenerationTool tool = new GenerationTool();
        tool.setConnection(connection);
        tool.run(configuration);
    }

    /**
     * Runs jooq code generation on the database at the supplied creation script.
     * Places generated source files in named package with the main java source
     *
     * @param pathToCreationScript a path to a valid database creation script, must not be null
     * @param schemaName           the name of the schema for which tables need to be generated, must not be null
     * @param pkgDirName           the directory that holds the target package, must not be null
     * @param packageName          name of package to be created to hold generated code, must not be null
     */
    public static void jooqCodeGeneration(Path pathToCreationScript, String schemaName,
                                          String pkgDirName, String packageName) throws Exception {
        Objects.requireNonNull(pathToCreationScript, "The path to the creation script was null");
        Objects.requireNonNull(schemaName, "The schema name was null");
        Objects.requireNonNull(pkgDirName, "The package directory was null");
        Objects.requireNonNull(packageName, "The package name was null");
        org.jooq.meta.jaxb.Configuration configuration = new org.jooq.meta.jaxb.Configuration();
        configuration.withGenerator(new Generator()
                .withDatabase(
                        new org.jooq.meta.jaxb.Database()
                                .withName("org.jooq.meta.ddl.DDLDatabase")
                                .withInputSchema(schemaName)
                                .withProperties(new Property()
                                        .withKey("scripts")
                                        .withValue(pathToCreationScript.toString())))
                .withTarget(new Target().withPackageName(packageName).withDirectory(pkgDirName)));

        GenerationTool tool = new GenerationTool();
        tool.run(configuration);
    }

    /**
     * Runs jooq code generation on the database at the supplied path.  Assumes
     * that the database exists and has well defined structure.  Places generated
     * source files in package gensrc with the main java source. Squelches all exceptions.
     *
     * @param dataSource  a DataSource that can provide a connection to the database, must not be null
     * @param schemaName  the name of the schema for which tables need to be generated, must not be null
     * @param pkgDirName  the directory that holds the target package, must not be null
     * @param packageName name of package to be created to hold generated code, must not be null
     */
    public static void runJooQCodeGenerationDerbyDatabase(DataSource dataSource, String schemaName,
                                                          String pkgDirName, String packageName) {
        try {
            jooqCodeGenerationDerbyDatabase(dataSource, schemaName, pkgDirName, packageName);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.trace("Error in jooq code generation for database: schemaName {} ,pkgDirName {}, packageName {}", schemaName, pkgDirName, packageName);
        }
    }

    /**
     * Drops the named schema from the database. If no such schema exist with the name, then nothing is done.
     *
     * @param schemaName the name of the schema to drop, must not be null
     * @param tableNames the table names in the order that they must be dropped, must not be null
     * @param viewNames  the view names in the order that they must be dropped, must not be null
     */
    default void dropSchema(String schemaName, List<String> tableNames, List<String> viewNames) {
        Objects.requireNonNull(schemaName, "The schema name cannot be null");
        Objects.requireNonNull(tableNames, "The list of table names cannot be null");
        Objects.requireNonNull(viewNames, "The list of view names cannot be null");
        if (containsSchema(schemaName)) {
            // need to delete the schema and any tables/data
            Schema schema = getSchema(schemaName);
            LOGGER.debug("The database {} contains the JSL schema {}", getLabel(), schema.getName());
            LOGGER.debug("Attempting to drop the schema {}....", schema.getName());

            //first drop any views, then the tables
            org.jooq.Table<?> table = null;
            List<org.jooq.Table<?>> tables = schema.getTables();
            LOGGER.debug("Schema {} has jooq tables or views ... ", schema.getName());
            for (org.jooq.Table<?> t : tables) {
                LOGGER.debug("table or view: {}", t.getName());
            }
            for (String name : viewNames) {
                if (name == null) {
                    continue;
                }
                LOGGER.debug("Checking for view {} ", name);
                table = getTable(schema, name);
                if (table != null) {
                    getDSLContext().dropView(table).execute();
                    LOGGER.debug("Dropped view {} ", table.getName());
                }
            }
            for (String name : tableNames) {
                if (name == null) {
                    continue;
                }
                LOGGER.debug("Checking for table {} ", name);
                table = getTable(schema, name);
                if (table != null) {
                    getDSLContext().dropTable(table).execute();
                    LOGGER.debug("Dropped table {} ", table.getName());
                }
            }
            getDSLContext().dropSchema(schema.getName()).execute(); // works
            //db.getDSLContext().dropSchema(schema).execute(); // doesn't work
            // db.getDSLContext().execute("drop schema jsl_db restrict"); //works
            //boolean exec = db.executeCommand("drop schema jsl_db restrict");
            LOGGER.debug("Completed the dropping of the schema {}", schema.getName());
        } else {
            LOGGER.debug("The database {} does not contain the schema {}", getLabel(), schemaName);
            List<Schema> schemas = getDSLContext().meta().getSchemas();
            LOGGER.debug("The database {} has the following schemas", getLabel());
            for (Schema s : schemas) {
                LOGGER.debug("schema: {}", s.getName());
            }
        }
    }

    /**  Attempts to determine the SQLDialect for the data source
     *  <a href= "https://www.jooq.org/javadoc/latest/org/jooq/tools/jdbc/JDBCUtils.html#dialect-java.sql.Connection-"> Reference to JDBCUtils</a>
     *
     * @param dataSource the data source, must not null
     * @return the SQLDialect wrapped in an Optional, may be null if no connection could be established to
     * determine the dialect.
     */
    public static Optional<SQLDialect> getSQLDialect(DataSource dataSource) {
        Objects.requireNonNull(dataSource, "The database source was null");
        SQLDialect dialect = null;
        try {
            Connection connection = dataSource.getConnection();
            dialect = JDBCUtils.dialect(connection);
            connection.close();
            return Optional.of(dialect);
        } catch (SQLException e) {
            LOGGER.warn("Could not establish connection to data sources to determine SQLDialect");
            return Optional.ofNullable(dialect);
        }
    }

}
