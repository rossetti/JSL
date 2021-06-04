// JSLExtensions

plugins {
    // java
    `java-library`
    // uncomment for publishing task
//    `maven-publish`
    id("org.openjfx.javafxplugin") version "0.0.8"
}

version = "1.0-SNAPSHOT"

// uncomment for publishing task

task<Jar>("sourcesJar") {
    classifier = "sources"
    from(sourceSets.main.get().allJava)
}

task<Jar>("javadocJar") {
    classifier = "javadoc"
    from(tasks.javadoc.get().destinationDir)
}

//match properties in ~/.gradle/gradle.properties, stored in $GRADLE_USER_HOME/.gradle/gradle.properties
//val uaArchivaUser: String by project
//val uaArchivaPassword: String by project
//
//publishing {
//    repositories {
//        maven {
//            credentials {
//                username = uaArchivaUser
//                password = uaArchivaPassword
//            }
//            setUrl("https://archiva.uark.edu/repository/jsl")
//            metadataSources {
//                gradleMetadata()
//            }
//        }
//    }
//    publications {
//        val mavenPublication = create<MavenPublication>("jslextensions") {
//            groupId = "edu.uark.jsl"
//            artifactId = "JSLExtensions"
//            version = "R1.0.1"
//            from(components["java"])
//            artifact(tasks["sourcesJar"])
//            artifact(tasks["javadocJar"])
//        }
//    }
//}

repositories {
    jcenter()
    mavenCentral()
}

javafx {
    version = "12"
    modules("javafx.controls", "javafx.fxml")
    configuration = "api"
}

dependencies {

	api(project(":JSLCore"))

    api(group = "com.opencsv", name = "opencsv", version = "5.2")

    api(group = "org.apache.commons", name = "commons-math3", version = "3.6.1")

    api(group = "commons-io", name = "commons-io", version = "2.7")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-core
    api(group = "tech.tablesaw", name = "tablesaw-core", version =  "0.38.1")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-jsplot
    api(group = "tech.tablesaw", name = "tablesaw-jsplot", version = "0.38.1")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-excel
    // no documentation available
//    api(group = "tech.tablesaw", name = "tablesaw-excel", version = "0.38.1")

    //https://github.com/knowm/XChart
//    api(group ="org.knowm.xchart", name = "xchart", version = "3.8.0")

    // https://mvnrepository.com/artifact/tech.tablesaw/tablesaw-smile
    api(group = "tech.tablesaw", name = "tablesaw-smile", version = "0.32.1")

    // https://github.com/d3xsystems/d3x-morpheus
//    api(group = "com.d3xsystems", name = "d3x-morpheus-core", version = "1.0.31")
//    api(group = "com.d3xsystems", name = "d3x-morpheus-viz", version = "1.0.31")
//    api(group = "com.d3xsystems", name = "d3x-morpheus-db", version = "1.0.31")
//    api(group = "com.d3xsystems", name = "d3x-morpheus-excel", version = "1.0.31")

    api(group = "com.google.guava", name = "guava", version = "29.0-jre")

    // https://db.apache.org/derby/releases/release-10.15.1.3.cgi#New+Features
    implementation(group = "org.apache.derby", name = "derby", version = "10.15.1.3")
    implementation(group = "org.apache.derby", name = "derbyshared", version = "10.15.1.3")
    implementation(group = "org.apache.derby", name = "derbyclient", version = "10.15.1.3")
    implementation(group = "org.apache.derby", name = "derbytools", version = "10.15.1.3")

    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.2")

    implementation(group = "com.zaxxer", name = "HikariCP", version = "3.4.5")

    // https://mvnrepository.com/artifact/org.jooq/jooq
    //api(group = "org.jooq", name = "jooq", version = "3.12.3")
    api(group = "org.jooq", name = "jooq", version = "3.14.11")
    // https://mvnrepository.com/artifact/org.jooq/jooq-meta
    api(group = "org.jooq", name = "jooq-meta", version = "3.14.11")
    // https://mvnrepository.com/artifact/org.jooq/jooq-codegen
    api(group = "org.jooq", name = "jooq-codegen", version = "3.14.11")
    // this is to use jooq code generation from script
    runtime(group = "org.jooq", name = "jooq-meta-extensions", version = "3.14.11")

    // https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
    api(group = "javax.xml.bind", name = "jaxb-api", version = "2.3.1")

    // https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-runtime
    api(group = "org.glassfish.jaxb", name = "jaxb-runtime", version = "2.3.1")

    // https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api
    //https://github.com/jOOQ/jOOQ/issues/7565
    api( group= "javax.annotation", name = "javax.annotation-api", version = "1.3.2")

    // https://mvnrepository.com/artifact/org.apache.poi/poi
    api(group = "org.apache.poi", name = "poi", version = "4.1.2")
    // https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
    api(group = "org.apache.poi", name = "poi-ooxml", version = "4.1.2")

    // include local jar files found in libs folder in the compile
    implementation(fileTree(baseDir = "libs"))

}

configure<JavaPluginConvention> {
    //sourceCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.jar {
//    manifest {
//        attributes(
//                "Implementation-Title" to project.name,
//                "Implementation-Version" to project.version
//        )
//    }
    exclude("logback.xml")
}
