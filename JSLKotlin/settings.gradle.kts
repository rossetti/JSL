rootProject.name = "JSLKotlin"

include(":JSLCore")
include(":JSLExamples")
include(":JSLExtensions")

project(":JSLCore").projectDir = file("../JSLCore")
project(":JSLExamples").projectDir = file("../JSLExamples")
project(":JSLExtensions").projectDir = file("../JSLExtensions")