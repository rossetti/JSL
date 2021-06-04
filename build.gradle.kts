plugins {
    // java
    `java-library`
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

version = "1.0-SNAPSHOT"

buildscript {
    repositories { jcenter() }

    dependencies {
        classpath("com.netflix.nebula:gradle-aggregate-javadocs-plugin:2.2.+")
    }
}

apply(plugin = "nebula-aggregate-javadocs")

repositories {
    jcenter()
    mavenCentral()
}

dependencies {

	api(project(":JSLCore"))
	api(project(":JSLExamples"))
	api(project(":JSLExtensions"))

    // include local jar files found in libs folder in the compile
    implementation(fileTree(baseDir = "libs"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

//tasks.jar {
//    manifest {
//        attributes(
//                "Implementation-Title" to project.name,
//                "Implementation-Version" to project.version
//        )
//    }
//    exclude("logback.xml")
//}

//tasks.shadowJar {
//    baseName = "INEG2214Libraries"
//    classifier = null
//    version = null
//}
