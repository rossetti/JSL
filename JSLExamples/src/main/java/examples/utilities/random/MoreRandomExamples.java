/*
 * Copyright (c) 2019. Manuel D. Rossetti, rossetti@uark.edu
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

package examples.utilities.random;

import jsl.utilities.random.robj.DPopulation;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jsl.utilities.random.rvariable.JSLRandom.*;

public class MoreRandomExamples {

    public static void main(String[] args) {

        JSLRandomExamples();

        PermutationExamples();
    }

    public static void JSLRandomExamples(){
        // use import static jsl.utilities.random.rvariable.JSLRandom.*;
        // at the top of your java file
        double v = rUniform(10.0, 15.0); // generate a U(10, 15) value
        double x = rNormal(5.0, 2.0); // generate a Normal(mu=5.0, var= 2.0) value
        double n = rPoisson(4.0); //generate from a Poisson(mu=4.0) value
        System.out.printf("v = %f, x = %f, n = %f %n", v, x, n);
        System.out.println();

        // create a list
        List<String> strings = Arrays.asList("A", "B", "C", "D");
        // randomly pick from the list, with equal probability
        for (int i=1; i<=5; i++){
            System.out.println(randomlySelect(strings));
        }

    }

    public static void PermutationExamples(){

        // create an array to hold a population of values
        double[] y = new double[10];
        for (int i = 0; i < 10; i++) {
            y[i] = i + 1;
        }

        // create the population
        DPopulation p = new DPopulation(y);
        System.out.println(p);

        // permute the population
        p.permute();
        System.out.println(p);

        // directly permute the array using JSLRandom
        System.out.println("Permuting y");
        JSLRandom.permutation(y);
        System.out.println(DPopulation.toString(y));

        // sample from the population
        double[] x = p.sample(5);
        System.out.println("Sampling 5 from the population");
        System.out.println(DPopulation.toString(x));

        // create a string list and permute it
        List<String> strList = new ArrayList<>();
        strList.add("a");
        strList.add("b");
        strList.add("c");
        strList.add("d");
        System.out.println(strList);
        JSLRandom.permutation(strList);
        System.out.println(strList);
    }
}
