package examples.book.chapter3;

import java.util.Arrays;
import java.util.List;

import static jsl.utilities.random.rvariable.JSLRandom.randomlySelect;

/**
 *  This example illustrates how to use the randomlySelect() method
 *  of the JSLRandom class to randomly select from a list.
 */
public class Example6 {
    public static void main(String[] args) {
        // create a list
        List<String> strings = Arrays.asList("A", "B", "C", "D");
        // randomly pick from the list, with equal probability
        for (int i = 1; i <= 5; i++) {
            System.out.println(randomlySelect(strings));
        }

    }
}
