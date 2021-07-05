package ksl.process

import kotlin.coroutines.intrinsics.*
import jsl.simulation.JSLEvent
import kotlin.coroutines.*

// just place holders
//class Entity {} // entities are supposed to experience processes
class Resource {} // entities may use resources during their processes, must have some kind of queueing
class Task {} // a task is what the entity asks resources to do during the process
class Signal {} // represents a signal to hold a process for, must have some kind of queue

@RestrictsSuspension
interface ProcessScope {

    /**
     *  Activates the process. Causes the process to be scheduled to start at the present time or some time
     *  into the future. This schedules an event
     *
     *  @param atTime the time into the future at which the process should be activated (started) for
     *  the supplied entity
     *  @param priority used to indicate priority of activation if there are activations at the same time.
     *  Lower priority goes first.
     *  @return JSLEvent the event used to schedule the activation
     */
    fun activate(atTime: Double = 0.0, priority: Int = JSLEvent.DEFAULT_PRIORITY) : JSLEvent<Entity>
// maybe activate should take in a process and not be in this scope?

    /**
     *  Suspends the execution of the process
     */
    suspend fun suspend()

    /**
     *  Resumes the process after it was halted (suspended).
     */
    suspend fun resume() //TODO I don't think it needs to be a suspending function

    /**
     *  Causes the process to halt, waiting for the signal to be on.  If the signal if off, when the process
     *  executes this method, it should halt until the signal becomes on. If the signal is on, when the process
     *  executes this method, the process simply continues executing.
     *
     *  @param signal a general on/off indicator for controlling the process
     *  @param priority a priority indicator to inform ordering when there is more than one process waiting for
     *  the same signal
     */
    suspend fun waitFor(signal: Signal, priority: Int = JSLEvent.DEFAULT_PRIORITY)

    /**
     *  Requests a number of units of the indicated resource.
     *
     *  @param numRequested the number of units of the resource needed for the request.
     *   The default is 1 unit.
     *  @param resource the resource from which the units are being requested.
     *  @param taskTime the amount of time associated with the request. By default, this is infinite. The task time
     *  may be used to inform any allocation mechanism for requests that may be competing for the resource.
     *  @param priority the priority of the request. This is meant to inform any allocation mechanism for
     *  requests that may be competing for the resource.
     *  @return the Task representing the request for the Resource. After returning, the task indicates that the units
     *  of the resource have been allocated to the entity making the request. A task should not be returned until
     *  all requested units of the resource have been allocated.
     */
    suspend fun seize( resource: Resource, numRequested: Int = 1,
                        taskTime: Double = Double.POSITIVE_INFINITY,
                        priority: Int = JSLEvent.DEFAULT_PRIORITY) : Task

    /**
     *  Causes the process to delay (suspend execution) for the specified amount of time.
     *
     *  @param time, the length of time required before the process continues executing, must not be negative and
     *  must be finite.
     *  @param priority, since the delay is scheduled, a priority can be used to determine the order of events for
     *  delays that might be scheduled to complete at the same time.
     */
    suspend fun delay(time: Double, priority: Int = JSLEvent.DEFAULT_PRIORITY)

    /**
     *  Releases a number of units of the indicated resource.
     *
     *
     *  @param numReleased the number of units of the resource needed for the request.
     *   The default is 1 unit. Cannot be more than the number of units
     *  @param resource the resource from which the units are being requested
     */
    suspend fun release(resource: Resource, numReleased: Int = 1) //TODO I don't think it needs to be a suspending function

    /**
     *  A method for delaying with a task time.
     *
     *  @param task The supplied task must have a finite task time.
     */
    suspend fun work(task: Task) //TODO not sure if work should be a suspending function of task
    suspend fun complete(task: Task) //TODO not sure if complete should be method on task and if it should be suspending

}

internal open class ProcessContinuation : Continuation<Unit> {
    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        //not sure what to do with this
        println("before result.getOrThrow()")
        result.getOrThrow()
        println("after result.getOrThrow()")
        //TODO("Not yet implemented")
    }
}

// need to be able to just create the coroutine
/* issues:
    how to schedule events
    how to capture/resume the continuation
    clearly a process can only have one suspension point "suspended" at time
    maybe a Process should be a model element that uses a ProcessCoroutine and
    delegates the suspend/resume work to it
    maybe we should just start with the basic suspend/resume primitive
    within a model element a process builder should be used
 */

internal class ProcessCoroutine : ProcessScope, ProcessContinuation() {
    lateinit var continuation : Continuation<Unit>

    override fun activate(atTime: Double, priority: Int): JSLEvent<Entity> {
        TODO("Not yet implemented")
    }

    override suspend fun resume() {
        // what to do if the process is not suspended
        continuation.resume(Unit)
        //TODO("Not yet implemented")
    }

//    override suspend fun halt() {
//       // return suspendCoroutineUninterceptedOrReturn { cont -> COROUTINE_SUSPENDED }
//
//        TODO("Not yet implemented")
//    }

    override suspend fun suspend() {
        return suspendCoroutineUninterceptedOrReturn { cont ->
            continuation = cont
            COROUTINE_SUSPENDED }
    }

//    override suspend fun halt() = suspendCoroutineUninterceptedOrReturn {
//        cont: Continuation<Unit> -> COROUTINE_SUSPENDED
//    }

    override suspend fun waitFor(signal: Signal, priority: Int) {
        // if signal is on/true then just return
        // if signal is off/false then suspend
        TODO("Not yet implemented")
    }

    override suspend fun seize(resource: Resource, numRequested: Int, taskTime: Double, priority: Int): Task {
        // if the request/task has been allocated then just return
        // otherwise suspend
        TODO("Not yet implemented")
    }

    override suspend fun delay(time: Double, priority: Int) {
        // if time < 0 throw error
        // if time = 0 don't delay, just return
        // if time > 0, then schedule a resume after the delay, and then suspend
        // need to think about what happens if the event associated with this delay is cancelled
        TODO("Not yet implemented")
    }

    //TODO consider scheduleResumeAfterDelay()
    // https://github.com/Kotlin/kotlinx.coroutines/blob/3cb61fc44bec51f85abde11f83bc5f556e5e313a/kotlinx-coroutines-core/common/src/Delay.kt

    override suspend fun release(resource: Resource, numReleased: Int) {
        // this is not really a suspending function
        TODO("Not yet implemented")
    }

    override suspend fun work(task: Task) {
        TODO("Not yet implemented")
    }

    override suspend fun complete(task: Task) {
        TODO("Not yet implemented")
    }

}