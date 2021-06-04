The Java Simulation Library (JSL) is a Java library for performing Monte Carlo and Discrete-Event
Dynamic System computer simulations.

This is a Gradle based project.

## JSL Book

https://rossetti.git-pages.uark.edu/jslbookdownbook

## JSL Documentation

If you are looking for the JSL documentation you can find it here:

https://rossetti.git-pages.uark.edu/JSL-Documentation/

The repository for the documentation is here:

https://git.uark.edu/rossetti/JSL-Documentation

Please be aware that the book and javadoc documentation may lag the releases due to lack of developer time.

## Gradle and Build Details

The JSL is organized as a multi-project gradle build.  There are three sub-projects:

JSLCore - the main simulation functionality, with only dependency on SL4J

JSLExtensions - an extension of the JSL that adds database, Excel and other functionality that has many open source dependencies

JSExamples - a project that has example code that illustrates the JSLCore and JSLExtensions being used.

The current jar files are found [here](https://archiva.uark.edu/repository/jsl/edu/uark/jsl/). This is an archiva maven repository, 
which you can include in your gradle build via the maven closure. The build is organized as
three separate projects to allow the JSLCore to have the least amount of dependencies and 
thus the smallest jar footprint.

```gradle
repositories {
    maven {
        setUrl("https://archiva.uark.edu/repository/jsl")
    }
}
```

To add the JSL to your gradle build files use the following artifact coordinates:

group = "edu.uark.jsl"
name = "JSLCore"
version = "R1.0.6"

group = "edu.uark.jsl"
name = "JSLExamples"
version = "R1.0.6"

group = "edu.uark.jsl"
name = "JSLExtensions"
version = "R1.0.6"

Of course, the version numbers may be different for additional releases. As an example, for kotlin DLS:

```gradle
dependencies {

    // to include just JSLCore, uncomment the next line
//    api(group = "edu.uark.jsl", name = "JSLCore", version = "R1.0.6")

    // to include JSLExtensions and also JSLCore classes, uncomment the next line
//    api(group = "edu.uark.jsl", name = "JSLExtensions", version = "R1.0.6")

    // to include JSLExamples, JSLExtensions, and also JSLCore classes, uncomment the next line
    api(group = "edu.uark.jsl", name = "JSLExamples", version = "R1.0.6")

    testCompile("junit", "junit", "4.12")
}
```

There is an example gradle project called JSLExampleProject that has the appropriate gradle build script that will get you started with a JSL project.

## Cloning and Setting Up a Project

If you are using IntelliJ, you can use its clone repository functionality to 
setup a working version. Or, simply download the repository and use IntelliJ to open up
the repository.  IntelliJ will recognize the JSL project as a gradle build and configure an appropriate project.

## Release Notes

Latest Release: R1.0.6

Release 1.0.6 is not backwards compatible. There are changes to the JSL class that are likely to cause users to need to update their code.