package jslx.dbutilities.dbutil;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jsl.utilities.JSLFileUtil;
import jslx.dbutilities.JSLDatabase;
import org.apache.derby.jdbc.ClientDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConnection;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

public class DatabaseFactory {

    /**
     * If the database already exists it is deleted. Creates the database in JSLDatabase.dbDir
     *
     * @param dbName the name of the database
     * @return the created database
     */
    public static DatabaseIfc createEmbeddedDerbyDatabase(String dbName) {
        return createEmbeddedDerbyDatabase(dbName, JSLDatabase.dbDir);
    }

    /**
     * If the database already exists it is deleted
     *
     * @param dbName the name of the embedded database. Must not be null
     * @param dbDir  a path to the directory to hold the database. Must not be null
     * @return the created database
     */
    public static DatabaseIfc createEmbeddedDerbyDatabase(String dbName, Path dbDir) {
        Objects.requireNonNull(dbName, "The database name was null");
        Objects.requireNonNull(dbDir, "The path to the database must not be null");
        Path pathToDb = dbDir.resolve(dbName);
        deleteEmbeddedDerbyDatabase(pathToDb);
        DataSource ds = createEmbeddedDerbyDataSource(pathToDb, true);
        Database db = new Database(dbName, ds, SQLDialect.DERBY);
        return db;
    }

    /**
     * @param dbLabel    a label for the database
     * @param dataSource the data source for connections
     * @return the created database
     */
    public static DatabaseIfc createEmbeddedDerbyDatabase(String dbLabel, DataSource dataSource) {
        Objects.requireNonNull(dataSource, "The data source was null");
        Database db = new Database(dbLabel, dataSource, SQLDialect.DERBY);
        return db;
    }

    /**
     * The database must already exist. It is not created. An exception is thrown if it does not exist.
     * Assumes that the named database is  in JSLDatabase.dbDir
     *
     * @param dbName the name of the embedded database, must not be null
     * @return the created database
     */
    public static DatabaseIfc getEmbeddedDerbyDatabase(String dbName) {
        return getEmbeddedDerbyDatabase(dbName, JSLDatabase.dbDir);
    }

    /**
     * The database must already exist. It is not created. An exception is thrown if it does not exist.
     *
     * @param dbName the name of the embedded database, must not be null
     * @param dbDir  a path to the directory that holds the database, must not be null
     * @return the created database
     */
    public static DatabaseIfc getEmbeddedDerbyDatabase(String dbName, Path dbDir) {
        Objects.requireNonNull(dbName, "The database name was null");
        Objects.requireNonNull(dbDir, "The path to the database must not be null");
        Path pathToDb = dbDir.resolve(dbName);
        if (!isEmbeddedDerbyDatabaseExists(dbName, dbDir)) {
            throw new IllegalStateException("The database does not exist at location " + pathToDb);
        }
        DataSource ds = createEmbeddedDerbyDataSource(pathToDb, false);
        Database db = new Database(dbName, ds, SQLDialect.DERBY);
        return db;
    }

    /**
     * The database must already exist. It is not created. An exception is thrown if it does not exist.
     *
     * @param pathToDb the full path to the directory that is the database, must not be null
     * @return the database
     */
    public static DatabaseIfc getEmbeddedDerbyDatabase(Path pathToDb) {
        Objects.requireNonNull(pathToDb, "The path to the database must not be null");
        if (!isEmbeddedDerbyDatabaseExists(pathToDb)) {
            throw new IllegalStateException("The database does not exist at location " + pathToDb);
        }
        DataSource ds = createEmbeddedDerbyDataSource(pathToDb, false);
        Database db = new Database(pathToDb.getFileName().toString(), ds, SQLDialect.DERBY);
        return db;
    }

    /**
     * Checks if the database exists in in JSLDatabase.dbDir
     *
     * @param dbName the name of the database
     * @return true if it exists false if not
     */
    public static boolean isEmbeddedDerbyDatabaseExists(String dbName) {
        return isEmbeddedDerbyDatabaseExists(dbName, JSLDatabase.dbDir);
    }

    /**
     * @param dbName the name of the database
     * @param dbDir  the directory to the database
     * @return true if it exists false if not
     */
    public static boolean isEmbeddedDerbyDatabaseExists(String dbName, Path dbDir) {
        if ((dbDir == null) || (dbName == null)) {
            return false;
        }
        Path pathToDb = dbDir.resolve(dbName);
        return isEmbeddedDerbyDatabaseExists(pathToDb);
    }

    /**
     * @param fullPath the full path to the database including its name (because derby
     *                 stores the database in a directory
     * @return true if it exists
     */
    public static boolean isEmbeddedDerbyDatabaseExists(Path fullPath) {
        if (fullPath == null) {
            return false;
        }
        return Files.exists(fullPath);
    }

    /**
     * This does not check if the database is shutdown.  It simply removes the
     * database from the file system.  If it doesn't exist, then nothing happens.
     *
     * @param pathToDb the path to the embedded database on disk
     */
    public static void deleteEmbeddedDerbyDatabase(Path pathToDb) {
        boolean b = JSLFileUtil.deleteDirectory(pathToDb.toFile());
        if (b){
            DatabaseIfc.LOGGER.info("Deleting directory to derby database {}", pathToDb);
        } else {
            DatabaseIfc.LOGGER.error("Unable to delete directory to derby database {}", pathToDb);
//            throw new DataAccessException("Unable to delete directory to derby database: " + pathToDb.toString());
        }

//        try {
//            JSLFileUtil.deleteDirectory(pathToDb.toFile());
//            DatabaseIfc.LOGGER.info("Deleting directory to derby database {}", pathToDb);
//        } catch (IOException e) {
//            DatabaseIfc.LOGGER.error("Unable to delete directory to derby database {}", pathToDb);
//            throw new DataAccessException("Unable to delete directory to derby database: " + pathToDb.toString());
//        }
    }

    /**
     * The database must already exist. It will not be created
     *
     * @param pathToDb a path to the database, must not be null
     * @return the created DataSource
     */
    public static DataSource createEmbeddedDerbyDataSource(Path pathToDb) {
        return createEmbeddedDerbyDataSource(pathToDb, null, null, false);
    }

    /**
     * @param pathToDb a path to the database, must not be null
     * @param create   a flag to indicate if the database should be created upon first connection
     * @return the created DataSource
     */
    public static DataSource createEmbeddedDerbyDataSource(Path pathToDb, boolean create) {
        return createEmbeddedDerbyDataSource(pathToDb, null, null, create);
    }

    /**
     * @param pathToDb a path to the database, must not be null
     * @param user     a user name, can be null
     * @param pWord    a password, can be null
     * @param create   a flag to indicate if the database should be created upon first connection
     * @return the created DataSource
     */
    public static DataSource createEmbeddedDerbyDataSource(Path pathToDb, String user,
                                                           String pWord, boolean create) {
        return createEmbeddedDerbyDataSource(pathToDb.toString(), user, pWord, create);
    }

    /**
     * Assumes that the database exists
     *
     * @param dbName the path to the database, must not be null
     * @return the created DataSource
     */
    public static DataSource createEmbeddedDerbyDataSource(String dbName) {
        return createEmbeddedDerbyDataSource(dbName, null, null, false);
    }

    /**
     * @param dbName the path to the database, must not be null
     * @param create a flag to indicate if the database should be created upon first connection
     * @return the created DataSource
     */
    public static DataSource createEmbeddedDerbyDataSource(String dbName, boolean create) {
        return createEmbeddedDerbyDataSource(dbName, null, null, create);
    }

    /**
     * @param dbName the path to the database, must not be null
     * @param user   a user name, can be null
     * @param pWord  a password, can be null
     * @param create a flag to indicate if the database should be created upon first connection
     * @return the created DataSource
     */
    public static DataSource createEmbeddedDerbyDataSource(String dbName, String user, String pWord,
                                                           boolean create) {
        Objects.requireNonNull(dbName, "The path name to the database must not be null");
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName(dbName);
        if (user != null)
            ds.setUser(user);
        if (pWord != null)
            ds.setPassword(pWord);
        if (create) {
            Path path = Paths.get(dbName);
            DatabaseIfc.LOGGER.info("Create option is on for {}", dbName);
            if (isEmbeddedDerbyDatabaseExists(path)) {
                DatabaseIfc.LOGGER.info("Database already exists at location {}", dbName);
                deleteEmbeddedDerbyDatabase(path);
            }
            ds.setCreateDatabase("create");
        }
        DatabaseIfc.LOGGER.info("Created an embedded Derby data source for {}", dbName);
        return ds;
    }

    /**
     * Sends a shutdown connection to the database.
     *
     * @param pathToDb a path to the database, must not be null
     * @return true if successfully shutdown
     */
    public static boolean shutDownEmbeddedDerbyDatabase(Path pathToDb) {
        return shutDownEmbeddedDerbyDatabase(pathToDb, null, null);
    }

    /**
     * Sends a shutdown connection to the database.
     *
     * @param pathToDb a path to the database, must not be null
     * @param user     a user name, can be null
     * @param pWord    a password, can be null
     * @return true if successfully shutdown
     */
    public static boolean shutDownEmbeddedDerbyDatabase(Path pathToDb, String user, String pWord) {
        DataSource dataSource = shutDownEmbeddedDerbyDataSource(pathToDb, user, pWord);
        try (Connection connection = dataSource.getConnection()) {
        } catch (SQLException e) {
            if ("08006".equals(e.getSQLState())) {
                DatabaseIfc.LOGGER.info("Derby shutdown succeeded. SQLState={}", e.getSQLState());
                return true;
            }
            DatabaseIfc.LOGGER.error("Derby shutdown failed", e);
        }
        return false;
    }

    /**
     * Creates a data source that can be used to shut down an embedded derby database upon
     * first connection.
     *
     * @param pathToDb a path to the database, must not be null
     * @return the created DataSource
     */
    public static DataSource shutDownEmbeddedDerbyDataSource(Path pathToDb) {
        return shutDownEmbeddedDerbyDataSource(pathToDb, null, null);
    }

    /**
     * Creates a data source that can be used to shut down an embedded derby database upon
     * first connection.
     *
     * @param pathToDb a path to the database, must not be null
     * @param user     a user name, can be null
     * @param pWord    a password, can be null
     * @return the created DataSource
     */
    public static DataSource shutDownEmbeddedDerbyDataSource(Path pathToDb, String user, String pWord) {
        return shutDownEmbeddedDerbyDataSource(pathToDb.toString(), user, pWord);
    }

    /**
     * Creates a data source that can be used to shut down an embedded derby database upon
     * first connection.
     *
     * @param dbName the path to the database, must not be null
     * @return the created DataSource
     */
    public static DataSource shutDownEmbeddedDerbyDataSource(String dbName) {
        return shutDownEmbeddedDerbyDataSource(dbName, null, null);
    }

    /**
     * Creates a data source that can be used to shut down an embedded derby database upon
     * first connection.
     *
     * @param dbName the path to the database, must not be null
     * @param user   a user name, can be null
     * @param pWord  a password, can be null
     * @return the created DataSource
     */
    public static DataSource shutDownEmbeddedDerbyDataSource(String dbName, String user, String pWord) {
        Objects.requireNonNull(dbName, "The path name to the database must not be null");
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName(dbName);
        if (user != null)
            ds.setUser(user);
        if (pWord != null)
            ds.setPassword(pWord);
        ds.setShutdownDatabase("shutdown");
        DatabaseIfc.LOGGER.info("Created an embedded Derby shutdown data source for {}", dbName);
        return ds;
    }

    /**
     * @param dbName the path to the database, must not be null
     * @param user   a user name, can be null
     * @param pWord  a password, can be null
     * @param create a flag to indicate if the database should be created upon first connection
     * @return the created DataSource
     */
    public static DataSource createClientDerbyDataSourceWithLocalHost(String dbName, String user, String pWord,
                                                                      boolean create) {
        Objects.requireNonNull(dbName, "The path name to the database must not be null");
        ClientDataSource ds = new ClientDataSource();
        ds.setDatabaseName(dbName);
        ds.setServerName("localhost");
        ds.setPortNumber(1527);
        if (user != null)
            ds.setUser(user);
        if (pWord != null)
            ds.setPassword(pWord);
        if (create) {
            ds.setCreateDatabase("create");
        }
        DatabaseIfc.LOGGER.info("Created a Derby client data source for {}", dbName);
        return ds;
    }

    /**
     * @param dbName the name of the database, must not be null
     * @param user   the user
     * @param pWord  the password
     * @return the DataSource for getting connections
     */
    public static DataSource getPostGresDataSourceWithLocalHost(String dbName, String user, String pWord) {
        return getPostGresDataSource("localhost", dbName, user, pWord);
    }

    /**
     * Assumes standard PostGres port
     *
     * @param dbServerName the name of the database server, must not be null
     * @param dbName       the name of the database, must not be null
     * @param user         the user
     * @param pWord        the password
     * @return the DataSource for getting connections
     */
    public static DataSource getPostGresDataSource(String dbServerName, String dbName, String user,
                                                   String pWord) {
        return getPostGresDataSource(dbServerName, dbName, user, pWord, 5432);
    }

    /**
     * @param dbServerName the name of the database server, must not be null
     * @param dbName       the name of the database, must not be null
     * @param user         the user
     * @param pWord        the password
     * @param portNumber   a valid port number
     * @return the DataSource for getting connections
     */
    public static DataSource getPostGresDataSource(String dbServerName, String dbName, String user,
                                                   String pWord, int portNumber) {
        Properties props = makePostGresProperties(dbServerName, dbName, user, pWord);
        return getDataSource(props);
    }

    /**
     * @param dbServerName the database server name, must not be null
     * @param dbName       the database name, must not be null
     * @param user         the user, must not be null
     * @param pWord        the password, must not be null
     * @return the Properties instance
     */
    public static Properties makePostGresProperties(String dbServerName, String dbName, String user, String pWord) {
        Objects.requireNonNull(dbServerName, "The name to the database server must not be null");
        Objects.requireNonNull(dbName, "The path name to the database must not be null");
        Objects.requireNonNull(user, "The user for the database must not be null");
        Objects.requireNonNull(pWord, "The password for the database must not be null");
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.user", user);
        props.setProperty("dataSource.password", pWord);
        props.setProperty("dataSource.databaseName", dbName);
        props.setProperty("dataSource.serverName", dbServerName);
        return props;
    }

    /**
     * Assumes that the properties are appropriately configured to create a DataSource
     * via  HikariCP
     *
     * @param properties the properties
     * @return a pooled connection DataSource
     */
    public static DataSource getDataSource(Properties properties) {
        Objects.requireNonNull(properties, "The properties must not be null");
        HikariConfig config = new HikariConfig(properties);
        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }

    /**
     * @param pathToPropertiesFile must not be null
     * @return a DataSource for making a database
     */
    public static DataSource getDataSource(Path pathToPropertiesFile) {
        Objects.requireNonNull(pathToPropertiesFile, "The properties file path must not be null");
        HikariConfig config = new HikariConfig(pathToPropertiesFile.toString());
        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }

    /**
     * Duplicates the database into a new database with the supplied name and directory.
     * Assumes that the source database has no active connections and performs a file system copy
     *
     * @param sourceDB  the path to the database that needs duplicating
     * @param dupName   the name of the duplicate database
     * @param directory the directory to place the database in
     * @throws IOException thrown if the system file copy commands fail
     */
    public static void copyEmbeddedDerbyDatabase(Path sourceDB, String dupName, Path directory) throws IOException {
        if (sourceDB == null) {
            throw new IllegalArgumentException("The path to the source must not be null!");
        }

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

        File target = directory.resolve(dupName).toFile();
        File source = sourceDB.toFile();
        JSLFileUtil.copyDirectory(source, target);
    }

    /**
     * Uses an active database connection and derby system commands to freeze the database,
     * uses system OS commands to copy the database, and then unfreezes the database.  The duplicate name
     * and directory path must not already exist
     *
     * @param ds        a DataSource to the embedded derby database, obviously it must point to the derby database
     * @param dupName   the name of the duplicate database, obviouisly it must reference the same database that is
     *                  referenced by the DataSource
     * @param directory the directory to place the database in
     * @throws SQLException thrown if the derby commands fail
     * @throws IOException  thrown if the system file copy commands fail
     */
    public static final void copyEmbeddedDerbyDatabase(DataSource ds, Path sourceDB, String dupName, Path directory)
            throws SQLException, IOException {
        if (ds == null) {
            throw new IllegalArgumentException("The data source must not be null");
        }

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

        Statement s = ds.getConnection().createStatement();
        // freeze the database
        s.executeUpdate("CALL SYSCS_UTIL.SYSCS_FREEZE_DATABASE()");
        //copy the database directory during this interval
        // translate paths to files
        File target = directory.resolve(dupName).toFile();
        File source = sourceDB.toFile();
        JSLFileUtil.copyDirectory(source, target);
        s.executeUpdate("CALL SYSCS_UTIL.SYSCS_UNFREEZE_DATABASE()");
        s.close();
    }

    /**
     * There is no way to guarantee with 100 percent certainty that the path
     * is in fact a embedded derby database because someone could be faking
     * the directory structure.  The database directory of an embedded derby database
     * must have a service.properties file, a log directory, and a seg0 directory.
     * If these exist and the supplied path is a directory, then the method
     * returns true.
     *
     * @param path the path to check, must not be null
     * @return true if it could be an embedded derby database
     */
    public static boolean isEmbeddedDerbyDatabase(Path path) {
        Objects.requireNonNull(path, "The supplied path was null");
        // the path itself must be a directory not a file
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            // not a directory cannot be an embedded derby database
            return false;
        }
        // the path is a directory, could be a embedded derby database
        // it must have log directory, a seg0 directory and a services.properties file
        Path log = path.resolve("log");
        // log must exist and be a directory
        if (!Files.isDirectory(log, LinkOption.NOFOLLOW_LINKS)) {
            // no log directory, can't be embedded derby db
            return false;
        }
        // seq0 must exist and be a directory
        Path seg0 = path.resolve("seg0");
        if (!Files.isDirectory(seg0, LinkOption.NOFOLLOW_LINKS)) {
            // no seg0 directory, can't be embedded derby db
            return false;
        }
        // service.properties must exist and be a file
        Path sp = path.resolve("service.properties");
        if (!Files.isRegularFile(sp, LinkOption.NOFOLLOW_LINKS)) {
            // no service.properties file, can't be embedded derby db
            return false;
        }
        // likely to be be embedded derby db
        return true;
    }

    /**
     * A convenience method for those that use File instead of Path.
     * Calls isEmbeddedDerbyDatabase(file.toPath())
     *
     * @param file the file to check, must not be null
     * @return true if it could be an embedded derby database
     */
    public static boolean isEmbeddedDerbyDatabase(File file) {
        Objects.requireNonNull(file, "The supplied file was null");
        return isEmbeddedDerbyDatabase(file.toPath());
    }

    /**
     * Checks if a file is a valid SQLite database
     * Strategy:
     * - path must reference a regular file
     * - if file exists, check if it is larger than 100 bytes (SQLite header size)
     * - then check if a database operation works
     *
     * @param pathToFile the path to the database file, must not be null
     * @return true if the path points to a valid SQLite database file
     */
    public static boolean isSQLiteDatabase(Path pathToFile) {
        Objects.requireNonNull(pathToFile, "The supplied path was null");
        // the path itself must be a directory or a file, i.e. it must exist
        if (!Files.exists(pathToFile)) {
            return false;
        }
        // now the thing exists, check if it is a regular file
        if (!Files.isRegularFile(pathToFile, LinkOption.NOFOLLOW_LINKS)) {
            // if it is not a regular file, then it cannot be an SQLite database
            return false;
        }
        // it must be a regular file, need to check its size
        // now try a database operation specific to SQLite
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setDatabaseName(pathToFile.toString());
        ds.setReadOnly(true);
        try {
            SQLiteConnection sqLiteConnection = ds.getConnection("", "");
            sqLiteConnection.libversion();
            sqLiteConnection.close();
        } catch (SQLException exception) {
            return false;
        }
        return true;
    }

    /**
     * Deletes a SQLite database
     * Strategy:
     * - simply deletes the file at the end of the path
     * - it may or not be a valid SQLiteDatabase
     *
     * @param pathToDb the path to the database file, must not be null
     */
    public static void deleteSQLiteDatabase(Path pathToDb) {
        Objects.requireNonNull(pathToDb, "The supplied path was null");
        try {
            Files.deleteIfExists(pathToDb);
            DatabaseIfc.LOGGER.info("Deleting SQLite database {}", pathToDb);
        } catch (IOException e) {
            DatabaseIfc.LOGGER.error("Unable to delete SQLite database {}", pathToDb);
            throw new DataAccessException("Unable to delete SQLite database" + pathToDb.toString());
        }
    }

    /**
     * @param pathToDb the path to the database file, must not be null
     * @return the data source
     */
    public static SQLiteDataSource createSQLiteDataSource(Path pathToDb) {
        Objects.requireNonNull(pathToDb, "The supplied path was null");
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setDatabaseName(pathToDb.toString());
        // there is a bug in the SQLiteDataSource class, it will not work without setting the URL
        // even though the name of the database has been set.
        ds.setUrl("jdbc:sqlite:" + pathToDb);
        ds.setConfig(createDefaultSQLiteConfiguration());
        DatabaseIfc.LOGGER.info("Created SQLite data source {}", pathToDb);
        return ds;
    }

    /** Creates a recommended non-read only configuration that has been tested for
     *  performance.  https://ericdraken.com/sqlite-performance-testing/
     *
     * @return the configuration
     */
    public static SQLiteConfig createDefaultSQLiteConfiguration(){
        return createDefaultSQLiteConfiguration(false);
    }

    /** Creates a recommended configuration that has been tested for
     *  performance.  https://ericdraken.com/sqlite-performance-testing/
     *
     * @param readOnly indicates read only mode
     * @return the configuration
     */
    public static SQLiteConfig createDefaultSQLiteConfiguration(boolean readOnly) {
        SQLiteConfig config = new SQLiteConfig();
        final int cacheSize = 1000;
        final int pageSize = 8192;
        config.setReadOnly(readOnly);
        config.setTempStore(SQLiteConfig.TempStore.MEMORY);  // Hold indices in memory
        config.setCacheSize(cacheSize);
        config.setPageSize(pageSize);
        config.setJounalSizeLimit(cacheSize * pageSize);
        config.setOpenMode(SQLiteOpenMode.NOMUTEX);
        config.setLockingMode(SQLiteConfig.LockingMode.NORMAL);
        config.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
        config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
        // If read-only, then use the existing journal, if any
        //TODO removed to prevent creation of shm and wal files
//        if (!readOnly) {
//            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
//        }
        return config;
    }

    /**
     * @param dbLabel    a label for the database
     * @param dataSource the data source for connections
     * @return the created database
     */
    public static DatabaseIfc createSQLiteDatabase(String dbLabel, SQLiteDataSource dataSource) {
        Objects.requireNonNull(dbLabel, "The database label was null");
        Objects.requireNonNull(dataSource, "The data source was null");
        return new Database(dbLabel, dataSource, SQLDialect.SQLITE);
    }

    /**
     * If the database already exists it is deleted
     *
     * @param dbName the name of the SQLite database. Must not be null
     * @param dbDir  a path to the directory to hold the database. Must not be null
     * @return the created database
     */
    public static DatabaseIfc createSQLiteDatabase(String dbName, Path dbDir) {
        Objects.requireNonNull(dbName, "The database name was null");
        Objects.requireNonNull(dbDir, "The path to the database must not be null");
        Path pathToDb = dbDir.resolve(dbName);
        // if it exists, delete it
        if (Files.exists(pathToDb)) {
            deleteSQLiteDatabase(pathToDb);
        }
        SQLiteDataSource ds = createSQLiteDataSource(pathToDb);
        return createSQLiteDatabase(dbName, ds);
    }

    /**
     * If the database already exists it is deleted. Created within JSLDatabase.dbDir
     *
     * @param dbName the name of the SQLite database. Must not be null
     * @return the created database
     */
    public static DatabaseIfc createSQLiteDatabase(String dbName) {
        return createSQLiteDatabase(dbName, JSLDatabase.dbDir);
    }

    /**
     * The database file must already exist at the path
     *
     * @param pathToDb the path to the database file, must not be null
     * @param readOnly true indicates that the database is read only
     * @return the database
     */
    public static DatabaseIfc getSQLiteDatabase(Path pathToDb, boolean readOnly) {
        Objects.requireNonNull(pathToDb, "The path to the database file was null");
        if (!isSQLiteDatabase(pathToDb)) {
            throw new IllegalStateException("The path does represent a valid SQLite database " + pathToDb);
        }
        // must exist and be at path
        SQLiteDataSource dataSource = createSQLiteDataSource(pathToDb);
        dataSource.setReadOnly(readOnly);
        return new Database(pathToDb.getFileName().toString(), dataSource, SQLDialect.SQLITE);
    }

    /**
     * The database file must already exist at the path. It is opened for reading and writing
     *
     * @param pathToDb the path to the database file, must not be null
     * @return the database
     */
    public static DatabaseIfc getSQLiteDatabase(Path pathToDb) {
        return getSQLiteDatabase(pathToDb, false);
    }

    /**
     * The database file must already exist within the JSLDatabase.dbDir directory
     * It is opened for reading and writing.
     *
     * @param fileName the name of database file, must not be null
     * @return the database
     */
    public static DatabaseIfc getSQLiteDatabase(String fileName) {
        Objects.requireNonNull(fileName, "The file name to the database was null");
        return getSQLiteDatabase(JSLDatabase.dbDir.resolve(fileName));
    }
}
