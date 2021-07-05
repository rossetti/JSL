// JSLExamples

plugins {
    java
}

repositories {
    jcenter()
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

