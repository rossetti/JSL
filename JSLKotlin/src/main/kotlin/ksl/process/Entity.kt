package ksl.process

//import jsl.modeling.queue.QObject

fun main(){
    val e = Entity(0.0)
    println(e.creationTime)
    e.name = "Tom"
    println(e.name)
}

open class Entity(val creationTime: Double, var name : String? = null)  {

    inner class Process {

    }
}