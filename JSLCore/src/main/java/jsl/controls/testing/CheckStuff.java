package jsl.controls.testing;

import jsl.utilities.JSLArrayUtil;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CheckStuff {

    public static void main(String[] args) {

        int s = 0;
        int n = 10;
        double[] reps = IntStream.range(s, n + s)
                .mapToDouble(x -> (double) x).toArray();

        System.out.println((Arrays.toString(JSLArrayUtil.toString(reps))));

//        Class<?> xClass = java.lang.Long.class;
//        Class<?> yClass = java.lang.Long.class;
//        if (xClass.isAssignableFrom(yClass)){
//            System.out.println("isInstance true");
//        } else {
//            System.out.println("isInstance false");
//        }
    }
}
