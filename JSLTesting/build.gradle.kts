plugins {
    // java
    `java-library`
}

version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {

	api(project(":JSLCore"))
	api(project(":JSLExamples"))
	api(project(":JSLExtensions"))

// https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.5.1")

//    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    //sourceCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_11
}

//TODO had to switch to IntelliJ for build for test to work
// https://stackoverflow.com/questions/30474767/no-tests-found-for-given-includes-error-when-running-parameterized-unit-test-in
tasks.test {
    useJUnitPlatform()
}

