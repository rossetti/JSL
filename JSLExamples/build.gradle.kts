// JSLExamples

plugins {
    // java
    `java-library`
    // uncomment for publishing task
//    `maven-publish`
}

version = "1.0-SNAPSHOT"

//uncomment for publishing task

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
//        val mavenPublication = create<MavenPublication>("jslexamples") {
//            groupId = "edu.uark.jsl"
//            artifactId = "JSLExamples"
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

dependencies {

	api(project(":JSLCore"))
	api(project(":JSLExtensions"))

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
//    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
//    compile group: 'org.slf4j', name: 'slf4j-simple', version: '1.7.30'
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
//    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-core
//    implementation(group = "ch.qos.logback", name = "logback-core", version = "1.2.3")

}

configure<JavaPluginConvention> {
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
