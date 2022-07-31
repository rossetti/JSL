The Java Simulation Library (JSL) is a Java library for performing Monte Carlo and Discrete-Event
Dynamic System computer simulations.

This is a Gradle based project.

## JSL Book

https://rossetti.github.io/JSLBook/

## JSL Documentation

If you are looking for the JSL documentation you can find it here:

https://rossetti.github.io/JSLJavaDocs/

The repository for the documentation is here:

https://github.com/rossetti/JSLJavaDocs

Please be aware that the book and javadoc documentation may lag the releases due to lack of developer time.

## Gradle and Build Details

The JSL is organized as a multi-project gradle build.  There are two sub-projects:

JSLCore - the main simulation functionality, with a limited number of dependencies

JSLExtensions - an extension of the JSL that adds database, Excel and other functionality that has many open source dependencies

Additional projects are available for illustrating and other work related to the JSL.

JSExamples - a project that has example code that illustrates the JSLCore and JSLExtensions being used.

JSLExampleProject - a pre-configured project using gradle that is setup to use the JSLCore and JSLExtensions

JSLTesting - a separate project that does some very basic testing related to the JSL

JSLKotlin - very preliminary work on porting the JSL to Kotlin

To add the JSL to your gradle build files use the following artifact coordinates:

group = "io.github.rossetti"
name = "JSLCore"
version = "R1.0.12"

group = "io.github.rossetti"
name = "JSLExtensions"
version = "R1.0.12"

Of course, the version numbers may be different for additional releases. As an example, for kotlin DLS:

```gradle
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

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}
```

There is an example gradle project called JSLExampleProject that has the appropriate gradle build script that will get you started with a JSL project.

## Cloning and Setting Up a Project

If you are using IntelliJ, you can use its clone repository functionality to 
setup a working version. Or, simply download the repository and use IntelliJ to open up
the repository.  IntelliJ will recognize the JSL project as a gradle build and configure an appropriate project.

## Release Notes
Latest Release: R1.0.12
- Added jsl.controls package. This is the beginning effort to add annotated controls to models
  - controls allow input controls to be added to jsl modelelements to permit large scale experimenation and optimization
- Updated jsl.utilities.rvariable package to permit capturing of random variable parameters.
  - This facilitates large scale experiments and optimization
- Updated jsl.utilitities.random.mcintegration to make it easier to run monte-carlo experiments
- Updated JSLArrayUtil for additional array/matrix functions
- new BoxPlotSummary and TimeWeightedStatistic classes within jsl.utilities.statistics

Latest Release: R1.0.11
- Fixed computation of initialization bias test statistic
- Various work on random variate generation to improve BetaRV
- new HistogramB class that uses breakpoints, old Histogram class will likely be deprecated

Latest Release: R1.0.10
- Update dependency on Apache POI to 5.2.0 to remove log4j vulnerabilities

Latest Release: R1.0.9
- Updated random variable classes
- Update dependency on Apache POI to 5.0.0

Latest Release: R1.0.8
 - Removed deprecated JSLFXUtil, updated excel functionality, added tabular file support
 - Removed dependency on Apache Commons IO, reduced some dependency on Apache POI   

Latest Release: R1.0.7
	The project has been moved to GitHub and now available on maven central.

Latest Release: R1.0.6
	Release 1.0.6 is not backwards compatible. There are changes to the JSL class that are likely to cause users to need to update their code.