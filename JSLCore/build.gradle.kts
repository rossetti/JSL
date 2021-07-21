// JSLCore

plugins {
    // java
    `java-library`
    // uncomment for publishing task
    `maven-publish`
    // uncomment for signing the jars during publishing task
    signing
}

// commented to not make snapshot
//version = "1.0-SNAPSHOT"
group = "io.github.rossetti"
version = "R1.0.7"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {

    //https://github.com/google/gson
    api(group = "com.google.code.gson", name = "gson", version = "2.8.6")
	// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    api(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
//    compile group: 'org.slf4j', name: 'slf4j-simple', version: '1.7.30'
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    api(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-core
    api(group = "ch.qos.logback", name = "logback-core", version = "1.2.3")

}

configure<JavaPluginConvention> {
//    sourceCompatibility = JavaVersion.VERSION_1_8
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

// these extensions are needed when publishing to maven
// because maven requires javadoc jar, sources jar, and the build jar
// these jars are placed in build/libs by default
java {
    // comment this out to not make jar file with javadocs during normal build
//    withJavadocJar()
    // comment this out to not make jar file with source during normal build
//    withSourcesJar()
}

// run the publishing task to generate the signed jars required for maven central
// jars will be found in build/JSL/releases or build/JSL/snapshots
publishing {
    publications {
        create<MavenPublication>("JSLCore") {
            groupId = "io.github.rossetti"
            artifactId = "JSLCore"
            // update this field when generating new release
            version = "R1.0.7"
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
                name.set("JSLCore")
                description.set("The JSL, an open source library for simulation")
                url.set("https://github.com/rossetti/JSL")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

// signing requires config information in folder user home directory
// .gradle/gradle.properties. To publish jars without signing, just comment out
signing {
    sign(publishing.publications["JSLCore"])
}
