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
package jsl.utilities.math;

import java.util.Set;
import java.util.TreeSet;

/** Some basic set operations
 *
 * @author rossetti
 */
public class Sets {

    /** Use Google Guava Sets instead
     *
     * @param setA a set
     * @param setB another set
     * @param <T> the type in the set
     * @return a set representing the union
     */
    @Deprecated
    public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    /** Use Google Guava Sets instead
     *
     * @param setA a set
     * @param setB another set
     * @param <T> the type in the set
     * @return a set representing the intersection
     */
    @Deprecated
    public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<>();
        for (T x : setA) {
            if (setB.contains(x)) {
                tmp.add(x);
            }
        }
        return tmp;
    }

    /** Use Google Guava Sets instead
     *
     * @param setA a set
     * @param setB another set
     * @param <T> the type in the set
     * @return a set representing the set difference
     */
    @Deprecated
    public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new TreeSet<>(setA);
        tmp.removeAll(setB);
        return tmp;
    }

    /** Use Google Guava Sets instead
     *
     * @param setA a set
     * @param setB another set
     * @param <T> the type in the set
     * @return a set representing the symmetric difference
     */
    @Deprecated
    public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
        Set<T> tmpA;
        Set<T> tmpB;

        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }

    public static <T> boolean isSubset(Set<T> setA, Set<T> setB) {
        return setB.containsAll(setA);
    }

    public static <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
        return setA.containsAll(setB);
    }
}
