package ksl.utilities.random.rng


fun main() {
    test()
    test1()
    test2()
}

fun test() {
    val rnp = RNStreamProvider()
    val defaultStream = rnp.nextRNStream()
    for (i in 1..3) {
        println("u = " + defaultStream.randU01())
    }
    val f = RNStreamProvider()
    val s1 = f.nextRNStream()
    println("default stream")
    for (i in 1..3) {
        println("u = " + s1.randU01())
    }
    s1.advanceToNextSubstream()
    println("advanced")
    for (i in 1..3) {
        println("u = " + s1.randU01())
    }
    s1.resetStartStream()
    println("reset")
    for (i in 1..3) {
        println("u = " + s1.randU01())
    }
    val s2 = f.nextRNStream()
    println("2nd stream")
    //TODO doesn't match JSL generator
    for (i in 1..3) {
        println("u = " + s2.randU01())
    }
}

fun test1() {
    val rm = RNStreamFactory()
    val rng = rm.nextStream()
    var sum = 0.0
    val n = 1000
    for (i in 1..n) {
        sum = sum + rng.randU01()
    }
    println("-----------------------------------------------------")
    println("This test program should print the number   490.9254839801")
    println("Actual test result = $sum")
    check(sum==490.9254839801)
}

fun test2() {
    // test the advancement of streams
    val count = 100
    val advance = 20
    val rm = RNStreamFactory()
    rm.advanceSeeds(advance)
    val rng = rm.nextStream()
    var sum = 0.0
    for (i in 1..count) {
        val x = rng.randU01()
        sum = sum + x
//        println("$i   $x   $sum")
    }
    println("-----------------------------------------------------")
    println("This test program should print the number   55.445704270784404")
    println("Actual test result = $sum")
    check(sum == 55.445704270784404){
        "didn't match"
    }
}
