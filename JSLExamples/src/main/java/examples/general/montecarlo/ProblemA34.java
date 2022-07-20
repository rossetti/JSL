package examples.general.montecarlo;

import jsl.utilities.random.rvariable.JSLRandom;

public class ProblemA34 {

    public static void main(String[] args) {

        for (int i = 1; i <= 5; i++) {
            System.out.println(generate(5.0, JSLRandom.rUniform()));
        }
    }


    static public double generate(double c, double u) {
        if ((u < 0.0) || (u > 1.0)) {
            throw new IllegalArgumentException("The value of u must be in [0,1]");
        }
        return c * (1.0 - Math.cbrt(1.0 - u));
    }

}
