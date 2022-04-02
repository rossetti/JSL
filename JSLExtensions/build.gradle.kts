// JSLExtensions

plugins {
    // java
    `java-library`
    // uncomment for publishing task
    `maven-publish`
    // uncomment for signing the jars during publishing task
    signing
    // necessary because some classes use javafx
    id("org.openjfx.javafxplugin") version "0.0.8"
}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.addAll(listOf("-Xlint:unchecked"))
}

// comment out to not make snapshot
//version = "1.0-SNAPSHOT"

group = "io.github.rossetti"
version = "R1.0.11"

repositories {
    //jcenter()
    mavenCentral()
}

javafx {
    version = "12"
    modules("javafx.controls", "javafx.fxml")
    configuration = "api"
}

dependencies {

    // depends on JSLCore as an internal project in multi-project build
    // this permits changes to the JSLCore to be immediately reflected in JSLExtensions
	api(project(":JSLCore"))

    api(group = "com.opencsv", name = "opencsv", version = "5.5.2")

    api(group = "org.apache.commons", name = "commons-math3", version = "3.6.1")

//    api(group = "commons-io", name = "commons-io", version = "2.11.0")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-core
    api(group = "tech.tablesaw", name = "tablesaw-core", version =  "0.42.0")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-jsplot
    api(group = "tech.tablesaw", name = "tablesaw-jsplot", version = "0.42.0")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-excel
    // no documentation available
//    api(group = "tech.tablesaw", name = "tablesaw-excel", version = "0.38.1")

    //https://github.com/knowm/XChart
//    api(group ="org.knowm.xchart", name = "xchart", version = "3.8.0")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-smile
//    api(group = "tech.tablesaw", name = "tablesaw-smile", version = "0.42.0")

    // https://github.com/d3xsystems/d3x-morpheus
//    api(group = "com.d3xsystems", name = "d3x-morpheus-core", version = "1.0.31")
//    api(group = "com.d3xsystems", name = "d3x-morpheus-viz", version = "1.0.31")
//    api(group = "com.d3xsystems", name = "d3x-morpheus-db", version = "1.0.31")
//    api(group = "com.d3xsystems", name = "d3x-morpheus-excel", version = "1.0.31")

    api(group = "com.google.guava", name = "guava", version = "31.0.1-jre")

    // https://db.apache.org/derby/releases/release-10.15.1.3.cgi#New+Features
    implementation(group = "org.apache.derby", name = "derby", version = "10.15.2.0")
    implementation(group = "org.apache.derby", name = "derbyshared", version = "10.15.2.0")
    implementation(group = "org.apache.derby", name = "derbyclient", version = "10.15.2.0")
    implementation(group = "org.apache.derby", name = "derbytools", version = "10.15.2.0")

    implementation(group = "org.postgresql", name = "postgresql", version = "42.3.1")
    
    implementation(group = "org.xerial", name = "sqlite-jdbc", version = "3.36.0.3")

    implementation(group = "com.zaxxer", name = "HikariCP", version = "5.0.1")

    // https://mvnrepository.com/artifact/org.jooq/jooq
    api(group = "org.jooq", name = "jooq", version = "3.16.1")
    // https://mvnrepository.com/artifact/org.jooq/jooq-meta
    api(group = "org.jooq", name = "jooq-meta", version = "3.16.1")
    // https://mvnrepository.com/artifact/org.jooq/jooq-codegen
    api(group = "org.jooq", name = "jooq-codegen", version = "3.16.1")
    // this is to use jooq code generation from script
    runtimeOnly(group = "org.jooq", name = "jooq-meta-extensions", version = "3.16.1")

    // https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
//    api(group = "javax.xml.bind", name = "jaxb-api", version = "2.3.1")

    // https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-runtime
//    api(group = "org.glassfish.jaxb", name = "jaxb-runtime", version = "2.3.1")

    // https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api
    //https://github.com/jOOQ/jOOQ/issues/7565
//    api( group= "javax.annotation", name = "javax.annotation-api", version = "1.3.2")

    //TODO work to update version of POI or consider using https://github.com/dhatim/fastexcel/ instead
    // https://mvnrepository.com/artifact/org.apache.poi/poi
    api(group = "org.apache.poi", name = "poi", version = "5.2.0")
    // https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
    api(group = "org.apache.poi", name = "poi-ooxml", version = "5.2.0")

    // include local jar files found in libs folder in the compile
    implementation(fileTree(baseDir = "libs"))

}

// TODO, need to understand why this is here
configure<JavaPluginConvention> {
    //sourceCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_11
}

// this is supposed to exclude the logback.xml resource file from the generated jar
// this is good because user can then provide their own logging specification
// TODO need reference to why this is good
tasks.jar {
//    manifest {
//        attributes(
//                "Implementation-Title" to project.name,
//                "Implementation-Version" to project.version
//        )
//    }
    exclude("logback.xml")
}

// these extensions are needed when publishing to maven
// because maven requires javadoc jar, sources jar, and the build jar
// these jars are placed in build/libs by default
java {
    // comment this out to not make jar file with javadocs during normal build
    withJavadocJar()
    // comment this out to not make jar file with source during normal build
    withSourcesJar()
}

// run the publishing task to generate the signed jars required for maven central
// jars will be found in build/JSL/releases or build/JSL/snapshots
publishing {
    publications {
        create<MavenPublication>("JSLExtensions") {
            groupId = "io.github.rossetti"
            artifactId = "JSLExtensions"
            // update this field when making a new release
            version = "R1.0.11"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("JSLExtensions")
                description.set("The JSL, an open source library for simulation")
                url.set("https://github.com/rossetti/JSL")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("rossetti")
                        name.set("Manuel D. Rossetti")
                        email.set("rossetti@uark.edu")
                    }
                }
                scm {
                    connection.set("https://github.com/rossetti/JSL.git")
                    developerConnection.set("git@github.com:rossetti/JSL.git")
                    url.set("https://github.com/rossetti/JSL")
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            // this publishes to local folder within build directory
            // avoids having to log into maven, etc, but requires manual upload of releases
            val releasesRepoUrl = uri(layout.buildDirectory.dir("JSL/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("JSL/snapshots"))
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

// signing requires config information user home directory
// .gradle/gradle.properties. To publish jars without signing, just comment out
signing {
    sign(publishing.publications["JSLExtensions"])
}
