package jslx.dbutilities.dbutil;

import jsl.utilities.reporting.JSL;
import jslx.dbutilities.JSLDatabase;
import org.jooq.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DatabaseFactoryTest {

    public static void main(String[] args) throws IOException {

        testSQLite();
//        testSQLite2();
        // testDerbyLocalHost();
        // testDataSourceConnection();
        // testParsing();
        //     testDatabaseCreation();
        //testDerbyEmbeddedExisting();
        //       testExcelDbExport();
//              testExcelDbImport();

        //       metaDataTest();

        // testPostgresLocalHost();
        //testPostgresLocalHostJSLDb();

//        testEmbeddedDerbyPropertiesFile();

//        testPostgresPropertiesFile();

//        testSPDatabaseCreation();
//        testDatabaseCreation();
    }

    public static void testSQLite(){
//        Path dbPath = JSLDatabase.dbDir.resolve("sql-murder-mystery.db");
//        Path dbPath = JSLDatabase.dbDir.resolve("empty.db");
        //boolean test = DatabaseFactory.isSQLiteDatabase(dbPath);
        //System.out.println("Is dbPath a SQLite database? " + test);
        DatabaseIfc database = DatabaseFactory.createSQLiteDatabase("someDB.db");
        database.executeCommand("drop table if exists person");
        database.executeCommand("create table person (id integer, name string)");
        List<String> allTableNames = database.getAllTableNames();
        for(String s: allTableNames){
            System.out.println("Table: " + s);
        }
        database.executeCommand("insert into person values(1, 'PersonA')");
        database.executeCommand("insert into person values(2, 'PersonB')");
        database.printTableAsText("person");
        System.out.println("Done!");
    }

    public static void testSQLite2(){
        DatabaseIfc database = DatabaseFactory.getSQLiteDatabase("someDB.db");
        database.printTableAsText("person");
        System.out.println("Done!");
    }

    public static void testDatabaseCreation(){
        Path path = Paths.get("/Users/rossetti/Documents/Development/Temp");
        String name = "manuel";
        DatabaseIfc database = DatabaseFactory.createEmbeddedDerbyDatabase(name, path);
        database.getDatabaseMetaData();
    }

    public static void testDataSourceConnection() {
        Path path = JSLDatabase.dbDir.resolve("JSLDb_DLB_with_Q");
        String name = path.toAbsolutePath().toString();
//        DataSource dataSource = DataSourceFactory.createClientDerbyDataSourceWithLocalHost(name,
//                null, null, false);
        DataSource dataSource = DatabaseFactory.createEmbeddedDerbyDataSource(name,
                null, null, false);
        try {
            dataSource.getConnection();
            System.out.println("Connection established.");
        } catch (SQLException e) {
            System.out.println("Could not establish a connection.");
        }

    }

    public static void testEmbeddedDerbyPropertiesFile(){
        Path path = JSLDatabase.dbScriptsDir.resolve("embeddedDerbyDB.properties");

        DataSource dataSource = DatabaseFactory.getDataSource(path);

        System.out.println(dataSource);

        DatabaseIfc testDb = new Database("SPDatabase", dataSource, SQLDialect.DERBY);

        testDb.printAllTablesAsText("APP");
    }

    public static void testPostgresPropertiesFile(){
        Path path = JSLDatabase.dbScriptsDir.resolve("postgresDB.properties");

        DataSource dataSource = DatabaseFactory.getDataSource(path);

        System.out.println(dataSource);

        DatabaseIfc testDb = new Database("SPDatabase", dataSource, SQLDialect.POSTGRES);

        testDb.printAllTablesAsText("public");
    }


    public static void testDerbyLocalHost() {
        //note the server must be started for this to work
        Path path = JSLDatabase.dbDir.resolve("JSLDb_DLB_with_Q");
        String name = path.toAbsolutePath().toString();
        DataSource dataSource = DatabaseFactory.createClientDerbyDataSourceWithLocalHost(name,
                null, null, false);
        Database db = new Database("JSL", dataSource, SQLDialect.DERBY);
        db.setJooQDefaultExecutionLoggingOption(true);

        List<String> jsl_db = db.getTableNames("JSL_DB");

        for (String s : jsl_db) {
            System.out.println(s);
        }
        System.out.println();
        db.printTableAsText("ACROSS_REP_STAT");
    }

    public static void testPostgresLocalHost() {
        String dbName = "test";
        String user = "test";
        String pw = "test";
        DataSource dataSource = DatabaseFactory.getPostGresDataSourceWithLocalHost(dbName, user, pw);
        // make the database
        Database db = new Database(dbName, dataSource, SQLDialect.POSTGRES);
        // builder the creation task
        Path pathToCreationScript = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Postgres.sql");
        DbCreateTask task = db.create().withCreationScript(pathToCreationScript).execute();
        System.out.println(task);
        task.getCreationScriptCommands().forEach(System.out::println);
        db.printTableAsText("s");
    }

    public static void testPostgresLocalHostJSLDb() {
        String dbName = "test";
        String user = "test";
        String pw = "test";
        DataSource dataSource = DatabaseFactory.getPostGresDataSourceWithLocalHost(dbName, user, pw);
        // make the database
        Database db = new Database(dbName, dataSource, SQLDialect.POSTGRES);
        db.executeCommand("DROP SCHEMA IF EXISTS jsl_db CASCADE");
        // builder the creation task
        Path pathToCreationScript = JSLDatabase.dbScriptsDir.resolve("JSLDb.sql");
        DbCreateTask task = db.create().withCreationScript(pathToCreationScript).execute();
        System.out.println(task);
        task.getCreationScriptCommands().forEach(System.out::println);
        db.printTableAsText("simulation_run");
    }

    public static void testDerbyEmbeddedWithCreateScript() throws IOException {
        Path path = JSLDatabase.dbDir.resolve("TmpDb");
        Path pathToCreationScript = JSLDatabase.dbScriptsDir.resolve("JSLDb.sql");
        DataSource dataSource = DatabaseFactory.createEmbeddedDerbyDataSource(path, true);
        Database db = new Database("TmpDb", dataSource, SQLDialect.DERBY);
        db.executeScript(pathToCreationScript);
        List<String> jsl_db = db.getTableNames("JSL_DB");

        for (String s : jsl_db) {
            System.out.println(s);
        }
    }

    public static void testDerbyEmbeddedExisting() {
        Path path = JSLDatabase.dbDir.resolve("JSLDb_DLB_with_Q");
        DatabaseIfc db = DatabaseFactory.getEmbeddedDerbyDatabase("JSLDb_DLB_with_Q");
//        DataSource dataSource = DatabaseFactory.createEmbeddedDerbyDataSource(path);
//        Database db = new Database("JSL", dataSource, SQLDialect.DERBY);
        db.setJooQDefaultExecutionLoggingOption(true);

        List<String> jsl_db = db.getTableNames("JSL_DB");

        for (String s : jsl_db) {
            System.out.println(s);
        }
        db.printTableAsText("ACROSS_REP_STAT");
    }

    public static void testParsing() throws IOException {
        Path path = JSLDatabase.dbDir.resolve("TmpDb2");
        Path pathToCreationScript = JSLDatabase.dbScriptsDir.resolve("JSLDb.sql");
        DataSource dataSource = DatabaseFactory.createEmbeddedDerbyDataSource(path, true);
        Database db = new Database("TmpDb2", dataSource, SQLDialect.DERBY);
        List<String> lines = DatabaseIfc.parseQueriesInSQLScript(pathToCreationScript);
        //List<String> lines = Files.readAllLines(pathToCreationScript, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (String s : lines) {
            sb.append(s).append(";").append(System.lineSeparator());
        }
//        System.out.println(sb);
        Queries queries = db.getDSLContext().parser().parse(sb.toString());
        Iterator<Query> iterator = queries.iterator();

        int i = 1;
        for (String s : lines) {
            System.out.println("Line " + i);
            System.out.print("Original:    ");
            System.out.println(s);
            Query query = iterator.next();
            //System.out.println();
            System.out.print("Jooq Parser: ");
            System.out.println(query.getSQL());
            System.out.println();
            i++;
        }
//        while(iterator.hasNext()){
//            Query query = iterator.next();
//            System.out.println(query.getSQL());
//            System.out.println("Executing query");
//            query.execute();
//        }

        //System.out.println("Executing batch of queries");
        // queries.executeBatch();
    }

    public static void testSPDatabaseCreation() {
        Path path = JSLDatabase.dbDir.resolve("TestCreationTaskDb");
        String name = path.toAbsolutePath().toString();
        // just create it
        DataSource dataSource = DatabaseFactory.createEmbeddedDerbyDataSource(name, true);
        // make the database
        Database db = new Database(name, dataSource, SQLDialect.DERBY);
        // builder the creation task
        //Path pathToCreationScript = JSLDatabase.dbScriptsDir.resolve("JSLDb.sql");

        Path tables = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Tables.sql");
        Path inserts = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Insert.sql");
        Path alters = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Alter.sql");

        DbCreateTask task = db.create()
                .withTables(tables).withInserts(inserts).withConstraints(alters)
                .execute();
        System.out.println(task);

        task.getCreationScriptCommands().forEach(System.out::println);
        task.getInsertCommands().forEach(System.out::println);
        task.getAlterCommands().forEach(System.out::println);

        System.out.println(task);
    }

    public static void testExcelDbExport() throws IOException {
        String dbName = "SP";
        // make the database
        DatabaseIfc db = DatabaseFactory.createEmbeddedDerbyDatabase(dbName);
        // builder the creation task
        Path tables = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Tables.sql");
        Path inserts = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Insert.sql");
        Path alters = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Alter.sql");
        DbCreateTask task = db.create().withTables(tables).withInserts(inserts).withConstraints(alters)
                .execute();

        System.out.println(task);
        db.writeDbToExcelWorkbook("APP");
    }

    public static void testExcelDbImport() throws IOException {
        String dbName = "SPViaExcel";
        // make the database
        DatabaseIfc db = DatabaseFactory.createEmbeddedDerbyDatabase(dbName);

        // builder the creation task
        Path tables = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Tables.sql");
        Path inserts = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Insert.sql");
        Path alters = JSLDatabase.dbScriptsDir.resolve("SPDatabase_Alter.sql");

        Path wbPath = JSL.getInstance().getExcelDir().resolve("SP.xlsx");

        db.create().withTables(tables)
                .withExcelData(wbPath, Arrays.asList("S", "P", "SP"))
                .withConstraints(alters)
                .execute();

        db.printAllTablesAsText("APP");
    }

    public static void metaDataTest() {

        DatabaseIfc sp = DatabaseFactory.getEmbeddedDerbyDatabase("SP");
        Meta meta = sp.getDSLContext().meta();

        System.out.println("The catalogs are:");
        List<Catalog> catalogs = meta.getCatalogs();
        for (Catalog c : catalogs) {
            System.out.println(c);
        }
        List<Schema> schemas = meta.getSchemas();
        System.out.println(schemas);

    }

}
