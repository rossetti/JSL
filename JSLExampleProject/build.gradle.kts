plugins {
    `java-library`
}

//group = "org.example"
//version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // next two lines allows use of JSL libraries within the project
    // update the release number when new releases become available
    api(group = "io.github.rossetti", name = "JSLCore", version = "R1.0.12")
    api(group = "io.github.rossetti", name = "JSLExtensions", version = "R1.0.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}