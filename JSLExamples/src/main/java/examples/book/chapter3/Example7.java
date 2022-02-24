package examples.book.chapter3;

import jsl.utilities.random.robj.DPopulation;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.ArrayList;
import java.util.List;

/**
 *  This example illustrates how to define a population of
 *  values (DPopulation) and use it to perform sampling operations
 *  such as random samples and permutations.  Similar functionality
 *  is also demonstrated by directly using the static methods of
 *  the JSLRandom class.
 */
public class Example7 {
    public static void main(String[] args) {
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
