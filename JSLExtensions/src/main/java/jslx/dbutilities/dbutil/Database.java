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

import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.conf.Settings;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.Objects;
import java.util.Optional;

/**
 * A concrete implementation of the DatabaseIfc interface.
 * <p>
 * Many databases define the terms database, user, schema in a variety of ways. This abstraction
 * defines this concept as the userSchema.  It is the name of the organizational construct for
 * which the user defined database objects are contained. These are not the system abstractions.
 * The database label provided to the construct is for labeling and may or may not have any relationship
 * to the actual file name or database name of the database. The supplied DataSource has all
 * the information that it needs to access the database.
 */
public class Database implements DatabaseIfc {

    static {
        System.setProperty("org.jooq.no-logo", "true");
    }

    private final String myLabel;
    private final DataSource myDataSource;
    private final SQLDialect mySQLDialect;
    private String myDefaultSchemaName;
    private final DSLContext myDSLContext;

    /** Create a Database.  The SQLDialect is guessed based on establishing a connection
     * with the supplied DataSource. If the dialect cannot be guessed then an exception will occur.
     * This can be checked prior to the call by using:
     *
     *  <a href= "https://www.jooq.org/javadoc/latest/org/jooq/tools/jdbc/JDBCUtils.html#dialect-java.sql.Connection-"> Reference to JDBCUtils</a>
     *
     * @param dbLabel    a string representing a label for the database must not be null. This label
     *                   may or may not have any relation to the actual name of the database. This is
     *                   used for labeling purposes.
     * @param dataSource the DataSource backing the database, must not be null
     */
    public Database(String dbLabel, DataSource dataSource) {
        this(dbLabel, dataSource, null);
    }

    /**
     * @param dbLabel    a string representing a label for the database must not be null. This label
     *                   may or may not have any relation to the actual name of the database. This is
     *                   used for labeling purposes.
     * @param dataSource the DataSource backing the database, must not be null
     * @param dialect    the SLQ dialect for this type of database. It obviously must
     *                   be consistent with the database referenced by the connection
     */
    public Database(String dbLabel, DataSource dataSource, SQLDialect dialect) {
        this(dbLabel, dataSource, dialect, null);
    }

    /**
     * @param dbLabel    a string representing a label for the database must not be null. This label
     *                   may or may not have any relation to the actual name of the database. This is
     *                   used for labeling purposes.
     * @param dataSource the DataSource backing the database, must not be null
     * @param dialect    the SLQ dialect for this type of database. It obviously must
     *                   be consistent with the database referenced by the connection
     * @param settings  the JOOQ settings for the database context, may be null
     */
    public Database(String dbLabel, DataSource dataSource, SQLDialect dialect, Settings settings) {
        Objects.requireNonNull(dbLabel, "The database name was null");
        Objects.requireNonNull(dataSource, "The database source was null");

        if (dialect == null){
            Optional<SQLDialect> optionalSQLDialect = DatabaseIfc.getSQLDialect(dataSource);
            if (optionalSQLDialect.isPresent()){
                dialect = optionalSQLDialect.get();
                if (dialect == SQLDialect.DEFAULT){
                    LOGGER.error("Could not determine SQLDialect for database {}", dbLabel);
                    throw new DataAccessException("Could not determine the SQLDialect for database " + dbLabel);
                }
            } else {
                LOGGER.error("Could not establish connection to database {} to determine SQLDialect", dbLabel);
                throw new DataAccessException("Could not establish database connection to determine SQLDialect for database " + dbLabel);
            }
        }
        myLabel = dbLabel;
        myDataSource = dataSource;
        mySQLDialect = dialect;
        if (settings == null) {
            myDSLContext = DSL.using(dataSource, dialect);
        }else {
            myDSLContext = DSL.using(dataSource, dialect, settings);
        }
        setJooQDefaultExecutionLoggingOption(false);
        // force it to be made by establishing a connection to get the meta data
        getDatabaseMetaData();
        LOGGER.info("Established connection to Database {} ", dbLabel);
    }

    @Override
    public final DataSource getDataSource() {
        return myDataSource;
    }

    @Override
    public final String getLabel() {
        return myLabel;
    }

    @Override
    public final SQLDialect getSQLDialect() {
        return mySQLDialect;
    }

    @Override
    public DSLContext getDSLContext() {
        return myDSLContext;
    }

    @Override
    public String getDefaultSchemaName() {
        return myDefaultSchemaName;
    }

    @Override
    public void setDefaultSchemaName(String defaultSchemaName) {
        LOGGER.debug("Setting the default schema name to {}", defaultSchemaName);
        myDefaultSchemaName = defaultSchemaName;
        if (defaultSchemaName != null) {
            if (!containsSchema(defaultSchemaName)) {
                LOGGER.warn("The supplied default schema name {} was not in the database {}.",
                        defaultSchemaName, myLabel);
            }
        } else {
            LOGGER.warn("The default schema name was set to null for database {}.", myLabel);
        }
    }

}
