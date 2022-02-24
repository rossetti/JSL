package examples.general.utilities.random;

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.*;

public class GenerateRVExamples {

    public static void main(String[] args) {
        System.out.println();
       // NormalRVExample2();
    }


    public static void NormalRVExample2(){
        // create distribution
        Normal n = new Normal(20.0, 4.0);
        // get a random variable
        RVariableIfc rv1 = n.getRandomVariable();
        // change the parameters of the distribution
        n.setMean(100.0);
        n.setVariance(20.0);
        // get another random variable
        RVariableIfc rv2 = n.getRandomVariable();
        System.out.printf("%3s %15s %15s %n", "n", "rv1", "rv2");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%3d %15f %15f %n", i+1, rv1.getValue(), rv2.getValue());
        }
    }

}
