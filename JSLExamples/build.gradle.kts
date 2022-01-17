// JSLExamples

plugins {
    java
}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.addAll(listOf("-Xlint:unchecked"))
}

repositories {
//    jcenter()
    mavenCentral()
}

dependencies {

    // use multi-project build dependency to ensure changes to JSLCore and JSLExtension can be
    // immediately used within JSLExamples
	implementation(project(":JSLCore"))
    implementation(project(":JSLExtensions"))
}

configure<JavaPluginConvention> {
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
