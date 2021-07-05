/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package ksl.utilities

/** An interface to defining the name of an object
 */
interface NameIfc {
    /**
     *
     * @return a string representing the name of the object
     */
    val name: String?
}

open class Name(override val name: String?) : NameIfc

/** An interface to defining the identity of an object in terms
 * of a name and a number
 */
interface IdentityIfc : NameIfc {
    /**
     *
     * @return an int representing the id of the object
     */
    val id: Int

    var label: String?
}

open class Identity(aName: String? = null) : IdentityIfc, NameIfc {

    companion object {
        private var IDCounter: Int = 0
    }

    override val id: Int = ++IDCounter

    override val name: String = aName ?:javaClass.simpleName + "#" + id

    override var label: String? = null

    override fun toString(): String {
        return "Identity(id=$id, name=$name, label=$label)"
    }

}

class Something: Identity(){

}


fun main() {
    val n1 = Identity("Manuel")
    val n2 = Identity("Joe")
    val n3 = Identity("Maria")
    val n4 = Identity()
    println(n1)
    println(n2)
    println(n3)
    println(n4)
}