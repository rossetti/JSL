package examplepkg

import ksl.process.ProcessContinuation
import ksl.process.ProcessCoroutine
import java.time.Instant
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume

fun main(){

    //val parse = Instant.parse("2020-12-25")
    //println(parse)

    val execResult: Result<Instant> = runCatching {
        Instant.parse("2020-12-25") // this fails
    }

    println(execResult.isSuccess)
    println(execResult.getOrNull())

   // println(execResult.getOrThrow())  // throws exception, no get available

    var c = object : Continuation<Unit>{
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            println("I'm here")
            TODO("Not yet implemented")
        }
    }
    //c.resume(Unit)

    var p = ProcessContinuation()

    p.resume(Unit)

    var pc = ProcessCoroutine()

   // pc.halt()


}

