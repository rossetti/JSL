// An example gradle build file for a project that depends on the JSL

plugins {
    `java-library`
}

repositories {
    maven {
        setUrl("https://archiva.uark.edu/repository/jsl")
    }
    jcenter()
    mavenCentral()
}

dependencies {

    // to include just JSLCore, uncomment the next line
//    api(group = "edu.uark.jsl", name = "JSLCore", version = "R1.0.6")

    // to include JSLExtensions and also JSLCore classes, uncomment the next line
//    api(group = "edu.uark.jsl", name = "JSLExtensions", version = "R1.0.6")

    // to include JSLExamples, JSLExtensions, and also JSLCore classes, uncomment the next line
    api(group = "edu.uark.jsl", name = "JSLExamples", version = "R1.0.6")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}