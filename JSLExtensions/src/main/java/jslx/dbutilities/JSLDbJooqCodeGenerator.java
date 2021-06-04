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

package jslx.dbutilities;

import jslx.dbutilities.dbutil.DatabaseFactory;
import jslx.dbutilities.dbutil.Database;
import jslx.dbutilities.dbutil.DatabaseIfc;
import org.apache.commons.io.FileUtils;
import org.jooq.SQLDialect;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSLDbJooqCodeGenerator {

    public static void main(String[] args) throws IOException {

//        makeEmptyDb("JSLDbCodeGen", "JSLDb.sql");

        runCodeGenerationUsingEmptyDb();

    }

    public static DatabaseIfc makeEmptyDb(String dbName, String scriptName) throws IOException {
        System.out.println("Making database: " + dbName);

        FileUtils.deleteDirectory(JSLDatabase.dbDir.resolve(dbName).toFile());

        Path createScript = JSLDatabase.dbScriptsDir.resolve(scriptName);
        Path dbPath = JSLDatabase.dbDir.resolve(dbName);
        DataSource derbyDataSource = DatabaseFactory.createEmbeddedDerbyDataSource(dbPath, true);
        Database db = new Database(dbName, derbyDataSource, SQLDialect.DERBY);
        db.create().withCreationScript(createScript).execute();
        System.out.println("Created database: " + dbName);
        return db;
    }

    public static void runCodeGenerationUsingEmptyDb() throws IOException {
        // make the database
        String dbName = "tmpJSLDb";
        System.out.println("Making database: " + dbName);
        Path dbPath = JSLDatabase.dbDir.resolve(dbName);
        FileUtils.deleteDirectory(dbPath.toFile());
       // Path createScript = Paths.get("src").resolve("main").resolve("resources").resolve("JSLDb.sql");
        Path createScript = Paths.get("src").resolve("main").resolve("resources").resolve("JSLDb.sql");
        DataSource derbyDataSource = DatabaseFactory.createEmbeddedDerbyDataSource(dbPath, true);
        Database db = new Database(dbName, derbyDataSource, SQLDialect.DERBY);
        db.create().withCreationScript(createScript).execute();
        System.out.println("Created database: " + dbPath);
        System.out.println("Running code generation.");
        DatabaseIfc.runJooQCodeGenerationDerbyDatabase(db.getDataSource(), "JSL_DB",
                "src/main/java", "jslx.dbutilities.jsldbsrc");
        System.out.println("Completed code generation.");
        System.out.println("Deleting the database");
        FileUtils.deleteDirectory(dbPath.toFile());
    }

//    public static void runCodeGenerationUsingScriptOnly() throws Exception {
//        Path createScript = Paths.get("src").resolve("main").resolve("resources").resolve("JSLDb.sql");
//        System.out.println("Running code generation.");
//        DatabaseIfc.runJooQCodeGeneration(createScript, "JSL_DB",
//                "src/main/java", "jslx.dbutilities.jsldbsrc");
//        System.out.println("Completed code generation.");
//    }
}
