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

package jsl.utilities.statistic;

import java.util.*;
import java.util.stream.Collectors;

/** A statistical run is a sequence of objects that are determined equal based on
 *  a comparator.  A single item is a run of length 1.  A set of items that are all the
 *  same are considered a single run. The set (0, 1, 1, 1, 0) has 3 runs.
 *
 * @param <T> the type of object associated with the statistical run
 */
public class StatisticalRun<T> {

    private final int myStartingIndex;
    private final int myEndingIndex;
    private final T myStartingObject;
    private final T myEndingObject;

    public StatisticalRun(int startingIndex, int endingIndex, T startingObj, T endingObj) {
        if (startingIndex < 0){
            throw new IllegalArgumentException("The run's starting index must be >= 0");
        }
        if (endingIndex < startingIndex){
            throw new IllegalArgumentException("The run's ending index must be >= the starting index");
        }
        myStartingIndex = startingIndex;
        myEndingIndex = endingIndex;
        myStartingObject = startingObj;
        myEndingObject = endingObj;
    }

    /**
     *
     * @return the starting index of the run
     */
    public final int getStartingIndex() {
        return myStartingIndex;
    }

    /**
     *
     * @return the ending index of the run
     */
    public final int getEndingIndex() {
        return myEndingIndex;
    }

    /**
     *
     * @return the length of the run, the number of consecutive items in the run
     */
    public final int getLength(){
        return myEndingIndex - myStartingIndex + 1;
    }

    /**
     *
     * @return the object associated with the starting index
     */
    public final T getStartingObject() {
        return myStartingObject;
    }

    /**
     *
     * @return the object associated with the ending index
     */
    public final T getEndingObject() {
        return myEndingObject;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatisticalRun{");
        sb.append("myStartingIndex=").append(myStartingIndex);
        sb.append(", myEndingIndex=").append(myEndingIndex);
        sb.append(", myStartingObject=").append(myStartingObject);
        sb.append(", myEndingObject=").append(myEndingObject);
        sb.append('}');
        return sb.toString();
    }

    /**
     *
     * @param list A list holding a sequence of objects for comparison, must not be null
     * @param comparator a comparator to check for equality of the objects
     * @param <T> the type of objects being compared
     * @return the List of the runs in the order that they were determined.
     */
    public static <T> List<StatisticalRun<T>> findRuns(List<T> list, Comparator<T> comparator){
        Objects.requireNonNull(list, "The list cannot be null");
        Objects.requireNonNull(comparator, "The comparator cannot be null");
        List<StatisticalRun<T>> listOfRuns = new ArrayList<>();
        if (list.isEmpty()){
            return listOfRuns;
        }
        // list has at least 1
        // list has 1 element
        if (list.size() == 1){
            StatisticalRun<T> run = new StatisticalRun<>(0, 0, list.get(0), list.get(0));
            listOfRuns.add(run);
            return listOfRuns;
        }
        // list has at least 2 elements
        // starts at the same place
        int startIndex = 0;
        int endIndex = startIndex;
        T startingObj = list.get(startIndex);
        T endingObj = startingObj;
        boolean started = true;
        boolean ended = false;
//        System.out.println("start = " + startIndex + " end = " + endIndex + " so = " + startingObj + " eo = " + endingObj);
        for(int i=1; i<list.size(); i++){
            if (comparator.compare(startingObj, list.get(i))== 0){
                // they are the same, this continues a run, with same start index and same starting object
                endIndex = i;
                endingObj = list.get(i);
                started = true;
                ended = false;
            } else {
                // they are different, this ends a run
                StatisticalRun<T> run = new StatisticalRun<>(startIndex, endIndex, startingObj, endingObj);
                listOfRuns.add(run);
                startIndex = i;
                startingObj = list.get(i);
                endIndex = startIndex;
                endingObj = startingObj;
                started = false;
                ended = true;
            }
//            System.out.println("start = " + startIndex + " end = " + endIndex + " so = " + startingObj + " eo = " + endingObj);
        }
        if ((started == true) && (ended == false)){
            // a run has been started that hasn't ended, close it off
            StatisticalRun<T> run = new StatisticalRun<>(startIndex, endIndex, startingObj, endingObj);
            listOfRuns.add(run);
        }
        if (startIndex == endIndex){
            StatisticalRun<T> run = new StatisticalRun<>(startIndex, endIndex, startingObj, endingObj);
            listOfRuns.add(run);
        }
        return listOfRuns;
    }

    public static void main(String[] args) {

//        int data[] = {0, 0, 1, 1, 0};
//        int data[] = {1, 1, 1, 1, 1};
//        int data[] = {0, 0, 1, 1, 1};
//        int data[] = {1, 0, 1, 1, 1,0};
//        int data[] = {1};
//        int data[] = {1, 0};
        int data[] = {1, 1};
        List<Integer> test = Arrays.stream(data).boxed().collect(Collectors.toList());
        List<StatisticalRun<Integer>> runs = findRuns(test, Comparator.<Integer>naturalOrder());

        runs.forEach(System.out::println);

    }
}
